package com.gymlog.app.data.repository

import com.gymlog.app.data.local.WorkoutDao
import com.gymlog.app.data.local.DefaultWorkoutTemplates
import com.gymlog.app.data.local.PersonalBest
import com.gymlog.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * 训练数据仓库 - 纯本地模式
 */
class WorkoutRepository(private val dao: WorkoutDao) {

    // ============ 训练计划管理 ============
    
    val allPlans: Flow<List<WorkoutPlan>> = dao.getAllPlans()

    suspend fun getPlanById(planId: String): WorkoutPlan? {
        return withContext(Dispatchers.IO) {
            dao.getPlanById(planId)
        }
    }

    /**
     * 导入默认三分化训练计划
     */
    suspend fun importDefaultPlan(): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = DefaultWorkoutTemplates.getPushPullLegsPlan()
            dao.insertPlan(plan)
            plan
        }
    }

    /**
     * 从 JSON 导入训练计划
     */
    suspend fun importPlanFromJson(json: String): WorkoutPlan? {
        return withContext(Dispatchers.IO) {
            try {
                val plan = com.google.gson.Gson().fromJson(json, WorkoutPlan::class.java)
                dao.insertPlan(plan)
                plan
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 导出训练计划为 JSON
     */
    suspend fun exportPlanToJson(plan: WorkoutPlan): String {
        return withContext(Dispatchers.IO) {
            com.google.gson.Gson().toJson(plan)
        }
    }

    suspend fun savePlan(plan: WorkoutPlan) {
        withContext(Dispatchers.IO) {
            dao.insertPlan(plan.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deletePlan(plan: WorkoutPlan) {
        withContext(Dispatchers.IO) {
            dao.deletePlan(plan)
        }
    }

    /**
     * 创建新的训练计划
     */
    suspend fun createPlan(
        name: String,
        description: String = "",
        splitType: SplitType = SplitType.CUSTOM,
        workoutDays: List<WorkoutDay> = emptyList()
    ): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = WorkoutPlan(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                splitType = splitType,
                workoutDays = workoutDays,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            dao.insertPlan(plan)
            plan
        }
    }

    /**
     * 更新训练计划
     */
    suspend fun updatePlan(plan: WorkoutPlan): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val updated = plan.copy(updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    // ============ 训练日管理 ============

    /**
     * 添加训练日到计划
     */
    suspend fun addWorkoutDayToPlan(planId: String, workoutDay: WorkoutDay): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays + workoutDay
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    /**
     * 更新训练日
     */
    suspend fun updateWorkoutDay(planId: String, workoutDayId: String, updatedDay: WorkoutDay): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays.map { if (it.id == workoutDayId) updatedDay else it }
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    /**
     * 从计划中删除训练日
     */
    suspend fun deleteWorkoutDayFromPlan(planId: String, workoutDayId: String): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays.filter { it.id != workoutDayId }
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    // ============ 动作管理 ============

    /**
     * 添加动作到训练日
     */
    suspend fun addExerciseToWorkoutDay(planId: String, workoutDayId: String, exercise: Exercise): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays.map { day ->
                if (day.id == workoutDayId) {
                    day.copy(exercises = day.exercises + exercise)
                } else day
            }
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    /**
     * 更新训练日中的动作
     */
    suspend fun updateExerciseInWorkoutDay(planId: String, workoutDayId: String, exerciseId: String, updatedExercise: Exercise): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays.map { day ->
                if (day.id == workoutDayId) {
                    day.copy(exercises = day.exercises.map { ex ->
                        if (ex.id == exerciseId) updatedExercise else ex
                    })
                } else day
            }
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    /**
     * 从训练日中删除动作
     */
    suspend fun deleteExerciseFromWorkoutDay(planId: String, workoutDayId: String, exerciseId: String): WorkoutPlan {
        return withContext(Dispatchers.IO) {
            val plan = dao.getPlanById(planId) ?: throw IllegalArgumentException("Plan not found")
            val updatedDays = plan.workoutDays.map { day ->
                if (day.id == workoutDayId) {
                    day.copy(exercises = day.exercises.filter { it.id != exerciseId })
                } else day
            }
            val updated = plan.copy(workoutDays = updatedDays, updatedAt = System.currentTimeMillis())
            dao.insertPlan(updated)
            updated
        }
    }

    // ============ 训练会话管理 ============
    
    val allSessions: Flow<List<WorkoutSession>> = dao.getAllSessions()

    fun getSessionsByPlan(planId: String): Flow<List<WorkoutSession>> {
        return dao.getSessionsByPlan(planId)
    }

    /**
     * 开始新的训练会话
     */
    suspend fun startWorkoutSession(planId: String, workoutDayId: String): WorkoutSession {
        return withContext(Dispatchers.IO) {
            val session = WorkoutSession(
                id = UUID.randomUUID().toString(),
                planId = planId,
                workoutDayId = workoutDayId,
                startTime = System.currentTimeMillis(),
                completedExercises = emptyList()
            )
            dao.insertSession(session)
            session
        }
    }

    /**
     * 更新训练中的动作记录
     */
    suspend fun updateExerciseInSession(
        sessionId: String,
        completedExercise: CompletedExercise
    ) {
        withContext(Dispatchers.IO) {
            val session = dao.getSessionById(sessionId) ?: return@withContext
            val updatedExercises = session.completedExercises
                .filter { it.exerciseId != completedExercise.exerciseId } + completedExercise
            
            val updatedSession = session.copy(completedExercises = updatedExercises)
            dao.insertSession(updatedSession)
        }
    }

    /**
     * 完成训练会话
     */
    suspend fun completeWorkoutSession(
        sessionId: String,
        rating: Int = 0,
        notes: String = ""
    ) {
        withContext(Dispatchers.IO) {
            val session = dao.getSessionById(sessionId) ?: return@withContext
            val endTime = System.currentTimeMillis()
            val duration = ((endTime - session.startTime) / 1000 / 60).toInt()
            
            val updatedSession = session.copy(
                endTime = endTime,
                totalDuration = duration,
                rating = rating,
                notes = notes
            )
            dao.insertSession(updatedSession)
            
            // 更新个人最佳记录
            updatePersonalBests(updatedSession)
        }
    }

    /**
     * 更新个人最佳记录
     */
    private suspend fun updatePersonalBests(session: WorkoutSession) {
        session.completedExercises.forEach { completedExercise ->
            val maxWeightInSession = completedExercise.sets
                .filter { it.completed }
                .maxOfOrNull { it.weight } ?: 0f
            
            val maxRepsInSession = completedExercise.sets
                .filter { it.completed && it.weight == maxWeightInSession }
                .maxOfOrNull { it.reps } ?: 0
            
            val estimated1RM = calculate1RM(maxWeightInSession, maxRepsInSession)
            
            val totalVolume = completedExercise.sets
                .filter { it.completed }
                .sumOf { (it.weight * it.reps).toDouble() }
            
            val existingPB = dao.getPersonalBestByExercise(completedExercise.exerciseId)
            
            if (existingPB == null || maxWeightInSession > existingPB.maxWeight) {
                dao.insertPersonalBest(
                    PersonalBest(
                        exerciseId = completedExercise.exerciseId,
                        exerciseName = completedExercise.exerciseName,
                        maxWeight = maxWeightInSession,
                        maxReps = maxRepsInSession,
                        estimated1RM = estimated1RM,
                        maxVolume = totalVolume.toFloat()
                    )
                )
            }
        }
    }

    /**
     * 计算 1RM (Epley 公式)
     */
    private fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0) return 0f
        return weight * (1 + reps / 30f)
    }

    val personalBests: Flow<List<PersonalBest>> = dao.getAllPersonalBests()

    suspend fun getPersonalBest(exerciseId: String): PersonalBest? {
        return withContext(Dispatchers.IO) {
            dao.getPersonalBestByExercise(exerciseId)
        }
    }
}
