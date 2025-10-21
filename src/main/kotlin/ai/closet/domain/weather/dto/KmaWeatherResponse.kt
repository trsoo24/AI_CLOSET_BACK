package ai.closet.domain.weather.dto

// 기상청 API 응답 DTO
data class KmaWeatherResponse(
    val response: Response?
) {
    data class Response(
        val header: Header?,
        val body: Body?
    )

    data class Header(
        val resultCode: String?,
        val resultMsg: String?
    )

    data class Body(
        val dataType: String?,
        val items: Items?,
        val pageNo: Int?,
        val numOfRows: Int?,
        val totalCount: Int?
    )

    data class Items(
        val item: List<Item>?
    )

    data class Item(
        val baseDate: String?,      // 기상청이 null 로 주는 경우가 생겨서 nullable로 변경 - 25.10.21
        val baseTime: String?,
        val category: String?,
        val fcstDate: String?,
        val fcstTime: String?,
        val fcstValue: String?,
        val nx: Int?,
        val ny: Int?
    )
}

// 클라이언트 응답 DTO
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

// 좌표 요청 DTO
data class WeatherRequest(
    val latitude: Double,
    val longitude: Double
)