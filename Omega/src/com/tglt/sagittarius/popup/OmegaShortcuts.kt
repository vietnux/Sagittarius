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

package com.tglt.sagittarius.popup

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.android.launcher3.*
import com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import com.android.launcher3.model.data.*
import com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_SYSTEM_YES
import com.android.launcher3.popup.SystemShortcut
import com.tglt.sagittarius.OmegaLauncher
import com.tglt.sagittarius.preferences.OmegaPreferences
import com.tglt.sagittarius.util.hasFlag
import com.tglt.sagittarius.util.hasFlags
import com.tglt.sagittarius.views.CustomBottomSheet
import java.net.URISyntaxException

class OmegaShortcuts {

    class Customize(
        private val launcher: OmegaLauncher,
        itemInfo: ItemInfo
    ) : SystemShortcut<OmegaLauncher>(
        R.drawable.ic_edit_no_shadow,
        R.string.action_preferences, launcher, itemInfo
    ) {

        private var prefs: OmegaPreferences = Utilities.getOmegaPrefs(launcher)

        override fun onClick(v: View?) {
            if (launcher.isInState(LauncherState.ALL_APPS)) {
                if (prefs.drawerPopupEdit) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    CustomBottomSheet.show(launcher, mItemInfo)
                }
            } else if (launcher.isInState(LauncherState.NORMAL)) {
                if (prefs.desktopPopupEdit && !prefs.lockDesktop) {
                    AbstractFloatingView.closeAllOpenViews(mTarget)
                    CustomBottomSheet.show(launcher, mItemInfo)
                }
            }
        }
    }

    class AppRemove(
        private val launcher: OmegaLauncher,
        itemInfo: ItemInfo
    ) : SystemShortcut<OmegaLauncher>(
        R.drawable.ic_remove_no_shadow,
        R.string.remove_drop_target_label, launcher, itemInfo
    ) {

        override fun onClick(v: View?) {
            dismissTaskMenuView(mTarget)
            launcher.removeItem(v, mItemInfo, true)
            launcher.workspace.stripEmptyScreens()
            launcher.model.forceReload()
        }
    }

    class AppUninstall(
        private val launcher: OmegaLauncher,
        itemInfo: ItemInfo
    ) : SystemShortcut<OmegaLauncher>(
        R.drawable.ic_uninstall_no_shadow,
        R.string.uninstall_drop_target_label, launcher, itemInfo
    ) {

        override fun onClick(v: View?) {
            AbstractFloatingView.closeAllOpenViews(mTarget)
            try {
                val componentName: ComponentName? = getUninstallTarget(launcher, mItemInfo)
                val i: Intent =
                    Intent.parseUri(mTarget.getString(R.string.delete_package_intent), 0)
                        .setData(
                            Uri.fromParts(
                                "package",
                                componentName?.packageName,
                                componentName?.className
                            )
                        )
                        .putExtra(Intent.EXTRA_USER, mItemInfo.user)
                mTarget.startActivity(i)
            } catch (e: URISyntaxException) {
                Toast.makeText(launcher, R.string.uninstall_failed, Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUninstallTarget(launcher: Launcher, item: ItemInfo): ComponentName? {
            if (item.itemType == ITEM_TYPE_APPLICATION && item.id == NO_ID) {
                val intent = item.intent
                val user = item.user
                if (intent != null) {
                    val info = launcher
                        .getSystemService(LauncherApps::class.java)
                        .resolveActivity(intent, user)
                    if (info != null && !info.applicationInfo.flags.hasFlag(ApplicationInfo.FLAG_SYSTEM))
                        return info.componentName
                }
            } else {
                return item.targetComponent
            }
            return null
        }
    }

    companion object {
        val CUSTOMIZE = SystemShortcut.Factory<OmegaLauncher> { activity, itemInfo ->
            if (itemInfo.itemType == ITEM_TYPE_APPLICATION) Customize(activity, itemInfo) else null
        }

        val APP_REMOVE = SystemShortcut.Factory<OmegaLauncher> { launcher, itemInfo ->
            val prefs = Utilities.getOmegaPrefs(launcher)
            var appRemove: AppRemove? = null
            if (Launcher.getLauncher(launcher).isInState(LauncherState.NORMAL)) {
                if (itemInfo is WorkspaceItemInfo
                    || itemInfo is LauncherAppWidgetInfo
                    || itemInfo is FolderInfo
                ) {
                    if (prefs.desktopPopupRemove
                        && !prefs.lockDesktop
                    ) {
                        appRemove = AppRemove(launcher, itemInfo)
                    }
                }
            }
            appRemove
        }

        val APP_UNINSTALL = SystemShortcut.Factory<OmegaLauncher> { launcher, itemInfo ->
            val prefs = Utilities.getOmegaPrefs(launcher)
            var appUninstall: AppUninstall? = null

            if (prefs.drawerPopupUninstall && launcher.isInState(LauncherState.ALL_APPS)) {

                if (itemInfo is ItemInfoWithIcon) {
                    if (!itemInfo.runtimeStatusFlags.hasFlags(FLAG_SYSTEM_YES)) {
                        appUninstall = AppUninstall(launcher, itemInfo)
                    }
                }
            }
            appUninstall
        }
    }
}