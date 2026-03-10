package com.gymlog.app.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowBack
import com.gymlog.app.data.local.GymLogDatabase
import com.gymlog.app.data.local.PersonalBest
import com.gymlog.app.data.model.WorkoutDay
import com.gymlog.app.data.repository.WorkoutRepository
import com.gymlog.app.ui.theme.GymLogTheme
import kotlinx.coroutines.runBlocking

/**
 * 应用主导航
 */
@Composable
fun GymLogApp() {
    // 初始化数据库和 Repository
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { GymLogDatabase.getDatabase(context) }
    val repository = remember { WorkoutRepository(database.workoutDao()) }

    val navController = rememberNavController()

    GymLogTheme {
        NavHost(
            navController = navController,
            startDestination = "plans"
        ) {
            composable("plans") {
                val viewModel: PlanListViewModel = viewModel {
                    PlanListViewModel(repository)
                }
                val plans by viewModel.plans.collectAsState()

                PlanListScreen(
                    plans = plans,
                    onImportDefault = { viewModel.importDefaultPlan() },
                    onPlanClick = { plan ->
                        navController.navigate("plan_detail/${plan.id}")
                    },
                    onDeletePlan = { plan ->
                        viewModel.deletePlan(plan)
                    }
                )
            }

            composable("plan_detail/{planId}") { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                PlanDetailScreen(
                    planId = planId,
                    repository = repository,
                    onStartWorkout = { workoutDay ->
                        navController.navigate("workout/${planId}/${workoutDay.id}")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("workout/{planId}/{workoutDayId}") { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                val workoutDayId = backStackEntry.arguments?.getString("workoutDayId") ?: return@composable
                
                // 创建训练会话并进入训练界面
                var sessionId by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(Unit) {
                    val session = repository.startWorkoutSession(planId, workoutDayId)
                    sessionId = session.id
                }

                sessionId?.let { sid ->
                    val viewModel: ActiveWorkoutViewModel = viewModel {
                        ActiveWorkoutViewModel(repository, sid)
                    }
                    
                    // 获取训练日信息
                    val plan = remember(planId) { 
                        runBlocking { repository.getPlanById(planId) } 
                    }
                    val workoutDay = plan?.workoutDays?.find { it.id == workoutDayId }
                    
                    if (workoutDay != null) {
                        ActiveWorkoutScreen(
                            workoutDay = workoutDay,
                            activeWorkoutViewModel = viewModel,
                            onCompleteWorkout = {
                                navController.popBackStack("plans", inclusive = false)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            composable("history") {
                val viewModel: HistoryViewModel = viewModel {
                    HistoryViewModel(repository)
                }
                val sessions by viewModel.sessions.collectAsState()
                val personalBests by viewModel.personalBests.collectAsState()

                HistoryScreen(
                    sessions = sessions,
                    personalBests = personalBests,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// 移除重复的 runBlocking 定义，使用 kotlinx.coroutines.runBlocking

/**
 * 训练计划详情屏幕
 */
@Composable
fun PlanDetailScreen(
    planId: String,
    repository: WorkoutRepository,
    onStartWorkout: (WorkoutDay) -> Unit,
    onBack: () -> Unit
) {
    val plan = remember(planId) {
        runBlocking { repository.getPlanById(planId) }
    }

    if (plan == null) {
        onBack()
        return
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(plan.name) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 计划描述
            androidx.compose.material3.Card(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = plan.description,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 训练日列表
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(plan.workoutDays) { day ->
                    WorkoutDayCard(
                        workoutDay = day,
                        onStartWorkout = { onStartWorkout(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutDayCard(
    workoutDay: WorkoutDay,
    onStartWorkout: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = workoutDay.name,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = "${workoutDay.exercises.size} 个动作 · 预计 ${workoutDay.estimatedDuration} 分钟",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = onStartWorkout,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = null
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                androidx.compose.material3.Text("开始训练")
            }
        }
    }
}

/**
 * 训练历史屏幕
 */
@Composable
fun HistoryScreen(
    sessions: List<com.gymlog.app.data.model.WorkoutSession>,
    personalBests: List<PersonalBest>,
    onBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("训练历史") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 个人最佳
            if (personalBests.isNotEmpty()) {
                androidx.compose.material3.Text(
                    text = "个人最佳",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                )
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    items(personalBests) { pb ->
                        androidx.compose.material3.Card {
                            androidx.compose.foundation.layout.Row(
                                modifier = androidx.compose.ui.Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                androidx.compose.material3.Text(pb.exerciseName)
                                androidx.compose.material3.Text(
                                    text = "${pb.maxWeight}kg x ${pb.maxReps}",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
