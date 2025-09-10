package dev.hyo.martie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import dev.hyo.openiap.store.OpenIapStore
import dev.hyo.openiap.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import dev.hyo.openiap.OpenIapError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionFlowScreen(
    navController: NavController,
    storeParam: OpenIapStore? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiScope = rememberCoroutineScope()
    val iapStore = storeParam ?: (IapContext.LocalOpenIapStore.current
        ?: IapContext.rememberOpenIapStore())
    val products by iapStore.products.collectAsState()
    val purchases by iapStore.availablePurchases.collectAsState()
    val status by iapStore.status.collectAsState()
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Modal states
    var selectedProduct by remember { mutableStateOf<OpenIapProduct?>(null) }
    var selectedPurchase by remember { mutableStateOf<OpenIapPurchase?>(null) }
    
    // Initialize and connect on first composition (spec-aligned names)
    val startupScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        startupScope.launch {
            try {
                val connected = iapStore.initConnection()
                if (connected) {
                    println("SubscriptionFlow: Loading subscription products: ${IapConstants.SUBS_SKUS}")
                    iapStore.fetchProducts(
                        skus = IapConstants.SUBS_SKUS,
                        type = ProductRequest.ProductRequestType.SUBS
                    )
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
                title = { Text("Subscription Flow") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                        type = ProductRequest.ProductRequestType.SUBS
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
            
            // Loading State
            if (status.isLoading) {
                item {
                    LoadingCard()
                }
            }
            
            // Active Subscriptions Section
            // Treat any purchase with matching subscription SKU as subscribed
            val activeSubscriptions = purchases.filter { it.productId in IapConstants.SUBS_SKUS }
            if (activeSubscriptions.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Active Subscriptions")
                }
                
                items(activeSubscriptions) { subscription ->
                    ActiveSubscriptionListItem(
                        purchase = subscription,
                        onClick = { selectedPurchase = subscription }
                    )
                }
            }
            
            // Available Subscription Products
            if (products.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Available Subscriptions")
                }
                
                items(products) { product ->
                    ProductCard(
                        product = product,
                        isPurchasing = status.isPurchasing(product.id),
                        isSubscribed = purchases.any { it.productId == product.id && it.purchaseState == OpenIapPurchase.PurchaseState.PURCHASED },
                        onPurchase = {
                            // Prevent re-purchase if already subscribed
                            val alreadySubscribed = purchases.any { it.productId == product.id && it.purchaseState == OpenIapPurchase.PurchaseState.PURCHASED }
                            if (alreadySubscribed) {
                                showError = true
                                errorMessage = "Already subscribed to ${product.id}"
                                return@ProductCard
                            }
                            scope.launch {
                                try {
                                    val reqType = if (product.type == OpenIapProduct.ProductType.SUBS)
                                        ProductRequest.ProductRequestType.SUBS else ProductRequest.ProductRequestType.INAPP
                                    iapStore.setActivity(activity)
                                    val purchase = iapStore.requestPurchase(
                                        params = RequestPurchaseAndroidProps(skus = listOf(product.id)),
                                        type = reqType
                                    )
                                    if (purchase != null) {
                                        val isConsumable = product.type == OpenIapProduct.ProductType.INAPP &&
                                                (product.id.contains("consumable", true) || product.id.contains("bulb", true))
                                        iapStore.finishTransaction(purchase, isConsumable)
                                        // Silent refresh without toggling loading indicator
                                        iapStore.loadPurchases()
                                        selectedProduct = null
                                    } else {
                                        showError = true
                                        errorMessage = "Purchase cancelled"
                                    }
                                } catch (e: Exception) {
                                    val msg = e.message ?: "Purchase failed"
                                    if (e is OpenIapError.UserCancelled || msg.contains("cancel", true)) {
                                        showError = true
                                        errorMessage = "Purchase cancelled by user"
                                    } else {
                                        showError = true
                                        errorMessage = msg
                                    }
                                }
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
            } else if (!status.isLoading && products.isEmpty()) {
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
    
    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Product Detail Modal
    selectedProduct?.let { product ->
        ProductDetailModal(
            product = product,
            onDismiss = { selectedProduct = null },
            onPurchase = {
                uiScope.launch {
                    try {
                        val reqType = if (product.type == OpenIapProduct.ProductType.SUBS)
                            ProductRequest.ProductRequestType.SUBS else ProductRequest.ProductRequestType.INAPP
                        iapStore.setActivity(activity)
                        val purchase = iapStore.requestPurchase(
                            params = RequestPurchaseAndroidProps(skus = listOf(product.id)),
                            type = reqType
                        )
                        if (purchase != null) {
                            val isConsumable = product.type == OpenIapProduct.ProductType.INAPP &&
                                    (product.id.contains("consumable", true) || product.id.contains("bulb", true))
                            iapStore.finishTransaction(purchase, isConsumable)
                            iapStore.loadPurchases()
                            selectedProduct = null
                        } else {
                            showError = true
                            errorMessage = "Purchase cancelled"
                        }
                    } catch (e: Exception) {
                        val msg = e.message ?: "Purchase failed"
                        if (e is OpenIapError.UserCancelled || msg.contains("cancel", true)) {
                            showError = true
                            errorMessage = "Purchase cancelled by user"
                        } else {
                            showError = true
                            errorMessage = msg
                        }
                    }
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
