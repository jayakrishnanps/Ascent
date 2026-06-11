package com.yourapp.productivity.ui.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourapp.productivity.data.local.database.entities.AchievementConditionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAchievementScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddEditAchievementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var taskDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Achievement") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Achievement Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )


            ExposedDropdownMenuBox(
                expanded = taskDropdownExpanded,
                onExpandedChange = { taskDropdownExpanded = it }
            ) {
                val selectedTask = uiState.availableTasks.find { it.id == uiState.selectedTaskId }
                OutlinedTextField(
                    value = selectedTask?.title ?: "Select a task",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Task") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = taskDropdownExpanded,
                    onDismissRequest = { taskDropdownExpanded = false }
                ) {
                    uiState.availableTasks.forEach { task ->
                        DropdownMenuItem(
                            text = { Text(task.title) },
                            onClick = {
                                viewModel.updateSelectedTask(task.id)
                                taskDropdownExpanded = false
                            }
                        )
                    }
                }
            }


            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (uiState.conditionType == AchievementConditionType.COMPLETE_N_TIMES) "Complete N times" else "Complete until end date",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Condition Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Complete N times") },
                        onClick = {
                            viewModel.updateConditionType(AchievementConditionType.COMPLETE_N_TIMES)
                            typeDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Complete until end date") },
                        onClick = {
                            viewModel.updateConditionType(AchievementConditionType.COMPLETE_UNTIL_END_DATE)
                            typeDropdownExpanded = false
                        }
                    )
                }
            }

            if (uiState.conditionType == AchievementConditionType.COMPLETE_N_TIMES) {
                OutlinedTextField(
                    value = uiState.targetValue,
                    onValueChange = { viewModel.updateTargetValue(it) },
                    label = { Text("Number of completions required") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text(
                    text = "This achievement will unlock when you successfully complete the recurring task series up to its end date. Note: The task MUST have an end date set for this to work.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveAchievement(onSuccess = onSaveSuccess) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Achievement")
                }
            }
        }
    }
}
