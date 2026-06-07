package com.yourapp.productivity.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourapp.productivity.domain.model.Difficulty
import com.yourapp.productivity.domain.model.RecurrenceType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    onNavigateBack: () -> Unit,
    taskId: String?,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onNavigateBack()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTask()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Add Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveTask() }) {
                Text(modifier = Modifier.padding(horizontal = 16.dp), text = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Text("Difficulty", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Difficulty.values().forEach { difficulty ->
                    FilterChip(
                        selected = uiState.difficulty == difficulty,
                        onClick = { viewModel.updateDifficulty(difficulty) },
                        label = { Text(difficulty.name) }
                    )
                }
            }

            Text("Recurrence", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RecurrenceType.values().forEach { type ->
                    FilterChip(
                        selected = uiState.recurrenceType == type,
                        onClick = { viewModel.updateRecurrenceType(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            if (uiState.recurrenceType == RecurrenceType.WEEKLY) {
                Text("Select Days", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, dayName ->
                        FilterChip(
                            selected = uiState.weeklyDays.contains(index),
                            onClick = { viewModel.toggleWeeklyDay(index) },
                            label = { Text(dayName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Subtasks", style = MaterialTheme.typography.titleMedium)
            uiState.subtasks.forEach { subtask ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = subtask.title, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeSubtask(subtask) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove subtask", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.newSubtaskTitle,
                    onValueChange = { viewModel.updateNewSubtaskTitle(it) },
                    label = { Text("New Subtask") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.addSubtask() },
                    enabled = uiState.newSubtaskTitle.isNotBlank()
                ) {
                    Text("Add")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Due Date: ${formatDate(uiState.dueDate)}", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { viewModel.updateDueDate(System.currentTimeMillis()) }) {
                Text("Set Due Date to Today")
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Not set"
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
