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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.android.launcher3.Insettable
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.material.snackbar.Snackbar
import com.tglt.sagittarius.blur.BlurDrawable
import com.tglt.sagittarius.blur.BlurWallpaperProvider
import com.tglt.sagittarius.util.getBooleanAttr
import com.tglt.sagittarius.util.getColorAttr
import com.tglt.sagittarius.util.getDimenAttr
import com.tglt.sagittarius.util.parents
import java.io.File

@SuppressLint("ViewConstructor")
class DecorLayout(context: Context) : InsettableFrameLayout(context, null),
        View.OnClickListener, BlurWallpaperProvider.Listener {

    private var tapCount = 0

    private val contentFrame: View

    private val shouldDrawBackground by lazy { context.getBooleanAttr(android.R.attr.windowShowWallpaper) }
    private val settingsBackground by lazy { context.getColorAttr(R.attr.settingsBackground) }

    private val contentTop
        get() = when {
            else -> context.getDimenAttr(R.attr.actionBarSize)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.decor_layout, this)

        contentFrame = findViewById(android.R.id.content)

        onEnabledChanged()

        if (shouldDrawBackground) {
            setWillNotDraw(false)
        }
    }

    override fun onEnabledChanged() {
        val enabled = BlurWallpaperProvider.isEnabled
        (background as? BlurDrawable)?.let {
            if (!enabled) {
                it.stopListening()
                background = null
            }
        } ?: if (enabled) {
            background = BlurWallpaperProvider.getInstance(context).createDrawable().apply {
                startListening()
            }
        } else {

        }
    }

    override fun draw(canvas: Canvas) {
        if (shouldDrawBackground) canvas.drawColor(settingsBackground)
        super.draw(canvas)
    }

    override fun onClick(v: View?) {
        if (tapCount == 6 && allowDevOptions()) {
            Utilities.getOmegaPrefs(context).developerOptionsEnabled = true
            Snackbar.make(
                    findViewById(R.id.content),
                    R.string.developer_options_enabled,
                    Snackbar.LENGTH_LONG).show()
            tapCount++
        } else if (tapCount < 6) {
            tapCount++
        }
    }

    private fun allowDevOptions(): Boolean {
        return try {
            File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "Omega/dev").exists()
        } catch (e: SecurityException) {
            false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        BlurWallpaperProvider.getInstance(context).addListener(this)
        (background as BlurDrawable?)?.startListening()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        BlurWallpaperProvider.getInstance(context).removeListener(this)
        (background as BlurDrawable?)?.stopListening()
    }

    class ContentFrameLayout(context: Context, attrs: AttributeSet?) : InsettableFrameLayout(context, attrs) {

        private var decorLayout: DecorLayout? = null

        private val contentPath = Path()
        internal val dividerPath = Path()
        internal val backScrimPath = Path()
        internal val frontScrimPath = Path()

        private val selfRect = RectF()
        private val insetsRect = RectF()
        private val contentRect = RectF()

        private val dividerSize = Utilities.pxFromDp(1f, resources.displayMetrics).toFloat()

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            decorLayout = parents.first { it is DecorLayout } as DecorLayout
        }

        override fun setInsets(insets: Rect) {
            decorLayout?.also {
                setInsetsInternal(Rect(
                        insets.left,
                        insets.top + it.contentTop,
                        insets.right,
                        insets.bottom))
            } ?: setInsetsInternal(insets)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            if (changed) {
                selfRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                computeClip()
            }
        }

        private fun setInsetsInternal(insets: Rect) {
            super.setInsets(insets)
            insetsRect.set(insets)
            computeClip()
        }

        private fun computeClip() {
            contentRect.set(
                    selfRect.left + insetsRect.left,
                    selfRect.top + insetsRect.top,
                    selfRect.right - insetsRect.right,
                    selfRect.bottom - insetsRect.bottom
            )

            dividerPath.reset()
            when {
                isNavBarToRightEdge() -> dividerPath.addRect(
                        contentRect.right,
                        selfRect.top,
                        contentRect.right + dividerSize,
                        selfRect.bottom,
                        Path.Direction.CW)
                isNavBarToLeftEdge() -> dividerPath.addRect(
                        contentRect.left - dividerSize,
                        selfRect.top,
                        contentRect.left,
                        selfRect.bottom,
                        Path.Direction.CW)
                else -> dividerPath.addRect(
                        selfRect.left,
                        contentRect.bottom,
                        selfRect.right,
                        contentRect.bottom + dividerSize,
                        Path.Direction.CW)
            }

            contentPath.reset()
            contentPath.addRect(contentRect, Path.Direction.CW)

            frontScrimPath.reset()
            frontScrimPath.addRect(selfRect, Path.Direction.CW)
            frontScrimPath.op(contentPath, Path.Op.DIFFERENCE)
            frontScrimPath.op(dividerPath, Path.Op.DIFFERENCE)

            backScrimPath.reset()
            backScrimPath.addRect(selfRect, Path.Direction.CW)
            backScrimPath.op(frontScrimPath, Path.Op.DIFFERENCE)

            invalidate()
        }

        private fun isNavBarToRightEdge(): Boolean {
            return insetsRect.bottom == 0f && insetsRect.right > 0
        }

        private fun isNavBarToLeftEdge(): Boolean {
            return insetsRect.bottom == 0f && insetsRect.left > 0
        }
    }

    class BackScrimView(context: Context, attrs: AttributeSet?) : View(context, attrs), Insettable {

        private val parent by lazy { parents.first { it is ContentFrameLayout } as ContentFrameLayout }

        override fun onFinishInflate() {
            super.onFinishInflate()
            background = background.mutate().apply { alpha = 230 }
        }

        override fun draw(canvas: Canvas) {
            val count = canvas.save()
            canvas.clipPath(parent.backScrimPath)
            super.draw(canvas)
            canvas.restoreToCount(count)
        }

        override fun setInsets(insets: Rect?) {
            // ignore this
        }
    }

    class FrontScrimView(context: Context, attrs: AttributeSet?) : View(context, attrs), Insettable {

        private val parent by lazy { parents.first { it is ContentFrameLayout } as ContentFrameLayout }

        override fun onFinishInflate() {
            super.onFinishInflate()
            background = background.mutate().apply { alpha = 230 }
        }

        override fun draw(canvas: Canvas) {
            val count = canvas.save()
            canvas.clipPath(parent.frontScrimPath)
            super.draw(canvas)
            canvas.restoreToCount(count)
            canvas.drawPath(parent.dividerPath, Paint().apply { color = 0x1f000000 })
        }

        override fun setInsets(insets: Rect?) {
            // ignore this
        }
    }
}