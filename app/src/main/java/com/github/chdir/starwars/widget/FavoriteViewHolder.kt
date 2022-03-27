package com.github.chdir.starwars.widget

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.chdir.starwars.R

class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public val name : TextView
    public val gender : ImageView
    public val deleteButton : ImageButton

    init {
        name = itemView.findViewById(R.id.character_info)
        gender = itemView.findViewById(R.id.gender_image)
        deleteButton = itemView.findViewById(R.id.delete_button)
    }
}
