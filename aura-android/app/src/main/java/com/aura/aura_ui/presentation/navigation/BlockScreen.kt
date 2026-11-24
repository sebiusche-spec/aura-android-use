package com.aura.aura_ui.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.SilkMeshBackground
import com.aura.aura_ui.ui.theme.MonoScheme

/**
 * Full-screen, non-dismissible page shown instead of the whole app while the remote gate blocks.
 */
@Composable
fun BlockScreen(title: String, message: String, updateUrl: String?, scheme: MonoScheme) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SilkMeshBackground()
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(26.dp),
            cornerRadiusDp = 26.dp,
            strongBlur = true,
            tintAlpha = 0.62f,
            accentGlow = LiquidPalette.PrismBlue,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.85f),
                )
                if (updateUrl != null) {
                    Spacer(Modifier.height(20.dp))
                    if (updateUrl.isNotBlank()) {
                        LiquidGlassButton(
                            onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))) }
                            },
                            accentGlow = LiquidPalette.SilkCyan,
                        ) {
                            Text("Get the update")
                        }
                    } else {
                        Text(
                            text = "Install the latest version from wherever you got THAKUR OS.",
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

/** Dismissible, shown once per app session on top of the normal nav content. */
@Composable
fun UpdateBanner(updateUrl: String, scheme: MonoScheme, onDismiss: () -> Unit) {
    val context = LocalContext.current
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        cornerRadiusDp = 16.dp,
        strongBlur = false,
        tintAlpha = 0.58f,
        accentGlow = LiquidPalette.PrismBlue,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("An update is available", color = Color.White, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiquidGlassButton(
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))) }
                    },
                    accentGlow = LiquidPalette.SilkCyan,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) { Text("Update", fontSize = 12.sp) }
                LiquidGlassButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) { Text("Dismiss", fontSize = 12.sp) }
            }
        }
    }
}
