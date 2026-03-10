package com.gymlog.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 训练中屏幕 - 包含实时调整和休息计时器
 */
@Composable
fun ActiveWorkoutScreen(
    workoutDay: WorkoutDay,
    activeWorkoutViewModel: ActiveWorkoutViewModel,
    onCompleteWorkout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentExerciseIndex by activeWorkoutViewModel.currentExerciseIndex.collectAsState()
    val restTimerSeconds by activeWorkoutViewModel.restTimerSeconds.collectAsState()
    val isResting by activeWorkoutViewModel.isResting.collectAsState()
    val currentSets by activeWorkoutViewModel.currentSets.collectAsState()

    val currentExercise = workoutDay.exercises.getOrNull(currentExerciseIndex)

    if (currentExercise == null) {
        // 训练完成
        WorkoutCompleteScreen(
            onComplete = onCompleteWorkout,
            onBack = onBack
        )
        return
    }

    // 初始化当前动作
    LaunchedEffect(currentExercise.id) {
        activeWorkoutViewModel.setCurrentExercise(currentExercise)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutDay.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Text(
                        text = "${currentExerciseIndex + 1}/${workoutDay.exercises.size}",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            WorkoutBottomBar(
                isResting = isResting,
                restTimeSeconds = currentExercise.restTimeSeconds,
                onStartRest = {
                    activeWorkoutViewModel.startRestTimer(currentExercise.restTimeSeconds)
                },
                onStopRest = {
                    activeWorkoutViewModel.stopRestTimer()
                },
                onPrevious = {
                    activeWorkoutViewModel.previousExercise()
                },
                onNext = {
                    // 保存当前动作 - 由调用方处理
                    activeWorkoutViewModel.nextExercise()
                },
                canGoPrevious = currentExerciseIndex > 0
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 休息计时器显示
            if (isResting) {
                RestTimerDisplay(
                    seconds = restTimerSeconds,
                    onStop = { activeWorkoutViewModel.stopRestTimer() }
                )
            }

            // 动作信息
            ExerciseHeader(exercise = currentExercise)

            // 组列表 - 可实时调整
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(currentSets, key = { _, set -> set.setNumber }) { index, set ->
                    SetRow(
                        set = set,
                        onWeightChange = { activeWorkoutViewModel.updateSetWeight(index, it) },
                        onRepsChange = { activeWorkoutViewModel.updateSetReps(index, it) },
                        onComplete = { activeWorkoutViewModel.completeSet(index) }
                    )
                }

                item {
                    // 添加组按钮
                    Button(
                        onClick = { activeWorkoutViewModel.addSet() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加一组")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseHeader(exercise: Exercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = exercise.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "目标：${exercise.muscleGroups.joinToString(" / ")}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "默认休息：${exercise.restTimeSeconds}s",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: CompletedSet,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
    onComplete: () -> Unit
) {
    var weightText by remember { mutableStateOf(set.weight.toInt().toString()) }
    var repsText by remember { mutableStateOf(set.reps.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (set.completed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 组号
            Text(
                text = "第${set.setNumber}组",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp)
            )

            // 重量输入
            OutlinedTextField(
                value = weightText,
                onValueChange = {
                    weightText = it
                    it.toFloatOrNull()?.let { weight ->
                        onWeightChange(weight)
                    }
                },
                label = { Text("kg") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            // 次数输入
            OutlinedTextField(
                value = repsText,
                onValueChange = {
                    repsText = it
                    it.toIntOrNull()?.let { reps ->
                        onRepsChange(reps)
                    }
                },
                label = { Text("次") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            // 完成按钮
            IconButton(
                onClick = onComplete,
                enabled = !set.completed
            ) {
                Icon(
                    imageVector = if (set.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (set.completed) "已完成" else "标记完成",
                    tint = if (set.completed) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun RestTimerDisplay(
    seconds: Int,
    onStop: () -> Unit
) {
    val minutes = seconds / 60
    val secs = seconds % 60

    // 倒计时动画
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "休息中",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%02d:%02d", minutes, secs),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Button(onClick = onStop) {
                Text("跳过")
            }
        }
    }
}

@Composable
private fun WorkoutBottomBar(
    isResting: Boolean,
    restTimeSeconds: Int,
    onStartRest: () -> Unit,
    onStopRest: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canGoPrevious: Boolean
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = canGoPrevious
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("上一个")
            }

            if (isResting) {
                Button(
                    onClick = onStopRest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("停止休息")
                }
            } else {
                Button(onClick = onStartRest) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始休息 ${restTimeSeconds}s")
                }
            }

            Button(onClick = onNext) {
                Text("下一个")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun WorkoutCompleteScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("训练完成！") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "太棒了！训练完成！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text("给本次训练评分")
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                for (i in 1..5) {
                    IconButton(onClick = { rating = i }) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("训练备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存训练记录")
            }
        }
    }
}
