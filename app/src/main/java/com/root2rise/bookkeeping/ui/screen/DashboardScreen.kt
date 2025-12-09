package com.root2rise.bookkeeping.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.root2rise.bookkeeping.R
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.*
import com.root2rise.bookkeeping.ui.theme.*
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel
import com.root2rise.bookkeeping.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = Color.Transparent,
        floatingActionButton = {
            MicFab(
                isRecording = isRecording,
                onClick = onStartVoiceInput
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
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
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 0.5f else 1.0f,
        label = "mic-scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
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
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Record transaction",
            modifier = Modifier.size(32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    lastResponse: String,
    uiState: UiState,
    viewModel: BookkeepingViewModel,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A1628))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Top Bar with Logo and Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "ShriLekhan Logo",
                            modifier = Modifier.size(80.dp)
                        )

                        Column {
                            Text(
                                text = "ShriLekhan",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Your Financial Assistant",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Menu Icon
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

            // Current Balance Card
            item {
                BalanceCardNew(transactions)
            }

            // Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 22.sp,
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

            // Response Card (if present)
            if (lastResponse.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState is UiState.Success)
                                    Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (uiState is UiState.Success)
                                    Color(0xFF4ECCA3) else Color(0xFF4A90E2),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = lastResponse,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Text(
                    text = "Recent Transactions",
                    fontSize = 22.sp,
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
}

@Composable
private fun BalanceCardNew(transactions: List<TransactionEntity>) {
    val income = transactions.filter { it.direction == "in" }.sumOf { it.amount }
    val expenses = transactions.filter { it.direction == "out" }.sumOf { it.amount }
    val balance = income - expenses

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3B5998),
                            Color(0xFF4A90E2),
                            Color(0xFF5BA3F5)
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = "₹${String.format("%,.2f", balance)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFF4ECCA3),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "₹${String.format("%,.0f", income)}",
                            fontSize = 16.sp,
                            color = Color(0xFF4ECCA3),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color(0xFFEE6C4D),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "₹${String.format("%,.0f", expenses)}",
                            fontSize = 16.sp,
                            color = Color(0xFFEE6C4D),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White,
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
            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.4f)
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(iconBg.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.type.replace("_", " ").capitalize(),
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    transaction.partyName?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    transaction.notes?.let { notes ->
                        if (notes.isNotBlank()) {
                            Text(
                                text = notes,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                        }
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
                fontSize = 20.sp,
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
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
