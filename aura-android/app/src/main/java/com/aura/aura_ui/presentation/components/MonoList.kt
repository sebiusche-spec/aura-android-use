package com.aura.aura_ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.aura_ui.ui.theme.Mono
import com.aura.aura_ui.ui.theme.MonoScheme

// ============================================================================
// MONO LIST KIT — the grouped-list language of the redesigned hub/settings
// screens (per the approved references): section labels above white/charcoal
// cards, rows with a small icon chip + title + chevron/toggle, hairline
// dividers, red reserved for destructive rows. Theme-aware via MonoScheme.
// ============================================================================

/**
 * The app's grouped-list scheme. Dark mode was removed as a user-facing app
 * setting, so every in-app surface renders light; the overlay owns its own fixed
 * dark palette separately.
 */
@Composable
fun rememberMonoScheme(): MonoScheme = Mono.scheme(dark = true)

/**
 * Large-title top bar with optional back button, on the canvas.
 *
 * [actions] renders at the trailing edge. The title column takes `weight(1f)` so a long
 * title yields space to the actions instead of pushing them off-screen — a `Row` measures
 * non-weighted children against whatever width is left, which is how text ends up squeezed
 * into one character per line.
 */
@Composable
fun MonoTopBar(
    title: String,
    scheme: MonoScheme,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (onBack != null) 8.dp else 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            LiquidGlassSurface(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(40.dp),
                shape = CircleShape,
                cornerRadiusDp = 20.dp,
                strongBlur = false,
                tintAlpha = 0.5f,
                onClick = onBack,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = scheme.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp,
                ),
                color = scheme.textPrimary,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.textSecondary,
                )
            }
        }
        actions()
    }
}

/** Small uppercase-free section label above a card group. */
@Composable
fun MonoSectionLabel(text: String, scheme: MonoScheme, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = scheme.textSecondary,
        modifier = modifier.padding(start = 8.dp, top = 18.dp, bottom = 8.dp),
    )
}

/** A rounded card containing a vertical group of rows separated by hairlines. */
@Composable
fun MonoCardGroup(
    scheme: MonoScheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScopeMarker.() -> Unit,
) {
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = Mono.ShapeCard,
        cornerRadiusDp = 24.dp,
        strongBlur = false,
        tintAlpha = 0.56f,
        prismIntensity = 0.85f,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ColumnScopeMarker(scheme).content()
        }
    }
}

/** Scope carrier so rows inside a group can draw their own dividers. */
class ColumnScopeMarker(val scheme: MonoScheme)

/** Hairline divider inset past the icon chip, for use between rows. */
@Composable
fun ColumnScopeMarker.MonoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp)
            .height(1.dp)
            .background(scheme.divider),
    )
}

/**
 * One list row: icon chip · title(+subtitle) · trailing (chevron by default).
 * [destructive] renders title + icon in blood red (delete/deactivate rows only).
 */
@Composable
fun ColumnScopeMarker.MonoRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (destructive) Mono.Blood else scheme.textPrimary
    val rowContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = Mono.ShapeChip,
                color = if (destructive) Mono.BloodContainer else scheme.chip,
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (destructive) Mono.Blood else scheme.chipContent,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = contentColor,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.textSecondary,
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = scheme.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
    if (onClick != null) {
        LiquidGlassSurface(
            modifier = modifier.fillMaxWidth(),
            shape = Mono.ShapeChip,
            cornerRadiusDp = 12.dp,
            strongBlur = false,
            tintAlpha = 0.08f,
            prismIntensity = 0.35f,
            onClick = onClick,
        ) {
            rowContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) { rowContent() }
    }
}

/**
 * Capsule switch (2026-07-10): ON = ink track with white thumb (cream track +
 * ink thumb in dark theme) — the black-object-on-light language, no iOS green.
 * Nullable [onCheckedChange] renders a read-only (disabled) switch.
 */
@Composable
fun MonoSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    scheme: MonoScheme,
) {
    LiquidGlassToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

/** Compact Liquid Glass segmented control: selected pill glows with teal Liquid Glass. */
@Composable
fun MonoSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    scheme: MonoScheme,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = Mono.ShapePill,
        cornerRadiusDp = 24.dp,
        strongBlur = true,
        tintAlpha = 0.46f,
        prismIntensity = 0.9f,
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            options.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                LiquidGlassSurface(
                    shape = Mono.ShapePill,
                    cornerRadiusDp = 20.dp,
                    strongBlur = false,
                    tintAlpha = if (selected) 0.62f else 0.0f,
                    prismIntensity = if (selected) 1.25f else 0.0f,
                    accentGlow = if (selected) LiquidPalette.SilkCyan else null,
                    onClick = { onSelect(i) },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selected) Color.White else scheme.textSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
