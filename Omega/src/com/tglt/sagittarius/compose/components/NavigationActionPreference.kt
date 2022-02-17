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

package com.tglt.sagittarius.compose.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationActionPreference(
    label: String,
    subtitle: String? = null,
    destination: String,
    startWidget: (@Composable () -> Unit)? = null
) {
    val navController = rememberAnimatedNavController()
    CompositionLocalProvider(
        LocalNavController provides navController,
    ) {
        PreferenceItem(
            modifier = Modifier.clickable { navController.navigate(route = destination) },
            title = { Text(text = label) },
            description = { subtitle?.let { Text(text = it) } },
            startWidget = startWidget,
            showDivider = false
        )
    }
}

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}
