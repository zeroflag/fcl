# FCL - Forth Calculator's Language

FCL is the programming language of an Android app called Forth Calculator. It is a Forth dialect with optional local variables, complex data structures, quotations and Java interoperability.

```forth
: fib ( n1 n2 -- n1 n2 n3 ) 2dup + ;

: nfib ( n -- .. ) -> n ( local variable )
  0 1 { fib } n times ; ( quotation )
```

Besides all the high-level features, FCL supports the traditional Forth programming structures and uses the same compilation model (compile/interpret mode, dictionary, immediate words, etc.) as traditional Forth systems.

## The Syntax

The syntax is a superset of the Forth language. In FCL there are literal syntax for creaing Lists `[ 1 2 3 ]`, Maps `#[ 'key' 'value' ]#`, Quotations `{ dup + }` and Strings `'Hello World'`.

## Control structures

FCL supports the traditional Forth conditional and loop control structures.

General form of `if else then`.

```forth
<bool> if <consequent> else <alternative> then
```

For example:
```forth
: max ( a b -- max )
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
: day ( n -- )
  case
    1 of print: 'Monday' endof
    2 of print: 'Tuesday' endof
    3 of print: 'Wednesday' endof
    4 of print: 'Thursday' endof
    5 of print: 'Friday' endof
    6 of print: 'Saturday' endof
    7 of print: 'Sunday' endof
    drop 'Unknown'
  endcase ;
````

#### Count-controlled loops

The `limit` and `start` before the word `do` defines the number of times the loop will run.

```forth
<limit> <start> do <loop-body> loop
```

*Do* loops iterate through integers by starting at *start* and incrementing until you reach the *limit*. The word *i* pushes the loop index onto the stack. In a nested loop, the inner loop may access the loop variable of the outer loop by using the word *j*.

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
    1- dup
    0 < 
  until
  drop ;

5 countdown \ prints 5 4 3 2 1 0
```

##### while loop

```forth
begin .. <bool> while <loop-body> repeat
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

There are two types of locals in FCL. Local constant `->` and local variable `=>`.

`-> a` loads the top of the stack into the local, called `a`.

`a` pushes the value of the local.

`=> b` loads the top of the stack into the local variable, called `b`.

`b` pushes the reference of the local. `b @` pushes the value of the local.

The `->` and `=>` words can be used anywhere within a word, including loop bodies and quotations. You can initialize a local (`0 -> a`) within the word or use the data that was supplied on the call site (`-> a`).

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

### Implementation notes

Local variable support is implemented in FCL itself. Locals are stored in a parameter stack. Both `->` and `=>` are immediate parsing words. They have both runtime and compilation semantics. They compile an inline *lookup word* within the enclosing word. At runtime they load the top of the stack into the proper location of the parameter stack. The *lookup word* gets the value or the reference from the parameter stack and pushes it onto the data stack.

## List


`<list>` \ creates a new empty list

`<list> dup 1 add` \ creates an empty list and adds *1* to it.

`[ 1 2 3 ]` \ creates a list with 3 elements.


## Maps


`<map>` \ creates a new empty map

`<map> dup 'key1' 'value1' put` \ creates an empty map and puts *'key1' => 'value1'* into it.

`#[ 'key1' 'value1' ]#` \ same as above

## Quotations

`{ dup * }` \ creates a quotation

## HTTP
