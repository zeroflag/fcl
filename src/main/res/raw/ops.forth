: min ( n n -- n ) 2dup < if drop else nip then ;
: max ( n n -- n ) 2dup < if nip else drop then ;
: neg ( n -- n ) -1 * ;
: abs ( n -- n ) dup 0 < if -1 * then ;
: sum* ( .. -- n ) depth 1- 0 do + loop ;
: prod* ( .. -- n ) depth 1- 0 do * loop ;
: percent ( n n -- n ) * 100.0 / ;
: odd?  2 /mod drop 0 != ;
: even? 2 /mod drop 0  = ;
: mod /mod drop ;
: div /mod nip ;
( finance )
: cin1 ( b i n -- t ) swap 100 / 1+ swap pow * ;
: cin2 ( base interest years monthly-contribution -- .. )
    12 * rot ( interest ) >r ( contribution ) >r
    begin
        dup ( years ) 0 >
    while
        over j ( interest ) 100 / 1+ * i ( contribution ) + swap
        ( years ) 1 -
    repeat
    drop ( years ) r> r> 2drop ;
: dis ( b i n -- t ) swap 100 / 1+ swap pow / ;
: tip1 ( n -- n ) 15 percent ;
: tip2 ( bill split --  total tip ) / dup 115 percent swap 15 percent ;
( trigonometry )
: pi ( n -- n ) :java.lang.Math/PI jvm-static-var ;
: sin ( n -- n ) :java.lang.Math/sin/d jvm-call-static ;
: cos ( n -- n ) :java.lang.Math/cos/d jvm-call-static ;
: tan ( n -- n ) :java.lang.Math/tan/d jvm-call-static ;
: asin ( n -- n ) :java.lang.Math/asin/d jvm-call-static ;
: acos ( n -- n ) :java.lang.Math/acos/d jvm-call-static ;
: atan ( n -- n ) :java.lang.Math/atan/d jvm-call-static ;
: sinh ( n -- n ) :java.lang.Math/sinh/d jvm-call-static ;
: cosh ( n -- n ) :java.lang.Math/cosh/d jvm-call-static ;
: tanh ( n -- n ) :java.lang.Math/tanh/d jvm-call-static ;
( math )
: e ( n -- n ) :java.lang.Math/E jvm-static-var ;
: round ( n -- n ) :java.lang.Math/round/d jvm-call-static ;
: sqrt ( n -- n ) :java.lang.Math/sqrt/d jvm-call-static ;
: 10log ( n -- n ) :java.lang.Math/log10/d jvm-call-static ;
: nlog ( n n -- n ) swap 10log swap 10log / ;
: 2log ( n -- n ) 2.0 nlog ;
: elog ( n -- n ) :java.lang.Math/log/d jvm-call-static ;
: n! ( n -- n )
    dup 1 <= if
        drop 1
    else
        round dup 1 do i * loop
    then ;
: avg* ( .. -- n ) depth dup 1 < if drop else >r sum* r> / then ;
: rnd ( -- n ) :com.vectron.fcl.interop.JvmInterOp/random jvm-call-static ;
: min* ( .. -- n ) depth 1- 0 do min loop ;
: max* ( .. -- n ) depth 1- 0 do max loop ;
( unit conversion )
: ft>m ( n -- n ) 0.3048 * ;
: m>ft ( n -- n ) 0.3048 / ;
: in>cm ( n -- n ) 2.54 * ;
: cm>in ( n -- n ) 2.54 / ;
: mi>km ( n -- n ) 1.60934 * ;
: km>mi ( n -- n ) 1.60934 / ;
: nm>mi ( n -- n ) 1.15078 * ;
: mi>nm ( n -- n ) 1.15078 / ;
: nm>km ( n -- n ) 1.852 * ;
: km>nm ( n -- n ) 1.852 / ;
: yd>m ( n -- n ) 0.9144 * ;
: m>yd ( n -- n ) 0.9144 / ;
: lb>kg ( n -- n ) 2.20462 / ;
: kg>lb ( n -- n ) 2.20462 * ;
: oz>g ( n -- n ) 28.3495 * ;
: g>oz ( n -- n ) 28.3495 / ;
: dg>rd ( n -- n ) pi 180 / * ;
: rd>dg ( n -- n ) 180 pi / * ;
: c>f ( n -- n ) 9 * 5 / 32 + ;
: f>c ( n -- n ) 32 - 5 * 9 / ;
: w>hp ( n -- n ) 745.7 / ;
: hp>w ( n -- n ) 745.7 * ;
: kc>j ( n -- n ) 4184 * ;
: j>kc ( n -- n ) 4184 / ;
: b>pa ( n -- n ) 100000 * ;
: pa>b ( n -- n ) 100000 / ;
: t>pa ( n -- n ) 133.322387415 * ;
: pa>t ( n -- n ) 133.322387415 / ;
: uptime ( -- millis ) :android.os.SystemClock/elapsedRealtime jvm-call-static ;
: >num ( s -- n ) :com.vectron.fcl.types.Num/parse/s jvm-call-static ;
: year ( -- y )
    :java.util.Calendar/YEAR jvm-static-var
    :java.util.Calendar/getInstance jvm-call-static
    :get/i jvm-call-method ;
: month ( -- y )
    :java.util.Calendar/MONTH jvm-static-var
    :java.util.Calendar/getInstance jvm-call-static
    :get/i jvm-call-method
    1 + ;
: day ( -- y )
    :java.util.Calendar/DAY_OF_MONTH jvm-static-var
    :java.util.Calendar/getInstance jvm-call-static
    :get/i jvm-call-method ;