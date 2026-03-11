package com.gymlog.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.app.data.model.RestTimerRecord
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 独立休息计时器屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestTimerScreen(
    onBack: () -> Unit
) {
    var selectedSeconds by remember { mutableStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(60) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var timerRecords by remember { mutableStateOf<List<RestTimerRecord>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // 加载历史记录
    LaunchedEffect(Unit) {
        // 从 SharedPreferences 或简单存储中读取记录
        // 这里简化处理，实际应该使用 DataStore
    }

    // 倒计时逻辑
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (isRunning && remainingSeconds == 0) {
            isRunning = false
            // 休息完成，保存记录
            val record = RestTimerRecord(
                id = UUID.randomUUID().toString(),
                durationSeconds = selectedSeconds,
                completedAt = System.currentTimeMillis()
            )
            // 实际应该保存到数据库，这里简化
        }
    }

    // 格式化时间显示
    val minutes = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, secs)

    // 进度动画
    val progress = if (selectedSeconds > 0) remainingSeconds.toFloat() / selectedSeconds else 0f
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("休息计时器") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 计时器显示
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(if (isRunning) scale else 1f)
            ) {
                // 圆形进度条
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(280.dp),
                    strokeWidth = 12.dp,
                    color = if (remainingSeconds <= 10 && isRunning)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // 时间显示
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeText,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingSeconds <= 10 && isRunning)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isRunning) "休息中" else "准备就绪",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 预设时间选择
            Text(
                text = "预设时间",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(30, 60, 90, 120)
                presets.forEach { seconds ->
                    TimePresetChip(
                        seconds = seconds,
                        selected = selectedSeconds == seconds && !isRunning,
                        onClick = {
                            if (!isRunning) {
                                selectedSeconds = seconds
                                remainingSeconds = seconds
                            }
                        },
                        enabled = !isRunning
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 自定义时间按钮
            OutlinedButton(
                onClick = { showCustomTimeDialog = true },
                enabled = !isRunning,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("自定义时间")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 控制按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 重置按钮
                OutlinedButton(
                    onClick = {
                        remainingSeconds = selectedSeconds
                        isRunning = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning || remainingSeconds != selectedSeconds
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重置")
                }

                // 开始/停止按钮
                Button(
                    onClick = {
                        if (isRunning) {
                            isRunning = false
                        } else {
                            isRunning = true
                        }
                    },
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRunning) "暂停" else "开始")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 历史记录
            if (timerRecords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "历史记录",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timerRecords.size) { index ->
                        val record = timerRecords[index]
                        RestTimerRecordItem(record = record)
                    }
                }
            }
        }
    }

    // 自定义时间对话框
    if (showCustomTimeDialog) {
        CustomTimeDialog(
            onConfirm = { seconds ->
                selectedSeconds = seconds
                remainingSeconds = seconds
                showCustomTimeDialog = false
            },
            onDismiss = { showCustomTimeDialog = false }
        )
    }
}

/**
 * 时间预设芯片按钮
 */
@Composable
private fun TimePresetChip(
    seconds: Int,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val label = when {
        seconds < 60 -> "${seconds}s"
        seconds == 60 -> "1 分钟"
        seconds % 60 == 0 -> "${seconds / 60}分钟"
        else -> "${seconds / 60}分${seconds % 60}秒"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        modifier = Modifier.weight(1f),
        enabled = enabled
    )
}

/**
 * 历史记录项
 */
@Composable
private fun RestTimerRecordItem(record: RestTimerRecord) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
    val durationText = when {
        record.durationSeconds < 60 -> "${record.durationSeconds}秒"
        record.durationSeconds == 60 -> "1 分钟"
        record.durationSeconds % 60 == 0 -> "${record.durationSeconds / 60}分钟"
        else -> "${record.durationSeconds / 60}分${record.durationSeconds % 60}秒"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "休息 $durationText",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(record.completedAt)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 自定义时间输入对话框
 */
@Composable
private fun CustomTimeDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var minutesText by remember { mutableStateOf("1") }
    var secondsText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义休息时间") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it },
                        label = { Text("分钟") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it },
                        label = { Text("秒") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                val totalSeconds = (minutesText.toIntOrNull() ?: 0) * 60 + (secondsText.toIntOrNull() ?: 0)
                Text(
                    text = "总计：$totalSeconds 秒",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalSeconds = (minutesText.toIntOrNull() ?: 0) * 60 + (secondsText.toIntOrNull() ?: 0)
                    if (totalSeconds in 1..3600) {
                        onConfirm(totalSeconds)
                    }
                }
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

/**
 * 圆形进度条组件
 */
@Composable
private fun CircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // 绘制背景圆环
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // 绘制进度圆弧
        if (progress > 0) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke
            )
        }
    }
}
