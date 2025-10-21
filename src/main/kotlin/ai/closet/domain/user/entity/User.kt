package ai.closet.domain.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true, nullable = false)
    var email: String,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    var name: String,

    @Column(name = "profile_image")
    var profileImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: Provider = Provider.LOCAL,

    @Column(name = "provider_id")
    var providerId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()) {
    fun updateProfile(name: String, profileImage: String?) {
        this.name = name
        this.profileImage = profileImage
    }
    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }
}

enum class Provider {
    LOCAL, NAVER, KAKAO
}

enum class Role {
    USER, ADMIN
}
