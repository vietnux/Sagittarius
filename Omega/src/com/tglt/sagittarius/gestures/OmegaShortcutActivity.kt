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

package com.tglt.sagittarius.gestures

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.android.launcher3.icons.LauncherIcons
import com.tglt.sagittarius.gestures.ui.HandlerListAdapter
import com.tglt.sagittarius.gestures.ui.RunHandlerActivity

class OmegaShortcutActivity : AppCompatActivity() {
    private var selectedHandler: GestureHandler? = null
    private val launcherIcons by lazy { LauncherIcons.obtain(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != Intent.ACTION_CREATE_SHORTCUT) {
            finish()
        }
        setContentView(R.layout.preference_insettable_recyclerview)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.adapter = HandlerListAdapter(this, false, "", ::onSelectHandler, false)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun onSelectHandler(handler: GestureHandler) {
        selectedHandler = handler
        if (handler.configIntent != null) {
            startActivityForResult(handler.configIntent, REQUEST_CODE)
        } else {
            saveChanges()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            selectedHandler?.onConfigResult(data)
            saveChanges()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveChanges() {
        val shortcutIntent = Intent(this, RunHandlerActivity::class.java).apply {
            action = START_ACTION
            `package` = packageName
            putExtra(EXTRA_HANDLER, selectedHandler.toString())
        }
        val icon = if (selectedHandler?.icon != null)
            launcherIcons.createScaledBitmapWithoutShadow(
                selectedHandler?.icon,
                Build.VERSION.SDK_INT
            )
        else null

        // TODO move to use ShortcutManager.createShortcutResultIntent()
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, selectedHandler?.displayName)
            if (icon != null)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
            else
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, selectedHandler?.iconResource)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val START_ACTION = "com.tglt.sagittarius.START_ACTION"
        const val EXTRA_HANDLER = "com.tglt.sagittarius.EXTRA_HANDLER"
        const val REQUEST_CODE = 1337
    }
}