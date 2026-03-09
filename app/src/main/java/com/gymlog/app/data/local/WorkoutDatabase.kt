package com.gymlog.app.data.local

import androidx.room.*
import com.gymlog.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ============ 训练计划 ============
    @Query("SELECT * FROM workout_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans WHERE id = :planId")
    suspend fun getPlanById(planId: String): WorkoutPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlan)

    @Delete
    suspend fun deletePlan(plan: WorkoutPlan)

    // ============ 训练记录 ============
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE planId = :planId ORDER BY startTime DESC")
    fun getSessionsByPlan(planId: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): WorkoutSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("UPDATE workout_sessions SET endTime = :endTime, totalDuration = :duration WHERE id = :sessionId")
    suspend fun completeSession(sessionId: String, endTime: Long, duration: Int)

    // ============ 个人最佳记录 ============
    @Query("SELECT * FROM personal_bests ORDER BY updatedAt DESC")
    fun getAllPersonalBests(): Flow<List<PersonalBest>>

    @Query("SELECT * FROM personal_bests WHERE exerciseId = :exerciseId")
    suspend fun getPersonalBestByExercise(exerciseId: String): PersonalBest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalBest(personalBest: PersonalBest)
}

@Database(
    entities = [
        WorkoutPlan::class,
        WorkoutSession::class,
        PersonalBest::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymLogDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: GymLogDatabase? = null

        fun getDatabase(context: android.content.Context): GymLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymLogDatabase::class.java,
                    "gymlog_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Room 类型转换器
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String {
        return com.google.gson.Gson().toJson(value)
    }

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        return com.google.gson.Gson().fromJson(value, Array<Exercise>::class.java).toList()
    }

    @TypeConverter
    fun fromWorkoutDayList(value: List<WorkoutDay>): String {
        return com.google.gson.Gson().toJson(value)
    }

    @TypeConverter
    fun toWorkoutDayList(value: String): List<WorkoutDay> {
        return com.google.gson.Gson().fromJson(value, Array<WorkoutDay>::class.java).toList()
    }

    @TypeConverter
    fun fromCompletedExerciseList(value: List<CompletedExercise>): String {
        return com.google.gson.Gson().toJson(value)
    }

    @TypeConverter
    fun toCompletedExerciseList(value: String): List<CompletedExercise> {
        return com.google.gson.Gson().fromJson(value, Array<CompletedExercise>::class.java).toList()
    }
}

/**
 * 个人最佳记录
 */
@Entity(tableName = "personal_bests")
data class PersonalBest(
    @PrimaryKey val exerciseId: String,
    val exerciseName: String,
    val maxWeight: Float,
    val maxReps: Int,
    val estimated1RM: Float,
    val maxVolume: Float, // 最大容量 = 重量 x 组数 x 次数
    val updatedAt: Long = System.currentTimeMillis()
)
