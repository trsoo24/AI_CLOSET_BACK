package ai.closet.domain.weather.dto

/**
 * 클라이언트 응답 DTO
 */
data class WeatherResponse(
    val location: String,
    val currentTemp: String,
    val minTemp: String,
    val maxTemp: String,
    val skyCondition: String,
    val rainProbability: String,
    val humidity: String,
    val windSpeed: String,
    val hourlyForecasts: List<HourlyForecast>,
    val clothingAdvice: String
)

data class HourlyForecast(
    val time: String,
    val temperature: String,
    val skyCondition: String,
    val rainProbability: String
)

/**
 * 좌표 요청 DTO
 */
data class WeatherRequest(
    val latitude: Double,
    val longitude: Double
)
