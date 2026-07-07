package com.restaurant.offlinemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurant.offlinemanager.core.design.RestaurantOfflineTheme
import com.restaurant.offlinemanager.core.navigation.AppNavGraph
import com.restaurant.offlinemanager.ui.RestaurantViewModel
import com.restaurant.offlinemanager.ui.RestaurantViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RestaurantOfflineApp).container
        setContent {
            RestaurantOfflineTheme {
                val viewModel: RestaurantViewModel = viewModel(
                    factory = RestaurantViewModelFactory(
                        repository = container.repository,
                        settingsRepository = container.settingsRepository,
                        useCases = container.useCases
                    )
                )
                AppNavGraph(viewModel)
            }
        }
    }
}
