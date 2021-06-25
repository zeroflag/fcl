: fib ( n1 n2 -- n1 n2 n3 ) 2dup + ;

: nfib ( n -- .. ) -> n
  0 1 { fib } n times ;