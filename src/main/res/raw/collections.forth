: <map> ( -- c ) :com.vectron.fcl.types.Dic/empty jvm-call-static ;
: <list> ( -- c ) :com.vectron.fcl.types.Lst/empty jvm-call-static ;
: size ( c -- n ) :size jvm-call-method ;
: put ( c k v -- ) swap rot :put/OO jvm-call-method ;
: at ( c k -- v ) swap :at/O jvm-call-method ;
: 1st ( c -- e ) 0 at ;
: 2nd ( c -- e ) 1 at ;
: clear ( c -- ) :clear jvm-call-method ;
: add ( c v -- ) swap :append/O jvm-call-method ;
: prep ( c v -- ) swap :prep/O jvm-call-method ;
: iter ( c -- ) :iterator jvm-call-method ;
: next? ( i -- ) :hasNext jvm-call-method ;
: next ( i -- o ) :next jvm-call-method ;
: jvmValue ( p -- o ) :value jvm-call-method ;
: reverse ( l -- l ) :reverse jvm-call-method ;
: format ( l s -- s ) :format/t jvm-call-method ;

: each ( c q -- )
    -> q iter -> it
    begin
        it next?
    while
        it next q yield
    repeat ;

: map ( c q -- c )
    -> q iter -> it <list> -> result
    begin
        it next?
    while
        result
        it next q yield
        add
    repeat
    result ;

: filter ( c q -- c )
    -> q iter -> it <list> -> result
    begin
        it next?
    while
        it next -> item
        item q yield if
            result item add
        then
    repeat
    result ;

: [ ( -- ) depth >r rswap ;
: ] ( .. -- lst )
    <list> -> result
    depth rswap r> - -> count
    count 0 do
        result swap prep
    loop
    result ;

: peel ( l -- .. ) -> lst lst {  } each ;
: peel# ( m -- .. ) -> m m { dup 1st swap 2nd } each ;

: list* ( .. -- l )
    <list> -> lst
    depth 0 do lst swap add loop
    lst reverse ;

: map* ( .. -- m )
    depth odd? if 'expected even number of items for a map*' abort then
    <map> -> m
    depth 2 / 0 do m -rot put loop
    m .
    m ;

: #[ ( -- ) depth >r rswap ;
: ]# ( .. -- map )
    <map> -> result
    depth rswap r> - -> count
    count 2 /mod drop 0 != if 'expected even number of items for a map' abort then
    count 2 / 0 do
        result -rot put
    loop
    result ;

: sublst ( l n n -- l ) swap rot :subList/ii jvm-call-method ;
: remove-at ( l n -- l ) swap :removeAt/i jvm-call-method ;
: remove ( l o -- l ) swap :remove/O jvm-call-method ;
: keys ( d -- l ) :keys jvm-call-method ;
: values ( d -- l ) :values jvm-call-method ;

: ... ( lower upper step -- lst ) :com.vectron.fcl.types.Range/create/NNN jvm-call-static ;
: .. ( lower upper -- lst ) 1  ... ;

: times ( q n -- ) -> n -> q n 0 do q yield loop  ;

: substr ( s n n -- s ) swap rot :substr/ii jvm-call-method ;
: split ( str s -- s ) swap :split/s jvm-call-method ;
: upper ( s -- s ) :upper jvm-call-method ;
: lower ( s -- s ) :lower jvm-call-method ;
: trim ( s -- s ) :trim jvm-call-method ;
: index-of ( s sub -- n ) swap :indexOf/O jvm-call-method ;
: replace ( s old new -- s ) swap rot :replace/ss jvm-call-method ;
: concat ( s1 s2 -- s ) swap :concat/O jvm-call-method ;
: >str ( o -- s ) :asStr jvm-call-method ;

: minl ( ls -- n )
    nil => result
    { result @ nil != if result @ min then result ! } each
    result @ ;

: maxl ( ls -- n )
    nil => result
    { result @ nil != if result @ max then result ! } each
    result @ ;