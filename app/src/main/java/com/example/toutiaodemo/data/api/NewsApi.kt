
package com.example.toutiaodemo.data.api

import com.example.toutiaodemo.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    /**
     * 适配真实接口的参数说明：
     * - type: 新闻类型（top=头条，shehui=社会，guonei=国内，guoji=国际，yule=娱乐，tiyu=体育，junshi=军事，keji=科技，caijing=财经，shishang=时尚）
     * - key: API密钥
     * - page: 页码
     * - page_size: 每页条数
     * 真实接口返回结构：code=200、msg=success 代表成功，result.list是新闻列表
     */
    @GET("index")
    suspend fun getNewsList(
        @Query("type") category: String,
        @Query("key") key: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponse
}