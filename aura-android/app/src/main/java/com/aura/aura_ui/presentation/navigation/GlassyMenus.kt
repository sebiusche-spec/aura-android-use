package com.aura.aura_ui.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.SilkMeshBackground
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ============================================================================
// LIQUID GLASS NAVIGATION & 6 ANIMATED MENUS
//
// Translucent Liquid Glass pill bottom bar (5 tabs: Home, Search, Create +,
// Inbox, Saved with glowing teal indicator), top command strip, and all 6
// 300ms animated menus: Push Back, Kebab Popover, Gooey Menu, Fold Down,
// Button to Panel, and Command Palette.
// ============================================================================

internal object GlassyColors {
    val Teal = Color(0xFF14F1D9)
    val Cyan = Color(0xFF00E5FF)
    val Purple = Color(0xFF9D4EDD)
    val Danger = Color(0xFFFF5C7A)

    val IconActive = Color.White
    val IconInactive = Color.White.copy(alpha = 0.66f)
    val TextSecondary = Color(0xFFC2C8DA)
}

internal const val GLASS_ANIM_MS = 300

internal enum class GlassTabId { HOME, SEARCH, CREATE, INBOX, SAVED }

internal data class GlassBottomTab(
    val id: GlassTabId,
    val route: String?,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val isCenterCreate: Boolean = false,
)

internal val glassBottomTabs = listOf(
    GlassBottomTab(GlassTabId.HOME, AuraRoutes.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    GlassBottomTab(GlassTabId.SEARCH, AuraRoutes.AGENT, "Search", Icons.Outlined.Search, Icons.Filled.Search),
    GlassBottomTab(GlassTabId.CREATE, null, "Create", Icons.Rounded.Add, Icons.Rounded.Add, isCenterCreate = true),
    GlassBottomTab(GlassTabId.INBOX, AuraRoutes.ACTIVITY, "Inbox", Icons.Outlined.Inbox, Icons.Filled.Inbox),
    GlassBottomTab(GlassTabId.SAVED, AuraRoutes.AGENT_MEMORY, "Saved", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
)

internal data class CommandPaletteEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String,
    val onRun: () -> Unit,
)

// ─────────────────────────────────────────────────────────────────────────────
// LIQUID GLASS BOTTOM NAVIGATION BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GlassyBottomBar(
    currentRoute: String?,
    createPanelOpen: Boolean,
    onSelectTab: (GlassBottomTab) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTabLevel = currentRoute == null || glassBottomTabs.any { it.route == currentRoute }
    val pillShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = pillShape,
            cornerRadiusDp = 34.dp,
            strongBlur = true,
            tintAlpha = 0.50f,
            prismIntensity = 1.15f,
        ) {
            AnimatedContent(
                targetState = isTabLevel,
                transitionSpec = {
                    fadeIn(tween(GLASS_ANIM_MS, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(tween(200))
                },
                label = "liquidBarMode",
            ) { tabLevel ->
                if (tabLevel) {
                    GlassyNavTabsRow(
                        currentRoute = currentRoute,
                        createPanelOpen = createPanelOpen,
                        onSelectTab = onSelectTab,
                    )
                } else {
                    GlassyContextBar(
                        title = titleForRoute(currentRoute),
                        onBack = onBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassyNavTabsRow(
    currentRoute: String?,
    createPanelOpen: Boolean,
    onSelectTab: (GlassBottomTab) -> Unit,
) {
    val activeIndex = remember(currentRoute, createPanelOpen) {
        if (createPanelOpen) {
            2
        } else {
            val idx = glassBottomTabs.indexOfFirst { it.route != null && it.route == currentRoute }
            if (idx >= 0) idx else 0
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 8.dp),
    ) {
        val tabWidth = maxWidth / glassBottomTabs.size
        val indicatorCenterOffset by animateDpAsState(
            targetValue = tabWidth * activeIndex + tabWidth / 2,
            animationSpec = tween(durationMillis = GLASS_ANIM_MS, easing = FastOutSlowInEasing),
            label = "tealLightX",
        )

        // Glowing teal / cyan light cone above the selected tab
        Box(
            modifier = Modifier
                .offset(x = indicatorCenterOffset - 28.dp)
                .width(56.dp)
                .height(44.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(36.dp)
                    .blur(radius = 12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                GlassyColors.Cyan.copy(alpha = 0.72f),
                                GlassyColors.Teal.copy(alpha = 0.28f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GlassyColors.Teal,
                                GlassyColors.Cyan,
                                GlassyColors.Teal,
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            glassBottomTabs.forEachIndexed { index, tab ->
                val selected = index == activeIndex
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowInEasing),
                    label = "tabScale_${tab.id}",
                )

                LiquidGlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    cornerRadiusDp = 20.dp,
                    strongBlur = false,
                    tintAlpha = if (tab.isCenterCreate) 0.14f else if (selected) 0.34f else 0.12f,
                    prismIntensity = if (tab.isCenterCreate) 0.35f else if (selected) 0.95f else 0.25f,
                    accentGlow = if (selected && !tab.isCenterCreate) GlassyColors.Cyan else null,
                    onClick = { onSelectTab(tab) },
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (tab.isCenterCreate) {
                            LiquidGlassSurface(
                                modifier = Modifier
                                    .size(42.dp)
                                    .scale(iconScale),
                                shape = CircleShape,
                                cornerRadiusDp = 21.dp,
                                strongBlur = false,
                                tintAlpha = if (selected) 0.68f else 0.56f,
                                prismIntensity = if (selected) 1.45f else 1.15f,
                                accentGlow = if (selected) GlassyColors.Teal else GlassyColors.Purple,
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (createPanelOpen) Icons.Rounded.Close else tab.selectedIcon,
                                        contentDescription = tab.label,
                                        tint = GlassyColors.IconActive,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .blur(10.dp, BlurredEdgeTreatment.Unbounded)
                                            .background(GlassyColors.Cyan.copy(alpha = 0.30f), CircleShape),
                                    )
                                }
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selected) GlassyColors.IconActive else GlassyColors.IconInactive,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(iconScale),
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) GlassyColors.Teal else GlassyColors.IconInactive,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassyContextBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiquidGlassSurface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            cornerRadiusDp = 22.dp,
            strongBlur = false,
            tintAlpha = 0.45f,
            onClick = onBack,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(44.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIQUID GLASS TOP COMMAND BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GlassyTopCommandStrip(
    drawerOpen: Boolean,
    gooeyOpen: Boolean,
    categoriesOpen: Boolean,
    kebabOpen: Boolean,
    onToggleDrawer: () -> Unit,
    onToggleGooey: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onToggleCategories: () -> Unit,
    onToggleKebab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 1. Push Back left-menu button
        GlassyCircleAction(
            icon = Icons.Outlined.Hub,
            contentDescription = "Side menu",
            active = drawerOpen,
            accent = GlassyColors.Purple,
            onClick = onToggleDrawer,
        )

        // 3. Gooey Hamburger button
        GlassyCircleAction(
            icon = if (gooeyOpen) Icons.Rounded.Close else Icons.Rounded.Menu,
            contentDescription = "Quick actions menu",
            active = gooeyOpen,
            accent = GlassyColors.Teal,
            onClick = onToggleGooey,
        )

        // 6. Command Palette drop-down search pill
        LiquidGlassSurface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            shape = RoundedCornerShape(22.dp),
            cornerRadiusDp = 20.dp,
            strongBlur = false,
            tintAlpha = 0.48f,
            onClick = onOpenCommandPalette,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Command palette",
                    tint = GlassyColors.Teal,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Search...",
                    color = GlassyColors.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 4. Fold Down Categories button
        val chevronRotation by animateFloatAsState(
            targetValue = if (categoriesOpen) 180f else 0f,
            animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowInEasing),
            label = "catChevron",
        )
        LiquidGlassSurface(
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(22.dp),
            cornerRadiusDp = 20.dp,
            strongBlur = false,
            tintAlpha = if (categoriesOpen) 0.65f else 0.48f,
            prismIntensity = if (categoriesOpen) 1.35f else 0.9f,
            onClick = onToggleCategories,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Categories",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Categories",
                    tint = GlassyColors.Teal,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation),
                )
            }
        }

        // 2. Kebab 3-dots popover button
        GlassyCircleAction(
            icon = Icons.Default.MoreVert,
            contentDescription = "More options",
            active = kebabOpen,
            accent = GlassyColors.Purple,
            onClick = onToggleKebab,
        )
    }
}

@Composable
private fun GlassyCircleAction(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        cornerRadiusDp = 20.dp,
        strongBlur = false,
        tintAlpha = if (active) 0.66f else 0.48f,
        prismIntensity = if (active) 1.4f else 0.95f,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (active) Modifier.background(accent.copy(alpha = 0.18f)) else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 1: PUSH BACK — Main screen shrinks & slides right to reveal left
// side menu: Home, History, Skills, Settings.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PushBackDrawerContainer(
    isOpen: Boolean,
    currentRoute: String?,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(durationMillis = GLASS_ANIM_MS, easing = FastOutSlowInEasing),
        label = "pushBackProgress",
    )
    val density = LocalDensity.current
    val slidePx = with(density) { 236.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Colorful silk mesh backdrop behind everything
        SilkMeshBackground()

        // Left side menu (Home, History, Skills, Settings)
        if (progress > 0.01f) {
            PushBackSideMenu(
                progress = progress,
                currentRoute = currentRoute,
                onSelect = { route ->
                    onDismiss()
                    onNavigate(route)
                },
            )
        }

        // Main app screen — shrinks to 0.82f and slides right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f - 0.18f * progress
                    scaleX = scale
                    scaleY = scale
                    translationX = slidePx * progress
                    shape = RoundedCornerShape((28f * progress).dp)
                    clip = progress > 0.01f
                }
                .then(
                    if (progress > 0.01f) {
                        Modifier.border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.55f * progress),
                                    GlassyColors.Teal.copy(alpha = 0.45f * progress),
                                    GlassyColors.Purple.copy(alpha = 0.40f * progress),
                                ),
                            ),
                            shape = RoundedCornerShape((28f * progress).dp),
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            // Silk backdrop continues seamlessly inside the clipped main screen
            SilkMeshBackground()
            content()

            if (isOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.12f * progress))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PushBackSideMenu(
    progress: Float,
    currentRoute: String?,
    onSelect: (String) -> Unit,
) {
    data class DrawerItem(val route: String, val label: String, val icon: ImageVector)

    val items = remember {
        listOf(
            DrawerItem(AuraRoutes.HOME, "Home", Icons.Outlined.Home),
            DrawerItem(AuraRoutes.ACTIVITY, "History", Icons.Outlined.History),
            DrawerItem(AuraRoutes.AGENT_SKILLS, "Skills", Icons.Outlined.AutoAwesome),
            DrawerItem(AuraRoutes.SETTINGS, "Settings", Icons.Outlined.Settings),
        )
    }

    Column(
        modifier = Modifier
            .width(226.dp)
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 28.dp)
            .graphicsLayer {
                alpha = progress
                translationX = -28f * (1f - progress)
            },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "THAKUR OS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                ),
                color = Color.White,
            )
            Text(
                text = "Navigation",
                style = MaterialTheme.typography.labelMedium,
                color = GlassyColors.Teal,
            )
            Spacer(Modifier.height(18.dp))

            items.forEach { item ->
                val selected = currentRoute == item.route
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    cornerRadiusDp = 16.dp,
                    strongBlur = false,
                    tintAlpha = if (selected) 0.62f else 0.34f,
                    prismIntensity = if (selected) 1.35f else 0.65f,
                    onClick = { onSelect(item.route) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) GlassyColors.Teal else Color.White,
                            modifier = Modifier.size(21.dp),
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }
        }

        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            cornerRadiusDp = 14.dp,
            strongBlur = false,
            tintAlpha = 0.42f,
            onClick = { onSelect(AuraRoutes.SETTINGS_MCP_CENTER) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Hub,
                    contentDescription = "MCP Center",
                    tint = GlassyColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "MCP Server",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassyColors.TextSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 2: KEBAB POPOVER — 3 dots button expands to Edit, Share, Delete
// options with pop animation.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun KebabPopoverOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 56.dp, end = 14.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowInEasing),
                initialScale = 0.25f,
                transformOrigin = TransformOrigin(0.92f, 0f),
            ) + fadeIn(tween(240)),
            exit = scaleOut(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                targetScale = 0.25f,
                transformOrigin = TransformOrigin(0.92f, 0f),
            ) + fadeOut(tween(180)),
        ) {
            LiquidGlassSurface(
                modifier = Modifier.width(192.dp),
                shape = RoundedCornerShape(22.dp),
                cornerRadiusDp = 22.dp,
                strongBlur = true,
                tintAlpha = 0.62f,
                prismIntensity = 1.25f,
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    KebabOptionRow(
                        label = "Edit",
                        icon = Icons.Outlined.Edit,
                        tint = Color.White,
                        onClick = {
                            onDismiss()
                            onEdit()
                        },
                    )
                    KebabOptionRow(
                        label = "Share",
                        icon = Icons.Outlined.Share,
                        tint = GlassyColors.Teal,
                        onClick = {
                            onDismiss()
                            onShare()
                        },
                    )
                    KebabOptionRow(
                        label = "Delete",
                        icon = Icons.Outlined.DeleteOutline,
                        tint = GlassyColors.Danger,
                        onClick = {
                            onDismiss()
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun KebabOptionRow(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        cornerRadiusDp = 14.dp,
        strongBlur = false,
        tintAlpha = 0.28f,
        prismIntensity = 0.65f,
        accentGlow = if (tint == Color.White) null else tint,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (tint == GlassyColors.Danger) GlassyColors.Danger else Color.White,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 3: GOOEY MENU — Hamburger button splits into 4 gooey round buttons
// (Voice, Type, Camera, Quick Task), then snaps back.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GooeyMenuOverlay(
    open: Boolean,
    onDismiss: () -> Unit,
    onVoice: () -> Unit,
    onType: () -> Unit,
    onCamera: () -> Unit,
    onQuickTask: () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = tween(durationMillis = GLASS_ANIM_MS, easing = FastOutSlowInEasing),
        label = "gooeySplit",
    )

    if (progress <= 0.01f) return

    data class GooeyItem(
        val label: String,
        val icon: ImageVector,
        val angleDeg: Double,
        val distanceDp: Float,
        val accent: Color,
        val onClick: () -> Unit,
    )

    val items = remember {
        listOf(
            GooeyItem("Voice", Icons.Default.Mic, 86.0, 74f, GlassyColors.Teal, onVoice),
            GooeyItem("Type", Icons.Outlined.Keyboard, 60.0, 118f, GlassyColors.Purple, onType),
            GooeyItem("Camera", Icons.Outlined.CameraAlt, 32.0, 134f, GlassyColors.Cyan, onCamera),
            GooeyItem("Quick Task", Icons.Default.Bolt, 4.0, 122f, GlassyColors.Purple, onQuickTask),
        )
    }

    val density = LocalDensity.current
    val originX = with(density) { 82.dp.toPx() }
    val originY = with(density) { 28.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f * progress))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Liquid gooey metaball bridges drawn during split & snap-back
            Canvas(modifier = Modifier.fillMaxSize()) {
                val snapPull = sin(progress * PI).toFloat()
                items.forEach { item ->
                    val rad = item.angleDeg * PI / 180.0
                    val distPx = with(density) { item.distanceDp.dp.toPx() } * progress
                    val cx = originX + (cos(rad) * distPx).toFloat()
                    val cy = originY + (sin(rad) * distPx).toFloat()

                    if (snapPull > 0.05f) {
                        val dx = cx - originX
                        val dy = cy - originY
                        val len = kotlin.math.hypot(dx, dy).coerceAtLeast(1f)
                        val nx = -dy / len
                        val ny = dx / len
                        val baseR = with(density) { (16f * snapPull).dp.toPx() }
                        val tipR = with(density) { (13f * snapPull).dp.toPx() }
                        val midX = (originX + cx) * 0.5f
                        val midY = (originY + cy) * 0.5f
                        val waist = with(density) { (4f * snapPull).dp.toPx() }

                        val path = Path().apply {
                            moveTo(originX + nx * baseR, originY + ny * baseR)
                            quadraticTo(midX + nx * waist, midY + ny * waist, cx + nx * tipR, cy + ny * tipR)
                            lineTo(cx - nx * tipR, cy - ny * tipR)
                            quadraticTo(midX - nx * waist, midY - ny * waist, originX - nx * baseR, originY - ny * baseR)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GlassyColors.Purple.copy(alpha = 0.68f * snapPull),
                                    item.accent.copy(alpha = 0.74f * snapPull),
                                ),
                                start = Offset(originX, originY),
                                end = Offset(cx, cy),
                            ),
                        )
                    }
                }
            }

            // 4 Liquid Glass round gooey buttons
            items.forEach { item ->
                val rad = item.angleDeg * PI / 180.0
                val distPx = with(density) { item.distanceDp.dp.toPx() } * progress
                val cx = originX + (cos(rad) * distPx).toFloat()
                val cy = originY + (sin(rad) * distPx).toFloat()
                val btnRadiusPx = with(density) { 26.dp.toPx() }

                Column(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (cx - btnRadiusPx).roundToInt(),
                                y = (cy - btnRadiusPx).roundToInt(),
                            )
                        }
                        .graphicsLayer {
                            scaleX = 0.35f + 0.65f * progress
                            scaleY = 0.35f + 0.65f * progress
                            alpha = progress
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LiquidGlassSurface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        cornerRadiusDp = 26.dp,
                        strongBlur = true,
                        tintAlpha = 0.58f,
                        prismIntensity = 1.35f,
                        onClick = {
                            onDismiss()
                            item.onClick()
                        },
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0C101B))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 4: FOLD DOWN — Categories dropdown unfolds like paper with rows:
// Apps, Tasks, Skills, History.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun FoldDownCategoriesOverlay(
    open: Boolean,
    onDismiss: () -> Unit,
    onApps: () -> Unit,
    onTasks: () -> Unit,
    onSkills: () -> Unit,
    onHistory: () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = tween(durationMillis = GLASS_ANIM_MS, easing = FastOutSlowInEasing),
        label = "paperFold",
    )

    if (progress <= 0.01f) return

    data class CategoryRow(val label: String, val icon: ImageVector, val accent: Color, val onClick: () -> Unit)

    val rows = remember {
        listOf(
            CategoryRow("Apps", Icons.Outlined.Apps, GlassyColors.Teal, onApps),
            CategoryRow("Tasks", Icons.Outlined.CheckCircle, GlassyColors.Purple, onTasks),
            CategoryRow("Skills", Icons.Outlined.AutoAwesome, GlassyColors.Cyan, onSkills),
            CategoryRow("History", Icons.Outlined.History, GlassyColors.Purple, onHistory),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 56.dp, end = 14.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        val density = LocalDensity.current.density
        Column(
            modifier = Modifier
                .width(214.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            rows.forEachIndexed { index, row ->
                val rowProgress = ((progress * 2.2f) - index * 0.30f).coerceIn(0f, 1f)
                if (rowProgress > 0.01f) {
                    val foldAngle = -88f * (1f - rowProgress)
                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((52f * rowProgress).dp)
                            .graphicsLayer {
                                cameraDistance = 12f * density
                                transformOrigin = TransformOrigin(0.5f, 0f)
                                rotationX = foldAngle
                                alpha = rowProgress
                            },
                        shape = RoundedCornerShape(10.dp),
                        cornerRadiusDp = 10.dp,
                        strongBlur = false,
                        tintAlpha = 0.62f,
                        prismIntensity = 0.9f,
                        onClick = {
                            onDismiss()
                            row.onClick()
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.36f * (1f - rowProgress))),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = row.icon,
                                contentDescription = row.label,
                                tint = row.accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = row.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 5: BUTTON TO PANEL — Center + New Task button grows into full
// panel with Note, Voice Task, Quick Task.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun NewTaskMorphPanelOverlay(
    open: Boolean,
    onDismiss: () -> Unit,
    onNote: (String?) -> Unit,
    onVoiceTask: () -> Unit,
    onQuickTask: (String?) -> Unit,
) {
    val morphProgress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = tween(durationMillis = GLASS_ANIM_MS, easing = FastOutSlowInEasing),
        label = "buttonToPanel",
    )

    if (morphProgress <= 0.01f) return

    var noteText by remember { mutableStateOf("") }
    var showNoteInput by remember { mutableStateOf(false) }

    LaunchedEffect(open) {
        if (!open) {
            showNoteInput = false
            noteText = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.48f * morphProgress))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 96.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val cornerDp = (28f + 22f * (1f - morphProgress)).dp
        val panelShape = RoundedCornerShape(cornerDp)
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val s = 0.22f + 0.78f * morphProgress
                    scaleX = s
                    scaleY = s
                    alpha = morphProgress
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            shape = panelShape,
            cornerRadiusDp = cornerDp,
            strongBlur = true,
            tintAlpha = 0.60f,
            prismIntensity = 1.35f,
            onClick = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(GlassyColors.Purple, GlassyColors.Teal)),
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            text = "+ New Task",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }

                    LiquidGlassSurface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        cornerRadiusDp = 16.dp,
                        strongBlur = false,
                        tintAlpha = 0.46f,
                        onClick = onDismiss,
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                if (showNoteInput) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BasicTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                            cursorBrush = SolidColor(GlassyColors.Teal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(86.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, GlassyColors.Teal.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            decorationBox = { inner ->
                                if (noteText.isEmpty()) {
                                    Text(
                                        text = "Write a note or memory for THAKUR OS...",
                                        color = GlassyColors.TextSecondary,
                                        fontSize = 14.sp,
                                    )
                                }
                                inner()
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LiquidGlassButton(
                                onClick = { showNoteInput = false },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 14.dp,
                                    vertical = 8.dp,
                                ),
                            ) {
                                Text("Cancel", fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            LiquidGlassButton(
                                onClick = {
                                    val value = noteText.trim().takeIf { it.isNotEmpty() }
                                    onDismiss()
                                    onNote(value)
                                },
                                accentGlow = LiquidPalette.SilkCyan,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                            ) {
                                Text("Save Note", fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    PanelActionTile(
                        title = "Note",
                        subtitle = "Save a quick note or open memory",
                        icon = Icons.Outlined.Description,
                        accent = GlassyColors.Purple,
                        onClick = { showNoteInput = true },
                    )
                    PanelActionTile(
                        title = "Voice Task",
                        subtitle = "Speak naturally to THAKUR OS",
                        icon = Icons.Default.Mic,
                        accent = GlassyColors.Teal,
                        onClick = {
                            onDismiss()
                            onVoiceTask()
                        },
                    )
                    PanelActionTile(
                        title = "Quick Task",
                        subtitle = "Launch instant phone automation",
                        icon = Icons.Default.Bolt,
                        accent = GlassyColors.Cyan,
                        onClick = {
                            onDismiss()
                            onQuickTask(null)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        cornerRadiusDp = 16.dp,
        strongBlur = false,
        tintAlpha = 0.42f,
        prismIntensity = 0.85f,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.24f))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassyColors.TextSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATION 6: COMMAND PALETTE — Top search bar drops down, typing shows
// results with stagger animation.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun CommandPaletteOverlay(
    open: Boolean,
    entries: List<CommandPaletteEntry>,
    onDismiss: () -> Unit,
    onAskAi: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    LaunchedEffect(open) {
        if (!open) query = ""
    }

    val filtered = remember(query, entries) {
        val q = query.trim()
        val matched = if (q.isEmpty()) {
            entries
        } else {
            entries.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.subtitle.contains(q, ignoreCase = true) ||
                    it.tag.contains(q, ignoreCase = true)
            }
        }
        if (q.isNotEmpty()) {
            listOf(
                CommandPaletteEntry(
                    id = "ask_dynamic",
                    title = "Ask THAKUR OS: \"$q\"",
                    subtitle = "Run this command now",
                    icon = Icons.Default.Bolt,
                    tag = "RUN",
                    onRun = { onAskAi(q) },
                ),
            ) + matched
        } else {
            matched
        }
    }

    if (open) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }

    AnimatedVisibility(
        visible = open,
        enter = slideInVertically(
            animationSpec = tween(GLASS_ANIM_MS, easing = FastOutSlowInEasing),
            initialOffsetY = { -it },
        ) + fadeIn(tween(GLASS_ANIM_MS)),
        exit = slideOutVertically(
            animationSpec = tween(240, easing = FastOutSlowInEasing),
            targetOffsetY = { -it },
        ) + fadeOut(tween(200)),
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(26.dp),
            cornerRadiusDp = 26.dp,
            strongBlur = true,
            tintAlpha = 0.62f,
            prismIntensity = 1.3f,
            onClick = {},
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, GlassyColors.Teal.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = GlassyColors.Teal,
                        modifier = Modifier.size(18.dp),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Type a command, screen, or skill...",
                                color = GlassyColors.TextSecondary,
                                fontSize = 14.sp,
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(GlassyColors.Teal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close search",
                        tint = GlassyColors.TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                if (query.isNotEmpty()) query = "" else onDismiss()
                            },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    filtered.forEachIndexed { index, entry ->
                        StaggeredCommandRow(
                            index = index,
                            queryKey = query,
                            entry = entry,
                            onClick = {
                                onDismiss()
                                entry.onRun()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredCommandRow(
    index: Int,
    queryKey: String,
    entry: CommandPaletteEntry,
    onClick: () -> Unit,
) {
    val anim = remember(entry.id, queryKey) { Animatable(0f) }
    LaunchedEffect(entry.id, queryKey) {
        delay((index.coerceAtMost(8) * 32).toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        )
    }

    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = anim.value
                translationY = 14f * (1f - anim.value)
            },
        shape = RoundedCornerShape(14.dp),
        cornerRadiusDp = 14.dp,
        strongBlur = false,
        tintAlpha = 0.34f,
        prismIntensity = 0.70f,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GlassyColors.Purple.copy(alpha = 0.26f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = entry.title,
                    tint = GlassyColors.Teal,
                    modifier = Modifier.size(17.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = GlassyColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GlassyColors.Teal.copy(alpha = 0.16f),
                border = BorderStroke(0.5.dp, GlassyColors.Teal.copy(alpha = 0.50f)),
            ) {
                Text(
                    text = entry.tag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassyColors.Teal,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }
    }
}

private val glassRouteTitles = mapOf(
    AuraRoutes.AGENT_BRAIN to "Brain",
    AuraRoutes.AGENT_TOOLS to "Tools",
    AuraRoutes.AGENT_HOOKS to "Hooks",
    AuraRoutes.AGENT_SKILLS to "Skills",
    AuraRoutes.AGENT_MEMORY to "Memory",
    AuraRoutes.AGENT_MCP_SERVERS to "MCP Servers",
    AuraRoutes.AGENT_COMPANION to "Companion",
    AuraRoutes.AGENT_VOICE to "Voice",
    AuraRoutes.ACTIVITY to "Activity",
    AuraRoutes.ACTIVITY_SESSION to "Session",
    AuraRoutes.ACTIVITY_RETENTION to "Retention",
    AuraRoutes.SETTINGS to "Settings",
    AuraRoutes.SETTINGS_MCP_CENTER to "MCP Center",
    AuraRoutes.SETTINGS_TAVILY to "Search key",
    AuraRoutes.SETTINGS_PERMISSIONS to "Permissions",
    AuraRoutes.SETTINGS_PRIVACY to "Privacy",
    AuraRoutes.SETTINGS_TERMS to "Terms",
    AuraRoutes.SETTINGS_LICENSES to "Licences",
)

internal fun titleForRoute(route: String?): String =
    glassRouteTitles[route]
        ?: route?.substringAfterLast('/')?.substringBefore('{')
            ?.replace('_', ' ')
            ?.replaceFirstChar { it.uppercase() }
            .orEmpty()
