package com.github.chdir.starwars.api

import com.google.gson.annotations.SerializedName

data class CharacterDTO(
    val url: String?,
    val name: String?,
    val gender: String?,
    val height: String?,
    val films: List<String>,
    @SerializedName("birth_year")
    val birthYear: String?,
    @SerializedName("eye_color")
    val eyeColor: String?,
    @SerializedName("skin_color")
    val skinColor: String?,
    @SerializedName("hair_color")
    val hairColor: String?,
)