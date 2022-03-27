package com.github.chdir.starwars.widget

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.chdir.starwars.R

sealed class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class Stub(itemView: View) : SearchViewHolder(itemView) {
    }

    class Normal(itemView: View) : SearchViewHolder(itemView) {
        public val name : TextView
        public val gender : ImageView
        public val star : ImageView

        init {
            name = itemView.findViewById(R.id.character_info)
            gender = itemView.findViewById(R.id.gender_image)
            star = itemView.findViewById(R.id.star_image)
        }
    }
}
