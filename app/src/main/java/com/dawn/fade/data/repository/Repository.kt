package com.dawn.fade.data.repository

import com.dawn.fade.data.model.article.ArticlePageData
import com.dawn.fade.data.model.base.ApiResult
import com.dawn.fade.data.model.home.BannerItem
import com.dawn.fade.data.model.knowledge.KnowledgeTreeItem
import com.dawn.fade.data.model.user.UserData
import com.dawn.fade.data.model.user.UserProfileResponse
import com.dawn.fade.data.network.WanAndroidApiClient
import com.dawn.fade.data.network.executeApiCall

/**
 * 统一仓库层，集中封装项目中的数据请求入口和错误兜底逻辑。
 */
class Repository {
    suspend fun fetchArticlePage(
        page: Int,
        cid: Int? = null,
        author: String? = null
    ): ApiResult<ArticlePageData> {
        return executeApiCall {
            WanAndroidApiClient.service.getArticleList(page = page, cid = cid, author = author)
        }
    }

    suspend fun fetchBannerList(): ApiResult<List<BannerItem>> {
        return executeApiCall {
            WanAndroidApiClient.service.getBannerList()
        }
    }

    suspend fun fetchKnowledgeTree(): ApiResult<List<KnowledgeTreeItem>> {
        return executeApiCall {
            WanAndroidApiClient.service.getKnowledgeTree()
        }
    }

    suspend fun login(username: String, password: String): ApiResult<UserData> {
        return executeApiCall {
            WanAndroidApiClient.service.login(username = username, password = password)
        }
    }

    suspend fun register(username: String, password: String, repassword: String): ApiResult<UserData> {
        return executeApiCall {
            WanAndroidApiClient.service.register(
                username = username,
                password = password,
                repassword = repassword
            )
        }
    }

    suspend fun logout(): ApiResult<Any?> {
        return executeApiCall {
            WanAndroidApiClient.service.logout()
        }
    }

    suspend fun fetchUserInfo(): ApiResult<UserProfileResponse> {
        return executeApiCall {
            WanAndroidApiClient.service.getUserInfo()
        }
    }
}
