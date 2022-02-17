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

package com.tglt.sagittarius.gestures

import android.content.Context
import android.graphics.PointF
import android.text.TextUtils
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.android.launcher3.util.TouchController
import com.tglt.sagittarius.OmegaLauncher
import com.tglt.sagittarius.gestures.gestures.*
import com.tglt.sagittarius.gestures.handlers.*
import com.tglt.sagittarius.util.omegaPrefs
import org.json.JSONException
import org.json.JSONObject

class GestureController(val launcher: OmegaLauncher) : TouchController {

    private val prefs = launcher.omegaPrefs
    val blankGestureHandler = BlankGestureHandler(launcher, null)
    private val doubleTapGesture by lazy { DoubleTapGesture(this) }
    private val pressHomeGesture by lazy { PressHomeGesture(this) }
    private val pressBackGesture by lazy { PressBackGesture(this) }
    private val longPressGesture by lazy { LongPressGesture(this) }
    private val assistantGesture by lazy { LaunchAssistantGesture(this) }

    val hasBackGesture
        get() = pressBackGesture.handler !is BlankGestureHandler
    val verticalSwipeGesture by lazy { VerticalSwipeGesture(this) }
    val navSwipeUpGesture by lazy { NavSwipeUpGesture(this) }

    var touchDownPoint = PointF()

    private var swipeUpOverride: Pair<GestureHandler, Long>? = null

    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun onControllerTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    fun attachDoubleTapListener(gestureDetector: GestureDetector) {
        gestureDetector.setOnDoubleTapListener(doubleTapGesture.createDoubleTapListener())
    }

    fun onLongPress() {
        longPressGesture.isEnabled && longPressGesture.onEvent()
    }

    fun onPressHome() {
        pressHomeGesture.isEnabled && pressHomeGesture.onEvent()
    }

    fun onPressBack() {
        pressBackGesture.isEnabled && pressBackGesture.onEvent()
    }

    fun onLaunchAssistant() {
        assistantGesture.isEnabled && assistantGesture.onEvent()
    }

    fun setSwipeUpOverride(handler: GestureHandler, downTime: Long) {
        if (swipeUpOverride?.second != downTime) {
            swipeUpOverride = Pair(handler, downTime)
        }
    }

    fun getSwipeUpOverride(downTime: Long): GestureHandler? {
        swipeUpOverride?.let {
            if (it.second == downTime) {
                return it.first
            } else {
                swipeUpOverride = null
            }
        }
        return null
    }

    fun createHandlerPref(key: String, defaultValue: GestureHandler = blankGestureHandler) =
        prefs.StringBasedPref(
            key,
            defaultValue,
            prefs.doNothing,
            ::createGestureHandler,
            GestureHandler::toString,
            GestureHandler::onDestroy
        )

    private fun createGestureHandler(jsonString: String) =
        createGestureHandler(launcher, jsonString, blankGestureHandler)

    companion object {

        private const val TAG = "GestureController"
        private val LEGACY_SLEEP_HANDLERS = listOf(
            "com.tglt.sagittarius.gestures.handlers.SleepGestureHandlerDeviceAdmin",
            "com.tglt.sagittarius.gestures.handlers.SleepGestureHandlerAccessibility"
        )

        fun createGestureHandler(
            context: Context,
            jsonString: String?,
            fallback: GestureHandler
        ): GestureHandler {
            if (!TextUtils.isEmpty(jsonString)) {
                val config: JSONObject? = try {
                    JSONObject(jsonString ?: "{ }")
                } catch (e: JSONException) {
                    null
                }
                var className = config?.getString("class") ?: jsonString
                if (className in LEGACY_SLEEP_HANDLERS) {
                    className = SleepGestureHandler::class.java.name
                }
                val configValue =
                    if (config?.has("config") == true) config.getJSONObject("config") else null
                try {
                    val handler = Class.forName(className)
                        .getConstructor(Context::class.java, JSONObject::class.java)
                        .newInstance(context, configValue) as GestureHandler
                    if (handler.isAvailable) return handler
                } catch (t: Throwable) {
                    Log.e(TAG, "can't create gesture handler", t)
                }
            }
            return fallback
        }

        fun getClassName(jsonString: String): String {
            val config: JSONObject? = try {
                JSONObject(jsonString)
            } catch (e: JSONException) {
                null
            }
            val className = config?.getString("class") ?: jsonString
            return if (className in LEGACY_SLEEP_HANDLERS) {
                SleepGestureHandler::class.java.name
            } else {
                className
            }
        }

        fun getGestureHandlers(context: Context, isSwipeUp: Boolean, hasBlank: Boolean) =
            mutableListOf(
                PressBackGestureHandler(context, null),
                SleepGestureHandler(context, null),
                SleepGestureHandlerTimeout(context, null),
                OpenDashGestureHandler(context, null),
                OpenDrawerGestureHandler(context, null),
                OpenWidgetsGestureHandler(context, null),
                NotificationsOpenGestureHandler(context, null),
                OpenOverlayGestureHandler(context, null),
                OpenOverviewGestureHandler(context, null),
                StartGlobalSearchGestureHandler(context, null),
                StartAppSearchGestureHandler(context, null),
                StartAppGestureHandler(context, null),
                OpenSettingsGestureHandler(context, null)
            ).apply {
                if (hasBlank) {
                    add(0, BlankGestureHandler(context, null))
                }
            }.filter { it.isAvailableForSwipeUp(isSwipeUp) }
    }
}
