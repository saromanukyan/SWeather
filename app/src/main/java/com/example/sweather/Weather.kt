package com.example.sweather

import org.json.JSONException
import org.json.JSONObject
import kotlin.math.round

class Weather {
    private var temperature: String? = null
    var icon: String? = null
    var city: String? = null
    private var weatherType: String? = null
    private var weatherCondition = 0
    fun getTemperature(): String {
        return "$temperature°C"
    }

    fun getWeatherType(): String? {
        return weatherType
    }

    companion object {
        fun fromJson(jsonObject: JSONObject): Weather? {
            return try {
                val weatherData = Weather()
                weatherData.city = jsonObject.getString("name")
                weatherData.weatherCondition =
                    jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id")
                weatherData.weatherType =
                    jsonObject.getJSONArray("weather").getJSONObject(0).getString("main")
                weatherData.icon = changeWeatherIcon(weatherData.weatherCondition)
                val tempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15
                val roundedValue = round(tempResult).toInt()
                weatherData.temperature = roundedValue.toString()
                weatherData
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }

        private fun changeWeatherIcon(conditionCode: Int): String = when (conditionCode) {
            in 0..232 -> "thunderstorm"
            in 300..500 -> "rain"
            in 501..600 -> "shower"
            in 600..615 -> "snow"
            in 616..622, 903 -> "heavy_snow"
            in 701..771 -> "fog"
            in 772..781, 800 -> "cloudy"
            904 -> "sunny"
            in 801..804 -> "few_clouds"
            else -> "storm"
        }
    }

}
