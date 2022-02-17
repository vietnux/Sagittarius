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

package com.tglt.sagittarius.gestures.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.shortcuts.ShortcutKey
import com.tglt.sagittarius.OmegaLayoutInflater
import com.tglt.sagittarius.preferences.AppsShortcutsAdapter
import com.tglt.sagittarius.theme.ThemeManager
import com.tglt.sagittarius.theme.ThemeOverride
import com.tglt.sagittarius.util.omegaPrefs
import com.tglt.sagittarius.util.recreateAnimated

class SelectAppActivity : AppCompatActivity(), ThemeManager.ThemeableActivity,
    AppsShortcutsAdapter.Callback {
    private lateinit var themeOverride: ThemeOverride
    private val themeSet: ThemeOverride.ThemeSet get() = ThemeOverride.Settings()
    private val customLayoutInflater by lazy {
        OmegaLayoutInflater(super.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater, this)
    }

    override var currentTheme = 0
    override var currentAccent = 0
    private var paused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeOverride = ThemeOverride(themeSet, this)
        themeOverride.applyTheme(this)
        currentTheme = themeOverride.getTheme(this)
        currentAccent = omegaPrefs.accentColor

        setContentView(R.layout.preference_insettable_recyclerview)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = AppsShortcutsAdapter(this, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAppSelected(app: AppsShortcutsAdapter.AppItem) {
        setResult(RESULT_OK, Intent().apply {
            putExtra("type", "app")
            putExtra("appName", app.info.label)
            putExtra("target", app.key.toString())
        })
        finish()
    }

    override fun onShortcutSelected(shortcut: AppsShortcutsAdapter.ShortcutItem) {
        setResult(RESULT_OK, Intent().apply {
            putExtra("type", "shortcut")
            putExtra("appName", shortcut.label)
            putExtra("intent", ShortcutKey.makeIntent(shortcut.info).toUri(0))
            putExtra("user", shortcut.info.userHandle)
            putExtra("packageName", shortcut.info.`package`)
            putExtra("id", shortcut.info.id)
        })
        finish()
    }

    override fun onThemeChanged() {
        if (currentTheme == themeOverride.getTheme(this)) return
        if (paused) {
            recreate()
        } else {
            recreateAnimated()
        }
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun getSystemService(name: String): Any? {
        if (name == Context.LAYOUT_INFLATER_SERVICE) {
            return customLayoutInflater
        }
        return super.getSystemService(name)
    }

}
