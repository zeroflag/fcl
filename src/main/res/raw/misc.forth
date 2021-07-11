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
: torch ( n -- ) :com.vectron.forthcalc.support.Torch/toggle/O jvm-call-static ;

: match: immediate ` lastword set-predicate ;