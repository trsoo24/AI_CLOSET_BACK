package ai.closet.domain.wardrobe.entity

import java.time.LocalDate

enum class Season(
    val displayName: String,
    val description: String
) {
    SPRING("봄", "3월~5월"),
    SUMMER("여름", "6월~8월"),
    FALL("가을", "9월~11월"),
    WINTER("겨울", "12월~2월"),
    ALL_SEASON("사계절", "연중 착용 가능");

    companion object {
        fun getCurrentSeason(): Season {
            val month = LocalDate.now().monthValue
            return when (month) {
                in 3..5 -> SPRING
                in 6..8 -> SUMMER
                in 9..11 -> FALL
                else -> WINTER
            }
        }
    }
}