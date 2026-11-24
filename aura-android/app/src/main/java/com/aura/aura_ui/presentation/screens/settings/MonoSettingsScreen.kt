package com.aura.aura_ui.presentation.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aura.aura_ui.R
import com.aura.aura_ui.accessibility.AccessibilityCapturePolicy
import com.aura.aura_ui.accessibility.AuraAccessibilityService
import com.aura.aura_ui.agent.conversation.PersonaOwner
import com.aura.aura_ui.data.preferences.DiagnosticsStore
import com.aura.aura_ui.data.preferences.ThakurSecurityStore
import com.aura.aura_ui.data.preferences.ThemeManager
import com.aura.aura_ui.data.preferences.UnlockMethod
import com.aura.aura_ui.presentation.components.AuraScreenScaffold
import com.aura.aura_ui.presentation.components.AuraSwitch
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidGlassToggle
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.MonoCardGroup
import com.aura.aura_ui.presentation.components.MonoDivider
import com.aura.aura_ui.presentation.components.MonoRow
import com.aura.aura_ui.presentation.components.MonoSectionLabel
import com.aura.aura_ui.presentation.components.PermissionToggleRow
import com.aura.aura_ui.presentation.components.permissionIcon
import com.aura.aura_ui.presentation.components.rememberMonoScheme
import com.aura.aura_ui.presentation.permissions.PermissionRegistry
import com.aura.aura_ui.presentation.permissions.openPermissionSystemPage
import com.aura.aura_ui.presentation.screens.InteractivePatternPad
import com.aura.aura_ui.remote.AuraSignIn
import com.aura.aura_ui.remote.AuthState
import com.aura.aura_ui.remote.SignInOutcome
import com.aura.aura_ui.services.WakeWordListeningService
import com.aura.aura_ui.ui.theme.Mono
import com.aura.aura_ui.ui.theme.MonoScheme
import kotlinx.coroutines.launch

@Composable
fun MonoSettingsScreen(
    onOpenPermissions: () -> Unit,
    onOpenTavilyKey: () -> Unit,
    onRequestScreenCapture: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenRestrictedApps: () -> Unit = {},
    onOpenEvalSuite: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scheme = rememberMonoScheme()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember(context) {
        DiagnosticsStore.hydrate(context)
        ThakurSecurityStore.hydrate(context)
    }
    val diagnosticsEnabled by DiagnosticsStore.enabled.collectAsState()
    val wakeWordEnabled by ThemeManager.enableWakeWord.collectAsState()
    val haptics by ThemeManager.enableHapticFeedback.collectAsState()
    val edgeGlow by ThemeManager.enableEdgeGlow.collectAsState()
    val notifications by ThemeManager.enableNotifications.collectAsState()
    val screenCaptureAvailable by AuraAccessibilityService.screenCaptureAvailable.collectAsState()

    // THAKUR OS Security states
    val wakeWord by ThakurSecurityStore.wakeWord.collectAsState()
    val secretEnabled by ThakurSecurityStore.secretEnabled.collectAsState()
    val hasSecretCode by ThakurSecurityStore.hasSecretCode.collectAsState()
    val hasPinCode by ThakurSecurityStore.hasPinCode.collectAsState()
    val hasPatternCode by ThakurSecurityStore.hasPatternCode.collectAsState()
    val securedAccessEnabled by ThakurSecurityStore.securedAccessEnabled.collectAsState()
    val methodFingerprint by ThakurSecurityStore.methodFingerprint.collectAsState()
    val methodFace by ThakurSecurityStore.methodFace.collectAsState()
    val methodPattern by ThakurSecurityStore.methodPattern.collectAsState()
    val methodPin by ThakurSecurityStore.methodPin.collectAsState()
    val methodPassword by ThakurSecurityStore.methodPassword.collectAsState()

    var showWakeWordDialog by remember { mutableStateOf(false) }
    var showSecretDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }

    var refresh by remember { mutableIntStateOf(0) }
    var signInTick by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refresh++ }

    AuraScreenScaffold(title = "Settings", modifier = modifier) {
        // ── Profile ──────────────────────────────────────────────────────────
        val signedInEmail = remember(refresh, signInTick) { AuthState.email() }
        val displayName = remember(refresh, signInTick) {
            PersonaOwner.get(context) ?: signedInEmail?.substringBefore('@')
        }
        ProfileHeaderCard(
            scheme = scheme,
            name = displayName ?: "Guest",
            subtitle = signedInEmail ?: "Not signed in — tap to sign in with Google",
            signedIn = signedInEmail != null,
        ) {
            if (signedInEmail != null) {
                Toast.makeText(context, "Signed in as $signedInEmail", Toast.LENGTH_SHORT).show()
            } else {
                scope.launch {
                    when (val out = AuraSignIn(context).signIn(context)) {
                        is SignInOutcome.Success -> signInTick++
                        SignInOutcome.Cancelled -> Unit
                        SignInOutcome.NoGoogleAccount -> Toast.makeText(
                            context,
                            "No Google account on this phone. Add one in Android Settings.",
                            Toast.LENGTH_LONG,
                        ).show()
                        is SignInOutcome.Misconfigured ->
                            Toast.makeText(context, out.message, Toast.LENGTH_LONG).show()
                        is SignInOutcome.Failed ->
                            Toast.makeText(context, out.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // ── THAKUR OS Security ───────────────────────────────────────────────
        MonoSectionLabel("THAKUR OS Security", scheme)

        // 0. Secured Access ON/OFF & 5 Unlock Methods
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = Mono.ShapeCard,
            cornerRadiusDp = 24.dp,
            strongBlur = true,
            tintAlpha = 0.54f,
            prismIntensity = 1.25f,
            accentGlow = if (securedAccessEnabled) LiquidPalette.PrismBlue else LiquidPalette.SilkViolet,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = LiquidPalette.PrismBlue, modifier = Modifier.size(22.dp))
                        Column {
                            Text(
                                text = "Secured Access",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                            Text(
                                text = if (securedAccessEnabled) "Lock screen shown on every launch" else "OFF — app opens directly",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        }
                    }
                    LiquidGlassToggle(
                        checked = securedAccessEnabled,
                        onCheckedChange = { enabled ->
                            ThakurSecurityStore.setSecuredAccessEnabled(context, enabled)
                            Toast.makeText(
                                context,
                                if (enabled) "Secured Access enabled" else "Secured Access turned off",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    )
                }

         Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.White.copy(alpha = 0.12f))
)

                Text(
                    text = "Unlock Methods (enable any or all)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = LiquidPalette.PrismBlue,
                )

                UnlockMethodToggleRow(
                    icon = Icons.Filled.Fingerprint,
                    title = "Fingerprint",
                    subtitle = "Hardware biometric fingerprint",
                    checked = methodFingerprint,
                    onCheckedChange = { ThakurSecurityStore.setUnlockMethodEnabled(context, UnlockMethod.FINGERPRINT, it) },
                )
                UnlockMethodToggleRow(
                    icon = Icons.Filled.Face,
                    title = "Face Unlock",
                    subtitle = "Biometric face verification",
                    checked = methodFace,
                    onCheckedChange = { ThakurSecurityStore.setUnlockMethodEnabled(context, UnlockMethod.FACE, it) },
                )
                UnlockMethodToggleRow(
                    icon = Icons.Filled.Gesture,
                    title = "Pattern",
                    subtitle = if (hasPatternCode) "Encrypted pattern configured" else "Not configured yet",
                    checked = methodPattern,
                    actionLabel = if (hasPatternCode) "Change" else "Set",
                    onActionClick = { showPatternDialog = true },
                    onCheckedChange = { on ->
                        ThakurSecurityStore.setUnlockMethodEnabled(context, UnlockMethod.PATTERN, on)
                        if (on && !hasPatternCode) showPatternDialog = true
                    },
                )
                UnlockMethodToggleRow(
                    icon = Icons.Filled.Pin,
                    title = "PIN",
                    subtitle = if (hasPinCode) "Encrypted PIN configured" else "Not configured yet",
                    checked = methodPin,
                    actionLabel = if (hasPinCode) "Change" else "Set",
                    onActionClick = { showPinDialog = true },
                    onCheckedChange = { on ->
                        ThakurSecurityStore.setUnlockMethodEnabled(context, UnlockMethod.PIN, on)
                        if (on && !hasPinCode) showPinDialog = true
                    },
                )
                UnlockMethodToggleRow(
                    icon = Icons.Filled.Lock,
                    title = "Password",
                    subtitle = if (hasSecretCode) "Uses your encrypted Secret Code" else "Not configured yet",
                    checked = methodPassword,
                    actionLabel = if (hasSecretCode) "Change" else "Set",
                    onActionClick = { showSecretDialog = true },
                    onCheckedChange = { on ->
                        ThakurSecurityStore.setUnlockMethodEnabled(context, UnlockMethod.PASSWORD, on)
                        if (on && !hasSecretCode) showSecretDialog = true
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Section 1 — Wake Word
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = Mono.ShapeCard,
            cornerRadiusDp = 24.dp,
            strongBlur = true,
            tintAlpha = 0.54f,
            prismIntensity = 1.15f,
            accentGlow = LiquidPalette.SilkViolet,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = LiquidPalette.SilkCyan, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wake Word",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        Text(
                            text = "Speak this to wake THAKUR OS",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.68f),
                        )
                    }
                    LiquidGlassButton(
                        onClick = { showWakeWordDialog = true },
                        accentGlow = LiquidPalette.PrismBlue,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("Change")
                    }
                }
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    cornerRadiusDp = 14.dp,
                    strongBlur = false,
                    tintAlpha = 0.42f,
                    accentGlow = LiquidPalette.PrismBlue,
                ) {
                    Text(
                        text = "\"$wakeWord\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = LiquidPalette.SilkCyan,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Section 2 — Secret Code (Voice + Password Assistant Gate)
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = Mono.ShapeCard,
            cornerRadiusDp = 24.dp,
            strongBlur = true,
            tintAlpha = 0.54f,
            prismIntensity = 1.15f,
            accentGlow = if (secretEnabled) LiquidPalette.SilkCyan else LiquidPalette.SilkPurple,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null, tint = LiquidPalette.SilkViolet, modifier = Modifier.size(22.dp))
                        Column {
                            Text(
                                text = "Secret Code",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                            Text(
                                text = when {
                                    secretEnabled && hasSecretCode -> "ON — required before assistant opens"
                                    secretEnabled -> "ON — set a secret word or PIN below"
                                    else -> "OFF — assistant opens freely"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.70f),
                            )
                        }
                    }
                    LiquidGlassToggle(
                        checked = secretEnabled,
                        onCheckedChange = { on ->
                            ThakurSecurityStore.setSecretEnabled(context, on)
                            if (on && !hasSecretCode) showSecretDialog = true
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    LiquidGlassButton(
                        onClick = { showSecretDialog = true },
                        accentGlow = LiquidPalette.SilkViolet,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text(if (hasSecretCode) "Change Secret" else "Set Secret")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Section 3 — Reset
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = Mono.ShapeCard,
            cornerRadiusDp = 24.dp,
            strongBlur = false,
            tintAlpha = 0.48f,
            accentGlow = LiquidPalette.DangerRed,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = LiquidPalette.DangerRed, modifier = Modifier.size(22.dp))
                    Column {
                        Text(
                            text = "Reset Security Defaults",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        Text(
                            text = "Restores \"Wake up THAKUR OS\" & turns locks OFF",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }
                }
                LiquidGlassButton(
                    onClick = {
                        ThakurSecurityStore.resetToDefault(context)
                        WakeWordListeningService.reloadWakeWord(context)
                        Toast.makeText(context, "Wake word & security reset to default", Toast.LENGTH_SHORT).show()
                    },
                    destructive = true,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("Reset to Default")
                }
            }
        }

        // ── Access — uniform permission toggles ──────────────────────────────
        MonoSectionLabel("Access", scheme)
        MonoCardGroup(scheme) {
            PermissionRegistry.tier1.forEachIndexed { index, permission ->
                if (index > 0) MonoDivider()
                val granted = remember(permission.id, refresh) { permission.isGranted(context) }
                PermissionToggleRow(
                    permission = permission,
                    granted = granted,
                    icon = permissionIcon(permission.id),
                    onRequestRuntime = { runtimeLauncher.launch(it.toTypedArray()) },
                    onOpenSystem = {
                        openPermissionSystemPage(context, permission, onScreenCapture = onRequestScreenCapture)
                    },
                )
            }
            if (AccessibilityCapturePolicy.MIRROR_ENABLED) {
                MonoDivider()
                MonoRow(
                    icon = Icons.Outlined.Screenshot,
                    title = "Screen capture",
                    subtitle = if (screenCaptureAvailable) "Enabled for this session" else "Let the agent see the screen",
                    trailing = {
                        AuraSwitch(
                            checked = screenCaptureAvailable,
                            onCheckedChange = { enabled ->
                                if (enabled && !screenCaptureAvailable) onRequestScreenCapture()
                            },
                        )
                    },
                )
            }
            MonoDivider()
            MonoRow(
                icon = Icons.Outlined.Security,
                title = "See all permissions",
                subtitle = "Manage everything THAKUR OS can access",
                onClick = onOpenPermissions,
            )
        }

        // ── Safety ───────────────────────────────────────────────────────────
        MonoSectionLabel("Safety", scheme)
        MonoCardGroup(scheme) {
            MonoRow(
                icon = Icons.Outlined.Block,
                title = "Restricted Apps",
                subtitle = "Apps THAKUR OS declines to act in — banking, payments, auth",
                onClick = onOpenRestrictedApps,
            )
        }

        // ── Integrations ─────────────────────────────────────────────────────
        MonoSectionLabel("Integrations", scheme)
        MonoCardGroup(scheme) {
            MonoRow(
                icon = Icons.Outlined.Search,
                title = "Web search (Tavily)",
                subtitle = "API key for live web lookups",
                onClick = onOpenTavilyKey,
            )
        }

        // ── Preferences ──────────────────────────────────────────────────────
        MonoSectionLabel("Preferences", scheme)
        MonoCardGroup(scheme) {
            MonoRow(
                icon = Icons.Outlined.Hearing,
                title = "\"$wakeWord\" wake word",
                trailing = {
                    AuraSwitch(
                        checked = wakeWordEnabled,
                        onCheckedChange = { enabled ->
                            ThemeManager.updateWakeWord(enabled)
                            if (enabled) {
                                WakeWordListeningService.start(context)
                            } else {
                                WakeWordListeningService.stop(context)
                            }
                        },
                    )
                },
            )
            MonoDivider()
            MonoRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                trailing = {
                    AuraSwitch(
                        checked = notifications,
                        onCheckedChange = ThemeManager::updateNotifications,
                    )
                },
            )
            if (android.os.Build.VERSION.SDK_INT >= 36) {
                MonoDivider()
                MonoRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Live Updates",
                    subtitle = "Manage status bar chip settings",
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                )
            }
            MonoDivider()
            MonoRow(
                icon = Icons.Outlined.Vibration,
                title = "Haptic feedback",
                trailing = {
                    AuraSwitch(
                        checked = haptics,
                        onCheckedChange = ThemeManager::updateHapticFeedback,
                    )
                },
            )
            MonoDivider()
            MonoRow(
                icon = Icons.Outlined.BlurOn,
                title = "Edge lighting",
                subtitle = "White glow around the screen while THAKUR OS is open or working",
                trailing = {
                    AuraSwitch(
                        checked = edgeGlow,
                        onCheckedChange = ThemeManager::updateEdgeGlow,
                    )
                },
            )
        }

        // ── Privacy & Data ───────────────────────────────────────────────────
        MonoSectionLabel("Privacy & Data", scheme)
        MonoCardGroup(scheme) {
            MonoRow(
                icon = Icons.Outlined.Insights,
                title = "Share diagnostics",
                subtitle = "Your Google email, phone model, app version, usage counts, crash reports. " +
                    "Never your tasks, screen or messages.",
                trailing = {
                    AuraSwitch(
                        checked = diagnosticsEnabled,
                        onCheckedChange = { DiagnosticsStore.setEnabled(context, it) },
                    )
                },
            )
            MonoDivider()
            MonoRow(icon = Icons.Outlined.Shield, title = "Privacy Policy", onClick = onOpenPrivacy)
            MonoDivider()
            MonoRow(icon = Icons.Outlined.Description, title = "Terms of Service", onClick = onOpenTerms)
            MonoDivider()
            MonoRow(icon = Icons.Outlined.Code, title = "Open-source licenses", onClick = onOpenLicenses)
        }

        // ── Developer (debug builds only) ────────────────────────────────────
        onOpenEvalSuite?.let { openEval ->
            MonoSectionLabel("Developer", scheme)
            MonoCardGroup(scheme) {
                MonoRow(icon = Icons.Outlined.Science, title = "Eval suite", onClick = openEval)
            }
        }

        // ── Community ────────────────────────────────────────────────────────
        MonoSectionLabel("Community", scheme)
        MonoCardGroup(scheme) {
            MonoRow(
                icon = Icons.Outlined.Forum,
                title = "Join the THAKUR OS Discord",
                subtitle = "Chat with us, leave a review, report bugs",
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(DISCORD_INVITE))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
                trailing = {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = scheme.textSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

        // ── About (with full THAKUR OS branding image) ───────────────────────
        MonoSectionLabel("About", scheme)
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = Mono.ShapeCard,
            cornerRadiusDp = 24.dp,
            strongBlur = true,
            tintAlpha = 0.56f,
            prismIntensity = 1.3f,
            accentGlow = LiquidPalette.PrismBlue,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.thakur_brand_full),
                    contentDescription = "THAKUR OS Branding",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(164.dp)
                        .clip(RoundedCornerShape(24.dp)),
                )
                Text(
                    text = "THAKUR OS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                    ),
                    color = Color.White,
                )
                Text(
                    text = "THINK • SEE • ACT • COMPLETE",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                    color = LiquidPalette.SilkCyan,
                )
                Text(
                    text = "by Shivam Thakur",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = LiquidPalette.PrismRose,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MonoCardGroup(scheme) {
            val (versionName, versionCode) = remember { appVersion(context) }
            MonoRow(
                icon = Icons.Outlined.Info,
                title = "Version",
                trailing = {
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.textSecondary,
                    )
                },
            )
            MonoDivider()
            MonoRow(
                icon = Icons.Outlined.Info,
                title = "Build",
                trailing = {
                    Text(
                        text = versionCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.textSecondary,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // ── Security Dialogs ─────────────────────────────────────────────────────
    if (showWakeWordDialog) {
        var input by remember { mutableStateOf(wakeWord) }
        AlertDialog(
            onDismissRequest = { showWakeWordDialog = false },
            containerColor = Color(0xFF0E1324),
            title = { Text("Change Wake Word", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Type any phrase (e.g. \"Hello Thakur\") to wake THAKUR OS.",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        label = { Text("Wake phrase") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LiquidPalette.PrismBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedLabelColor = LiquidPalette.PrismBlue,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.65f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        ThakurSecurityStore.setWakeWord(context, input)
                        WakeWordListeningService.reloadWakeWord(context)
                        showWakeWordDialog = false
                        Toast.makeText(context, "Wake word updated immediately", Toast.LENGTH_SHORT).show()
                    },
                    enabled = input.isNotBlank(),
                    accentGlow = LiquidPalette.PrismBlue,
                ) { Text("Save") }
            },
            dismissButton = {
                LiquidGlassButton(onClick = { showWakeWordDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showSecretDialog) {
        var input by remember { mutableStateOf("") }
        var showChars by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            containerColor = Color(0xFF0E1324),
            title = { Text("Set Secret Code / Password", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Stored in encrypted storage for Secret Code lock & Password unlock.",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        label = { Text("Secret word or password") },
                        visualTransformation = if (showChars) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showChars = !showChars }) {
                                Icon(
                                    imageVector = if (showChars) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showChars) "Hide" else "Show",
                                    tint = LiquidPalette.PrismBlue,
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LiquidPalette.SilkViolet,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            focusedLabelColor = LiquidPalette.SilkViolet,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.65f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        ThakurSecurityStore.setSecretCode(context, input)
                        showSecretDialog = false
                        Toast.makeText(context, "Secret code encrypted & saved", Toast.LENGTH_SHORT).show()
                    },
                    enabled = input.isNotBlank(),
                    accentGlow = LiquidPalette.SilkViolet,
                ) { Text("Save") }
            },
            dismissButton = {
                LiquidGlassButton(onClick = { showSecretDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var showPin by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = Color(0xFF0E1324),
            title = { Text("Set Secured Access PIN", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pinInput = it },
                    singleLine = true,
                    label = { Text("4-8 digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPin) "Hide" else "Show",
                                tint = LiquidPalette.PrismBlue,
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LiquidPalette.PrismBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        focusedLabelColor = LiquidPalette.PrismBlue,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.65f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        ThakurSecurityStore.setPinCode(context, pinInput)
                        showPinDialog = false
                        Toast.makeText(context, "PIN encrypted & saved", Toast.LENGTH_SHORT).show()
                    },
                    enabled = pinInput.length >= 4,
                    accentGlow = LiquidPalette.PrismBlue,
                ) { Text("Save") }
            },
            dismissButton = {
                LiquidGlassButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showPatternDialog) {
        val selectedNodes = remember { mutableStateListOf<Int>() }
        var dragPos by remember { mutableStateOf<Offset?>(null) }
        AlertDialog(
            onDismissRequest = { showPatternDialog = false },
            containerColor = Color(0xFF0E1324),
            title = { Text("Draw New Pattern", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (selectedNodes.size >= 4) "Pattern recorded (${selectedNodes.size} dots)" else "Connect at least 4 dots",
                        color = LiquidPalette.PrismBlue,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    InteractivePatternPad(
                        selectedNodes = selectedNodes,
                        dragPosition = dragPos,
                        primaryGlow = LiquidPalette.SilkViolet,
                        secondaryGlow = LiquidPalette.PrismBlue,
                        onStart = { selectedNodes.clear() },
                        onUpdateDrag = { dragPos = it },
                        onFinish = { dragPos = null },
                        modifier = Modifier.size(210.dp),
                    )
                }
            },
            confirmButton = {
                LiquidGlassButton(
                    onClick = {
                        ThakurSecurityStore.setPatternCode(context, selectedNodes.joinToString("-"))
                        showPatternDialog = false
                        Toast.makeText(context, "Pattern encrypted & saved", Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedNodes.size >= 4,
                    accentGlow = LiquidPalette.SilkViolet,
                ) { Text("Save") }
            },
            dismissButton = {
                LiquidGlassButton(onClick = { showPatternDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun UnlockMethodToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(19.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f),
            )
        }
        if (actionLabel != null && onActionClick != null) {
            LiquidGlassButton(
                onClick = onActionClick,
                accentGlow = LiquidPalette.PrismBlue,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(actionLabel, fontSize = 11.5.sp)
            }
        }
        LiquidGlassToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Liquid Glass account card matching Settings' theme. */
@Composable
private fun ProfileHeaderCard(
    scheme: MonoScheme,
    name: String,
    subtitle: String,
    signedIn: Boolean,
    onClick: () -> Unit,
) {
    LiquidGlassSurface(
        onClick = onClick,
        shape = Mono.ShapeCard,
        cornerRadiusDp = 24.dp,
        strongBlur = true,
        tintAlpha = 0.56f,
        prismIntensity = 1.2f,
        accentGlow = LiquidPalette.PrismBlue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(LiquidPalette.SilkViolet, LiquidPalette.PrismBlue)),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (signedIn) Icons.Outlined.Verified else Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private const val DISCORD_INVITE = "https://discord.com/invite/H66ws9zMPF"

private fun appVersion(context: Context): Pair<String, String> = try {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    val code = if (android.os.Build.VERSION.SDK_INT >= 28) {
        info.longVersionCode.toString()
    } else {
        @Suppress("DEPRECATION") info.versionCode.toString()
    }
    (info.versionName ?: "—") to code
} catch (e: Exception) {
    "—" to "—"
}
