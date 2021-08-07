: nip swap drop ;
: over >r dup r> swap ;
: 2dup over over ;
: 2drop drop drop ;
: rot >r swap r> swap ;
: -rot swap >r swap r> ;
: 2swap rot >r rot r> ;
: 2rot swap >r >r 2swap r> r> swap 2swap ;
: -2rot 2rot 2rot ;
: 2over 2swap 2dup -2rot ;
: tuck swap over ;
: != = not ;
: >= < not ;
: <= swap < not ;
: > swap < ;
: 1+ 1 + ;
: 1- 1 - ;
: inc dup @ 1+ swap ! ;
: dec dup @ 1- swap ! ;
: (dummy) here 0 , ;
: resolve here over - swap ! ;
: (offset) here - , ;
: if immediate ['] jmp#f , (dummy) ;
: else immediate ['] jmp , (dummy) swap resolve ;
: then immediate resolve ;
: begin immediate here ;
: again immediate ['] jmp , (offset) ;
: while immediate ['] jmp#f , (dummy) ;
: repeat immediate swap ['] jmp , (offset)  resolve ;
: until immediate ['] jmp#f , (offset) ;
: do immediate ['] 2dup , ['] swap , ['] >r , ['] >r , ['] > , ['] jmp#f , (dummy) here ;
: unloop r> r> r> 2drop >r ;
: loop immediate
    ['] r> , ['] 1+ , ['] >r ,
    ['] i , ['] j , ['] >= ,
    ['] jmp#f , (offset)
    resolve
    ['] unloop , ;
: crlf? dup 10 = swap 13 = or ;
: ( begin key dup 41 = swap -1 = or until ; immediate
: \ begin key dup crlf? swap -1 = or until ; immediate

: case ( -- branch-counter ) immediate 0 ;

: of immediate ['] over , ['] = , ['] jmp#f , (dummy) ['] drop , ;

: endof immediate
    swap 1+ swap                            ( inc number of branches )
    ['] jmp , (dummy) swap
    resolve
    swap ;                                  ( keep branch counter at TOS )

: endcase ( #branches #branchesi*a -- ) immediate 0 do resolve loop ;

var: exit.prim ( primitive exit - in case some word needs to override )
` exit exit.prim !

: unimplemented 'Uninitialized deferred word' abort ;
: defer: word create
    ['] lit   ,  ['] unimplemented  ,
    ['] exec  ,  ['] exit           , ;

: is: ( xt -- ) ` :longValue jvm-call-method 1+ ! ;