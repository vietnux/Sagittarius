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

package com.tglt.sagittarius.gestures.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.android.launcher3.util.ActivityTracker
import com.tglt.sagittarius.OmegaLauncher
import com.tglt.sagittarius.gestures.BlankGestureHandler
import com.tglt.sagittarius.gestures.GestureController
import com.tglt.sagittarius.gestures.GestureHandler
import com.tglt.sagittarius.gestures.OmegaShortcutActivity

class RunHandlerActivity : AppCompatActivity() {
    private val fallback by lazy { BlankGestureHandler(this, null) }
    private val launcher
        get() = (LauncherAppState.getInstance(this).launcher as? OmegaLauncher)
    private val controller
        get() = launcher?.gestureController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == OmegaShortcutActivity.START_ACTION) {
            val handlerString = intent.getStringExtra(OmegaShortcutActivity.EXTRA_HANDLER)
            if (handlerString != null) {
                val handler = GestureController.createGestureHandler(
                    this.applicationContext,
                    handlerString,
                    fallback
                )
                if (handler.requiresForeground) {
                    val homeIntent =
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .setPackage(packageName)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    RunHandlerActivityTracker(handler).addToIntent(homeIntent)
                    startActivity(homeIntent)
                } else {
                    triggerGesture(handler)
                }
            }
        } else if (intent.action == Intent.ACTION_ASSIST || intent.action == Intent.ACTION_SEARCH_LONG_PRESS) {
            controller?.onLaunchAssistant()
        }
        finish()
    }

    private fun triggerGesture(handler: GestureHandler) = if (controller != null) {
        handler.onGestureTrigger(controller!!)
    } else {
        Toast.makeText(this.applicationContext, R.string.failed, Toast.LENGTH_LONG).show()
    }

    class RunHandlerActivityTracker(private val handler: GestureHandler) :
        ActivityTracker.SchedulerCallback<OmegaLauncher> {
        override fun init(activity: OmegaLauncher, alreadyOnHome: Boolean): Boolean {
            handler.onGestureTrigger(activity.gestureController)
            return true
        }
    }
}