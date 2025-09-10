package dev.hyo.martie.screens

import androidx.compose.foundation.background
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
import androidx.navigation.NavController
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.IapConstants
import dev.hyo.martie.screens.uis.*
import dev.hyo.openiap.IapContext
import dev.hyo.openiap.store.OpenIapStore
import dev.hyo.openiap.models.*
import kotlinx.coroutines.launch
import dev.hyo.openiap.OpenIapError
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import dev.hyo.openiap.models.OpenIapSerialization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFlowScreen(
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
    val lastPurchase by iapStore.currentPurchase.collectAsState(initial = null)
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    val clipboard = LocalClipboardManager.current
    
    var showPurchaseResult by remember { mutableStateOf(false) }
    var purchaseResultMessage by remember { mutableStateOf("") }
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
                    iapStore.fetchProducts(
                        skus = IapConstants.INAPP_SKUS,
                        type = ProductRequest.ProductRequestType.INAPP
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
                title = { Text("Purchase Flow") },
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
                                        skus = IapConstants.INAPP_SKUS,
                                        type = ProductRequest.ProductRequestType.INAPP
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
                                    val ok = iapStore.finishTransaction(purchase, isConsumable)
                                    iapStore.loadPurchases()
                                        showPurchaseResult = true
                                        purchaseResultMessage = if (ok) {
                                            "Purchase successful: ${product.title}"
                                        } else {
                                            "Purchase successful but finish failed: ${product.title}"
                                        }
                                        selectedProduct = null
                                    } else {
                                        showError = true
                                        errorMessage = "Purchase cancelled"
                                    }
                                } catch (e: Exception) {
                                    // Graceful handling for user cancel
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
            } else if (!status.isLoading) {
                item {
                    EmptyStateCard(
                        message = "No products available",
                        icon = Icons.Default.ShoppingBag
                    )
                }
            }
            
            // Purchase Result
            if (showPurchaseResult) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PurchaseResultCard(
                            message = purchaseResultMessage,
                            onDismiss = { showPurchaseResult = false }
                        )
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
                                    showPurchaseResult = true
                                    purchaseResultMessage = "Restored ${restored.size} purchases"
                                } catch (e: Exception) {
                                    showError = true
                                    errorMessage = e.message ?: "Restore failed"
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
                                        skus = IapConstants.INAPP_SKUS,
                                        type = ProductRequest.ProductRequestType.INAPP
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
                            val ok = iapStore.finishTransaction(purchase, isConsumable)
                            iapStore.loadPurchases()
                            showPurchaseResult = true
                            purchaseResultMessage = if (ok) {
                                "Purchase successful: ${product.title}"
                            } else {
                                "Purchase successful but finish failed: ${product.title}"
                            }
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
