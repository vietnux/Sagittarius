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
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.appcompat.content.res.AppCompatResources
import com.android.launcher3.R
import com.tglt.sagittarius.dash.DashActionProvider

class ManageApps(context: Context) : DashActionProvider(context) {
    override val itemId = 7
    override val name = context.getString(R.string.tab_manage_apps)
    override val description = context.getString(R.string.dash_manage_apps_summary)

    override val icon: Drawable?
        get() = AppCompatResources.getDrawable(context, R.drawable.ic_build).apply {
            this?.setTint(darkenColor(accentColor))
        }

    override fun runAction(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
    }
}