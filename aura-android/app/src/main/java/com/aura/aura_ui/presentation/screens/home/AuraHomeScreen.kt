package com.aura.aura_ui.presentation.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.LruCache
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aura.aura_ui.R
import com.aura.aura_ui.data.AppInfo
import com.aura.aura_ui.data.preferences.ThakurSecurityStore
import com.aura.aura_ui.presentation.components.BloodDot
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.RelaxWorkLiquidToggle
import com.aura.aura_ui.presentation.utils.AuraHapticType
import com.aura.aura_ui.presentation.utils.HapticUtils
import com.aura.aura_ui.presentation.utils.rememberHapticFeedback
import com.aura.aura_ui.utils.AppInventoryScanner
import com.aura.aura_ui.utils.isLaunchable
import kotlin.math.abs
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Core-permission health used by the conditional banner. */
data class HomeHealth(
    val accessibilityEnabled: Boolean,
    val overlayGranted: Boolean,
    val micGranted: Boolean,
) {
    val broken: Boolean get() = !accessibilityEnabled || !overlayGranted || !micGranted

    val firstIssue: String
        get() = when {
            !accessibilityEnabled -> "Accessibility service is off"
            !overlayGranted -> "Overlay permission missing"
            !micGranted -> "Microphone permission missing"
            else -> ""
        }
}

/** Re-checks the core permissions every time the screen resumes. */
@Composable
fun rememberHomeHealth(): HomeHealth {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var health by remember { mutableStateOf(checkHealth(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) health = checkHealth(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return health
}

private fun checkHealth(context: Context): HomeHealth {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val accessibilityOn = am.getEnabledAccessibilityServiceList(
        android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
    ).any {
        it.resolveInfo.serviceInfo.packageName == context.packageName &&
            it.resolveInfo.serviceInfo.name.endsWith("AuraAccessibilityService")
    }
    return HomeHealth(
        accessibilityEnabled = accessibilityOn,
        overlayGranted = Settings.canDrawOverlays(context),
        micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED,
    )
}

private object AppIconCache {
    val cache = LruCache<String, ImageBitmap>(180)
}

/**
 * THAKUR OS Home / Launcher screen:
 * 1. Top neon tech frame with T+Crown logo, "THAKUR OS", "• Listening •", voice waveform & settings gear.
 * 2. Full-width Liquid Glass Search Bar ("Search apps...", search icon left, 4-dot grid quick hub right).
 * 3. 5-column installed app grid with purple-blue neon rings over the dark sunset mountain wallpaper.
 * 4. Bottom Liquid Glass Speaking panel with T+Crown glowing ring, "THAKUR OS" + "Speaking...",
 *    live animated blue-purple waveform bars, and circular X button.
 */
@Composable
fun AuraHomeScreen(
    onStartChat: (seedCommand: String?) -> Unit,
    onOpenActivity: () -> Unit,
    onOpenBrowser: () -> Unit,
    onFixHealth: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onOpenAgent: () -> Unit = {},
    onOpenSkills: () -> Unit = {},
    onOpenCommandPalette: () -> Unit = {},
    runActive: Boolean = false,
) {
    val context = LocalContext.current
    val health = rememberHomeHealth()
    val haptic = rememberHapticFeedback()
    remember(context) { ThakurSecurityStore.hydrate(context) }

    val isWorkMode by ThakurSecurityStore.isWorkTheme.collectAsState()
    val primaryGlow by animateColorAsState(
        targetValue = if (isWorkMode) LiquidPalette.WorkOrange else LiquidPalette.SilkViolet,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "launcherPrimaryGlow",
    )
    val secondaryGlow by animateColorAsState(
        targetValue = if (isWorkMode) LiquidPalette.WorkAmber else LiquidPalette.PrismBlue,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "launcherSecondaryGlow",
    )

    var searchQuery by remember { mutableStateOf("") }
    var quickHubExpanded by remember { mutableStateOf(false) }

    // Load all launchable installed apps asynchronously without blocking the main thread
    val installedAppsState by produceState<List<AppInfo>?>(initialValue = null, context) {
        val loaded = withContext(Dispatchers.Default) {
            runCatching {
                AppInventoryScanner(context)
                    .scanInstalledApps()
                    .filter { it.isLaunchable() }
                    .distinctBy { it.packageName }
                    .sortedBy { it.appName.lowercase() }
            }.getOrElse { emptyList() }
        }
        value = loaded
    }

    val filteredApps = remember(installedAppsState, searchQuery) {
        val list = installedAppsState ?: emptyList()
        val q = searchQuery.trim()
        if (q.isEmpty()) list else list.filter { it.appName.contains(q, ignoreCase = true) }
    }

    // Smooth infinite clock for neon pulse and live waveform bars
    val infiniteTransition = rememberInfiniteTransition(label = "launcherAnim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    val chamferShape = remember { launcherChamferShape(14.dp) }

    Box(modifier = modifier.fillMaxSize()) {
        // Dark sunset mountain lake wallpaper
        Image(
            painter = painterResource(R.drawable.thakur_sunset_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Moody top/bottom readability vignette keeping mountain horizon vivid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF040610).copy(alpha = 0.72f),
                        0.28f to Color(0xFF040610).copy(alpha = 0.24f),
                        0.68f to Color(0xFF050712).copy(alpha = 0.26f),
                        1.0f to Color(0xFF040610).copy(alpha = 0.82f),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .padding(horizontal = 12.dp),
        ) {
            Spacer(Modifier.height(6.dp))

            // 1. TOP BAR: T + Crown | THAKUR OS + • Listening • | Waveform + Gear
            ThakurLauncherTopBar(
                shape = chamferShape,
                primaryGlow = primaryGlow,
                secondaryGlow = secondaryGlow,
                wavePhase = wavePhase,
                runActive = runActive,
                onLogoClick = {
                    haptic(AuraHapticType.LIGHT)
                    onOpenDrawer()
                },
                onWaveformClick = {
                    haptic(AuraHapticType.MEDIUM)
                    onStartChat(null)
                },
                onSettingsClick = {
                    haptic(AuraHapticType.LIGHT)
                    onOpenSettings()
                },
            )

            Spacer(Modifier.height(10.dp))

            // 2. SEARCH BAR: "Search apps...", search icon left, grid icon right
            ThakurLauncherSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                quickHubOpen = quickHubExpanded,
                primaryGlow = primaryGlow,
                secondaryGlow = secondaryGlow,
                onGridClick = {
                    haptic(AuraHapticType.LIGHT)
                    quickHubExpanded = !quickHubExpanded
                },
            )

            // Optional expandable Quick Hub triggered by the Grid icon (preserves Relax/Work, Browser, Activity, Agent, Search Palette)
            AnimatedVisibility(
                visible = quickHubExpanded,
                enter = fadeIn(tween(240)) + expandVertically(tween(260)),
                exit = fadeOut(tween(180)) + shrinkVertically(tween(220)),
            ) {
                QuickLauncherHubStrip(
                    isWorkMode = isWorkMode,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    onToggleWork = { next -> ThakurSecurityStore.setWorkTheme(context, next) },
                    onBrowser = {
                        quickHubExpanded = false
                        onOpenBrowser()
                    },
                    onActivity = {
                        quickHubExpanded = false
                        onOpenActivity()
                    },
                    onAgent = {
                        quickHubExpanded = false
                        onOpenAgent()
                    },
                    onSkills = {
                        quickHubExpanded = false
                        onOpenSkills()
                    },
                    onCommandPalette = {
                        quickHubExpanded = false
                        onOpenCommandPalette()
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            // 3. APP GRID: 5 columns with glowing purple-blue neon rings around each icon
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val apps = installedAppsState
                if (apps == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = secondaryGlow,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 14.dp, start = 2.dp, end = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (health.broken) {
                            item(span = { GridItemSpan(5) }) {
                                LauncherHealthBanner(
                                    issue = health.firstIssue,
                                    primaryGlow = primaryGlow,
                                    onFix = onFixHealth,
                                )
                            }
                        }

                        if (filteredApps.isEmpty()) {
                            item(span = { GridItemSpan(5) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 36.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No apps match \"$searchQuery\"",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        } else {
                            items(
                                items = filteredApps,
                                key = { it.packageName },
                            ) { app ->
                                NeonRingAppTile(
                                    app = app,
                                    primaryGlow = primaryGlow,
                                    secondaryGlow = secondaryGlow,
                                    onLaunch = {
                                        haptic(AuraHapticType.LIGHT)
                                        launchInstalledApp(context, app)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 4. BOTTOM BAR: Big Liquid Glass Speaking Panel with T+Crown, "THAKUR OS" + "Speaking...",
            //    live animated blue-purple waveform bars, and circular X close/clear button
            ThakurSpeakingBottomBar(
                shape = chamferShape,
                primaryGlow = primaryGlow,
                secondaryGlow = secondaryGlow,
                wavePhase = wavePhase,
                glowPulse = glowPulse,
                statusText = if (runActive) "Active..." else "Speaking...",
                onPrimaryClick = {
                    haptic(AuraHapticType.RECORDING_START)
                    onStartChat(null)
                },
                onCloseClick = {
                    haptic(AuraHapticType.LIGHT)
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = ""
                    } else if (quickHubExpanded) {
                        quickHubExpanded = false
                    } else {
                        onOpenCommandPalette()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun ThakurLauncherTopBar(
    shape: GenericShape,
    primaryGlow: Color,
    secondaryGlow: Color,
    wavePhase: Float,
    runActive: Boolean,
    onLogoClick: () -> Unit,
    onWaveformClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            secondaryGlow.copy(alpha = 0.55f),
                            primaryGlow.copy(alpha = 0.70f),
                            secondaryGlow.copy(alpha = 0.55f),
                        ),
                    ),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(width = 1.6.dp.toPx()),
                )
            },
        shape = shape,
        cornerRadiusDp = 16.dp,
        strongBlur = true,
        tintAlpha = 0.64f,
        prismIntensity = 1.45f,
        accentGlow = primaryGlow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: T + Crown logo in glowing ring (opens Push Back menu)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onLogoClick),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .blur(8.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    secondaryGlow.copy(alpha = 0.55f),
                                    primaryGlow.copy(alpha = 0.45f),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                Image(
                    painter = painterResource(R.drawable.thakur_crown_t_mark),
                    contentDescription = "THAKUR OS Menu",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(44.dp),
                )
                if (runActive) {
                    BloodDot(
                        size = 7.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp),
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Vertical neon separator
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(34.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                secondaryGlow.copy(alpha = 0.85f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            // Center: THAKUR OS + • Listening • in pink
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onWaveformClick,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ThakurOsNeonTitle(fontSizeSp = 19f, secondaryGlow = secondaryGlow, primaryGlow = primaryGlow)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "• Listening •",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFFFF6BE6),
                    maxLines = 1,
                )
            }

            // Right: Animated voice waveform icon button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onWaveformClick),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(30.dp)) {
                    val bars = 9
                    val step = size.width / bars
                    val barW = (step * 0.52f).coerceAtLeast(2.dp.toPx())
                    val cy = size.height / 2f
                    for (i in 0 until bars) {
                        val centerWeight = 1f - abs(i - 4f) / 4.6f
                        val anim = 0.55f + 0.45f * sin(wavePhase + i * 0.8f)
                        val barH = size.height * (0.18f + 0.72f * centerWeight * anim)
                        val x = step * i + step / 2f
                        val c = if (i % 2 == 0) secondaryGlow else primaryGlow
                        drawLine(
                            color = c,
                            start = Offset(x, cy - barH / 2f),
                            end = Offset(x, cy + barH / 2f),
                            strokeWidth = barW,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Far right: Settings gear inside neon tech ring
            LiquidGlassSurface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                cornerRadiusDp = 20.dp,
                strongBlur = false,
                tintAlpha = 0.60f,
                prismIntensity = 1.4f,
                accentGlow = secondaryGlow,
                onClick = onSettingsClick,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "THAKUR OS Settings",
                        tint = Color(0xFF6CE8FF),
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThakurLauncherSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    quickHubOpen: Boolean,
    primaryGlow: Color,
    secondaryGlow: Color,
    onGridClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            primaryGlow.copy(alpha = 0.72f),
                            secondaryGlow.copy(alpha = 0.65f),
                            primaryGlow.copy(alpha = 0.72f),
                        ),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(width = 1.3.dp.toPx()),
                )
            },
        shape = RoundedCornerShape(24.dp),
        cornerRadiusDp = 24.dp,
        strongBlur = true,
        tintAlpha = 0.56f,
        prismIntensity = 1.35f,
        accentGlow = primaryGlow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search apps",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search apps...",
                        color = Color.White.copy(alpha = 0.76f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    cursorBrush = SolidColor(secondaryGlow),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear search",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onQueryChange("") },
                )
                Spacer(Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(Color.White.copy(alpha = 0.25f)),
            )
            Spacer(Modifier.width(12.dp))
            // 2x2 Glowing neon grid icon
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onGridClick),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val r = 4.2.dp.toPx()
                    val offset1 = size.width * 0.26f
                    val offset2 = size.width * 0.74f
                    val ringColor = if (quickHubOpen) secondaryGlow else Color(0xFFD88CFF)
                    listOf(
                        Offset(offset1, offset1),
                        Offset(offset2, offset1),
                        Offset(offset1, offset2),
                        Offset(offset2, offset2),
                    ).forEach { center ->
                        drawCircle(
                            color = ringColor,
                            radius = r,
                            center = center,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLauncherHubStrip(
    isWorkMode: Boolean,
    primaryGlow: Color,
    secondaryGlow: Color,
    onToggleWork: (Boolean) -> Unit,
    onBrowser: () -> Unit,
    onActivity: () -> Unit,
    onAgent: () -> Unit,
    onSkills: () -> Unit,
    onCommandPalette: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(20.dp),
        cornerRadiusDp = 20.dp,
        strongBlur = true,
        tintAlpha = 0.64f,
        prismIntensity = 1.3f,
        accentGlow = primaryGlow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Quick OS Hub",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                RelaxWorkLiquidToggle(
                    isWorkMode = isWorkMode,
                    onModeChange = onToggleWork,
                    modifier = Modifier.size(width = 148.dp, height = 34.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HubChip(
                    icon = Icons.Outlined.SmartToy,
                    label = "Agent",
                    glow = primaryGlow,
                    onClick = onAgent,
                    modifier = Modifier.weight(1f),
                )
                HubChip(
                    icon = Icons.Outlined.Language,
                    label = "Browser",
                    glow = secondaryGlow,
                    onClick = onBrowser,
                    modifier = Modifier.weight(1f),
                )
                HubChip(
                    icon = Icons.Outlined.Timeline,
                    label = "Inbox",
                    glow = primaryGlow,
                    onClick = onActivity,
                    modifier = Modifier.weight(1f),
                )
                HubChip(
                    icon = Icons.Outlined.AutoAwesome,
                    label = "Skills",
                    glow = secondaryGlow,
                    onClick = onSkills,
                    modifier = Modifier.weight(1f),
                )
                HubChip(
                    icon = Icons.Filled.Search,
                    label = "Palette",
                    glow = primaryGlow,
                    onClick = onCommandPalette,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HubChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    glow: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(14.dp),
        cornerRadiusDp = 14.dp,
        strongBlur = false,
        tintAlpha = 0.56f,
        prismIntensity = 1.1f,
        accentGlow = glow,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
        }
    }
}

@Composable
private fun NeonRingAppTile(
    app: AppInfo,
    primaryGlow: Color,
    secondaryGlow: Color,
    onLaunch: () -> Unit,
) {
    val context = LocalContext.current
    val iconBitmap by produceState<ImageBitmap?>(initialValue = AppIconCache.cache.get(app.packageName), app.packageName) {
        if (value == null) {
            val bmp = withContext(Dispatchers.IO) {
                runCatching {
                    val drawable = context.packageManager.getApplicationIcon(app.packageName)
                    drawable.toBitmap(width = 128, height = 128).asImageBitmap()
                }.getOrNull()
            }
            if (bmp != null) {
                AppIconCache.cache.put(app.packageName, bmp)
                value = bmp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onLaunch)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Soft outer neon halo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .blur(6.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                primaryGlow.copy(alpha = 0.42f),
                                secondaryGlow.copy(alpha = 0.25f),
                                Color.Transparent,
                            ),
                        ),
                        CircleShape,
                    ),
            )

            // Real app icon clipped circularly inside glowing purple-blue ring
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0E1324)),
                contentAlignment = Alignment.Center,
            ) {
                val bmp = iconBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = app.appName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }

            // Neon gradient ring + top glass specular arc
            Canvas(modifier = Modifier.size(50.dp)) {
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            secondaryGlow,
                            Color.White.copy(alpha = 0.72f),
                            primaryGlow,
                            LiquidPalette.PrismRose.copy(alpha = 0.85f),
                        ),
                    ),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = app.appName,
            style = TextStyle(
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.9f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 4f,
                ),
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LauncherHealthBanner(
    issue: String,
    primaryGlow: Color,
    onFix: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        cornerRadiusDp = 16.dp,
        strongBlur = false,
        tintAlpha = 0.62f,
        prismIntensity = 1.15f,
        accentGlow = primaryGlow,
        onClick = onFix,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BloodDot()
            Text(
                text = issue,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Fix",
                color = LiquidPalette.PrismBlue,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ThakurSpeakingBottomBar(
    shape: GenericShape,
    primaryGlow: Color,
    secondaryGlow: Color,
    wavePhase: Float,
    glowPulse: Float,
    statusText: String,
    onPrimaryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier
            .height(92.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            secondaryGlow.copy(alpha = 0.72f * glowPulse),
                            primaryGlow.copy(alpha = 0.85f * glowPulse),
                            LiquidPalette.PrismRose.copy(alpha = 0.68f * glowPulse),
                            secondaryGlow.copy(alpha = 0.72f * glowPulse),
                        ),
                    ),
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
            },
        shape = shape,
        cornerRadiusDp = 22.dp,
        strongBlur = true,
        tintAlpha = 0.72f,
        prismIntensity = 1.55f,
        accentGlow = primaryGlow,
        onClick = onPrimaryClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: T + Crown logo in glowing neon circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable(onClick = onPrimaryClick),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .blur(10.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    primaryGlow.copy(alpha = 0.55f * glowPulse),
                                    secondaryGlow.copy(alpha = 0.45f * glowPulse),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                Canvas(modifier = Modifier.size(66.dp)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                secondaryGlow,
                                primaryGlow,
                                LiquidPalette.PrismRose,
                                secondaryGlow,
                            ),
                        ),
                        style = Stroke(width = 2.2.dp.toPx()),
                    )
                }
                Image(
                    painter = painterResource(R.drawable.thakur_crown_t_mark),
                    contentDescription = "Talk to THAKUR OS",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(Modifier.width(8.dp))

            // Vertical neon divider line
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(54.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                secondaryGlow.copy(alpha = 0.85f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Spacer(Modifier.width(10.dp))

            // Center: THAKUR OS + Speaking... + live animated blue-purple waveform bars
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ThakurOsNeonTitle(fontSizeSp = 15.5f, secondaryGlow = secondaryGlow, primaryGlow = primaryGlow)
                Text(
                    text = statusText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryGlow,
                    maxLines = 1,
                )
                Spacer(Modifier.height(5.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                ) {
                    val bars = 30
                    val step = size.width / bars
                    val barW = (step * 0.48f).coerceAtLeast(2.dp.toPx())
                    val cy = size.height / 2f
                    val centerIdx = (bars - 1) / 2f
                    for (i in 0 until bars) {
                        val distNorm = abs(i - centerIdx) / centerIdx
                        val envelope = (1f - distNorm * distNorm * 0.85f).coerceAtLeast(0.14f)
                        val wave = 0.35f + 0.65f * ((sin(wavePhase * 1.4f + i * 0.55f) + 1f) * 0.5f)
                        val barH = size.height * envelope * wave
                        val x = step * i + step / 2f
                        val barColor = if (i < bars / 2) {
                            secondaryGlow
                        } else if (i < bars * 3 / 4) {
                            primaryGlow
                        } else {
                            LiquidPalette.PrismRose
                        }
                        drawLine(
                            color = barColor,
                            start = Offset(x, cy - barH / 2f),
                            end = Offset(x, cy + barH / 2f),
                            strokeWidth = barW,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Right: Circular neon X button
            LiquidGlassSurface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                cornerRadiusDp = 20.dp,
                strongBlur = false,
                tintAlpha = 0.68f,
                prismIntensity = 1.4f,
                accentGlow = primaryGlow,
                onClick = onCloseClick,
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThakurOsNeonTitle(
    fontSizeSp: Float,
    secondaryGlow: Color,
    primaryGlow: Color,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                append("THAKUR ")
            }
            withStyle(
                SpanStyle(
                    brush = Brush.horizontalGradient(
                        listOf(secondaryGlow, LiquidPalette.PrismRose, primaryGlow),
                    ),
                    fontWeight = FontWeight.Black,
                ),
            ) {
                append("OS")
            }
        },
        fontSize = fontSizeSp.sp,
        letterSpacing = 1.2.sp,
        maxLines = 1,
    )
}

private fun launchInstalledApp(context: Context, app: AppInfo) {
    runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Unable to launch ${app.appName}", Toast.LENGTH_SHORT).show()
        }
    }.onFailure {
        Toast.makeText(context, "Unable to launch ${app.appName}", Toast.LENGTH_SHORT).show()
    }
}

private fun launcherChamferShape(cut: Dp) = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val c = minOf(cut.value * 2.2f, minOf(w, h) * 0.34f)
    moveTo(c, 0f)
    lineTo(w - c, 0f)
    lineTo(w, c)
    lineTo(w, h - c)
    lineTo(w - c, h)
    lineTo(c, h)
    lineTo(0f, h - c)
    lineTo(0f, c)
    close()
}
