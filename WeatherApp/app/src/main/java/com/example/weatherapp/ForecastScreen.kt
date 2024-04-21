package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class WeatherForecast(
    val city: String,
    val forecastList: List<ForecastItem>
)

data class ForecastItem(
    val dateTime: Long,
    val temperature: Double
)

class ForecastScreen : AppCompatActivity() {

    private val CITY: String = "tampere,fi"
    private val API: String = "383da520e096daa3458eaf5b9ca7f91f" // Use API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast_screen)

        FetchWeatherForecastTask().execute()

        findViewById<Button>(R.id.backtomain).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Optional: finish the ForecastScreen activity when returning to MainActivity
        }
    }

    inner class FetchWeatherForecastTask : AsyncTask<Void, Void, WeatherForecast>() {

        override fun doInBackground(vararg params: Void?): WeatherForecast? {
            val urlString = "https://api.openweathermap.org/data/2.5/forecast?q=$CITY&appid=$API"
            val result = URL(urlString).readText()
            return parseWeatherForecast(result)
        }

        override fun onPostExecute(result: WeatherForecast?) {
            super.onPostExecute(result)
            result?.let { weatherForecast ->
                // Update UI with weather forecast data
                updateUI(weatherForecast)
            }
        }

        private fun parseWeatherForecast(jsonString: String): WeatherForecast {
            val jsonObject = JSONObject(jsonString)
            val city = jsonObject.getJSONObject("city").getString("name")
            val forecastList = mutableListOf<ForecastItem>()
            val jsonArray = jsonObject.getJSONArray("list")
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val dateTime = item.getLong("dt")
                val main = item.getJSONObject("main")
                val beforetemp = main.getDouble("temp")
                val temperature = beforetemp - 273.15
                forecastList.add(ForecastItem(dateTime, temperature))
            }
            return WeatherForecast(city, forecastList)
        }
    }

    private fun updateUI(weatherForecast: WeatherForecast) {
        findViewById<TextView>(R.id.address).text = weatherForecast.city
        val forecastListView = findViewById<ListView>(R.id.forecastListView)
        val adapter = ForecastAdapter(this@ForecastScreen, R.layout.forecast_item, weatherForecast.forecastList)
        forecastListView.adapter = adapter
    }

    inner class ForecastAdapter(context: Context, resource: Int, private val forecastList: List<ForecastItem>) :
        ArrayAdapter<ForecastItem>(context, resource, forecastList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.forecast_item, parent, false)
            }
            val item = forecastList[position]
            view?.findViewById<TextView>(R.id.dateTextView)?.text = formatDate(item.dateTime)

            // Update the line where temperature is set in ForecastAdapter
            view?.findViewById<TextView>(R.id.temperatureTextView)?.text = String.format("%.2f °C",item.temperature)
            return view!!
        }

        private fun formatDate(dateTime: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(dateTime * 1000)
            return sdf.format(date)
        }
    }
}
