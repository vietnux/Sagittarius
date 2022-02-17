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

package com.tglt.sagittarius.search.providers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.res.ResourcesCompat
import com.android.launcher3.R
import com.tglt.sagittarius.search.SearchProvider
import com.tglt.sagittarius.util.Config
import com.tglt.sagittarius.util.isAppEnabled

@Keep
class GoogleSearchProvider(context: Context) : SearchProvider(context) {

    override val name = context.getString(R.string.google_app)
    override val supportsVoiceSearch = true
    override val supportsAssistant = true
    override val isAvailable: Boolean
        get() = context.packageManager.isAppEnabled(Config.GOOGLE_QSB, 0)
    override val supportsFeed = true

    override val packageName: String
        get() = Config.GOOGLE_QSB
    override val isBroadcast: Boolean
        get() = true

    override fun startSearch(callback: (intent: Intent) -> Unit) =
            callback(Intent().setClassName(Config.GOOGLE_QSB, "${Config.GOOGLE_QSB}.SearchActivity"))

    override fun startVoiceSearch(callback: (intent: Intent) -> Unit) =
            callback(
                    Intent("android.intent.action.VOICE_ASSIST").addFlags(268468224)
                            .setPackage(Config.GOOGLE_QSB)
            )

    override fun startAssistant(callback: (intent: Intent) -> Unit) =
            callback(
                    Intent(Intent.ACTION_VOICE_COMMAND).addFlags(268468224).setPackage(Config.GOOGLE_QSB)
            )

    override fun startFeed(callback: (intent: Intent) -> Unit) {


        callback(
                Intent(Intent.ACTION_MAIN).setClassName(
                    Config.GOOGLE_QSB,
                    "${Config.GOOGLE_QSB}.SearchActivity"
                )
            )

    }

    override val icon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_super_g_color, null)!!

    override val voiceIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_mic_color, null)!!

    override val assistantIcon: Drawable
        get() = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_assistant, null)!!
}
