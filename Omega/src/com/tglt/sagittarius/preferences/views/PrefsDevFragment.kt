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
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.farmerbb.taskbar.lib.Taskbar
import com.tglt.sagittarius.PREFS_DESKTOP_MODE_SETTINGS
import com.tglt.sagittarius.PREFS_KILL
import com.tglt.sagittarius.theme.ThemeOverride

class PrefsDevFragment :
    BasePreferenceFragment(R.xml.preferences_dev, R.string.developer_options_title) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>(PREFS_KILL)?.setOnPreferenceClickListener {
            Utilities.killLauncher()
            false
        }
        findPreference<Preference>(PREFS_DESKTOP_MODE_SETTINGS)?.setOnPreferenceClickListener {
            Taskbar.openSettings(
                requireContext(),
                ThemeOverride.Settings().getTheme(requireContext())
            )
            true
        }
    }
}