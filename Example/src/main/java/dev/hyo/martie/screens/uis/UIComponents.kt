package dev.hyo.martie.screens.uis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hyo.martie.models.AppColors
import dev.hyo.openiap.models.OpenIapPurchase

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector = Icons.Default.Info
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.textSecondary
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
fun SectionHeaderView(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ActivePurchaseCard(
    purchase: OpenIapPurchase,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.success.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    purchase.productId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    "State: ${purchase.purchaseState.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                
                if (purchase.isAutoRenewing) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AppColors.success.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "AUTO-RENEWING",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.success
                        )
                    }
                }
            }
            
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.success,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PurchaseResultCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.success.copy(alpha = 0.1f)
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
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.success,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.success
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = AppColors.textSecondary
                )
            }
        }
    }
}

@Composable
fun InstructionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.info.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = AppColors.info,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Testing Instructions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InstructionRow("1", "Configure products in Google Play Console")
                InstructionRow("2", "Upload app to internal testing track")
                InstructionRow("3", "Add test accounts in Play Console")
                InstructionRow("4", "Sign in with test account on device")
                InstructionRow("5", "Enable license testing for the account")
            }
        }
    }
}

@Composable
private fun InstructionRow(
    number: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = AppColors.info.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.info
                )
            }
        }
        
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}