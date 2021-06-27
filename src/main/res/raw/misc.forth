: hist ( c -- m )
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