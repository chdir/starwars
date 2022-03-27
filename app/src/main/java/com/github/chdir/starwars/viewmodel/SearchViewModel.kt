package com.github.chdir.starwars.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.chdir.starwars.CharacterInfoActivity
import com.github.chdir.starwars.StarWarsApp
import com.github.chdir.starwars.api.CharacterListDTO
import com.github.chdir.starwars.api.StarWarsService
import com.github.chdir.starwars.db.StarWarsDb
import com.github.chdir.starwars.model.CharacterList
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import com.github.chdir.starwars.db.Character as Character

class SearchViewModel : ViewModel() {
    private val searchLiveData = MutableLiveData<StateData<CharacterList>>()
    private val favoritesLiveData = MutableLiveData<StateData<List<String>>>()

    private var textEvents: Disposable? = null
    private var pagedLoad: Disposable? = null

    init {
        StarWarsDb.INSTANCE.characterDao.getFavorites()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                favoritesLiveData.postValue(StateData.Success(it))
            }, {
                favoritesLiveData.postValue(StateData.Failure(it))
            })
    }

    public fun getSearchLiveData(): LiveData<StateData<CharacterList>> {
        return searchLiveData
    }

    public fun getFavoritesLiveData(): LiveData<StateData<List<String>>> {
        return favoritesLiveData
    }

    public fun openCharacterPage(character: Character) {
        val uri = character.url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri), StarWarsApp.INSTANCE, CharacterInfoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        StarWarsApp.INSTANCE.startActivity(intent)
        return
    }

    public fun setEditTextObservable(searchObservable: Observable<String>) {
        val oldSubscription = textEvents
        if (oldSubscription != null) {
            oldSubscription.dispose()
            textEvents = null
        }

        val starWars = StarWarsService.INSTANCE

        textEvents = searchObservable
            .doOnNext {
                pagedLoad?.dispose()
                pagedLoad = null
            }
            .observeOn(Schedulers.io())
            .switchMap {
                starWars.findCharacters(it)
                    .timeout(4, TimeUnit.SECONDS)
                    .doOnError { error: Throwable ->
                        when (error) {
                            is InterruptedException -> {}
                            is InterruptedIOException -> {}
                            else -> {
                                searchLiveData.postValue(StateData.Failure(error))
                            }
                        }
                    }
                    .onErrorResumeNext { Observable.empty() }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val result = dtoToCharList(it)
                searchLiveData.postValue(StateData.Success(result))
            }
    }

    public fun fetchMoreResults() {
        val starWars = StarWarsService.INSTANCE

        val current = searchLiveData.value
        if (current !is StateData.Success) {
            return
        }

        val list = current.getValue()

        val nextLink = list.next ?: return

        pagedLoad = starWars.loadCharacters(nextLink)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val converted = dtoToCharList(it)
                val newList = ArrayList(list.results)
                newList.addAll(converted.results)
                converted.results = newList
                searchLiveData.postValue(StateData.Success(converted))
            }, {
                favoritesLiveData.postValue(StateData.Failure(it))
            })
    }

    private fun dtoToCharList(dto: CharacterListDTO) : CharacterList {
        val newList = Character.transformDto(dto.results)
        return CharacterList(newList, dto.next, dto.count)
    }

    fun updateFavorites(items: List<Character>, removingFromFavorites: Boolean) {
        if (removingFromFavorites) {
            StarWarsDb.INSTANCE.characterDao.deleteFavorites(items)
                .subscribeOn(Schedulers.io())
                .subscribe()
        } else {
            StarWarsDb.INSTANCE.characterDao.addFavorites(items)
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }
}