package com.aura.aura_ui.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aura.aura_ui.R
import com.aura.aura_ui.ui.theme.Mono

// ============================================================================
// MONO COMPONENTS — shared building blocks of the production design language.
//
// One pill, one card, one badge — defined once so app + overlay stay visually
// identical. Spec: docs/superpowers/specs/2026-07-08-production-ui-redesign-design.md
// ============================================================================

/**
 * The canonical primary pill button — ink with white content in light theme,
 * inverted (white with ink content) in dark, matching the reference's dark
 * variant. Fully rounded, spring press-scale.
 */
@Composable
fun MonoPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val fg = Color.White
    LiquidGlassSurface(
        modifier = modifier,
        shape = Mono.ShapePill,
        cornerRadiusDp = 30.dp,
        strongBlur = true,
        tintAlpha = if (enabled) 0.54f else 0.30f,
        prismIntensity = if (enabled) 1.15f else 0.4f,
        onClick = if (enabled) onClick else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) fg else fg.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) fg else fg.copy(alpha = 0.5f),
            )
            content?.invoke(this)
        }
    }
}

/**
 * The canonical dark hero card rendered with Liquid Glass refraction & prism edge highlights.
 */
@Composable
fun MonoInkCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = Mono.ShapeCard,
        cornerRadiusDp = 24.dp,
        strongBlur = true,
        tintAlpha = 0.54f,
        prismIntensity = 1.15f,
        onClick = onClick,
    ) {
        content()
    }
}

/** Liquid Glass card with subtle rim highlight for content sitting on the silk canvas. */
@Composable
fun MonoCanvasCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = Mono.ShapeCard,
        cornerRadiusDp = 24.dp,
        strongBlur = false,
        tintAlpha = 0.50f,
        prismIntensity = 0.9f,
        onClick = onClick,
    ) {
        content()
    }
}

/**
 * The AURA mark drawn in a single tint (default white). ONLY place this on ink
 * surfaces — the white mark never sits on the white canvas (spec §2, logo usage).
 */
@Composable
fun MonoLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
) {
    val logo = ImageBitmap.imageResource(R.mipmap.ic_launcher_foreground)
    Canvas(modifier = modifier.size(size)) {
        // The launcher foreground carries transparent safe-zone padding; scale up
        // ~1.28x so the mark reads at small sizes (same treatment as AuraLogoButton).
        val scale = 1.28f
        val w = this.size.width * scale
        val h = this.size.height * scale
        drawImage(
            image = logo,
            dstOffset = IntOffset(((this.size.width - w) / 2f).toInt(), ((this.size.height - h) / 2f).toInt()),
            dstSize = IntSize(w.toInt(), h.toInt()),
            colorFilter = ColorFilter.tint(tint),
        )
    }
}

/** Scope badge for tool listings: READ = outlined, WRITE = filled ink, SENSITIVE = blood. */
@Composable
fun MonoScopeBadge(
    label: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    blood: Boolean = false,
) {
    val bg = when {
        blood -> Mono.Blood
        filled -> Mono.TextOnInk
        else -> Color.Transparent
    }
    val fg = when {
        blood -> Color.White
        filled -> Mono.Ink
        else -> Mono.TextOnInkDim
    }
    Surface(
        shape = Mono.ShapeChip,
        color = bg,
        border = if (!filled && !blood) BorderStroke(1.dp, Mono.OutlineOnInk) else null,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/** A small blood-red dot — live/attention indicator (active run, broken permission). */
@Composable
fun BloodDot(modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(color = Mono.Blood)
        }
    }
}
