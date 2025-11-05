package ai.closet.domain.user.service

import ai.closet.domain.user.entity.Provider
import ai.closet.domain.user.entity.Role
import ai.closet.domain.user.entity.User
import ai.closet.domain.user.repository.UserRepository
import ai.closet.security.jwt.JwtTokenProvider
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    @Transactional
    fun register(request: SignUpRequest): TokenResponse {
        logger.info("[User Registration] 회원가입 시도 | Email: {}", request.email)

        if (userRepository.existsByEmail(request.email)) {
            logger.warn("[User Registration] 회원가입 실패 - 이메일 중복 | Email: {}", request.email)
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
        logger.info("[User Registration] 회원가입 성공 | Email: {}, Name: {}", request.email, request.name)

        return generateTokens(user)
    }

    fun login(request: LoginRequest): TokenResponse {
        logger.info("[User Login] 로그인 시도 | Email: {}", request.email)

        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                logger.warn("[User Login] 로그인 실패 - 존재하지 않는 사용자 | Email: {}", request.email)
                IllegalArgumentException("존재하지 않는 사용자입니다.")
            }

        if (user.provider != Provider.LOCAL) {
            logger.warn("[User Login] 로그인 실패 - 소셜 로그인 사용자 | Email: {}, Provider: {}", request.email, user.provider)
            throw IllegalArgumentException("소셜 로그인 사용자입니다.")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            logger.warn("[User Login] 로그인 실패 - 비밀번호 불일치 | Email: {}", request.email)
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }

        logger.info("[User Login] 로그인 성공 | Email: {}, UserID: {}", request.email, user.id)
        return generateTokens(user)
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        logger.debug("[Token Refresh] 토큰 갱신 시도")

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            logger.warn("[Token Refresh] 토큰 갱신 실패 - 유효하지 않은 토큰")
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")
        }

        val email = jwtTokenProvider.getEmailFromToken(refreshToken)
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                logger.warn("[Token Refresh] 토큰 갱신 실패 - 존재하지 않는 사용자 | Email: {}", email)
                IllegalArgumentException("존재하지 않는 사용자입니다.")
            }

        logger.info("[Token Refresh] 토큰 갱신 성공 | Email: {}, UserID: {}", email, user.id)
        return generateTokens(user)
    }

    fun getUserByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        return UserResponse.from(user)
    }

    @Transactional
    fun updateProfile(email: String, request: UpdateProfileRequest): UserResponse {
        logger.info("[Profile Update] 프로필 수정 시도 | Email: {}", email)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        user.updateProfile(request.name, request.profileImage)
        logger.info("[Profile Update] 프로필 수정 성공 | Email: {}, Name: {}", email, request.name)

        return UserResponse.from(user)
    }

    @Transactional
    fun changePassword(email: String, request: ChangePasswordRequest) {
        logger.info("[Password Change] 비밀번호 변경 시도 | Email: {}", email)

        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        if (user.provider != Provider.LOCAL) {
            logger.warn("[Password Change] 비밀번호 변경 실패 - 소셜 로그인 사용자 | Email: {}, Provider: {}", email, user.provider)
            throw IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.")
        }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            logger.warn("[Password Change] 비밀번호 변경 실패 - 현재 비밀번호 불일치 | Email: {}", email)
            throw IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.")
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword))
        logger.info("[Password Change] 비밀번호 변경 성공 | Email: {}", email)
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