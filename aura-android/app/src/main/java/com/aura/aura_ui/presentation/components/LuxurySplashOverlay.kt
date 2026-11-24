package com.aura.aura_ui.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.aura_ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2.5s luxury Compose splash sequence:
 * 1. 300ms pure black screen
 * 2. Slow formation of T + Crown emblem with glowing blue-purple radial light rays
 * 3. Staggered fade-in below: silver "THAKUR OS", small "THINK • SEE • ACT • COMPLETE", "by Shivam Thakur"
 * 4. Hold ~1.35s, then smooth fade + subtle slide/scale into main dark UI (<= 2500ms total).
 */
@Composable
fun LuxurySplashOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val raysAlpha = remember { Animatable(0f) }
    val raysSpin = remember { Animatable(0f) }
    val logoForm = remember { Animatable(0f) }
    val brandTextAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val authorAlpha = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitSlide = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Black screen for 300ms
        delay(300)

        // 2. Spin light rays subtly & form T + Crown slowly (620ms)
        launch {
            raysSpin.animateTo(
                targetValue = 28f,
                animationSpec = tween(durationMillis = 2050, easing = LinearEasing),
            )
        }
        launch {
            raysAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            )
        }
        logoForm.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 580, easing = FastOutSlowInEasing),
        )

        // 3. Staggered text reveal: THAKUR OS -> tagline -> by Shivam Thakur
        launch {
            brandTextAlpha.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
        }
        delay(100)
        launch {
            taglineAlpha.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
        }
        delay(100)
        authorAlpha.animateTo(1f, tween(220, easing = FastOutSlowInEasing))

        // 4. Hold ~1.1s (total ~2.4s <= 2.5s max), then smooth fade + upward glide
        delay(960)
        launch {
            exitSlide.animateTo(-48f, tween(260, easing = FastOutSlowInEasing))
        }
        exitAlpha.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = exitAlpha.value
                translationY = exitSlide.value
            }
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // Volumetric blue-purple light rays behind the T + Crown
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(raysAlpha.value),
        ) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.42f
            val maxR = maxOf(size.width, size.height) * 0.72f

            // Central cosmic halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidPalette.PrismBlue.copy(alpha = 0.44f),
                        LiquidPalette.SilkPurple.copy(alpha = 0.34f),
                        LiquidPalette.SilkMagenta.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, cy),
                    radius = maxR * 0.55f,
                ),
                center = Offset(cx, cy),
                radius = maxR * 0.55f,
            )

            // Rotating glowing blue-purple light rays
            val rayCount = 14
            for (i in 0 until rayCount) {
                val baseAngle = (i * (360f / rayCount)) + raysSpin.value
                val rad1 = (baseAngle - 6.5f) * (PI / 180.0)
                val rad2 = (baseAngle + 6.5f) * (PI / 180.0)
                val rayLen = maxR * (0.58f + 0.25f * ((i % 3) / 2f))
                val rayColor = when (i % 3) {
                    0 -> LiquidPalette.PrismBlue
                    1 -> LiquidPalette.SilkViolet
                    else -> LiquidPalette.SilkMagenta
                }
                val p = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + (cos(rad1) * rayLen).toFloat(), cy + (sin(rad1) * rayLen).toFloat())
                    lineTo(cx + (cos(rad2) * rayLen).toFloat(), cy + (sin(rad2) * rayLen).toFloat())
                    close()
                }
                drawPath(
                    path = p,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            rayColor.copy(alpha = 0.32f),
                            rayColor.copy(alpha = 0.09f),
                            Color.Transparent,
                        ),
                        center = Offset(cx, cy),
                        radius = rayLen,
                    ),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            // T + Crown emblem forming slowly
            Image(
                painter = painterResource(id = R.drawable.thakur_crown_t_mark),
                contentDescription = "THAKUR OS Emblem",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(216.dp)
                    .graphicsLayer {
                        val f = logoForm.value
                        alpha = f
                        val s = 0.72f + 0.28f * f
                        scaleX = s
                        scaleY = s
                    },
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Silver "THAKUR OS" title
            Text(
                text = "THAKUR OS",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFDCE4F2),
                            Color(0xFF9FB4D6),
                            Color(0xFFF3F6FC),
                        ),
                    ),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.5.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = brandTextAlpha.value
                    translationY = (1f - brandTextAlpha.value) * 16f
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Small spaced tagline
            Text(
                text = "THINK  •  SEE  •  ACT  •  COMPLETE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.6.sp,
                color = Color(0xFFDCE7FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = taglineAlpha.value
                    translationY = (1f - taglineAlpha.value) * 10f
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(1.5.dp)
                    .alpha(taglineAlpha.value)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                LiquidPalette.PrismBlue,
                                LiquidPalette.SilkMagenta,
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "by Shivam Thakur",
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            LiquidPalette.SilkCyan,
                            LiquidPalette.PrismRose,
                        ),
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = authorAlpha.value
                    translationY = (1f - authorAlpha.value) * 8f
                },
            )
        }
    }
}
