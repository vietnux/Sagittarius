/*
 *  This file is part of Sagittarius Launcher
 *  Copyright (c) 2021   Saul Henriquez
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

package com.tglt.sagittarius.preferences

import android.content.Context
import android.content.SharedPreferences
import com.android.launcher3.R
import com.android.launcher3.Utilities.makeComponentKey
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.MainThreadInitializedObject
import com.android.launcher3.util.Themes
import com.tglt.sagittarius.*
import com.tglt.sagittarius.groups.AppGroupsManager
import com.tglt.sagittarius.groups.DrawerTabs
import com.tglt.sagittarius.icons.CustomAdaptiveIconDrawable
import com.tglt.sagittarius.icons.IconShape
import com.tglt.sagittarius.icons.IconShapeManager
import com.tglt.sagittarius.search.SearchProviderController
import com.tglt.sagittarius.smartspace.SmartSpaceDataWidget
import com.tglt.sagittarius.smartspace.eventprovider.BatteryStatusProvider
import com.tglt.sagittarius.smartspace.eventprovider.NotificationUnreadProvider
import com.tglt.sagittarius.smartspace.eventprovider.NowPlayingProvider
import com.tglt.sagittarius.smartspace.eventprovider.PersonalityProvider
import com.tglt.sagittarius.theme.ThemeManager
import com.tglt.sagittarius.util.Temperature

class OmegaPreferences(val context: Context) : BasePreferences(context) {

    private val onIconShapeChanged = {
        initializeIconShape()
        com.android.launcher3.graphics.IconShape.init(context)
        idp.onPreferencesChanged(context)
    }

    fun initializeIconShape() {
        val shape = iconShape
        CustomAdaptiveIconDrawable.sInitialized = true
        CustomAdaptiveIconDrawable.sMaskId = shape.getHashString()
        CustomAdaptiveIconDrawable.sMask = shape.getMaskPath()
    }

    // DESKTOP
    val desktopIconScale by FloatPref(PREFS_DESKTOP_ICON_SCALE, 1f, reloadIcons)
    val usePopupMenuView by BooleanPref(PREFS_DESKTOP_POPUP_MENU, true, doNothing)
    var dashProviders = StringListPref(
        PREFS_DASH_PROVIDERS,
        listOf("17", "15", "4", "6", "8", "5"), doNothing
    )
    val lockDesktop by BooleanPref(PREFS_DESKTOP_LOCK, false, reloadAll)
    val hideStatusBar by BooleanPref(PREFS_STATUSBAR_HIDE, false, doNothing)
    val enableMinus by BooleanPref(PREFS_ENABLE_MINUS, false, restart)
    var allowEmptyScreens by BooleanPref(PREFS_EMPTY_SCREENS, false)
    val hideAppLabels by BooleanPref(PREFS_DESKTOP_HIDE_LABEL, false, reloadApps)
    val desktopTextScale by FloatPref(PREFS_DESKTOP_ICON_TEXT_SCALE, 1f, reloadApps)
    val allowFullWidthWidgets by BooleanPref(PREFS_WIDGETS_FULL_WIDTH, false, restart)
    private val homeMultilineLabel by BooleanPref(
        PREFS_DESKTOP_ICON_LABEL_TWOLINES,
        false,
        reloadApps
    )
    val homeLabelRows get() = if (homeMultilineLabel) 2 else 1

    // DOCK
    var dockHide by BooleanPref(PREFS_DOCK_HIDE, false, restart)
    val dockIconScale by FloatPref(PREFS_DOCK_ICON_SCALE, 1f, restart)
    var dockScale by FloatPref(PREFS_DOCK_SCALE, 1f, restart)
    val dockBackground by BooleanPref(PREFS_DOCK_BACKGROUND, false, restart)
    val dockBackgroundColor by IntPref(PREFS_DOCK_BACKGROUND_COLOR, 0x101010, restart)
    var dockOpacity by AlphaPref(PREFS_DOCK_OPACITY, -1, restart)
    var dockSearchBar by BooleanPref("pref_dock_search", false, restart)

    // DRAWER
    var sortMode by StringIntPref(PREFS_SORT, 0, restart)
    var hiddenAppSet by StringSetPref(PREFS_HIDDEN_SET, setOf(), reloadApps)
    var hiddenPredictionAppSet by StringSetPref(PREFS_HIDDEN_PREDICTION_SET, setOf(), doNothing)
    var protectedAppsSet by StringSetPref(PREFS_PROTECTED_SET, setOf(), reloadApps)
    var enableProtectedApps by BooleanPref(PREFS_PROTECTED_APPS, false)
    var allAppsIconScale by FloatPref(PREFS_DRAWER_ICON_SCALE, 1f, reloadApps)
    val allAppsTextScale by FloatPref(PREFS_DRAWER_ICON_TEXT_SCALE, 1f)
    val hideAllAppsAppLabels by BooleanPref(PREFS_DRAWER_HIDE_LABEL, false, reloadApps)
    private val drawerMultilineLabel by BooleanPref(
        PREFS_DRAWER_ICON_LABEL_TWOLINES,
        false,
        reloadApps
    )
    val drawerLabelRows get() = if (drawerMultilineLabel) 2 else 1
    val allAppsCellHeightMultiplier by FloatPref(PREFS_DRAWER_HEIGHT_MULTIPLIER, 1F, restart)
    val separateWorkApps by BooleanPref(PREFS_WORK_PROFILE_SEPARATED, false, recreate)
    val appGroupsManager by lazy { AppGroupsManager(this) }
    val drawerTabs get() = appGroupsManager.drawerTabs
    val currentTabsModel
        get() = appGroupsManager.getEnabledModel() as? DrawerTabs ?: appGroupsManager.drawerTabs

    val saveScrollPosition by BooleanPref(PREFS_KEEP_SCROLL_STATE, false, doNothing)

    val customAppName =
        object : MutableMapPref<ComponentKey, String>("pref_appNameMap", reloadAll) {
            override fun flattenKey(key: ComponentKey) = key.toString()
            override fun unflattenKey(key: String) = makeComponentKey(context, key)
            override fun flattenValue(value: String) = value
            override fun unflattenValue(value: String) = value
        }

    // THEME
    var launcherTheme by StringIntPref(
        PREFS_THEME,
        ThemeManager.getDefaultTheme()
    ) { ThemeManager.getInstance(context).updateTheme() }
    val accentColor by IntPref(PREFS_ACCENT, (0xffff1744).toInt(), doNothing)
    var enableBlur by BooleanPref(PREFS_BLUR, false, updateBlur)
    var blurRadius by IntPref(PREFS_BLUR_RADIUS, 75, updateBlur)
    var customWindowCorner by BooleanPref(PREFS_WINDOWCORNER, false, doNothing)
    var windowCornerRadius by FloatPref(PREFS_WINDOWCORNER_RADIUS, 8f, updateBlur)
    var iconPackPackage by StringPref(PREFS_ICON_PACK, "", reloadIcons)

    var iconShape by StringBasedPref(
        PREFS_ICON_SHAPE, IconShape.Circle, onIconShapeChanged,
        {
            IconShape.fromString(it) ?: IconShapeManager.getSystemIconShape(context)
        }, IconShape::toString
    ) { /* no dispose */ }
    var coloredBackground by BooleanPref(PREFS_COLORED_BACKGROUND, false, doNothing)
    var enableWhiteOnlyTreatment by BooleanPref(PREFS_WHITE_TREATMENT, false, doNothing)
    var enableLegacyTreatment by BooleanPref(PREFS_LEGACY_TREATMENT, false, doNothing)
    var adaptifyIconPacks by BooleanPref(PREFS_FORCE_ADAPTIVE, false, doNothing)
    var forceShapeless by BooleanPref(PREFS_FORCE_SHAPELESS, false, doNothing)

    // SEARCH & FOLDER
    var searchBarRadius by DimensionPref("pref_searchbar_radius", -1f, recreate)
    var allAppsGlobalSearch by BooleanPref("pref_allAppsGlobalSearch", true, doNothing)
    var searchProvider by StringPref(PREFS_SEARCH_PROVIDER, "") {
        SearchProviderController.getInstance(context).onSearchProviderChanged()
    }
    var folderRadius by DimensionPref(PREFS_FOLDER_RADIUS, -1f, restart)
    val customFolderBackground by BooleanPref(PREFS_FOLDER_BACKGROUND_CUSTOM, false, restart)
    val folderBackground by IntPref(
        PREFS_FOLDER_BACKGROUND,
        Themes.getAttrColor(context, R.attr.folderFillColor),
        restart
    )
    val folderColumns by FloatPref("pref_folder_columns", 4f, reloadIcons)
    val folderRows by FloatPref("pref_folder_rows", 4f, reloadIcons)

    // GESTURES & NOTIFICATION
    val notificationCount: Boolean by BooleanPref(PREFS_NOTIFICATION_COUNT, false, restart)
    val notificationCustomColor: Boolean by BooleanPref(
        PREFS_NOTIFICATION_BACKGROUND_CUSTOM,
        false,
        restart
    )
    val notificationBackground by IntPref(
        PREFS_NOTIFICATION_BACKGROUND,
        R.color.notification_background,
        restart
    )
    val folderBadgeCount by BooleanPref(PREFS_NOTIFICATION_COUNT_FOLDER, true, recreate)

    /*
    * Preferences not used. Added to register the change and restart only
    */
    var doubleTapGesture by StringPref(PREFS_GESTURE_DOUBLE_TAP, "", restart)
    var longPressGesture by StringPref(PREFS_GESTURE_LONG_PRESS, "", restart)
    var homePressGesture by StringPref(PREFS_GESTURE_HOME, "", restart)
    var backPressGesture by StringPref(PREFS_GESTURE_BACK, "", restart)
    var swipeDownGesture by StringPref(PREFS_GESTURE_SWIPE_DOWN, "", restart)
    var swipeUpGesture by StringPref(PREFS_GESTURE_SWIPE_UP, "", restart)
    var dockSwipeUpGesture by StringPref(PREFS_GESTURE_SWIPE_UP_DOCK, "", restart)
    var launchAssistantGesture by StringPref(PREFS_GESTURE_ASSISTANT, "", restart)

    // ADVANCED
    var language by StringPref(PREFS_LANGUAGE, "", restart)
    var firstRun by BooleanPref(PREFS_FIRST_RUN, true)

    // DEVELOPER PREFERENCES
    var developerOptionsEnabled by BooleanPref(PREFS_DEV_PREFS_SHOW, false, restart)
    var desktopModeEnabled by BooleanPref(PREFS_DESKTOP_MODE, true, restart)
    private val lowPerformanceMode by BooleanPref(PREFS_LOW_PREFORMANCE, false, restart)
    val enablePhysics get() = !lowPerformanceMode
    val showDebugInfo by BooleanPref(PREFS_DEBUG_MODE, true, doNothing)

    // FEED
    var feedProvider by StringPref(PREFS_FEED_PROVIDER, "", restart)
    val ignoreFeedWhitelist by BooleanPref(PREFS_FEED_PROVIDER_ALLOW_ALL, true, restart)

    // SMARTSPACE
    var usePillQsb by BooleanPref(PREF_PILL_QSB, false, recreate)
    val enableSmartspace by BooleanPref(PREFS_SMARTSPACE_ENABLE, false, recreate)
    val smartspaceTime by BooleanPref(PREFS_SMARTSPACE_TIME, false, recreate)
    val smartspaceDate by BooleanPref(PREFS_SMARTSPACE_DATE, true, recreate)
    val smartspaceTimeAbove by BooleanPref(PREFS_SMARTSPACE_TIME_ABOVE, false, recreate)
    val smartspaceTime24H by BooleanPref(PREFS_TIME_24H, false, recreate)
    val weatherUnit by StringBasedPref(
        "pref_weather_units", Temperature.Unit.Celsius, ::updateSmartspaceProvider,
        Temperature.Companion::unitFromString, Temperature.Companion::unitToString
    ) { }
    var smartspaceWidgetId by IntPref("smartspace_widget_id", -1, doNothing)
    var weatherIconPack by StringPref("pref_weatherIcons", "", doNothing)
    var weatherProvider by StringPref(
        "pref_smartspace_widget_provider",
        SmartSpaceDataWidget::class.java.name, ::updateSmartspaceProvider
    )
    var eventProvider by StringPref(
        "pref_smartspace_event_provider",
        SmartSpaceDataWidget::class.java.name, ::updateSmartspaceProvider
    )
    var eventProviders = StringListPref(
        "pref_smartspace_event_providers", listOf(
            eventProvider,
            NotificationUnreadProvider::class.java.name,
            NowPlayingProvider::class.java.name,
            BatteryStatusProvider::class.java.name,
            PersonalityProvider::class.java.name
        ),
        ::updateSmartspaceProvider
    )

    var torchState by BooleanPref(PREFS_TORCH, false, doNothing)

    // POPUP DIALOG PREFERENCES
    val desktopPopupEdit by BooleanPref(PREFS_DESKTOP_POPUP_EDIT, true, doNothing)
    val desktopPopupRemove by BooleanPref(PREFS_DESKTOP_POPUP_REMOVE, false, doNothing)
    val drawerPopupEdit by BooleanPref(PREFS_DRAWER_POPUP_EDIT, true, doNothing)
    val drawerPopupUninstall by BooleanPref(PREFS_DRAWER_POPUP_UNINSTALL, false, doNothing)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        onChangeMap[key]?.invoke()
        onChangeListeners[key]?.toSet()?.forEach { it.onValueChanged(key, this, false) }
    }

    fun addOnPreferenceChangeListener(listener: OnPreferenceChangeListener, vararg keys: String) {
        keys.forEach { addOnPreferenceChangeListener(it, listener) }
    }

    fun addOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        if (onChangeListeners[key] == null) {
            onChangeListeners[key] = HashSet()
        }
        onChangeListeners[key]?.add(listener)
        listener.onValueChanged(key, this, true)
    }

    fun removeOnPreferenceChangeListener(
        listener: OnPreferenceChangeListener,
        vararg keys: String
    ) {
        keys.forEach { removeOnPreferenceChangeListener(it, listener) }
    }

    fun removeOnPreferenceChangeListener(key: String, listener: OnPreferenceChangeListener) {
        onChangeListeners[key]?.remove(listener)
    }

    interface OnPreferenceChangeListener {
        fun onValueChanged(key: String, prefs: OmegaPreferences, force: Boolean)
    }

    interface MutableListPrefChangeListener {
        fun onListPrefChanged(key: String)
    }

    companion object {

        @JvmField
        val INSTANCE = MainThreadInitializedObject(::OmegaPreferences)

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE.get(context)!!

    }
}