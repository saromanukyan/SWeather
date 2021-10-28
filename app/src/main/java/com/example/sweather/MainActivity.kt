package com.example.sweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val appID = "dab3af44de7d24ae7ff86549334e45bd"
    private val url = "https://api.openweathermap.org/data/2.5/weather"
    private val minTime: Long = 5000
    private val minDistance = 1000f
    private val requestCode = 101

    private var locationProvider = LocationManager.GPS_PROVIDER

    private lateinit var cityName: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var temperature: TextView
    private lateinit var conditionIcon: TextView
    private lateinit var searchCity: EditText

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        weatherCondition = findViewById(R.id.weatherCondition)
        temperature = findViewById(R.id.textViewCelsius)
        conditionIcon = findViewById(R.id.conditionIcon)
        cityName = findViewById(R.id.textViewCity)

        searchCity = findViewById(R.id.searchCity)
        searchCity.setOnEditorActionListener { _, _, _ ->
            val newCity = searchCity.text.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("City", newCity)
            startActivity(intent)
            finish()
            true
        }
        cityName.setOnClickListener {
            searchCity.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val city = intent.getStringExtra("City")
        if (city != null) {
            newCityWeather(city)
        } else {
            currentCityWeather()
        }
    }

    private fun newCityWeather(city: String) {
        val params = RequestParams()
        params.put("q", city)
        params.put("appid", appID)
        networkRequest(params)
    }

    private fun currentCityWeather() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude.toString()
                val longitude = location.longitude.toString()
                val params = RequestParams()
                params.put("lat", latitude)
                params.put("lon", longitude)
                params.put("appid", appID)
                networkRequest(params)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
            return
        }
        locationManager.requestLocationUpdates(
            locationProvider,
            minTime,
            minDistance,
            locationListener
        )
    }


    private fun networkRequest(params: RequestParams) {
        val asyncHttpClient = AsyncHttpClient()
        asyncHttpClient.get(url, params, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header?>?,
                response: JSONObject?
            ) {
                val weatherData: Weather? = response?.let { Weather.fromJson(it) }
                weatherData?.let { update(it) }

            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header?>?,
                throwable: Throwable?,
                errorResponse: JSONObject?
            ) {
            }
        })
    }

    private fun update(weather: Weather) {
        temperature.text = weather.getTemperature()
        cityName.setText(weather.city)
        weatherCondition.text = weather.getWeatherType()
        val imageID = resources.getIdentifier(weather.icon, "drawable", packageName)
        conditionIcon.setBackgroundResource(imageID)
    }
}