package com.jackowski.movies.api

import android.content.Context
import com.jackowski.movies.models.MoviesList
import com.jackowski.movies.utils.ConnectivityInterceptor
import com.jackowski.movies.utils.Constants.DOMAIN
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

const val CONNECT_TIMEOUT = 100L
const val READ_TIMEOUT = 100L

interface Service {

    @GET("movie/now_playing")
    fun getNowPlayingMovies(@Query("page") page: String): Observable<MoviesList>

    @GET("search/movie")
    fun searchMovieByTitle(@Query("query") title: String, @Query("page") page: String): Observable<MoviesList>

    companion object {
        fun create(context: Context?): Service {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(ConnectivityInterceptor(context))
                .addInterceptor(QueryInterceptor())
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(DOMAIN)
                .build()

            return retrofit.create(Service::class.java)
        }
    }
}