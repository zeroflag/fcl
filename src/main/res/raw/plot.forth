: draw-circle ( x y r -- ) :com.vectron.forthcalc.CanvasView/drawCircle/ddd jvm-call-static ;
: draw-rect ( left top right bottom -- ) :com.vectron.forthcalc.CanvasView/drawRect/dddd jvm-call-static ;
: draw-point ( x y -- ) :com.vectron.forthcalc.CanvasView/drawPoint/dd jvm-call-static ;
: draw-line ( x1 y1 x2 y2 -- ) :com.vectron.forthcalc.CanvasView/drawLine/dddd jvm-call-static ;
: draw-text ( i x y s -- ) :com.vectron.forthcalc.CanvasView/drawText/sddi jvm-call-static ;
: paint ( width color -- ) :com.vectron.forthcalc.CanvasView/setPaint/id jvm-call-static ;
: clear-canvas ( -- ) :com.vectron.forthcalc.CanvasView/clear jvm-call-static ;

320        val: width
320        val: height
0xFF000000 val: AXIS-COLOR
0xFF2874A6 val: LINE-COLOR
0xFF93D078 val: GRID-COLOR
0xFF000000 val: TEXT-COLOR

var: xmin
var: ymin
var: xmax
var: ymax
var: ox
var: oy

: xstep ( -- n ) xmax @ xmin @ - 100 / ;
: reset-zoom ( -- )
    -10 xmin ! 10 xmax !
    -10 ymin ! 10 ymax !
     0  ox   ! 0  oy   ! ;

: trans -> y -> x
    x xmin @ - width 1- * xmax @ xmin @ - abs /
    height 1- y ymin @ - height 1- * ymax @ ymin @ - abs / - ;

: draw-axis
    3 AXIS-COLOR paint
    xmin @ oy @ trans xmax @ oy @ trans draw-line
    ox @ ymin @ trans ox @ ymax @ trans draw-line ;

: draw-scale
    0 GRID-COLOR paint
    xmin @ xmax @ xmax @ xmin @ - abs 10 / ... { -> x
        x ymin @ trans
        x ymax @ trans
        draw-line
    } each
    ymin @ ymax @ ymax @ ymin @ - abs 10 / ... { -> y
        xmin @ y trans
        xmax @ y trans
        draw-line
    } each
    1 TEXT-COLOR paint
    1 xmax @ oy   @ trans 5 -         xmax @ >str draw-text
    2 ox   @ ymax @ trans { 5 + } dip ymax @ >str draw-text ;

: plotq ( q -- ) -> q
    clear-canvas
    draw-scale
    draw-axis
    nil => px
    nil => py
    2 LINE-COLOR paint
    xmin @ xmax @ xstep ... { -> x
        x q yield -> y
        px @ nil != if
            px @ py @
            x y trans
            draw-line
        then
        x y trans py ! px !
    } each ;

: plotl ( ls -- ) -> ls
    1           xmin !
    ls size     xmax !
    ls minl     ymin !
    ls maxl     ymax !
    xmin @      ox !
    ymin @      oy !
    clear-canvas
    draw-scale
    draw-axis
    2 LINE-COLOR paint
    nil => px nil => py 1 => x
    ls { -> y
        x @ y trans 5 draw-circle
        px @ nil != if
            px @ py @ trans x @ y trans draw-line
        then
        x @ px ! y py !
        x inc
    } each ;

: plots ( .. -- ) depth 0 != if list* plotl then ;

reset-zoom