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

package com.tglt.sagittarius.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.tglt.sagittarius.iconpack.IconPackProvider
import com.tglt.sagittarius.preferences.OmegaPreferences

class IconPackPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs),
        OmegaPreferences.OnPreferenceChangeListener {
    private val prefs = Utilities.getOmegaPrefs(context)
    val packs = IconPackProvider.INSTANCE.get(context).getIconPackList()
    private val current
        get() = packs.firstOrNull { it.packageName == prefs.iconPackPackage }
                ?: packs[0]

    init {
        layoutResource = R.layout.preference_preview_icon
        updateSummaryAndIcon()
    }

    override fun onAttached() {
        super.onAttached()
        prefs.addOnPreferenceChangeListener("pref_icon_pack_package", this)
    }

    override fun onDetached() {
        super.onDetached()
        prefs.removeOnPreferenceChangeListener("pref_icon_pack_package", this)
    }

    override fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean) {
        updateSummaryAndIcon()
    }

    private fun updateSummaryAndIcon() {
        summary = current.name
        icon = current.icon
    }
}