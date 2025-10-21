package ai.closet.domain.user.repository

import ai.closet.domain.user.entity.Provider
import ai.closet.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findByProviderAndProviderId(provider: Provider, providerId: String): Optional<User>
}