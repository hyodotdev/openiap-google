package dev.hyo.martie.screens.uis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.hyo.martie.models.AppColors
import dev.hyo.openiap.models.OpenIapProduct

@Composable
fun ProductCard(
    product: OpenIapProduct,
    isPurchasing: Boolean = false,
    onPurchase: () -> Unit,
    onClick: () -> Unit = {},
    onDetails: () -> Unit = onClick,
    isSubscribed: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                // Prefer a cleaner title (avoid app suffix in parentheses if present)
                val mainTitle = product.displayName ?: product.title.substringBefore(" (")
                Text(
                    mainTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (product.type) {
                            OpenIapProduct.ProductType.Subs -> AppColors.secondary
                            else -> AppColors.primary
                        }.copy(alpha = 0.2f)
                    ) {
                        Text(
                            product.type.value.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (product.type) {
                                OpenIapProduct.ProductType.Subs -> AppColors.secondary
                                else -> AppColors.primary
                            }
                        )
                    }
                    
                    if (isSubscribed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.success.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "SUBSCRIBED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.success
                            )
                        }
                    }
                }

                // Show product SKU below badges, allow wrapping to avoid ugly cuts
                Text(
                    product.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    product.displayPrice,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primary
                )

                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDetails,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Details")
                        }
                        Button(
                            onClick = onPurchase,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Buy")
                        }
                    }
                }
            }
        }
    }
}
