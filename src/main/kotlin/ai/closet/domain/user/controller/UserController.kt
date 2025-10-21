package ai.closet.domain.user.controller

import ai.closet.common.response.ApiResponse
import ai.closet.domain.user.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/auth/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val response = userService.register(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원가입이 완료되었습니다."))
    }

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val response = userService.login(request)
        return ResponseEntity.ok(ApiResponse.success(response, "로그인이 완료되었습니다."))
    }

    @PostMapping("/auth/refresh")
    fun refreshToken(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponse<TokenResponse>> {
        val refreshToken = request["refreshToken"]
            ?: throw IllegalArgumentException("리프레시 토큰이 필요합니다.")
        val response = userService.refreshToken(refreshToken)
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 갱신되었습니다."))
    }

    @GetMapping("/users/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ApiResponse<UserResponse>> {
        val response = userService.getUserByEmail(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PutMapping("/users/me")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val response = userService.updateProfile(userDetails.username, request)
        return ResponseEntity.ok(ApiResponse.success(response, "프로필이 수정되었습니다."))
    }

    @PutMapping("/users/me/password")
    fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.changePassword(userDetails.username, request)
        return ResponseEntity.ok(ApiResponse.success(message = "비밀번호가 변경되었습니다."))
    }
}