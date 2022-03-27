package com.github.chdir.starwars.model

import com.github.chdir.starwars.db.Character as Character

data class CharacterList(
    var results : List<Character>,
    val next: String?,
    val count: Int,
)