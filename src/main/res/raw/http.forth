: http-get ( u -- n b )
    'com.vectron.forthcalc.support.HttpClient/get/s' jvm-call-static -> response
    response 'body' jvm-call-method
    response 'code' jvm-call-method
    ;

: http-post ( d u -- n b )
    -> url -> request
    request jvmValue url 'com.vectron.forthcalc.support.HttpClient/post/sm' jvm-call-static -> response
    response 'body' jvm-call-method
    response 'code' jvm-call-method
    ;