package com.restaurant.offlinemanager.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

private data class PressMotion(
    val interactionSource: MutableInteractionSource,
    val modifier: Modifier
)

@Composable
private fun rememberPressMotion(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f
): PressMotion {
    val motionEnabled = LocalMotionEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (motionEnabled && enabled && pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.56f,
        animationSpec = tween(180),
        label = "pressAlpha"
    )
    return PressMotion(
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    )
}

@Composable
private fun Modifier.cardEntrance(): Modifier {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) return this
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entered = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "cardAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.985f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )
    return graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
        translationY = (1f - alpha) * 18f
    }
}

@Composable
fun MotionVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) {
        if (visible) Box(modifier) { content() }
        return
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(220)) + expandVertically(tween(260)) + slideInVertically(tween(260)) { it / 5 },
        exit = fadeOut(tween(140)) + shrinkVertically(tween(220)) + slideOutVertically(tween(180)) { -it / 8 }
    ) { content() }
}

@Composable
fun <T> MotionContent(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) {
        Box(modifier) { content(targetState) }
        return
    }
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 7 }) togetherWith
                (fadeOut(tween(140)) + slideOutVertically(tween(200)) { -it / 9 })
        },
        label = "motionContent"
    ) { state -> content(state) }
}

@Composable
private fun AnimatedPremiumBackdrop(modifier: Modifier = Modifier) {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        BackgroundStart,
                        BackgroundMid,
                        BackgroundEnd
                    )
                )
            )
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        AppCyan.copy(alpha = 0.035f),
                        Color.Transparent,
                        Gold.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        return
    }
    val transition = rememberInfiniteTransition(label = "premiumBackdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdropDrift"
    )
    val glow by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdropGlow"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    BackgroundStart,
                    BackgroundMid,
                    BackgroundEnd
                )
            )
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    AppCyan.copy(alpha = 0.055f * glow),
                    Color.Transparent,
                    Gold.copy(alpha = 0.045f * glow),
                    Color.Transparent
                ),
                start = Offset(size.width * (-0.35f + drift * 0.35f), 0f),
                end = Offset(size.width * (0.55f + drift * 0.45f), size.height)
            )
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    AppPurple.copy(alpha = 0.04f * glow),
                    Color.Transparent
                ),
                start = Offset(size.width, size.height * (0.08f + drift * 0.14f)),
                end = Offset(0f, size.height * (0.78f + drift * 0.08f))
            )
        )
    }
}

@Composable
fun AppScaffold(
    title: String,
    currentRoute: String?,
    bottomDestinations: List<BottomDestination>,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    motionEnabled: Boolean = true,
    isBusy: Boolean = false,
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
        onBack = onBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        motionEnabled = motionEnabled,
        isBusy = isBusy,
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
    onBack: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    motionEnabled: Boolean = true,
    isBusy: Boolean = false,
    floatingAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        Box(modifier = modifier.fillMaxSize()) {
            AnimatedPremiumBackdrop()
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    PremiumTopBar(
                        title = title,
                        onOpenSettings = onOpenSettings,
                        onOpenSearch = onOpenSearch,
                        onBack = onBack
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
                        PremiumSnackbarHost(snackbarHostState)
                    }
                },
                floatingActionButton = { floatingAction?.invoke() },
                content = content
            )
            MotionVisibility(
                visible = isBusy,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 66.dp, start = 22.dp, end = 22.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    color = Gold,
                    trackColor = SurfaceGlassStrong
                )
            }
        }
    }
}

@Composable
private fun PremiumSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) { data ->
        val message = data.visuals.message
        val isError = listOf("خطا", "نامعتبر", "نمی‌تواند", "الزامی", "کافی نیست").any { message.contains(it) }
        val isDelete = listOf("حذف", "آرشیو").any { message.contains(it) }
        val accent = when {
            isError || isDelete -> AppRed
            listOf("ذخیره", "ثبت", "انجام", "فعال", "خروجی", "بازیابی").any { message.contains(it) } -> AppGreen
            else -> Gold
        }
        val icon = when {
            isError -> Icons.Outlined.ErrorOutline
            isDelete -> Icons.Outlined.Warning
            else -> Icons.Outlined.CheckCircle
        }
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            accent = accent,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = accent)
                Text(
                    text = message,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedPremiumBackdrop()
        content()
    }
}

@Composable
fun AppTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onBack: (() -> Unit)? = null
) = PremiumTopBar(title, onOpenSettings, onOpenSearch, onBack)

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    val motion = rememberPressMotion()
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(AppDimens.MinimumTouchTarget)
            .then(motion.modifier)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        SurfaceGlassStrong.copy(alpha = 0.72f),
                        SurfaceGlass.copy(alpha = 0.45f)
                    )
                )
            )
            .border(1.dp, BorderSoft.copy(alpha = 0.68f), shape)
            .clickable(
                interactionSource = motion.interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

@Composable
fun PremiumTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AppDimens.ScreenPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            TopBarIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "بازگشت", onBack)
        } else {
            AppLogoMark(modifier = Modifier.size(46.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "topBarTitle"
            ) { currentTitle ->
                Text(
                    text = currentTitle,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = PersianDateFormatter.formatLong(PersianDateFormatter.nowMillis()),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TopBarIconButton(Icons.Outlined.Search, "جستجو", onOpenSearch, tint = TextSecondary)
        Spacer(Modifier.width(8.dp))
        TopBarIconButton(Icons.Outlined.Menu, "منو", onOpenSettings)
    }
}

@Composable
fun AppLogoMark(
    modifier: Modifier = Modifier,
    showGlow: Boolean = true
) {
    val motionEnabled = LocalMotionEnabled.current
    val shape = RoundedCornerShape(18.dp)
    val transition = rememberInfiniteTransition(label = "logoPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulseScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                val animatedPulse = if (showGlow && motionEnabled) pulse else 1f
                scaleX = animatedPulse
                scaleY = animatedPulse
            }
            .then(
                if (showGlow) {
                    Modifier.shadow(
                        elevation = (14f * (if (motionEnabled) pulse else 1f)).dp,
                        shape = shape,
                        ambientColor = Gold.copy(alpha = 0.24f),
                        spotColor = AppCyan.copy(alpha = 0.18f)
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
        modifier = modifier
            .cardEntrance()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.36f),
                spotColor = (accent ?: Gold).copy(alpha = 0.16f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, (accent ?: BorderSoft).copy(alpha = if (accent == null) 0.72f else 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SurfaceGlass.copy(alpha = 0.97f),
                            SurfaceGlassStrong.copy(alpha = 0.91f),
                            BackgroundStart.copy(alpha = 0.38f)
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                (accent ?: Gold).copy(alpha = 0.72f),
                                Color.Transparent
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            )
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
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
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 2 }) togetherWith
                            (fadeOut(tween(130)) + slideOutVertically(tween(180)) { -it / 2 })
                    },
                    label = "statValue"
                ) { targetValue ->
                    Text(
                        targetValue,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        (fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 2 }) togetherWith
                            (fadeOut(tween(120)) + slideOutVertically(tween(170)) { -it / 2 })
                    },
                    label = "miniStatValue"
                ) { targetValue ->
                    Text(targetValue, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
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
    val motion = rememberPressMotion(pressedScale = 0.965f)
    GlassCard(
        modifier = modifier
            .heightIn(min = 86.dp)
            .then(motion.modifier)
            .clickable(
                interactionSource = motion.interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
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
    val motionEnabled = LocalMotionEnabled.current
    val transition = rememberInfiniteTransition(label = "iconBubble")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconBubblePulse"
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                val animatedPulse = if (motionEnabled) pulse else 1f
                scaleX = 0.98f + (animatedPulse * 0.02f)
                scaleY = 0.98f + (animatedPulse * 0.02f)
            }
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.30f * (if (motionEnabled) pulse else 1f)), accent.copy(alpha = 0.08f)))),
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
    val motionEnabled = LocalMotionEnabled.current
    val shape = RoundedCornerShape(AppDimens.ButtonRadius)
    val motion = rememberPressMotion(enabled = enabled, pressedScale = 0.965f)
    val shimmer = rememberInfiniteTransition(label = "primaryButtonShimmer")
    val shimmerProgress by shimmer.animateFloat(
        initialValue = -0.65f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "buttonShimmerProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .then(motion.modifier)
            .shadow(14.dp, shape, ambientColor = Gold.copy(alpha = 0.18f), spotColor = Gold.copy(alpha = 0.18f))
            .clip(shape)
            .background(
                if (enabled) Brush.verticalGradient(listOf(GoldLight, Gold))
                else Brush.verticalGradient(listOf(BorderSoft, SurfaceGlassStrong))
            )
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.18f else 0.06f), shape)
            .clickable(
                interactionSource = motion.interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (enabled && motionEnabled) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val x = size.width * shimmerProgress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        start = Offset(x, 0f),
                        end = Offset(x + size.width * 0.32f, size.height)
                    )
                )
            }
        }
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
    val motion = rememberPressMotion(pressedScale = 0.97f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.ButtonHeight)
            .then(motion.modifier)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        SurfaceGlassStrong,
                        accent.copy(alpha = 0.12f),
                        SurfaceGlass.copy(alpha = 0.82f)
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.36f), shape)
            .clickable(
                interactionSource = motion.interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
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

@Composable
fun FormActionFooter(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.CheckCircle,
    enabled: Boolean = true,
    accent: Color = Gold
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accent = accent,
        contentPadding = PaddingValues(10.dp)
    ) {
        GoldPrimaryButton(
            text = text,
            onClick = onClick,
            icon = icon,
            enabled = enabled
        )
    }
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
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusElevation by animateDpAsState(
        targetValue = if (focused || isError) 8.dp else 0.dp,
        animationSpec = tween(180),
        label = "fieldElevation"
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = focusElevation,
                shape = RoundedCornerShape(AppDimens.SmallRadius),
                ambientColor = if (isError) AppRed.copy(alpha = 0.16f) else Gold.copy(alpha = 0.14f),
                spotColor = if (isError) AppRed.copy(alpha = 0.14f) else AppCyan.copy(alpha = 0.10f)
            )
            .heightIn(min = AppDimens.InputHeight),
        label = { Text(label) },
        singleLine = singleLine,
        readOnly = readOnly,
        isError = isError,
        supportingText = supportingText?.let { text -> { Text(text) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interactionSource,
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
    val motionEnabled = LocalMotionEnabled.current
    val haptics = LocalHapticFeedback.current
    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp)
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
            val animatedIconScale by animateFloatAsState(
                targetValue = if (selected) 1.16f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "bottomIconScale"
            )
            val animatedIconColor by animateColorAsState(
                targetValue = if (selected) Gold else TextMuted,
                animationSpec = tween(220),
                label = "bottomIconColor"
            )
            val iconScale = if (motionEnabled) animatedIconScale else if (selected) 1.08f else 1f
            val iconColor = if (motionEnabled) animatedIconColor else if (selected) Gold else TextMuted
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigate(destination.route)
                    }
                },
                alwaysShowLabel = selected,
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = destination.label,
                        tint = iconColor,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    )
                },
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
    val chipColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(220),
        label = "chipColor"
    )
    Box(
        modifier = modifier
            .heightIn(min = 34.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(chipColor.copy(alpha = 0.14f))
            .border(1.dp, chipColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = chipColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MoneyText(
    amount: Long,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge
) {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) {
        Text(
            text = MoneyFormatter.format(amount),
            modifier = modifier,
            color = color,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        return
    }
    AnimatedContent(
        targetState = amount,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState >= initialState) 1 else -1
            (fadeIn(tween(240)) + slideInVertically(tween(300)) { direction * it / 2 }) togetherWith
                (fadeOut(tween(150)) + slideOutVertically(tween(220)) { -direction * it / 2 })
        },
        label = "moneyText"
    ) { targetAmount ->
        Text(
            text = MoneyFormatter.format(targetAmount),
            color = color,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
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
fun MetricProgressBar(
    label: String,
    value: Float,
    max: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    valueLabel: String? = null
) {
    val motionEnabled = LocalMotionEnabled.current
    val target = if (max <= 0f) 0f else (value / max).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(520),
        label = "metricProgress"
    )
    val progress = if (motionEnabled) animatedProgress else target
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(
                valueLabel ?: "${NumberFormatter.format((target * 100).toDouble())}٪",
                color = accent,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlassStrong)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(accent, GoldLight.copy(alpha = 0.82f))))
            )
        }
    }
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
    modifier: Modifier = Modifier,
    message: String = "در حال بارگذاری..."
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
fun LocalDateSelector(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DarkOutlinedTextField(
            value = PersianDateFormatter.format(value),
            onValueChange = {},
            label = label,
            readOnly = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryGlassButton(
                text = "روز قبل",
                onClick = { onValueChange(PersianDateFormatter.shiftDays(value, -1)) },
                modifier = Modifier.weight(1f),
                accent = AppCyan
            )
            SecondaryGlassButton(
                text = "امروز",
                onClick = { onValueChange(PersianDateFormatter.todayStartMillis()) },
                modifier = Modifier.weight(1f),
                accent = Gold
            )
            SecondaryGlassButton(
                text = "روز بعد",
                onClick = { onValueChange(PersianDateFormatter.shiftDays(value, 1)) },
                modifier = Modifier.weight(1f),
                accent = AppCyan
            )
        }
    }
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
        value = NumberFormatter.formatMoneyInput(value),
        onValueChange = { onValueChange(NumberFormatter.formatMoneyInput(it)) },
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
    modifier: Modifier = Modifier,
    allowNegative: Boolean = false
) {
    QuantityInputField(value = value, onValueChange = onValueChange, label = label, modifier = modifier, allowNegative = allowNegative)
}

@Composable
fun QuantityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowNegative: Boolean = false
) {
    DarkOutlinedTextField(
        value = NumberFormatter.formatQuantityInput(value, allowNegative),
        onValueChange = { onValueChange(NumberFormatter.formatQuantityInput(it, allowNegative)) },
        label = label,
        modifier = modifier,
        keyboardType = if (allowNegative) KeyboardType.Text else KeyboardType.Decimal
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
            val isSelected = selected == option
            val chipScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.96f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "filterChipScale"
            )
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option) },
                modifier = Modifier.graphicsLayer {
                    scaleX = chipScale
                    scaleY = chipScale
                },
                label = { Text(option, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold.copy(alpha = 0.18f),
                    selectedLabelColor = Gold,
                    containerColor = SurfaceGlassStrong,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
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
    clearLabel: String? = null,
    onClear: (() -> Unit)? = null,
    onSelected: (T) -> Unit
) {
    AppDropdownField(
        label = label,
        options = options,
        selected = selected,
        optionLabel = optionLabel,
        modifier = modifier,
        clearLabel = clearLabel,
        onClear = onClear,
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
    clearLabel: String? = null,
    onClear: (() -> Unit)? = null,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(180),
        label = "dropdownArrowRotation"
    )
    Box(modifier = modifier.fillMaxWidth()) {
        DarkOutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = options.isNotEmpty()) { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SurfaceGlassStrong)
                .heightIn(max = 320.dp)
        ) {
            if (onClear != null && clearLabel != null) {
                DropdownMenuItem(
                    text = { Text(clearLabel, color = TextSecondary) },
                    onClick = {
                        onClear()
                        expanded = false
                    }
                )
            }
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
    val motion = rememberPressMotion(enabled = onClick != null, pressedScale = 0.985f)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    motion.modifier.clickable(
                        interactionSource = motion.interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
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
    val motion = rememberPressMotion(enabled = onClick != null, pressedScale = 0.985f)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    motion.modifier.clickable(
                        interactionSource = motion.interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
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
    val motion = rememberPressMotion(enabled = onClick != null, pressedScale = 0.985f)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    motion.modifier.clickable(
                        interactionSource = motion.interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
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
