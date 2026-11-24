package com.aura.aura_ui.presentation.screens

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aura.aura_ui.R
import com.aura.aura_ui.data.preferences.ThakurSecurityStore
import com.aura.aura_ui.data.preferences.UnlockMethod
import com.aura.aura_ui.presentation.components.LiquidGlassButton
import com.aura.aura_ui.presentation.components.LiquidGlassSurface
import com.aura.aura_ui.presentation.components.LiquidPalette
import com.aura.aura_ui.presentation.utils.HapticUtils
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Full-screen cosmic Liquid Glass security gate for THAKUR OS.
 *
 * Contract:
 * - Visuals match the THAKUR OS Secured Access layout: full-screen cosmic backdrop,
 *   twinkling stars, slowly rotating Earth behind orbiting energy rings, pulsing
 *   center shield + padlock, neon HUD header, "SECURED ACCESS" banner, 5 Liquid Glass
 *   unlock cards (Fingerprint, Face Unlock, Pattern, PIN, Password), "Unlock Now"
 *   chamfered CTA, and "Your Security, Our Priority" footer.
 * - Relax mode uses Purple + Neon Blue accents; Work mode shifts accents to Orange + Amber.
 * - Fingerprint / Face Unlock invoke Android [BiometricPrompt]; Pattern, PIN, and Password
 *   verify against AES-256 encrypted secrets in [ThakurSecurityStore].
 * - Wrong attempts trigger `"Wrong, try again"` feedback with horizontal spring shake.
 */
@Composable
fun SecuredAccessScreen(
    onUnlocked: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    remember(context) { ThakurSecurityStore.hydrate(context) }

    val isWorkTheme by ThakurSecurityStore.isWorkTheme.collectAsState()
    val fpEnabled by ThakurSecurityStore.methodFingerprint.collectAsState()
    val faceEnabled by ThakurSecurityStore.methodFace.collectAsState()
    val patternEnabled by ThakurSecurityStore.methodPattern.collectAsState()
    val pinEnabled by ThakurSecurityStore.methodPin.collectAsState()
    val passwordEnabled by ThakurSecurityStore.methodPassword.collectAsState()

    val primaryGlow by animateColorAsState(
        targetValue = if (isWorkTheme) LiquidPalette.WorkOrange else LiquidPalette.SilkViolet,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "securedPrimaryGlow",
    )
    val secondaryGlow by animateColorAsState(
        targetValue = if (isWorkTheme) LiquidPalette.WorkAmber else LiquidPalette.PrismBlue,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "securedSecondaryGlow",
    )

    var activeModalMethod by remember { mutableStateOf<UnlockMethod?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var inlineStatus by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }

    val shakeX = remember { Animatable(0f) }
    fun triggerWrongAttempt(message: String = "Wrong, try again") {
        isErrorStatus = true
        inlineStatus = message
        HapticUtils.performErrorHaptic(context)
        scope.launch {
            shakeX.snapTo(0f)
            val offsets = listOf(-18f, 18f, -14f, 14f, -8f, 8f, -4f, 0f)
            for (x in offsets) {
                shakeX.animateTo(x, tween(40))
            }
        }
    }

    fun launchBiometric(method: UnlockMethod) {
        val activity = context.findFragmentActivity()
        if (activity == null) {
            triggerWrongAttempt("Biometric prompt requires an active window")
            return
        }
        val bm = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        when (bm.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                inlineStatus = null
                isErrorStatus = false
                val executor = ContextCompat.getMainExecutor(activity)
                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            HapticUtils.performSuccessHaptic(context)
                            onUnlocked()
                        }

                        override fun onAuthenticationFailed() {
                            triggerWrongAttempt("Wrong, try again")
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                errorCode != BiometricPrompt.ERROR_CANCELED
                            ) {
                                triggerWrongAttempt(errString.toString())
                            }
                        }
                    },
                )
                val title = if (method == UnlockMethod.FACE) {
                    "Face Unlock · THAKUR OS"
                } else {
                    "Fingerprint · THAKUR OS"
                }
                val subtitle = if (method == UnlockMethod.FACE) {
                    "Look at the screen to verify your identity"
                } else {
                    "Touch the fingerprint sensor to continue"
                }
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(authenticators)
                    .setNegativeButtonText("Use PIN / Password")
                    .build()
                prompt.authenticate(info)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                triggerWrongAttempt("No biometric enrolled on phone — try PIN, Pattern, or Password")
            }
            else -> {
                triggerWrongAttempt("${method.label} sensor unavailable — use PIN, Pattern, or Password")
            }
        }
    }

    fun handleCardClick(method: UnlockMethod) {
        val methodOn = when (method) {
            UnlockMethod.FINGERPRINT -> fpEnabled
            UnlockMethod.FACE -> faceEnabled
            UnlockMethod.PATTERN -> patternEnabled
            UnlockMethod.PIN -> pinEnabled
            UnlockMethod.PASSWORD -> passwordEnabled
        }
        if (!methodOn) {
            Toast.makeText(
                context,
                "${method.label} is turned off in Settings → THAKUR OS Security",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        inlineStatus = null
        isErrorStatus = false
        when (method) {
            UnlockMethod.FINGERPRINT, UnlockMethod.FACE -> launchBiometric(method)
            UnlockMethod.PATTERN -> {
                if (ThakurSecurityStore.getPatternCode(context).isNullOrBlank()) {
                    triggerWrongAttempt("Pattern not set yet — configure it in Settings or unlock with another method")
                } else {
                    activeModalMethod = UnlockMethod.PATTERN
                }
            }
            UnlockMethod.PIN -> {
                if (ThakurSecurityStore.getPinCode(context).isNullOrBlank() &&
                    ThakurSecurityStore.getSecretCode(context).isNullOrBlank()
                ) {
                    triggerWrongAttempt("PIN not set yet — configure PIN or Secret Code in Settings")
                } else {
                    activeModalMethod = UnlockMethod.PIN
                }
            }
            UnlockMethod.PASSWORD -> {
                if (ThakurSecurityStore.getSecretCode(context).isNullOrBlank()) {
                    triggerWrongAttempt("Password not set yet — configure Secret Code in Settings")
                } else {
                    activeModalMethod = UnlockMethod.PASSWORD
                }
            }
        }
    }

    fun handleUnlockNow() {
        when {
            fpEnabled -> launchBiometric(UnlockMethod.FINGERPRINT)
            faceEnabled -> launchBiometric(UnlockMethod.FACE)
            pinEnabled && (!ThakurSecurityStore.getPinCode(context).isNullOrBlank() ||
                !ThakurSecurityStore.getSecretCode(context).isNullOrBlank()) ->
                activeModalMethod = UnlockMethod.PIN
            patternEnabled && !ThakurSecurityStore.getPatternCode(context).isNullOrBlank() ->
                activeModalMethod = UnlockMethod.PATTERN
            passwordEnabled && !ThakurSecurityStore.getSecretCode(context).isNullOrBlank() ->
                activeModalMethod = UnlockMethod.PASSWORD
            else -> {
                // If user turned on Secured Access without configuring any lock secret/method yet,
                // keep usability safe rather than permanently locking them out.
                Toast.makeText(
                    context,
                    "No lock secret or biometric configured — unlocking THAKUR OS",
                    Toast.LENGTH_SHORT,
                ).show()
                onUnlocked()
            }
        }
    }

    // Continuous cosmic animations
    val infiniteTransition = rememberInfiniteTransition(label = "securedCosmos")
    val earthRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "earthRotate",
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ringRotate",
    )
    val shieldPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shieldPulse",
    )
    val shieldGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shieldGlow",
    )
    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "starTwinkle",
    )
    val unlockPulse by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "unlockPulse",
    )

    val chamferShape = remember { chamferedRectShape(14.dp) }
    val hexShape = remember { hexagonShape() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidPalette.DeepNight),
    ) {
        // 1. Full-screen cosmic background (Earth horizon, planets, Saturn, cliff portal)
        Image(
            painter = painterResource(R.drawable.thakur_secured_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Subtle Work/Relax atmospheric wash so purple vs orange theme shines clearly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.22f),
                            secondaryGlow.copy(alpha = 0.08f),
                            Color(0xFF04060F).copy(alpha = 0.34f),
                        ),
                    ),
                ),
        )

        // 2. Twinkling starfield
        TwinklingStarsOverlay(
            phase = twinklePhase,
            tintSecondary = secondaryGlow,
            tintPrimary = primaryGlow,
            modifier = Modifier.fillMaxSize(),
        )

        // 3. Foreground HUD layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .offset { IntOffset(shakeX.value.roundToInt(), 0) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Top Bar: Back Hex · Cosmic Orb + THAKUR OS HUD · Info Hex ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                HexHudIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    shape = hexShape,
                    borderColor = secondaryGlow,
                    glowColor = primaryGlow,
                    onClick = onBack,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                ) {
                    // Glowing cosmic orb crowning the HUD frame
                    Canvas(modifier = Modifier.size(30.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    secondaryGlow.copy(alpha = 0.9f),
                                    primaryGlow.copy(alpha = 0.65f),
                                    Color.Transparent,
                                ),
                            ),
                            radius = size.minDimension * 0.56f,
                        )
                        drawCircle(
                            color = secondaryGlow.copy(alpha = 0.85f),
                            radius = size.minDimension * 0.36f,
                            style = Stroke(width = 1.4.dp.toPx()),
                        )
                    }

                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-6).dp),
                        shape = chamferShape,
                        cornerRadiusDp = 12.dp,
                        strongBlur = true,
                        tintAlpha = 0.62f,
                        prismIntensity = 1.35f,
                        accentGlow = primaryGlow,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "THAKUR OS",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.4.sp,
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.White,
                                            secondaryGlow,
                                            Color.White,
                                        ),
                                    ),
                                ),
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                text = "AI OPERATING SYSTEM",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.6.sp,
                                color = secondaryGlow.copy(alpha = 0.92f),
                                maxLines = 1,
                            )
                        }
                    }
                }

                HexHudIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = "Security Info",
                    shape = hexShape,
                    borderColor = secondaryGlow,
                    glowColor = primaryGlow,
                    onClick = { showInfoDialog = true },
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Center Earth + Orbiting Rings + Pulsing Shield/Padlock ──────
            Box(
                modifier = Modifier
                    .size(196.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Outer nebula aura
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondaryGlow.copy(alpha = 0.42f * shieldGlowAlpha),
                                primaryGlow.copy(alpha = 0.28f * shieldGlowAlpha),
                                Color.Transparent,
                            ),
                        ),
                        radius = size.minDimension * 0.50f,
                    )
                }

                // Rotating Earth sphere
                Image(
                    painter = painterResource(R.drawable.thakur_earth_globe),
                    contentDescription = "THAKUR OS Rotating Earth",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .rotate(earthRotation),
                )

                // Orbiting energy ellipses around Earth
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    rotate(degrees = ringRotation * 0.25f - 22f, pivot = Offset(cx, cy)) {
                        drawOval(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    secondaryGlow.copy(alpha = 0.90f),
                                    primaryGlow.copy(alpha = 0.85f),
                                    Color.Transparent,
                                ),
                            ),
                            topLeft = Offset(cx - size.width * 0.48f, cy - size.height * 0.16f),
                            size = Size(size.width * 0.96f, size.height * 0.32f),
                            style = Stroke(width = 2.2.dp.toPx()),
                        )
                    }
                    rotate(degrees = -ringRotation * 0.18f + 24f, pivot = Offset(cx, cy)) {
                        drawOval(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    primaryGlow.copy(alpha = 0.88f),
                                    Color.White.copy(alpha = 0.65f),
                                    Color.Transparent,
                                ),
                            ),
                            topLeft = Offset(cx - size.width * 0.46f, cy - size.height * 0.14f),
                            size = Size(size.width * 0.92f, size.height * 0.28f),
                            style = Stroke(width = 1.8.dp.toPx()),
                        )
                    }
                }

                // Pulsing Neon Shield + Padlock in center
                GlowingShieldLock(
                    scale = shieldPulse,
                    glowAlpha = shieldGlowAlpha,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.size(94.dp),
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Center Banner: SECURED ACCESS ───────────────────────────────
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = chamferShape,
                cornerRadiusDp = 14.dp,
                strongBlur = true,
                tintAlpha = 0.64f,
                prismIntensity = 1.45f,
                accentGlow = primaryGlow,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "SECURED ACCESS",
                        style = TextStyle(
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.2.sp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    secondaryGlow,
                                    Color.White,
                                    primaryGlow,
                                ),
                            ),
                        ),
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "— Verify Your Identity to Continue —",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }

            // Inline error/status feedback (shakes horizontally on failure)
            AnimatedVisibility(visible = inlineStatus != null) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    cornerRadiusDp = 14.dp,
                    strongBlur = false,
                    tintAlpha = 0.72f,
                    accentGlow = if (isErrorStatus) LiquidPalette.DangerRed else secondaryGlow,
                ) {
                    Text(
                        text = inlineStatus.orEmpty(),
                        color = if (isErrorStatus) Color(0xFFFFB3C1) else Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 5 Liquid Glass Unlock Cards ─────────────────────────────────
            // Row 1: Fingerprint · Face Unlock · Pattern
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UnlockMethodCard(
                    method = UnlockMethod.FINGERPRINT,
                    subtitle = "Tap to unlock",
                    enabledInSettings = fpEnabled,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { handleCardClick(UnlockMethod.FINGERPRINT) },
                )
                UnlockMethodCard(
                    method = UnlockMethod.FACE,
                    subtitle = "Look at the screen",
                    enabledInSettings = faceEnabled,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { handleCardClick(UnlockMethod.FACE) },
                )
                UnlockMethodCard(
                    method = UnlockMethod.PATTERN,
                    subtitle = "Draw pattern",
                    enabledInSettings = patternEnabled,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { handleCardClick(UnlockMethod.PATTERN) },
                )
            }

            Spacer(Modifier.height(10.dp))

            // Row 2: PIN · Password (centered 2 cards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UnlockMethodCard(
                    method = UnlockMethod.PIN,
                    subtitle = "Enter PIN",
                    enabledInSettings = pinEnabled,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { handleCardClick(UnlockMethod.PIN) },
                )
                UnlockMethodCard(
                    method = UnlockMethod.PASSWORD,
                    subtitle = "Enter Password",
                    enabledInSettings = passwordEnabled,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { handleCardClick(UnlockMethod.PASSWORD) },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Big Liquid Glass Unlock Now Button with Pulse ───────────────
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = unlockPulse
                        scaleY = unlockPulse
                    }
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    secondaryGlow.copy(alpha = 0.45f),
                                    primaryGlow.copy(alpha = 0.55f),
                                ),
                            ),
                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx()),
                        )
                    },
                shape = chamferShape,
                cornerRadiusDp = 16.dp,
                strongBlur = true,
                tintAlpha = 0.68f,
                prismIntensity = 1.55f,
                accentGlow = primaryGlow,
                onClick = { handleUnlockNow() },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    secondaryGlow.copy(alpha = 0.24f),
                                    primaryGlow.copy(alpha = 0.34f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "»",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = secondaryGlow,
                        )
                        Text(
                            text = "Unlock Now",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFFFFF3C4),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Footer: Your Security, Our Priority ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(1.dp)
                        .background(secondaryGlow.copy(alpha = 0.55f)),
                )
                Text(
                    text = "Your Security,",
                    fontSize = 11.5.sp,
                    color = secondaryGlow.copy(alpha = 0.90f),
                    fontWeight = FontWeight.Medium,
                )
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = secondaryGlow,
                        modifier = Modifier.size(16.dp),
                    )
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = LiquidPalette.DeepNight,
                        modifier = Modifier.size(9.dp),
                    )
                }
                Text(
                    text = "Our Priority",
                    fontSize = 11.5.sp,
                    color = secondaryGlow.copy(alpha = 0.90f),
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(1.dp)
                        .background(secondaryGlow.copy(alpha = 0.55f)),
                )
            }
        }

        // ── Interactive Unlock Modals (PIN / Password / Pattern) ────────────
        AnimatedVisibility(
            visible = activeModalMethod != null,
            enter = fadeIn(tween(240)) + scaleIn(tween(260), initialScale = 0.92f),
            exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.94f),
        ) {
            when (activeModalMethod) {
                UnlockMethod.PIN -> PinVerifyOverlay(
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    onDismiss = { activeModalMethod = null },
                    onVerify = { pin ->
                        if (ThakurSecurityStore.verifyPin(context, pin)) {
                            HapticUtils.performSuccessHaptic(context)
                            activeModalMethod = null
                            onUnlocked()
                            true
                        } else {
                            HapticUtils.performErrorHaptic(context)
                            false
                        }
                    },
                )
                UnlockMethod.PASSWORD -> PasswordVerifyOverlay(
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    onDismiss = { activeModalMethod = null },
                    onVerify = { pwd ->
                        if (ThakurSecurityStore.verifyPassword(context, pwd)) {
                            HapticUtils.performSuccessHaptic(context)
                            activeModalMethod = null
                            onUnlocked()
                            true
                        } else {
                            HapticUtils.performErrorHaptic(context)
                            false
                        }
                    },
                )
                UnlockMethod.PATTERN -> PatternVerifyOverlay(
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    onDismiss = { activeModalMethod = null },
                    onVerify = { seq ->
                        if (ThakurSecurityStore.verifyPattern(context, seq)) {
                            HapticUtils.performSuccessHaptic(context)
                            activeModalMethod = null
                            onUnlocked()
                            true
                        } else {
                            HapticUtils.performErrorHaptic(context)
                            false
                        }
                    },
                )
                else -> Unit
            }
        }

        // ── Info Dialog (Top-right 'i' icon) ────────────────────────────────
        AnimatedVisibility(
            visible = showInfoDialog,
            enter = fadeIn(tween(220)) + scaleIn(tween(240), initialScale = 0.92f),
            exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.94f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.68f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showInfoDialog = false },
                contentAlignment = Alignment.Center,
            ) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {},
                    shape = RoundedCornerShape(24.dp),
                    cornerRadiusDp = 24.dp,
                    strongBlur = true,
                    tintAlpha = 0.78f,
                    prismIntensity = 1.4f,
                    accentGlow = primaryGlow,
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = secondaryGlow,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = "THAKUR OS Security",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                        Text(
                            text = "Identity verification protects THAKUR OS voice commands, automation, " +
                                "and device controls. Biometrics stay inside Android Keystore and PIN, " +
                                "Pattern, and Password secrets are AES-256 encrypted on device.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color.White.copy(alpha = 0.80f),
                        )
                        Text(
                            text = "Theme Mode: " + if (isWorkTheme) "Work (Orange Glow)" else "Relax (Purple Glow)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryGlow,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            LiquidGlassButton(
                                onClick = { showInfoDialog = false },
                                accentGlow = primaryGlow,
                            ) {
                                Text("Got it")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HexHudIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    shape: GenericShape,
    borderColor: Color,
    glowColor: Color,
    onClick: () -> Unit,
) {
    LiquidGlassSurface(
        modifier = Modifier
            .size(44.dp)
            .drawBehind {
                drawCircle(
                    color = borderColor.copy(alpha = 0.35f),
                    radius = size.minDimension * 0.52f,
                    style = Stroke(width = 1.6.dp.toPx()),
                )
            },
        shape = shape,
        cornerRadiusDp = 12.dp,
        strongBlur = true,
        tintAlpha = 0.62f,
        prismIntensity = 1.3f,
        accentGlow = glowColor,
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun UnlockMethodCard(
    method: UnlockMethod,
    subtitle: String,
    enabledInSettings: Boolean,
    primaryGlow: Color,
    secondaryGlow: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cardShape = remember { chamferedRectShape(12.dp) }
    var tapCount by remember { mutableIntStateOf(0) }
    LiquidGlassSurface(
        modifier = modifier
            .height(116.dp)
            .alpha(if (enabledInSettings) 1f else 0.44f),
        shape = cardShape,
        cornerRadiusDp = 16.dp,
        strongBlur = true,
        tintAlpha = if (enabledInSettings) 0.58f else 0.40f,
        prismIntensity = if (enabledInSettings) 1.45f else 0.55f,
        accentGlow = if (enabledInSettings) secondaryGlow else primaryGlow.copy(alpha = 0.45f),
        flashSignal = tapCount,
        onClick = {
            tapCount++
            onClick()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (method) {
                    UnlockMethod.FINGERPRINT -> FingerprintNeonGraphic(secondaryGlow)
                    UnlockMethod.FACE -> FaceScanNeonGraphic(secondaryGlow, primaryGlow)
                    UnlockMethod.PATTERN -> PatternGridNeonGraphic(secondaryGlow)
                    UnlockMethod.PIN -> PinKeypadNeonGraphic(secondaryGlow, primaryGlow)
                    UnlockMethod.PASSWORD -> PasswordBoxNeonGraphic(secondaryGlow, primaryGlow)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = method.label,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (enabledInSettings) subtitle else "Disabled",
                    fontSize = 9.5.sp,
                    color = if (enabledInSettings) secondaryGlow.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FingerprintNeonGraphic(glow: Color) {
    Canvas(modifier = Modifier.size(44.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        listOf(0.88f, 0.70f, 0.52f, 0.34f).forEachIndexed { idx, factor ->
            val r = (size.minDimension / 2f) * factor
            drawArc(
                color = glow,
                startAngle = -215f + idx * 14f,
                sweepAngle = 250f - idx * 24f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r * 1.08f),
                size = Size(r * 2f, r * 2.16f),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(cx, cy - size.height * 0.08f),
            end = Offset(cx, cy + size.height * 0.22f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun FaceScanNeonGraphic(cyan: Color, accent: Color) {
    Canvas(modifier = Modifier.size(44.dp)) {
        val w = size.width
        val h = size.height
        val b = w * 0.24f
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        // Corner scanner brackets
        listOf(
            listOf(Offset(2f, b), Offset(2f, 2f), Offset(b, 2f)),
            listOf(Offset(w - b, 2f), Offset(w - 2f, 2f), Offset(w - 2f, b)),
            listOf(Offset(2f, h - b), Offset(2f, h - 2f), Offset(b, h - 2f)),
            listOf(Offset(w - b, h - 2f), Offset(w - 2f, h - 2f), Offset(w - 2f, h - b)),
        ).forEach { pts ->
            val p = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                lineTo(pts[1].x, pts[1].y)
                lineTo(pts[2].x, pts[2].y)
            }
            drawPath(p, color = cyan, style = stroke)
        }
        // Stylized face silhouette + scan line
        drawOval(
            color = accent.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.27f, h * 0.18f),
            size = Size(w * 0.46f, h * 0.60f),
            style = Stroke(width = 1.6.dp.toPx()),
        )
        drawCircle(color = cyan, radius = 2.2.dp.toPx(), center = Offset(w * 0.40f, h * 0.42f))
        drawCircle(color = cyan, radius = 2.2.dp.toPx(), center = Offset(w * 0.60f, h * 0.42f))
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(w * 0.16f, h * 0.52f),
            end = Offset(w * 0.84f, h * 0.52f),
            strokeWidth = 1.4.dp.toPx(),
        )
    }
}

@Composable
private fun PatternGridNeonGraphic(glow: Color) {
    Canvas(modifier = Modifier.size(42.dp)) {
        val step = size.width / 4f
        for (row in 1..3) {
            for (col in 1..3) {
                val center = Offset(col * step, row * step)
                drawCircle(
                    color = glow,
                    radius = 4.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx()),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = 1.4.dp.toPx(),
                    center = center,
                )
            }
        }
    }
}

@Composable
private fun PinKeypadNeonGraphic(cyan: Color, accent: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
        rows.forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                r.forEach { d ->
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accent.copy(alpha = 0.25f))
                            .drawBehind {
                                drawRoundRect(
                                    color = cyan.copy(alpha = 0.8f),
                                    cornerRadius = CornerRadius(3.dp.toPx()),
                                    style = Stroke(width = 0.9.dp.toPx()),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = d, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordBoxNeonGraphic(cyan: Color, accent: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = cyan,
            modifier = Modifier.size(22.dp),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.24f))
                .drawBehind {
                    drawRoundRect(
                        color = cyan.copy(alpha = 0.85f),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 1.1.dp.toPx()),
                    )
                }
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = "* * * * *",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun GlowingShieldLock(
    scale: Float,
    glowAlpha: Float,
    primaryGlow: Color,
    secondaryGlow: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val shieldPath = Path().apply {
                moveTo(w * 0.50f, h * 0.06f)
                lineTo(w * 0.88f, h * 0.20f)
                cubicTo(w * 0.88f, h * 0.56f, w * 0.74f, h * 0.80f, w * 0.50f, h * 0.94f)
                cubicTo(w * 0.26f, h * 0.80f, w * 0.12f, h * 0.56f, w * 0.12f, h * 0.20f)
                close()
            }
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    listOf(
                        secondaryGlow.copy(alpha = 0.40f * glowAlpha),
                        primaryGlow.copy(alpha = 0.45f * glowAlpha),
                    ),
                ),
            )
            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    listOf(secondaryGlow, Color.White, primaryGlow),
                ),
                style = Stroke(width = 3.2.dp.toPx(), join = StrokeJoin.Round),
            )
        }
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Secured Shield Lock",
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun TwinklingStarsOverlay(
    phase: Float,
    tintSecondary: Color,
    tintPrimary: Color,
    modifier: Modifier = Modifier,
) {
    val stars = remember {
        listOf(
            Triple(0.08f, 0.12f, 2.2f),
            Triple(0.22f, 0.09f, 1.6f),
            Triple(0.84f, 0.15f, 2.4f),
            Triple(0.92f, 0.28f, 1.8f),
            Triple(0.14f, 0.27f, 2.0f),
            Triple(0.75f, 0.22f, 1.5f),
            Triple(0.32f, 0.18f, 1.4f),
            Triple(0.64f, 0.11f, 2.1f),
            Triple(0.10f, 0.48f, 1.9f),
            Triple(0.88f, 0.54f, 2.3f),
            Triple(0.46f, 0.06f, 2.0f),
            Triple(0.55f, 0.31f, 1.5f),
        )
    }
    Canvas(modifier = modifier) {
        stars.forEachIndexed { idx, (xf, yf, rDp) ->
            val wave = 0.35f + 0.65f * ((kotlin.math.sin(phase + idx * 0.9f) + 1f) * 0.5f)
            val color = when (idx % 3) {
                0 -> Color.White.copy(alpha = wave)
                1 -> tintSecondary.copy(alpha = wave)
                else -> tintPrimary.copy(alpha = wave)
            }
            val pos = Offset(size.width * xf, size.height * yf)
            val r = rDp.dp.toPx()
            drawCircle(color = color, radius = r, center = pos)
            if (idx % 2 == 0) {
                drawLine(
                    color = color.copy(alpha = wave * 0.8f),
                    start = Offset(pos.x - r * 2.4f, pos.y),
                    end = Offset(pos.x + r * 2.4f, pos.y),
                    strokeWidth = 0.9.dp.toPx(),
                )
                drawLine(
                    color = color.copy(alpha = wave * 0.8f),
                    start = Offset(pos.x, pos.y - r * 2.4f),
                    end = Offset(pos.x, pos.y + r * 2.4f),
                    strokeWidth = 0.9.dp.toPx(),
                )
            }
        }
    }
}

@Composable
private fun PinVerifyOverlay(
    primaryGlow: Color,
    secondaryGlow: Color,
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean,
) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun submitPin(attempt: String) {
        if (onVerify(attempt)) return
        errorMsg = "Wrong, try again"
        pin = ""
        scope.launch {
            shake.snapTo(0f)
            for (x in listOf(-16f, 16f, -12f, 12f, -6f, 0f)) {
                shake.animateTo(x, tween(38))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .offset { IntOffset(shake.value.roundToInt(), 0) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(26.dp),
            cornerRadiusDp = 26.dp,
            strongBlur = true,
            tintAlpha = 0.82f,
            prismIntensity = 1.45f,
            accentGlow = primaryGlow,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Enter PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    LiquidGlassButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(8.dp),
                        accentGlow = primaryGlow,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                // Masked PIN dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    val slots = maxOf(4, pin.length)
                    repeat(slots.coerceAtMost(8)) { idx ->
                        val filled = idx < pin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (filled) secondaryGlow else Color.White.copy(alpha = 0.16f)),
                        )
                    }
                }

                errorMsg?.let {
                    Text(
                        text = it,
                        color = LiquidPalette.DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Numeric keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                )
                rows.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        r.forEach { d ->
                            LiquidGlassButton(
                                onClick = {
                                    errorMsg = null
                                    if (pin.length < 8) pin += d
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                accentGlow = secondaryGlow,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(d, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LiquidGlassButton(
                        onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        accentGlow = primaryGlow,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", modifier = Modifier.size(18.dp))
                    }
                    LiquidGlassButton(
                        onClick = {
                            errorMsg = null
                            if (pin.length < 8) pin += "0"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        accentGlow = secondaryGlow,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("0", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    LiquidGlassButton(
                        onClick = { submitPin(pin) },
                        enabled = pin.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        accentGlow = primaryGlow,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Verify", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordVerifyOverlay(
    primaryGlow: Color,
    secondaryGlow: Color,
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean,
) {
    var input by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .offset { IntOffset(shake.value.roundToInt(), 0) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(26.dp),
            cornerRadiusDp = 26.dp,
            strongBlur = true,
            tintAlpha = 0.82f,
            prismIntensity = 1.45f,
            accentGlow = primaryGlow,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Enter Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    LiquidGlassButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(8.dp),
                        accentGlow = primaryGlow,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; errorMsg = null },
                    label = { Text("Secret word or password") },
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Icon(
                            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (visible) "Hide" else "Show",
                            tint = secondaryGlow,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { visible = !visible },
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = secondaryGlow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                        focusedLabelColor = secondaryGlow,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.65f),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                errorMsg?.let {
                    Text(
                        text = it,
                        color = LiquidPalette.DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LiquidGlassButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        accentGlow = primaryGlow,
                    ) {
                        Text("Cancel")
                    }
                    LiquidGlassButton(
                        onClick = {
                            if (!onVerify(input)) {
                                errorMsg = "Wrong, try again"
                                scope.launch {
                                    shake.snapTo(0f)
                                    for (x in listOf(-16f, 16f, -12f, 12f, -6f, 0f)) {
                                        shake.animateTo(x, tween(38))
                                    }
                                }
                            }
                        },
                        enabled = input.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        accentGlow = secondaryGlow,
                    ) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternVerifyOverlay(
    primaryGlow: Color,
    secondaryGlow: Color,
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean,
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var dragPos by remember { mutableStateOf<Offset?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .offset { IntOffset(shake.value.roundToInt(), 0) }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(26.dp),
            cornerRadiusDp = 26.dp,
            strongBlur = true,
            tintAlpha = 0.82f,
            prismIntensity = 1.45f,
            accentGlow = primaryGlow,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Draw Pattern",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    LiquidGlassButton(
                        onClick = onDismiss,
                        contentPadding = PaddingValues(8.dp),
                        accentGlow = primaryGlow,
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = errorMsg ?: "Connect the dots in order",
                    color = if (errorMsg != null) LiquidPalette.DangerRed else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                InteractivePatternPad(
                    selectedNodes = selectedNodes,
                    dragPosition = dragPos,
                    primaryGlow = primaryGlow,
                    secondaryGlow = secondaryGlow,
                    onStart = {
                        errorMsg = null
                        selectedNodes.clear()
                    },
                    onUpdateDrag = { pos -> dragPos = pos },
                    onFinish = {
                        dragPos = null
                        if (selectedNodes.size >= 2) {
                            val seq = selectedNodes.joinToString("-")
                            if (!onVerify(seq)) {
                                errorMsg = "Wrong, try again"
                                selectedNodes.clear()
                                scope.launch {
                                    shake.snapTo(0f)
                                    for (x in listOf(-16f, 16f, -12f, 12f, -6f, 0f)) {
                                        shake.animateTo(x, tween(38))
                                    }
                                }
                            }
                        } else {
                            selectedNodes.clear()
                        }
                    },
                    modifier = Modifier.size(240.dp),
                )
            }
        }
    }
}

/**
 * Reusable interactive 3x3 pattern pad used by both [SecuredAccessScreen] verification
 * and Settings pattern configuration.
 */
@Composable
fun InteractivePatternPad(
    selectedNodes: MutableList<Int>,
    dragPosition: Offset?,
    primaryGlow: Color,
    secondaryGlow: Color,
    onStart: () -> Unit,
    onUpdateDrag: (Offset?) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                val cellW = size.width / 3f
                val cellH = size.height / 3f
                val hitRadius = minOf(cellW, cellH) * 0.36f
                fun nodeCenter(idx: Int): Offset {
                    val col = idx % 3
                    val row = idx / 3
                    return Offset(col * cellW + cellW / 2f, row * cellH + cellH / 2f)
                }
                fun hitTest(p: Offset) {
                    for (idx in 0..8) {
                        val c = nodeCenter(idx)
                        if (hypot((p.x - c.x).toDouble(), (p.y - c.y).toDouble()) <= hitRadius) {
                            if (!selectedNodes.contains(idx)) {
                                selectedNodes.add(idx)
                            }
                            break
                        }
                    }
                }
                detectDragGestures(
                    onDragStart = { start ->
                        onStart()
                        onUpdateDrag(start)
                        hitTest(start)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onUpdateDrag(change.position)
                        hitTest(change.position)
                    },
                    onDragEnd = { onFinish() },
                    onDragCancel = { onFinish() },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { }
            },
    ) {
        val cellW = size.width / 3f
        val cellH = size.height / 3f
        fun centerOf(idx: Int) = Offset(
            (idx % 3) * cellW + cellW / 2f,
            (idx / 3) * cellH + cellH / 2f,
        )

        // Draw connecting neon line
        if (selectedNodes.isNotEmpty()) {
            val path = Path().apply {
                val first = centerOf(selectedNodes.first())
                moveTo(first.x, first.y)
                for (i in 1 until selectedNodes.size) {
                    val next = centerOf(selectedNodes[i])
                    lineTo(next.x, next.y)
                }
                dragPosition?.let { lineTo(it.x, it.y) }
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(listOf(secondaryGlow, primaryGlow)),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        // Draw 9 nodes
        for (idx in 0..8) {
            val c = centerOf(idx)
            val active = selectedNodes.contains(idx)
            if (active) {
                drawCircle(
                    color = secondaryGlow.copy(alpha = 0.28f),
                    radius = 20.dp.toPx(),
                    center = c,
                )
            }
            drawCircle(
                color = if (active) secondaryGlow else Color.White.copy(alpha = 0.40f),
                radius = 12.dp.toPx(),
                center = c,
                style = Stroke(width = if (active) 2.5.dp.toPx() else 1.5.dp.toPx()),
            )
            drawCircle(
                color = if (active) Color.White else secondaryGlow.copy(alpha = 0.70f),
                radius = if (active) 5.dp.toPx() else 3.5.dp.toPx(),
                center = c,
            )
        }
    }
}

private fun chamferedRectShape(cut: Dp) = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val c = minOf(cut.value * 2.5f, minOf(w, h) * 0.35f)
    moveTo(c, 0f)
    lineTo(w - c, 0f)
    lineTo(w, c)
    lineTo(w, h - c)
    lineTo(w - c, h)
    lineTo(c, h)
    lineTo(0f, h - c)
    lineTo(0f, c)
    close()
}

private fun hexagonShape() = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.50f, 0f)
    lineTo(w * 0.94f, h * 0.25f)
    lineTo(w * 0.94f, h * 0.75f)
    lineTo(w * 0.50f, h)
    lineTo(w * 0.06f, h * 0.75f)
    lineTo(w * 0.06f, h * 0.25f)
    close()
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
