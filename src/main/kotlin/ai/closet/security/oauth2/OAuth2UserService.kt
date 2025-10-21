package ai.closet.security.oauth2

import ai.closet.domain.user.entity.Provider
import ai.closet.domain.user.entity.Role
import ai.closet.domain.user.entity.User
import ai.closet.domain.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class OAuth2UserService(
    private val userRepository: UserRepository
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId

        val userInfo = when (registrationId) {
            "naver" -> NaverUserInfo(oAuth2User.attributes)
            "kakao" -> KakaoUserInfo(oAuth2User.attributes)
            else -> throw IllegalArgumentException("Unsupported provider: $registrationId")
        }

        val user = saveOrUpdate(userInfo)

        return CustomOAuth2User(user, oAuth2User.attributes)
    }

    private fun saveOrUpdate(userInfo: OAuth2UserInfo): User {
        return userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
            .map { user ->
                user.updateProfile(userInfo.getName(), userInfo.getProfileImage())
                userRepository.save(user)
            }
            .orElseGet {
                val newUser = User(
                    email = userInfo.getEmail(),
                    name = userInfo.getName(),
                    profileImage = userInfo.getProfileImage(),
                    provider = userInfo.getProvider(),
                    providerId = userInfo.getProviderId(),
                    role = Role.USER
                )
                userRepository.save(newUser)
            }
    }
}

interface OAuth2UserInfo {
    fun getProvider(): Provider
    fun getProviderId(): String
    fun getEmail(): String
    fun getName(): String
    fun getProfileImage(): String?
}

// 네이버 사용자 정보 (안전한 null 처리)
class NaverUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {

    private val response: Map<String, Any>? = attributes["response"] as? Map<String, Any>

    override fun getProvider() = Provider.NAVER

    override fun getProviderId(): String {
        return response?.get("id")?.toString()
            ?: throw IllegalArgumentException("네이버 ID를 가져올 수 없습니다.")
    }

    override fun getEmail(): String {
        // 실제 이메일이 있으면 사용, 없으면 가짜 이메일 생성
        val realEmail = response?.get("email")?.toString()

        return if (!realEmail.isNullOrBlank()) {
            realEmail
        } else {
            // 네이버 ID 기반 가짜 이메일 생성
            val providerId = getProviderId()
            "naver_${providerId}@naver.com"
        }
    }
    override fun getName(): String {
        return response?.get("name")?.toString()
            ?: response?.get("nickname")?.toString()
            ?: "네이버 사용자"
    }

    override fun getProfileImage(): String? {
        return response?.get("profile_image")?.toString()
    }
}

// 카카오 사용자 정보 (안전한 null 처리)
class KakaoUserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {

    private val kakaoAccount: Map<String, Any>? = attributes["kakao_account"] as? Map<String, Any>
    private val profile: Map<String, Any>? = kakaoAccount?.get("profile") as? Map<String, Any>

    override fun getProvider() = Provider.KAKAO

    override fun getProviderId(): String {
        return attributes["id"]?.toString()
            ?: throw IllegalArgumentException("카카오 ID를 가져올 수 없습니다.")
    }

    override fun getEmail(): String {
        // 실제 이메일이 있으면 사용, 없으면 가짜 이메일 생성
        val realEmail = kakaoAccount?.get("email")?.toString()

        return if (!realEmail.isNullOrBlank()) {
            realEmail
        } else {
            // 카카오 ID 기반 가짜 이메일 생성
            val providerId = getProviderId()
            "kakao_${providerId}@kakao.com"
        }
    }

    override fun getName(): String {
        return profile?.get("nickname")?.toString()
            ?: kakaoAccount?.get("name")?.toString()
            ?: "카카오 사용자"
    }

    override fun getProfileImage(): String? {
        return profile?.get("profile_image_url")?.toString()
            ?: profile?.get("thumbnail_image_url")?.toString()
    }
}

class CustomOAuth2User(
    val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {
    override fun getName() = user.email
    override fun getAttributes() = attributes
    override fun getAuthorities() = listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_${user.role}"))
}