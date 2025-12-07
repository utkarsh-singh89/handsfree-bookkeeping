package com.root2rise.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.ui.navigation.BottomNavItem
import com.root2rise.bookkeeping.ui.theme.*

@Composable
fun AppBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Transparent)
    ) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = NeonBlue.copy(alpha = 0.3f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkCard.copy(alpha = 0.95f),
                            DarkSurface.copy(alpha = 0.98f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    selected = currentRoute == com.root2rise.bookkeeping.ui.navigation.Screen.Home.route,
                    onClick = { onNavigate(com.root2rise.bookkeeping.ui.navigation.Screen.Home.route) }
                )

                BottomNavItem(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = "Wallet",
                    selected = currentRoute == com.root2rise.bookkeeping.ui.navigation.Screen.Wallet.route,
                    onClick = { onNavigate(com.root2rise.bookkeeping.ui.navigation.Screen.Wallet.route) }
                )

                BottomNavItem(
                    icon = Icons.Filled.Receipt,
                    label = "Transactions",
                    selected = currentRoute == com.root2rise.bookkeeping.ui.navigation.Screen.Transactions.route,
                    onClick = { onNavigate(com.root2rise.bookkeeping.ui.navigation.Screen.Transactions.route) }
                )

                BottomNavItem(
                    icon = Icons.Filled.Person,
                    label = "Profile",
                    selected = currentRoute == com.root2rise.bookkeeping.ui.navigation.Screen.Profile.route,
                    onClick = { onNavigate(com.root2rise.bookkeeping.ui.navigation.Screen.Profile.route) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(70.dp)
            .height(60.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (selected) 48.dp else 40.dp)
                    .background(
                        if (selected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonBlue.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) NeonBlue else TextTertiary,
                    modifier = Modifier.size(if (selected) 28.dp else 24.dp)
                )
            }
        }

        if (selected) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonBlue
            )
        }
    }
}
