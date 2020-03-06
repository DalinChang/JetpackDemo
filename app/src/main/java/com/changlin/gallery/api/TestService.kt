package com.changlin.gallery.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface TestService {

    @Headers("accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"
        ,"accept-encoding: gzip, deflate, br"
        ,"accept-language: zh-CN,zh;q=0.9"
        ,"cache-control: no-cache"
        )
    @GET("/toplist")
    fun getTopList(@Query("page")page: Int): Observable<String>
}