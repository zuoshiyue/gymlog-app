package com.gymlog.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymlog.app.data.model.WorkoutPlan
import com.gymlog.app.data.model.SplitType

/**
 * 训练计划列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanListScreen(
    plans: List<WorkoutPlan>,
    onImportDefault: () -> Unit,
    onCreateCustom: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onPlanClick: (WorkoutPlan) -> Unit,
    onDeletePlan: (WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddPlanDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<WorkoutPlan?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("我的训练计划", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToTimer) {
                        Icon(Icons.Default.Timer, contentDescription = "休息计时器")
                    }
                    IconButton(onClick = { showAddPlanDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加计划")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (plans.isEmpty()) {
                EmptyPlanState(
                    onImportDefault = onImportDefault,
                    onCreateCustom = onCreateCustom
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plans, key = { it.id }) { plan ->
                        PlanCard(
                            plan = plan,
                            onClick = { onPlanClick(plan) },
                            onEdit = { onPlanClick(plan) },
                            onDelete = {
                                planToDelete = plan
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddPlanDialog) {
        AddPlanDialog(
            onImportDefault = {
                onImportDefault()
                showAddPlanDialog = false
            },
            onCreateCustom = {
                onCreateCustom()
                showAddPlanDialog = false
            },
            onDismiss = { showAddPlanDialog = false }
        )
    }

    if (showDeleteConfirmDialog && planToDelete != null) {
        DeleteConfirmationDialog(
            planName = planToDelete!!.name,
            onConfirm = {
                onDeletePlan(planToDelete!!)
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                planToDelete = null
            }
        )
    }
}

@Composable
private fun EmptyPlanState(
    onImportDefault: () -> Unit,
    onCreateCustom: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无训练计划",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "导入模板或创建自定义计划开始吧！",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onImportDefault,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "导入三分化训练计划",
                    maxLines = 2,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCreateCustom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "创建自定义计划",
                    maxLines = 2,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: WorkoutPlan,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        text = plan.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SplitTypeBadge(plan.splitType)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${plan.workoutDays.size} 个训练日",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SplitTypeBadge(splitType: SplitType) {
    val (label, color) = when (splitType) {
        SplitType.PUSH_PULL_LEGS -> "推/拉/腿" to MaterialTheme.colorScheme.primary
        SplitType.UPPER_LOWER -> "上/下肢" to MaterialTheme.colorScheme.secondary
        SplitType.FULL_BODY -> "全身" to MaterialTheme.colorScheme.tertiary
        SplitType.CUSTOM -> "自定义" to MaterialTheme.colorScheme.outline
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color
        )
    }
}

@Composable
private fun AddPlanDialog(
    onImportDefault: () -> Unit,
    onCreateCustom: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入训练计划") },
        text = { Text("选择要导入的训练计划模板或创建自定义计划") },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        onImportDefault()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导入三分化训练计划")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onCreateCustom()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("创建自定义计划")
                }
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
 * 删除确认对话框
 */
@Composable
private fun DeleteConfirmationDialog(
    planName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("删除计划") },
        text = {
            Text("确定要删除「${planName}」吗？此操作不可撤销。")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
