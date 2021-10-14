: hist ( c -- m )
    dup 'iterator' jvm-has-method not if
        drop #[ ]# exit
    then
    <map> -> tbl {
       -> elem
       tbl elem at -> count
       count nil = if
         tbl elem 1 put
       else
         tbl elem count 1+ put
       then
    } each
    tbl ;

: ms ( n -- ) :java.lang.Thread/sleep/l jvm-call-static ;
: tone ( hz ms -- ) swap :com.vectron.forthcalc.support.Tone/play/di jvm-call-static ;
: torch ( n -- n ) :com.vectron.forthcalc.support.Torch/toggle/O jvm-call-static ;

: match: immediate ` lastword set-predicate ;

: round* { round } map* ;

: npv ( cashflow rate -- n )
    -> rate 0 => year
    { rate year @ dis year inc } map sum ;

: npv* ( .. rate -- n ) >r >list* r> npv ;

: npv/npv' ( cashflow rate -- npv/npv' )
    1+ -> rate 0 => n
    0 0 rot {
        dup  ( each ) n @ neg * rate n @ 1+ neg pow * ( npv' ) rot +
        swap ( each ) rate n @ pow /                  ( npv  ) rot +
        swap
        n inc
    } each / ;

var: irr-guess 0 irr-guess !

: irr ( cashflow -- n/nil )
    -> cashflow irr-guess @ 100 / => guess
    1000 0 do
        guess @ cashflow guess @ npv/npv' - ( new guess )
        dup guess @ - abs 0.01 < if
            100 * unloop exit
        then
        guess !
    loop
    nil ;

: irr* ( cashflow -- n ) >list* irr ;
: pmt -> n 100 / -> r -> p p 1 1 1 r + n pow / - r / / ;

var: juggler.steps 5 juggler.steps !
[ ] val: juggler.exclude

: juggler.solve ( steps exclude-list output-list input-list -- list/nil ) :com.vectron.fcl.Juggler/solve/TTTi jvm-call-static ;
: wzd* ( stack1 stack2 -- list/nil ) >list* exchange >list* aux> juggler.steps @ juggler.exclude 2swap juggler.solve ;

: udp-send-byte ( host port byte -- n ) :com.vectron.forthcalc.support.Udp/sendByte/Nis jvm-call-static ;
: udp-send-str ( host port str -- n ) :com.vectron.forthcalc.support.Udp/sendStr/sis jvm-call-static ;
: udp-send-lst ( host port lst -- n ) :com.vectron.forthcalc.support.Udp/sendLst/Tis jvm-call-static ;