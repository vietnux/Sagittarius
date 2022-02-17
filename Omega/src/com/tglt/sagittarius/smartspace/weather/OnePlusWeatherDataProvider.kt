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
package com.tglt.sagittarius.smartspace.weather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Criteria
import android.location.LocationManager
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import com.android.launcher3.R
import com.android.launcher3.util.PackageManagerHelper
import com.tglt.sagittarius.perms.CustomPermissionManager
import com.tglt.sagittarius.smartspace.OmegaSmartSpaceController
import com.tglt.sagittarius.smartspace.weather.icons.WeatherIconManager
import com.tglt.sagittarius.twilight.TwilightManager
import com.tglt.sagittarius.util.*
import net.oneplus.launcher.OPWeatherProvider
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.concurrent.TimeUnit

@Keep
class OnePlusWeatherDataProvider(controller: OmegaSmartSpaceController) :
    OmegaSmartSpaceController.DataProvider(controller), OPWeatherProvider.IWeatherCallback {

    private val provider by lazy { OPWeatherProvider(context) }
    private val locationAccess by lazy { context.checkLocationAccess() }
    private val locationManager: LocationManager? by lazy {
        if (locationAccess) {
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        } else null
    }
    private val ipLocation = IPLocation(context)

    init {
        if (!isAvailable(context)) {
            throw RuntimeException("OP provider is not available")
        }
    }

    override fun startListening() {
        provider.registerContentObserver(context.contentResolver)
        provider.subscribeCallback(this)
        super.startListening()
    }

    override fun forceUpdate() {
        super.forceUpdate()
        provider.getCurrentWeatherInformation(this)
    }

    override fun onWeatherUpdated(weatherData: OPWeatherProvider.WeatherData) {
        if (locationAccess) {
            update(weatherData)
        } else {
            CustomPermissionManager.getInstance(context).checkOrRequestPermission(
                CustomPermissionManager.PERMISSION_IPLOCATE,
                R.string.permission_iplocate_twilight_explanation
            ) {
                // update regardless of the result
                update(weatherData)
            }
        }
    }

    private fun update(weatherData: OPWeatherProvider.WeatherData) {
        runOnUiWorkerThread {
            val weather = OmegaSmartSpaceController.WeatherData(
                getConditionIcon(weatherData),
                Temperature(weatherData.temperature, getTemperatureUnit(weatherData)),
                forecastIntent = Intent().setClassName(
                    OPWeatherProvider.WEATHER_PACKAGE_NAME,
                    OPWeatherProvider.WEATHER_LAUNCH_ACTIVITY
                )
            )
            runOnMainThread { updateData(weather, null) }
        }
    }

    private fun getConditionIcon(data: OPWeatherProvider.WeatherData): Bitmap {
        // let's never again forget that unix timestamps is seconds, not millis
        val c = Calendar.getInstance()
            .apply { timeInMillis = TimeUnit.SECONDS.toMillis(data.timestamp) }
        var isDay = c.get(HOUR_OF_DAY) in 6 until 20
        if (locationAccess) {
            locationManager?.getBestProvider(Criteria(), true)?.let { provider ->
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return
                }
                locationManager?.getLastKnownLocation(provider)?.let { location ->
                    isDay = TwilightManager.calculateTwilightState(
                        location.latitude,
                        location.longitude,
                        c.timeInMillis
                    ).isNight != true
                }
            }
        } else {
            val res = ipLocation.get()
            if (res.success) {
                isDay = TwilightManager.calculateTwilightState(
                    res.lat,
                    res.lon,
                    c.timeInMillis
                ).isNight != true
            }
        }

        return WeatherIconManager.getInstance(context).getIcon(data.icon, !isDay)
    }

    private fun getTemperatureUnit(data: OPWeatherProvider.WeatherData): Temperature.Unit {
        return if (data.temperatureUnit == OPWeatherProvider.TEMP_UNIT_FAHRENHEIT) {
            Temperature.Unit.Fahrenheit
        } else Temperature.Unit.Celsius
    }


    override fun stopListening() {
        super.stopListening()
        provider.unregisterContentObserver(context.contentResolver)
        provider.unsubscribeCallback(this)
    }

    companion object {

        fun isAvailable(context: Context): Boolean {
            return PackageManagerHelper.isAppEnabled(
                context.packageManager,
                OPWeatherProvider.WEATHER_PACKAGE_NAME,
                0
            )
        }
    }
}
