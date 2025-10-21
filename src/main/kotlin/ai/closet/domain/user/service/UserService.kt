package ai.closet.domain.user.service

import ai.closet.domain.user.entity.Provider
import ai.closet.domain.user.entity.Role
import ai.closet.domain.user.entity.User
import ai.closet.domain.user.repository.UserRepository
import ai.closet.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun register(request: SignUpRequest): TokenResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            provider = Provider.LOCAL,
            role = Role.USER
        )

        userRepository.save(user)

        return generateTokens(user)
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        if (user.provider != Provider.LOCAL) {
            throw IllegalArgumentException("소셜 로그인 사용자입니다.")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }

        return generateTokens(user)
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")
        }

        val email = jwtTokenProvider.getEmailFromToken(refreshToken)
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        return generateTokens(user)
    }

    fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        return UserResponse.from(user)
    }

    @Transactional
    fun updateProfile(email: String, request: UpdateProfileRequest): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        user.updateProfile(request.name, request.profileImage)

        return UserResponse.from(user)
    }

    @Transactional
    fun changePassword(email: String, request: ChangePasswordRequest) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        if (user.provider != Provider.LOCAL) {
            throw IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.")
        }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.")
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword))
    }

    private fun generateTokens(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.createAccessToken(user.email, user.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.email)

        return TokenResponse(accessToken, refreshToken)
    }
}

// DTOs
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val name: String,
    val profileImage: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val profileImage: String?,
    val provider: Provider,
    val role: Role
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            profileImage = user.profileImage,
            provider = user.provider,
            role = user.role
        )
    }
}