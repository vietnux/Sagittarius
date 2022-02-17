/*     This file is part of Lawnchair Launcher.
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
package com.tglt.sagittarius.gestures.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.anim.AnimatorListeners
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.views.OptionsPopupView
import com.android.launcher3.widget.picker.WidgetsFullSheet
import com.android.quickstep.SysUINavigationMode
import com.tglt.sagittarius.dash.DashBottomSheet
import com.tglt.sagittarius.gestures.GestureController
import com.tglt.sagittarius.gestures.GestureHandler
import com.tglt.sagittarius.gestures.ui.SelectAppActivity
import com.tglt.sagittarius.search.SearchProviderController
import com.tglt.sagittarius.util.getIcon
import com.tglt.sagittarius.util.omegaPrefs
import org.json.JSONObject

@Keep
open class OpenDrawerGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config),
    VerticalSwipeGestureHandler, StateChangeGestureHandler {

    override val displayName: String = context.getString(getNameRes())
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.mipmap.ic_allapps_adaptive
        )
    }
    override val requiresForeground = true

    private fun getNameRes(): Int {
        return if (SysUINavigationMode.INSTANCE.get(context).mode == SysUINavigationMode.Mode.NO_BUTTON) {
            R.string.action_open_drawer_or_recents
        } else {
            R.string.action_open_drawer
        }
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.stateManager.goToState(
            LauncherState.ALL_APPS,
            true,
            AnimatorListeners.forEndCallback(getOnCompleteRunnable(controller))
        )
    }

    open fun getOnCompleteRunnable(controller: GestureController): Runnable? {
        return Runnable { }
    }

    override fun getTargetState(): LauncherState {
        return LauncherState.ALL_APPS
    }
}

@Keep
class OpenWidgetsGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_widgets)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_widget
        )
    }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        WidgetsFullSheet.show(controller.launcher, true)
    }
}

@Keep
class OpenDashGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_dash)

    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        DashBottomSheet.show(controller.launcher, true)
    }
}

@Keep
class StartGlobalSearchGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    private val searchProvider get() = SearchProviderController.getInstance(context).searchProvider
    override val displayName: String = context.getString(R.string.action_global_search)
    override val icon: Drawable? by lazy { searchProvider.icon }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        searchProvider.startSearch {
            try {
                if (context !is AppCompatActivity) {
                    it.flags = it.flags or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(it)
            } catch (e: Exception) {
                Log.e(this::class.java.name, "Failed to start global search", e)
            }
        }
    }
}

@Keep
class StartAppSearchGestureHandler(context: Context, config: JSONObject?) :
    OpenDrawerGestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_app_search)
    override val iconResource: Intent.ShortcutIconResource
            by lazy {
                Intent.ShortcutIconResource.fromContext(
                    context,
                    R.drawable.ic_search
                )
            }

    override fun getOnCompleteRunnable(controller: GestureController): Runnable {
        return Runnable { controller.launcher.appsView.searchUiManager.startSearch() }
    }
}

@Keep
class OpenOverlayGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_overlay)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_super_g_color
        )
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(
            Intent(Intent.ACTION_MAIN).setClassName(
                PACKAGE,
                "$PACKAGE.SearchActivity"
            )
        )
    }

    companion object {
        private const val PACKAGE = "com.google.android.googlequicksearchbox"
    }
}

@Keep
class StartAppGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val hasConfig = true
    override val configIntent = Intent(context, SelectAppActivity::class.java)
    override val displayName
        get() = if (target != null)
            String.format(displayNameWithTarget, appName) else displayNameWithoutTarget
    override val icon: Drawable
        get() = when {
            intent != null -> try {
                context.packageManager.getActivityIcon(intent!!)
            } catch (e: Exception) {
                context.getIcon()
            }
            target != null -> try {
                context.packageManager.getApplicationIcon(target?.componentName!!.packageName)
            } catch (e: Exception) {
                context.getIcon()
            }
            else -> context.getIcon()
        }

    private val displayNameWithoutTarget: String = context.getString(R.string.action_open_app)
    private val displayNameWithTarget: String =
        context.getString(R.string.action_open_app_with_target)

    var type: String? = null
    var appName: String? = null
    var target: ComponentKey? = null
    var intent: Intent? = null
    var user: UserHandle? = null
    var packageName: String? = null
    var id: String? = null

    init {
        if (config?.has("appName") == true) {
            appName = config.getString("appName")
            type = if (config.has("type")) config.getString("type") else "app"
            if (type == "app") {
                Log.d("GestureController", "Class " + target.toString())
                target = Utilities.makeComponentKey(context, config.getString("target"))
            } else {
                intent = Intent.parseUri(config.getString("intent"), 0)
                user = context.getSystemService(UserManager::class.java)
                    .getUserForSerialNumber(config.getLong("user"))
                packageName = config.getString("packageName")
                id = config.getString("id")
            }
        }
    }

    override fun saveConfig(config: JSONObject) {
        super.saveConfig(config)
        config.put("appName", appName)
        config.put("type", type)
        when (type) {
            "app" -> {
                config.put("target", target.toString())
            }
            "shortcut" -> {
                config.put("intent", intent!!.toUri(0))
                config.put(
                    "user",
                    context.getSystemService(UserManager::class.java).getSerialNumberForUser(user)
                )
                config.put("packageName", packageName)
                config.put("id", id)
            }
        }
    }

    override fun onConfigResult(data: Intent?) {
        super.onConfigResult(data)
        if (data != null) {
            appName = data.getStringExtra("appName")
            type = data.getStringExtra("type")
            when (type) {
                "app" ->
                    target = Utilities
                        .makeComponentKey(context, data.getStringExtra("target") ?: "")
                "shortcut" -> {
                    intent = Intent.parseUri(data.getStringExtra("intent"), 0)
                    user = data.getParcelableExtra("user")
                    packageName = data.getStringExtra("packageName")
                    id = data.getStringExtra("id")
                }
            }
        }
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (view == null) {
            val down = controller.touchDownPoint
            controller.launcher.prepareDummyView(down.x.toInt(), down.y.toInt()) {
                onGestureTrigger(controller, controller.launcher.dummyView)
            }
            return
        }

        val opts = view.let { controller.launcher.getActivityLaunchOptionsAsBundle(it) }

        fun showErrorToast() {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_LONG).show()
        }

        when (type) {
            "app" -> {
                target?.let {
                    try {
                        context.getSystemService(LauncherApps::class.java)
                            .startMainActivity(it.componentName, it.user, null, opts)
                    } catch (e: SecurityException) {
                        showErrorToast()
                    }
                } ?: run {
                    showErrorToast()
                }
            }
            "shortcut" -> {
                Launcher.getLauncher(context).startShortcut(packageName, id, null, opts, user)
            }
        }
    }
}

@Keep
class OpenSettingsGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName = context.getString(R.string.action_open_settings)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_omega_settings)
    }

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        controller.launcher.startActivity(Intent(Intent.ACTION_APPLICATION_PREFERENCES).apply {
            `package` = controller.launcher.packageName
        })
    }
}

@Keep
class OpenOverviewGestureHandler(context: Context, config: JSONObject?) :
    GestureHandler(context, config) {

    override val displayName: String = context.getString(R.string.action_open_overview)
    override val iconResource: Intent.ShortcutIconResource by lazy {
        Intent.ShortcutIconResource.fromContext(
            context,
            R.drawable.ic_drag_handle
        )
    }
    override val requiresForeground = true

    override fun onGestureTrigger(controller: GestureController, view: View?) {
        if (context.omegaPrefs.usePopupMenuView) {
            OptionsPopupView.showDefaultOptions(
                controller.launcher,
                controller.touchDownPoint.x, controller.touchDownPoint.y
            )
        } else {
            controller.launcher.stateManager.goToState(LauncherState.OPTIONS)
        }
    }
}

interface VerticalSwipeGestureHandler {
    fun onDragStart(start: Boolean) {}
    fun onDrag(displacement: Float, velocity: Float) {}
    fun onDragEnd(velocity: Float, fling: Boolean) {}
}

interface StateChangeGestureHandler {
    fun getTargetState(): LauncherState
}
