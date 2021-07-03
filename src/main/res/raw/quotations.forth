: <q> ( adr sfp -- c ) :com.vectron.fcl.types.Quot/create/ii jvm-call-static ;
: qt.adr ( q -- a ) :address jvm-call-method ;
: qt.sfp ( q -- a ) :stackFrame jvm-call-method ;

: { immediate
    frame.allocated @ not if    ( We need to have a PSP up front for <q>, because quotations might have its own locals )
        ['] frame.alloc ,       ( But at this point it might not be available yet unless the enclosing function has locals before the quotations )
        true frame.allocated !
    then
    ['] lit , here 6 + ,        ( beginning of the quotation )
    ['] sfp , ['] @ ,           ( current stack frame )
    ['] <q> ,                   ( make a quotation object from address + sfp )
    ['] jmp , (dummy)           ( bypass inline code )
;

: } immediate
    ['] exit.prim @ ,
    resolve ;

: yield ( q -- ? )
    sfp @ >r
    dup
    qt.sfp sfp !
    qt.adr exec
    r> sfp ! ;

: dip ( a xt -- a ) swap >r yield r> ;
: keep ( a xt -- xt.a a ) over >r yield r> ;
: bi ( a xt1 xt2 -- xt1.a xt2.a ) { keep } dip yield ;
: bi* ( a b xt1 xt2 -- xt1.a xt2.b ) { dip } dip yield ;
: bi@ ( a b xt -- xt.a xt.b ) dup bi* ;