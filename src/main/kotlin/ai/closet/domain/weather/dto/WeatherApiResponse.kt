package ai.closet.domain.weather.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * WeatherAPI.com API 응답 DTO
 */
data class WeatherApiResponse(
    val location: Location?,
    val current: Current?,
    val forecast: Forecast?
) {
    data class Location(
        val name: String?,
        val region: String?,
        val country: String?,
        val lat: Double?,
        val lon: Double?,
        @JsonProperty("localtime")
        val localTime: String?
    )

    data class Current(
        @JsonProperty("temp_c")
        val tempC: Double?,
        val condition: Condition?,
        val humidity: Int?,
        @JsonProperty("wind_kph")
        val windKph: Double?,
        @JsonProperty("feelslike_c")
        val feelsLikeC: Double?
    )

    data class Condition(
        val text: String?,
        val icon: String?,
        val code: Int?
    )

    data class Forecast(
        @JsonProperty("forecastday")
        val forecastDay: List<ForecastDay>?
    )

    data class ForecastDay(
        val date: String?,
        val day: Day?,
        val hour: List<Hour>?
    )

    data class Day(
        @JsonProperty("maxtemp_c")
        val maxTempC: Double?,
        @JsonProperty("mintemp_c")
        val minTempC: Double?,
        @JsonProperty("avgtemp_c")
        val avgTempC: Double?,
        @JsonProperty("daily_chance_of_rain")
        val dailyChanceOfRain: Int?,
        val condition: Condition?
    )

    data class Hour(
        val time: String?,
        @JsonProperty("temp_c")
        val tempC: Double?,
        val condition: Condition?,
        @JsonProperty("chance_of_rain")
        val chanceOfRain: Int?,
        val humidity: Int?,
        @JsonProperty("wind_kph")
        val windKph: Double?
    )
}
