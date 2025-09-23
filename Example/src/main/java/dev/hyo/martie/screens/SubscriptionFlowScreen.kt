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
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.IapConstants
import dev.hyo.martie.screens.uis.*
import dev.hyo.openiap.IapContext
import dev.hyo.openiap.ProductAndroid
import dev.hyo.openiap.ProductQueryType
import dev.hyo.openiap.ProductType
import dev.hyo.openiap.PurchaseAndroid
import dev.hyo.openiap.PurchaseState
import dev.hyo.openiap.store.OpenIapStore
import dev.hyo.openiap.store.PurchaseResultStatus
import dev.hyo.openiap.OpenIapError
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import dev.hyo.martie.util.findActivity

// Helper to format remaining time like "3d 4h" / "2h 12m" / "35m"
private fun formatRemaining(deltaMillis: Long): String {
    if (deltaMillis <= 0) return "0m"
    val totalMinutes = deltaMillis / 60000
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes % (60 * 24)) / 60
    val minutes = totalMinutes % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionFlowScreen(
    navController: NavController,
    storeParam: OpenIapStore? = null
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val uiScope = rememberCoroutineScope()
    val appContext = remember(context) { context.applicationContext }
    val iapStore = storeParam ?: remember(appContext) { OpenIapStore(appContext) }
    val products by iapStore.products.collectAsState()
    val purchases by iapStore.availablePurchases.collectAsState()
    val androidProducts = remember(products) { products.filterIsInstance<ProductAndroid>() }
    val androidPurchases = remember(purchases) { purchases.filterIsInstance<PurchaseAndroid>() }
    val status by iapStore.status.collectAsState()
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    val lastPurchase by iapStore.currentPurchase.collectAsState(initial = null)
    val lastPurchaseAndroid: PurchaseAndroid? = remember(lastPurchase) {
        when (val purchase = lastPurchase) {
            is PurchaseAndroid -> purchase
            else -> null
        }
    }

    // Real-time subscription status (expiry/renewal). This requires server validation.
    data class SubscriptionUiInfo(
        val renewalDate: Long? = null,       // expiryTimeMillis
        val autoRenewing: Boolean = true,
        val gracePeriodEndDate: Long? = null,
        val freeTrialEndDate: Long? = null
    )
    var subStatus by remember { mutableStateOf<Map<String, SubscriptionUiInfo>>(emptyMap()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    // Tick clock to update countdown once per second
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    // TODO: Replace with your backend call to Play Developer API
    suspend fun fetchSubStatusFromServer(productId: String, purchaseToken: String): SubscriptionUiInfo? {
        // Expected mapping of your server response (ReceiptValidationResultAndroid)
        // return SubscriptionUiInfo(
        //   renewalDate = result.renewalDate,
        //   autoRenewing = result.autoRenewing,
        //   gracePeriodEndDate = result.gracePeriodEndDate,
        //   freeTrialEndDate = result.freeTrialEndDate,
        // )
        return null
    }

    // Refresh server-side status when purchases change
    LaunchedEffect(androidPurchases) {
        val map = mutableMapOf<String, SubscriptionUiInfo>()
        androidPurchases
            .filter { it.productId in IapConstants.SUBS_SKUS }
            .forEach { purchase ->
                val token = purchase.purchaseToken ?: return@forEach
                val info = fetchSubStatusFromServer(purchase.productId, token)
                if (info != null) {
                    map[purchase.productId] = info.copy(autoRenewing = purchase.isAutoRenewing)
                }
            }
        subStatus = map
    }
    val statusMessage = status.lastPurchaseResult
    
    // Modal states
    var selectedProduct by remember { mutableStateOf<ProductAndroid?>(null) }
    var selectedPurchase by remember { mutableStateOf<PurchaseAndroid?>(null) }
    
    // Initialize and connect on first composition (spec-aligned names)
    val startupScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        startupScope.launch {
            try {
                val connected = iapStore.initConnection()
                if (connected) {
                    iapStore.setActivity(activity)
                    println("SubscriptionFlow: Loading subscription products: ${IapConstants.SUBS_SKUS}")
                    iapStore.fetchProducts(
                        skus = IapConstants.SUBS_SKUS,
                        type = ProductQueryType.Subs
                    )
                    iapStore.getAvailablePurchases()
                }
            } catch (_: Exception) { }
        }
        
        onDispose {
            // End connection and clear listeners when this screen leaves (per-screen lifecycle)
            startupScope.launch {
                runCatching { iapStore.endConnection() }
                runCatching { iapStore.clear() }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Flow") },
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
                                    iapStore.setActivity(activity)
                                    iapStore.fetchProducts(
                                        skus = IapConstants.SUBS_SKUS,
                                        type = ProductQueryType.Subs
                                    )
                                } catch (_: Exception) { }
                            }
                        },
                        enabled = !status.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                                Icons.Default.Autorenew,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.secondary
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Subscription Flow",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    "Manage recurring subscriptions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                        
                        Text(
                            "Purchase and manage auto-renewable subscriptions. View active subscriptions and their renewal status.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
            statusMessage?.let { result ->
                item("status-message") {
                    PurchaseResultCard(
                        message = result.message,
                        status = result.status,
                        onDismiss = { iapStore.clearStatusMessage() },
                        code = result.code
                    )
                }
            }
            
            // Loading State
            if (status.isLoading) {
                item {
                    LoadingCard()
                }
            }
            
            // Active Subscriptions Section
            // Treat any purchase with matching subscription SKU as subscribed
            val activeSubscriptions = androidPurchases.filter { it.productId in IapConstants.SUBS_SKUS }
            if (activeSubscriptions.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Active Subscriptions")
                }
                
                items(activeSubscriptions) { subscription ->
                    val info = subStatus[subscription.productId]
                    val fmt = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                    val statusText = when {
                        info?.freeTrialEndDate != null ->
                            "Free trial ends: ${fmt.format(java.util.Date(info.freeTrialEndDate))} (${formatRemaining(info.freeTrialEndDate - now)})"
                        info?.gracePeriodEndDate != null ->
                            "Grace period ends: ${fmt.format(java.util.Date(info.gracePeriodEndDate))} (${formatRemaining(info.gracePeriodEndDate - now)})"
                        info?.renewalDate != null && (info.autoRenewing) ->
                            "Renews on ${fmt.format(java.util.Date(info.renewalDate))} (${formatRemaining(info.renewalDate - now)})"
                        info?.renewalDate != null && (!info.autoRenewing) ->
                            "Expires on ${fmt.format(java.util.Date(info.renewalDate))} (${formatRemaining(info.renewalDate - now)})"
                        else -> null
                    }
                    ActiveSubscriptionListItem(
                        purchase = subscription,
                        statusText = statusText,
                        onClick = { selectedPurchase = subscription }
                    )
                }
            }
            
            // Available Subscription Products
            if (androidProducts.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Available Subscriptions")
                }
                
                items(androidProducts) { product ->
                    ProductCard(
                        product = product,
                        isPurchasing = status.isPurchasing(product.id),
                        isSubscribed = androidPurchases.any { it.productId == product.id && it.purchaseState == PurchaseState.Purchased },
                        onPurchase = {
                            // Prevent re-purchase if already subscribed
                            val alreadySubscribed = androidPurchases.any { it.productId == product.id && it.purchaseState == PurchaseState.Purchased }
                            if (alreadySubscribed) {
                                iapStore.postStatusMessage(
                                    message = "Already subscribed to ${product.id}",
                                    status = PurchaseResultStatus.Info,
                                    productId = product.id
                                )
                                return@ProductCard
                            }
                            scope.launch {
                                val reqType = if (product.type == ProductType.Subs)
                                    ProductQueryType.Subs else ProductQueryType.InApp
                                iapStore.setActivity(activity)
                                iapStore.requestPurchase(
                                    skus = listOf(product.id),
                                    type = reqType
                                )
                            }
                        },
                        onClick = {
                            selectedProduct = product
                        },
                        onDetails = {
                            selectedProduct = product
                        }
                    )
                }
            } else if (!status.isLoading && androidProducts.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "No subscription products available",
                        icon = Icons.Default.Autorenew
                    )
                }
            }
            
            // Subscription Management Info
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.info
                            )
                            Text(
                                "Subscription Management",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Text(
                            "• Subscriptions auto-renew until cancelled\n" +
                            "• Manage subscriptions in Google Play Store\n" +
                            "• Cancellation takes effect at the end of the current billing period\n" +
                            "• Family sharing may be available for some subscriptions",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }

    // Auto-handle purchase: validate on server then finish (expo-iap style)
    // IMPORTANT: Implement real server-side receipt validation in validateReceiptOnServer()
    suspend fun validateReceiptOnServer(purchase: PurchaseAndroid): Boolean {
        // TODO: Replace with your real backend API call
        // e.g., POST purchase.purchaseToken to your server and verify
        return true
    }

    LaunchedEffect(lastPurchaseAndroid?.id) {
        val purchase = lastPurchaseAndroid ?: return@LaunchedEffect
        try {
            // 1) Server-side validation (replace with your backend call)
            val valid = validateReceiptOnServer(purchase)
            if (!valid) {
                iapStore.postStatusMessage(
                    message = "Receipt validation failed",
                    status = PurchaseResultStatus.Error,
                    productId = purchase.productId
                )
                return@LaunchedEffect
            }
            // 2) Determine consumable vs non-consumable (subs -> false)
            val product = products.find { it.id == purchase.productId }
            val isConsumable = product?.let {
                it.type == ProductType.InApp &&
                        (it.id.contains("consumable", true) || it.id.contains("bulb", true))
            } == true

            // 3) Ensure connection (quick retry)
            if (!connectionStatus) {
                runCatching { iapStore.initConnection() }
                val started = System.currentTimeMillis()
                while (!iapStore.isConnected.value && System.currentTimeMillis() - started < 1500) {
                    kotlinx.coroutines.delay(100)
                }
            }

            // 4) Finish transaction
            val ok = iapStore.finishTransaction(purchase, isConsumable)
            if (!ok) {
                iapStore.postStatusMessage(
                    message = "finishTransaction failed",
                    status = PurchaseResultStatus.Error,
                    productId = purchase.productId
                )
            } else {
                iapStore.loadPurchases()
            }
        } catch (e: Exception) {
            iapStore.postStatusMessage(
                message = e.message ?: "Failed to finish purchase",
                status = PurchaseResultStatus.Error,
                productId = purchase.productId
            )
        }
    }

    // Product Detail Modal
    selectedProduct?.let { product ->
        ProductDetailModal(
            product = product,
            onDismiss = { selectedProduct = null },
            onPurchase = {
                uiScope.launch {
                    val reqType = if (product.type == ProductType.Subs)
                        ProductQueryType.Subs else ProductQueryType.InApp
                    iapStore.setActivity(activity)
                    iapStore.requestPurchase(
                        skus = listOf(product.id),
                        type = reqType
                    )
                }
            },
            isPurchasing = status.isPurchasing(product.id)
        )
    }
    
    // Purchase Detail Modal
    selectedPurchase?.let { purchase ->
        PurchaseDetailModal(
            purchase = purchase,
            onDismiss = { selectedPurchase = null }
        )
    }
}

// Moved to reusable component at: screens/uis/ActiveSubscriptionListItem.kt
