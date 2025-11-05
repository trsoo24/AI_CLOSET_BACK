package ai.closet.domain.weather.service

import ai.closet.domain.weather.dto.HourlyForecast
import ai.closet.domain.weather.dto.KmaWeatherResponse
import ai.closet.domain.weather.dto.WeatherResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

@Service
class WeatherService(
    @Value("\${weather.kma.api-key}") private val apiKey: String,
    @Value("\${weather.kma.base-url}") private val weatherUrl: String,
    @Value("\${weather.kma.api-uri}") private val weatherUri: String
) {
    private val logger = LoggerFactory.getLogger(WeatherService::class.java)

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(weatherUrl)
        .build()
    /**
     * 위도/경도로 날씨 조회
     */
    fun getWeatherByCoordinates(latitude: Double, longitude: Double): WeatherResponse {
        logger.info("[Weather] 날씨 조회 시작 | Latitude: {}, Longitude: {}", latitude, longitude)

        // 1. 위도/경도를 기상청 격자 좌표로 변환
        val (nx, ny) = convertToGrid(latitude, longitude)
        logger.debug("[Weather] 격자 좌표 변환 완료 | Lat: {}, Lon: {} -> Grid X: {}, Y: {}", latitude, longitude, nx, ny)

        // 2. 기상청 API 호출
        val kmaResponse = fetchWeatherFromKma(nx, ny)

        // 3. 응답 데이터 파싱 및 가공
        val response = parseWeatherResponse(kmaResponse, "서울특별시") // TODO: 역지오코딩으로 실제 지역명 얻기
        logger.info("[Weather] 날씨 조회 완료 | Latitude: {}, Longitude: {}, CurrentTemp: {}",
            latitude, longitude, response.currentTemp)

        return response
    }

    /**
     * 기상청 API 호출
     */
    private fun fetchWeatherFromKma(nx: Int, ny: Int): KmaWeatherResponse {
        val now = LocalDateTime.now()
        val baseDateTime = getBaseDateTime(now)

        logger.debug("[Weather API] 기상청 API 호출 | Grid X: {}, Y: {}, BaseDate: {}, BaseTime: {}",
            nx, ny, baseDateTime.first, baseDateTime.second)

        val startTime = System.currentTimeMillis()

        try {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(weatherUri)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 1000)
                        .queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDateTime.first)
                        .queryParam("base_time", baseDateTime.second)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .queryParam("authKey", apiKey)
                        .build()
                }
                .retrieve()
                .bodyToMono(KmaWeatherResponse::class.java)
                .block() ?: throw RuntimeException("기상청 API 호출 실패")

            val duration = System.currentTimeMillis() - startTime
            logger.info("[Weather API] 기상청 API 호출 성공 | Duration: {}ms", duration)

            return response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("[Weather API] 기상청 API 호출 실패 | Grid X: {}, Y: {}, Duration: {}ms, Error: {}",
                nx, ny, duration, e.message, e)
            throw RuntimeException("기상청 API 호출 실패: ${e.message}", e)
        }
    }

    /**
     * 기상청 응답 데이터 파싱
     */
    private fun parseWeatherResponse(kmaResponse: KmaWeatherResponse, location: String): WeatherResponse {
        logger.debug("[Weather] 날씨 데이터 파싱 시작 | Location: {}", location)

        // null 체크
        val items = kmaResponse.response?.body?.items?.item
            ?: throw RuntimeException("기상청 API 응답 데이터가 올바르지 않습니다.")

        if (items.isEmpty()) {
            logger.warn("[Weather] 날씨 데이터 없음 | Location: {}", location)
            throw RuntimeException("날씨 데이터가 없습니다. 좌표를 확인해주세요.")
        }

        logger.debug("[Weather] 날씨 데이터 파싱 | Items Count: {}", items.size)

        // 현재 시간대 데이터 추출
        val now = LocalDateTime.now()
        val currentHour = now.hour

        val currentData = items
            .filter { it.fcstTime == String.format("%02d00", currentHour) }
            .mapNotNull { item ->
                item.category?.let { category ->
                    category to (item.fcstValue ?: "")
                }
            }
            .toMap()

        // 시간대별 예보 생성 (다음 6시간)
        val hourlyForecasts = (0..5).map { hour ->
            val targetHour = (currentHour + hour) % 24
            val hourData = items
                .filter { it.fcstTime == String.format("%02d00", targetHour) }
                .mapNotNull { item ->
                    item.category?.let { category ->
                        category to (item.fcstValue ?: "")
                    }
                }
                .toMap()

            HourlyForecast(
                time = String.format("%02d:00", targetHour),
                temperature = "${hourData["TMP"] ?: "-"}°C",
                skyCondition = parseSkyCondition(hourData["SKY"] ?: "1"),
                rainProbability = "${hourData["POP"] ?: "0"}%"
            )
        }

        // 최저/최고 기온 찾기
        val temperatures = items
            .filter { it.category == "TMP" }
            .mapNotNull { it.fcstValue?.toDoubleOrNull() }

        val minTemp = temperatures.minOrNull() ?: 0.0
        val maxTemp = temperatures.maxOrNull() ?: 0.0
        val currentTemp = currentData["TMP"]?.toDoubleOrNull() ?: 0.0

        return WeatherResponse(
            location = location,
            currentTemp = "${currentTemp}°C",
            minTemp = "${minTemp}°C",
            maxTemp = "${maxTemp}°C",
            skyCondition = parseSkyCondition(currentData["SKY"] ?: "1"),
            rainProbability = "${currentData["POP"] ?: "0"}%",
            humidity = "${currentData["REH"] ?: "0"}%",
            windSpeed = "${currentData["WSD"] ?: "0"}m/s",
            hourlyForecasts = hourlyForecasts,
            clothingAdvice = getClothingAdvice(currentTemp, maxTemp, minTemp)
        )
    }

    /**
     * 하늘 상태 파싱
     */
    private fun parseSkyCondition(code: String): String {
        return when (code) {
            "1" -> "맑음"
            "3" -> "구름많음"
            "4" -> "흐림"
            else -> "알 수 없음"
        }
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

    /**
     * 발표 시각 계산 (기상청 API 특성상 필요)
     */
    private fun getBaseDateTime(now: LocalDateTime): Pair<String, String> {
        val baseHours = listOf("0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300")
        val currentHour = now.hour
        val currentMinute = now.minute

        // 현재 시각 기준으로 가장 최근 발표 시각 찾기
        val baseTime = baseHours.findLast {
            val hour = it.substring(0, 2).toInt()
            currentHour > hour || (currentHour == hour && currentMinute >= 10)
        } ?: "2300"

        // 어제 23시 발표인 경우
        val baseDate = if (baseTime == "2300" && currentHour < 2) {
            now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        } else {
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        }

        return Pair(baseDate, baseTime)
    }

    /**
     * 위도/경도를 기상청 격자 좌표로 변환
     * (기상청 격자 변환 공식 적용)
     */
    private fun convertToGrid(latitude: Double, longitude: Double): Pair<Int, Int> {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0 // 격자 간격(km)
        val SLAT1 = 30.0 // 투영 위도1(degree)
        val SLAT2 = 60.0 // 투영 위도2(degree)
        val OLON = 126.0 // 기준점 경도(degree)
        val OLAT = 38.0 // 기준점 위도(degree)
        val XO = 43.0 // 기준점 X좌표(GRID)
        val YO = 136.0 // 기준점 Y좌표(GRID)

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = longitude * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = floor(ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = floor(ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Pair(x, y)
    }
}