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

package com.tglt.sagittarius.groups

import android.content.Context
import android.os.Process
import com.android.launcher3.model.data.WorkspaceItemInfo
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.LabelComparator

class ShortcutInfoComparator(context: Context) : Comparator<WorkspaceItemInfo> {

    private val userCache = UserCache.INSTANCE.get(context)
    private val myUser = Process.myUserHandle()
    private val labelComparator = LabelComparator()

    override fun compare(a: WorkspaceItemInfo, b: WorkspaceItemInfo): Int {
        // Order by the title in the current locale
        val result = labelComparator.compare(a.title.toString(), b.title.toString())
        if (result != 0) {
            return result
        }

        return if (myUser == a.user) {
            -1
        } else {
            val aUserSerial = userCache.getSerialNumberForUser(a.user)
            val bUserSerial = userCache.getSerialNumberForUser(b.user)
            aUserSerial.compareTo(bUserSerial)
        }
    }
}

