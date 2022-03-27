package com.github.chdir.starwars.api

import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

import com.github.chdir.starwars.db.Character as Character

interface StarWarsService {
    @GET("/api/people/")
    fun findCharacters(@Query("search") query: String) : Observable<CharacterListDTO>

    @GET
    fun loadCharacters(@Url url: String) : Observable<CharacterListDTO>

    @GET
    fun loadCharacter(@Url url: String) : Observable<CharacterDTO>

    @GET
    fun loadFilm(@Url url: String) : Observable<FilmDTO>

    companion object {
        val INSTANCE by lazy<StarWarsService> {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

            Retrofit.Builder()
                .baseUrl("https://swapi.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
                .create(StarWarsService::class.java)
        }
    }
}