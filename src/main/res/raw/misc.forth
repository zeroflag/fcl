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

: ms ( n -- ) 'java.lang.Thread/sleep/l' jvm-call-static ;