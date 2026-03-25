package com.dawn.fade.data.network.service

import com.dawn.fade.data.model.article.ArticlePageData
import com.dawn.fade.data.model.base.BaseResponse
import com.dawn.fade.data.model.home.BannerItem
import com.dawn.fade.data.model.knowledge.KnowledgeTreeItem
import com.dawn.fade.data.model.user.UserData
import com.dawn.fade.data.model.user.UserProfileResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 统一接口定义，集中维护项目中所有网络请求方法。
 */
interface ApiService {
    @GET("article/list/{page}/json")
    suspend fun getArticleList(
        @Path("page") page: Int,
        @Query("cid") cid: Int? = null,
        @Query("author") author: String? = null
    ): BaseResponse<ArticlePageData>

    @GET("banner/json")
    suspend fun getBannerList(): BaseResponse<List<BannerItem>>

    @GET("tree/json")
    suspend fun getKnowledgeTree(): BaseResponse<List<KnowledgeTreeItem>>

    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): BaseResponse<UserData>

    @FormUrlEncoded
    @POST("user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("repassword") repassword: String
    ): BaseResponse<UserData>

    @GET("user/logout/json")
    suspend fun logout(): BaseResponse<Any?>

    @GET("user/lg/userinfo/json")
    suspend fun getUserInfo(): BaseResponse<UserProfileResponse>
}
