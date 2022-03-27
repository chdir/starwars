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

class FavoritesAdapter(context: Context) : ListAdapter<Character, FavoriteViewHolder>(DiffCallback()) {
    private var clickListener : View.OnClickListener? = null
    private var deleteClickListener : View.OnClickListener? = null

    private val inflater : LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val layout = inflater.inflate(R.layout.item_character_favorite, parent, false)

        val holder = FavoriteViewHolder(layout)

        holder.itemView.setOnClickListener(this.clickListener)
        holder.deleteButton.setOnClickListener(this.deleteClickListener)

        return holder
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item : Character = getItem(position)

        holder.name.text = item.name

        holder.gender.setImageResource(if (item.gender <= 0) R.drawable.ic_baseline_girl_24 else R.drawable.ic_baseline_man_24)
    }

    public fun getItem(item: RecyclerView.ViewHolder): Character {
        return getItem(item.adapterPosition)
    }

    public fun setClickListener(listener : View.OnClickListener) {
        this.clickListener = listener
    }

    public fun setDeleteClickListener(listener : View.OnClickListener) {
        this.deleteClickListener = listener
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