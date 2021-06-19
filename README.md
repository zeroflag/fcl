# FCL - Forth Calculator's Language

FCL is the programming language of an Android app called Forth Calculator. It is a Forth dialect with optional local variables, complex data structures, quotations and Java interoperability.

```forth
: fib ( n1 n2 -- n1 n2 n3 ) 2dup + ;

: nfib ( n -- .. ) -> n ( local variable )
  0 1 { fib } n times ; ( quotation )
```

Besides all the high-level features, FCL supports the traditional Forth programming structures and uses the same compilation model (compile/interpret mode, dictionary, immediate words, etc.) as traditional Forth systems.

## The Syntax

## Locals

## Maps

## List

## HTTP
