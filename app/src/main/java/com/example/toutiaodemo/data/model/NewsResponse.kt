package com.example.toutiaodemo.data.model
import com.google.gson.annotations.SerializedName

import androidx.room.Entity
import androidx.room.PrimaryKey



data class NewsResponse (
    @SerializedName("error_code") val code: Int,       // JSON的error_code → 变量名code
    @SerializedName("reason") val msg: String,         // JSON的reason → 变量名msg
    @SerializedName("result") val data: NewsData       // JSON的result → 变量名data
)



data class NewsData(
    @SerializedName("stat") val stat: String,          // JSON的stat（1=成功）
    @SerializedName("data") val newsList: List<News>,  // JSON的data → 变量名newsList
    @SerializedName("page") val page: String,          // JSON的page（页码）
    @SerializedName("pageSize") val pageSize: String   // JSON的pageSize（每页条数）
)


@Entity(tableName = "news")
data class News(

    @PrimaryKey @SerializedName("uniquekey") val id: String, // JSON的uniquekey → 主键id
    @SerializedName("title") val title: String,              // JSON的title
    @SerializedName("date") val publishTime: String,         // JSON的date → 变量名publishTime（语义化）
    @SerializedName("category") val category: String,        // JSON的category
    @SerializedName("author_name") val source: String,       // JSON的author_name → 变量名source
    @SerializedName("url") val newsUrl: String,              // JSON的url → 变量名newsUrl（避免和content混淆）
    @SerializedName("thumbnail_pic_s") val imageUrl: String? = null, // JSON的主图
    @SerializedName("thumbnail_pic_s02") val imageUrl2: String? = null, // JSON的副图1
    @SerializedName("thumbnail_pic_s03") val imageUrl3: String? = null, // JSON的副图2
    @SerializedName("is_content") val isContent: String,     // JSON的is_content（1=有内容）



    val content: String? = null
)