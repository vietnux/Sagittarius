/*
 * Copyright (c) 2020 Sagittarius Launcher
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
 */

package com.tglt.sagittarius.gestures.handlers

import android.view.View
import android.view.animation.DecelerateInterpolator
import com.android.launcher3.R
import com.android.launcher3.touch.OverScroll
import com.tglt.sagittarius.gestures.GestureController
import com.tglt.sagittarius.gestures.GestureHandler

class ViewSwipeUpGestureHandler(private val view: View, private val handler: GestureHandler) :
    GestureHandler(view.context, null), VerticalSwipeGestureHandler {

    private val negativeMax by lazy { view.resources.getDimensionPixelSize(R.dimen.swipe_up_negative_max) }
    private val positiveMax by lazy { view.resources.getDimensionPixelSize(R.dimen.swipe_up_positive_max) }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.prepareDummyView(this.view) {
            handler.onGestureTrigger(controller, it)
        }
    }

    override fun onDrag(displacement: Float, velocity: Float) {
        view.translationY = OverScroll.dampedScroll(
            displacement, if (displacement < 0)
                negativeMax else positiveMax
        ).toFloat()
    }

    override fun onDragEnd(velocity: Float, fling: Boolean) {
        view.animate().translationY(0f).setDuration(100).setInterpolator(DecelerateInterpolator())
            .start()
    }

    override val displayName = ""
}
