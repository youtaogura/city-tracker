package com.example.c_tracker.database

import androidx.room.*
import java.time.LocalDateTime
import java.util.*

@Entity(
    tableName = "reached_cities",
    indices = arrayOf(Index(value = ["prefecture_code"]))
)
data class ReachedCity(
    @PrimaryKey val code: String,
    @ColumnInfo(name = "prefecture_code") val prefectureCode: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "first_reached_at") val firstReachedAt: String
)

@Dao
interface ReachedCityDao {
//    @Transaction
    @Query("SELECT * FROM reached_cities")
    suspend fun getAll(): List<ReachedCity>

//    @Transaction
    @Query("SELECT * FROM reached_cities WHERE prefecture_code = (:prefectureCode)")
    suspend fun loadAllByPrefectureCode(prefectureCode: String): List<ReachedCity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg city: ReachedCity)
}

class ReachedCitiesRepository {
}