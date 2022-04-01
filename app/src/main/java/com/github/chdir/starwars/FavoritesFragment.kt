package com.github.chdir.starwars;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.chdir.starwars.viewmodel.FavoritesViewModel
import com.github.chdir.starwars.viewmodel.StateData
import com.github.chdir.starwars.widget.FavoritesAdapter

import com.github.chdir.starwars.db.Character as Character

class FavoritesFragment : Fragment() {
    private val viewModel: FavoritesViewModel by viewModels()

    private lateinit var favoritesAdapter : FavoritesAdapter

    private lateinit var favorites: RecyclerView
    private lateinit var emptyView : View
    private lateinit var favoritesToolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.screen_favorites, container, false)

        val context = this.context ?: throw java.lang.IllegalStateException()

        favorites = view.findViewById(R.id.favorites_view)
        emptyView = view.findViewById(R.id.favorites_empty_holder)
        favoritesToolbar = view.findViewById(R.id.favorites_toolbar)

        val adapter2 = FavoritesAdapter(context)
        this.favoritesAdapter = adapter2

        favorites.layoutManager = LinearLayoutManager(context)
        favorites.adapter = adapter2

        favoritesAdapter.setClickListener { openCharacterPage(it) }
        favoritesAdapter.setDeleteClickListener { deleteItem(it) }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getFavoritesLiveData().observe(this) { handleFavoritesLoadOutcome(it) }
    }

    private fun openCharacterPage(view: View) {
        val holder = favorites.findContainingViewHolder(view)

        viewModel.openCharacterPage(favoritesAdapter.getItem(holder!!))
    }

    private fun deleteItem(view: View) {
        val holder = favorites.findContainingViewHolder(view)

        viewModel.deleteItem(favoritesAdapter.getItem(holder!!))
    }

    private fun handleFavoritesLoadOutcome(state: StateData<List<Character>>) {
        when (state) {
            is StateData.Success -> {
                onFavoritesUpdated(state.getValue())
            }
            is StateData.Loading -> {}
            is StateData.Failure -> {}
        }
    }

    private fun onFavoritesUpdated(favorites: List<Character>) {
        if (favorites.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }

        favoritesAdapter.submitList(favorites)
    }
}
