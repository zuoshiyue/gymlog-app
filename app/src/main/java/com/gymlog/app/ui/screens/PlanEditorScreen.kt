package com.gymlog.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gymlog.app.data.model.*
import com.gymlog.app.data.repository.WorkoutRepository
import java.util.UUID

/**
 * 训练计划编辑器 - 创建/编辑训练计划
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanEditorScreen(
    planId: String?,
    repository: WorkoutRepository,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var plan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showDayEditor by remember { mutableStateOf(false) }
    var editingDay by remember { mutableStateOf<WorkoutDay?>(null) }

    // 加载计划数据
    LaunchedEffect(planId) {
        if (planId != null) {
            plan = repository.getPlanById(planId)
        } else {
            // 创建新计划
            plan = WorkoutPlan(
                id = UUID.randomUUID().toString(),
                name = "",
                description = "",
                splitType = SplitType.CUSTOM,
                workoutDays = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
        isLoading = false
    }

    if (isLoading || plan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (planId == null) "新建计划" else "编辑计划") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveConfirm = true }) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 计划名称
            item {
                OutlinedTextField(
                    value = plan!!.name,
                    onValueChange = { plan = plan!!.copy(name = it) },
                    label = { Text("计划名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如：三分化训练计划") }
                )
            }

            // 计划描述
            item {
                OutlinedTextField(
                    value = plan!!.description,
                    onValueChange = { plan = plan!!.copy(description = it) },
                    label = { Text("计划描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("简要描述训练计划的目标和特点") }
                )
            }

            // 分化类型
            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = when (plan!!.splitType) {
                            SplitType.PUSH_PULL_LEGS -> "推/拉/腿"
                            SplitType.UPPER_LOWER -> "上/下肢"
                            SplitType.FULL_BODY -> "全身"
                            SplitType.CUSTOM -> "自定义"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分化类型") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("推/拉/腿") },
                            onClick = {
                                plan = plan!!.copy(splitType = SplitType.PUSH_PULL_LEGS)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("上/下肢") },
                            onClick = {
                                plan = plan!!.copy(splitType = SplitType.UPPER_LOWER)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("全身") },
                            onClick = {
                                plan = plan!!.copy(splitType = SplitType.FULL_BODY)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("自定义") },
                            onClick = {
                                plan = plan!!.copy(splitType = SplitType.CUSTOM)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 训练日列表标题
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "训练日",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(onClick = {
                        editingDay = null // null 表示新建
                        showDayEditor = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加训练日")
                    }
                }
            }

            // 训练日列表
            if (plan!!.workoutDays.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "暂无训练日",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "点击上方的「添加训练日」按钮开始",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(plan!!.workoutDays, key = { _, day -> day.id }) { index, day ->
                    EditableWorkoutDayCard(
                        workoutDay = day,
                        index = index,
                        onEdit = {
                            editingDay = day
                            showDayEditor = true
                        },
                        onDelete = {
                            // 删除确认
                            plan = plan!!.copy(
                                workoutDays = plan!!.workoutDays.filter { it.id != day.id }
                            )
                        },
                        onMoveUp = if (index > 0) {{
                            val days = plan!!.workoutDays.toMutableList()
                            days[index] = days[index - 1]
                            days[index - 1] = day
                            plan = plan!!.copy(workoutDays = days)
                        }} else null,
                        onMoveDown = if (index < plan!!.workoutDays.size - 1) {{
                            val days = plan!!.workoutDays.toMutableList()
                            days[index] = days[index + 1]
                            days[index + 1] = day
                            plan = plan!!.copy(workoutDays = days)
                        }} else null
                    )
                }
            }
        }
    }

    // 保存确认对话框
    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("保存计划") },
            text = {
                if (plan!!.name.isBlank()) {
                    Text("请输入计划名称")
                } else if (plan!!.workoutDays.isEmpty()) {
                    Text("请至少添加一个训练日")
                } else {
                    Text("确定要保存这个训练计划吗？")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plan!!.name.isNotBlank() && plan!!.workoutDays.isNotEmpty()) {
                            // 保存计划
                            runBlocking {
                                if (planId == null) {
                                    repository.createPlan(
                                        name = plan!!.name,
                                        description = plan!!.description,
                                        splitType = plan!!.splitType,
                                        workoutDays = plan!!.workoutDays
                                    )
                                } else {
                                    repository.updatePlan(plan!!)
                                }
                            }
                            onSave()
                        }
                    },
                    enabled = plan!!.name.isNotBlank() && plan!!.workoutDays.isNotEmpty()
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 训练日编辑器对话框
    if (showDayEditor) {
        WorkoutDayEditorDialog(
            workoutDay = editingDay,
            onSave = { day ->
                if (editingDay == null) {
                    // 新增
                    plan = plan!!.copy(workoutDays = plan!!.workoutDays + day)
                } else {
                    // 更新
                    plan = plan!!.copy(
                        workoutDays = plan!!.workoutDays.map { if (it.id == day.id) day else it }
                    )
                }
                showDayEditor = false
            },
            onDismiss = { showDayEditor = false }
        )
    }
}

/**
 * 可编辑的训练日卡片
 */
@Composable
private fun EditableWorkoutDayCard(
    workoutDay: WorkoutDay,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. ${workoutDay.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${workoutDay.exercises.size} 个动作 · 预计 ${workoutDay.estimatedDuration} 分钟",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onMoveUp ?: {}, enabled = onMoveUp != null) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "上移",
                            tint = if (onMoveUp == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onMoveDown ?: {}, enabled = onMoveDown != null) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "下移",
                            tint = if (onMoveDown == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 动作列表预览
            if (workoutDay.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                workoutDay.exercises.take(3).forEach { exercise ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exercise.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${exercise.defaultSets}组 x ${exercise.defaultReps}次",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                if (workoutDay.exercises.size > 3) {
                    Text(
                        text = "还有 ${workoutDay.exercises.size - 3} 个动作...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除训练日") },
            text = { Text("确定要删除「${workoutDay.name}」吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 训练日编辑器对话框
 */
@Composable
private fun WorkoutDayEditorDialog(
    workoutDay: WorkoutDay?,
    onSave: (WorkoutDay) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(workoutDay?.name ?: "") }
    var duration by remember { mutableStateOf((workoutDay?.estimatedDuration ?: 60).toString()) }
    var exercises by remember { mutableStateOf(workoutDay?.exercises ?: emptyList()) }
    var showExerciseEditor by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (workoutDay == null) "添加训练日" else "编辑训练日") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("训练日名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如：推日 - 胸部/三头") }
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("预计时长（分钟）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "训练动作",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    FilledTonalButton(onClick = {
                        editingExercise = null
                        showExerciseEditor = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (exercises.isEmpty()) {
                    Text(
                        "暂无动作，点击上方按钮添加",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    exercises.forEachIndexed { index, exercise ->
                        ExerciseItem(
                            exercise = exercise,
                            index = index,
                            onEdit = {
                                editingExercise = exercise
                                showExerciseEditor = true
                            },
                            onDelete = {
                                exercises = exercises.filter { it.id != exercise.id }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WorkoutDay(
                            id = workoutDay?.id ?: UUID.randomUUID().toString(),
                            name = name.ifBlank { "训练日" },
                            estimatedDuration = duration.toIntOrNull() ?: 60,
                            exercises = exercises
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showExerciseEditor) {
        ExerciseEditorDialog(
            exercise = editingExercise,
            onSave = { exercise ->
                if (editingExercise == null) {
                    exercises = exercises + exercise
                } else {
                    exercises = exercises.map { if (it.id == exercise.id) exercise else it }
                }
                showExerciseEditor = false
            },
            onDismiss = { showExerciseEditor = false }
        )
    }
}

/**
 * 动作列表项
 */
@Composable
private fun ExerciseItem(
    exercise: Exercise,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${exercise.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${exercise.defaultSets}组 x ${exercise.defaultReps}次 @ ${exercise.defaultWeight}kg · 休息${exercise.restTimeSeconds}s",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * 动作编辑器对话框
 */
@Composable
private fun ExerciseEditorDialog(
    exercise: Exercise?,
    onSave: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var sets by remember { mutableStateOf((exercise?.defaultSets ?: 3).toString()) }
    var reps by remember { mutableStateOf((exercise?.defaultReps ?: 10).toString()) }
    var weight by remember { mutableStateOf((exercise?.defaultWeight ?: 0f).toString()) }
    var restTime by remember { mutableStateOf((exercise?.restTimeSeconds ?: 90).toString()) }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exercise == null) "添加动作" else "编辑动作") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("动作名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如：杠铃卧推") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("组数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("次数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("重量 (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = restTime,
                        onValueChange = { restTime = it },
                        label = { Text("休息 (秒)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    placeholder = { Text("动作要点、注意事项等") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Exercise(
                            id = exercise?.id ?: UUID.randomUUID().toString(),
                            name = name.ifBlank { "未知动作" },
                            category = exercise?.category ?: ExerciseCategory.COMPOUND,
                            muscleGroups = exercise?.muscleGroups ?: emptyList(),
                            equipment = exercise?.equipment ?: "barbell",
                            defaultSets = sets.toIntOrNull() ?: 3,
                            defaultReps = reps.toIntOrNull() ?: 10,
                            defaultWeight = weight.toFloatOrNull() ?: 0f,
                            restTimeSeconds = restTime.toIntOrNull() ?: 90,
                            notes = notes,
                            order = exercise?.order ?: 0
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
