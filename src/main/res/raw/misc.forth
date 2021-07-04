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

: tone ( hz ms -- ) swap :com.vectron.forthcalc.support.Tone/play/di jvm-call-static ;
: torch ( n -- ) :com.vectron.forthcalc.support.Torch/toggle/O jvm-call-static ;

( TODO: wip )

: draw-circle ( x y r -- ) :com.vectron.forthcalc.CanvasView/drawCircle/ddd jvm-call-static ;
: draw-rect ( left top right bottom -- ) :com.vectron.forthcalc.CanvasView/drawRect/dddd jvm-call-static ;
: draw-point ( x y -- ) :com.vectron.forthcalc.CanvasView/drawPoint/dd jvm-call-static ;
: draw-line ( x1 y1 x2 y2 -- ) :com.vectron.forthcalc.CanvasView/drawLine/dddd jvm-call-static ;

: width  :com.vectron.forthcalc.CanvasView/width jvm-call-static ;
: height :com.vectron.forthcalc.CanvasView/height jvm-call-static ;

: width 320 ;
: height 200 ;

var: xmin -10 xmin !
var: ymin -10 ymin !
var: xmax  10 xmax !
var: ymax  10 ymax !

: xstep ( -- n ) xmax @ xmin @ - 100 / ;

: translate -> y -> x
    x width  2 / * xmax @ / width  2 / +
    height y height 2 / * ymax @ / height 2 / + - ;

: draw-axis
    0           height 2 /  width       height 2 /  draw-line
    width 2 /   0           width 2 /   height      draw-line ;

: plotq ( q -- ) -> q
    draw-axis
    nil => px
    nil => py
    xmin @ xmax @ xstep ... { -> x
        x q yield -> y
        px @ nil != if
            px @ py @
            x y translate
            draw-line
        then
        x y translate py ! px !
    } each ;


\ { dup dup dup * * swap 2 * - } plotq
