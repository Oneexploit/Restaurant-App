package com.restaurant.offlinemanager

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.restaurant.offlinemanager.core.notifications.AppNotificationManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurant.offlinemanager.core.design.RestaurantOfflineTheme
import com.restaurant.offlinemanager.core.navigation.AppNavGraph
import com.restaurant.offlinemanager.ui.RestaurantViewModel
import com.restaurant.offlinemanager.ui.RestaurantViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AppNotificationManager.ensureChannels(this)
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
