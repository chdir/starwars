package com.github.chdir.starwars.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.github.chdir.starwars.api.FilmDTO
import com.github.chdir.starwars.api.StarWarsService
import com.github.chdir.starwars.db.StarWarsDb
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Objects
import java.util.concurrent.TimeUnit
import com.github.chdir.starwars.db.Character as Character

class CharacterInfoViewModel(val url: String) : ViewModel() {
    private var character: Character? = null
    private var isFavorite: Boolean = false

    private val characterLiveData = MutableLiveData<StateData<Character>>()
    private val filmLiveData = MutableLiveData<StateData<List<FilmDTO>>>()

    init {
        loadData()

        StarWarsDb.INSTANCE.characterDao.getCharacter(url)
            .subscribe { processFavStatus(it) }
    }

    fun loadData() {
        characterLiveData.postValue(StateData.Loading())

        val swService = StarWarsService.INSTANCE

        swService.loadCharacter(url)
            .doOnNext { loadFilms(it.films) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val character = Character.transformDto(it)

                character.favorite = isFavorite

                this.character = character

                characterLiveData.postValue(StateData.Success(character))
            }, {
                characterLiveData.postValue(StateData.Failure(it))
            })
    }

    private fun loadFilms(films: List<String>) {
        val filmList = ArrayList<Observable<FilmDTO>>()

        val swService = StarWarsService.INSTANCE

        for (film in films) {
            filmList.add(swService
                .loadFilm(film)
                .subscribeOn(Schedulers.io()))
        }

        Observable.combineLatest(filmList) { f -> f.map { value -> value as FilmDTO } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                filmLiveData.postValue(StateData.Success(it))
            }, {
                filmLiveData.postValue(StateData.Failure(it))
            })
    }

    fun getCharacterData() : LiveData<StateData<Character>> {
        return characterLiveData
    }

    fun getFilmsData() : LiveData<StateData<List<FilmDTO>>> {
        return filmLiveData
    }

    private fun processFavStatus(foundInDatabase: List<Character>) {
        isFavorite = foundInDatabase.isNotEmpty()

        val character = this.character ?: return

        character.favorite = isFavorite

        characterLiveData.postValue(StateData.Success(character))
    }

    fun addToFavorites() {
        val character = this.character ?: return

        val dao = StarWarsDb.INSTANCE.characterDao

        if (isFavorite) {
            dao.deleteFavorites(listOf(character)).subscribeOn(Schedulers.io()).subscribe()
        } else {
            character.favorite = true

            dao.addFavorites(listOf(character)).subscribeOn(Schedulers.io()).subscribe()
        }
    }

    class Factory(private val url: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CharacterInfoViewModel(url) as T
        }

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return CharacterInfoViewModel(url) as T
        }
    }
}