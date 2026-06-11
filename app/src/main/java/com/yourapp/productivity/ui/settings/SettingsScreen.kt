package com.yourapp.productivity.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.yourapp.productivity.ui.auth.AuthViewModel
import com.yourapp.productivity.ui.components.AscentTopAppBar

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: com.yourapp.productivity.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authProfile by authViewModel.userProfile.collectAsState()
    var showDeleteAccountDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AscentTopAppBar(
                title = "SETTINGS",
                photoUrl = authProfile?.photoUrl,
                onMenuClick = onMenuClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Account Information", icon = Icons.Default.AccountCircle) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = authProfile?.displayName ?: "Hero User", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(text = authProfile?.email ?: uiState.userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SettingsSection(title = "Aesthetic Customization", icon = Icons.Default.Palette) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "App Theme Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ThemeColorOption(Color(0xFFAA73FF), isSelected = uiState.selectedThemeColor == "DARK", onClick = { viewModel.setTheme("DARK") }) // Violet
                        ThemeColorOption(Color(0xFF32D978), isSelected = uiState.selectedThemeColor == "GREEN", onClick = { viewModel.setTheme("GREEN") }) // Green
                        ThemeColorOption(Color(0xFFFF5449), isSelected = uiState.selectedThemeColor == "RED", onClick = { viewModel.setTheme("RED") }) // Red
                        ThemeColorOption(Color(0xFFFFC000), isSelected = uiState.selectedThemeColor == "YELLOW", onClick = { viewModel.setTheme("YELLOW") }) // Yellow
                        ThemeColorOption(Color(0xFF3A7BFF), isSelected = uiState.selectedThemeColor == "BLUE", onClick = { viewModel.setTheme("BLUE") }) // Blue
                    }
                }
            }

            SettingsSection(title = "General Settings", icon = Icons.Default.Notifications) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Push Notifications", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            SettingsSection(title = "Danger Zone", icon = Icons.Default.Delete, iconColor = MaterialTheme.colorScheme.error) {
                Button(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(text = "Delete Account", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to permanently delete your account? This will erase all your tasks, progress, and authentication data. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            profileViewModel.deleteAccount(
                                onSuccess = {
                                    showDeleteAccountDialog = false
                                    onSignOut()
                                },
                                onError = { e ->
                                    showDeleteAccountDialog = false
                                    e.printStackTrace()
                                }
                            )
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
        }
        content()
    }
}

@Composable
fun ThemeColorOption(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}
