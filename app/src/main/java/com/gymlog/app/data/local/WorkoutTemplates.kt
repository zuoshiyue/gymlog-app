package com.gymlog.app.data.local

import com.gymlog.app.data.model.*
import java.util.UUID

/**
 * 默认健身计划模板 - 三分化训练（推/拉/腿）
 */
object DefaultWorkoutTemplates {

    fun getPushPullLegsPlan(): WorkoutPlan {
        return WorkoutPlan(
            id = UUID.randomUUID().toString(),
            name = "经典三分化训练",
            description = "推/拉/腿分化，适合中级训练者，每周训练 3-6 天",
            splitType = SplitType.PUSH_PULL_LEGS,
            workoutDays = listOf(
                createPushDay(),
                createPullDay(),
                createLegsDay()
            )
        )
    }

    private fun createPushDay(): WorkoutDay {
        return WorkoutDay(
            id = UUID.randomUUID().toString(),
            name = "推日 - 胸部/肩部/三头肌",
            dayOfWeek = 1, // 周一
            estimatedDuration = 70,
            exercises = listOf(
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "杠铃卧推",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("胸部", "三头肌", "前束"),
                    equipment = "barbell",
                    defaultSets = 4,
                    defaultReps = 8,
                    defaultWeight = 60f,
                    restTimeSeconds = 120,
                    order = 1
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "上斜哑铃卧推",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("上胸", "前束"),
                    equipment = "dumbbell",
                    defaultSets = 3,
                    defaultReps = 10,
                    defaultWeight = 25f,
                    restTimeSeconds = 90,
                    order = 2
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "哑铃侧平举",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("中束"),
                    equipment = "dumbbell",
                    defaultSets = 3,
                    defaultReps = 12,
                    defaultWeight = 10f,
                    restTimeSeconds = 60,
                    order = 3
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "绳索下压",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("三头肌"),
                    equipment = "cable",
                    defaultSets = 3,
                    defaultReps = 12,
                    defaultWeight = 20f,
                    restTimeSeconds = 60,
                    order = 4
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "双杠臂屈伸",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("下胸", "三头肌"),
                    equipment = "bodyweight",
                    defaultSets = 3,
                    defaultReps = 10,
                    defaultWeight = 0f,
                    restTimeSeconds = 90,
                    order = 5
                )
            )
        )
    }

    private fun createPullDay(): WorkoutDay {
        return WorkoutDay(
            id = UUID.randomUUID().toString(),
            name = "拉日 - 背部/二头肌/后束",
            dayOfWeek = 3, // 周三
            estimatedDuration = 70,
            exercises = listOf(
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "引体向上",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("背阔肌", "二头肌"),
                    equipment = "pullup_bar",
                    defaultSets = 4,
                    defaultReps = 8,
                    defaultWeight = 0f,
                    restTimeSeconds = 120,
                    order = 1
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "杠铃划船",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("中背", "后束"),
                    equipment = "barbell",
                    defaultSets = 4,
                    defaultReps = 8,
                    defaultWeight = 50f,
                    restTimeSeconds = 120,
                    order = 2
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "坐姿划船",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("中背"),
                    equipment = "cable",
                    defaultSets = 3,
                    defaultReps = 10,
                    defaultWeight = 40f,
                    restTimeSeconds = 90,
                    order = 3
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "面拉",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("后束", "三角肌后束"),
                    equipment = "cable",
                    defaultSets = 3,
                    defaultReps = 15,
                    defaultWeight = 15f,
                    restTimeSeconds = 60,
                    order = 4
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "杠铃弯举",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("二头肌"),
                    equipment = "barbell",
                    defaultSets = 3,
                    defaultReps = 10,
                    defaultWeight = 30f,
                    restTimeSeconds = 60,
                    order = 5
                )
            )
        )
    }

    private fun createLegsDay(): WorkoutDay {
        return WorkoutDay(
            id = UUID.randomUUID().toString(),
            name = "腿日 - 股四头肌/腘绳肌/小腿",
            dayOfWeek = 5, // 周五
            estimatedDuration = 75,
            exercises = listOf(
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "杠铃深蹲",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("股四头肌", "臀部", "核心"),
                    equipment = "barbell",
                    defaultSets = 4,
                    defaultReps = 8,
                    defaultWeight = 80f,
                    restTimeSeconds = 150,
                    order = 1
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "罗马尼亚硬拉",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("腘绳肌", "臀部", "下背"),
                    equipment = "barbell",
                    defaultSets = 3,
                    defaultReps = 10,
                    defaultWeight = 60f,
                    restTimeSeconds = 120,
                    order = 2
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "腿举",
                    category = ExerciseCategory.COMPOUND,
                    muscleGroups = listOf("股四头肌", "臀部"),
                    equipment = "machine",
                    defaultSets = 3,
                    defaultReps = 12,
                    defaultWeight = 150f,
                    restTimeSeconds = 90,
                    order = 3
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "腿弯举",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("腘绳肌"),
                    equipment = "machine",
                    defaultSets = 3,
                    defaultReps = 12,
                    defaultWeight = 40f,
                    restTimeSeconds = 60,
                    order = 4
                ),
                Exercise(
                    id = UUID.randomUUID().toString(),
                    name = "站姿提踵",
                    category = ExerciseCategory.ISOLATION,
                    muscleGroups = listOf("小腿"),
                    equipment = "machine",
                    defaultSets = 4,
                    defaultReps = 15,
                    defaultWeight = 50f,
                    restTimeSeconds = 45,
                    order = 5
                )
            )
        )
    }
}
