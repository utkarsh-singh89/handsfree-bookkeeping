package com.root2rise.bookkeeping.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.R
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.TransactionOptionsDialog
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel
import com.root2rise.bookkeeping.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BookkeepingViewModel,
    onStartVoiceInput: () -> Unit,
    onStartModificationVoiceInput: (TransactionEntity) -> Unit,
    speaker: (String) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Determine if recording based on UI state
    val isRecording = uiState is UiState.Processing

    Scaffold(
        containerColor = Color(0xFF0A1628), // Dark background color
        floatingActionButton = {
            MicFab(
                isRecording = isRecording,
                onClick = onStartVoiceInput
            )
        },
        floatingActionButtonPosition = FabPosition.End, // Move FAB to bottom right
        content = { innerPadding ->
            HomeContent(
                modifier = Modifier.padding(innerPadding),
                transactions = transactions,
                lastResponse = lastResponse,
                uiState = uiState,
                viewModel = viewModel,
                onTransactionClick = { selectedTransaction = it }
            )
        }
    )

    // Transaction Options Dialog
    selectedTransaction?.let { transaction ->
        TransactionOptionsDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction = null },
            onReadAloud = {
                viewModel.processTransactionUpdate(transaction, "read aloud") { response ->
                    speaker(response)
                }
                selectedTransaction = null
            },
            onDelete = {
                viewModel.processTransactionUpdate(transaction, "delete") { response ->
                    speaker(response)
                }
                selectedTransaction = null
            },
            onModify = {
                onStartModificationVoiceInput(transaction)
                selectedTransaction = null
            }
        )
    }
}

@Composable
private fun MicFab(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    // Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f, // Scale up if recording
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Wave Effect (Optional: simplified as a larger transparent circle behind)
    if (isRecording) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale * 1.5f
                    scaleY = scale * 1.5f
                    alpha = 0.3f
                }
                .background(Color(0xFF4A90E2), CircleShape)
        )
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .padding(bottom = 16.dp, end = 16.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = Color(0xFF4A90E2).copy(alpha = 0.8f)
            ),
        containerColor = Color(0xFF4A90E2),
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record transaction",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    lastResponse: String,
    uiState: UiState,
    viewModel: BookkeepingViewModel,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // Top Bar with Logo and Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 0.dp), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Center vertically to align everything on one centerline
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Align logo and text vertically centered
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Maintain 12-16px spacing
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "ShriLekhan Logo",
                        modifier = Modifier.size(80.dp) // Slightly reduced from 100 to fit "one unified row" better if needed, but keeping large as requested previously. Let's stick to 80-100. User asked for 3x, let's keep it large but aligned.
                    )

                    Column(
                        verticalArrangement = Arrangement.Center
                        // Removed top padding to align center-to-center with logo
                    ) {
                        Text(
                            text = "Shrilekhan",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Your Financial Assistant",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Notification and Menu Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                    // Removed top padding to keep them aligned
                ) {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { /* TODO: Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Split Balance Cards (Income & Expense)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val income = transactions.filter { it.direction == "in" }.sumOf { it.amount }
                val expenses = transactions.filter { it.direction == "out" }.sumOf { it.amount }

                // Income Card
                MiniBalanceCard(
                    title = "Total Income",
                    amount = income,
                    icon = Icons.Default.TrendingUp,
                    iconColor = Color(0xFF4ECCA3),
                    modifier = Modifier.weight(1f)
                )

                // Expense Card
                MiniBalanceCard(
                    title = "Total Expenses",
                    amount = expenses,
                    icon = Icons.Default.TrendingDown,
                    iconColor = Color(0xFFEE6C4D),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Balance Card - "Current Balance"
        item {
            MainBalanceCard(transactions)
        }

        // Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.TrendingUp,
                        label = "Sales",
                        iconColor = Color(0xFF4ECCA3),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.processVoiceInput("Show today's sales")
                    }

                    QuickActionCard(
                        icon = Icons.Default.TrendingDown,
                        label = "Expenses",
                        iconColor = Color(0xFFEE6C4D),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.processVoiceInput("Show today's expenses")
                    }

                    QuickActionCard(
                        icon = Icons.Default.Assessment,
                        label = "Summary",
                        iconColor = Color(0xFF4A90E2),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.processVoiceInput("Show overall summary")
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Text(
                text = "Recent Transactions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No transactions yet",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(transactions.take(10), key = { it.id }) { transaction ->
                TransactionCardNew(transaction, onTransactionClick)
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun MiniBalanceCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp), 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2746) 
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.0f", amount)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MainBalanceCard(transactions: List<TransactionEntity>) {
    val income = transactions.filter { it.direction == "in" }.sumOf { it.amount }
    val expenses = transactions.filter { it.direction == "out" }.sumOf { it.amount }
    val balance = income - expenses

    // Gradient blue card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A90E2), // Lighter blue top
                            Color(0xFF3B5998)  // Darker blue bottom
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Current Balance",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.2f", balance)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "↑ ₹${String.format(Locale.getDefault(), "%,.0f", income)}",
                        fontSize = 14.sp,
                        color = Color(0xFF4ECCA3).copy(alpha = 0.9f), // Brighter green
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "↓ ₹${String.format(Locale.getDefault(), "%,.0f", expenses)}",
                        fontSize = 14.sp,
                        color = Color(0xFFEE6C4D).copy(alpha = 0.9f), // Brighter red
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp) // Slightly reduced height
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2746)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TransactionCardNew(
    transaction: TransactionEntity,
    onClick: (TransactionEntity) -> Unit
) {
    val isIncome = transaction.direction == "in"
    val amountColor = if (isIncome) Color(0xFF4ECCA3) else Color(0xFFEE6C4D)
    
    val (icon, iconBg) = when (transaction.type) {
        "sale" -> Icons.Default.ShoppingCart to Color(0xFF4ECCA3)
        "purchase" -> Icons.Default.ShoppingBag to Color(0xFFEE6C4D)
        "loan_given" -> Icons.Default.ArrowUpward to Color(0xFF4A90E2)
        "loan_taken" -> Icons.Default.ArrowDownward to Color(0xFFFFB84D)
        "expense" -> Icons.Default.LocalAtm to Color(0xFFEE6C4D)
        else -> Icons.Default.Receipt to Color(0xFF8B8B8B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2746)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBg.copy(alpha = 0.2f), CircleShape), // Transparent bg for icon
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.type.replace("_", " ").capitalize(),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Added Notes/Description
                    if (!transaction.notes.isNullOrBlank()) {
                        Text(
                            text = transaction.notes,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    
                    Text(
                        text = formatDate(transaction.date),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = "${if (isIncome) "+" else "-"}₹${transaction.amount.toInt()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

private fun formatDate(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date)
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        parsedDate?.let { outputFormat.format(it) } ?: date
    } catch (e: Exception) {
        date
    }
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
