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

package com.tglt.sagittarius.smartspace.eventprovider

import android.service.notification.StatusBarNotification
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.R
import com.android.launcher3.notification.NotificationInfo
import com.android.launcher3.notification.NotificationMainView.NOTIFICATION_ITEM_INFO
import com.android.launcher3.util.PackageUserKey
import com.tglt.sagittarius.flowerpot.Flowerpot
import com.tglt.sagittarius.flowerpot.FlowerpotApps
import com.tglt.sagittarius.smartspace.OmegaSmartSpaceController
import com.tglt.sagittarius.smartspace.OmegaSmartSpaceController.CardData
import com.tglt.sagittarius.smartspace.OmegaSmartSpaceController.Line
import com.tglt.sagittarius.util.loadSmallIcon
import com.tglt.sagittarius.util.runOnMainThread
import com.tglt.sagittarius.util.runOnUiWorkerThread

@Keep
class NotificationUnreadProvider(controller: OmegaSmartSpaceController) :
    OmegaSmartSpaceController.NotificationBasedDataProvider(controller),
    NotificationsManager.OnChangeListener {

    private val manager = NotificationsManager
    private var flowerpotLoaded = false
    private var flowerpotApps: FlowerpotApps? = null
    private val tmpKey = PackageUserKey(null, null)
    private var zenModeEnabled = false
        set(value) {
            if (field != value) {
                field = value
                onNotificationsChanged()
            }
        }
    private val zenModeListener = ZenModeListener(controller.context.contentResolver) {
        zenModeEnabled = it
    }

    override fun startListening() {
        super.startListening()

        manager.addListener(this)
        zenModeListener.startListening()
        runOnUiWorkerThread {
            flowerpotApps = Flowerpot.Manager.getInstance(controller.context)
                .getPot("COMMUNICATION", true)?.apps
            flowerpotLoaded = true
            onNotificationsChanged()
        }
    }

    override fun onNotificationsChanged() {
        runOnMainThread {
            updateData(null, getEventCard())
        }
    }

    private fun isCommunicationApp(sbn: StatusBarNotification): Boolean {
        return tmpKey.updateFromNotification(sbn)
                && flowerpotApps?.packageMatches?.contains(tmpKey) != false
    }

    private fun getEventCard(): CardData? {
        if (!flowerpotLoaded) return null
        val sbn = manager.notifications
            .asSequence()
            .filter { !it.isOngoing }
            .filter { it.notification.priority >= PRIORITY_DEFAULT }
            .filter { isCommunicationApp(it) }
            .maxWithOrNull(
                compareBy(
                    { it.notification.priority },
                    { it.notification.`when` })
            ) ?: return null

        if (zenModeEnabled) {
            return CardData(
                AppCompatResources.getDrawable(context, R.drawable.ic_zen_mode)!!.toBitmap(),
                listOf(Line(context.getString(R.string.zen_mode_enabled)))
            )
        }

        val context = controller.context
        val notif = NotificationInfo(context, sbn, NOTIFICATION_ITEM_INFO)
        val app = getApp(sbn).toString()
        val title = notif.title?.toString() ?: ""
        val splitted = splitTitle(title)
        val body = notif.text?.toString()?.trim()?.split("\n")?.firstOrNull() ?: ""

        val lines = mutableListOf<Line>()
        if (!TextUtils.isEmpty(body)) {
            lines.add(Line(body))
        }
        lines.addAll(splitted.reversed().map { Line(it) })

        val appLine = Line(app)
        if (!lines.contains(appLine)) {
            lines.add(appLine)
        }
        return CardData(
            sbn.loadSmallIcon(context)?.toBitmap(), lines,
            OmegaSmartSpaceController.NotificationClickListener(sbn)
        )
    }

    private fun splitTitle(title: String): Array<String> {
        val delimiters = arrayOf(": ", " - ", " • ")
        for (del in delimiters) {
            if (title.contains(del)) {
                return title.split(del.toRegex(), 2).toTypedArray()
            }
        }
        return arrayOf(title)
    }

    override fun stopListening() {
        super.stopListening()
        manager.removeListener(this)
        zenModeListener.stopListening()
    }
}

