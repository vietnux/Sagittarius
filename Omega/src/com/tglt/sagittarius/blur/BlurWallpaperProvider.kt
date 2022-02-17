/*
 *  Copyright (c) 2020 Sagittarius Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.tglt.sagittarius.blur

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.util.Executors.MAIN_EXECUTOR
import com.tglt.sagittarius.util.*

class BlurWallpaperProvider(val context: Context) {

    private val prefs by lazy { context.omegaPrefs }
    private val mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)
    private val mListeners = ArrayList<Listener>()
    private val mDisplayMetrics = DisplayMetrics()
    var wallpaper: Bitmap? = null
        private set(value) {
            if (field != value) {
                field?.recycle()
                field = value
            }
        }
    var placeholder: Bitmap? = null
        private set(value) {
            if (field != value) {
                field?.recycle()
                field = value
            }
        }
    private var mOffset: Float = 0.6f

    private val mVibrancyPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val mColorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mPath = Path()

    private var mWallpaperWidth: Int = 0
    private var mDisplayHeight: Int = 0
    var wallpaperYOffset: Float = 0f
        private set
    private val sCanvas = Canvas()

    private val mUpdateRunnable = Runnable { updateWallpaper() }

    private val wallpaperFilter = BlurWallpaperFilter(context)
    private var applyTask: WallpaperFilter.ApplyTask? = null

    private var updatePending = false

    init {
        isEnabled = getEnabledStatus()

        wallpaperFilter.applyPrefs(prefs)
        updateAsync()
    }

    private fun getEnabledStatus() = mWallpaperManager.wallpaperInfo == null && prefs.enableBlur

    @SuppressLint("MissingPermission")
    private fun updateWallpaper() {
        if (applyTask != null) {
            updatePending = true
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !context.hasStoragePermission) {
            prefs.enableBlur = false
            return
        }
        val enabled = getEnabledStatus()
        if (enabled != isEnabled) {
            isEnabled = enabled
            runOnMainThread {
                mListeners.safeForEach(Listener::onEnabledChanged)
            }
        }

        if (!isEnabled) {
            wallpaper = null
            placeholder = null
            return
        }

        wallpaperFilter.applyPrefs(prefs)

        var wallpaper = try {
            Utilities.drawableToBitmap(mWallpaperManager.drawable, true) as Bitmap
        } catch (e: Exception) {
            prefs.enableBlur = false
            runOnMainThread {
                val msg = "${context.getString(R.string.failed)}: ${e.message}"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                notifyWallpaperChanged()
            }
            return
        }
        wallpaper = scaleToScreenSize(wallpaper)
        val wallpaperHeight = wallpaper.height
        wallpaperYOffset = if (wallpaperHeight > mDisplayHeight) {
            (wallpaperHeight - mDisplayHeight) * 0.5f
        } else {
            0f
        }

        mWallpaperWidth = wallpaper.width

        placeholder = createPlaceholder(wallpaper.width, wallpaper.height)
        wallpaper = applyVibrancy(wallpaper)
        Log.d("BWP", "starting blur")

        applyTask = wallpaperFilter.apply(wallpaper).setCallback { result, error ->
            if (error == null) {
                this@BlurWallpaperProvider.wallpaper = result
                Log.d("BWP", "blur done")
                runOnMainThread(::notifyWallpaperChanged)
                wallpaper.recycle()
            } else {
                if (error is OutOfMemoryError) {
                    prefs.enableBlur = false
                    runOnMainThread {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_LONG).show()
                        notifyWallpaperChanged()
                    }
                }
                wallpaper.recycle()
            }
            applyTask = null
            if (updatePending) {
                updatePending = false
                updateWallpaper()
            }
        }
    }

    private fun notifyWallpaperChanged() {
        mListeners.forEach(Listener::onWallpaperChanged)
    }

    private fun scaleToScreenSize(bitmap: Bitmap): Bitmap {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        display?.getRealMetrics(mDisplayMetrics)

        val width = mDisplayMetrics.widthPixels
        val height = mDisplayMetrics.heightPixels
        mDisplayHeight = height

        val widthFactor = width.toFloat() / bitmap.width
        val heightFactor = height.toFloat() / bitmap.height

        val upscaleFactor = widthFactor.coerceAtLeast(heightFactor)
        if (upscaleFactor <= 0) {
            return bitmap
        }

        val scaledWidth = width.coerceAtLeast((bitmap.width * upscaleFactor).ceilToInt())
        val scaledHeight = height.coerceAtLeast((bitmap.height * upscaleFactor).ceilToInt())
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false)
    }

    private fun createPlaceholder(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sCanvas.setBitmap(bitmap)

        mPath.moveTo(0f, 0f)
        mPath.lineTo(0f, height.toFloat())
        mPath.lineTo(width.toFloat(), height.toFloat())
        mPath.lineTo(width.toFloat(), 0f)
        mColorPaint.xfermode = null
        mColorPaint.color = tintColor
        sCanvas.drawPath(mPath, mColorPaint)

        return bitmap
    }

    val tintColor: Int
        get() = 0x45ffffff // TODO: replace this with theme attr
//        get() = Utilities.resolveAttributeData(context, R.attr.blurTintColor)

    fun updateAsync() {
        MAIN_EXECUTOR.execute(mUpdateRunnable)
    }

    private fun applyVibrancy(wallpaper: Bitmap): Bitmap {
        val width = wallpaper.width
        val height = wallpaper.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(1.25f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        mVibrancyPaint.colorFilter = filter
        canvas.drawBitmap(wallpaper, 0f, 0f, mVibrancyPaint)

        wallpaper.recycle()

        return bitmap
    }

    fun addListener(listener: Listener) {
        mListeners.add(listener)
        listener.onOffsetChanged(mOffset)
    }

    fun removeListener(listener: Listener) {
        mListeners.remove(listener)
    }

    fun createDrawable(): BlurDrawable {
        return ShaderBlurDrawable(this)
    }

    fun createDrawable(radius: Float): BlurDrawable {
        return ShaderBlurDrawable(this)
            .apply { blurRadii = BlurDrawable.Radii(radius) }
    }

    fun createDrawable(topRadius: Float, bottomRadius: Float): BlurDrawable {
        return ShaderBlurDrawable(this)
            .apply { blurRadii = BlurDrawable.Radii(topRadius, bottomRadius) }
    }

    fun setWallpaperOffset(offset: Float) {
        if (!isEnabled) return
        if (wallpaper == null) return

        val availw = mDisplayMetrics.widthPixels - mWallpaperWidth
        var xPixels = availw / 2

        if (availw < 0)
            xPixels += (availw * (offset - .5f) + .5f).toInt()

        mOffset = Utilities.boundToRange(
            (-xPixels).toFloat(),
            0f, (mWallpaperWidth - mDisplayMetrics.widthPixels).toFloat()
        )

        for (listener in ArrayList(mListeners)) {
            listener.onOffsetChanged(mOffset)
        }
    }

    interface Listener {

        fun onWallpaperChanged() {}
        fun onOffsetChanged(offset: Float) {}
        fun setUseTransparency(useTransparency: Boolean) {}
        fun onEnabledChanged() {}
    }

    companion object :
        SingletonHolder<BlurWallpaperProvider, Context>(ensureOnMainThread(useApplicationContext(::BlurWallpaperProvider))) {
        const val DOWNSAMPLE_FACTOR = 8

        var isEnabled: Boolean = false
        private var sEnabledFlag: Int = 0

        fun isEnabled(flag: Int): Boolean {
            return isEnabled && sEnabledFlag and flag != 0
        }
    }
}
