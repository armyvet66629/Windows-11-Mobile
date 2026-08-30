package com.example.windows11mobile.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

data class WeatherData(
    val temperature: Float,
    val condition: String,
    val locationName: String,
    val highTemp: Float,
    val lowTemp: Float,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList()
)

data class HourlyForecast(val time: String, val temp: Float, val icon: String)
data class DailyForecast(val day: String, val temp: Float, val icon: String)

class WeatherRepository(private val context: Context) {
    private val _weather = MutableStateFlow<WeatherData?>(null)
    val weather: StateFlow<WeatherData?> = _weather

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun updateWeather(useFahrenheit: Boolean = false) = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return@withContext
        }

        try {
            var location = fusedLocationClient.lastLocation.await()
            
            if (location == null) {
                // Request fresh location if lastLocation is null
                val cts = CancellationTokenSource()
                location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
            }

            if (location == null) return@withContext

            val geocoder = Geocoder(context, Locale.getDefault())
            
            // Modern Geocoder API for Android 33+
            val cityName = if (android.os.Build.VERSION.SDK_INT >= 33) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()?.locality ?: "Unknown"
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()?.locality ?: "Unknown"
            }

            val unit = if (useFahrenheit) "fahrenheit" else "celsius"
            val url = "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current_weather=true&hourly=temperature_2m,weathercode&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=auto&temperature_unit=$unit"
            val response = URL(url).readText()
            val json = JSONObject(response)

            val current = json.getJSONObject("current_weather")
            val daily = json.getJSONObject("daily")
            val hourly = json.getJSONObject("hourly")

            val hourlyList = mutableListOf<HourlyForecast>()
            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val codes = hourly.getJSONArray("weathercode")
            for (i in 0 until 24) {
                hourlyList.add(HourlyForecast(
                    time = times.getString(i).substringAfter("T"),
                    temp = temps.getDouble(i).toFloat(),
                    icon = getWeatherCondition(codes.getInt(i))
                ))
            }

            val dailyList = mutableListOf<DailyForecast>()
            val days = daily.getJSONArray("time")
            val maxTemps = daily.getJSONArray("temperature_2m_max")
            val dailyCodes = daily.getJSONArray("weathercode")
            for (i in 0 until 5) {
                val dateStr = days.getString(i)
                dailyList.add(DailyForecast(
                    day = formatToShortDay(dateStr),
                    temp = maxTemps.getDouble(i).toFloat(),
                    icon = getWeatherCondition(dailyCodes.getInt(i))
                ))
            }
            
            val weatherData = WeatherData(
                temperature = current.getDouble("temperature").toFloat(),
                condition = getWeatherCondition(current.getInt("weathercode")),
                locationName = cityName,
                highTemp = maxTemps.getDouble(0).toFloat(),
                lowTemp = daily.getJSONArray("temperature_2m_min").getDouble(0).toFloat(),
                hourlyForecast = hourlyList,
                dailyForecast = dailyList
            )
            _weather.value = weatherData
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            71, 73, 75 -> "Snowy"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }

    private fun formatToShortDay(dateStr: String): String {
        return try {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: return dateStr
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Mon"
                java.util.Calendar.TUESDAY -> "Tues"
                java.util.Calendar.WEDNESDAY -> "Wed"
                java.util.Calendar.THURSDAY -> "Thur"
                java.util.Calendar.FRIDAY -> "Fri"
                java.util.Calendar.SATURDAY -> "Sat"
                java.util.Calendar.SUNDAY -> "Sun"
                else -> ""
            }
        } catch (_: Exception) {
            dateStr
        }
    }
}
