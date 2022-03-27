package com.github.chdir.starwars.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout

class CheckableLinearLayout : LinearLayout, Checkable {
    private val checkedStateSet = intArrayOf(
        android.R.attr.state_checked
    )

    private var checked: Boolean = false
    private var showStar: Boolean = false
    private var female: Boolean? = null

    override fun setChecked(checked: Boolean) {
        if (this.checked == checked) {
            return
        }

        this.checked = checked
        refreshDrawableState()
    }

    override fun isChecked() : Boolean {
        return checked
    }

    override fun toggle() {
        isChecked = !checked
    }

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs : AttributeSet) : super(ctx, attrs)

    constructor(ctx: Context, attrs : AttributeSet, theme: Int) : super(ctx, attrs, theme)

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)

        if (checked) mergeDrawableStates(drawableState, checkedStateSet)

        return drawableState
    }
}