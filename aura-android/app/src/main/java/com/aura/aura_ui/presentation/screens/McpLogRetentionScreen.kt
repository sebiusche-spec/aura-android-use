package com.aura.aura_ui.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aura.aura_ui.mcp.log.LogRetentionPrefs
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.MonoTopBar
import com.aura.aura_ui.presentation.components.SilkMeshBackground
import com.aura.aura_ui.presentation.components.rememberMonoScheme
import com.aura.aura_ui.ui.theme.Mono

/**
 * Phase 10B — pick how many days to keep MCP session logs.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun McpLogRetentionScreen(
    onNavigateBack: () -> Unit,
) {
    val scheme = rememberMonoScheme()
    val context = LocalContext.current
    val prefs = remember { LogRetentionPrefs(context) }
    var days by remember { mutableIntStateOf(prefs.retentionDays()) }
    var customMode by remember { mutableStateOf(days !in LogRetentionPrefs.PRESETS) }
    var customText by remember { mutableStateOf(days.toString()) }

    Box(modifier = Modifier.fillMaxSize()) {
        SilkMeshBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            MonoTopBar(
                title = "Log retention",
                subtitle = "How long session logs are kept",
                scheme = scheme,
            )

            Text(
                "Session logs older than this are deleted automatically once per day.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.textSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LogRetentionPrefs.PRESETS.forEach { preset ->
                    RetentionChip(
                        label = "$preset days",
                        selected = !customMode && days == preset,
                        onClick = {
                            customMode = false
                            days = preset
                            prefs.setRetentionDays(preset)
                        },
                    )
                }
                RetentionChip(
                    label = "Custom",
                    selected = customMode,
                    onClick = {
                        customMode = true
                        customText = days.toString()
                    },
                )
            }

            if (customMode) {
                Spacer(Modifier.height(12.dp))
                LiquidGlassSurface(
                    shape = Mono.ShapeCardSmall,
                    cornerRadiusDp = 16.dp,
                    strongBlur = false,
                    tintAlpha = 0.58f,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { input -> customText = input.filter { it.isDigit() }.take(4) },
                            singleLine = true,
                            label = { Text("Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = LiquidPalette.SilkCyan,
                                unfocusedIndicatorColor = scheme.outline,
                                focusedLabelColor = LiquidPalette.SilkCyan,
                                unfocusedLabelColor = scheme.textSecondary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = LiquidPalette.SilkCyan,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        LiquidGlassButton(
                            modifier = Modifier.align(Alignment.End),
                            enabled = customText.toIntOrNull()?.let { it >= 1 } == true,
                            accentGlow = LiquidPalette.SilkCyan,
                            onClick = {
                                val value = customText.toIntOrNull()?.coerceAtLeast(1) ?: return@LiquidGlassButton
                                days = value
                                prefs.setRetentionDays(value)
                            },
                        ) { Text("Save") }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            LiquidGlassSurface(
                shape = Mono.ShapeCardSmall,
                cornerRadiusDp = 16.dp,
                strongBlur = false,
                tintAlpha = 0.56f,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Currently keeping logs for $days day${if (days == 1) "" else "s"}.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RetentionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    LiquidGlassButton(
        onClick = onClick,
        accentGlow = if (selected) LiquidPalette.SilkCyan else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) LiquidPalette.SilkCyan else Color.White,
        )
    }
}
