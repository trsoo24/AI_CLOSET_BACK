package ai.closet.domain.wardrobe.entity

enum class Category(
    val displayName: String,
    val description: String
) {
    TOP("상의", "티셔츠, 블라우스, 니트 등"),
    BOTTOM("하의", "바지, 치마, 레깅스 등"),
    OUTER("아우터", "코트, 자켓, 가디건 등"),
    DRESS("원피스", "원피스, 점프수트 등"),
    SHOES("신발", "운동화, 구두, 샌들 등"),
    BAG("가방", "백팩, 숄더백, 토트백 등"),
    ACCESSORY("액세서리", "목걸이, 귀걸이, 벨트 등"),
    HAT("모자", "캡, 비니, 버킷햇 등"),
    ETC("기타", "기타 아이템")
}