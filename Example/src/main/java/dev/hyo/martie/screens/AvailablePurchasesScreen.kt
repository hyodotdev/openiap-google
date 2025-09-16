package dev.hyo.martie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.screens.uis.*
import dev.hyo.openiap.IapContext
import dev.hyo.openiap.store.OpenIapStore
import dev.hyo.openiap.models.*
import dev.hyo.openiap.store.PurchaseResultStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailablePurchasesScreen(
    navController: NavController,
    storeParam: OpenIapStore? = null
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val iapStore = storeParam ?: (IapContext.LocalOpenIapStore.current
        ?: remember(appContext) {
            val storeKey = dev.hyo.martie.BuildConfig.OPENIAP_STORE
            val appId = dev.hyo.martie.BuildConfig.HORIZON_APP_ID
            android.util.Log.i("OpenIapFactory", "example-create storeKey=${storeKey} appIdSet=${appId.isNotEmpty()}")
            runCatching { OpenIapStore(appContext, storeKey, appId) }
                .getOrElse { OpenIapStore(appContext, "auto", appId) }
        })
    val purchases by iapStore.availablePurchases.collectAsState()
    val status by iapStore.status.collectAsState()
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    val statusMessage = status.lastPurchaseResult
    
    // Modal state
    var selectedPurchase by remember { mutableStateOf<OpenIapPurchase?>(null) }
    
    // Initialize and connect on first composition (spec-aligned names)
    val startupScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        startupScope.launch {
            try {
                val connected = iapStore.initConnection()
                if (connected) {
                    iapStore.getAvailablePurchases()
                }
            } catch (_: Exception) { }
        }
        onDispose {
            startupScope.launch { runCatching { iapStore.endConnection() } }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Purchases") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val restored = iapStore.restorePurchases()
                                    iapStore.postStatusMessage(
                                        message = "Restored ${restored.size} purchases",
                                        status = PurchaseResultStatus.SUCCESS
                                    )
                                } catch (e: Exception) {
                                    iapStore.postStatusMessage(
                                        message = e.message ?: "Restore failed",
                                        status = PurchaseResultStatus.ERROR
                                    )
                                }
                            }
                        },
                        enabled = !status.isLoading
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.background
                )
            )
        }
    ) { paddingValues ->
        val scope = rememberCoroutineScope()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.background),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.success
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Available Purchases",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    "View and restore purchases",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                        
                        Text(
                            "View all your active purchases including consumables not yet consumed, non-consumables, and active subscriptions. Tap 'Restore' to recover purchases from your Google account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
            
            // Loading State
            if (status.isLoading) {
                item {
                    LoadingCard()
                }
            }
            
            statusMessage?.let { result ->
                item("status-message") {
                    PurchaseResultCard(
                        message = result.message,
                        status = result.status,
                        onDismiss = { iapStore.clearStatusMessage() }
                    )
                }
            }
            
            // Group purchases by type
            val consumables = purchases.filter { 
                !it.isAutoRenewing && (
                    it.productId.contains("consumable", ignoreCase = true) ||
                    it.productId.contains("bulb", ignoreCase = true)
                )
            }
            val nonConsumables = purchases.filter { 
                !it.isAutoRenewing && 
                it.productId != "dev.hyo.martie.premium" &&
                !(
                    it.productId.contains("consumable", ignoreCase = true) ||
                    it.productId.contains("bulb", ignoreCase = true)
                )
            }
            val subscriptions = purchases.filter { 
                it.isAutoRenewing || it.productId == "dev.hyo.martie.premium"
            }
            
            // Check for unfinished transactions (purchases that need acknowledgment/consumption)
            val unfinishedPurchases = purchases.filter { purchase ->
                // TODO: In real implementation, check if purchase needs acknowledgment/consumption
                // This would typically check: purchase.purchaseState == PURCHASED && !purchase.isAcknowledged
                // For demo purposes, let's assume some consumable purchases might need finishing
                (purchase.productId.contains("consumable", ignoreCase = true) ||
                 purchase.productId.contains("bulb", ignoreCase = true)) && 
                purchases.indexOf(purchase) < 2 // Show first 2 consumables as unfinished for demo
            }
            
            // Unfinished Transactions Section
            if (unfinishedPurchases.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "⚠️ Unfinished Transactions (${unfinishedPurchases.size})")
                }
                
                items(unfinishedPurchases) { purchase ->
                    UnfinishedTransactionCard(
                        purchase = purchase,
                        onFinish = { isConsumable ->
                            scope.launch {
                                try {
                                    val ok = iapStore.finishTransaction(purchase, isConsumable)
                                    if (ok) {
                                        iapStore.postStatusMessage(
                                            message = "Transaction finished successfully",
                                            status = PurchaseResultStatus.SUCCESS,
                                            productId = purchase.productId
                                        )
                                        iapStore.getAvailablePurchases()
                                    } else {
                                        iapStore.postStatusMessage(
                                            message = "Failed to finish transaction",
                                            status = PurchaseResultStatus.ERROR,
                                            productId = purchase.productId
                                        )
                                    }
                                } catch (e: Exception) {
                                    iapStore.postStatusMessage(
                                        message = e.message ?: "Failed to finish transaction",
                                        status = PurchaseResultStatus.ERROR,
                                        productId = purchase.productId
                                    )
                                }
                            }
                        },
                        onClick = { selectedPurchase = purchase }
                    )
                }
                
                // Warning card for unfinished transactions
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.warning.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppColors.warning
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Unfinished Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "These purchases need to be acknowledged or consumed to complete the transaction. Tap 'Finish' to complete them after server validation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // Active Subscriptions
            if (subscriptions.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Active Subscriptions (${subscriptions.size})")
                }
                
                items(subscriptions) { purchase ->
                    ActiveSubscriptionListItem(
                        purchase = purchase,
                        onClick = { selectedPurchase = purchase }
                    )
                }
            }
            
            // Non-Consumables
            if (nonConsumables.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Non-Consumables (${nonConsumables.size})")
                }
                
                items(nonConsumables) { purchase ->
                    PurchaseItemCard(
                        purchase = purchase,
                        type = PurchaseType.NON_CONSUMABLE,
                        onClick = { selectedPurchase = purchase }
                    )
                }
            }
            
            // Pending Consumables
            if (consumables.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Pending Consumables (${consumables.size})")
                }
                
                items(consumables) { purchase ->
                    PurchaseItemCard(
                        purchase = purchase,
                        type = PurchaseType.CONSUMABLE,
                        onClick = { selectedPurchase = purchase }
                    )
                }
            }
            
            // Empty State
            if (purchases.isEmpty() && !status.isLoading) {
                item {
                    EmptyStateCard(
                        message = "No purchases found. Try restoring purchases from your Google account.",
                        icon = Icons.Default.ShoppingBag
                    )
                }
            }
            
            // Statistics Card
            if (purchases.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Purchase Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatisticItem(
                                    count = purchases.size,
                                    label = "Total",
                                    color = AppColors.primary
                                )
                                StatisticItem(
                                    count = subscriptions.size,
                                    label = "Subscriptions",
                                    color = AppColors.secondary
                                )
                                StatisticItem(
                                    count = nonConsumables.size,
                                    label = "Owned",
                                    color = AppColors.success
                                )
                                StatisticItem(
                                    count = consumables.size,
                                    label = "Pending",
                                    color = AppColors.warning
                                )
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { scope.launch { iapStore.getAvailablePurchases() } },
                        modifier = Modifier.weight(1f),
                        enabled = !status.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val restored = iapStore.restorePurchases()
                                    iapStore.postStatusMessage(
                                        message = "Restored ${restored.size} purchases",
                                        status = PurchaseResultStatus.SUCCESS
                                    )
                                } catch (e: Exception) {
                                    iapStore.postStatusMessage(
                                        message = e.message ?: "Restore failed",
                                        status = PurchaseResultStatus.ERROR
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !status.isLoading
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore")
                    }
                }
            }
        }
    }
    
    // Purchase Detail Modal
    selectedPurchase?.let { purchase ->
        PurchaseDetailModal(
            purchase = purchase,
            onDismiss = { selectedPurchase = null }
        )
    }
}

enum class PurchaseType {
    CONSUMABLE,
    NON_CONSUMABLE,
    SUBSCRIPTION
}

@Composable
fun PurchaseItemCard(
    purchase: OpenIapPurchase,
    type: PurchaseType,
    onClick: () -> Unit
) {
    val (backgroundColor, iconColor, icon) = when (type) {
        PurchaseType.SUBSCRIPTION -> Triple(
            AppColors.secondary.copy(alpha = 0.1f),
            AppColors.secondary,
            Icons.Default.Autorenew
        )
        PurchaseType.NON_CONSUMABLE -> Triple(
            AppColors.success.copy(alpha = 0.1f),
            AppColors.success,
            Icons.Default.CheckCircle
        )
        PurchaseType.CONSUMABLE -> Triple(
            AppColors.warning.copy(alpha = 0.1f),
            AppColors.warning,
            Icons.Default.Schedule
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        purchase.productId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        "Purchased: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(purchase.transactionDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    
                    if (purchase.isAutoRenewing) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AppColors.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "AUTO-RENEWING",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.secondary
                            )
                        }
                    }
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StatisticItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.textSecondary
        )
    }
}

@Composable
fun UnfinishedTransactionCard(
    purchase: OpenIapPurchase,
    onFinish: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    var showFinishDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.warning.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppColors.warning,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        purchase.productId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        "Needs acknowledgment/consumption",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.warning
                    )
                    
                    Text(
                        "Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(purchase.transactionDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }
            }
            
            Button(
                onClick = { showFinishDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.warning
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Finish", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
    
    // Finish Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Product: ${purchase.productId}")
                    Text("Choose how to finish this transaction:")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (purchase.productId.contains("consumable", ignoreCase = true) ||
                        purchase.productId.contains("bulb", ignoreCase = true)) {
                        TextButton(
                            onClick = {
                                showFinishDialog = false
                                onFinish(true) // Consume
                            }
                        ) {
                            Text("Consume")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                showFinishDialog = false
                                onFinish(false) // Acknowledge
                            }
                        ) {
                            Text("Acknowledge")
                        }
                    }
                }
            }
        )
    }
}
