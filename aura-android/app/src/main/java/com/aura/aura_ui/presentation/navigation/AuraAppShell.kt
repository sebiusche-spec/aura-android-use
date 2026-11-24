package com.aura.aura_ui.presentation.navigation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aura.aura_ui.BuildConfig
import com.aura.aura_ui.presentation.components.rememberMonoScheme
import com.aura.aura_ui.presentation.screens.CompanionScreen
import com.aura.aura_ui.presentation.screens.McpCenterScreen
import com.aura.aura_ui.presentation.screens.McpLogRetentionScreen
import com.aura.aura_ui.presentation.screens.McpServersScreen
import com.aura.aura_ui.presentation.screens.McpSessionDetailScreen
import com.aura.aura_ui.presentation.screens.McpSessionLogScreen
import com.aura.aura_ui.presentation.screens.MemoryScreen
import com.aura.aura_ui.presentation.screens.ProviderSettingsScreen
import com.aura.aura_ui.presentation.screens.RestrictedAppsScreen
import com.aura.aura_ui.presentation.screens.SkillsScreen
import com.aura.aura_ui.presentation.screens.TavilyKeyScreen
import com.aura.aura_ui.presentation.screens.TrustedDevicesScreen
import com.aura.aura_ui.presentation.screens.VoiceSettingsScreen
import com.aura.aura_ui.presentation.screens.agent.AgentHubScreen
import com.aura.aura_ui.presentation.screens.agent.HooksScreen
import com.aura.aura_ui.presentation.screens.agent.ToolsScreen
import com.aura.aura_ui.presentation.screens.eval.EvalSuiteScreen
import com.aura.aura_ui.presentation.screens.home.AuraHomeScreen
import com.aura.aura_ui.presentation.screens.legal.LicensesScreen
import com.aura.aura_ui.presentation.screens.legal.PrivacyPolicyScreen
import com.aura.aura_ui.presentation.screens.legal.TermsScreen
import com.aura.aura_ui.presentation.screens.settings.MonoSettingsScreen
import com.aura.aura_ui.presentation.screens.settings.PermissionsHealthScreen
import com.aura.aura_ui.remote.GateState
import com.aura.aura_ui.remote.RemoteGateViewModel
import com.aura.aura_ui.ui.theme.Mono
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

// ============================================================================
// APP SHELL — the single-activity navigation host that replaces both the old
// auto-overlay entry and MainActivity's hand-rolled settings state machine.
//
// Four top-level tabs (Home · Agent · Activity · Settings); every pre-existing
// screen is REHOMED here, not rewritten. Spec §3 rule: agent-behavior screens
// live under Agent, app-behavior screens under Settings.
// ============================================================================

object AuraRoutes {
    const val HOME = "home"
    const val AGENT = "agent"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"

    const val AGENT_BRAIN = "agent/brain"
    const val AGENT_TOOLS = "agent/tools"
    const val AGENT_HOOKS = "agent/hooks"
    const val AGENT_SKILLS = "agent/skills"
    const val AGENT_MEMORY = "agent/memory"
    const val AGENT_MCP_SERVERS = "agent/mcp_servers"
    const val AGENT_COMPANION = "agent/companion"
    const val AGENT_VOICE = "agent/voice"

    const val SETTINGS_TAVILY = "settings/tavily"
    const val SETTINGS_MCP_CENTER = "settings/mcp_center"
    const val SETTINGS_TRUSTED_DEVICES = "settings/trusted_devices"
    const val SETTINGS_RESTRICTED_APPS = "settings/restricted_apps"
    const val SETTINGS_PERMISSIONS = "settings/permissions"
    const val SETTINGS_PRIVACY = "settings/privacy"
    const val SETTINGS_TERMS = "settings/terms"
    const val SETTINGS_LICENSES = "settings/licenses"

    /**
     * Debug-only eval cockpit. Registered behind `BuildConfig.DEBUG` below, so in a release build
     * this string names a destination the graph does not contain.
     */
    const val EVAL_SUITE = "eval"

    const val ACTIVITY_SESSION = "activity/session/{sessionId}"
    const val ACTIVITY_RETENTION = "activity/retention"

    fun sessionDetail(sessionId: String) = "activity/session/$sessionId"
}

    val scheme = rememberMonoScheme()
    val gateViewModel: RemoteGateViewModel = hiltViewModel()
    val gateState by gateViewModel.gateState.collectAsState()

    var drawerOpen by remember { mutableStateOf(false) }
    var kebabOpen by remember { mutableStateOf(false) }
    var gooeyOpen by remember { mutableStateOf(false) }
    var categoriesOpen by remember { mutableStateOf(false) }
    var createPanelOpen by remember { mutableStateOf(false) }
    var commandPaletteOpen by remember { mutableStateOf(false) }

    fun closeAllMenus() {
        drawerOpen = false
        kebabOpen = false
        gooeyOpen = false
        categoriesOpen = false
        createPanelOpen = false
        commandPaletteOpen = false
    }

    fun navigateTop(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isTabLevel = currentRoute == null || glassBottomTabs.any { it.route == currentRoute }

    val commandEntries = remember(navController, onStartChat, onOpenBrowser) {
        listOf(
            CommandPaletteEntry("voice", "Voice Assistant", "Talk to THAKUR OS", Icons.Filled.Mic, "RUN") {
                onStartChat(null)
            },
            CommandPaletteEntry("browser", "THAKUR OS Browser", "Open built-in WebView browser", Icons.Outlined.SmartToy, "WEB") {
                onOpenBrowser()
            },
            CommandPaletteEntry("home", "Home", "Main dashboard & quick prompts", Icons.Outlined.Home, "TAB") {
                navigateTop(AuraRoutes.HOME)
            },
            CommandPaletteEntry("agent", "Agent Hub", "Brain, Tools, Hooks, Skills", Icons.Outlined.SmartToy, "TAB") {
                navigateTop(AuraRoutes.AGENT)
            },
            CommandPaletteEntry("brain", "Model & Provider", "Configure AI brain & API keys", Icons.Outlined.Psychology, "AGENT") {
                navController.navigate(AuraRoutes.AGENT_BRAIN)
            },
            CommandPaletteEntry("tools", "Device Tools", "Inspect on-device capabilities", Icons.Outlined.Construction, "AGENT") {
                navController.navigate(AuraRoutes.AGENT_TOOLS)
            },
            CommandPaletteEntry("skills", "Skills", "Reusable task playbooks", Icons.Outlined.AutoAwesome, "AGENT") {
                navController.navigate(AuraRoutes.AGENT_SKILLS)
            },
            CommandPaletteEntry("memory", "Saved Memory", "Memories, commitments & learnings", Icons.Outlined.Memory, "SAVED") {
                navigateTop(AuraRoutes.AGENT_MEMORY)
            },
            CommandPaletteEntry("history", "Inbox & History", "Past conversations & session logs", Icons.Outlined.History, "INBOX") {
                navigateTop(AuraRoutes.ACTIVITY)
            },
            CommandPaletteEntry("mcp_center", "MCP Center", "Phone-as-MCP-server controls", Icons.Outlined.Hub, "MCP") {
                navController.navigate(AuraRoutes.SETTINGS_MCP_CENTER)
            },
            CommandPaletteEntry("mcp_servers", "External MCP Servers", "Connected tool servers", Icons.Outlined.Extension, "MCP") {
                navController.navigate(AuraRoutes.AGENT_MCP_SERVERS)
            },
            CommandPaletteEntry("voice_cfg", "Voice & Companion", "Gemini Live & voice settings", Icons.Outlined.RecordVoiceOver, "VOICE") {
                navController.navigate(AuraRoutes.AGENT_COMPANION)
            },
            CommandPaletteEntry("restricted", "Restricted Apps", "Apps THAKUR OS avoids", Icons.Outlined.Block, "SAFETY") {
                navController.navigate(AuraRoutes.SETTINGS_RESTRICTED_APPS)
            },
            CommandPaletteEntry("permissions", "Permissions", "Accessibility, overlay & mic health", Icons.Outlined.Security, "ACCESS") {
                navController.navigate(AuraRoutes.SETTINGS_PERMISSIONS)
            },
            CommandPaletteEntry("settings", "Settings", "App preferences & privacy", Icons.Outlined.Settings, "APP") {
                navController.navigate(AuraRoutes.SETTINGS)
            },
            CommandPaletteEntry("quick", "Quick Task", "Automate any phone action", Icons.Filled.Bolt, "TASK") {
                onStartChat("Quick task")
            },
        )
    }

    when (val state = gateState) {
        is GateState.KillSwitchBlocked ->
            BlockScreen(title = "THAKUR OS is turned off", message = state.message, updateUrl = null, scheme = scheme)
        is GateState.VersionBlocked ->
            BlockScreen(title = "Update required", message = state.message, updateUrl = state.updateUrl, scheme = scheme)
        else -> {
            // Allowed or UpdateAvailable: render the app inside Push Back container
            // with Liquid Glass bottom bar, silk backdrop, and all 6 menu animations.
            var updateDismissed by remember { mutableStateOf(false) }
            PushBackDrawerContainer(
                isOpen = drawerOpen,
                currentRoute = currentRoute,
                onDismiss = { drawerOpen = false },
                onNavigate = { route ->
                    if (glassBottomTabs.any { it.route == route }) {
                        navigateTop(route)
                    } else {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val isLauncherHome = currentRoute == null || currentRoute == AuraRoutes.HOME
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            if (isTabLevel && !isLauncherHome) {
                                GlassyTopCommandStrip(
                                    drawerOpen = drawerOpen,
                                    gooeyOpen = gooeyOpen,
                                    categoriesOpen = categoriesOpen,
                                    kebabOpen = kebabOpen,
                                    onToggleDrawer = {
                                        val next = !drawerOpen
                                        closeAllMenus()
                                        drawerOpen = next
                                    },
                                    onToggleGooey = {
                                        val next = !gooeyOpen
                                        closeAllMenus()
                                        gooeyOpen = next
                                    },
                                    onOpenCommandPalette = {
                                        closeAllMenus()
                                        commandPaletteOpen = true
                                    },
                                    onToggleCategories = {
                                        val next = !categoriesOpen
                                        closeAllMenus()
                                        categoriesOpen = next
                                    },
                                    onToggleKebab = {
                                        val next = !kebabOpen
                                        closeAllMenus()
                                        kebabOpen = next
                                    },
                                )
                            }
                        },
                        bottomBar = {
                            if (!isLauncherHome) {
                                GlassyBottomBar(
                                    currentRoute = currentRoute,
                                    createPanelOpen = createPanelOpen,
                                    onSelectTab = { tab ->
                                        if (tab.isCenterCreate) {
                                            val next = !createPanelOpen
                                            closeAllMenus()
                                            createPanelOpen = next
                                        } else {
                                            closeAllMenus()
                                            tab.route?.let(::navigateTop)
                                        }
                                    },
                                    onBack = {
                                        closeAllMenus()
                                        navController.navigateUp()
                                    },
                                )
                            }
                        },
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        ) {
                            if (state is GateState.UpdateAvailable && state.updateUrl.isNotBlank() && !updateDismissed) {
                                UpdateBanner(
                                    updateUrl = state.updateUrl,
                                    scheme = scheme,
                                    onDismiss = { updateDismissed = true },
                                )
                            }
                            NavHost(
                                navController = navController,
                                startDestination = startDestination,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                enterTransition = { fadeIn(tween(Mono.MotionNormal)) },
                                exitTransition = { fadeOut(tween(Mono.MotionFast)) },
                                popEnterTransition = { fadeIn(tween(Mono.MotionNormal)) },
                                popExitTransition = { fadeOut(tween(Mono.MotionFast)) },
                            ) {
            // ── Top level ──────────────────────────────────────────────
            composable(AuraRoutes.HOME) {
                AuraHomeScreen(
                    onStartChat = onStartChat,
                    onOpenActivity = { navController.navigate(AuraRoutes.ACTIVITY) },
                    onOpenBrowser = onOpenBrowser,
                    onFixHealth = { navController.navigate(AuraRoutes.SETTINGS_PERMISSIONS) },
                    onOpenSettings = { navController.navigate(AuraRoutes.SETTINGS) },
                    onOpenDrawer = {
                        val next = !drawerOpen
                        closeAllMenus()
                        drawerOpen = next
                    },
                    onOpenAgent = { navigateTop(AuraRoutes.AGENT) },
                    onOpenSkills = { navController.navigate(AuraRoutes.AGENT_SKILLS) },
                    onOpenCommandPalette = {
                        closeAllMenus()
                        commandPaletteOpen = true
                    },
                )
            }
            composable(AuraRoutes.AGENT) {
                AgentHubScreen(
                    onOpenBrain = { navController.navigate(AuraRoutes.AGENT_BRAIN) },
                    onOpenTools = { navController.navigate(AuraRoutes.AGENT_TOOLS) },
                    onOpenHooks = { navController.navigate(AuraRoutes.AGENT_HOOKS) },
                    onOpenSkills = { navController.navigate(AuraRoutes.AGENT_SKILLS) },
                    onOpenMemory = { navController.navigate(AuraRoutes.AGENT_MEMORY) },
                    onOpenMcpServers = { navController.navigate(AuraRoutes.AGENT_MCP_SERVERS) },
                    onOpenVoice = { navController.navigate(AuraRoutes.AGENT_COMPANION) },
                )
            }
            composable(AuraRoutes.ACTIVITY) {
                McpSessionLogScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onOpenSession = { id -> navController.navigate(AuraRoutes.sessionDetail(id)) },
                    onOpenRetention = { navController.navigate(AuraRoutes.ACTIVITY_RETENTION) },
                )
            }
            composable(AuraRoutes.SETTINGS) {
                MonoSettingsScreen(
                    onOpenPermissions = { navController.navigate(AuraRoutes.SETTINGS_PERMISSIONS) },
                    onOpenTavilyKey = { navController.navigate(AuraRoutes.SETTINGS_TAVILY) },
                    onRequestScreenCapture = onRequestScreenCapture,
                    onOpenPrivacy = { navController.navigate(AuraRoutes.SETTINGS_PRIVACY) },
                    onOpenTerms = { navController.navigate(AuraRoutes.SETTINGS_TERMS) },
                    onOpenLicenses = { navController.navigate(AuraRoutes.SETTINGS_LICENSES) },
                    onOpenRestrictedApps = { navController.navigate(AuraRoutes.SETTINGS_RESTRICTED_APPS) },
                    onOpenEvalSuite = if (BuildConfig.DEBUG) {
                        { navController.navigate(AuraRoutes.EVAL_SUITE) }
                    } else {
                        null
                    },
                )
            }
            // The eval cockpit spends real provider quota driving the agent through the whole task
            // suite, so it is not part of a release graph at all — not merely hidden in one.
            if (BuildConfig.DEBUG) {
                composable(AuraRoutes.EVAL_SUITE) {
                    EvalSuiteScreen(
                        onOpenTrace = { id -> navController.navigate(AuraRoutes.sessionDetail(id)) },
                    )
                }
            }

            // ── Agent sub-screens ─────────────────────────────────────
            composable(AuraRoutes.AGENT_BRAIN) {
                ProviderSettingsScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onOpenTrustedDevices = { navController.navigate(AuraRoutes.SETTINGS_TRUSTED_DEVICES) },
                    onOpenRestrictedApps = { navController.navigate(AuraRoutes.SETTINGS_RESTRICTED_APPS) },
                    onOpenActivityLog = { navController.navigate(AuraRoutes.ACTIVITY) },
                    onOpenRetention = { navController.navigate(AuraRoutes.ACTIVITY_RETENTION) },
                )
            }
            composable(AuraRoutes.AGENT_TOOLS) {
                ToolsScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.AGENT_HOOKS) {
                HooksScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.AGENT_SKILLS) {
                SkillsScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.AGENT_MEMORY) {
                MemoryScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.AGENT_MCP_SERVERS) {
                McpServersScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.AGENT_COMPANION) {
                CompanionScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onOpenVoiceSettings = { navController.navigate(AuraRoutes.AGENT_VOICE) },
                )
            }
            composable(AuraRoutes.AGENT_VOICE) {
                VoiceSettingsScreen()
            }

            // ── Settings sub-screens ──────────────────────────────────
            composable(AuraRoutes.SETTINGS_PERMISSIONS) {
                PermissionsHealthScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onRequestScreenCapture = onRequestScreenCapture,
                )
            }
            composable(AuraRoutes.SETTINGS_TAVILY) {
                TavilyKeyScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.SETTINGS_PRIVACY) {
                PrivacyPolicyScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.SETTINGS_TERMS) {
                TermsScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.SETTINGS_LICENSES) {
                LicensesScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.SETTINGS_MCP_CENTER) {
                McpCenterScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onOpenActivityLog = { navController.navigate(AuraRoutes.ACTIVITY) },
                    onOpenRetention = { navController.navigate(AuraRoutes.ACTIVITY_RETENTION) },
                    onOpenTrustedDevices = { navController.navigate(AuraRoutes.SETTINGS_TRUSTED_DEVICES) },
                    onOpenRestrictedApps = { navController.navigate(AuraRoutes.SETTINGS_RESTRICTED_APPS) },
                )
            }
            composable(AuraRoutes.SETTINGS_TRUSTED_DEVICES) {
                TrustedDevicesScreen(onNavigateBack = { navController.navigateUp() })
            }
            composable(AuraRoutes.SETTINGS_RESTRICTED_APPS) {
                RestrictedAppsScreen(onNavigateBack = { navController.navigateUp() })
            }

            // ── Activity sub-screens ──────────────────────────────────
            composable(
                route = AuraRoutes.ACTIVITY_SESSION,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
            ) { entry ->
                McpSessionDetailScreen(
                    sessionId = entry.arguments?.getString("sessionId").orEmpty(),
                    onNavigateBack = { navController.navigateUp() },
                )
            }
            composable(AuraRoutes.ACTIVITY_RETENTION) {
                McpLogRetentionScreen(onNavigateBack = { navController.navigateUp() })
            }
                            }
                        }
                    }

                    // 2. Kebab Popover (Edit, Share, Delete)
                    KebabPopoverOverlay(
                        visible = kebabOpen,
                        onDismiss = { kebabOpen = false },
                        onEdit = { navController.navigate(AuraRoutes.AGENT_BRAIN) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "THAKUR OS — on-device AI assistant developed by Shivam Thakur.",
                                )
                            }
                            runCatching {
                                analyticsContext.startActivity(
                                    Intent.createChooser(shareIntent, "Share THAKUR OS")
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        },
                        onDelete = { navController.navigate(AuraRoutes.ACTIVITY_RETENTION) },
                    )

                    // 3. Gooey Menu (Voice, Type, Camera, Quick Task)
                    GooeyMenuOverlay(
                        open = gooeyOpen,
                        onDismiss = { gooeyOpen = false },
                        onVoice = { onStartChat(null) },
                        onType = { commandPaletteOpen = true },
                        onCamera = { onRequestScreenCapture() },
                        onQuickTask = { createPanelOpen = true },
                    )

                    // 4. Fold Down Categories (Apps, Tasks, Skills, History)
                    FoldDownCategoriesOverlay(
                        open = categoriesOpen,
                        onDismiss = { categoriesOpen = false },
                        onApps = { navController.navigate(AuraRoutes.SETTINGS_RESTRICTED_APPS) },
                        onTasks = { navigateTop(AuraRoutes.AGENT) },
                        onSkills = { navController.navigate(AuraRoutes.AGENT_SKILLS) },
                        onHistory = { navigateTop(AuraRoutes.ACTIVITY) },
                    )

                    // 5. Button to Panel (Center + New Task -> Note, Voice Task, Quick Task)
                    NewTaskMorphPanelOverlay(
                        open = createPanelOpen,
                        onDismiss = { createPanelOpen = false },
                        onNote = { note ->
                            if (note != null) {
                                Toast.makeText(analyticsContext, "Note queued for THAKUR OS", Toast.LENGTH_SHORT).show()
                                onStartChat("Remember this note: $note")
                            } else {
                                navigateTop(AuraRoutes.AGENT_MEMORY)
                            }
                        },
                        onVoiceTask = { onStartChat(null) },
                        onQuickTask = { cmd -> onStartChat(cmd) },
                    )

                    // 6. Command Palette (Top search bar drop-down + staggered results)
                    CommandPaletteOverlay(
                        open = commandPaletteOpen,
                        entries = commandEntries,
                        onDismiss = { commandPaletteOpen = false },
                        onAskAi = { q -> onStartChat(q) },
                    )
                }
            }
        }
    }
}
