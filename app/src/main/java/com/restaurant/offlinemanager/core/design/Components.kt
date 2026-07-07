package com.restaurant.offlinemanager.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.restaurant.offlinemanager.R
import com.restaurant.offlinemanager.core.navigation.BottomDestination
import com.restaurant.offlinemanager.core.utils.MoneyFormatter
import com.restaurant.offlinemanager.core.utils.NumberFormatter
import com.restaurant.offlinemanager.core.utils.PersianDateFormatter

@Composable
fun AppScaffold(
    title: String,
    currentRoute: String?,
    bottomDestinations: List<BottomDestination>,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    floatingAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    PremiumScaffold(
        title = title,
        currentRoute = currentRoute,
        bottomDestinations = bottomDestinations,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings,
        onOpenSearch = onOpenSearch,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingAction = floatingAction,
        content = content
    )
}

@Composable
fun PremiumScaffold(
    title: String,
    currentRoute: String?,
    bottomDestinations: List<BottomDestination>,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    floatingAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.appBackground(),
        containerColor = Color.Transparent,
        topBar = {
            PremiumTopBar(
                title = title,
                onOpenSettings = onOpenSettings,
                onOpenSearch = onOpenSearch
            )
        },
        bottomBar = {
            PremiumBottomNavigation(
                destinations = bottomDestinations,
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        },
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(snackbarHostState)
            }
        },
        floatingActionButton = { floatingAction?.invoke() },
        content = content
    )
}

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackgroundBrush())
    ) {
        content()
    }
}

@Composable
fun AppTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit
) = PremiumTopBar(title, onOpenSettings, onOpenSearch)

@Composable
fun PremiumTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppLogoMark(modifier = Modifier.size(46.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = PersianDateFormatter.formatLong(PersianDateFormatter.nowMillis()),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onOpenSearch, modifier = Modifier.size(AppDimens.MinimumTouchTarget)) {
            Icon(Icons.Outlined.Search, contentDescription = "جستجو", tint = TextSecondary)
        }
        IconButton(onClick = { }, modifier = Modifier.size(AppDimens.MinimumTouchTarget)) {
            Icon(Icons.Outlined.Notifications, contentDescription = "اعلان ها", tint = Gold)
        }
        IconButton(onClick = onOpenSettings, modifier = Modifier.size(AppDimens.MinimumTouchTarget)) {
            Icon(Icons.Outlined.Menu, contentDescription = "منو", tint = TextPrimary)
        }
    }
}

@Composable
fun AppLogoMark(
    modifier: Modifier = Modifier,
    showGlow: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .then(
                if (showGlow) {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = shape,
                        ambientColor = Gold.copy(alpha = 0.22f),
                        spotColor = Gold.copy(alpha = 0.28f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(SurfaceGlassStrong, BackgroundStart)))
            .border(1.dp, Gold.copy(alpha = 0.55f), shape)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "لوگوی برنامه",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(AppDimens.CardPadding),
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.CardRadius)
    Card(
        modifier = modifier.shadow(
            elevation = 16.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.34f),
            spotColor = (accent ?: Gold).copy(alpha = 0.12f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, (accent ?: BorderSoft).copy(alpha = if (accent == null) 0.72f else 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SurfaceGlass.copy(alpha = 0.98f),
                            SurfaceGlassStrong.copy(alpha = 0.9f),
                            BackgroundStart.copy(alpha = 0.32f)
                        )
                    )
                )
                .background(
                    Brush.linearGradient(
                        listOf(
                            (accent ?: Color.White).copy(alpha = if (accent == null) 0.035f else 0.13f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.018f)
                        )
                    )
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun ElevatedGlassCard(
    modifier: Modifier = Modifier,
    accent: Color = Gold,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(
        modifier = modifier.shadow(
            elevation = 24.dp,
            shape = RoundedCornerShape(AppDimens.CardRadiusLarge),
            ambientColor = Color.Black.copy(alpha = 0.48f),
            spotColor = accent.copy(alpha = 0.18f)
        ),
        accent = accent,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    GlassCard(modifier = modifier.heightIn(min = 112.dp), accent = accent.copy(alpha = 0.9f)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IconBubble(icon = icon, accent = accent)
            Column {
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    value,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    GlassCard(
        modifier = modifier.heightIn(min = 84.dp),
        accent = accent,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (icon != null) IconBubble(icon = icon, accent = accent, size = 34.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ActionCard(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    GlassCard(
        modifier = modifier
            .heightIn(min = 86.dp)
            .clickable(onClick = onClick),
        accent = accent,
        contentPadding = PaddingValues(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent)
        Spacer(Modifier.height(8.dp))
        Text(label, color = TextPrimary, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) {
            Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
private fun IconBubble(
    icon: ImageVector,
    accent: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.08f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent)
    }
}

@Composable
fun GoldPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Add,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(AppDimens.ButtonRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .shadow(14.dp, shape, ambientColor = Gold.copy(alpha = 0.18f), spotColor = Gold.copy(alpha = 0.18f))
            .clip(shape)
            .background(
                if (enabled) Brush.verticalGradient(listOf(GoldLight, Gold))
                else Brush.verticalGradient(listOf(BorderSoft, SurfaceGlassStrong))
            )
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.18f else 0.06f), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (enabled) BackgroundStart else TextMuted)
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = if (enabled) BackgroundStart else TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SecondaryGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = Gold
) {
    val shape = RoundedCornerShape(AppDimens.ButtonRadius)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .clip(shape)
            .background(SurfaceGlassStrong)
            .border(1.dp, accent.copy(alpha = 0.36f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = TextPrimary, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Warning
) {
    SecondaryGlassButton(text = text, onClick = onClick, modifier = modifier, icon = icon, accent = AppRed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppDimens.InputHeight),
        label = { Text(label) },
        singleLine = singleLine,
        readOnly = readOnly,
        isError = isError,
        supportingText = supportingText?.let { text -> { Text(text) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(AppDimens.SmallRadius),
        colors = TextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Gold,
            focusedIndicatorColor = Gold,
            unfocusedIndicatorColor = BorderSoft,
            errorIndicatorColor = AppRed,
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextSecondary,
            errorLabelColor = AppRed,
            focusedContainerColor = SurfaceGlassStrong,
            unfocusedContainerColor = SurfaceGlassStrong,
            disabledContainerColor = SurfaceGlassStrong,
            errorContainerColor = SurfaceGlassStrong,
            focusedTrailingIconColor = Gold,
            unfocusedTrailingIconColor = TextMuted,
            errorTrailingIconColor = AppRed
        )
    )
}

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "جستجو"
) {
    DarkOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        trailingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMuted) }
    )
}

@Composable
fun AppBottomNavigation(
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) = PremiumBottomNavigation(destinations, currentRoute, onNavigate)

@Composable
fun PremiumBottomNavigation(
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .heightIn(min = AppDimens.BottomNavHeight)
            .shadow(18.dp, RoundedCornerShape(AppDimens.BottomNavRadius), ambientColor = Color.Black.copy(alpha = 0.45f), spotColor = Gold.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(AppDimens.BottomNavRadius))
            .background(SurfaceGlass.copy(alpha = 0.92f))
            .border(1.dp, BorderSoft.copy(alpha = 0.72f), RoundedCornerShape(AppDimens.BottomNavRadius)),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = {
                    Text(
                        destination.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Gold,
                    selectedTextColor = Gold,
                    indicatorColor = Gold.copy(alpha = 0.18f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AssistChip(
        modifier = modifier.heightIn(min = 34.dp),
        onClick = {},
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.14f),
            labelColor = color
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    )
}

@Composable
fun MoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge
) {
    Text(
        text = MoneyFormatter.format(amount),
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PersianDateText(
    timestamp: Long,
    modifier: Modifier = Modifier,
    long: Boolean = false,
    color: Color = TextSecondary
) {
    Text(
        text = if (long) PersianDateFormatter.formatLong(timestamp) else PersianDateFormatter.format(timestamp),
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PercentChangeText(
    percent: Double,
    modifier: Modifier = Modifier
) {
    val positive = percent >= 0
    Text(
        text = "${if (positive) "+" else ""}${NumberFormatter.format(percent)}٪",
        modifier = modifier,
        color = if (positive) AppGreen else AppRed,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    EmptyStateCard(title = title, message = message, modifier = modifier)
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Search
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = TextMuted.copy(alpha = 0.8f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(icon = icon, accent = TextMuted)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LoadingState(
    message: String = "در حال بارگذاری...",
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = Gold) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = Gold, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ErrorStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = AppRed) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = AppRed)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = Gold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = TextSecondary) }
        },
        title = { Text(title, color = TextPrimary) },
        text = { Text(message, color = TextSecondary) },
        containerColor = SurfaceGlassStrong,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetActionMenu(
    title: String,
    actions: List<SheetAction>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceGlassStrong,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "بستن", tint = TextSecondary)
                }
            }
            actions.forEach { action ->
                SecondaryGlassButton(
                    text = action.label,
                    icon = action.icon,
                    accent = action.accent,
                    onClick = {
                        onDismiss()
                        action.onClick()
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

data class SheetAction(
    val label: String,
    val icon: ImageVector,
    val accent: Color = Gold,
    val onClick: () -> Unit
)

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    DarkOutlinedTextField(value = value, onValueChange = onValueChange, label = label, modifier = modifier)
}

@Composable
fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    MoneyInputField(value = value, onValueChange = onValueChange, label = label, modifier = modifier)
}

@Composable
fun MoneyInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    DarkOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Number
    )
}

@Composable
fun QuantityField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    QuantityInputField(value = value, onValueChange = onValueChange, label = label, modifier = modifier)
}

@Composable
fun QuantityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    DarkOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Decimal
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = Gold, maxLines = 1)
            }
        }
    }
}

@Composable
fun AppSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "جستجو"
) {
    GlassCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp), accent = AppCyan.copy(alpha = 0.65f)) {
        SearchTextField(value = value, onValueChange = onValueChange, label = label)
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "جستجو"
) = AppSearchBar(value = value, onValueChange = onValueChange, modifier = modifier, label = label)

@Composable
fun FilterChipRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold.copy(alpha = 0.18f),
                    selectedLabelColor = Gold,
                    containerColor = SurfaceGlassStrong,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == option,
                    borderColor = BorderSoft,
                    selectedBorderColor = Gold.copy(alpha = 0.55f)
                )
            )
        }
    }
}

@Composable
fun <T> OptionSelector(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    onSelected: (T) -> Unit
) {
    AppDropdownField(
        label = label,
        options = options,
        selected = selected,
        optionLabel = optionLabel,
        modifier = modifier,
        onSelected = onSelected
    )
}

@Composable
fun <T> AppDropdownField(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        DarkOutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
            },
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SurfaceGlassStrong)
                .heightIn(max = 320.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), color = TextPrimary) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LowStockWarningCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        accent = AppOrange
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = AppOrange)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppOrange, style = MaterialTheme.typography.titleMedium)
                Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun TransactionListItem(
    title: String,
    subtitle: String,
    amount: String,
    date: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    chip: String? = null
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = accent) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon = icon, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(date, color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (chip != null) StatusChip(chip, accent)
                Text(amount, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            }
        }
    }
}

@Composable
fun ProjectCard(
    name: String,
    company: String,
    address: String,
    workerCount: String,
    mealPrice: String,
    receivable: Long,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        accent = statusColor
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Outlined.Business, statusColor, size = 48.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(name, color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    StatusChip(status, statusColor)
                }
                Text(company, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(address, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$workerCount نفر • $mealPrice", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("مانده مطالبات", color = TextSecondary, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    MoneyText(receivable.coerceAtLeast(0), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        actions()
    }
}

@Composable
fun MaterialCard(
    name: String,
    subtitle: String,
    quantity: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    value: Long? = null
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = statusColor) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji ?: "•", style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(quantity, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status, statusColor)
                if (value != null) MoneyText(value, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun PurchaseCard(
    supplier: String,
    subtitle: String,
    total: Long,
    paymentType: String,
    paymentColor: Color,
    date: String,
    modifier: Modifier = Modifier,
    emoji: String? = null
) {
    TransactionListItem(
        title = supplier,
        subtitle = subtitle,
        amount = MoneyFormatter.format(total),
        date = date,
        icon = Icons.Outlined.ShoppingCart,
        accent = paymentColor,
        chip = paymentType,
        modifier = modifier
    )
}

@Composable
fun FinanceSummaryCard(
    title: String,
    amount: Long,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = Icons.Outlined.Payments
) {
    GlassCard(modifier = modifier.heightIn(min = 112.dp), accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(icon = icon, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                MoneyText(amount, color = TextPrimary, style = MaterialTheme.typography.headlineSmall)
                if (subtitle != null) Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        accent = accent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBubble(icon = icon, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (onClick != null) {
                Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = Gold)
            }
        }
    }
}
