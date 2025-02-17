/*
 * Copyright 2021, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tglt.sagittarius.smartspace

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.util.Log
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.tglt.sagittarius.util.Config
import com.tglt.sagittarius.util.SingletonHolder
import com.tglt.sagittarius.util.ensureOnMainThread
import com.tglt.sagittarius.util.useApplicationContext

class FeedBridge(private val context: Context) {

    private val shouldUseFeed =
        context.applicationInfo.flags and (FLAG_DEBUGGABLE or FLAG_SYSTEM) == 0
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }
    private val bridgePackages by lazy {
        listOf(
            PixelBridgeInfo(
                "com.google.android.apps.nexuslauncher",
                R.integer.bridge_signature_hash
            ),
            BridgeInfo("app.lawnchair.lawnfeed", R.integer.lawnfeed_signature_hash)
        )
    }

    fun resolveBridge(): BridgeInfo? {
        val customBridge = customBridgeOrNull()
        return when {
            customBridge != null -> customBridge
            !shouldUseFeed -> null
            else -> bridgePackages.firstOrNull { it.isAvailable() }
        }
    }

    private fun customBridgeOrNull() = if (prefs.feedProvider.isNotBlank()) {
        val bridge = CustomBridgeInfo(prefs.feedProvider)
        if (bridge.isAvailable()) {
            bridge
        } else null
    } else null

    private fun customBridgeAvailable() = customBridgeOrNull()?.isAvailable() == true

    fun isInstalled(): Boolean {
        return customBridgeAvailable() || !shouldUseFeed || bridgePackages.any { it.isAvailable() }
    }

    fun resolveSmartspace(): String {
        return bridgePackages.firstOrNull { it.supportsSmartspace }?.packageName
            ?: Config.GOOGLE_QSB
    }

    open inner class BridgeInfo(val packageName: String, signatureHashRes: Int) {

        protected open val signatureHash =
            if (signatureHashRes > 0) context.resources.getInteger(signatureHashRes) else 0

        open val supportsSmartspace = true

        fun isAvailable(): Boolean {
            val info = context.packageManager.resolveService(
                Intent(overlayAction)
                    .setPackage(packageName)
                    .setData(
                        Uri.parse(
                            StringBuilder(packageName.length + 18)
                                .append("app://")
                                .append(packageName)
                                .append(":")
                                .append(Process.myUid())
                                .toString()
                        )
                            .buildUpon()
                            .appendQueryParameter("v", "7")
                            .appendQueryParameter("cv", "9")
                            .build()
                    ), 0
            )
            return info != null && isSigned()
        }

        open fun isSigned(): Boolean {
            if (Utilities.ATLEAST_P) {
                val info = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = info.signingInfo
                if (signingInfo.hasMultipleSigners()) return false
                return signingInfo.signingCertificateHistory.any { it.hashCode() == signatureHash }
            } else {
                val info = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures.forEach {
                    if (it.hashCode() != signatureHash) return false
                }
                return info.signatures.isNotEmpty()
            }
        }
    }

    private inner class CustomBridgeInfo(packageName: String) : BridgeInfo(packageName, 0) {
        override val signatureHash = whitelist[packageName]?.toInt() ?: -1
        private val disableWhitelist = prefs.ignoreFeedWhitelist
        override fun isSigned(): Boolean {
            if (signatureHash == -1 && Utilities.ATLEAST_P) {
                val info = context.packageManager
                    .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                if (signingInfo.hasMultipleSigners()) return false
                signingInfo.signingCertificateHistory.forEach {
                    val hash = it.hashCode().toString(16)
                    Log.d("FeedBridge", "Feed provider $packageName(0x$hash) isn't whitelisted")
                }
            }
            return disableWhitelist || signatureHash != -1 && super.isSigned()
        }
    }

    private inner class PixelBridgeInfo(packageName: String, signatureHashRes: Int) :
        BridgeInfo(packageName, signatureHashRes) {

        override val supportsSmartspace get() = isAvailable()
    }

    companion object : SingletonHolder<FeedBridge, Context>(
        ensureOnMainThread(
            useApplicationContext(::FeedBridge)
        )
    ) {

        private const val TAG = "FeedBridge"
        private const val overlayAction = "com.android.launcher3.WINDOW_OVERLAY"

        private val whitelist = mapOf<String, Long>(
            "ua.itaysonlab.homefeeder" to 0x887456ed, // HomeFeeder, t.me/homefeeder
            "launcher.libre.dev" to 0x2e9dbab5 // Librechair, t.me/librechair
        )

        fun getAvailableProviders(context: Context) = context.packageManager
            .queryIntentServices(
                Intent(overlayAction).setData(
                    Uri.parse("app://" + context.packageName)
                ), PackageManager.GET_META_DATA
            )
            .map { it.serviceInfo.applicationInfo }
            .distinct()
            .filter { getInstance(context).CustomBridgeInfo(it.packageName).isSigned() }

        @JvmStatic
        fun useBridge(context: Context) =
            getInstance(context).let { it.shouldUseFeed || it.customBridgeAvailable() }
    }
}