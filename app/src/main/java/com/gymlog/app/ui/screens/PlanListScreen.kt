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
@Composable
fun PlanListScreen(
    plans: List<WorkoutPlan>,
    onImportDefault: () -> Unit,
    onPlanClick: (WorkoutPlan) -> Unit,
    onDeletePlan: (WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddPlanDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的训练计划", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddPlanDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "导入计划")
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
                EmptyPlanState(onImportDefault = onImportDefault)
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
                            onDelete = { onDeletePlan(plan) }
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
            onDismiss = { showAddPlanDialog = false }
        )
    }
}

@Composable
private fun EmptyPlanState(onImportDefault: () -> Unit) {
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
                text = "导入经典的三分化训练计划开始吧！",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onImportDefault) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导入三分化训练计划")
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: WorkoutPlan,
    onClick: () -> Unit,
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
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入训练计划") },
        text = { Text("选择要导入的训练计划模板") },
        confirmButton = {
            Button(onClick = onImportDefault) {
                Text("导入三分化训练计划")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
