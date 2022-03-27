package com.github.chdir.starwars.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.chdir.starwars.StarWarsApp

@Database(version = 3, entities = [Character::class])
abstract class StarWarsDb() : RoomDatabase() {
    abstract val characterDao: CharacterDao

    companion object {
        val INSTANCE by lazy {
            Room.databaseBuilder(StarWarsApp.INSTANCE, StarWarsDb::class.java, "chars.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}