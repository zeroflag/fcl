# FCL - Forth Calculator's Language

FCL is the programming language of an Android app called [Forth Calculator](https://play.google.com/store/apps/details?id=com.vectron.forthcalc.pro). It is a Forth dialect with optional local variables, complex data structures, quotations and Java interoperability.

<img align="right" src="/img/screenshot1.png" width="180">

Watch this [demo](https://www.youtube.com/watch?v=9rf8Y_lwj3g) to see it in action.


```forth
\ calculates the first n+2 elements of the fibonacci sequence
: fib ( n -- n* ) -> n     ( local variable )
  0 1 { 2dup + } n times ; ( quotation )
```

Besides all the high-level features, FCL supports the traditional Forth programming structures and uses the same compilation model (compile/interpret mode, dictionary, immediate words, etc.) as a classic Forth.

## The Syntax

The syntax is a superset of the Forth language. In FCL there are literal syntax for creaing Lists `[ 1 2 3 ]`, Maps `#[ 'key' 'value' ]#`, Quotations `{ dup + }`,  Strings `'Hello World'`, Ranges `1 10 ..` and Symbols `:symbol`. But many other things are the same as in a classic Forth system.

## Low-level control structures

FCL supports the traditional Forth conditionals (`if` and `case`) and loops (`do`, `while`, `until`). These are immediate words whose compilation semantics are to append the proper JUMP primitives to the current definition. FCL compiles high level threaded code, where execution tokens are method references of host language (Java).

General form of `if else then`.

```forth
<bool> if <consequent> else <alternative> then
```

For example:
```forth
: max ( n n -- n )
  2dup < if nip else drop then ;

10 100 max . \ prints 100
```

The `else` part is optional.

```forth
: abs ( n -- n ) 
  dup 0 < if -1 * then ;

-10 abs . \ prints 10
```

#### Case statement

FCL supports switch-case like flow control logic as shown in the following example.

```forth
: day ( n -- s )
  case
    1 of 'Monday' endof
    2 of 'Tuesday' endof
    3 of 'Wednesday' endof
    4 of 'Thursday' endof
    5 of 'Friday' endof
    6 of 'Saturday' endof
    7 of 'Sunday' endof
    drop 'Unknown'
  endcase ;
````

#### Count-controlled loops

The `limit` and `start` before the word `do` defines the number of times the loop will run.

```forth
<limit> <start> do <loop-body> loop
```

*DO* loops iterate through integers by starting at *start* and incrementing until you reach the *limit*. The word *i* pushes the loop index onto the stack. In a nested loop, the inner loop may access the loop variable of the outer loop by using the word *j*.

For example:
```forth
5 0 do i . loop \ prints 0 1 2 3 4
```

It is important to understand the implementation details of this loop. `DO` loops store the loop index on the return stack. You can break the semantics of *i* and *j* if you use the return stack to store temporary data. Exiting from the loop requires clearing up the return stack by using the `unloop` word.

#### Condition-controlled loops

##### until loop

```forth
begin <loop-body> <bool> until
```
The *begin*...*until* loop repeats until a condition is true. This loop always executes at least one time.

For example:

```forth
: countdown ( n -- )
  begin
    dup . 
    1- 
    dup 0 < 
  until
  drop ;

5 countdown \ prints 5 4 3 2 1 0
```

##### while loop

```forth
begin <bool-exp> while <loop-body> repeat
```
For example:
```forth
: countdown ( n -- )
  begin
    dup 0 >=
  while
    dup . 1-
  repeat
  drop ;

5 countdown \ prints 5 4 3 2 1 0
```

Control structres are compile time words with no interpretation semantics.

## Locals

```
: example ( a b -- n )
  -> b -> a 42 -> c 0 => d
  a b + c * d !
  d @ ;
```

You can load the top of the stack to a local by either using `->` or `=>`. The name after the arrow denotes the name of the local and its value comes from the data stack.

There are two types of locals in FCL. Local constant `->` and local variable `=>`.

`-> a` loads the top of the stack into the local, called `a`.

Using `a` anywhere inside the word will push the value of the local.

`=> b` loads the top of the stack into the local variable, called `b`.

`b` pushes the address of the local. `b @` pushes the value of the local. You can use the `!` word to change the value of the local variable.

The `->` and `=>` words can be used anywhere within a word, including loop bodies and quotations. You can initialize a local (`0 -> a`) within the word or use the data that was supplied on the call site (`-> a`).

The locals are only accessible by the current word or a quotation which was defined within the word.

For example, here we load first parameter into `n` and initialize a local varialbe, called `count` to zero.

```
: count-even ( n -- c )
  -> n 0 => count
  n 0 do
    i 2 /mod -> quotient -> remainder
    remainder 0 = if
      count inc
    then
  loop
  count @ ;
```

In the loop body we use two more locals to name the output of the `/mod` which returns both the `quotient` and the `remainder` of a divide operation. We keep updating the `count` and in the end, we return its value.

### Implementation notes

Local variable support is [implemented](src/main/res/raw/locals.forth) in FCL itself. Locals are stored in a parameter stack. Both `->` and `=>` are immediate parsing words. They have both runtime and compilation semantics. They compile an inline *lookup word* within the enclosing word. The lookup word is removed from the dictionary after the compilation is finished.

At runtime they load the top of the stack into the proper location of the parameter stack which is associated to the current local.

The *lookup word* gets the value (or the address) from the parameter stack and pushes it onto the data stack. The `exit` and `;` words are redefined so that they unwind the parameter stack at return.

## Quotations

```forth
{ dup * } \ creates a quotation
```

A quotations is an anonymous word that contain a snippet of code and its evaluation is delayed until it is called by the word `yield`.

```forth
{ 'hello world' . } \ quotation pushes its address (plus a parameter stack adddress) to the data stack
yield               \ calls the quotation
```

```forth
{ 'hello' . } 10 times
```

A quotation can access to local variables of the enclosing word and have its own local variables as well.

```forth

: tst ( -- n ) 
  0 => sum
  [ 1 2 3 4 5 ] { -> item sum @ item + sum ! } each 
  sum @ ;
```

Here the quotation is called by the word `each` not by `tst`. It can still access to `sum` as it maintains the same stack frame as `tst`.

Local variables are lexically scoped. If the quotation is called by another word, the `sum` still denotes the variable that was defined in the quotation's context.

Quotations don't act as lexical closures however. The parameter stack is unwinded after the enclosing function is returned.

### Implementation notes

The quotation code is compiled into the enclosing word and bypassed by a jump. At runtime the quotation pushes its address as well as a stack frame to the stack. The word `yield` calls the address like a normal word and sets the parameter stack pointer to point to the quotation's stack frame. Quotations are [implemented](src/main/res/raw/quotations.forth) in fcl.

## Strings

Strings are surrounded by single quotes and they're immutable. For example `'Hello world'`.

```forth
'hello world' 0 5 substr \ gets the characters from 0 to 5 (exclusive)
```

```forth
'hello world' 1 at \ gets the first character (as a string)
```

```forth
'hello' upper \ gets the upper case version of hello
```

```forth
'HELLO' lower \ gets the lower case version of hello
```

```forth
'  xx  ' trim \ removes the leading and trailing spaces from the string 
```

```forth
'abcd' 'bc' index-of \ finds the substring and returns its index, or -1 if not found
```

```forth
'abcxxdepkcxxk' 'xx' 'yyy' replace \ replace all occurances of 'xx' to 'yyy (the substring is a regexp)
```

```forth
'af' '123' concat \ concatenates two strings
```

```forth
'hello world' ' ' split \ splits the strings into parts by the separator string
```

```forth
123 >str \ conversts the number into a string
```

```forth
"[ 1 'xx' ] 'a=%d b=%s' format" \ creates a string using the given format by substituting the parts in place of the format characters. 
```

```forth
'ab' 3 * \ copies the string n times
```


## List

A list is a dynamic, ordered data structed. `[` and `]` are Forth words, so a whitespace between them and the elements are significant.

```forth
<list> \ creates a new empty list
```

```forth
<list> dup 1 add \ creates an empty list and adds 1 to it
```

```forth
[ 1 2 3 ] \ creates a list with 3 elements
```

```forth
[ 1 2 3 ] peel \ unloads the items from the list to the data stack
```
  
```forth
1 2 3 4 5 >list* \ creates a new list and loads all items from the stack into it
```
  
```forth
[ 1 2 3 ] 0 at \ returns the first item of the list
```

```forth
[ 1 2 3 ] 0 remove-at \ removes the first item from the list
```
  
```forth
[ 1 'abc' 3 ] 'abc' remove \ removes 'abc' from the list
```

```forth
[ 1 'abc' 3 ] index-of \ returns the index of 'abc'
```  
 
```forth
[ 1 2 ] [ 3 4 ] concat \ creates a new list with the first concatenated to the second
```  
  
```forth
[ 1 2 3 4 ] 1 3 sublst \ gets a sublist from the original from 1 (inclusive) to 3 (exclusive)
```  
  
```forth
[ 1 2 3 ] 2 * \ basic arithmetic with scalars work with lists, this will multiple each item with 2.
```

```forth
[ 1 2 3 ] minl \ selects the smallest item from the list
```

```forth
[ 1 2 3 ] maxl \ selects the largest item from the list
```

  
### Implementation notes

Lists are java.util.ArrayList instances and garbage collected automatically by the host language.

## Maps

Maps contain key value pairs.


```forth
<map> \ creates a new empty map
```

```forth
<map> dup 'key1' 'value1' put \ creates an empty map and puts *'key1' => 'value1'* into it.
```

```forth
'key1' 'value1' >map* \ creates a new map and loads all items from the stack into it. There must be an even number of items on the stack.
```

```forth
#[ 'a' 1 'b' 2 ]# peel# \ unloads all items from the map to the stack
```


```forth
#[ 'key1' 'value1' ]# \ creates a map with key and value
```

```forth
#[ 'a' 1 'b' 2 ]# keys \ returns the keys from the map
```

```forth
#[ 'a' 1 'b' 2 ]# values \ returns the values from the map
```

```forth
#[ 'a' 1 'b' 2 ]# 'b' remove \ removes 'b'->2 from the map
```

## Implementation notes

Lists are java.util.LinkedHashMap instances and garbage collected automatically by the host language.

## Collection operations

```forth
\ iterets through the list and calls the quotation on each element
[ 1 2 3 4 ] { . } each 
```

```forth
\ selects only the odd items from the list [ 1 3 ]
[ 1 2 3 4 ] { odd? } filter 
```

```forth
\ transforms the list to a new list that contain the squares of the original items [ 1 4 9 16 ]
[ 1 2 3 4 ] { dup * } map
```

```forth
1 5 .. \ creates a range from 1 to 5
```

```forth
10 1 -2 ... \ creates a range from 10 downto 2 by 2
```


```forth
\ adds 5 to the end of the list
[ 1 2 3 4 ] dup 5 add
```


```forth
\ prepends 0 to the beginning of the list
[ 1 2 3 4 ] dup 0 prep
```

```forth
[ 1 2 3 ] size \ gets the size of the list
```

```forth
[ 1 2 3 4 ] 2 at \ gets the second item of the list ( indexing is zero based )
```

```forth
alist clear \ removes all items from the list
```

## High level control structures

Quotations combined with the collection API offers some high level control structures.

```forth
1 10 .. { dup * . } each
```

## HTTP

There is basic support getting an url or posting a form.

```forth
http-get  ( url -- response-body http-code )
http-post ( map url -- response-body http-code )
http-put  ( map url -- response-body http-code )
```

For example:

```forth
'http://some.url' http-get

#[ 'year'     year
   'month'    month
   'day'      day
   'cost'     1234 ]#
'http://myhost.site/expense' http-post 
```

You can also specify HTTP headers by adding a `headers` key to the map. In this case you should put the body under the `content` key.

```forth
#[
  'headers' #[ <key1> <value2> <key2> <value2> ]#
  'content' #[ .. ]#
]#
```

For example this will set the content type header to json and the body will be sent as a JSON object.


```forth
#[
  'headers' #[ 'Content-Type' 'application/json' ]
  'content' #[ 'on' true 'xy' [ 0.3 0.4 ] 'transitiontime' 10 ]#
]#
```

A shorter way to do the same is to use `+json-type` on the map.

```forth
#[ 'on' true 'xy' [ 0.3 0.4 ] 'transitiontime' 10 ]# +json-type
```


### Examples

#### Controlling Philips Hue lights


```forth
: hue ( x y msec -- )
  10 / round -> t -> y -> x
  #[ 
     'on' true 
     'xy' [ x y ] 
     'bri' 254 
     'transitiontime' t ( centiseconds ) 
   ]# 
   +json-type 'http://<bridgeip>/api/<apikey>/groups/1/action' http-put ;
```
#### Controlling a Daikin Air Conditioner

```forth
: cool ( temperature -- response http-code )
  -> tp
  #[
    'pow'    tp 0 = if 0 else 1 then
    'f_rate' 'A' ( auto fan )
    'stemp'  tp 24 min 20 max
    'mode'   3   ( cooling )
    'f_dir'  0   ( fan direction )
    'shum'   0
  ]# 
  'http://192.168.0.25/aircon/set_control_info'
  http-post ;
```

```forth
: ac? ( -- map )
  <map> -> out
  'http://192.168.0.25/aircon/get_control_info' http-get 
  dup 200 = if
    drop ',' split 
    { '=' split dup size 2 = if out swap add else drop then } 
    each
    out
  then ;
```

## UDP

```forth
udp-send-byte ( host port byte -- n )
udp-send-str ( host port str -- n )
udp-send-lst ( host port lst -- n )
```

All of the above words return an error code where 0 indicates success, -1 indicates unkonw host, -2 indicates network error.

### Examples

```forth
'192.168.0.10' 8070 'A' udp-send-str \ send letter 'A' via UDP to 192.168.0.10:8070

'192.168.0.10' 8070 65 udp-send-byte \ same as above but the payload is a single byte

'192.168.0.10' 8070 [ 65 ] udp-send-lst \ same as above the payload is a list
```

## Misc

### Tone generator

```forth
tone ( hz ms -- )
```

The `tone` word is for generating a tone with a given frequency and duration.

### Camera LED

```forth
torch ( n -- n )
```

`torch` toggles the built-in camera LED. 

```forth
1 torch .
500 ms
0 torch .
```

This will turn on the LED for 500 milliseconds.

# CLI

FCL is designed to run by Forth Calculator on Android, but if you want to try out something on PC you can start the REPL in the command line or evaluate a script.

Starting the interactive REPL mode:

```bash
gradle repl --console plain
```

Executing a single `.fcl` file

```bash
gradle eval_file -Pfile=<file.fcl> --console plain
```

Note that some parts depend on the Android API and are not available when running on a regular computer.