package com.yourapp.productivity.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourapp.productivity.data.local.database.entities.Task
import com.yourapp.productivity.domain.model.Difficulty

import coil.compose.AsyncImage
import com.yourapp.productivity.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authProfile by authViewModel.userProfile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AscentTopAppBar(onMenuClick = onMenuClick, photoUrl = authProfile?.photoUrl)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    val displayName = authProfile?.displayName ?: "Hero"
                    Text(
                        text = "Welcome back, $displayName!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    SectionHeader(title = "Active Quests", icon = Icons.Default.Whatshot, iconColor = MaterialTheme.colorScheme.primary)
                }

                if (uiState.todayTasks.isEmpty()) {
                    item {
                        EmptyStateMessage("No active quests. Time to rest!")
                    }
                } else {
                    items(uiState.todayTasks, key = { it.task.id }) { taskWithSubtasks ->
                        TaskItem(
                            taskWithSubtasks = taskWithSubtasks,
                            onClick = { onTaskClick(taskWithSubtasks.task.id) },
                            onCompleteClick = { viewModel.completeTask(taskWithSubtasks) },
                            onSubtaskToggle = { subtask -> viewModel.toggleSubtask(taskWithSubtasks.task, subtask) },
                            isFuture = false
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Future Trials", icon = Icons.Default.DateRange, iconColor = MaterialTheme.colorScheme.outlineVariant)
                }

                if (uiState.upcomingTasks.isEmpty()) {
                    item {
                        EmptyStateMessage("No future trials on your radar.")
                    }
                } else {
                    items(uiState.upcomingTasks, key = { it.task.id }) { taskWithSubtasks ->
                        TaskItem(
                            taskWithSubtasks = taskWithSubtasks,
                            onClick = { onTaskClick(taskWithSubtasks.task.id) },
                            onCompleteClick = { viewModel.completeTask(taskWithSubtasks) },
                            onSubtaskToggle = { subtask -> viewModel.toggleSubtask(taskWithSubtasks.task, subtask) },
                            isFuture = true
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun AscentTopAppBar(onMenuClick: () -> Unit, level: Int = 12, xpProgress: Float = 0.75f, photoUrl: String? = null) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onMenuClick)
                ) {
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.align(Alignment.Center), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Lvl $level", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceDim
                    )
                }
            }
            
            Text(
                text = "ASCENT",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            
            IconButton(
                onClick = { /* Quick Action */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, CircleShape)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Quick Action", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = if (iconColor == MaterialTheme.colorScheme.primary) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TaskItem(
    taskWithSubtasks: com.yourapp.productivity.domain.model.TaskWithSubtasks,
    onClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onSubtaskToggle: (com.yourapp.productivity.data.local.database.entities.Subtask) -> Unit,
    isFuture: Boolean
) {
    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val allSubtasksCompleted = subtasks.isEmpty() || subtasks.all { it.isCompleted }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFuture) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if(isFuture) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                DifficultyBadge(difficulty = task.difficulty, isFuture = isFuture)
                Box(
                    modifier = Modifier
                        .background(if (isFuture) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val totalSubtaskXp = subtasks.filter { it.isCompleted }.size * (task.difficulty.xpAward * 0.1).toInt()
                    val xpReward = task.difficulty.xpAward - totalSubtaskXp
                    Text(
                        text = "+$xpReward XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isFuture) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isFuture) FontWeight.Medium else FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!task.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFuture) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    subtasks.forEach { subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubtaskToggle(subtask) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { onSubtaskToggle(subtask) },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtask.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (subtask.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                ),
                                color = if (subtask.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCompleteClick,
                    enabled = allSubtasksCompleted,
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, if (allSubtasksCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = if (allSubtasksCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty, isFuture: Boolean) {
    val (bgColor, textColor, borderColor) = if (isFuture) {
        Triple(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant, Color.Transparent)
    } else {
        when (difficulty) {
            Difficulty.LOW -> Triple(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurfaceVariant, Color.Transparent)
            Difficulty.MEDIUM -> Triple(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            Difficulty.HIGH -> Triple(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            Difficulty.VERY_HIGH -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.error)
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(percent = 50),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
