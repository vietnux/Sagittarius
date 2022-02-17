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

package com.tglt.sagittarius.preferences.views

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.tglt.sagittarius.PREFS_PROTECTED_APPS
import com.tglt.sagittarius.PREFS_TRUST_APPS
import com.tglt.sagittarius.util.Config
import com.tglt.sagittarius.util.omegaPrefs

class PrefsDrawerFragment :
    BasePreferenceFragment(R.xml.preferences_drawer, R.string.title__general_drawer) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<SwitchPreference>(PREFS_PROTECTED_APPS)?.apply {
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    requireActivity().omegaPrefs.enableProtectedApps = newValue as Boolean
                    true
                }

            isVisible = Utilities.ATLEAST_R
        }

        findPreference<Preference>(PREFS_TRUST_APPS)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (
                    Utilities.getOmegaPrefs(requireContext()).enableProtectedApps &&
                    Utilities.ATLEAST_R
                ) {
                    Config.showLockScreen(
                        requireContext(),
                        getString(R.string.trust_apps_manager_name)
                    ) {
                        val fragment = "com.tglt.sagittarius.preferences.views.HiddenAppsFragment"
                        PreferencesActivity.startFragment(
                            context,
                            fragment,
                            context.resources.getString(R.string.title__drawer_hide_apps)
                        )
                    }
                } else {
                    val fragment = "com.tglt.sagittarius.preferences.views.HiddenAppsFragment"
                    PreferencesActivity.startFragment(
                        context,
                        fragment,
                        context.resources.getString(R.string.title__drawer_hide_apps)
                    )
                }
                false
            }
        }
    }
}