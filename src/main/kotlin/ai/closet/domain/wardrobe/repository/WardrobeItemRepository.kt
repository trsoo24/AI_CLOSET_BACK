package ai.closet.domain.wardrobe.repository

import ai.closet.domain.user.entity.User
import ai.closet.domain.wardrobe.entity.Category
import ai.closet.domain.wardrobe.entity.Season
import ai.closet.domain.wardrobe.entity.WardrobeItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

@Repository
interface WardrobeItemRepository : JpaRepository<WardrobeItem, Long> {

    // 사용자별 전체 아이템 조회
    fun findByUser(user: User, pageable: Pageable): Page<WardrobeItem>

    // 사용자별 카테고리 필터링
    fun findByUserAndCategory(user: User, category: Category, pageable: Pageable): Page<WardrobeItem>

    // 사용자별 시즌 필터링
    fun findByUserAndSeason(user: User, season: Season, pageable: Pageable): Page<WardrobeItem>

    // 사용자별 카테고리 + 시즌 필터링
    fun findByUserAndCategoryAndSeason(
        user: User,
        category: Category,
        season: Season,
        pageable: Pageable
    ): Page<WardrobeItem>

    // 사용자별 색상 필터링
    fun findByUserAndColor(user: User, color: String, pageable: Pageable): Page<WardrobeItem>

    // 사용자의 특정 아이템 조회
    fun findByIdAndUser(id: Long, user: User): Optional<WardrobeItem>

    // 사용자별 아이템 개수
    fun countByUser(user: User): Long


    /**
     * 사용자 카테고리별 옷 개수 조회
     * 단일 쿼리로 카테고리별 통계 조회 (성능 최적화)
     */
    @Query("""
        SELECT w.category as category, COUNT(w) as count
        FROM WardrobeItem w
        WHERE w.user = :user
        GROUP BY w.category
    """)
    fun countByUserGroupByCategory(@Param("user") user: User): List<CategoryCountProjection>

    // 검색 (이름, 브랜드, 설명)
    @Query("""
        SELECT w FROM WardrobeItem w 
        WHERE w.user = :user 
        AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(w.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    fun searchByKeyword(
        @Param("user") user: User,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<WardrobeItem>

    // 최근 추가된 아이템
    fun findTop10ByUserOrderByCreatedAtDesc(user: User): List<WardrobeItem>

    // 많이 입은 아이템
    fun findTop10ByUserOrderByWearCountDesc(user: User): List<WardrobeItem>

    // Projection 인터페이스
    interface CategoryCountProjection {
        fun getCategory(): Category
        fun getCount(): Long
    }
}