package com.root2rise.bookkeeping.ui.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.*
import com.root2rise.bookkeeping.ui.theme.*
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: BookkeepingViewModel,
    onStartModificationVoiceInput: (TransactionEntity) -> Unit,
    speaker: (String) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var filterType by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredTransactions = if (filterType != null) {
        transactions.filter { it.type == filterType }
    } else {
        transactions
    }

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
                        text = "Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonBlue,
                        selectedLabelColor = TextPrimary,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = filterType == "sale",
                    onClick = { filterType = "sale" },
                    label = { Text("Sales") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessGreen,
                        selectedLabelColor = TextPrimary,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    )
                )
                FilterChip(
                    selected = filterType == "expense",
                    onClick = { filterType = "expense" },
                    label = { Text("Expenses") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ErrorRed,
                        selectedLabelColor = TextPrimary,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    )
                )
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Receipt,
                    title = if (filterType != null) "No $filterType transactions" else "No transactions yet",
                    subtitle = "Add a transaction to get started"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Group by date
                    val groupedTransactions = filteredTransactions.groupBy { it.date }

                    groupedTransactions.forEach { (date, txns) ->
                        item {
                            Text(
                                text = formatDateHeader(date),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(txns, key = { it.id }) { transaction ->
                            TransactionListItem(transaction) {
                                selectedTransaction = it
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Filter Menu
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Transactions") },
                onClick = {
                    filterType = null
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sales") },
                onClick = {
                    filterType = "sale"
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Purchases") },
                onClick = {
                    filterType = "purchase"
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Expenses") },
                onClick = {
                    filterType = "expense"
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Loans Given") },
                onClick = {
                    filterType = "loan_given"
                    showFilterMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Loans Taken") },
                onClick = {
                    filterType = "loan_taken"
                    showFilterMenu = false
                }
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
}

@Composable
private fun TransactionListItem(
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
                    transaction.notes?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = TextTertiary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}₹${transaction.amount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                Text(
                    text = formatTime(transaction.date),
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

private fun formatDateHeader(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date)
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val txnDate = Calendar.getInstance().apply { parsedDate?.let { time = it } }

        when {
            isSameDay(today, txnDate) -> "Today"
            isSameDay(yesterday, txnDate) -> "Yesterday"
            else -> {
                val outputFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                parsedDate?.let { outputFormat.format(it) } ?: date
            }
        }
    } catch (e: Exception) {
        date
    }
}

private fun formatTime(date: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = sdf.parse(date)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        parsedDate?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
