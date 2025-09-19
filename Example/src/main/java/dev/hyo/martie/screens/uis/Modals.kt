package dev.hyo.martie.screens.uis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.hyo.martie.models.AppColors
import dev.hyo.openiap.ProductAndroid
import dev.hyo.openiap.ProductType
import dev.hyo.openiap.PurchaseAndroid
import dev.hyo.openiap.PurchaseState
import java.util.Locale

@Composable
fun ProductDetailModal(
    product: ProductAndroid,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    isPurchasing: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "Product Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            product.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            product.displayPrice,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary
                        )
                    }

                    if (product.description.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant)
                        ) {
                            Text(
                                product.description,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (product.type) {
                                ProductType.Subs -> AppColors.secondary
                                else -> AppColors.primary
                            }.copy(alpha = 0.2f)
                        ) {
                            Text(
                                product.type.rawValue.uppercase(Locale.getDefault()),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when (product.type) {
                                    ProductType.Subs -> AppColors.secondary
                                    else -> AppColors.primary
                                }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Product ID", product.id)
                        DetailRow("Currency", product.currency)
                        product.price?.let { DetailRow("Raw Price", it.toString()) }
                        DetailRow("Platform", product.platform.rawValue)
                        product.nameAndroid?.let { DetailRow("Android Name", it) }
                    }

                    product.oneTimePurchaseOfferDetailsAndroid?.let { offer ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            "One-Time Purchase Details",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        DetailRow("Formatted Price", offer.formattedPrice)
                        DetailRow("Price (micros)", offer.priceAmountMicros)
                    }

                    product.subscriptionOfferDetailsAndroid?.takeIf { it.isNotEmpty() }?.let { offers ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                                colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    DetailRow("Base Plan", offer.basePlanId)
                                    offer.offerId?.let { DetailRow("Offer ID", it) }
                                    DetailRow("Offer Token", offer.offerToken)
                                    if (offer.offerTags.isNotEmpty()) {
                                        DetailRow("Tags", offer.offerTags.joinToString(", "))
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                    Button(
                        onClick = onPurchase,
                        modifier = Modifier.weight(1f),
                        enabled = !isPurchasing
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isPurchasing) "Purchasing..." else "Buy Now")
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseDetailModal(
    purchase: PurchaseAndroid,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "Purchase Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (purchase.purchaseState) {
                            PurchaseState.Purchased -> Icons.Default.CheckCircle
                            PurchaseState.Pending -> Icons.Default.Schedule
                            PurchaseState.Failed -> Icons.Default.Error
                            PurchaseState.Restored -> Icons.Default.Restore
                            PurchaseState.Deferred -> Icons.Default.Schedule
                            PurchaseState.Unknown -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (purchase.purchaseState) {
                            PurchaseState.Purchased -> AppColors.success
                            PurchaseState.Pending -> AppColors.warning
                            PurchaseState.Failed -> AppColors.danger
                            PurchaseState.Restored -> AppColors.info
                            PurchaseState.Deferred -> AppColors.warning
                            PurchaseState.Unknown -> AppColors.textSecondary
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
                            "Status: ${purchase.purchaseState.rawValue}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val detailRows = buildList {
                        add("id" to purchase.id)
                        add("transactionId" to (purchase.transactionId ?: "-"))
                        add("purchaseToken" to (purchase.purchaseToken ?: "-"))
                        add("purchaseState" to purchase.purchaseState.rawValue)
                        add("productId" to purchase.productId)
                        add("transactionDate" to purchase.transactionDate.toString())
                        add("isAutoRenewing" to purchase.isAutoRenewing.toString())
                        purchase.autoRenewingAndroid?.let { add("autoRenewingAndroid" to it.toString()) }
                        purchase.isAcknowledgedAndroid?.let { add("isAcknowledgedAndroid" to it.toString()) }
                        purchase.obfuscatedAccountIdAndroid?.let { add("obfuscatedAccountIdAndroid" to it) }
                        purchase.obfuscatedProfileIdAndroid?.let { add("obfuscatedProfileIdAndroid" to it) }
                        purchase.signatureAndroid?.let { add("signatureAndroid" to it) }
                    }
                    detailRows.forEach { (label, value) -> DetailRow(label, value) }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val json = purchase.toJson().toString()
                            clipboard.setText(AnnotatedString(json))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy JSON")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
