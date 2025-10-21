package ai.closet.security.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = when {
            exception.message?.contains("이메일") == true ->
                "이메일 정보가 필요합니다. 동의 항목을 확인해주세요."
            exception.message?.contains("ID") == true ->
                "사용자 정보를 가져올 수 없습니다."
            else ->
                "소셜 로그인에 실패했습니다. 다시 시도해주세요."
        }

        val encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)

        val targetUrl = UriComponentsBuilder.fromUriString("/login")
            .queryParam("error", "oauth2")
            .queryParam("message", encodedMessage)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}