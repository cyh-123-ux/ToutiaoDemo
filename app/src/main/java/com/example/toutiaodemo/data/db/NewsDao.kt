package com.example.toutiaodemo.data.db
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.toutiaodemo.data.model.News
import kotlinx.coroutines.flow.Flow


@Dao
interface NewsDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsList(newsList: List<News>)

    @Query("SELECT * FROM news WHERE category = :category ORDER BY publishTime DESC")
    fun getNewsByCategory(category: String): Flow<List<News>>

    // 读取所有本地新闻（用于搜索）
    @Query("SELECT * FROM news")
    fun getAllNews(): Flow<List<News>>

    // 清空新闻返回删除数量
    @Query("DELETE FROM news WHERE category = :category")
    suspend fun clearNewsByCategory(category: String): Int // 关键修改：返回 Int + suspend

}