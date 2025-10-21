package ai.closet.security.oauth2

import ai.closet.security.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${app.frontend.url}")
    private val frontendUrl : String
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User
        val user = oAuth2User.user

        val accessToken = jwtTokenProvider.createAccessToken(user.email, user.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.email)

        // 프론트엔드 콜백 페이지로 리다이렉트
        val targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .path("/login/oauth2/success")  // 프론트엔드 콜백 경로
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}