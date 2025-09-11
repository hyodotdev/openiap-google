package dev.hyo.martie.screens.uis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hyo.martie.models.AppColors
import dev.hyo.openiap.models.OpenIapPurchase

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
