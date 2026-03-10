package com.gymlog.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.data.model.*
import com.gymlog.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 训练计划列表 ViewModel
 */
class PlanListViewModel(private val repository: WorkoutRepository) : ViewModel() {
    val plans: StateFlow<List<WorkoutPlan>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun importDefaultPlan() {
        viewModelScope.launch {
            repository.importDefaultPlan()
        }
    }

    fun deletePlan(plan: WorkoutPlan) {
        viewModelScope.launch {
            repository.deletePlan(plan)
        }
    }
}

/**
 * 训练中 ViewModel - 处理实时训练逻辑
 */
class ActiveWorkoutViewModel(
    private val repository: WorkoutRepository,
    private val sessionId: String
) : ViewModel() {

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

    private val _currentSets = MutableStateFlow<List<CompletedSet>>(emptyList())
    val currentSets: StateFlow<List<CompletedSet>> = _currentSets.asStateFlow()

    private var restTimerJob: kotlinx.coroutines.Job? = null

    fun setCurrentExercise(exercise: Exercise) {
        _currentSets.value = List(exercise.defaultSets) { index ->
            CompletedSet(
                setNumber = index + 1,
                reps = exercise.defaultReps,
                weight = exercise.defaultWeight,
                completed = false
            )
        }
    }

    /**
     * 更新组的重量
     */
    fun updateSetWeight(setIndex: Int, weight: Float) {
        _currentSets.value = _currentSets.value.mapIndexed { index, set ->
            if (index == setIndex) set.copy(weight = weight) else set
        }
    }

    /**
     * 更新组的次数
     */
    fun updateSetReps(setIndex: Int, reps: Int) {
        _currentSets.value = _currentSets.value.mapIndexed { index, set ->
            if (index == setIndex) set.copy(reps = reps) else set
        }
    }

    /**
     * 标记组为完成
     */
    fun completeSet(setIndex: Int) {
        _currentSets.value = _currentSets.value.mapIndexed { index, set ->
            if (index == setIndex) set.copy(completed = true) else set
        }
    }

    /**
     * 添加新组
     */
    fun addSet() {
        val nextSetNumber = _currentSets.value.size + 1
        val lastSet = _currentSets.value.lastOrNull()
        _currentSets.value = _currentSets.value + CompletedSet(
            setNumber = nextSetNumber,
            reps = lastSet?.reps ?: 10,
            weight = lastSet?.weight ?: 0f,
            completed = false
        )
    }

    /**
     * 开始休息计时器
     */
    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        _isResting.value = true

        restTimerJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            while (_restTimerSeconds.value > 0 && _isResting.value) {
                kotlinx.coroutines.delay(1000)
                _restTimerSeconds.value -= 1
            }
            if (_restTimerSeconds.value == 0) {
                _isResting.value = false
                // 播放提示音或震动
            }
        }
    }

    /**
     * 停止休息计时器
     */
    fun stopRestTimer() {
        _isResting.value = false
        restTimerJob?.cancel()
    }

    /**
     * 跳过当前动作
     */
    fun nextExercise() {
        _currentExerciseIndex.value += 1
        _isResting.value = false
        restTimerJob?.cancel()
    }

    /**
     * 返回上一个动作
     */
    fun previousExercise() {
        if (_currentExerciseIndex.value > 0) {
            _currentExerciseIndex.value -= 1
        }
    }

    /**
     * 保存当前动作并继续
     */
    suspend fun saveCurrentExercise(
        exercise: Exercise,
        targetMuscles: List<String>
    ) {
        val completedExercise = CompletedExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            sets = _currentSets.value.filter { it.completed },
            targetMuscles = targetMuscles
        )
        repository.updateExerciseInSession(sessionId, completedExercise)
    }

    override fun onCleared() {
        super.onCleared()
        restTimerJob?.cancel()
    }
}

/**
 * 训练历史 ViewModel
 */
class HistoryViewModel(repository: WorkoutRepository) : ViewModel() {
    val sessions: StateFlow<List<WorkoutSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val personalBests: StateFlow<List<PersonalBest>> = repository.personalBests
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
