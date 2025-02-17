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

package com.tglt.sagittarius.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory.DIRECTION_BOTTOM
import com.tglt.sagittarius.util.getColorAttr
import com.tglt.sagittarius.util.omegaPrefs
import java.lang.reflect.Field

open class SpringNestedScrollView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private val springManager = SpringEdgeEffect.Manager(this)
    private val scrollBarColor by lazy {
        val colorControlNormal = context.getColorAttr(android.R.attr.colorControlNormal)
        val useAccentColor = colorControlNormal == context.omegaPrefs.accentColor
        if (useAccentColor) context.omegaPrefs.accentColor else colorControlNormal
    }

    open var shouldTranslateSelf = true

    var isTopFadingEdgeEnabled = true

    init {
        getField<NestedScrollView>("mEdgeGlowTop").set(this, springManager.createEdgeEffect(DIRECTION_BOTTOM, true))
        getField<NestedScrollView>("mEdgeGlowBottom").set(this, springManager.createEdgeEffect(DIRECTION_BOTTOM))
        overScrollMode = View.OVER_SCROLL_ALWAYS
    }

    override fun draw(canvas: Canvas) {
        springManager.withSpring(canvas, shouldTranslateSelf) {
            super.draw(canvas)
            false
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        springManager.withSpring(canvas, !shouldTranslateSelf) {
            super.dispatchDraw(canvas)
            false
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isTopFadingEdgeEnabled) super.getTopFadingEdgeStrength() else 0f
    }

    inline fun <reified T> getField(name: String): Field {
        return T::class.java.getDeclaredField(name).apply {
            isAccessible = true
        }
    }
}

