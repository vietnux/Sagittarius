/*
 *  This file is part of Sagittarius Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tglt.sagittarius.dash.actionprovider

import android.content.Context
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.tglt.sagittarius.dash.DashActionProvider
import com.tglt.sagittarius.util.omegaPrefs

class Torch(context: Context) : DashActionProvider(context) {
    override val itemId = 11
    override val name = context.getString(R.string.dash_torch)
    override val description = context.getString(R.string.dash_torch_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_torch).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override fun runAction(context: Context) {
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId = camManager!!.cameraIdList[0]
        context.omegaPrefs.torchState.let {
            context.omegaPrefs.torchState = !it
            camManager.setTorchMode(cameraId, !it)
        }
    }
}