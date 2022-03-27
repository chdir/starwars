package com.github.chdir.starwars

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.chdir.starwars.api.CharacterDTO
import com.github.chdir.starwars.api.FilmDTO
import com.github.chdir.starwars.api.StarWarsService
import com.github.chdir.starwars.db.StarWarsDb
import com.github.chdir.starwars.viewmodel.CharacterInfoViewModel
import com.github.chdir.starwars.viewmodel.StateData
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import com.github.chdir.starwars.db.Character as Character

class CharacterInfoActivity : AppCompatActivity() {

    private lateinit var charName: Toolbar
    private lateinit var birthYear: TextView
    private lateinit var height: TextView
    private lateinit var eyeColor: TextView
    private lateinit var hairColor: TextView
    private lateinit var skinColor: TextView
    private lateinit var characterSex: ImageView
    private lateinit var starButton: ImageButton
    private lateinit var progress: ProgressBar
    private lateinit var filmsContainer: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)

        charName = findViewById(R.id.character_info_toolbar)
        birthYear = findViewById(R.id.char_birth_year)
        height = findViewById(R.id.height)
        eyeColor = findViewById(R.id.eye_color)
        hairColor = findViewById(R.id.hair_color)
        skinColor = findViewById(R.id.skin_color)
        characterSex = findViewById(R.id.char_sex)
        starButton = findViewById(R.id.star)
        progress = findViewById(R.id.character_load_progress)
        filmsContainer = findViewById(R.id.character_films)

        val uri = intent.data ?: return

        val viewModel = ViewModelProvider(this, CharacterInfoViewModel.Factory(uri.toString()))
            .get(CharacterInfoViewModel::class.java)

        starButton.setOnClickListener { viewModel.addToFavorites() }

        viewModel.getCharacterData().observe(this) { handleStateChange(it) }
        viewModel.getFilmsData().observe(this) { handleFilmsQueryOutcome(it) }
    }

    private fun  handleFilmsQueryOutcome(state: StateData<List<FilmDTO>>) {
        when (state) {
            is StateData.Loading -> {
            }
            is StateData.Failure -> {
                showLoadFailDialog()
            }
            is StateData.Success -> {
                showFilmsInfo(state.getValue())
            }
        }
    }

    private fun showFilmsInfo(films: List<FilmDTO>) {
        for (film in films) {
            val filmView = layoutInflater.inflate(R.layout.item_film, filmsContainer, false) as TextView
            filmView.text = "\u2713 ${film.title} (${film.releaseDate.substring(0, 4)})"
            filmsContainer.addView(filmView)
        }
    }

    private fun  handleStateChange(state: StateData<Character>) {
        when (state) {
            is StateData.Loading -> {
                progress.visibility = View.VISIBLE
            }
            is StateData.Failure -> {
                progress.visibility = View.GONE

                showLoadFailDialog()
            }
            is StateData.Success -> {
                progress.visibility = View.GONE

                showCharacterInfo(state.getValue())
            }
        }
    }

    private fun showLoadFailDialog() {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.retry) { _, _ -> retry() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .setMessage(R.string.load_failed)
            .setOnCancelListener { finish() }
            .show()
    }

    private fun retry() {
        val uri = intent.data ?: return

        val viewModel = ViewModelProvider(this, CharacterInfoViewModel.Factory(uri.toString()))
            .get(CharacterInfoViewModel::class.java)

        viewModel.loadData()
    }

    private fun showCharacterInfo(char: Character) {
        if (char.favorite) {
            starButton.setImageResource(R.drawable.ic_baseline_star_24)
        } else {
            starButton.setImageResource(R.drawable.ic_baseline_star_outline_24)
        }

        charName.title = char.name

        hairColor.text = translate("hair_color", char.hair_color)
        eyeColor.text = translate("eye_color", char.eye_color)
        skinColor.text = translate("skin_color", char.skin_color)

        height.text = if (char.height == null) {
            resources.getString(R.string.unknown)
        } else {
            "${char.height}"
        }

        birthYear.text = if (char.birthYear == null || char.birthYear == "unknown") {
            resources.getString(R.string.unknown)
        } else {
            char.birthYear
        }

        val genderImg = when(char.gender) {
            1 -> R.drawable.ic_baseline_male_24
            0 -> R.drawable.ic_baseline_female_24
            -1 -> R.drawable.ic_baseline_herm_24
            else -> R.drawable.ic_baseline_question_mark_24
        }

        characterSex.setImageResource(genderImg)
    }

    private fun translate(type: String, name: String?): String {
        if (name == null || name == "unknown") {
            return resources.getString(R.string.unknown)
        } else if (name == "n/a") {
            return "-"
        }

        if (name.contains(',')) {
            return name.split(',').map { translate(type, it) }.joinToString(", ")
        }

        val nameWithoutDashes = name?.trim().replace('-', '_')

        val resId = resources.getIdentifier("${type}_${nameWithoutDashes}", "string", packageName)

        return if (resId == 0) name else resources.getString(resId)
    }
}
