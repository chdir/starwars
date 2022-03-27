package com.github.chdir.starwars.api

data class CharacterListDTO(
    val results : List<CharacterDTO>,
    val next: String,
    val previous: String,
    val count: Int,
    ) {}