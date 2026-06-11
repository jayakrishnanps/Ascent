package com.yourapp.productivity.ui.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.graphics.Color
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

    val isEarned = achievement.isEarned
    
    val containerColor = if (isEarned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val iconColor = if (isEarned) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val iconBgColor = if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
    val textColor = if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isEarned) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEarned) 12.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isEarned) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.05f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(iconBgColor)
                                .border(
                                    if (isEarned) 2.dp else 1.dp, 
                                    if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f) else Color.Transparent, 
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEarned) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = if (isEarned) "Trophy" else "Locked",
                                tint = iconColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = achievement.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            if (isEarned && achievement.earnedAt != null) {
                                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                Text(
                                    text = "Unlocked: ${formatter.format(Date(achievement.earnedAt))}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete", 
                            tint = if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEarned) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                
                if (!isEarned && achievement.conditionType == AchievementConditionType.COMPLETE_N_TIMES) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${achievement.currentProgress} / ${achievement.targetValue}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                } else if (!isEarned && achievement.conditionType == AchievementConditionType.COMPLETE_UNTIL_END_DATE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Active Quest", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
