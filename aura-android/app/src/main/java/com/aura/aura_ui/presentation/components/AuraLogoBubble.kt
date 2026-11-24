package com.aura.aura_ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aura.aura_ui.R

/**
 * Brand token for THAKUR OS: glowing T+Crown emblem framed in a blue-purple
 * Liquid Glass sphere.
 *
 * @param size overall diameter of the bubble.
 * @param activeRun when true, badges a blood dot at the top-right (a run is live).
 */
@Composable
fun AuraLogoBubble(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    activeRun: Boolean = false,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        LiquidGlassSurface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            cornerRadiusDp = size / 2,
            strongBlur = true,
            tintAlpha = 0.56f,
            prismIntensity = 1.3f,
            accentGlow = LiquidPalette.PrismBlue,
        ) {
            Image(
                painter = painterResource(id = R.drawable.thakur_crown_t_mark),
                contentDescription = "THAKUR OS",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.08f)
                    .clip(CircleShape),
            )
        }
        if (activeRun) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(size * 0.16f)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                BloodDot(size = size * 0.10f)
            }
        }
    }
}
