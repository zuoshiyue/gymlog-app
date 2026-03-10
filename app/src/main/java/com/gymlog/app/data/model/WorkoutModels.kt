package com.gymlog.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gymlog.app.data.local.Converters

/**
 * 健身计划模板 - 三分化训练（推/拉/腿）
 */
@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val splitType: SplitType = SplitType.PUSH_PULL_LEGS,
    @TypeConverters(Converters::class) val workoutDays: List<WorkoutDay>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SplitType {
    PUSH_PULL_LEGS,      // 推/拉/腿
    UPPER_LOWER,         // 上/下肢
    FULL_BODY,           // 全身
    CUSTOM               // 自定义
}

/**
 * 训练日（如：推日、拉日、腿日）
 */
data class WorkoutDay(
    val id: String,
    val name: String,           // 如："推日 - 胸部/三头"
    val dayOfWeek: Int? = null, // 1-7 (周一 - 周日)，null 表示灵活安排
    val exercises: List<Exercise>,
    val estimatedDuration: Int = 60 // 预计时长（分钟）
)

/**
 * 训练动作
 */
data class Exercise(
    val id: String,
    val name: String,                   // 动作名称
    val category: ExerciseCategory,     // 动作类别
    val muscleGroups: List<String>,     // 目标肌群
    val equipment: String = "barbell",  // 所需器械
    val defaultSets: Int = 3,           // 默认组数
    val defaultReps: Int = 10,          // 默认次数
    val defaultWeight: Float = 0f,      // 默认重量 (kg)
    val restTimeSeconds: Int = 90,      // 默认休息时长（秒）
    val notes: String = "",             // 动作说明
    val order: Int = 0                  // 动作顺序
)

enum class ExerciseCategory {
    COMPOUND,      // 复合动作
    ISOLATION,     // 孤立动作
    ACCESSORY,     // 辅助动作
    WARMUP,        // 热身动作
    STRETCH        // 拉伸动作
}

/**
 * 训练记录 - 单次训练的实际执行
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey val id: String,
    val planId: String,
    val workoutDayId: String,
    val startTime: Long,
    val endTime: Long? = null,
    @TypeConverters(Converters::class) val completedExercises: List<CompletedExercise>,
    val totalDuration: Int = 0,       // 总时长（分钟）
    val notes: String = "",
    val rating: Int = 0               // 训练评分 1-5
)

/**
 * 已完成的动作记录
 */
data class CompletedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val sets: List<CompletedSet>,
    val targetMuscles: List<String>
)

/**
 * 已完成的组记录
 */
data class CompletedSet(
    val setNumber: Int,
    val reps: Int,
    val weight: Float,
    val completed: Boolean = true,
    val rpe: Int? = null,             // 自觉用力程度 1-10
    val notes: String = ""
)

/**
 * 训练中的实时状态
 */
data class ActiveWorkout(
    val sessionId: String,
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,
    val restTimerSeconds: Int = 0,
    val isResting: Boolean = false,
    val startTime: Long = System.currentTimeMillis()
)
