package ai.closet.domain.wardrobe.entity

import ai.closet.domain.user.entity.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "wardrobe_items")
@EntityListeners(AuditingEntityListener::class)
class WardrobeItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: Category,

    @Column(nullable = false)
    var color: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var season: Season,

    @Column(name = "image_url")
    var imageUrl: String? = null,

    @Column(name = "purchase_date")
    val purchaseDate: LocalDate? = null,

    @Column
    var brand: String? = null,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var wearCount: Int = 0,

    @Column(name = "last_worn_date")
    var lastWornDate: LocalDate? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun updateInfo(name: String, color: String, season: Season, brand: String?, description: String?) {
        this.name = name
        this.color = color
        this.season = season
        this.brand = brand
        this.description = description
    }

    fun updateImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun wear() {
        this.wearCount++
        this.lastWornDate = LocalDate.now()
    }

    fun isOwnedBy(user: User): Boolean {
        return this.user.id == user.id
    }
}