'Content-Type'      val: CONTENT-TYPE
'application/json'  val: APPLICATION/JSON

: http-get ( u -- n b )
    'com.vectron.forthcalc.support.HttpClient/get/s' jvm-call-static -> response
    response 'body' jvm-call-method
    response 'code' jvm-call-method ;

: http-post ( d u -- n b )
    -> url -> request
    request url 'com.vectron.forthcalc.support.HttpClient/post/sM' jvm-call-static -> response
    response 'body' jvm-call-method
    response 'code' jvm-call-method ;

: http-put ( d u -- n b )
    -> url -> request
    request url 'com.vectron.forthcalc.support.HttpClient/put/sM' jvm-call-static -> response
    response 'body' jvm-call-method
    response 'code' jvm-call-method ;

: +json-type ( m -- m )  -> request
    request 'headers' at -> headers
    headers nil = if
        #[ 'content'  request
           'headers'  #[ CONTENT-TYPE APPLICATION/JSON ]# ]#
    else
        headers CONTENT-TYPE APPLICATION/JSON put
        request
    then ;