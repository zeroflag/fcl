( Local variable support:
    Syntax:
        : example -> name1 => name2
            name1 name2 @ + ;
        1 2 example

    Implementation:
        Local variables are stored on a parameter stack.
        At the first occurrence of -> or => a stack frame is allocated [8 bytes] on the pstack.
        Variables are moved from the data stack to the stack frame.
        Lookup words are compiled inside the word body. A lookup word gets the local var from the
        stack frame via index.
)

var: #loc                       ( number of local variables per word, used in compile time )
var: psp                        ( top of the parameter stack, each allocation adds max#loc to this )
var: qpsp                       ( parameter stack pointer used by quotations )
var: frame.allocated            ( compile time variable for checking if a frame was already allocated )
var: q.count                    ( compile time counter for quotations, nested into each other )
var: ldp

6    val:   look-word-size      ( compiled size of one lookup word )
8    val:   max#loc             ( maximum number of local variables per word )
1024 val:   ps.size             ( max pstack size )
ps.size     allot val: pstack   ( parameter stack for storing the locals )
max#loc     allot val: names    ( names of the local variables )

max#loc look-word-size *
allot val: scratch              ( scratch area for compiling temporary lookup words )

0     q.count !
false frame.allocated !

: full.check ( -- )  psp @ ps.size >= if 'pstack overflow' abort then ;
: empty.check ( -- ) psp @ 0 <= if 'pstack underflow' abort then ;

: frame.top   ( -- a ) pstack psp  @ + ;
: frame.top.q ( -- a ) pstack qpsp @ + ; ( for quotations we use the psp of the enclosing word )

: check# ( -- ) #loc @ max#loc >= if 'Too many local variables' abort then ;
: >names ( s -- )
    #loc @ 0 do
        dup names i + @ = if 'local already exists' abort then
    loop
    names #loc @ + ! ;

: frame.alloc ( -- )
    psp @ max#loc + psp ! ( we don't know how many #loc-s needed until ;, lets allocate max#loc )
    full.check ;

: frame.drop ( -- )
    empty.check
    max#loc 0 do jvm-null frame.top i - ! loop ( null out everything so that jvm gc can collect )
    psp @ max#loc - psp ! ;                    ( drop the stack frame )

: (frame.top)
    q.count @ 0 > if
        ['] frame.top.q ,
    else
        ['] frame.top   ,
    then ;

: lookup ( type index -- )
    (frame.top)
    ['] lit       ,  ( index )  ,
    ['] -         ,
    1 = if ['] @  , then ;

: postpone: ` , ; immediate

: local ( n -- )
    check#
    frame.allocated @ not if        ( is this the first local? )
        true frame.allocated !
        ['] frame.alloc ,           ( alloc new stack frame for max#loc )
        scratch ldp !
    then
    (frame.top)
    ['] lit       ,  #loc @  ,      ( local index )
    ['] -         ,  ['] !   ,      ( move local to from data stack to the stack frame )
    here                            ( save this dp and restore it after the lookup word is compiled to the scratch area )
    word dup >names                 ( store lookup word name )
    ldp @ dp!                       ( set dp to scratch area )
    create postpone: immediate      ( compile immediate lookup word  )
        ['] lit       ,  swap ,     ( type 1=val 0=var )
        ['] lit       ,  #loc @  ,  ( local index within the frame )
        ['] lookup    ,
        ['] exit      ,
    here ldp @ - look-word-size != if 'Invalid lookup word size' abort then
    #loc inc
    here ldp !
    dp! ;

: -> immediate 1 local ;
: => immediate 0 local ;

: unwind frame.allocated @ if ['] frame.drop , then ;

: exit immediate unwind exit.prim @ , ;

: ; immediate override
    ( runtime )
        unwind
        exit.prim @ ,
        nil ,
    ( compile time )
        #loc @ 0 do
            names i + @ delword
            jvm-null names i + !
        loop
        0 #loc !
        false frame.allocated !
        interpret
        reveal ;
