package com.github.chdir.starwars.api

import com.google.gson.annotations.SerializedName

data class FilmDTO(
    val title: String,
    @SerializedName("release_date")
    val releaseDate: String,
)