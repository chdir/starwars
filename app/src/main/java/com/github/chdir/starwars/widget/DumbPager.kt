package com.github.chdir.starwars.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.ViewAnimator

/**
 * Primitive ViewPager/ViewPager2 replacement, because I dobn't have time to fight them here
 */
public class DumbPager : ViewAnimator {
    public constructor(context: Context?) : super(context) {}
    public constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        return SavedState(superState, displayedChild)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val saved = state as SavedState

        displayedChild = saved.shownChild

        super.onRestoreInstanceState(saved.parentState)
    }

    private final class SavedState(val parentState: Parcelable?, val shownChild: Int) : Parcelable {
        override fun describeContents(): Int {
            return parentState?.describeContents() ?: 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(parentState, flags)
            dest.writeInt(shownChild)
        }

        companion object {
            public val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    val parentState = source.readParcelable<Parcelable?>(ViewAnimator::class.java.classLoader)
                    val shownChild = source.readInt()
                    return SavedState(parentState, shownChild)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return Array(size) { _ -> null }
                }
            }
        }
    }
}