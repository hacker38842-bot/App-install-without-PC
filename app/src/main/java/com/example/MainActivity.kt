package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TournamentEntity
import com.example.ui.components.ArenaXBottomNav
import com.example.ui.components.ArenaXTopBar
import com.example.ui.dialogs.AdminLoginDialog
import com.example.ui.dialogs.DepositDialog
import com.example.ui.dialogs.JoinTournamentDialog
import com.example.ui.dialogs.RoomCredentialsDialog
import com.example.ui.dialogs.WithdrawDialog
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reward.WatchEarnScreen
import com.example.ui.screens.tournaments.TournamentDetailScreen
import com.example.ui.screens.tournaments.TournamentListScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.ArenaXTheme
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ArenaXViewModel
import com.example.util.AppLanguage
import com.example.util.FirebaseConfigManager

class MainActivity : ComponentActivity() {

    private val viewModel: ArenaXViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase with the explicit project configuration
        FirebaseConfigManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ArenaXTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ArenaXViewModel) {
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val isAuthInitializing by viewModel.isAuthInitializing.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val tournaments by viewModel.tournaments.collectAsStateWithLifecycle()
    val userRegistrations by viewModel.userRegistrations.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val activePlayers by viewModel.activePlayers.collectAsStateWithLifecycle()
    val topReferrers by viewModel.topReferrers.collectAsStateWithLifecycle()
    val topDepositors by viewModel.topDepositors.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val rewardHistory by viewModel.rewardHistory.collectAsStateWithLifecycle()
    val allWithdrawals by viewModel.allWithdrawals.collectAsStateWithLifecycle()
    val prizeConfigs by viewModel.prizeConfigs.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val adminSessionToken by viewModel.adminSessionToken.collectAsStateWithLifecycle()
    val adminLockoutSec by viewModel.adminLockoutSec.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var joinTournamentTarget by remember { mutableStateOf<TournamentEntity?>(null) }
    var roomCredentialsTarget by remember { mutableStateOf<TournamentEntity?>(null) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // 1. Initial Authentication Check Screen
    if (isAuthInitializing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arena_x_logo),
                    contentDescription = "ARENA X Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = CyberCyan,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Connecting to Firebase...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    // 2. Unauthenticated State Gate: Show AuthScreen (Login / Register / Forgot Password)
    if (firebaseUser == null) {
        AuthScreen(
            currentLanguage = language,
            onLogin = { email, pass, rememberMe, callback ->
                viewModel.login(email, pass, rememberMe, callback)
            },
            onRegister = { name, email, pass, callback ->
                viewModel.register(name, email, pass, callback)
            },
            onForgotPassword = { email, callback ->
                viewModel.forgotPassword(email, callback)
            }
        )
        return
    }

    // 3. Authenticated State: Full Application
    val isSubScreen = currentScreen is AppScreen.TournamentDetail ||
            currentScreen is AppScreen.WatchAndEarn ||
            currentScreen is AppScreen.AdminDashboard

    val handleNavigateToScreen: (AppScreen) -> Unit = { targetScreen ->
        if (targetScreen is AppScreen.AdminDashboard && adminSessionToken == null) {
            showAdminLoginDialog = true
        } else {
            viewModel.navigateTo(targetScreen)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isSubScreen) {
                ArenaXTopBar(
                    balancePaise = wallet?.balancePaise ?: 0L,
                    currentLanguage = language,
                    isAdmin = true, // Shows icon to access secure admin portal
                    onWalletClick = { viewModel.navigateTo(AppScreen.Wallet) },
                    onLanguageToggle = {
                        val next = if (language == AppLanguage.ENGLISH) AppLanguage.BENGALI else AppLanguage.ENGLISH
                        viewModel.setLanguage(next)
                    },
                    onAdminClick = {
                        if (adminSessionToken != null) {
                            viewModel.navigateTo(AppScreen.AdminDashboard)
                        } else {
                            showAdminLoginDialog = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isSubScreen) {
                ArenaXBottomNav(
                    currentScreen = currentScreen,
                    currentLanguage = language,
                    onNavigate = handleNavigateToScreen
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Home -> {
                    HomeScreen(
                        currentUser = currentUser,
                        tournaments = tournaments,
                        userRegistrations = userRegistrations,
                        announcements = announcements,
                        currentLanguage = language,
                        onNavigate = handleNavigateToScreen,
                        onJoinTournament = { joinTournamentTarget = it },
                        onViewRoom = { roomCredentialsTarget = it }
                    )
                }
                is AppScreen.Tournaments -> {
                    TournamentListScreen(
                        tournaments = tournaments,
                        userRegistrations = userRegistrations,
                        currentLanguage = language,
                        onNavigate = handleNavigateToScreen,
                        onJoinTournament = { joinTournamentTarget = it },
                        onViewRoom = { roomCredentialsTarget = it }
                    )
                }
                is AppScreen.Leaderboard -> {
                    LeaderboardScreen(
                        activePlayers = activePlayers,
                        topReferrers = topReferrers,
                        topDepositors = topDepositors,
                        prizeConfigs = prizeConfigs
                    )
                }
                is AppScreen.Wallet -> {
                    WalletScreen(
                        wallet = wallet,
                        transactions = transactions,
                        onAddCashClick = { showDepositDialog = true },
                        onWithdrawClick = { showWithdrawDialog = true }
                    )
                }
                is AppScreen.Profile -> {
                    ProfileScreen(
                        user = currentUser,
                        currentLanguage = language,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onUpdateProfile = { name, ign, uid -> viewModel.updateProfile(name, ign, uid) },
                        onNavigate = handleNavigateToScreen,
                        onLogout = { viewModel.logout() }
                    )
                }
                is AppScreen.WatchAndEarn -> {
                    WatchEarnScreen(
                        rewardHistory = rewardHistory,
                        onBack = { viewModel.navigateBack() },
                        onClaimReward = { viewModel.claimAdReward() }
                    )
                }
                is AppScreen.AdminDashboard -> {
                    if (adminSessionToken == null) {
                        // Strict Gate: Never show dashboard without active administrator session
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Administrator Authorization Required",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "This control panel is protected. Please authenticate with administrator credentials.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { showAdminLoginDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Log In as Administrator", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        AdminDashboardScreen(
                            tournaments = tournaments,
                            withdrawals = allWithdrawals,
                            prizeConfigs = prizeConfigs,
                            onBack = { viewModel.navigateBack() },
                            onCreateTournament = { viewModel.adminCreateTournament(it) },
                            onReleaseRoom = { id, rId, rPass -> viewModel.adminReleaseRoom(id, rId, rPass) },
                            onFinalizeScores = { id, r1, r2, r3 -> viewModel.adminFinalizeScores(id, r1, r2, r3) },
                            onReviewWithdrawal = { id, approve, note -> viewModel.adminReviewWithdrawal(id, approve, note) },
                            onLogout = { viewModel.adminLogout() }
                        )
                    }
                }
                is AppScreen.TournamentDetail -> {
                    val tournament = tournaments.find { it.id == screen.tournamentId }
                    val isReg = userRegistrations.any { it.tournamentId == screen.tournamentId }
                    val regList = remember(screen.tournamentId, userRegistrations) {
                        userRegistrations.filter { it.tournamentId == screen.tournamentId }
                    }
                    TournamentDetailScreen(
                        tournament = tournament,
                        isRegistered = isReg,
                        registrations = regList,
                        onBack = { viewModel.navigateBack() },
                        onJoinTournament = { tournament?.let { joinTournamentTarget = it } },
                        onViewRoom = { tournament?.let { roomCredentialsTarget = it } }
                    )
                }
            }
        }
    }

    // Admin Login Dialog
    if (showAdminLoginDialog) {
        AdminLoginDialog(
            lockoutRemainingSec = adminLockoutSec,
            onDismiss = { showAdminLoginDialog = false },
            onSubmitLogin = { username, password, callback ->
                viewModel.adminLogin(username, password) { success, msg ->
                    callback(success, msg)
                    if (success) {
                        viewModel.navigateTo(AppScreen.AdminDashboard)
                    }
                }
            }
        )
    }

    // Join Tournament Dialog
    joinTournamentTarget?.let { tour ->
        JoinTournamentDialog(
            tournament = tour,
            user = currentUser,
            userBalancePaise = wallet?.balancePaise ?: 0L,
            onDismiss = { joinTournamentTarget = null },
            onConfirmJoin = { ign, uid ->
                viewModel.joinTournament(tour.id, ign, uid)
                joinTournamentTarget = null
            }
        )
    }

    // Room Credentials Dialog
    roomCredentialsTarget?.let { tour ->
        RoomCredentialsDialog(
            tournament = tour,
            onDismiss = { roomCredentialsTarget = null }
        )
    }

    // Add Cash Deposit Dialog
    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onConfirmDeposit = { paise, orderId ->
                viewModel.depositFunds(paise, orderId)
                showDepositDialog = false
            }
        )
    }

    // Withdraw Dialog
    if (showWithdrawDialog) {
        WithdrawDialog(
            availableBalancePaise = wallet?.balancePaise ?: 0L,
            onDismiss = { showWithdrawDialog = false },
            onConfirmWithdraw = { paise, upiId ->
                viewModel.requestWithdrawal(paise, upiId)
                showWithdrawDialog = false
            }
        )
    }
}
