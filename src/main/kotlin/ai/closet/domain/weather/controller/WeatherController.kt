package ai.closet.domain.weather.controller


import ai.closet.common.response.ApiResponse
import ai.closet.domain.weather.dto.WeatherResponse
import ai.closet.domain.weather.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService
) {

    /**
     * 좌표 기반 날씨 조회
     */
    @GetMapping("/current")
    fun getCurrentWeather(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double
    ): ResponseEntity<ApiResponse<WeatherResponse>> {
        val response = weatherService.getWeatherByCoordinates(latitude, longitude)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 서울 날씨 조회 (테스트용)
     */
    @GetMapping("/seoul")
    fun getSeoulWeather(): ResponseEntity<ApiResponse<WeatherResponse>> {
        // 서울시청 좌표
        val response = weatherService.getWeatherByCoordinates(37.5665, 126.9780)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}