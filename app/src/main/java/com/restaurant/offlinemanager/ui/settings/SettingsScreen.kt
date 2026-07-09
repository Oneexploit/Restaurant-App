package com.restaurant.offlinemanager.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.core.design.AppLogoMark
import com.restaurant.offlinemanager.core.design.ConfirmDialog
import com.restaurant.offlinemanager.core.design.GlassCard
import com.restaurant.offlinemanager.core.design.Gold
import com.restaurant.offlinemanager.core.design.GoldPrimaryButton
import com.restaurant.offlinemanager.core.design.SectionHeader
import com.restaurant.offlinemanager.core.design.TextMuted
import com.restaurant.offlinemanager.core.design.TextPrimary
import com.restaurant.offlinemanager.core.design.TextSecondary
import com.restaurant.offlinemanager.core.notifications.AppNotificationManager
import com.restaurant.offlinemanager.core.security.AppLockAuthenticator
import com.restaurant.offlinemanager.core.security.findFragmentActivity
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun SettingsScreen(
    state: AppUiState,
    context: Context,
    onAppLock: (Boolean) -> Unit,
    onImportantNotifications: (Context, Boolean) -> Unit,
    onLowStockNotifications: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onExportBackup: (Context) -> Unit,
    onRestoreBackup: (Context, Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmRestore by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showSecuritySetupDialog by remember { mutableStateOf(false) }
    var securityMessage by remember { mutableStateOf<String?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    val appLockAvailability = AppLockAuthenticator.availability(context)
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) selectedRestoreUri = uri
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationMessage = if (granted) {
            onImportantNotifications(context, true)
            "مجوز نوتیفیکیشن صادر شد."
        } else {
            onImportantNotifications(context, false)
            "مجوز نوتیفیکیشن داده نشد."
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("تنظیمات") }
        item {
            SettingsSection("اطلاعات کسب‌وکار") {
                Text("مدیریت غذای شرکتی و رستوران آفلاین", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("ساختار داده‌ها محلی است و برای استفاده روزانه بدون اینترنت طراحی شده است.", color = TextSecondary)
                Text("نام، لوگو و جزئیات رسمی کسب‌وکار می‌تواند در نسخه بعدی قابل ویرایش شود.", color = TextMuted)
            }
        }
        item {
            SettingsSection("پشتیبان‌گیری") {
                GoldPrimaryButton("خروجی JSON پشتیبان", onClick = { onExportBackup(context) }, icon = Icons.Outlined.FileDownload)
                Spacer(Modifier.height(10.dp))
                GoldPrimaryButton(
                    "بازیابی از فایل JSON",
                    onClick = { confirmRestore = true },
                    icon = Icons.Outlined.UploadFile
                )
            }
        }
        item {
            SettingsSection("امنیت") {
                SettingSwitch(
                    title = "قفل برنامه",
                    subtitle = "هنگام باز شدن یا برگشت از پس‌زمینه، برنامه با قفل امن گوشی باز می‌شود.",
                    checked = state.settings.appLockEnabled,
                    onCheckedChange = { enabled ->
                        val activity = context.findFragmentActivity()
                        securityMessage = null
                        when {
                            enabled && !appLockAvailability.canUse -> showSecuritySetupDialog = true
                            !enabled && !appLockAvailability.canUse -> onAppLock(false)
                            activity == null -> securityMessage = "امکان اجرای پنجره احراز هویت روی این دستگاه پیدا نشد."
                            else -> AppLockAuthenticator.authenticate(
                                activity = activity,
                                title = if (enabled) "فعال‌سازی قفل برنامه" else "غیرفعال‌سازی قفل برنامه",
                                subtitle = "برای تغییر وضعیت قفل، هویت خود را تایید کنید",
                                onSuccess = { onAppLock(enabled) },
                                onError = { securityMessage = it }
                            )
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                Text("روش‌های شناسایی‌شده: ${appLockAvailability.supportedMethodsLabel}", color = TextSecondary)
                Text(appLockAvailability.statusLabel, color = TextMuted)
                securityMessage?.let { Text(it, color = Gold) }
                if (appLockAvailability.needsDeviceSetup) {
                    Spacer(Modifier.height(10.dp))
                    GoldPrimaryButton(
                        text = "تنظیم قفل دستگاه",
                        onClick = { AppLockAuthenticator.openDeviceSecuritySettings(context) },
                        icon = Icons.Outlined.Security
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("شماره کارت‌ها در همه بخش‌ها ماسک می‌شوند.", color = TextMuted)
                Text("داده‌ها فقط در حافظه محلی دستگاه ذخیره می‌شوند و بکاپ ابری سیستم غیرفعال است.", color = TextSecondary)
            }
        }
        item {
            SettingsSection("هشدارها") {
                SettingSwitch(
                    title = "نوتیفیکیشن‌های مهم گوشی",
                    subtitle = "وقتی برنامه بسته است، یادآوری‌های آفلاین مثل کمبود موجودی، مطالبات و بدهی‌ها ارسال می‌شود.",
                    checked = state.settings.importantNotificationsEnabled && AppNotificationManager.canPostNotifications(context),
                    onCheckedChange = { enabled ->
                        notificationMessage = null
                        if (!enabled) {
                            onImportantNotifications(context, false)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !AppNotificationManager.hasRuntimePermission(context)) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else if (!AppNotificationManager.canPostNotifications(context)) {
                            notificationMessage = "نوتیفیکیشن از تنظیمات سیستم گوشی غیرفعال است."
                            AppNotificationManager.openNotificationSettings(context)
                        } else {
                            onImportantNotifications(context, true)
                        }
                    }
                )
                notificationMessage?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = TextMuted)
                }
                Spacer(Modifier.height(10.dp))
                SettingSwitch(
                    title = "هشدار کمبود موجودی",
                    subtitle = "اقلام زیر حداقل موجودی در داشبورد نمایش داده می‌شوند.",
                    checked = state.settings.lowStockNotificationsEnabled,
                    onCheckedChange = onLowStockNotifications
                )
            }
        }
        item {
            SettingsSection("نمایش و حرکت") {
                SettingSwitch(
                    title = "کاهش انیمیشن‌ها",
                    subtitle = "برای استفاده طولانی یا دستگاه‌های ضعیف، حرکت‌های تزئینی کم می‌شود.",
                    checked = state.settings.reducedMotionEnabled,
                    onCheckedChange = onReducedMotion
                )
            }
        }
        item {
            SettingsSection("خروجی‌ها") {
                Text("گزارش‌های CSV از بخش گزارش‌ها دریافت می‌شوند.", color = TextSecondary)
                Text("خروجی خریدها، موجودی، مطالبات، بدهی تامین‌کنندگان و پرداخت‌ها در دسترس است.", color = TextMuted)
            }
        }
        item {
            SettingsSection("اطلاعات برنامه") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AppLogoMark(modifier = Modifier.size(64.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restaurant Offline Manager", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("مدیریت غذای شرکتی • نسخه ۱.۱.۱", color = TextSecondary)
                        Text("کاملا آفلاین، تک‌کاربره، بدون ورود و بدون سرور", color = Gold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("طراحی و توسعه توسط امیرحسین نفر", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("مهندس نرم‌افزار و مهندس امنیت سایبری با بیش از ۸ سال تجربه.", color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoldPrimaryButton(
                        text = "LinkedIn",
                        onClick = { openExternalUrl(context, "https://www.linkedin.com/in/amirhosein-nafar/") },
                        icon = Icons.Outlined.Business,
                        modifier = Modifier.weight(1f)
                    )
                    GoldPrimaryButton(
                        text = "GitHub",
                        onClick = { openExternalUrl(context, "https://github.com/Oneexploit/") },
                        icon = Icons.Outlined.Search,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    if (confirmRestore) {
        ConfirmDialog(
            title = "بازیابی پشتیبان",
            message = "قبل از بازیابی مطمئن شوید فایل درست را انتخاب می‌کنید. اطلاعات فعلی ممکن است با داده‌های فایل جایگزین شود.",
            confirmText = "انتخاب فایل",
            onConfirm = {
                confirmRestore = false
                restoreLauncher.launch(arrayOf("application/json", "text/*"))
            },
            onDismiss = { confirmRestore = false }
        )
    }
    if (showSecuritySetupDialog) {
        ConfirmDialog(
            title = "قفل دستگاه آماده نیست",
            message = "برای فعال کردن قفل برنامه باید روی گوشی اثر انگشت، تشخیص چهره امن، PIN، الگو یا رمز عبور تنظیم شده باشد.",
            confirmText = "تنظیمات امنیتی",
            onConfirm = {
                showSecuritySetupDialog = false
                AppLockAuthenticator.openDeviceSecuritySettings(context)
            },
            onDismiss = { showSecuritySetupDialog = false }
        )
    }
    selectedRestoreUri?.let { uri ->
        ConfirmDialog(
            title = "تایید فایل پشتیبان",
            message = "فایل انتخاب‌شده: ${uri.lastPathSegment.orEmpty()}\nقبل از بازیابی، یک پشتیبان خودکار از اطلاعات فعلی ساخته می‌شود.",
            confirmText = "بازیابی",
            onConfirm = {
                selectedRestoreUri = null
                onRestoreBackup(context, uri)
            },
            onDismiss = { selectedRestoreUri = null }
        )
    }
}

private fun openExternalUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(Modifier.fillMaxWidth(), accent = Gold.copy(alpha = 0.75f)) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
