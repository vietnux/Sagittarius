/*
 * This file is part of Sagittarius Launcher
 * Copyright (c) 2022   Sagittarius Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tglt.sagittarius.search

import android.content.Context
import android.util.AttributeSet
import com.android.launcher3.DeviceProfile
import com.android.launcher3.LauncherAppState


class HotseatQsbLayout(context: Context, attrs: AttributeSet? = null) :
    AbstractQsbLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val requestedWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val idp = LauncherAppState.getIDP(mContext)!!
        val dp: DeviceProfile = idp.getDeviceProfile(mContext)
        val cellWidth = DeviceProfile.calculateCellWidth(
            requestedWidth,
            dp.cellLayoutBorderSpacingPx,
            dp.numShownHotseatIcons
        )
        val width = requestedWidth - (cellWidth - Math.round(dp.iconSizePx * 0.92f))
        setMeasuredDimension(width, height)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            }
        }
    }
}