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
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.tglt.sagittarius.allapps.OmegaAppFilter
import com.tglt.sagittarius.allapps.ProtectedAppsAdapter

class HiddenAppsFragment : Fragment(), ProtectedAppsAdapter.Callback {
    private lateinit var adapter: ProtectedAppsAdapter
    val layoutId = R.layout.activity_hidden_apps

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(container!!.context).inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRecyclerViewCreated(view.findViewById(R.id.iconList))
    }

    private fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        val hiddenApps = Utilities.getOmegaPrefs(requireContext())::hiddenAppSet
        val protectedApps = Utilities.getOmegaPrefs(requireContext())::protectedAppsSet
        adapter = ProtectedAppsAdapter.ofProperty(
                requireContext(),
                hiddenApps, protectedApps, this, OmegaAppFilter(requireContext())
        )

        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onSelectionsChanged(newSize: Int) {
        requireActivity().title = if (newSize > 0) {
            "$newSize${getString(R.string.hide_app_selected)}"
        } else {
            getString(R.string.title__drawer_hide_apps)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_hide_apps, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                adapter.clearSelection()
                true
            }
            R.id.action_reset_protected -> {
                adapter.clearProtectedApps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}