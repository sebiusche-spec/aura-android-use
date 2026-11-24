package com.aura.aura_ui.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ============================================================================
// AURA SWITCH — Liquid Glass toggle used across settings, permissions, prefs.
// ============================================================================

@Composable
fun AuraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    LiquidGlassToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}
