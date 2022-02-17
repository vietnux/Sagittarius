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
import com.android.launcher3.AppFilter
import com.tglt.sagittarius.OmegaLauncher

open class OmegaAppFilter(context: Context) : AppFilter() {

    private val mHideList = HashSet<ComponentName>()

    init {
        mHideList.add(ComponentName(context, OmegaLauncher::class.java.name))
        //Voice Search
        mHideList.add(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/.VoiceSearchActivity")!!)
        //Google Now Launcher
        mHideList.add(ComponentName.unflattenFromString("com.google.android.launcher/.StubApp")!!)
        //Actions Services
        mHideList.add(ComponentName.unflattenFromString("com.google.android.as/com.google.android.apps.miphone.aiai.allapps.main.MainDummyActivity")!!)
    }

    override fun shouldShowApp(componentName: ComponentName?, user: UserHandle?): Boolean {
        return !mHideList.contains(componentName) && super.shouldShowApp(componentName, user)
    }
}