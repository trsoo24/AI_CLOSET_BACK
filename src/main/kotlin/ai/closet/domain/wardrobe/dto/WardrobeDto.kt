package ai.closet.domain.wardrobe.dto

import ai.closet.domain.wardrobe.entity.Category
import ai.closet.domain.wardrobe.entity.Season
import ai.closet.domain.wardrobe.entity.WardrobeItem
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateWardrobeItemRequest(
    val name: String,
    val category: Category,
    val color: String,
    val season: Season,
    val brand: String? = null,
    val description: String? = null,
    val purchaseDate: LocalDate? = null
)

// 수정 요청 DTO
data class UpdateWardrobeItemRequest(
    val name: String,
    val color: String,
    val season: Season,
    val brand: String? = null,
    val description: String? = null
)

// 응답 DTO
data class WardrobeItemResponse(
    val id: Long,
    val name: String,
    val category: Category,
    val color: String,
    val season: Season,
    val imageUrl: String?,
    val purchaseDate: LocalDate?,
    val brand: String?,
    val description: String?,
    val wearCount: Int,
    val lastWornDate: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(item: WardrobeItem) = WardrobeItemResponse(
            id = item.id!!,
            name = item.name,
            category = item.category,
            color = item.color,
            season = item.season,
            imageUrl = item.imageUrl,
            purchaseDate = item.purchaseDate,
            brand = item.brand,
            description = item.description,
            wearCount = item.wearCount,
            lastWornDate = item.lastWornDate,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}

// 요약 응답 DTO (리스트용)
data class WardrobeItemSummaryResponse(
    val id: Long,
    val name: String,
    val category: Category,
    val color: String,
    val imageUrl: String?,
    val wearCount: Int
) {
    companion object {
        fun from(item: WardrobeItem) = WardrobeItemSummaryResponse(
            id = item.id!!,
            name = item.name,
            category = item.category,
            color = item.color,
            imageUrl = item.imageUrl,
            wearCount = item.wearCount
        )
    }
}

// 통계 응답 DTO
data class WardrobeStatisticsResponse(
    val totalCount: Long,
    val topCount: Long,
    val bottomCount: Long,
    val outerCount: Long,
    val dressCount: Long,
    val shoesCount: Long,
    val bagCount: Long,
    val accessoryCount: Long,
    val hatCount: Long,
    val etcCount: Long
)