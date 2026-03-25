package com.dawn.fade.data.model.user

/**
 * 用户数据模型，统一承载登录、注册和本地会话信息。
 */
data class UserData(
    var id: Int = 0,
    var username: String = "",
    var password: String = "",
    var icon: String? = null,
    var type: Int = 0,
    var collectIds: List<Int>? = null,
    var nickname: String = "",
    var email: String = "",
    var rank: String = "",
    var coinCount: Int = 0,
    var level: Int = 0,
    var publicName: String = ""
) {
    val displayName: String
        get() = nickname.ifBlank { publicName }.ifBlank { username }
}

data class UserProfileResponse(
    val coinInfo: CoinInfo = CoinInfo(),
    val userInfo: UserInfo = UserInfo()
) {
    fun toUserData(previous: UserData?): UserData {
        return UserData(
            id = userInfo.id,
            username = userInfo.username,
            password = previous?.password.orEmpty(),
            icon = userInfo.icon,
            type = userInfo.type,
            collectIds = userInfo.collectIds,
            nickname = userInfo.nickname,
            email = userInfo.email,
            rank = coinInfo.rank,
            coinCount = userInfo.coinCount,
            level = coinInfo.level,
            publicName = userInfo.publicName
        )
    }
}

data class CoinInfo(
    val coinCount: Int = 0,
    val level: Int = 0,
    val nickname: String = "",
    val rank: String = "",
    val userId: Int = 0,
    val username: String = ""
)

data class UserInfo(
    val admin: Boolean = false,
    val chapterTops: List<String> = emptyList(),
    val coinCount: Int = 0,
    val collectIds: List<Int> = emptyList(),
    val email: String = "",
    val icon: String? = null,
    val id: Int = 0,
    val nickname: String = "",
    val password: String = "",
    val publicName: String = "",
    val token: String = "",
    val type: Int = 0,
    val username: String = ""
)
