package ai.closet.domain.wardrobe.controller

import ai.closet.common.response.ApiResponse
import ai.closet.domain.wardrobe.dto.*
import ai.closet.domain.wardrobe.entity.Category
import ai.closet.domain.wardrobe.entity.Season
import ai.closet.domain.wardrobe.service.WardrobeItemService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/wardrobe")
class WardrobeItemController(
    private val wardrobeItemService: WardrobeItemService
) {

    /**
     * 옷 등록
     */
    @PostMapping("/items")
    fun createItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateWardrobeItemRequest
    ): ResponseEntity<ApiResponse<WardrobeItemResponse>> {
        val response = wardrobeItemService.createItem(userDetails.username, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "옷이 등록되었습니다."))
    }

    /**
     * 내 옷 목록 조회
     */
    @GetMapping("/items")
    fun getMyItems(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WardrobeItemResponse>>> {
        val response = wardrobeItemService.getMyItems(userDetails.username, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 카테고리별 조회
     */
    @GetMapping("/items/category/{category}")
    fun getItemsByCategory(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable category: Category,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WardrobeItemResponse>>> {
        val response = wardrobeItemService.getItemsByCategory(userDetails.username, category, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 시즌별 조회
     */
    @GetMapping("/items/season/{season}")
    fun getItemsBySeason(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable season: Season,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WardrobeItemResponse>>> {
        val response = wardrobeItemService.getItemsBySeason(userDetails.username, season, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 복합 필터링 (카테고리 + 시즌 + 색상)
     */
    @GetMapping("/items/filter")
    fun getItemsWithFilter(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) category: Category?,
        @RequestParam(required = false) season: Season?,
        @RequestParam(required = false) color: String?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WardrobeItemResponse>>> {
        val response = when {
            category != null && season != null ->
                wardrobeItemService.getItemsByCategoryAndSeason(userDetails.username, category, season, pageable)
            category != null ->
                wardrobeItemService.getItemsByCategory(userDetails.username, category, pageable)
            season != null ->
                wardrobeItemService.getItemsBySeason(userDetails.username, season, pageable)
            color != null ->
                wardrobeItemService.getItemsByColor(userDetails.username, color, pageable)
            else ->
                wardrobeItemService.getMyItems(userDetails.username, pageable)
        }

        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 검색
     */
    @GetMapping("/items/search")
    fun searchItems(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam keyword: String,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WardrobeItemResponse>>> {
        val response = wardrobeItemService.searchItems(userDetails.username, keyword, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 옷 상세 조회
     */
    @GetMapping("/items/{itemId}")
    fun getItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long
    ): ResponseEntity<ApiResponse<WardrobeItemResponse>> {
        val response = wardrobeItemService.getItem(userDetails.username, itemId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 옷 정보 수정
     */
    @PutMapping("/items/{itemId}")
    fun updateItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long,
        @RequestBody request: UpdateWardrobeItemRequest
    ): ResponseEntity<ApiResponse<WardrobeItemResponse>> {
        val response = wardrobeItemService.updateItem(userDetails.username, itemId, request)
        return ResponseEntity.ok(ApiResponse.success(response, "옷 정보가 수정되었습니다."))
    }

    /**
     * 이미지 URL 업데이트
     */
    @PatchMapping("/items/{itemId}/image")
    fun updateItemImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long,
        @RequestBody imageUrl: String
    ): ResponseEntity<ApiResponse<WardrobeItemResponse>> {
        val response = wardrobeItemService.updateItemImage(userDetails.username, itemId, imageUrl)
        return ResponseEntity.ok(ApiResponse.success(response, "이미지가 업데이트되었습니다."))
    }

    /**
     * 착용 기록
     */
    @PostMapping("/items/{itemId}/wear")
    fun wearItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long
    ): ResponseEntity<ApiResponse<WardrobeItemResponse>> {
        val response = wardrobeItemService.wearItem(userDetails.username, itemId)
        return ResponseEntity.ok(ApiResponse.success(response, "착용 기록이 저장되었습니다."))
    }

    /**
     * 옷 삭제
     */
    @DeleteMapping("/items/{itemId}")
    fun deleteItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        wardrobeItemService.deleteItem(userDetails.username, itemId)
        return ResponseEntity.ok(ApiResponse.success(message = "옷이 삭제되었습니다."))
    }

    /**
     * 최근 추가된 아이템 조회
     */
    @GetMapping("/items/recent")
    fun getRecentItems(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<List<WardrobeItemSummaryResponse>>> {
        val response = wardrobeItemService.getRecentItems(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 많이 입은 아이템 조회
     */
    @GetMapping("/items/frequently-worn")
    fun getFrequentlyWornItems(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<List<WardrobeItemSummaryResponse>>> {
        val response = wardrobeItemService.getFrequentlyWornItems(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 옷장 통계
     */
    @GetMapping("/statistics")
    fun getStatistics(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<WardrobeStatisticsResponse>> {
        val response = wardrobeItemService.getStatistics(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}