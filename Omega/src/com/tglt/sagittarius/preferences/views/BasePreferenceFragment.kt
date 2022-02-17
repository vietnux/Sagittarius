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

package com.tglt.sagittarius.preferences.views

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.tglt.sagittarius.preferences.custom.CustomDialogPreference
import com.tglt.sagittarius.preferences.custom.EventProvidersPreference
import com.tglt.sagittarius.smartspace.EventProvidersFragment

abstract class BasePreferenceFragment(val layoutId: Int, val titleId: Int = -1) :
    PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(layoutId, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val f: DialogFragment
        parentFragmentManager.let {
            f = when (preference) {
                is CustomDialogPreference -> {
                    PreferenceDialogFragment.newInstance(preference)
                }

                is EventProvidersPreference -> {
                    EventProvidersFragment.newInstance(preference.key)
                }
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }
            f.setTargetFragment(this, 0)
            f.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG")
        }
    }

    override fun onResume() {
        super.onResume()
        if (titleId != -1) requireActivity().title = requireActivity().getString(titleId)
    }
}