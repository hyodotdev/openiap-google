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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.IapConstants
import dev.hyo.martie.screens.uis.*
import dev.hyo.martie.viewmodels.OpenIapStore
import dev.hyo.openiap.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import dev.hyo.openiap.helpers.OpenIapError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionFlowScreen(
    navController: NavController,
    iapStore: OpenIapStore = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiScope = rememberCoroutineScope()
    val products by iapStore.products.collectAsState()
    val purchases by iapStore.purchases.collectAsState()
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
            iapStore.disconnect()
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
            val activeSubscriptions = purchases.filter { it.isAutoRenewing }
            if (activeSubscriptions.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Active Subscriptions")
                }
                
                items(activeSubscriptions) { subscription ->
                    SubscriptionCard(
                        purchase = subscription,
                        onClick = {
                            selectedPurchase = subscription
                        }
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
                        onPurchase = {
                            // Prevent re-purchase if already subscribed
                            val alreadySubscribed = activeSubscriptions.any { it.productId == product.id }
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

@Composable
fun SubscriptionCard(
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
            containerColor = AppColors.secondary.copy(alpha = 0.1f)
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    purchase.productId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AppColors.success.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.success
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (purchase.isAutoRenewing) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AppColors.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "AUTO-RENEWING",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.secondary
                                )
                            }
                        } else if (purchase.productId == "dev.hyo.martie.premium") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AppColors.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "PREMIUM",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.primary
                                )
                            }
                        }
                    }
                }
                
                Text(
                    "Purchased: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(purchase.transactionDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
