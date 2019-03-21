package com.jackowski.movies.api

import com.jackowski.movies.utils.Constants.API_KEY
import okhttp3.Interceptor
import okhttp3.Response

const val API_QUERY_NAME = "api_key"

class QueryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalUrl = request.url()

        val url = originalUrl.newBuilder()
            .addQueryParameter(API_QUERY_NAME, API_KEY)
            .build()

        val requestBuilder = request.newBuilder().url(url)

        return chain.proceed(requestBuilder.build())
    }
}