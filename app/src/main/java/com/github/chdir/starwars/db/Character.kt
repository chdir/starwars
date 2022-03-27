package com.github.chdir.starwars.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.chdir.starwars.api.CharacterDTO

@Entity(tableName = "characters")
data class Character(@ColumnInfo
                     @PrimaryKey
                     val url: String,

                     @ColumnInfo
                     val name: String,

                     @ColumnInfo(defaultValue = "1")
                     var favorite: Boolean = true,

                     @ColumnInfo
                     val birthYear: String?,

                     @ColumnInfo
                     val height: Int?,

                     @ColumnInfo
                     val eye_color: String,

                     @ColumnInfo
                     val hair_color: String,

                     @ColumnInfo
                     val skin_color: String,

                     @ColumnInfo
                     val gender: Int) {
    companion object {
        fun transformDto(networkResults : List<CharacterDTO>): List<Character> {
            val newList = ArrayList<Character>(networkResults.size)

            for (dto in networkResults) {
                if (dto.name == null || dto.url == null) {
                    continue
                }

                newList.add(transformDto(dto))
            }

            return newList
        }

        fun transformDto(dto: CharacterDTO) : Character {
            if (dto.name == null || dto.url == null) {
                throw Exception("Invalid response received from server")
            }

            val height = if (dto.height == null) {
                null
            } else {
                try { dto.height.toInt() } catch (e: NumberFormatException) { null }
            }

            return Character(
                dto.url,
                dto.name,
                true,
                dto.birthYear,
                height,
                dto.eyeColor ?: "",
                dto.hairColor ?: "",
                dto.skinColor ?: "",
                deviseGender(dto.gender))
        }

        private fun deviseGender(gender: String?) : Int {
            return when (gender) {
                "hermaphrodite" -> -1
                "female" -> 0
                else -> 1
            }
        }
    }
}