package ai.closet.config

import ai.closet.common.interceptor.LoggingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 설정
 * - 인터셉터 등록
 */
@Configuration
class WebMvcConfig(
    private val loggingInterceptor: LoggingInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/api/**")  // 모든 API 경로에 적용
            .excludePathPatterns(
                "/api/health",           // Health check 제외
                "/api/actuator/**"       // Actuator 제외
            )
    }
}
