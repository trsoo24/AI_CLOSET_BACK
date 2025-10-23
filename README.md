# AI Closet Backend

AI 기반 옷장 관리 애플리케이션의 백엔드 서버입니다.

## 환경 변수 설정

프로젝트를 실행하기 전에 다음 환경 변수를 설정해야 합니다:

### 데이터베이스
- `DB_USERNAME`: MySQL 사용자명
- `DB_PASSWORD`: MySQL 비밀번호

### OAuth 인증
- `NAVER_CLIENT_ID`: 네이버 OAuth 클라이언트 ID
- `NAVER_CLIENT_SECRET`: 네이버 OAuth 클라이언트 시크릿
- `KAKAO_CLIENT_ID`: 카카오 OAuth 클라이언트 ID
- `KAKAO_CLIENT_SECRET`: 카카오 OAuth 클라이언트 시크릿

### JWT
- `JWT_SECRET`: JWT 토큰 생성용 시크릿 키 (기본값 사용 가능)

### 날씨 API
- `WEATHER_API_KEY`: WeatherAPI.com API 키
  - [WeatherAPI.com](https://www.weatherapi.com/)에서 무료 API 키 발급 가능
  - 무료 플랜: 하루 100만 건 호출 가능

## 날씨 API 변경사항 (2025-10-21)

기존 기상청(KMA) API에서 WeatherAPI.com으로 변경되었습니다.

### 주요 변경사항
- **위치 기반 날씨 조회**: 사용자의 위도/경도를 기반으로 날씨 정보 제공
- **24시간 시간별 예보**: 오늘 00시부터 23시까지 1시간 간격으로 날씨 정보 제공
- **더 정확한 날씨 정보**: 글로벌 날씨 API로 변경하여 정확도 향상

### API 엔드포인트
- `GET /api/weather/current?latitude={lat}&longitude={lon}` - 좌표 기반 날씨 조회
- `GET /api/weather/seoul` - 서울 날씨 조회 (테스트용)

## 실행 방법

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.
