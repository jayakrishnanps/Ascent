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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
    var selectedFilter by remember { mutableStateOf("Active") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AscentTopAppBar(
                onMenuClick = onMenuClick, 
                level = uiState.userLevel,
                xpProgress = uiState.userXpProgress,
                photoUrl = authProfile?.photoUrl,
                onFilterChange = { selectedFilter = it }
            )
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
                if (selectedFilter == "Active") {
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
                } else {
                    item {
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
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun AscentTopAppBar(onMenuClick: () -> Unit, level: Int, xpProgress: Float, photoUrl: String? = null, onFilterChange: (String) -> Unit = {}) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 48.dp)
            ) {
                Text(
                    text = "ASCENT",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.FlashOn, contentDescription = "Quick Action", tint = MaterialTheme.colorScheme.primary)
            }
            
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                var menuExpanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Tasks") },
                        onClick = {
                            onFilterChange("Active")
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("View Future Tasks") },
                        onClick = {
                            onFilterChange("Future")
                            menuExpanded = false
                        }
                    )
                }
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
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
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
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${subtasks.count { it.isCompleted }}/${subtasks.size} Subtasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse subtasks" else "Expand subtasks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = expanded) {
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
            }
            
            val context = LocalContext.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (allSubtasksCompleted) {
                            onCompleteClick()
                        } else {
                            android.widget.Toast.makeText(context, "Complete all subtasks first.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
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
