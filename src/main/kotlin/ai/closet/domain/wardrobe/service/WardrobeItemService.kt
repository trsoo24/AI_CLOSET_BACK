package ai.closet.domain.wardrobe.service

import ai.closet.domain.user.repository.UserRepository
import ai.closet.domain.wardrobe.dto.*
import ai.closet.domain.wardrobe.entity.Category
import ai.closet.domain.wardrobe.entity.Season
import ai.closet.domain.wardrobe.entity.WardrobeItem
import ai.closet.domain.wardrobe.repository.WardrobeItemRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WardrobeItemService(
    private val wardrobeItemRepository: WardrobeItemRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(WardrobeItemService::class.java)

    /**
     * 옷 등록
     */
    @Transactional
    fun createItem(email: String, request: CreateWardrobeItemRequest): WardrobeItemResponse {
        logger.info("[Wardrobe Item] 옷 등록 시도 | Email: {}, Name: {}, Category: {}", email, request.name, request.category)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = WardrobeItem(
            user = user,
            name = request.name,
            category = request.category,
            color = request.color,
            season = request.season,
            brand = request.brand,
            description = request.description,
            purchaseDate = request.purchaseDate
        )

        val savedItem = wardrobeItemRepository.save(item)
        logger.info("[Wardrobe Item] 옷 등록 성공 | Email: {}, ItemID: {}, Category: {}", email, savedItem.id, savedItem.category)
        return WardrobeItemResponse.from(savedItem)
    }

    /**
     * 내 옷 목록 조회
     */
    fun getMyItems(email: String, pageable: Pageable): Page<WardrobeItemResponse> {
        logger.debug("[Wardrobe Item] 옷 목록 조회 | Email: {}, Page: {}, Size: {}", email, pageable.pageNumber, pageable.pageSize)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val items = wardrobeItemRepository.findByUser(user, pageable)
        logger.debug("[Wardrobe Item] 옷 목록 조회 완료 | Email: {}, TotalElements: {}", email, items.totalElements)

        return items.map { WardrobeItemResponse.from(it) }
    }

    /**
     * 카테고리별 조회
     */
    fun getItemsByCategory(
        email: String,
        category: Category,
        pageable: Pageable
    ): Page<WardrobeItemResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findByUserAndCategory(user, category, pageable)
            .map { WardrobeItemResponse.from(it) }
    }

    /**
     * 시즌별 조회
     */
    fun getItemsBySeason(
        email: String,
        season: Season,
        pageable: Pageable
    ): Page<WardrobeItemResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findByUserAndSeason(user, season, pageable)
            .map { WardrobeItemResponse.from(it) }
    }

    /**
     * 카테고리 + 시즌 필터링
     */
    fun getItemsByCategoryAndSeason(
        email: String,
        category: Category,
        season: Season,
        pageable: Pageable
    ): Page<WardrobeItemResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findByUserAndCategoryAndSeason(user, category, season, pageable)
            .map { WardrobeItemResponse.from(it) }
    }

    /**
     * 색상별 조회
     */
    fun getItemsByColor(
        email: String,
        color: String,
        pageable: Pageable
    ): Page<WardrobeItemResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findByUserAndColor(user, color, pageable)
            .map { WardrobeItemResponse.from(it) }
    }

    /**
     * 검색
     */
    fun searchItems(
        email: String,
        keyword: String,
        pageable: Pageable
    ): Page<WardrobeItemResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.searchByKeyword(user, keyword, pageable)
            .map { WardrobeItemResponse.from(it) }
    }

    /**
     * 옷 상세 조회
     */
    fun getItem(email: String, itemId: Long): WardrobeItemResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = wardrobeItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow { IllegalArgumentException("해당 아이템을 찾을 수 없습니다.") }

        return WardrobeItemResponse.from(item)
    }

    /**
     * 옷 정보 수정
     */
    @Transactional
    fun updateItem(
        email: String,
        itemId: Long,
        request: UpdateWardrobeItemRequest
    ): WardrobeItemResponse {
        logger.info("[Wardrobe Item] 옷 정보 수정 시도 | Email: {}, ItemID: {}", email, itemId)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = wardrobeItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow { IllegalArgumentException("해당 아이템을 찾을 수 없습니다.") }

        item.updateInfo(
            name = request.name,
            color = request.color,
            season = request.season,
            brand = request.brand,
            description = request.description
        )

        logger.info("[Wardrobe Item] 옷 정보 수정 성공 | Email: {}, ItemID: {}, Name: {}", email, itemId, request.name)
        return WardrobeItemResponse.from(item)
    }

    /**
     * 이미지 URL 업데이트
     */
    @Transactional
    fun updateItemImage(email: String, itemId: Long, imageUrl: String): WardrobeItemResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = wardrobeItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow { IllegalArgumentException("해당 아이템을 찾을 수 없습니다.") }

        item.updateImageUrl(imageUrl)

        return WardrobeItemResponse.from(item)
    }

    /**
     * 옷 착용 기록
     */
    @Transactional
    fun wearItem(email: String, itemId: Long): WardrobeItemResponse {
        logger.info("[Wardrobe Item] 옷 착용 기록 | Email: {}, ItemID: {}", email, itemId)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = wardrobeItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow { IllegalArgumentException("해당 아이템을 찾을 수 없습니다.") }

        val previousCount = item.wearCount
        item.wear()

        logger.info("[Wardrobe Item] 옷 착용 기록 완료 | Email: {}, ItemID: {}, WearCount: {} -> {}",
            email, itemId, previousCount, item.wearCount)
        return WardrobeItemResponse.from(item)
    }

    /**
     * 옷 삭제
     */
    @Transactional
    fun deleteItem(email: String, itemId: Long) {
        logger.info("[Wardrobe Item] 옷 삭제 시도 | Email: {}, ItemID: {}", email, itemId)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val item = wardrobeItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow { IllegalArgumentException("해당 아이템을 찾을 수 없습니다.") }

        wardrobeItemRepository.delete(item)
        logger.info("[Wardrobe Item] 옷 삭제 성공 | Email: {}, ItemID: {}, Name: {}", email, itemId, item.name)
    }

    /**
     * 최근 추가된 아이템
     */
    fun getRecentItems(email: String): List<WardrobeItemSummaryResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findTop10ByUserOrderByCreatedAtDesc(user)
            .map { WardrobeItemSummaryResponse.from(it) }
    }

    /**
     * 많이 입은 아이템
     */
    fun getFrequentlyWornItems(email: String): List<WardrobeItemSummaryResponse> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        return wardrobeItemRepository.findTop10ByUserOrderByWearCountDesc(user)
            .map { WardrobeItemSummaryResponse.from(it) }
    }

    /**
     * 통계 정보
     */
    fun getStatistics(email: String): WardrobeStatisticsResponse {
        logger.debug("[Wardrobe Item] 통계 정보 조회 | Email: {}", email)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        // 단일 쿼리로 카테고리별 count 조회
        val categoryCountMap = wardrobeItemRepository.countByUserGroupByCategory(user)
            .associate { it.getCategory() to it.getCount() }

        val response = WardrobeStatisticsResponse(
            totalCount = categoryCountMap.values.sum(),
            topCount = categoryCountMap[Category.TOP] ?: 0L,
            bottomCount = categoryCountMap[Category.BOTTOM] ?: 0L,
            outerCount = categoryCountMap[Category.OUTER] ?: 0L,
            dressCount = categoryCountMap[Category.DRESS] ?: 0L,
            shoesCount = categoryCountMap[Category.SHOES] ?: 0L,
            bagCount = categoryCountMap[Category.BAG] ?: 0L,
            accessoryCount = categoryCountMap[Category.ACCESSORY] ?: 0L,
            hatCount = categoryCountMap[Category.HAT] ?: 0L,
            etcCount = categoryCountMap[Category.ETC] ?: 0L
        )

        logger.info("[Wardrobe Item] 통계 정보 조회 완료 | Email: {}, TotalCount: {}", email, response.totalCount)
        return response
    }
}