package ai.closet.common.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.util.*

/**
 * API 요청/응답 로깅을 위한 인터셉터
 * 모든 API 호출의 흐름을 추적하고 성능 모니터링을 수행합니다.
 */
@Component
class LoggingInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    companion object {
        private const val START_TIME = "startTime"
        private const val REQUEST_ID = "requestId"
    }

    /**
     * Controller 실행 전 호출
     * - 요청 정보 로깅
     * - 요청 시작 시간 기록
     * - 요청 ID 생성 및 MDC 설정
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        // MDC에 요청 ID 저장 (로그에 자동으로 포함됨)
        MDC.put(REQUEST_ID, requestId)

        // Request 속성에 시작 시간 저장
        request.setAttribute(START_TIME, startTime)
        request.setAttribute(REQUEST_ID, requestId)

        // 요청 정보 로깅
        logger.info(
            "[API Request] {} {} | RequestID: {} | IP: {} | User-Agent: {}",
            request.method,
            request.requestURI,
            requestId,
            getClientIp(request),
            request.getHeader("User-Agent") ?: "Unknown"
        )

        // Query Parameters 로깅 (민감 정보 제외)
        val queryString = request.queryString
        if (!queryString.isNullOrBlank()) {
            logger.debug("[Query Parameters] {}", sanitizeQueryString(queryString))
        }

        return true
    }

    /**
     * Controller 실행 후, View 렌더링 전 호출
     */
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        // REST API에서는 주로 사용하지 않음
    }

    /**
     * 요청 처리 완료 후 호출 (View 렌더링 후)
     * - 응답 정보 로깅
     * - 처리 시간 계산 및 로깅
     * - MDC 정리
     */
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute(START_TIME) as? Long
        val requestId = request.getAttribute(REQUEST_ID) as? String

        if (startTime != null) {
            val executionTime = System.currentTimeMillis() - startTime

            // 응답 정보 로깅
            logger.info(
                "[API Response] {} {} | RequestID: {} | Status: {} | Duration: {}ms",
                request.method,
                request.requestURI,
                requestId,
                response.status,
                executionTime
            )

            // 성능 경고 (1초 이상 소요 시)
            if (executionTime > 1000) {
                logger.warn(
                    "[Performance Warning] Slow API detected | {} {} | Duration: {}ms | RequestID: {}",
                    request.method,
                    request.requestURI,
                    executionTime,
                    requestId
                )
            }
        }

        // 예외 발생 시 로깅
        if (ex != null) {
            logger.error(
                "[API Exception] {} {} | RequestID: {} | Exception: {}",
                request.method,
                request.requestURI,
                requestId,
                ex.message,
                ex
            )
        }

        // MDC 정리
        MDC.clear()
    }

    /**
     * 클라이언트의 실제 IP 주소 추출
     * Proxy나 Load Balancer를 거친 경우에도 실제 IP를 찾습니다.
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        )

        for (header in headers) {
            val ip = request.getHeader(header)
            if (!ip.isNullOrBlank() && ip != "unknown") {
                return ip.split(",").firstOrNull()?.trim() ?: request.remoteAddr
            }
        }

        return request.remoteAddr
    }

    /**
     * Query String에서 민감 정보 마스킹
     * password, token 등의 값을 숨깁니다.
     */
    private fun sanitizeQueryString(queryString: String): String {
        val sensitiveKeys = listOf("password", "token", "secret", "apikey", "api_key")
        var sanitized = queryString

        sensitiveKeys.forEach { key ->
            sanitized = sanitized.replace(
                Regex("($key)=([^&]*)", RegexOption.IGNORE_CASE),
                "$1=***"
            )
        }

        return sanitized
    }
}
