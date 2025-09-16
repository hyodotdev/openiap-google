package dev.hyo.martie.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.flow.first
import dev.hyo.openiap.OpenIapError
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import dev.hyo.openiap.models.OpenIapSerialization
import dev.hyo.openiap.store.PurchaseResultStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFlowScreen(
    navController: NavController,
    storeParam: OpenIapStore? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiScope = rememberCoroutineScope()
    val appContext = context.applicationContext as Context
    val iapStore = storeParam ?: remember(appContext) {
        val storeKey = dev.hyo.martie.BuildConfig.OPENIAP_STORE
        val appId = dev.hyo.martie.BuildConfig.HORIZON_APP_ID
        android.util.Log.i("OpenIapFactory", "example-create storeKey=${storeKey} appIdSet=${appId.isNotEmpty()}")
        runCatching { OpenIapStore(appContext, storeKey, appId) }
            .getOrElse {
                // Fallback to auto if horizon provider not present
                OpenIapStore(appContext, "auto", appId)
            }
    }
    val products by iapStore.products.collectAsState()
    val purchases by iapStore.availablePurchases.collectAsState()
    val status by iapStore.status.collectAsState()
    val lastPurchase by iapStore.currentPurchase.collectAsState(initial = null)
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    val clipboard = LocalClipboardManager.current
    val statusMessage = status.lastPurchaseResult
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
                    iapStore.setActivity(activity)
                    iapStore.fetchProducts(
                        skus = IapConstants.inappSkus(),
                        type = ProductRequest.ProductRequestType.InApp
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
                title = { Text("Purchase Flow") },
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
                                        skus = IapConstants.inappSkus(),
                                        type = ProductRequest.ProductRequestType.InApp
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
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.primary
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Purchase Flow",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    "Test product purchases",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                        
                        Text(
                            "Purchase consumable and non-consumable products. Events are handled through OpenIapStore callbacks.",
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PurchaseResultCard(
                            message = result.message,
                            status = result.status,
                            onDismiss = { iapStore.clearStatusMessage() }
                        )
                        if (result.status == PurchaseResultStatus.Success) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        lastPurchase?.let { p ->
                                            val json = OpenIapSerialization.toJson(p)
                                            clipboard.setText(AnnotatedString(json))
                                        }
                                    },
                                    enabled = lastPurchase != null
                                ) { Text("Copy Result") }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { selectedPurchase = lastPurchase },
                                    enabled = lastPurchase != null
                                ) { Text("Details") }
                            }
                        }
                    }
                }
            }

            // Products Section
            if (products.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Available Products")
                }

                items(products) { product ->
                    ProductCard(
                        product = product,
                        isPurchasing = status.isPurchasing(product.id),
                        onPurchase = {
                            scope.launch {
                                val reqType = if (product.type == OpenIapProduct.ProductType.Subs)
                                    ProductRequest.ProductRequestType.Subs else ProductRequest.ProductRequestType.InApp
                                iapStore.setActivity(activity)
                                iapStore.requestPurchase(
                                    params = RequestPurchaseParams(skus = listOf(product.id)),
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
            } else if (!status.isLoading) {
                item {
                    EmptyStateCard(
                        message = "No products available",
                        icon = Icons.Default.ShoppingBag
                    )
                }
            }
            
            // Active Purchases Section
            if (purchases.isNotEmpty()) {
                item {
                    SectionHeaderView(title = "Active Purchases")
                }
                
                items(purchases) { purchase ->
                    ActivePurchaseCard(
                        purchase = purchase,
                        onClick = {
                            selectedPurchase = purchase
                        }
                    )
                }
            }
            
            // Instructions Card
            item {
                InstructionCard()
            }
            
            // Actions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val restored = iapStore.restorePurchases()
                                    iapStore.postStatusMessage(
                                        message = "Restored ${restored.size} purchases",
                                        status = PurchaseResultStatus.Success
                                    )
                                } catch (e: Exception) {
                                    iapStore.postStatusMessage(
                                        message = e.message ?: "Restore failed",
                                        status = PurchaseResultStatus.Error
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
                    
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    iapStore.fetchProducts(
                                        skus = IapConstants.inappSkus(),
                                        type = ProductRequest.ProductRequestType.InApp
                                    )
                                } catch (_: Exception) { }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !status.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh")
                    }
                }
            }
        }
    }
    
    // Simple helper: simulate server-side receipt validation
    suspend fun validateReceiptOnServer(purchase: OpenIapPurchase): Boolean {
        // TODO: Replace with your real backend API call
        // e.g., POST purchase.purchaseToken to your server and verify
        return true
    }

    // Auto-handle purchase: validate on server then finish
    // IMPORTANT: Implement real server-side receipt validation in validateReceiptOnServer()
    LaunchedEffect(lastPurchase?.id) {
        val purchase = lastPurchase ?: return@LaunchedEffect
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
            // 2) Determine consumable vs non-consumable
            val product = products.find { it.id == purchase.productId }
            val isConsumable = product?.let {
                it.type == OpenIapProduct.ProductType.InApp &&
                        (it.id.contains("consumable", true) || it.id.contains("bulb", true))
            } == true

            // 3) Ensure connection (retry briefly if needed)
            if (!connectionStatus) {
                runCatching { iapStore.initConnection() }
                val started = System.currentTimeMillis()
                while (!iapStore.isConnected.first() && System.currentTimeMillis() - started < 1500) {
                    delay(100)
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
                iapStore.postStatusMessage(
                    message = "Purchase finished successfully",
                    status = PurchaseResultStatus.Success,
                    productId = purchase.productId
                )
                selectedProduct = null
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
                    val reqType = if (product.type == OpenIapProduct.ProductType.Subs)
                        ProductRequest.ProductRequestType.Subs else ProductRequest.ProductRequestType.InApp
                    iapStore.setActivity(activity)
                    iapStore.requestPurchase(
                        params = RequestPurchaseParams(skus = listOf(product.id)),
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
