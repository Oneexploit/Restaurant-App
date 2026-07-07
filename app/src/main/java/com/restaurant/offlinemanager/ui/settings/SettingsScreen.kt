package com.restaurant.offlinemanager.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.FileDownload
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
import com.restaurant.offlinemanager.ui.AppUiState

@Composable
fun SettingsScreen(
    state: AppUiState,
    context: Context,
    onDarkMode: (Boolean) -> Unit,
    onAppLock: (Boolean) -> Unit,
    onLowStockNotifications: (Boolean) -> Unit,
    onExportBackup: (Context) -> Unit,
    onRestoreBackup: (Context, Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmRestore by remember { mutableStateOf(false) }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onRestoreBackup(context, uri)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("تنظیمات") }
        item {
            SettingsSection("ظاهر برنامه") {
                SettingSwitch(
                    title = "حالت تاریک",
                    subtitle = "طراحی اصلی برنامه همیشه تیره و لوکس است.",
                    checked = state.settings.darkMode,
                    onCheckedChange = onDarkMode
                )
            }
        }
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
                    subtitle = "قفل محلی برنامه برای نسخه آفلاین.",
                    checked = state.settings.appLockEnabled,
                    onCheckedChange = onAppLock
                )
                Text("شماره کارت‌ها در همه بخش‌ها ماسک می‌شوند.", color = TextMuted)
            }
        }
        item {
            SettingsSection("هشدارها") {
                SettingSwitch(
                    title = "هشدار کمبود موجودی",
                    subtitle = "اقلام زیر حداقل موجودی در داشبورد نمایش داده می‌شوند.",
                    checked = state.settings.lowStockNotificationsEnabled,
                    onCheckedChange = onLowStockNotifications
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
                        Text("مدیریت غذای شرکتی • نسخه ۱.۰.۰", color = TextSecondary)
                        Text("کاملا آفلاین، تک‌کاربره، بدون ورود و بدون سرور", color = Gold)
                    }
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
