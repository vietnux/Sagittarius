/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tglt.sagittarius.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.Utilities
import com.android.launcher3.util.TouchController
import com.tglt.sagittarius.util.forEachChildReversed
import com.tglt.sagittarius.views.SettingsBottomSheet
import com.tglt.sagittarius.views.ViewScrim

class SettingsDragLayer(context: Context, attrs: AttributeSet?) : InsettableFrameLayout(context, attrs) {

    private val mTmpXY = FloatArray(2)
    private val mHitRect = Rect()

    private var activeController: TouchController? = null

    fun getTopOpenView(): SettingsBottomSheet? {
        forEachChildReversed {
            if (it is SettingsBottomSheet) {
                return it
            }
        }
        return null
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return findActiveController(ev)
    }

    private fun findActiveController(ev: MotionEvent): Boolean {
        activeController = null
        getTopOpenView()?.let {
            if (it.onControllerInterceptTouchEvent(ev)) {
                activeController = it
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        activeController?.let {
            return it.onControllerTouchEvent(event)
        }
        return findActiveController(event)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        ViewScrim[child]?.draw(canvas, width, height)
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun fitSystemWindows(insets: Rect): Boolean {
        setInsets(insets)
        return true
    }

    fun isEventOverView(view: View, ev: MotionEvent): Boolean {
        getDescendantRectRelativeToSelf(view, mHitRect)
        return mHitRect.contains(ev.x.toInt(), ev.y.toInt())
    }

    fun getDescendantRectRelativeToSelf(descendant: View, r: Rect): Float {
        mTmpXY[0] = 0F
        mTmpXY[1] = 0F
        val scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY)

        r.set(mTmpXY[0].toInt(), mTmpXY[1].toInt(),
                (mTmpXY[0] + scale * descendant.measuredWidth).toInt(),
                (mTmpXY[1] + scale * descendant.measuredHeight).toInt())
        return scale
    }

    private fun getDescendantCoordRelativeToSelf(descendant: View, coord: FloatArray): Float {
        return getDescendantCoordRelativeToSelf(descendant, coord, false)
    }

    private fun getDescendantCoordRelativeToSelf(descendant: View, coord: FloatArray,
                                                 includeRootScroll: Boolean): Float {
        return Utilities.getDescendantCoordRelativeToAncestor(descendant, this,
                coord, includeRootScroll)
    }
}
