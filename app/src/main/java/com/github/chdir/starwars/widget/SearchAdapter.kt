package com.github.chdir.starwars.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.chdir.starwars.R
import com.github.chdir.starwars.db.Character as Character

class SearchAdapter(context: Context) : ListAdapter<Character, SearchViewHolder>(DiffCallback()) {
    private val selectedItems: HashSet<String> = HashSet()

    private var favorites: Set<String> = HashSet()

    private var clickListener : View.OnClickListener? = null
    private var longCLickListener : View.OnLongClickListener? = null

    private val inflater : LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        if (viewType == 1) {
            return SearchViewHolder.Stub(inflater.inflate(R.layout.item_progress, parent, false))
        }

        val layout = inflater.inflate(R.layout.item_character_search, parent, false)

        val holder = SearchViewHolder.Normal(layout)

        holder.itemView.setOnClickListener(this.clickListener)
        holder.itemView.setOnLongClickListener(this.longCLickListener)

        return holder
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        if (holder !is SearchViewHolder.Normal) {
            return
        }

        val item : Character = getItem(position)

        holder.name.text = item.name
        holder.itemView.isSelected = selectedItems.contains(item.url)

        holder.star.visibility = if (favorites.contains(item.url)) View.VISIBLE else View.GONE

        holder.gender.setImageResource(if (item.gender <= 0) R.drawable.ic_baseline_girl_24 else R.drawable.ic_baseline_man_24)
    }

    fun setFavorites(favorites : Set<String>) {
        this.favorites = favorites

        notifyItemRangeChanged(0, itemCount)
    }

    public fun setLongClickListener(listener : View.OnLongClickListener) {
        this.longCLickListener = listener
    }

    public fun setClickListener(listener : View.OnClickListener) {
        this.clickListener = listener
    }

    public fun isEmpty(): Boolean {
        return itemCount == 0
    }

    public fun getItem(item: RecyclerView.ViewHolder): Character {
        return getItem(item.adapterPosition)
    }

    public fun checkItem(item: RecyclerView.ViewHolder): Character {
        val holder = item as SearchViewHolder

        val checkable = holder.itemView

        checkable.isSelected = !checkable.isSelected

        val char : Character = getItem(holder.adapterPosition)

        if (checkable.isSelected) {
            selectedItems.add(char.url)
        } else {
            selectedItems.remove(char.url)
        }

        return char
    }

    fun clearAll() {
        selectedItems.clear()

        notifyItemRangeChanged(0, itemCount)
    }

    fun checkedCount(): Int {
        return selectedItems.size
    }

    fun getCheckedItems() : List<out Character> {
        val newList = ArrayList<Character>(selectedItems.size)

        for (char in this.currentList) {
            if (selectedItems.contains(char.url)) {
                newList.add(char)
            }
        }

        return newList
    }

    fun isFavorite(c: Character): Boolean {
        return favorites.contains(c.url)
    }

    private class DiffCallback :DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.name == newItem.name
        }
    }
}