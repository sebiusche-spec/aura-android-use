package com.aura.aura_ui.presentation.screens.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aura.aura_ui.R
import com.aura.aura_ui.agent.conversation.CompanionConfig
import com.aura.aura_ui.agent.conversation.LiveModelResolver
import com.aura.aura_ui.agent.conversation.PersonaOwner
import com.aura.aura_ui.agent.llm.LlmEndpointCatalog
import com.aura.aura_ui.agent.llm.ModelCatalog
import com.aura.aura_ui.agent.llm.RestrictedAppClassifier
import com.aura.aura_ui.data.preferences.ThakurSecurityStore
import com.aura.aura_ui.mcp.AppRestrictedApps
import com.aura.aura_ui.mcp.bridge.ProviderKeyStore
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidGlassToggle
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.components.SilkMeshBackground
import com.aura.aura_ui.presentation.components.permissionIcon
import com.aura.aura_ui.presentation.permissions.PermissionRegistry
import com.aura.aura_ui.presentation.permissions.openPermissionSystemPage
import com.aura.aura_ui.presentation.utils.HapticUtils
import com.aura.aura_ui.remote.AuraSignIn
import com.aura.aura_ui.remote.AuthState
import com.aura.aura_ui.remote.SignInOutcome
import com.aura.aura_ui.services.WakeWordListeningService
import com.aura.aura_ui.telemetry.AuraAnalytics
import com.aura.aura_ui.utils.AppInventoryScanner
import com.aura.aura_ui.utils.isLaunchable
import com.aura.mcp.bridge.RestrictedAppEntry
import com.aura.mcp.bridge.RestrictedTier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================================
// THAKUR OS — 5-PAGE LIQUID GLASS ONBOARDING FLOW
//
// 0: Welcome (Full THAKUR OS emblem, tagline, by Shivam Thakur, Next)
// 1: What I Can Do (Think - Samajhta hu, See - Screen dekhta hu, Act - Khud kaam karta hu)
// 2: Set Wake Word (Default "Wake up THAKUR OS" + Save)
// 3: Set Secret Code (Encrypted phrase/PIN + ON/OFF toggle + Save)
// 4: Ready (Configured summary, optional Device/AI Setup sheet, "Start THAKUR OS")
// ============================================================================

private const val ONBOARDING_PAGE_COUNT = 5

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { ONBOARDING_PAGE_COUNT })

    LaunchedEffect(Unit) {
        ThakurSecurityStore.hydrate(context)
    }

    LaunchedEffect(pagerState.currentPage) {
        AuraAnalytics.onboardingStep(context, pagerState.currentPage)
    }

    val savedWakeWord by ThakurSecurityStore.wakeWord.collectAsState()
    val savedSecretEnabled by ThakurSecurityStore.secretEnabled.collectAsState()
    val isWorkMode by ThakurSecurityStore.isWorkTheme.collectAsState()
    val themeGlow = if (isWorkMode) LiquidPalette.WorkOrange else LiquidPalette.SilkViolet

    var wakeWordDraft by remember(savedWakeWord) {
        mutableStateOf(savedWakeWord.ifBlank { ThakurSecurityStore.DEFAULT_WAKE_WORD })
    }
    var wakeWordSavedFeedback by remember { mutableStateOf(false) }

    var secretToggle by remember(savedSecretEnabled) { mutableStateOf(savedSecretEnabled) }
    var secretDraft by remember {
        mutableStateOf(ThakurSecurityStore.getSecretCode(context).orEmpty())
    }
    var secretVisible by remember { mutableStateOf(false) }
    var secretSavedFeedback by remember { mutableStateOf(false) }

    var showDeviceSetupSheet by remember { mutableStateOf(false) }
    var refreshPermissions by remember { mutableIntStateOf(0) }
    var isSignedIn by remember { mutableStateOf(AuthState.isSignedIn()) }
    var signedInEmail by remember { mutableStateOf(AuthState.email()) }
    var signingIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf(PersonaOwner.get(context).orEmpty()) }
    var keyVerified by remember {
        mutableStateOf(ProviderKeyStore(context.applicationContext).isConfigured(LlmEndpointCatalog.GEMINI_ID))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions++
                isSignedIn = AuthState.isSignedIn()
                AuthState.email()?.let { signedInEmail = it }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refreshPermissions++ }

    fun completeOnboarding() {
        HapticUtils.performSuccessHaptic(context)
        AuraAnalytics.onboardingComplete(context)
        onDone()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidPalette.DeepNight),
    ) {
        SilkMeshBackground(modifier = Modifier.fillMaxSize())

        // Ambient blue-purple neon wash matching THAKUR OS emblem
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            themeGlow.copy(alpha = 0.22f),
                            LiquidPalette.PrismBlue.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .padding(horizontal = 20.dp),
        ) {
            // Top bar with optional Back + Skip pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (pagerState.currentPage > 0) {
                    LiquidGlassSurface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        cornerRadiusDp = 20.dp,
                        strongBlur = false,
                        tintAlpha = 0.48f,
                        accentGlow = themeGlow,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.size(40.dp))
                }

                LiquidGlassSurface(
                    shape = RoundedCornerShape(50),
                    cornerRadiusDp = 16.dp,
                    strongBlur = false,
                    tintAlpha = 0.42f,
                    accentGlow = LiquidPalette.PrismBlue,
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / $ONBOARDING_PAGE_COUNT",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }

                if (pagerState.currentPage < ONBOARDING_PAGE_COUNT - 1) {
                    LiquidGlassButton(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(ONBOARDING_PAGE_COUNT - 1) }
                        },
                        accentGlow = themeGlow,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("Skip", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(Modifier.size(40.dp))
                }
            }

            // 5-Page Swipeable Onboarding Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                when (page) {
                    0 -> WelcomeOnboardingPage(
                        themeGlow = themeGlow,
                        onNext = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                    )
                    1 -> WhatICanDoPage(
                        themeGlow = themeGlow,
                        onNext = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                    )
                    2 -> WakeWordOnboardingPage(
                        wakeWordDraft = wakeWordDraft,
                        savedFeedback = wakeWordSavedFeedback,
                        themeGlow = themeGlow,
                        onDraftChange = {
                            wakeWordDraft = it
                            wakeWordSavedFeedback = false
                        },
                        onResetDefault = {
                            wakeWordDraft = ThakurSecurityStore.DEFAULT_WAKE_WORD
                            ThakurSecurityStore.setWakeWord(context, ThakurSecurityStore.DEFAULT_WAKE_WORD)
                            WakeWordListeningService.reloadWakeWord(context)
                            wakeWordSavedFeedback = true
                        },
                        onSave = {
                            HapticUtils.performSuccessHaptic(context)
                            ThakurSecurityStore.setWakeWord(context, wakeWordDraft)
                            WakeWordListeningService.reloadWakeWord(context)
                            wakeWordSavedFeedback = true
                        },
                        onNext = {
                            ThakurSecurityStore.setWakeWord(context, wakeWordDraft)
                            WakeWordListeningService.reloadWakeWord(context)
                            scope.launch { pagerState.animateScrollToPage(3) }
                        },
                    )
                    3 -> SecretCodeOnboardingPage(
                        secretEnabled = secretToggle,
                        secretDraft = secretDraft,
                        secretVisible = secretVisible,
                        savedFeedback = secretSavedFeedback,
                        themeGlow = themeGlow,
                        onToggleEnabled = { enabled ->
                            secretToggle = enabled
                            ThakurSecurityStore.setSecretEnabled(context, enabled)
                            secretSavedFeedback = false
                        },
                        onSecretChange = {
                            secretDraft = it
                            secretSavedFeedback = false
                        },
                        onToggleVisibility = { secretVisible = !secretVisible },
                        onSave = {
                            HapticUtils.performSuccessHaptic(context)
                            val clean = secretDraft.trim()
                            if (clean.isNotEmpty()) {
                                ThakurSecurityStore.setSecretCode(context, clean)
                                if (clean.all { it.isDigit() }) {
                                    ThakurSecurityStore.setPinCode(context, clean)
                                }
                            }
                            ThakurSecurityStore.setSecretEnabled(context, secretToggle)
                            secretSavedFeedback = true
                        },
                        onNext = {
                            val clean = secretDraft.trim()
                            if (clean.isNotEmpty()) {
                                ThakurSecurityStore.setSecretCode(context, clean)
                                if (clean.all { it.isDigit() }) {
                                    ThakurSecurityStore.setPinCode(context, clean)
                                }
                            }
                            ThakurSecurityStore.setSecretEnabled(context, secretToggle)
                            scope.launch { pagerState.animateScrollToPage(4) }
                        },
                    )
                    else -> ReadyOnboardingPage(
                        wakeWord = savedWakeWord,
                        secretEnabled = savedSecretEnabled,
                        isSignedIn = isSignedIn,
                        keyVerified = keyVerified,
                        refreshPermissions = refreshPermissions,
                        themeGlow = themeGlow,
                        onOpenDeviceSetup = { showDeviceSetupSheet = true },
                        onStart = ::completeOnboarding,
                    )
                }
            }

            // Bottom Animated Neon Dot Indicators
            OnboardingDotsIndicator(
                pageCount = ONBOARDING_PAGE_COUNT,
                currentPage = pagerState.currentPage,
                accentColor = themeGlow,
                onSelectPage = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 14.dp),
            )
        }

        // Optional Full-Setup Liquid Glass Modal (Account · Permissions · Gemini Key · App Guard)
        AnimatedVisibility(
            visible = showDeviceSetupSheet,
            enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.94f),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.94f),
        ) {
            DeviceSetupLiquidModal(
                name = userName,
                onNameChanged = {
                    userName = it
                    PersonaOwner.set(context, it)
                },
                signedInEmail = signedInEmail,
                signedIn = isSignedIn,
                signingIn = signingIn,
                signInError = signInError,
                onSignIn = {
                    signingIn = true
                    signInError = null
                    scope.launch {
                        when (val out = AuraSignIn(context).signIn(context)) {
                            is SignInOutcome.Success -> {
                                signedInEmail = out.email ?: AuthState.email()
                                isSignedIn = true
                            }
                            SignInOutcome.Cancelled -> Unit
                            SignInOutcome.NoGoogleAccount -> signInError =
                                "No Google account on this phone. Add one in Android Settings, then try again."
                            is SignInOutcome.Misconfigured -> signInError = out.message
                            is SignInOutcome.Failed -> signInError = out.message
                        }
                        signingIn = false
                    }
                },
                refreshTrigger = refreshPermissions,
                onRequestRuntime = { runtimeLauncher.launch(it.toTypedArray()) },
                onOpenSystem = { openPermissionSystemPage(context, it, onScreenCapture = {}) },
                onVerifiedChanged = { keyVerified = it },
                onDismiss = { showDeviceSetupSheet = false },
            )
        }
    }
}

// ── Page 1: Welcome ──────────────────────────────────────────────────────────

@Composable
private fun WelcomeOnboardingPage(
    themeGlow: Color,
    onNext: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "welcomeGlow")
    val pulse by infinite.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "welcomePulse",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(6.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Full branding logo inside glowing neon Liquid Glass frame
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(244.dp)
                        .scale(0.95f + 0.08f * pulse)
                        .blur(32.dp, BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LiquidPalette.PrismBlue.copy(alpha = 0.44f * pulse),
                                    themeGlow.copy(alpha = 0.36f * pulse),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                LiquidGlassSurface(
                    modifier = Modifier.size(224.dp),
                    shape = RoundedCornerShape(34.dp),
                    cornerRadiusDp = 34.dp,
                    strongBlur = true,
                    tintAlpha = 0.44f,
                    prismIntensity = 1.35f,
                    accentGlow = LiquidPalette.PrismBlue,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.thakur_brand_full),
                        contentDescription = "THAKUR OS Brand",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .clip(RoundedCornerShape(28.dp)),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "THAKUR OS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.6.sp,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "THINK  •  SEE  •  ACT  •  COMPLETE",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
                color = LiquidPalette.PrismBlue,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            LiquidGlassSurface(
                shape = RoundedCornerShape(50),
                cornerRadiusDp = 16.dp,
                strongBlur = false,
                tintAlpha = 0.42f,
                accentGlow = themeGlow,
            ) {
                Text(
                    text = "by Shivam Thakur",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = Color(0xFFE0C8FF),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LiquidGlassButton(
            onClick = onNext,
            accentGlow = LiquidPalette.PrismBlue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Page 2: What I Can Do ────────────────────────────────────────────────────

private data class CapabilityShowcase(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val phaseOffset: Float,
)

@Composable
private fun WhatICanDoPage(
    themeGlow: Color,
    onNext: () -> Unit,
) {
    val cards = remember {
        listOf(
            CapabilityShowcase(
                title = "Think - Samajhta hu",
                subtitle = "Natural Hindi, Hinglish & English commands ko samajh kar smart plan banata hai.",
                icon = Icons.Outlined.Psychology,
                accent = LiquidPalette.SilkViolet,
                phaseOffset = 0f,
            ),
            CapabilityShowcase(
                title = "See - Screen dekhta hu",
                subtitle = "Aapki active screen, apps aur buttons ko safely read karke context samajhta hai.",
                icon = Icons.Filled.Visibility,
                accent = LiquidPalette.PrismBlue,
                phaseOffset = 0.33f,
            ),
            CapabilityShowcase(
                title = "Act - Khud kaam karta hu",
                subtitle = "Taps, typing, scrolling aur multi-app workflows bina haath lagaye complete karta hai.",
                icon = Icons.Filled.TouchApp,
                accent = LiquidPalette.SilkCyan,
                phaseOffset = 0.66f,
            ),
        )
    }

    val infinite = rememberInfiniteTransition(label = "capPulse")
    val animPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "capAnimPhase",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "What I Can Do",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "THAKUR OS thinks, sees, and executes across your phone",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB5C4E4),
            )
            Spacer(Modifier.height(18.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                cards.forEach { card ->
                    val wave = kotlin.math.sin((animPhase + card.phaseOffset) * 2f * kotlin.math.PI.toFloat())
                    val floatOffsetDp = (wave * 3f).dp
                    val badgeScale = 1f + wave * 0.06f

                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = floatOffsetDp.toPx() },
                        shape = RoundedCornerShape(22.dp),
                        cornerRadiusDp = 22.dp,
                        strongBlur = true,
                        tintAlpha = 0.56f,
                        prismIntensity = 1.25f,
                        accentGlow = card.accent,
                        onClick = {},
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(badgeScale)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                card.accent.copy(alpha = 0.34f),
                                                LiquidPalette.PrismBlue.copy(alpha = 0.22f),
                                            ),
                                        ),
                                    )
                                    .border(1.2.dp, card.accent.copy(alpha = 0.78f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = card.icon,
                                    contentDescription = card.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color.White,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = card.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                                    color = Color(0xFFCAD6F2),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LiquidGlassButton(
            onClick = onNext,
            accentGlow = themeGlow,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Page 3: Set Wake Word ────────────────────────────────────────────────────

@Composable
private fun WakeWordOnboardingPage(
    wakeWordDraft: String,
    savedFeedback: Boolean,
    themeGlow: Color,
    onDraftChange: (String) -> Unit,
    onResetDefault: () -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Set Wake Word",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Speak this custom phrase anytime to wake THAKUR OS hands-free.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB5C4E4),
            )
            Spacer(Modifier.height(20.dp))

            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                cornerRadiusDp = 24.dp,
                strongBlur = true,
                tintAlpha = 0.56f,
                prismIntensity = 1.25f,
                accentGlow = LiquidPalette.PrismBlue,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LiquidPalette.PrismBlue.copy(alpha = 0.22f))
                                .border(1.dp, LiquidPalette.PrismBlue.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RecordVoiceOver,
                                contentDescription = null,
                                tint = LiquidPalette.PrismBlue,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Summon Phrase",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                            Text(
                                text = "Works immediately without restart",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9EB4DC),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = wakeWordDraft,
                        onValueChange = onDraftChange,
                        singleLine = true,
                        label = { Text("Wake Word") },
                        placeholder = { Text(ThakurSecurityStore.DEFAULT_WAKE_WORD) },
                        shape = RoundedCornerShape(14.dp),
                        colors = onboardingTextFieldColors(LiquidPalette.PrismBlue),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiquidGlassButton(
                            onClick = onResetDefault,
                            accentGlow = themeGlow,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Text("Reset", fontSize = 13.sp)
                        }

                        LiquidGlassButton(
                            onClick = onSave,
                            enabled = wakeWordDraft.isNotBlank(),
                            accentGlow = LiquidPalette.SilkCyan,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                            Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = savedFeedback) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = LiquidPalette.SilkCyan,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Wake word active: \"${wakeWordDraft.trim().ifBlank { ThakurSecurityStore.DEFAULT_WAKE_WORD }}\"",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = LiquidPalette.SilkCyan,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LiquidGlassButton(
            onClick = onNext,
            accentGlow = LiquidPalette.PrismBlue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Page 4: Set Secret Code ──────────────────────────────────────────────────

@Composable
private fun SecretCodeOnboardingPage(
    secretEnabled: Boolean,
    secretDraft: String,
    secretVisible: Boolean,
    savedFeedback: Boolean,
    themeGlow: Color,
    onToggleEnabled: (Boolean) -> Unit,
    onSecretChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Set Secret Code",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Require a private secret word or PIN before THAKUR OS activates.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB5C4E4),
            )
            Spacer(Modifier.height(20.dp))

            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                cornerRadiusDp = 24.dp,
                strongBlur = true,
                tintAlpha = 0.56f,
                prismIntensity = 1.25f,
                accentGlow = themeGlow,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(themeGlow.copy(alpha = 0.22f))
                                    .border(1.dp, themeGlow.copy(alpha = 0.72f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "Secret Code Lock",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                                Text(
                                    text = if (secretEnabled) "Active on unlock" else "Currently OFF (optional)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (secretEnabled) LiquidPalette.SilkCyan else Color(0xFF9EB4DC),
                                )
                            }
                        }

                        LiquidGlassToggle(
                            checked = secretEnabled,
                            onCheckedChange = onToggleEnabled,
                        )
                    }

                    OutlinedTextField(
                        value = secretDraft,
                        onValueChange = onSecretChange,
                        singleLine = true,
                        label = { Text("Secret word or PIN") },
                        placeholder = { Text("e.g. open sesame or 4829") },
                        visualTransformation = if (secretVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            LiquidGlassSurface(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(34.dp),
                                shape = CircleShape,
                                cornerRadiusDp = 17.dp,
                                strongBlur = false,
                                tintAlpha = 0.45f,
                                onClick = onToggleVisibility,
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (secretVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (secretVisible) "Hide secret" else "Show secret",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = onboardingTextFieldColors(themeGlow),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "AES-256 Encrypted Storage",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8CA6D4),
                        )

                        LiquidGlassButton(
                            onClick = onSave,
                            enabled = secretDraft.isNotBlank() || !secretEnabled,
                            accentGlow = LiquidPalette.SilkCyan,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        ) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                            Text("Save", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = savedFeedback) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = LiquidPalette.SilkCyan,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (secretEnabled) "Secret code encrypted & enabled" else "Preferences saved",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = LiquidPalette.SilkCyan,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LiquidGlassButton(
            onClick = onNext,
            accentGlow = themeGlow,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Page 5: Ready ────────────────────────────────────────────────────────────

@Composable
private fun ReadyOnboardingPage(
    wakeWord: String,
    secretEnabled: Boolean,
    isSignedIn: Boolean,
    keyVerified: Boolean,
    refreshPermissions: Int,
    themeGlow: Color,
    onOpenDeviceSetup: () -> Unit,
    onStart: () -> Unit,
) {
    val context = LocalContext.current
    val grantedCount = remember(refreshPermissions) {
        PermissionRegistry.all.count { it.isGranted(context) }
    }
    val totalPerms = PermissionRegistry.all.size

    val infinite = rememberInfiniteTransition(label = "readyOrb")
    val pulse by infinite.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "readyPulse",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .scale(pulse)
                        .blur(26.dp, BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    LiquidPalette.PrismBlue.copy(alpha = 0.55f),
                                    themeGlow.copy(alpha = 0.42f),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                LiquidGlassSurface(
                    modifier = Modifier.size(116.dp),
                    shape = CircleShape,
                    cornerRadiusDp = 58.dp,
                    strongBlur = true,
                    tintAlpha = 0.52f,
                    prismIntensity = 1.4f,
                    accentGlow = LiquidPalette.PrismBlue,
                ) {
                    Image(
                        painter = painterResource(R.drawable.thakur_crown_t_mark),
                        contentDescription = "THAKUR OS Emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(CircleShape),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "You're Ready",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "THAKUR OS is tuned and ready for your command.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB5C4E4),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))

            // Configured Summary Glass Card
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                cornerRadiusDp = 22.dp,
                strongBlur = true,
                tintAlpha = 0.54f,
                prismIntensity = 1.2f,
                accentGlow = themeGlow,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SummaryRow(
                        label = "Wake Word",
                        value = "\"$wakeWord\"",
                        accent = LiquidPalette.PrismBlue,
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    SummaryRow(
                        label = "Secret Code Lock",
                        value = if (secretEnabled) "Enabled (Encrypted)" else "Off",
                        accent = if (secretEnabled) LiquidPalette.SilkCyan else Color(0xFF9EB4DC),
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    SummaryRow(
                        label = "Account & AI Brain",
                        value = buildString {
                            append(if (isSignedIn) "Signed in" else "Guest")
                            append(" · ")
                            append(if (keyVerified) "Gemini ready" else "Key optional")
                        },
                        accent = if (isSignedIn && keyVerified) LiquidPalette.SilkCyan else Color(0xFFCAD6F2),
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    SummaryRow(
                        label = "Device Permissions",
                        value = "$grantedCount / $totalPerms granted",
                        accent = if (grantedCount == totalPerms) LiquidPalette.SilkCyan else Color(0xFFF6C667),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Optional Quick Config Sheet Trigger (preserves Sign-in, Permissions, Gemini Key, App Guard)
            LiquidGlassButton(
                onClick = onOpenDeviceSetup,
                accentGlow = themeGlow,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Filled.Tune, null, modifier = Modifier.size(18.dp))
                Text(
                    text = "Device & AI Setup (Account · Permissions · Key)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LiquidGlassButton(
            onClick = onStart,
            accentGlow = LiquidPalette.PrismBlue,
            shape = RoundedCornerShape(28.dp),
            cornerRadiusDp = 28.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = "Start THAKUR OS",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9EB4DC),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Bottom Animated Dots Indicator ───────────────────────────────────────────

@Composable
private fun OnboardingDotsIndicator(
    pageCount: Int,
    currentPage: Int,
    accentColor: Color,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 28.dp else 8.dp,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "dotWidth_$index",
            )
            val color by animateColorAsState(
                targetValue = if (selected) LiquidPalette.PrismBlue else accentColor.copy(alpha = 0.42f),
                animationSpec = tween(300),
                label = "dotColor_$index",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            Brush.horizontalGradient(listOf(LiquidPalette.PrismBlue, accentColor))
                        } else {
                            Brush.horizontalGradient(listOf(color, color))
                        },
                    )
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) Color.White.copy(alpha = 0.65f) else Color.Transparent,
                        shape = CircleShape,
                    )
                    .clickable { onSelectPage(index) },
            )
        }
    }
}

// ── Optional Full Device & AI Setup Modal (Preserves Account / Permissions / Key / Guard) ──

@Composable
private fun DeviceSetupLiquidModal(
    name: String,
    onNameChanged: (String) -> Unit,
    signedInEmail: String?,
    signedIn: Boolean,
    signingIn: Boolean,
    signInError: String?,
    onSignIn: () -> Unit,
    refreshTrigger: Int,
    onRequestRuntime: (List<String>) -> Unit,
    onOpenSystem: (com.aura.aura_ui.presentation.permissions.AuraPermission) -> Unit,
    onVerifiedChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Account", "Permissions", "Gemini Key", "App Guard")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(28.dp),
            cornerRadiusDp = 28.dp,
            strongBlur = true,
            tintAlpha = 0.76f,
            prismIntensity = 1.3f,
            accentGlow = LiquidPalette.PrismBlue,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Device & AI Setup",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    LiquidGlassSurface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        cornerRadiusDp = 18.dp,
                        strongBlur = false,
                        tintAlpha = 0.5f,
                        onClick = onDismiss,
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tabs.forEachIndexed { idx, label ->
                        val selected = idx == activeTab
                        LiquidGlassSurface(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(14.dp),
                            cornerRadiusDp = 14.dp,
                            strongBlur = false,
                            tintAlpha = if (selected) 0.68f else 0.34f,
                            prismIntensity = if (selected) 1.35f else 0.55f,
                            accentGlow = if (selected) LiquidPalette.PrismBlue else null,
                            onClick = { activeTab = idx },
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else Color(0xFFB5C4E4),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        0 -> AccountSetupSection(
                            name = name,
                            onNameChanged = onNameChanged,
                            signedInEmail = signedInEmail,
                            signedIn = signedIn,
                            signingIn = signingIn,
                            error = signInError,
                            onSignIn = onSignIn,
                        )
                        1 -> PermissionsSetupSection(
                            refreshTrigger = refreshTrigger,
                            onRequestRuntime = onRequestRuntime,
                            onOpenSystem = onOpenSystem,
                        )
                        2 -> GeminiKeySetupSection(onVerifiedChanged = onVerifiedChanged)
                        else -> RestrictedAppsSetupSection()
                    }
                }

                Spacer(Modifier.height(10.dp))

                LiquidGlassButton(
                    onClick = onDismiss,
                    accentGlow = LiquidPalette.SilkCyan,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AccountSetupSection(
    name: String,
    onNameChanged: (String) -> Unit,
    signedInEmail: String?,
    signedIn: Boolean,
    signingIn: Boolean,
    error: String?,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            cornerRadiusDp = 18.dp,
            strongBlur = false,
            tintAlpha = 0.48f,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Person, null, tint = LiquidPalette.PrismBlue, modifier = Modifier.size(20.dp))
                    Text("What should THAKUR OS call you?", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    placeholder = { Text("Your name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = onboardingTextFieldColors(LiquidPalette.PrismBlue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        LiquidGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            cornerRadiusDp = 18.dp,
            strongBlur = false,
            tintAlpha = 0.48f,
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Google Account", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = "Secures your THAKUR OS profile. Does not request Gmail, Drive, or Calendar scopes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB5C4E4),
                )
                if (signedIn) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.MarkEmailRead, null, tint = LiquidPalette.SilkCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = signedInEmail ?: "Google account connected",
                            color = LiquidPalette.SilkCyan,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    LiquidGlassButton(
                        onClick = onSignIn,
                        enabled = !signingIn,
                        accentGlow = LiquidPalette.PrismBlue,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_google_g),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(if (signingIn) "Signing in..." else "Continue with Google")
                    }
                }
                if (error != null) {
                    Text(error, style = MaterialTheme.typography.bodySmall, color = LiquidPalette.DangerRed)
                }
            }
        }
    }
}

@Composable
private fun PermissionsSetupSection(
    refreshTrigger: Int,
    onRequestRuntime: (List<String>) -> Unit,
    onOpenSystem: (com.aura.aura_ui.presentation.permissions.AuraPermission) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                cornerRadiusDp = 16.dp,
                strongBlur = false,
                tintAlpha = 0.45f,
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Android 13+ Restricted Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "If Accessibility toggle is greyed out, open App Info -> ⋮ -> Allow restricted settings.",
                        color = Color(0xFFB5C4E4),
                        fontSize = 12.sp,
                    )
                    LiquidGlassButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}"),
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(15.dp))
                        Text("Open App Info", fontSize = 13.sp)
                    }
                }
            }
        }

        PermissionRegistry.all.forEach { perm ->
            val granted = remember(refreshTrigger) { perm.isGranted(context) }
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                cornerRadiusDp = 16.dp,
                strongBlur = false,
                tintAlpha = if (granted) 0.52f else 0.42f,
                accentGlow = if (granted) LiquidPalette.SilkCyan else null,
                onClick = {
                    if (perm.runtimePermissions.isNotEmpty() && !granted) {
                        onRequestRuntime(perm.runtimePermissions)
                    } else {
                        onOpenSystem(perm)
                    }
                },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = permissionIcon(perm.id),
                            contentDescription = null,
                            tint = if (granted) LiquidPalette.SilkCyan else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = perm.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                        )
                    }
                    LiquidGlassToggle(
                        checked = granted,
                        onCheckedChange = {
                            if (perm.runtimePermissions.isNotEmpty() && !granted) {
                                onRequestRuntime(perm.runtimePermissions)
                            } else {
                                onOpenSystem(perm)
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeminiKeySetupSection(onVerifiedChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val keyStore = remember { ProviderKeyStore(context.applicationContext) }
    val gemini = remember { LlmEndpointCatalog.builtinById(LlmEndpointCatalog.GEMINI_ID)!! }

    var keyText by remember { mutableStateOf(keyStore.getKey(LlmEndpointCatalog.GEMINI_ID).orEmpty()) }
    var revealed by remember { mutableStateOf(false) }
    var brainModel by remember { mutableStateOf(LlmEndpointCatalog.GEMINI_DEFAULT_MODEL_ID) }
    var liveModel by remember { mutableStateOf(CompanionConfig.LIVE_MODEL) }
    var brainModels by remember { mutableStateOf(listOf(LlmEndpointCatalog.GEMINI_DEFAULT_MODEL_ID)) }
    var liveModels by remember { mutableStateOf(listOf(CompanionConfig.LIVE_MODEL)) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var verified by remember { mutableStateOf(keyStore.isConfigured(LlmEndpointCatalog.GEMINI_ID)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LiquidGlassButton(
            onClick = { uriHandler.openUri(gemini.keyUrl ?: "https://aistudio.google.com/apikey") },
            accentGlow = LiquidPalette.PrismBlue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Key, null, modifier = Modifier.size(16.dp))
            Text("Get free key at aistudio.google.com", fontSize = 13.sp)
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(14.dp))
        }

        OutlinedTextField(
            value = keyText,
            onValueChange = { keyText = it; verified = false; onVerifiedChanged(false) },
            label = { Text("Gemini API key") },
            placeholder = { Text(gemini.keyHint ?: "AIza...") },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            colors = onboardingTextFieldColors(LiquidPalette.PrismBlue),
            visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                LiquidGlassSurface(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    cornerRadiusDp = 16.dp,
                    strongBlur = false,
                    tintAlpha = 0.45f,
                    onClick = { revealed = !revealed },
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        OnboardingModelDropdown(
            label = "Agent Brain Model",
            selected = brainModel,
            options = brainModels,
            stripPrefix = false,
            onSelect = { brainModel = it },
        )

        OnboardingModelDropdown(
            label = "Gemini Live Voice Model",
            selected = liveModel,
            options = liveModels,
            stripPrefix = true,
            onSelect = { liveModel = it },
        )

        LiquidGlassButton(
            enabled = !verifying && keyText.isNotBlank(),
            accentGlow = LiquidPalette.SilkCyan,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    verifying = true
                    error = null
                    val trimmed = keyText.trim()
                    val brainResult = ModelCatalog.fetch(gemini, trimmed)
                    val liveResult = LiveModelResolver.listLiveModels(trimmed)
                    val brainOk = brainResult.isSuccess && brainResult.getOrNull()?.isNotEmpty() == true
                    if (!brainOk) {
                        error = brainResult.exceptionOrNull()?.message ?: "Key rejected — check it and try again."
                        verified = false
                        onVerifiedChanged(false)
                    } else {
                        brainModels = brainResult.getOrNull().orEmpty().map { it.id }
                            .let { if (it.contains(brainModel)) it else listOf(brainModel) + it }
                        liveModels = liveResult.ifEmpty { listOf(liveModel) }
                            .let { if (it.contains(liveModel)) it else listOf(liveModel) + it }

                        keyStore.setKey(LlmEndpointCatalog.GEMINI_ID, trimmed)
                        keyStore.setSelectedEndpointId(LlmEndpointCatalog.GEMINI_ID)
                        keyStore.setSelectedModel(LlmEndpointCatalog.GEMINI_ID, brainModel)
                        val brainInfo = brainResult.getOrNull()?.firstOrNull { it.id == brainModel }
                        keyStore.setSelectedModelContextWindow(
                            LlmEndpointCatalog.GEMINI_ID,
                            brainInfo?.contextWindow,
                        )
                        keyStore.setSelectedModelVisionCapable(
                            LlmEndpointCatalog.GEMINI_ID,
                            brainInfo?.visionCapable,
                        )
                        val companion = CompanionConfig(context)
                        companion.liveModel = liveModel
                        companion.enabled = true
                        companion.useGeminiLive = true

                        verified = true
                        onVerifiedChanged(true)
                    }
                    verifying = false
                }
            },
        ) {
            if (verifying) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                Text("Verifying Key...")
            } else if (verified) {
                Icon(Icons.Filled.CheckCircle, null, tint = LiquidPalette.SilkCyan, modifier = Modifier.size(18.dp))
                Text("Key Verified")
            } else {
                Text("Verify API Key")
            }
        }

        error?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = LiquidPalette.DangerRed)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingModelDropdown(
    label: String,
    selected: String,
    options: List<String>,
    stripPrefix: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    fun display(id: String) = if (stripPrefix) id.removePrefix("models/") else id

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            shape = RoundedCornerShape(12.dp),
            colors = onboardingTextFieldColors(LiquidPalette.PrismBlue),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { id ->
                DropdownMenuItem(
                    text = {
                        Text(
                            display(id),
                            fontWeight = if (id == selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = { onSelect(id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun RestrictedAppsSetupSection() {
    val context = LocalContext.current
    val store = remember { AppRestrictedApps.get(context) }
    val entries by store.entries.collectAsState()

    var scanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var hasScanned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (entries.isEmpty() && !hasScanned) {
            scanning = true
            scanError = null
            try {
                val apps = AppInventoryScanner(context).scanInstalledApps().filter { it.isLaunchable() }
                val suggestions = RestrictedAppClassifier(context).classify(apps)
                store.mergeSuggestions(suggestions)
                hasScanned = true
            } catch (t: Throwable) {
                scanError = t.message ?: "Scan failed"
            } finally {
                scanning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Shield, null, tint = LiquidPalette.SilkCyan, modifier = Modifier.size(18.dp))
            Text("Banking & Sensitive Guard", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Automatically protects financial and authenticator apps from automation.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB5C4E4),
        )

        if (scanning) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = LiquidPalette.PrismBlue)
                Text("Scanning installed apps...", color = Color(0xFFB5C4E4))
            }
        } else if (scanError != null) {
            Text("Scan failed: $scanError", color = LiquidPalette.DangerRed, style = MaterialTheme.typography.bodySmall)
        } else if (entries.isEmpty()) {
            Text("No sensitive apps detected.", color = Color(0xFFB5C4E4), style = MaterialTheme.typography.bodySmall)
        } else {
            entries.sortedBy { it.appName }.forEach { entry: RestrictedAppEntry ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    cornerRadiusDp = 16.dp,
                    strongBlur = false,
                    tintAlpha = 0.45f,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(entry.appName, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiquidGlassButton(
                                onClick = { store.updateTier(entry.packageName, RestrictedTier.DECLINE_ONLY) },
                                accentGlow = if (entry.tier == RestrictedTier.DECLINE_ONLY) LiquidPalette.SilkCyan else null,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                            ) {
                                Text("Decline only", fontSize = 11.sp)
                            }
                            LiquidGlassButton(
                                onClick = { store.updateTier(entry.packageName, RestrictedTier.DISABLE_ACCESSIBILITY) },
                                accentGlow = if (entry.tier == RestrictedTier.DISABLE_ACCESSIBILITY) LiquidPalette.SilkViolet else null,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                            ) {
                                Text("Disable A11y", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun onboardingTextFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = accent,
    focusedBorderColor = accent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
    focusedLabelColor = accent,
    unfocusedLabelColor = Color(0xFFB0C0E0),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.42f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.42f),
)
