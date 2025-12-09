package com.root2rise.bookkeeping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.theme.*

@Composable
fun TransactionOptionsDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onReadAloud: () -> Unit,
    onDelete: () -> Unit,
    onModify: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Transaction Options",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "₹${transaction.amount.toInt()} - ${transaction.type.replace("_", " ").uppercase()}",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OptionItem(
                    icon = Icons.Default.VolumeUp,
                    label = "Read Aloud",
                    color = NeonBlue,
                    onClick = onReadAloud
                )
                
                OptionItem(
                    icon = Icons.Default.Edit,
                    label = "Modify Transaction",
                    color = AccentPurple,
                    onClick = onModify
                )
                
                OptionItem(
                    icon = Icons.Default.Delete,
                    label = "Delete Transaction",
                    color = ErrorRed,
                    onClick = onDelete
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}
