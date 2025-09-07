package dev.hyo.martie.screens.uis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.hyo.martie.models.AppColors
import dev.hyo.openiap.models.OpenIapProduct
import dev.hyo.openiap.models.OpenIapPurchase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductDetailModal(
    product: OpenIapProduct,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    isPurchasing: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Product Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.textSecondary
                        )
                    }
                }
                
                Divider()
                
                // Product Info
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Title & Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            product.displayPrice,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary
                        )
                    }
                    
                    // Description
                    if (product.description.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.surfaceVariant
                            )
                        ) {
                            Text(
                                product.description,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Product Type Badge
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (product.type) {
                                OpenIapProduct.ProductType.SUBS -> AppColors.secondary
                                else -> AppColors.primary
                            }.copy(alpha = 0.2f)
                        ) {
                            Text(
                                product.type.value.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when (product.type) {
                                    OpenIapProduct.ProductType.SUBS -> AppColors.secondary
                                    else -> AppColors.primary
                                }
                            )
                        }
                        
                        if (product.id.contains("consumable", ignoreCase = true)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.success.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "CONSUMABLE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.success
                                )
                            }
                        }
                    }
                    
                    // Technical Details
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Product ID", product.id)
                        DetailRow("Currency", product.currency)
                        product.price?.let {
                            DetailRow("Raw Price", it.toString())
                        }
                        DetailRow("Platform", product.platform)
                        
                        // Android specific details
                        product.nameAndroid?.let {
                            DetailRow("Android Name", it)
                        }
                        
                        product.oneTimePurchaseOfferDetailsAndroid?.let { offer ->
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                "One-Time Purchase Details",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            DetailRow("Formatted Price", offer.formattedPrice)
                            DetailRow("Price (micros)", offer.priceAmountMicros)
                        }
                        
                        product.subscriptionOfferDetailsAndroid?.let { offers ->
                            if (offers.isNotEmpty()) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    "Subscription Offers",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                offers.forEach { offer ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = AppColors.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            DetailRow("Base Plan", offer.basePlanId)
                                            offer.offerId?.let {
                                                DetailRow("Offer ID", it)
                                            }
                                            DetailRow("Offer Token", offer.offerToken)
                                            if (offer.offerTags.isNotEmpty()) {
                                                DetailRow("Tags", offer.offerTags.joinToString(", "))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    
                    Button(
                        onClick = onPurchase,
                        modifier = Modifier.weight(1f),
                        enabled = !isPurchasing
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Purchase")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseDetailModal(
    purchase: OpenIapPurchase,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Purchase Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.textSecondary
                        )
                    }
                }
                
                Divider()
                
                // Purchase Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (purchase.purchaseState) {
                            OpenIapPurchase.PurchaseState.PURCHASED -> Icons.Default.CheckCircle
                            OpenIapPurchase.PurchaseState.PENDING -> Icons.Default.Schedule
                            OpenIapPurchase.PurchaseState.FAILED -> Icons.Default.Error
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (purchase.purchaseState) {
                            OpenIapPurchase.PurchaseState.PURCHASED -> AppColors.success
                            OpenIapPurchase.PurchaseState.PENDING -> AppColors.warning
                            OpenIapPurchase.PurchaseState.FAILED -> AppColors.danger
                            else -> AppColors.info
                        },
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column {
                        Text(
                            purchase.productId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Status: ${purchase.purchaseState.value}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary
                        )
                    }
                }
                
                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (purchase.isAutoRenewing) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.success.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "AUTO-RENEWING",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.success
                            )
                        }
                    }
                    
                    if (purchase.isAcknowledgedAndroid == true) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.info.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "ACKNOWLEDGED",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.info
                            )
                        }
                    }
                }
                
                // Details
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Transaction ID", purchase.id)
                    DetailRow("Product ID", purchase.productId)
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    DetailRow("Purchase Date", dateFormat.format(Date(purchase.transactionDate)))
                    
                    DetailRow("Quantity", purchase.quantity.toString())
                    DetailRow("Platform", purchase.platform)
                    
                    purchase.ids?.let { ids ->
                        if (ids.isNotEmpty()) {
                            DetailRow("Product IDs", ids.joinToString(", "))
                        }
                    }
                    
                    // Android specific details
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Android Details",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    purchase.packageNameAndroid?.let {
                        DetailRow("Package Name", it)
                    }
                    
                    purchase.autoRenewingAndroid?.let {
                        DetailRow("Auto Renewing", it.toString())
                    }
                    
                    purchase.obfuscatedAccountIdAndroid?.let {
                        DetailRow("Account ID", it)
                    }
                    
                    purchase.obfuscatedProfileIdAndroid?.let {
                        DetailRow("Profile ID", it)
                    }
                    
                    // Token info
                    if (!purchase.purchaseToken.isNullOrEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "Token Information",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.surfaceVariant
                            )
                        ) {
                            Text(
                                purchase.purchaseToken!!.take(50) + "...",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}