package com.gymlog.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymlog.app.data.local.GymLogDatabase
import com.gymlog.app.data.model.WorkoutDay
import com.gymlog.app.data.repository.WorkoutRepository
import com.gymlog.app.ui.theme.GymLogTheme
import kotlinx.coroutines.runBlocking

/**
 * 应用主导航
 */
@OptIn(ExperimentalMaterial3Api::class)
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

/**
 * 训练计划详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan.name) },
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
        ) {
            // 计划描述
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 训练日列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDayCard(
    workoutDay: WorkoutDay,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = workoutDay.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${workoutDay.exercises.size} 个动作 · 预计 ${workoutDay.estimatedDuration} 分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始训练")
            }
        }
    }
}

/**
 * 训练历史屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    sessions: List<com.gymlog.app.data.model.WorkoutSession>,
    personalBests: List<PersonalBest>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练历史") },
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
        ) {
            // 个人最佳
            if (personalBests.isNotEmpty()) {
                Text(
                    text = "个人最佳",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(personalBests) { pb ->
                        Card {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pb.exerciseName)
                                Text(
                                    text = "${pb.maxWeight}kg x ${pb.maxReps}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
