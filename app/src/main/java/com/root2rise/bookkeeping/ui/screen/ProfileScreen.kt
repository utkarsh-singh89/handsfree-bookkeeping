package com.root2rise.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.ui.components.*
import com.root2rise.bookkeeping.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, DarkBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit profile */ }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Profile Header
                item {
                    ProfileHeaderCard()
                }

                // Account Section
                item {
                    SectionTitle(text = "Account")
                }

                item {
                    NeonCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProfileMenuItem(
                                icon = Icons.Filled.Person,
                                title = "Personal Information",
                                subtitle = "Update your details"
                            ) {
                                // TODO: Navigate to personal info
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Lock,
                                title = "Change Password",
                                subtitle = "Update your password"
                            ) {
                                // TODO: Navigate to change password
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Security,
                                title = "Security",
                                subtitle = "Manage security settings"
                            ) {
                                // TODO: Navigate to security
                            }
                        }
                    }
                }

                // Settings Section
                item {
                    SectionTitle(text = "Settings")
                }

                item {
                    NeonCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProfileMenuItem(
                                icon = Icons.Filled.Notifications,
                                title = "Notifications",
                                subtitle = "Manage notification preferences"
                            ) {
                                // TODO: Navigate to notifications
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Language,
                                title = "Language",
                                subtitle = "English"
                            ) {
                                // TODO: Navigate to language
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.DarkMode,
                                title = "Theme",
                                subtitle = "Dark Mode"
                            ) {
                                // TODO: Navigate to theme
                            }
                        }
                    }
                }

                // Data & Privacy Section
                item {
                    SectionTitle(text = "Data & Privacy")
                }

                item {
                    NeonCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProfileMenuItem(
                                icon = Icons.Filled.Download,
                                title = "Export Data",
                                subtitle = "Download your data"
                            ) {
                                // TODO: Export data
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Backup,
                                title = "Backup & Restore",
                                subtitle = "Manage your backups"
                            ) {
                                // TODO: Navigate to backup
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.PrivacyTip,
                                title = "Privacy Policy",
                                subtitle = "View privacy policy"
                            ) {
                                // TODO: Show privacy policy
                            }
                        }
                    }
                }

                // Support Section
                item {
                    SectionTitle(text = "Support")
                }

                item {
                    NeonCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ProfileMenuItem(
                                icon = Icons.Filled.Help,
                                title = "Help & Support",
                                subtitle = "Get help with the app"
                            ) {
                                // TODO: Navigate to help
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Feedback,
                                title = "Send Feedback",
                                subtitle = "Share your thoughts"
                            ) {
                                // TODO: Open feedback
                            }
                            Divider(color = DarkSurface)
                            ProfileMenuItem(
                                icon = Icons.Filled.Info,
                                title = "About",
                                subtitle = "Version 1.0.0"
                            ) {
                                // TODO: Show about
                            }
                        }
                    }
                }

                // Logout Button
                item {
                    GradientButton(
                        text = "Logout",
                        onClick = { showLogoutDialog = true }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = DarkCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

@Composable
private fun ProfileHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(NeonBlueDark, NeonBlue, AccentPurple)
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TextPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(48.dp),
                        tint = TextPrimary
                    )
                }

                // User Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "John Doe",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "john.doe@example.com",
                        fontSize = 14.sp,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "+91 98765 43210",
                        fontSize = 14.sp,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(24.dp)
        )
    }
}
