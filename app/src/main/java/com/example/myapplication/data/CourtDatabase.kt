package com.example.myapplication.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "courts")
data class CourtEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sportType: String,
    val location: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val pricePerHour: String,
    val rating: Double,
    val availableTimeSlots: List<String>,
    val aiMatchScore: Int,
    val isFavorite: Boolean,
    val hasAlertSet: Boolean,
    val amenities: List<String>
)

class CourtTypeConverters {
    @TypeConverter
    fun fromStringList(values: List<String>): String {
        return values.joinToString("\n")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split("\n")
    }
}

@Dao
interface CourtDao {
    @Query("SELECT * FROM courts ORDER BY rating DESC")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Query("SELECT * FROM courts ORDER BY rating DESC")
    suspend fun getAllCourtsOnce(): List<CourtEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourts(courts: List<CourtEntity>)

    @Query("DELETE FROM courts")
    suspend fun clearCourts()

    @Transaction
    suspend fun replaceCourts(courts: List<CourtEntity>) {
        clearCourts()
        insertCourts(courts)
    }

    @Query("UPDATE courts SET isFavorite = :isFavorite WHERE id = :courtId")
    suspend fun updateFavorite(courtId: String, isFavorite: Boolean)

    @Query("UPDATE courts SET hasAlertSet = :hasAlertSet WHERE id = :courtId")
    suspend fun updateAlert(courtId: String, hasAlertSet: Boolean)
}

@Database(
    entities = [CourtEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CourtTypeConverters::class)
abstract class CourtDatabase : RoomDatabase() {
    abstract fun courtDao(): CourtDao

    companion object {
        @Volatile
        private var INSTANCE: CourtDatabase? = null

        fun getDatabase(context: Context): CourtDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CourtDatabase::class.java,
                    "court_finder_database"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}

fun CourtEntity.toCourt(): Court {
    return Court(
        id = id,
        name = name,
        sportType = sportType,
        location = location,
        distanceKm = distanceKm,
        latitude = latitude,
        longitude = longitude,
        pricePerHour = pricePerHour,
        rating = rating,
        availableTimeSlots = availableTimeSlots,
        aiMatchScore = aiMatchScore,
        isFavorite = isFavorite,
        hasAlertSet = hasAlertSet,
        amenities = amenities
    )
}

fun Court.toEntity(): CourtEntity {
    return CourtEntity(
        id = id,
        name = name,
        sportType = sportType,
        location = location,
        distanceKm = distanceKm,
        latitude = latitude,
        longitude = longitude,
        pricePerHour = pricePerHour,
        rating = rating,
        availableTimeSlots = availableTimeSlots,
        aiMatchScore = aiMatchScore,
        isFavorite = isFavorite,
        hasAlertSet = hasAlertSet,
        amenities = amenities
    )
}
