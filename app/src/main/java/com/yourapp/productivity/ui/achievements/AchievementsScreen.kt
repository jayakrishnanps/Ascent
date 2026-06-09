package com.yourapp.productivity.ui.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourapp.productivity.data.local.database.entities.Achievement
import com.yourapp.productivity.data.local.database.entities.AchievementConditionType
import com.yourapp.productivity.ui.components.AscentTopAppBar
import com.yourapp.productivity.ui.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onMenuClick: () -> Unit,
    onAddAchievementClick: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authProfile by authViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            AscentTopAppBar(
                title = "ACHIEVEMENTS",
                photoUrl = authProfile?.photoUrl,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAchievementClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Achievement")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.achievements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No achievements created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.achievements, key = { it.id }) { achievement ->
                    AchievementItem(
                        achievement = achievement,
                        onDeleteClick = { viewModel.deleteAchievement(achievement) }
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement, onDeleteClick: () -> Unit) {
    val progressFraction = if (achievement.targetValue > 0) {
        (achievement.currentProgress.toFloat() / achievement.targetValue.toFloat()).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isEarned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, if (achievement.isEarned) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (achievement.isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (achievement.isEarned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = achievement.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (achievement.isEarned && achievement.earnedAt != null) {
                            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            Text(
                                text = "Earned on ${formatter.format(Date(achievement.earnedAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (achievement.conditionType == AchievementConditionType.COMPLETE_N_TIMES) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progress", style = MaterialTheme.typography.labelMedium)
                    Text("${achievement.currentProgress} / ${achievement.targetValue}", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else if (achievement.conditionType == AchievementConditionType.COMPLETE_UNTIL_END_DATE) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Condition", style = MaterialTheme.typography.labelMedium)
                    Text(if (achievement.isEarned) "Completed" else "Waiting for end date", style = MaterialTheme.typography.labelMedium, color = if (achievement.isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
