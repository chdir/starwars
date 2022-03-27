package com.github.chdir.starwars.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.chdir.starwars.CharacterInfoActivity
import com.github.chdir.starwars.StarWarsApp
import com.github.chdir.starwars.db.StarWarsDb
import io.reactivex.rxjava3.schedulers.Schedulers
import com.github.chdir.starwars.db.Character as Character

class FavoritesViewModel : ViewModel() {
    private val favoritesLiveData = MutableLiveData<StateData<List<Character>>>()

    init {
        StarWarsDb.INSTANCE.characterDao
            .getCharacters()
            .subscribe { newList ->
                favoritesLiveData.postValue(StateData.Success(newList))
            }
    }

    public fun getFavoritesLiveData(): LiveData<StateData<List<Character>>> {
        return favoritesLiveData
    }

    public fun openCharacterPage(character: Character) {
        val uri = character.url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri), StarWarsApp.INSTANCE, CharacterInfoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        StarWarsApp.INSTANCE.startActivity(intent)
        return
    }

    fun deleteItem(item: Character) {
        StarWarsDb.INSTANCE.characterDao
            .deleteFavorites(listOf(item))
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
    }
}