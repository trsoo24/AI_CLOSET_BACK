package ai.closet.domain.weather.service

import ai.closet.domain.weather.dto.HourlyForecast
import ai.closet.domain.weather.dto.WeatherApiResponse
import ai.closet.domain.weather.dto.WeatherResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherService(
    @Value("\${weather.api.api-key}") private val apiKey: String,
    @Value("\${weather.api.base-url}") private val weatherUrl: String
) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(weatherUrl)
        .build()

    /**
     * 위도/경도로 날씨 조회
     */
    fun getWeatherByCoordinates(latitude: Double, longitude: Double): WeatherResponse {
        // WeatherAPI.com API 호출
        val weatherData = fetchWeatherFromApi(latitude, longitude)

        // 응답 데이터 파싱 및 가공
        return parseWeatherResponse(weatherData)
    }

    /**
     * WeatherAPI.com API 호출
     * Forecast API를 사용하여 오늘의 시간별 예보 가져오기
     */
    private fun fetchWeatherFromApi(latitude: Double, longitude: Double): WeatherApiResponse {
        val response = webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/forecast.json")
                    .queryParam("key", apiKey)
                    .queryParam("q", "$latitude,$longitude")
                    .queryParam("days", 1)
                    .queryParam("aqi", "no")
                    .queryParam("alerts", "no")
                    .build()
            }
            .retrieve()
            .bodyToMono(WeatherApiResponse::class.java)
            .block() ?: throw RuntimeException("날씨 API 호출 실패")

        return response
    }

    /**
     * WeatherAPI.com 응답 데이터 파싱
     */
    private fun parseWeatherResponse(weatherData: WeatherApiResponse): WeatherResponse {
        val location = weatherData.location ?: throw RuntimeException("위치 정보가 없습니다.")
        val current = weatherData.current ?: throw RuntimeException("현재 날씨 정보가 없습니다.")
        val forecast = weatherData.forecast?.forecastDay?.firstOrNull()
            ?: throw RuntimeException("예보 정보가 없습니다.")

        // 위치 정보
        val locationName = buildLocationName(location)

        // 현재 날씨 정보
        val currentTemp = current.tempC ?: 0.0
        val minTemp = forecast.day?.minTempC ?: 0.0
        val maxTemp = forecast.day?.maxTempC ?: 0.0

        // 시간별 예보 - 오늘 00시부터 24시까지 (실제로는 23시까지)
        val hourlyForecasts = forecast.hour?.map { hour ->
            val time = hour.time?.substring(11, 16) ?: "00:00" // "2025-10-21 14:00" -> "14:00"
            HourlyForecast(
                time = time,
                temperature = "${hour.tempC ?: 0.0}°C",
                skyCondition = hour.condition?.text ?: "알 수 없음",
                rainProbability = "${hour.chanceOfRain ?: 0}%"
            )
        } ?: emptyList()

        return WeatherResponse(
            location = locationName,
            currentTemp = "${currentTemp}°C",
            minTemp = "${minTemp}°C",
            maxTemp = "${maxTemp}°C",
            skyCondition = current.condition?.text ?: "알 수 없음",
            rainProbability = "${forecast.day?.dailyChanceOfRain ?: 0}%",
            humidity = "${current.humidity ?: 0}%",
            windSpeed = "${current.windKph?.div(3.6)?.let { "%.1f".format(it) } ?: "0.0"}m/s", // km/h를 m/s로 변환
            hourlyForecasts = hourlyForecasts,
            clothingAdvice = getClothingAdvice(currentTemp, maxTemp, minTemp)
        )
    }

    /**
     * 위치명 구성
     */
    private fun buildLocationName(location: WeatherApiResponse.Location): String {
        return buildString {
            location.name?.let { append(it) }
            if (location.region != null && location.region != location.name) {
                if (isNotEmpty()) append(", ")
                append(location.region)
            }
        }.ifEmpty { "알 수 없는 위치" }
    }

    /**
     * 옷차림 조언 생성
     */
    private fun getClothingAdvice(current: Double, max: Double, min: Double): String {
        val avgTemp = (max + min) / 2

        return when {
            avgTemp >= 28 -> "민소매, 반팔, 반바지, 원피스 추천"
            avgTemp >= 23 -> "반팔, 얇은 셔츠, 반바지, 면바지 추천"
            avgTemp >= 20 -> "얇은 가디건, 긴팔티, 면바지, 청바지 추천"
            avgTemp >= 17 -> "얇은 니트, 맨투맨, 가디건, 청바지 추천"
            avgTemp >= 12 -> "자켓, 가디건, 야상, 스타킹, 청바지, 면바지 추천"
            avgTemp >= 9 -> "자켓, 트렌치코트, 야상, 니트, 청바지, 스타킹 추천"
            avgTemp >= 5 -> "코트, 가죽자켓, 히트텍, 니트, 레깅스 추천"
            else -> "패딩, 두꺼운 코트, 목도리, 기모제품 필수"
        }.let { advice ->
            val tempDiff = max - min
            if (tempDiff > 10) {
                "$advice (일교차가 크니 겉옷을 챙기세요!)"
            } else {
                advice
            }
        }
    }
}
