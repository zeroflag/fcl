: <q> ( adr psp -- c ) :com.vectron.fcl.types.Quot/create/ii jvm-call-static ;
: qt.adr ( q -- a ) :address jvm-call-method ;
: qt.psp ( q -- a ) :stackFrame jvm-call-method ;

: (psp)
    q.count @ 1 > if
        ['] qpsp , ['] @ ,      ( nested quotation use its q.psp )
    else
        ['] psp  , ['] @ ,      ( non nested quotation use the psp of the enclosing word )
    then ;

: { immediate
    q.count inc
    ['] lit , here 6 + ,        ( beginning of the quotation )
    (psp)
    ['] <q> ,                   ( make a quotation object from address + psp )
    ['] jmp , (dummy) ;         ( bypass inline code )

: } immediate
    ['] exit.prim @ ,
    resolve
    q.count dec ;

: yield ( q -- ? )
    qpsp @ >r
    dup
    qt.psp qpsp !
    qt.adr exec
    r> qpsp ! ;

: dip ( a xt -- a ) swap >r yield r> ;
: keep ( a xt -- xt.a a ) over >r yield r> ;
: bi ( a xt1 xt2 -- xt1.a xt2.a ) { keep } dip yield ;
: bi* ( a b xt1 xt2 -- xt1.a xt2.b ) { dip } dip yield ;
: bi@ ( a b xt -- xt.a xt.b ) dup bi* ;