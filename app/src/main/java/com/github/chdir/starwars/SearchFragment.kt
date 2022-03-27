package com.github.chdir.starwars;

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.chdir.starwars.db.Character
import com.github.chdir.starwars.model.CharacterList
import com.github.chdir.starwars.viewmodel.SearchViewModel
import com.github.chdir.starwars.viewmodel.StateData
import com.github.chdir.starwars.widget.SearchAdapter
import com.github.chdir.starwars.widget.EditTextObservable
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet

class SearchFragment : Fragment() {
    private lateinit var searchAdapter : SearchAdapter

    private lateinit var recycler: RecyclerView
    private lateinit var searchView: EditText
    private lateinit var emptyView : TextView
    private lateinit var progress : View
    private lateinit var searchToolbar: Toolbar

    private var actionMode: ActionMode? = null
    private var removingFromFavorites = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.screen_search, container, false)

        val context = this.context ?: throw java.lang.IllegalStateException()

        recycler = view.findViewById(R.id.result_view)
        emptyView = view.findViewById(R.id.search_empty_holder)
        searchToolbar = view.findViewById(R.id.search_toolbar)
        progress = view.findViewById(R.id.search_progress)

        recycler.layoutManager = LinearLayoutManager(context)

        val adapter = SearchAdapter(context)
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        adapter.setClickListener(View.OnClickListener { handleSingleClick(it) })
        adapter.setLongClickListener(View.OnLongClickListener { handleLongClick(it) })

        recycler.adapter = adapter

        this.searchAdapter = adapter

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val searchWidget = findSearchView()
        searchView = searchWidget.findViewById(androidx.appcompat.R.id.search_src_text)

        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        viewModel.getSearchLiveData().observe(this) { handleSearchOutcome(it) }
        viewModel.getFavoritesLiveData().observe(this) { handleFavoritesOutcome(it) }

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                maybeLoadMore()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        maybeLoadMore()
    }

    private fun maybeLoadMore() {
        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        if (!recycler.canScrollVertically(1)) {
            viewModel.fetchMoreResults()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        var editTextEventStream = EditTextObservable
            .create(searchView)
            .debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnNext { recycler.scrollToPosition(0) }

        if (savedInstanceState == null) {
            editTextEventStream = editTextEventStream.startWith(Observable.just(searchView.text.toString()))
        }

        viewModel.setEditTextObservable(editTextEventStream)
    }

    private fun handleFavoritesOutcome(results: StateData<List<String>>) {
        when(results) {
            is StateData.Success -> {
                searchAdapter.setFavorites(HashSet(results.getValue()))
            }
            is StateData.Failure -> {
                onError(results.getThrowable())
            }
            is StateData.Loading -> {}
        }
    }

    private fun handleSearchOutcome(results: StateData<CharacterList>) {
        when(results) {
            is StateData.Failure -> {
                progress.visibility = View.GONE

                onNetworkError(results.getThrowable())
            }
            is StateData.Loading -> {
                emptyView.visibility = View.GONE

                if (searchAdapter.itemCount == 0) {
                    progress.visibility = View.VISIBLE
                }
            }
            is StateData.Success -> {
                progress.visibility = View.GONE

                onResults(results.getValue())
            }
        }
    }

    private fun findSearchView(): SearchView {
        return searchToolbar.menu.findItem(R.id.toolbar_search).actionView as SearchView
    }

    private fun handleSingleClick(v: View) {
        val holder = recycler.findContainingViewHolder(v)

        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        val actionMode = this.actionMode
        if (actionMode == null) {
            val character = searchAdapter.getItem(holder!!)
            viewModel.openCharacterPage(character)
            return
        }

        val character = searchAdapter.checkItem(holder!!)
        val count = searchAdapter.checkedCount()
        actionMode.title = resources.getQuantityString(R.plurals.count_selected, count, count)
        if (searchAdapter.checkedCount() == 0) {
            actionMode?.finish()
        }

        if (isFavorite(character) != removingFromFavorites) {
            actionMode.finish()
        }
    }

    private fun isFavorite(c : Character): Boolean {
        return searchAdapter.isFavorite(c)
    }

    private fun handleLongClick(view: View): Boolean {
        val holder = recycler.findContainingViewHolder(view)

        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        val character = searchAdapter.checkItem(holder!!)
        if (searchAdapter.checkedCount() == 0) {
            actionMode?.finish()
        }

        var am = this.actionMode
        if (am != null) {
            val count = searchAdapter.checkedCount()
            am.title = resources.getQuantityString(R.plurals.count_selected, count, count)
            if (isFavorite(character) != removingFromFavorites) {
                am.finish()
            }
            return true
        }

        val menuInflater = MenuInflater(view.context)

        am = searchToolbar.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.action_menu, menu)

                return true
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem?): Boolean {
                val chosenItems = searchAdapter.getCheckedItems()
                viewModel.updateFavorites(chosenItems, removingFromFavorites)
                mode.finish()
                if (removingFromFavorites) {
                    note(resources.getQuantityString(R.plurals.count_removed, chosenItems.size, chosenItems.size))
                } else {
                    note(resources.getQuantityString(R.plurals.count_added, chosenItems.size, chosenItems.size))
                }
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
                val starItem = menu.findItem(R.id.action_add_to_favorites)
                val deleteItem = menu.findItem(R.id.action_remove_from_favorites)

                val isFavorite = isFavorite(character)
                removingFromFavorites = isFavorite
                deleteItem.isVisible = isFavorite
                starItem.isVisible = !isFavorite

                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                searchAdapter.clearAll()

                actionMode = null
            }
        })

        am.title = resources.getQuantityString(R.plurals.count_selected, 1, 1)

        this.actionMode = am

        return true
    }

    private fun onError(e : Throwable) {
        e.printStackTrace()

        note(e.toString())
    }

    private fun onNetworkError(e : Throwable) {
        val message = if (e is UnknownHostException) {
            getString(R.string.load_failed_no_internet)
        } else {
            e.printStackTrace()

            getString(R.string.load_failed_detailed, e.toString())
        }

        if (searchAdapter.itemCount == 0) {
            emptyView.text = message
            emptyView.visibility = View.VISIBLE
            return
        }

        note(message)
    }

    private fun showErrorDialog() {
        val activity = this.activity ?: return

        AlertDialog.Builder(activity)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setMessage(R.string.load_failed)
            .setOnDismissListener { activity.finish() }
            .show()
    }

    private fun note(text: CharSequence) {
        val activity = this.activity ?: return

        Snackbar.make(activity.window.decorView, text, Snackbar.LENGTH_SHORT)
            .setAnchorView(R.id.bottom_navigation)
            .show()
    }

    private fun onResults(results: CharacterList) {
        if (results.results.isEmpty()) {
            emptyView.text = getString(R.string.no_results)
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }

        searchAdapter.submitList(results.results)
    }

    override fun onStop() {
        val activity = this.activity
        if (activity != null && !activity.isFinishing) {
            actionMode?.finish()
        }

        super.onStop()
    }
}
