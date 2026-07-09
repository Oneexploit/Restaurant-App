package com.restaurant.offlinemanager.core.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.restaurant.offlinemanager.core.design.AppCyan
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.SecondaryGlassButton
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary

@Composable
fun AppLockGate(
    appLockEnabled: Boolean,
    settingsReady: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context.findFragmentActivity()
    val availability = remember(appLockEnabled, settingsReady) { AppLockAuthenticator.availability(context) }

    var unlocked by rememberSaveable { mutableStateOf(false) }
    var promptInFlight by remember { mutableStateOf(false) }
    var promptDismissed by remember { mutableStateOf(false) }
    var promptRequest by remember { mutableIntStateOf(0) }
    var lastReadyLockValue by remember { mutableStateOf<Boolean?>(null) }
    var lockMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(settingsReady, appLockEnabled) {
        if (!settingsReady) return@LaunchedEffect
        val previous = lastReadyLockValue
        if (!appLockEnabled) {
            unlocked = false
            promptInFlight = false
            promptDismissed = false
            lockMessage = null
        } else if (previous == false) {
            unlocked = true
            promptDismissed = false
            lockMessage = null
        } else if (!unlocked) {
            promptDismissed = false
            promptRequest++
        }
        lastReadyLockValue = appLockEnabled
    }

    DisposableEffect(lifecycleOwner, appLockEnabled, settingsReady) {
        val observer = LifecycleEventObserver { _, event ->
            if (!settingsReady || !appLockEnabled) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (!promptInFlight) unlocked = false
                    promptDismissed = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!unlocked && !promptDismissed) promptRequest++
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(settingsReady, appLockEnabled, unlocked, promptRequest, availability.canUse, promptDismissed) {
        if (!settingsReady || !appLockEnabled || unlocked || !availability.canUse || promptInFlight || promptDismissed || activity == null) return@LaunchedEffect
        promptInFlight = true
        lockMessage = null
        AppLockAuthenticator.authenticate(
            activity = activity,
            title = "قفل برنامه",
            subtitle = "برای ورود به مدیریت رستوران هویت خود را تایید کنید",
            onSuccess = {
                unlocked = true
                promptInFlight = false
                promptDismissed = false
                lockMessage = null
            },
            onError = { message ->
                promptInFlight = false
                promptDismissed = true
                lockMessage = message
            }
        )
    }

    when {
        !settingsReady -> LockedContent(
            title = "در حال آماده‌سازی",
            subtitle = "تنظیمات امنیتی برنامه بررسی می‌شود.",
            status = null,
            primaryAction = null,
            secondaryAction = null,
            modifier = modifier
        )

        !appLockEnabled || unlocked -> content()

        else -> {
            BackHandler(enabled = true) {}
            LockedContent(
                title = "برنامه قفل است",
                subtitle = if (availability.canUse) {
                    "برای ادامه از ${availability.supportedMethodsLabel} استفاده کنید."
                } else {
                    availability.statusLabel
                },
                status = lockMessage,
                primaryAction = if (availability.canUse) {
                    {
                        promptDismissed = false
                        lockMessage = null
                        promptRequest++
                    }
                } else {
                    { AppLockAuthenticator.openDeviceSecuritySettings(context) }
                },
                primaryActionText = if (availability.canUse) "باز کردن قفل" else "تنظیم قفل دستگاه",
                secondaryAction = null,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LockedContent(
    title: String,
    subtitle: String,
    status: String?,
    primaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    primaryActionText: String = "ادامه",
    secondaryAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            accent = Gold,
            contentPadding = PaddingValues(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (primaryAction == null) Icons.Outlined.Security else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Gold
                )
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                if (!status.isNullOrBlank()) {
                    Text(status, color = TextMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (primaryAction != null) {
                    GoldPrimaryButton(
                        text = primaryActionText,
                        onClick = primaryAction,
                        icon = Icons.Outlined.Fingerprint,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (secondaryAction != null) {
                    SecondaryGlassButton(
                        text = "تنظیمات امنیتی دستگاه",
                        onClick = secondaryAction,
                        icon = Icons.Outlined.Security,
                        accent = AppCyan,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
