package com.root2rise.bookkeeping.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.*
import com.root2rise.bookkeeping.ui.theme.*
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: BookkeepingViewModel
) {
    val transactions by viewModel.transactions.collectAsState()

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
                        text = "Wallet & Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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

                // Main Balance Card
                item {
                    WalletBalanceCard(transactions)
                }

                // Bar Chart
                item {
                    SectionTitle(text = "Monthly Overview")
                }

                item {
                    MonthlyBarChart(transactions)
                }

                // Monthly Stats
                item {
                    MonthlyStatsCard(transactions)
                }

                // Category Breakdown
                item {
                    SectionTitle(text = "Category Breakdown")
                }

                item {
                    CategoryBreakdownCard(transactions)
                }

                // Insights
                item {
                    SectionTitle(text = "Financial Insights")
                }

                item {
                    InsightsCard(transactions)
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(transactions: List<TransactionEntity>) {
    val chartData = listOf(
        BarChartData(
            label = "Sales",
            value = transactions.filter { it.type == "sale" }.sumOf { it.amount }.toFloat(),
            color = SuccessGreen
        ),
        BarChartData(
            label = "Purchase",
            value = transactions.filter { it.type == "purchase" }.sumOf { it.amount }.toFloat(),
            color = WarningYellow
        ),
        BarChartData(
            label = "Expense",
            value = transactions.filter { it.type == "expense" }.sumOf { it.amount }.toFloat(),
            color = ErrorRed
        ),
        BarChartData(
            label = "Loan Given",
            value = transactions.filter { it.type == "loan_given" }.sumOf { it.amount }.toFloat(),
            color = NeonCyan
        ),
        BarChartData(
            label = "Loan Taken",
            value = transactions.filter { it.type == "loan_taken" }.sumOf { it.amount }.toFloat(),
            color = AccentPurple
        )
    )

    BarChart(
        data = chartData,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun WalletBalanceCard(transactions: List<TransactionEntity>) {
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
                    Brush.linearGradient(
                        colors = listOf(
                            NeonBlueDark,
                            NeonBlue,
                            AccentPurple
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        fontSize = 16.sp,
                        color = TextPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TextPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "₹${String.format("%,.2f", balance)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Income",
                            fontSize = 12.sp,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "↑ ₹${String.format("%,.0f", income)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Expenses",
                            fontSize = 12.sp,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "↓ ₹${String.format("%,.0f", expenses)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatsCard(transactions: List<TransactionEntity>) {
    NeonCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "This Month",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Sales",
                    value = calculateSales(transactions),
                    icon = Icons.Filled.ShoppingCart,
                    iconColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Purchases",
                    value = calculatePurchases(transactions),
                    icon = Icons.Filled.ShoppingBag,
                    iconColor = WarningYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Loans Given",
                    value = calculateLoansGiven(transactions),
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Loans Taken",
                    value = calculateLoansTaken(transactions),
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(transactions: List<TransactionEntity>) {
    val categories = transactions.groupBy { it.type }

    NeonCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { (type, txns) ->
                val total = txns.sumOf { it.amount }
                CategoryRow(
                    category = type.replace("_", " ").capitalize(),
                    amount = total,
                    percentage = calculatePercentage(total, transactions)
                )
            }

            if (categories.isEmpty()) {
                Text(
                    text = "No data available",
                    fontSize = 14.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    amount: Double,
    percentage: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = "₹${String.format("%,.0f", amount)}",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DarkSurface, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(8.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonBlue, NeonCyan)
                        ),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun InsightsCard(transactions: List<TransactionEntity>) {
    NeonCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InsightRow(
                icon = Icons.Filled.TrendingUp,
                title = "Average Transaction",
                value = calculateAverageTransaction(transactions),
                iconColor = NeonBlue
            )

            Divider(color = DarkSurface)

            InsightRow(
                icon = Icons.Filled.Receipt,
                title = "Total Transactions",
                value = "${transactions.size}",
                iconColor = AccentPurple
            )

            Divider(color = DarkSurface)

            InsightRow(
                icon = Icons.Filled.CalendarToday,
                title = "Active Days",
                value = calculateActiveDays(transactions).toString(),
                iconColor = NeonCyan
            )
        }
    }
}

@Composable
private fun InsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

private fun calculateSales(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.type == "sale" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
}

private fun calculatePurchases(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.type == "purchase" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
}

private fun calculateLoansGiven(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.type == "loan_given" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
}

private fun calculateLoansTaken(transactions: List<TransactionEntity>): String {
    val total = transactions.filter { it.type == "loan_taken" }.sumOf { it.amount }
    return "₹${String.format("%,.0f", total)}"
}

private fun calculatePercentage(amount: Double, transactions: List<TransactionEntity>): Float {
    val total = transactions.sumOf { it.amount }
    return if (total > 0) (amount / total).toFloat() else 0f
}

private fun calculateAverageTransaction(transactions: List<TransactionEntity>): String {
    val avg = if (transactions.isNotEmpty()) {
        transactions.sumOf { it.amount } / transactions.size
    } else 0.0
    return "₹${String.format("%,.0f", avg)}"
}

private fun calculateActiveDays(transactions: List<TransactionEntity>): Int {
    return transactions.map { it.date }.distinct().size
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
