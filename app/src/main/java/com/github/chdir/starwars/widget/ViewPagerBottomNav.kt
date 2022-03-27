package com.github.chdir.starwars.widget

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager.DecorView
import com.google.android.material.bottomnavigation.BottomNavigationView

@DecorView
class ViewPagerBottomNav : BottomNavigationView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
    }
}