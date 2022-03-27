package com.github.chdir.starwars.db;

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun getCharacters() : Flowable<List<Character>>

    @Query("SELECT * FROM characters WHERE url = :url LIMIT 1")
    fun getCharacter(url: String) : Flowable<List<Character>>

    @Query("SELECT url FROM characters WHERE favorite != 0")
    fun getFavorites() : Flowable<List<String>>

    @Delete
    fun deleteFavorites(chars: List<out Character>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorites(chars: List<out Character>): Completable
}
