package com.root2rise.bookkeeping.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.R
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.*
import com.root2rise.bookkeeping.ui.theme.*
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel
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
    var showMenu by remember { mutableStateOf(false) }

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
            // Top Bar with Logo
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Shri Lekhan Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Column {
                            Text(
                                text = "ShreeLekhan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Your Financial Assistant",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Total Income",
                            value = calculateTotalIncome(transactions),
                            icon = Icons.Filled.TrendingUp,
                            iconColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total Expenses",
                            value = calculateTotalExpenses(transactions),
                            icon = Icons.Filled.TrendingDown,
                            iconColor = ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Balance Card
                item {
                    BalanceCard(transactions)
                }

                // Quick Actions
                item {
                    QuickActionsSection(viewModel)
                }

                // Response Card
                if (lastResponse.isNotEmpty()) {
                    item {
                        ResponseCard(lastResponse, uiState)
                    }
                }

                // Recent Transactions
                item {
                    SectionTitle(text = "Recent Transactions")
                }

                if (transactions.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.Receipt,
                            title = "No transactions yet",
                            subtitle = "Tap the mic button to add your first transaction"
                        )
                    }
                } else {
                    items(transactions.take(10), key = { it.id }) { transaction ->
                        DashboardTransactionCard(transaction) {
                            selectedTransaction = it
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Floating Mic Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            PulsingMicButton(
                onClick = onStartVoiceInput,
                isListening = false
            )
        }
    }

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

    // Dropdown Menu
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = { /* TODO: Navigate to settings */ }
        )
        DropdownMenuItem(
            text = { Text("Export Data") },
            onClick = { /* TODO: Export data */ }
        )
        DropdownMenuItem(
            text = { Text("Help") },
            onClick = { /* TODO: Show help */ }
        )
    }
}

@Composable
private fun BalanceCard(transactions: List<TransactionEntity>) {
    val income = transactions.filter { it.direction == "in" }.sumOf { it.amount }
    val expenses = transactions.filter { it.direction == "out" }.sumOf { it.amount }
    val balance = income - expenses

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(NeonBlueDark, NeonBlue, NeonBlueLight)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Current Balance",
                    fontSize = 14.sp,
                    color = TextPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = "₹${String.format("%,.2f", balance)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "↑ ₹${String.format("%,.0f", income)}",
                        fontSize = 14.sp,
                        color = SuccessGreen
                    )
                    Text(
                        text = "↓ ₹${String.format("%,.0f", expenses)}",
                        fontSize = 14.sp,
                        color = ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(viewModel: BookkeepingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(text = "Quick Actions")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Filled.TrendingUp,
                label = "Sales",
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.processVoiceInput("Show today's sales")
            }
            QuickActionButton(
                icon = Icons.Filled.TrendingDown,
                label = "Expenses",
                color = ErrorRed,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.processVoiceInput("Show today's expenses")
            }
            QuickActionButton(
                icon = Icons.Filled.Assessment,
                label = "Summary",
                color = NeonCyan,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.processVoiceInput("Show overall summary")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
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
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DashboardTransactionCard(
    transaction: TransactionEntity,
    onClick: (TransactionEntity) -> Unit
) {
    val isIncome = transaction.direction == "in"
    val amountColor = if (isIncome) SuccessGreen else ErrorRed
    val icon = when (transaction.type) {
        "sale" -> Icons.Filled.ShoppingCart
        "purchase" -> Icons.Filled.ShoppingBag
        "loan_given" -> Icons.Filled.ArrowUpward
        "loan_taken" -> Icons.Filled.ArrowDownward
        "expense" -> Icons.Filled.Payment
        else -> Icons.Filled.Receipt
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(amountColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.type.replace("_", " ").capitalize(),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    transaction.partyName?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = formatDate(transaction.date),
                        fontSize = 12.sp,
                        color = TextTertiary
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

private fun calculateTotalIncome(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.direction == "in" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
}

private fun calculateTotalExpenses(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.direction == "out" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
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
