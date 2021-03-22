package com.example.c_tracker.database

import androidx.room.*

@Entity(tableName = "reached_prefectures")
data class ReachedPrefecture(
    @PrimaryKey val code: String,
    @ColumnInfo(name = "name") val name: String
)

@Dao
interface ReachedPrefectureDao {
//    @Transaction
    @Query("SELECT * FROM reached_prefectures")
    suspend fun getAll(): List<ReachedPrefecture>

//    @Transaction
    @Query("SELECT * FROM reached_prefectures WHERE code = (:code)")
    suspend fun loadById(code: String): ReachedPrefecture

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg prefecture: ReachedPrefecture)
}

class ReachedPrefecturesRepository {
}