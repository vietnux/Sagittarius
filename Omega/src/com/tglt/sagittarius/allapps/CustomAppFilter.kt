/*
 *  This file is part of Sagittarius Launcher
 *  Copyright (c) 2021   Sagittarius Launcher Team
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

package com.tglt.sagittarius.allapps

import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey

class CustomAppFilter(private val mContext: Context) : OmegaAppFilter(mContext) {

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return super.shouldShowApp(componentName, user)
                && (user == null || !isHiddenApp(mContext, ComponentKey(componentName, user)))
    }

    companion object {
        fun setComponentNameState(context: Context?, comp: String, hidden: Boolean) {
            val hiddenApps = getHiddenApps(context)
            while (hiddenApps.contains(comp)) {
                hiddenApps.remove(comp)
            }
            if (hidden) {
                hiddenApps.add(comp)
            }
            setHiddenApps(context, hiddenApps)
        }

        fun isHiddenApp(context: Context?, key: ComponentKey?): Boolean {
            return getHiddenApps(context).contains(key.toString())
        }

        // This can't be null anyway
        fun getHiddenApps(context: Context?): MutableSet<String> {
            return HashSet(Utilities.getOmegaPrefs(context).hiddenAppSet)
        }

        fun setHiddenApps(context: Context?, hiddenApps: Set<String>?) {
            Utilities.getOmegaPrefs(context).hiddenAppSet = hiddenApps!!
        }
    }
}