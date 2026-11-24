package com.aura.aura_ui.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ============================================================================
// LIQUID GLASS DESIGN PRIMITIVES
//
// Translucent blurred glass with optical lens refraction/magnification feel,
// bright glossy top/side specular edge highlights, subtle RGB prism glow on
// corners, press bounce + glow + lightning flash feedback, and Liquid Glass
// buttons & Relax/Work + boolean toggles.
// ============================================================================

object LiquidPalette {
    val DeepNight = Color(0xFF060811)
    val SilkIndigo = Color(0xFF1A1042)
    val SilkPurple = Color(0xFF6D28D9)
    val SilkViolet = Color(0xFF9D4EDD)
    val SilkMagenta = Color(0xFFC026D3)
    val SilkTeal = Color(0xFF0D9488)
    val SilkCyan = Color(0xFF14F1D9)
    val PrismBlue = Color(0xFF00E5FF)
    val PrismRose = Color(0xFFFF4ECD)
    val WorkOrange = Color(0xFFFF7A1A)
    val WorkAmber = Color(0xFFFF5400)
    val DangerRed = Color(0xFFFF5C7A)

    val GlassBaseTop = Color(0xFF182034)
    val GlassBaseBottom = Color(0xFF090D18)
}

/**
 * Colorful dark flowing silk background so Liquid Glass refraction, blur, and
 * prism highlights are visibly rich while keeping zero per-frame allocations.
 */
@Composable
fun SilkMeshBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070914),
                    Color(0xFF0B0E1D),
                    Color(0xFF060811),
                ),
            ),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidPalette.SilkPurple.copy(alpha = 0.52f),
                    LiquidPalette.SilkMagenta.copy(alpha = 0.22f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.16f, h * 0.18f),
                radius = w * 0.85f,
            ),
            center = Offset(w * 0.16f, h * 0.18f),
            radius = w * 0.85f,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidPalette.SilkTeal.copy(alpha = 0.48f),
                    LiquidPalette.SilkCyan.copy(alpha = 0.18f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.88f, h * 0.44f),
                radius = w * 0.78f,
            ),
            center = Offset(w * 0.88f, h * 0.44f),
            radius = w * 0.78f,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidPalette.SilkViolet.copy(alpha = 0.42f),
                    LiquidPalette.PrismBlue.copy(alpha = 0.24f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.35f, h * 0.88f),
                radius = w * 0.72f,
            ),
            center = Offset(w * 0.35f, h * 0.88f),
            radius = w * 0.72f,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidPalette.SilkCyan.copy(alpha = 0.34f),
                    LiquidPalette.SilkMagenta.copy(alpha = 0.16f),
                    Color.Transparent,
                ),
                center = Offset(w * 0.78f, h * 0.90f),
                radius = w * 0.62f,
            ),
            center = Offset(w * 0.78f, h * 0.90f),
            radius = w * 0.62f,
        )

        val silkRibbon1 = Path().apply {
            moveTo(-w * 0.1f, h * 0.34f)
            cubicTo(w * 0.28f, h * 0.14f, w * 0.68f, h * 0.56f, w * 1.1f, h * 0.28f)
            lineTo(w * 1.1f, h * 0.52f)
            cubicTo(w * 0.65f, h * 0.74f, w * 0.25f, h * 0.36f, -w * 0.1f, h * 0.58f)
            close()
        }
        drawPath(
            path = silkRibbon1,
            brush = Brush.linearGradient(
                colors = listOf(
                    LiquidPalette.SilkMagenta.copy(alpha = 0.22f),
                    LiquidPalette.SilkViolet.copy(alpha = 0.26f),
                    LiquidPalette.SilkCyan.copy(alpha = 0.20f),
                ),
                start = Offset(0f, h * 0.2f),
                end = Offset(w, h * 0.6f),
            ),
        )

        val silkRibbon2 = Path().apply {
            moveTo(-w * 0.1f, h * 0.76f)
            cubicTo(w * 0.30f, h * 0.58f, w * 0.72f, h * 0.98f, w * 1.1f, h * 0.74f)
            lineTo(w * 1.1f, h * 0.98f)
            cubicTo(w * 0.72f, h * 1.12f, w * 0.28f, h * 0.80f, -w * 0.1f, h * 0.98f)
            close()
        }
        drawPath(
            path = silkRibbon2,
            brush = Brush.linearGradient(
                colors = listOf(
                    LiquidPalette.PrismBlue.copy(alpha = 0.22f),
                    LiquidPalette.SilkPurple.copy(alpha = 0.28f),
                    LiquidPalette.SilkCyan.copy(alpha = 0.24f),
                ),
                start = Offset(0f, h * 0.7f),
                end = Offset(w, h),
            ),
        )
    }
}

/**
 * Liquid Glass container with:
 * - Strong blurred translucent glass interior
 * - Magnified / curved background silk refraction feel (`scaleX/scaleY = 1.14f`)
 * - Bright glossy edge highlight on top & sides
 * - Subtle RGB prism glow on corners
 * - Bounce + glow surge + diagonal lightning flash on press when [onClick] is set
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    strongBlur: Boolean = true,
    tintAlpha: Float = 0.56f,
    prismIntensity: Float = 1f,
    accentGlow: Color? = null,
    enabled: Boolean = true,
    pressedOverride: Boolean = false,
    flashSignal: Int = 0,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var flashTrigger by remember { mutableIntStateOf(0) }
    val flashProgress = remember { Animatable(0f) }
    val glowSurge = remember { Animatable(0f) }
    val isPressedNow = pressed || pressedOverride

    val pressScale by animateFloatAsState(
        targetValue = if ((onClick != null || pressedOverride || flashSignal > 0) && enabled && isPressedNow) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "liquidBounce",
    )

    LaunchedEffect(isPressedNow) {
        if (isPressedNow && enabled && (onClick != null || pressedOverride || flashSignal > 0)) {
            flashTrigger++
        }
    }

    LaunchedEffect(flashTrigger, flashSignal) {
        if (flashTrigger > 0 || flashSignal > 0) {
            launch {
                glowSurge.snapTo(0.25f)
                glowSurge.animateTo(1f, tween(90, easing = FastOutSlowInEasing))
                glowSurge.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
            }
            launch {
                flashProgress.snapTo(0f)
                flashProgress.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
                flashProgress.snapTo(0f)
            }
        }
    }

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = {
                flashTrigger++
                onClick()
            },
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(shape)
            .then(clickableMod),
    ) {
        // 1. Magnified / bent refracted silk light layer + strong blur
        val blurModifier = if (strongBlur) {
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = 1.14f
                    scaleY = 1.14f
                }
                .blur(radius = 24.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
        } else {
            Modifier.matchParentSize()
        }

        Box(
            modifier = blurModifier.drawWithCache {
                val w = size.width
                val h = size.height
                val leftTint = accentGlow ?: LiquidPalette.SilkViolet
                val rightTint = accentGlow ?: LiquidPalette.SilkCyan
                val refractionBrush = Brush.linearGradient(
                    colors = listOf(
                        leftTint.copy(alpha = 0.28f),
                        LiquidPalette.GlassBaseTop.copy(alpha = 0.34f),
                        rightTint.copy(alpha = 0.24f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                )
                val domeCaustic = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.14f),
                        (accentGlow ?: LiquidPalette.PrismBlue).copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.5f, h * 0.18f),
                    radius = maxOf(w, h) * 0.72f,
                )
                onDrawBehind {
                    drawRect(refractionBrush)
                    drawRect(domeCaustic)
                }
            },
        )

        // 2. Dark translucent glass body + optical lens bend + RGB prism corners + glossy top/side edges
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val radiusPx = cornerRadiusDp.toPx().coerceAtMost(minOf(w, h) * 0.5f)
                    val cornerRadius = CornerRadius(radiusPx, radiusPx)
                    val glassBody = Brush.verticalGradient(
                        colors = listOf(
                            LiquidPalette.GlassBaseTop.copy(alpha = tintAlpha),
                            LiquidPalette.GlassBaseBottom.copy(alpha = (tintAlpha + 0.14f).coerceAtMost(0.88f)),
                        ),
                    )
                    val accentWash = accentGlow?.let { c ->
                        Brush.radialGradient(
                            colors = listOf(c.copy(alpha = 0.26f), Color.Transparent),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = maxOf(w, h) * 0.65f,
                        )
                    }
                    val lensCurve = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.20f),
                        ),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = maxOf(w, h) * 0.68f,
                    )
                    val topGloss = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.26f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = h * 0.42f,
                    )
                    val edgeSpecular = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.72f),
                            Color.White.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.10f),
                        ),
                    )
                    val tlCyan = Brush.radialGradient(
                        colors = listOf(
                            LiquidPalette.PrismBlue.copy(alpha = 0.36f * prismIntensity),
                            Color.Transparent,
                        ),
                        center = Offset(0f, 0f),
                        radius = minOf(w, h) * 0.65f,
                    )
                    val trMagenta = Brush.radialGradient(
                        colors = listOf(
                            LiquidPalette.PrismRose.copy(alpha = 0.30f * prismIntensity),
                            LiquidPalette.SilkViolet.copy(alpha = 0.16f * prismIntensity),
                            Color.Transparent,
                        ),
                        center = Offset(w, 0f),
                        radius = minOf(w, h) * 0.65f,
                    )
                    val brTeal = Brush.radialGradient(
                        colors = listOf(
                            LiquidPalette.SilkCyan.copy(alpha = 0.32f * prismIntensity),
                            Color.Transparent,
                        ),
                        center = Offset(w, h),
                        radius = minOf(w, h) * 0.60f,
                    )
                    val blPurple = Brush.radialGradient(
                        colors = listOf(
                            LiquidPalette.SilkPurple.copy(alpha = 0.30f * prismIntensity),
                            Color.Transparent,
                        ),
                        center = Offset(0f, h),
                        radius = minOf(w, h) * 0.60f,
                    )
                    val prismBorder = Brush.linearGradient(
                        colors = listOf(
                            (accentGlow ?: LiquidPalette.PrismBlue).copy(alpha = 0.56f * prismIntensity),
                            Color.White.copy(alpha = 0.58f),
                            LiquidPalette.PrismRose.copy(alpha = 0.48f * prismIntensity),
                            (accentGlow ?: LiquidPalette.SilkCyan).copy(alpha = 0.52f * prismIntensity),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h),
                    )

                    onDrawBehind {
                        drawRoundRect(brush = glassBody, cornerRadius = cornerRadius)
                        if (accentWash != null) drawRoundRect(brush = accentWash, cornerRadius = cornerRadius)
                        drawRoundRect(brush = lensCurve, cornerRadius = cornerRadius)
                        drawRoundRect(brush = topGloss, cornerRadius = cornerRadius)
                        drawRoundRect(brush = tlCyan, cornerRadius = cornerRadius)
                        drawRoundRect(brush = trMagenta, cornerRadius = cornerRadius)
                        drawRoundRect(brush = brTeal, cornerRadius = cornerRadius)
                        drawRoundRect(brush = blPurple, cornerRadius = cornerRadius)
                        drawRoundRect(
                            brush = prismBorder,
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.4.dp.toPx()),
                        )
                        drawRoundRect(
                            brush = edgeSpecular,
                            cornerRadius = cornerRadius,
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                }
                // 3. Press glow + diagonal lightning flash overlay
                .drawWithContent {
                    drawContent()
                    val g = glowSurge.value
                    val f = flashProgress.value
                    if (g > 0.01f || f > 0.01f) {
                        val w = size.width
                        val h = size.height
                        val r = cornerRadiusDp.toPx().coerceAtMost(minOf(w, h) * 0.5f)
                        val cr = CornerRadius(r, r)
                        val flashColor = accentGlow ?: LiquidPalette.SilkCyan
                        if (g > 0.01f) {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.24f * g),
                                        flashColor.copy(alpha = 0.28f * g),
                                        Color.Transparent,
                                    ),
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    radius = maxOf(w, h) * 0.85f,
                                ),
                                cornerRadius = cr,
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.65f * g),
                                cornerRadius = cr,
                                style = Stroke(width = 1.8.dp.toPx()),
                            )
                        }
                        if (f > 0.01f && f < 0.99f) {
                            val sweepX = -w * 0.4f + (w * 1.8f) * f
                            val bandW = maxOf(w * 0.36f, 44f)
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        flashColor.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.72f),
                                        LiquidPalette.SilkViolet.copy(alpha = 0.32f),
                                        Color.Transparent,
                                    ),
                                    start = Offset(sweepX - bandW, 0f),
                                    end = Offset(sweepX + bandW, h),
                                ),
                                cornerRadius = cr,
                            )
                        }
                    }
                },
        )

        content()
    }
}

/**
 * Drop-in Liquid Glass action button for primary, outlined, tonal, and destructive actions.
 * Keeps exact layout/function while providing Liquid Glass blur, refraction, prism corners,
 * and bounce + glow + lightning flash on press.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
    accentGlow: Color? = if (destructive) LiquidPalette.DangerRed else null,
    shape: Shape = RoundedCornerShape(50),
    cornerRadiusDp: Dp = 26.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val contentColor = when {
        !enabled -> Color.White.copy(alpha = 0.45f)
        destructive -> Color(0xFFFF8CA0)
        else -> Color.White
    }
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        cornerRadiusDp = cornerRadiusDp,
        strongBlur = true,
        tintAlpha = if (enabled) 0.54f else 0.32f,
        prismIntensity = if (enabled) 1.15f else 0.45f,
        accentGlow = accentGlow,
        enabled = enabled,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)) {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    content = content,
                )
            }
        }
    }
}

/**
 * Liquid Glass pill toggle switch replacing flat Material switches across Settings/Permissions/Skills.
 */
@Composable
fun LiquidGlassToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "liquidToggleX",
    )
    val trackAccent by animateColorAsState(
        targetValue = if (checked) LiquidPalette.SilkCyan else Color(0xFF475569),
        animationSpec = tween(260),
        label = "liquidToggleAccent",
    )

    LiquidGlassSurface(
        modifier = modifier
            .width(54.dp)
            .height(32.dp),
        shape = CircleShape,
        cornerRadiusDp = 16.dp,
        strongBlur = true,
        tintAlpha = if (checked) 0.56f else 0.44f,
        prismIntensity = if (checked) 1.25f else 0.7f,
        accentGlow = if (checked) trackAccent else null,
        enabled = enabled && onCheckedChange != null,
        onClick = if (onCheckedChange != null && enabled) {
            { onCheckedChange(!checked) }
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White,
                                if (checked) Color(0xFFD6FFF9) else Color(0xFFD0D5E2),
                            ),
                        ),
                    ),
            )
        }
    }
}

/**
 * Liquid Glass Relax / Work toggle: Purple for Relax, Orange for Work.
 * Includes sliding Liquid Glass highlight, strong blur, refraction feel,
 * RGB prism corners, and bounce + glow + lightning flash on press.
 */
@Composable
fun RelaxWorkLiquidToggle(
    isWorkMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeAccent by animateColorAsState(
        targetValue = if (isWorkMode) LiquidPalette.WorkOrange else LiquidPalette.SilkViolet,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "relaxWorkAccent",
    )

    val relaxInteraction = remember { MutableInteractionSource() }
    val workInteraction = remember { MutableInteractionSource() }
    val relaxPressed by relaxInteraction.collectIsPressedAsState()
    val workPressed by workInteraction.collectIsPressedAsState()
    var tapPulse by remember { mutableIntStateOf(0) }

    LiquidGlassSurface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(50),
        cornerRadiusDp = 21.dp,
        strongBlur = true,
        tintAlpha = 0.52f,
        prismIntensity = 1.25f,
        accentGlow = activeAccent,
        pressedOverride = relaxPressed || workPressed,
        flashSignal = tapPulse,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
        ) {
            val slotWidth = maxWidth / 2
            val sliderX by animateDpAsState(
                targetValue = if (isWorkMode) slotWidth else 0.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "relaxWorkSlide",
            )

            // Sliding Liquid Glass active pill (Purple for Relax, Orange for Work)
            LiquidGlassSurface(
                modifier = Modifier
                    .offset(x = sliderX)
                    .width(slotWidth)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(50),
                cornerRadiusDp = 18.dp,
                strongBlur = false,
                tintAlpha = 0.66f,
                prismIntensity = 1.40f,
                accentGlow = activeAccent,
                pressedOverride = relaxPressed || workPressed,
                flashSignal = tapPulse,
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }

            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = relaxInteraction,
                            indication = null,
                        ) {
                            tapPulse++
                            onModeChange(false)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Relax",
                        fontSize = 12.sp,
                        fontWeight = if (!isWorkMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isWorkMode) Color.White else Color.White.copy(alpha = 0.65f),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = workInteraction,
                            indication = null,
                        ) {
                            tapPulse++
                            onModeChange(true)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Work",
                        fontSize = 12.sp,
                        fontWeight = if (isWorkMode) FontWeight.Bold else FontWeight.Medium,
                        color = if (isWorkMode) Color.White else Color.White.copy(alpha = 0.65f),
                    )
                }
            }
        }
    }
}
