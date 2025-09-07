package dev.hyo.martie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.hyo.martie.models.AppColors
import dev.hyo.martie.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            OpenIapExampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        
        composable("purchase_flow") {
            PurchaseFlowScreen(navController)
        }
        
        composable("subscription_flow") {
            SubscriptionFlowScreen(navController)
        }
        
        composable("available_purchases") {
            AvailablePurchasesScreen(navController)
        }
        
        composable("offer_code") {
            OfferCodeScreen(navController)
        }
    }
}

@Composable
fun OpenIapExampleTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = AppColors.primary,
            secondary = AppColors.secondary,
            background = AppColors.darkBackground,
            surface = AppColors.darkCardBackground,
            surfaceVariant = AppColors.darkSurfaceVariant
        )
    } else {
        lightColorScheme(
            primary = AppColors.primary,
            secondary = AppColors.secondary,
            background = AppColors.background,
            surface = AppColors.cardBackground,
            surfaceVariant = AppColors.surfaceVariant
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}