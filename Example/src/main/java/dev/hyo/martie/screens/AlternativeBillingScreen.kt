package dev.hyo.martie.screens

import android.app.Activity
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.hyo.martie.IapConstants
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.screens.uis.*
import dev.hyo.openiap.store.OpenIapStore
import dev.hyo.openiap.store.PurchaseResultStatus
import kotlinx.coroutines.launch
import dev.hyo.openiap.ProductAndroid
import dev.hyo.openiap.ProductQueryType
import dev.hyo.openiap.ProductRequest
import dev.hyo.openiap.PurchaseAndroid
import dev.hyo.openiap.RequestPurchaseProps
import dev.hyo.openiap.RequestPurchaseAndroidProps
import dev.hyo.openiap.RequestPurchasePropsByPlatforms
import dev.hyo.openiap.PurchaseInput
import dev.hyo.openiap.AlternativeBillingMode
import dev.hyo.martie.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeBillingScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val appContext = remember(context) { context.applicationContext }

    // Initialize store with alternative billing enabled
    // Using ALTERNATIVE_ONLY mode (user cannot choose Google Play billing)
    // IMPORTANT: Create new instance every time to ensure proper initialization
    val iapStore = remember(Unit) {
        android.util.Log.d("AlternativeBillingScreen", "Creating new OpenIapStore with ALTERNATIVE_ONLY mode")
        // Enable OpenIapLog for debugging
        dev.hyo.openiap.OpenIapLog.isEnabled = true
        OpenIapStore(appContext, alternativeBillingMode = AlternativeBillingMode.ALTERNATIVE_ONLY)
    }

    val products by iapStore.products.collectAsState()
    val androidProducts = remember(products) { products.filterIsInstance<ProductAndroid>() }
    val status by iapStore.status.collectAsState()
    val lastPurchase by iapStore.currentPurchase.collectAsState(initial = null)
    val connectionStatus by iapStore.connectionStatus.collectAsState()
    val statusMessage = status.lastPurchaseResult

    // Initialize connection
    DisposableEffect(Unit) {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
        scope.launch {
            try {
                val connected = iapStore.initConnection()
                if (connected) {
                    iapStore.setActivity(activity)
                    val request = ProductRequest(
                        skus = IapConstants.INAPP_SKUS,
                        type = ProductQueryType.InApp
                    )
                    iapStore.fetchProducts(request)
                }
            } catch (_: Exception) { }
        }
        onDispose {
            scope.launch {
                runCatching { iapStore.endConnection() }
                runCatching { iapStore.clear() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alternative Billing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.warning.copy(alpha = 0.1f))
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
                                tint = AppColors.warning
                            )
                            Text(
                                "Alternative Billing Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            "⚠️ IMPORTANT: Alternative Billing Status\n\n" +
                            "Mode: ALTERNATIVE_ONLY\n" +
                            "Library: Billing 8.0.0\n\n" +
                            "Check logcat for initialization status:\n" +
                            "• Look for: '=== ALTERNATIVE BILLING ONLY INITIALIZATION ==='\n" +
                            "• Success: '✓ Alternative billing only enabled successfully'\n" +
                            "• Failure: '✗ enableAlternativeBillingOnly() method not found'\n\n" +
                            "If you see Google Play dialog:\n" +
                            "1. App NOT enrolled in alternative billing program\n" +
                            "2. Play Console setup incomplete\n" +
                            "3. Device/account not eligible\n" +
                            "4. enableAlternativeBillingOnly() not called\n\n" +
                            "Alternative billing requires:\n" +
                            "• Google Play Console enrollment & approval\n" +
                            "• Country/region eligibility\n" +
                            "• Proper BillingClient configuration\n\n" +
                            "This is a DEMO implementation showing the API usage.\n" +
                            "Actual alternative billing requires Google approval.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }

            // Connection Status
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (connectionStatus) AppColors.success.copy(alpha = 0.1f)
                        else AppColors.danger.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            if (connectionStatus) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (connectionStatus) AppColors.success else AppColors.danger
                        )
                        Column {
                            Text(
                                "Connection Status",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (connectionStatus) "Connected (Alternative Billing Enabled)"
                                else "Disconnected",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }
            }

            // Purchase Result
            if (statusMessage != null) {
                item {
                    PurchaseResultCard(
                        message = statusMessage.message,
                        status = statusMessage.status,
                        code = statusMessage.code?.toString(),
                        onDismiss = { iapStore.clearStatusMessage() }
                    )
                }
            }

            // Products Section
            item {
                SectionHeaderView(title = "Available Products")
            }

            if (status.isLoading && androidProducts.isEmpty()) {
                item {
                    LoadingCard()
                }
            } else if (androidProducts.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.ShoppingCart,
                        message = "No products available"
                    )
                }
            } else {
                items(androidProducts) { product ->
                    ProductCard(
                        product = product,
                        isPurchasing = status.isLoading,
                        onPurchase = {
                            scope.launch {
                                try {
                                    iapStore.setActivity(activity)

                                    // ============================================================
                                    // ALTERNATIVE BILLING FLOW - STEP BY STEP CONTROL
                                    // ============================================================
                                    // For production use, you should call these steps manually:
                                    //
                                    // Step 1: Check availability
                                    val isAvailable = iapStore.checkAlternativeBillingAvailability()
                                    if (!isAvailable) {
                                        android.util.Log.e("AlternativeBilling", "Not available")
                                        iapStore.postStatusMessage(
                                            "Alternative billing not available",
                                            dev.hyo.openiap.store.PurchaseResultStatus.Error
                                        )
                                        return@launch
                                    }

                                    // Step 2: Show information dialog
                                    val dialogAccepted = iapStore.showAlternativeBillingInformationDialog(activity!!)
                                    if (!dialogAccepted) {
                                        android.util.Log.d("AlternativeBilling", "User canceled dialog")
                                        iapStore.postStatusMessage(
                                            "User canceled",
                                            dev.hyo.openiap.store.PurchaseResultStatus.Info
                                        )
                                        return@launch
                                    }

                                    // ⚠️ Step 2.5: PRODUCTION - Process payment in YOUR system
                                    // ============================================================
                                    // TODO: Replace this with your actual payment processing
                                    // Example:
                                    // val paymentResult = YourPaymentSystem.processPayment(
                                    //     productId = product.id,
                                    //     amount = product.price,
                                    //     userId = currentUserId
                                    // )
                                    // if (!paymentResult.isSuccess) {
                                    //     // Handle payment failure
                                    //     return@launch
                                    // }
                                    android.util.Log.d("AlternativeBilling", "⚠️ Payment processing not implemented - skipping to token creation")

                                    // Step 3: Create token (AFTER payment success)
                                    val token = iapStore.createAlternativeBillingReportingToken()
                                    if (token != null) {
                                        android.util.Log.d("AlternativeBilling", "✓ Token created: $token")

                                        // ⚠️ Step 4: PRODUCTION - Send token to your backend
                                        // ============================================================
                                        // TODO: Send token to your backend within 24 hours
                                        // Example:
                                        // YourBackendApi.reportTransaction(
                                        //     externalTransactionToken = token,
                                        //     productId = product.id,
                                        //     userId = currentUserId
                                        // )
                                        // Backend should call Google Play Developer API:
                                        // POST https://androidpublisher.googleapis.com/androidpublisher/v3/
                                        //      applications/{packageName}/externalTransactions
                                        android.util.Log.w("AlternativeBilling", "⚠️ Backend reporting not implemented")

                                        iapStore.postStatusMessage(
                                            "Alternative billing flow completed (DEMO)\nToken: ${token.take(20)}...\n⚠️ Backend reporting required",
                                            dev.hyo.openiap.store.PurchaseResultStatus.Info,
                                            product.id
                                        )
                                    } else {
                                        android.util.Log.e("AlternativeBilling", "Failed to create token")
                                        iapStore.postStatusMessage(
                                            "Failed to create reporting token",
                                            dev.hyo.openiap.store.PurchaseResultStatus.Error
                                        )
                                    }
                                    // ============================================================
                                    // See: https://developer.android.com/google/play/billing/alternative
                                    // ============================================================
                                } catch (e: Exception) {
                                    // Error handled by store
                                }
                            }
                        },
                        onClick = {}
                    )
                }
            }

            // Last Purchase Info
            if (lastPurchase != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Last Purchase",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            val purchase = lastPurchase as? PurchaseAndroid
                            if (purchase != null) {
                                Text(
                                    "Product: ${purchase.productId}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "State: ${purchase.purchaseState}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Token: ${purchase.purchaseToken?.take(20)}...",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val purchaseInput = PurchaseInput(
                                                    id = purchase.id,
                                                    ids = purchase.ids,
                                                    isAutoRenewing = purchase.isAutoRenewing ?: false,
                                                    platform = purchase.platform,
                                                    productId = purchase.productId,
                                                    purchaseState = purchase.purchaseState,
                                                    purchaseToken = purchase.purchaseToken,
                                                    quantity = purchase.quantity ?: 1,
                                                    transactionDate = purchase.transactionDate ?: 0.0
                                                )
                                                iapStore.finishTransaction(purchaseInput, true)
                                            } catch (_: Exception) { }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text("Finish Transaction")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
