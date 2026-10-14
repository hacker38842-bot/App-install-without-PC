package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.DepositRequestEntity
import com.example.data.local.entity.LeaderboardPrizeConfigEntity
import com.example.data.local.entity.ReferralConfigEntity
import com.example.data.local.entity.ReferralRewardEntity
import com.example.data.local.entity.RegistrationEntity
import com.example.data.local.entity.RewardEventEntity
import com.example.data.local.entity.SuspiciousReferralEntity
import com.example.data.local.entity.TournamentEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletAccountEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.local.entity.WithdrawalRequestEntity
import com.example.data.repository.ArenaXRepository
import com.example.data.repository.ReferralDashboardSummary
import com.example.util.AppLanguage
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppScreen {
    object Home : AppScreen()
    object Tournaments : AppScreen()
    object Leaderboard : AppScreen()
    object Wallet : AppScreen()
    object Profile : AppScreen()
    object WatchAndEarn : AppScreen()
    object AdminDashboard : AppScreen()
    data class TournamentDetail(val tournamentId: String) : AppScreen()
}

class ArenaXViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArenaXRepository(application)

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf<AppScreen>(AppScreen.Home)

    // Language State
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Transient UI Message State
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Tournament Filters
    val selectedGameFilter = MutableStateFlow("All")
    val selectedFormatFilter = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")

    // Leaderboard Period Filter
    val leaderboardPeriod = MutableStateFlow("WEEKLY")

    // Firebase Auth State
    val firebaseUser: StateFlow<FirebaseUser?> = repository.firebaseUser
    val isAuthInitializing: StateFlow<Boolean> = repository.isAuthInitializing

    // Data Flows from Repository (dynamic per authenticated user)
    val currentUser: StateFlow<UserEntity?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wallet: StateFlow<WalletAccountEntity?> = repository.observeWallet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<WalletTransactionEntity>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tournaments: StateFlow<List<TournamentEntity>> = repository.observeTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userRegistrations: StateFlow<List<RegistrationEntity>> = repository.observeUserRegistrations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePlayers: StateFlow<List<UserEntity>> = repository.observeActivePlayersLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topReferrers: StateFlow<List<Pair<UserEntity, Int>>> = repository.observeTopReferrersLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topDepositors: StateFlow<List<Pair<UserEntity, Long>>> = repository.observeTopDepositorsLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.observeAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewardHistory: StateFlow<List<RewardEventEntity>> = repository.observeRewardHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalRequestEntity>> = repository.observeAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prizeConfigs: StateFlow<List<LeaderboardPrizeConfigEntity>> = repository.observePrizeConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referralSummary: StateFlow<ReferralDashboardSummary?> = repository.observeReferralSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val referralBonusTransactions: StateFlow<List<WalletTransactionEntity>> = repository.observeReferralBonusTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDeposits: StateFlow<List<DepositRequestEntity>> = repository.observePendingDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositRequestEntity>> = repository.observeAllDeposits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referralRewards: StateFlow<List<ReferralRewardEntity>> = repository.observeReferralRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referralConfig: StateFlow<ReferralConfigEntity> = repository.observeReferralConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReferralConfigEntity())

    val suspiciousReferrals: StateFlow<List<SuspiciousReferralEntity>> = repository.observeSuspiciousReferrals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==============================================================
    // Firebase Authentication Actions
    // ==============================================================

    fun login(email: String, pass: String, rememberMe: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.signInWithEmailAndPassword(email, pass, rememberMe)
            result.fold(
                onSuccess = { user ->
                    _userMessage.value = "Welcome back, ${user.displayName ?: user.email}!"
                    onResult(true, "Authentication successful")
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Login failed"
                    _userMessage.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun register(name: String, email: String, pass: String, referralCode: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.createUserWithEmailAndPassword(name, email, pass, referralCode)
            result.fold(
                onSuccess = { user ->
                    _userMessage.value = "Registration successful! Welcome to ARENA X, $name."
                    onResult(true, "Account created successfully")
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Registration failed"
                    _userMessage.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = { msg ->
                    _userMessage.value = msg
                    onResult(true, msg)
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Failed to send reset email"
                    _userMessage.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            _adminSessionToken.value = null
            _screenStack.clear()
            _screenStack.add(AppScreen.Home)
            _currentScreen.value = AppScreen.Home
            _userMessage.value = "Logged out successfully"
        }
    }

    // ==============================================================
    // Navigation Methods
    // ==============================================================

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenStack.size > 1) {
            _screenStack.removeAt(_screenStack.lastIndex)
            _currentScreen.value = _screenStack.last()
            return true
        }
        return false
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // User Operations
    fun updateProfile(displayName: String, inGameName: String, freeFireUid: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, inGameName, freeFireUid)
            _userMessage.value = "Profile updated successfully"
        }
    }

    fun depositFunds(amountPaise: Long, gatewayOrderId: String) {
        viewModelScope.launch {
            val result = repository.processDeposit(amountPaise, gatewayOrderId)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Deposit error: ${it.localizedMessage}" }
            )
        }
    }

    fun submitDepositRequest(amountPaise: Long, utrOrOrderId: String) {
        viewModelScope.launch {
            val result = repository.submitDepositRequest(amountPaise, utrOrOrderId)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Deposit error: ${it.localizedMessage}" }
            )
        }
    }

    fun verifyAndApproveDeposit(depositId: String, adminNote: String? = null) {
        viewModelScope.launch {
            val result = repository.verifyAndApproveDeposit(depositId, adminNote)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Verification error: ${it.localizedMessage}" }
            )
        }
    }

    fun rejectDeposit(depositId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectDeposit(depositId, reason)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Rejection error: ${it.localizedMessage}" }
            )
        }
    }

    fun updateReferralConfig(isEnabled: Boolean, rewardAmountInr: Long, minDepositInr: Long) {
        viewModelScope.launch {
            val result = repository.updateReferralConfig(isEnabled, rewardAmountInr, minDepositInr)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Config error: ${it.localizedMessage}" }
            )
        }
    }

    fun requestWithdrawal(amountPaise: Long, upiId: String) {
        viewModelScope.launch {
            val result = repository.requestWithdrawal(amountPaise, upiId)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = "Withdrawal error: ${it.localizedMessage}" }
            )
        }
    }

    fun joinTournament(tournamentId: String, inGameName: String, freeFireUid: String) {
        viewModelScope.launch {
            val result = repository.registerForTournament(tournamentId, inGameName, freeFireUid)
            result.fold(
                onSuccess = { _userMessage.value = it },
                onFailure = { _userMessage.value = it.localizedMessage ?: "Failed to join tournament" }
            )
        }
    }

    fun claimAdReward() {
        viewModelScope.launch {
            val result = repository.claimAdReward()
            result.fold(
                onSuccess = { paise -> _userMessage.value = "Reward verified! Added ₹${paise / 100.0} to your balance." },
                onFailure = { _userMessage.value = it.localizedMessage ?: "Reward claim failed" }
            )
        }
    }

    // ==============================================================
    // Admin Session State & Authorization
    // ==============================================================

    private val _adminSessionToken = MutableStateFlow<String?>(null)
    val adminSessionToken: StateFlow<String?> = _adminSessionToken.asStateFlow()

    private val _adminLockoutSec = MutableStateFlow(0L)
    val adminLockoutSec: StateFlow<Long> = _adminLockoutSec.asStateFlow()

    fun adminLogin(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.adminLogin(username, password)
            result.fold(
                onSuccess = { token ->
                    _adminSessionToken.value = token
                    _adminLockoutSec.value = 0L
                    _userMessage.value = "Administrator session authenticated successfully."
                    onResult(true, "Authentication successful")
                },
                onFailure = { err ->
                    val lockoutSec = repository.getAdminLockoutRemainingSec()
                    _adminLockoutSec.value = lockoutSec
                    val msg = err.localizedMessage ?: "Invalid administrator credentials"
                    _userMessage.value = msg
                    onResult(false, msg)
                }
            )
        }
    }

    fun adminLogout() {
        viewModelScope.launch {
            val token = _adminSessionToken.value
            if (token != null) {
                repository.adminLogout(token)
            }
            _adminSessionToken.value = null
            _userMessage.value = "Administrator logged out securely."
            if (_currentScreen.value is AppScreen.AdminDashboard) {
                navigateBack()
            }
        }
    }

    fun adminCreateTournament(tournament: TournamentEntity) {
        val token = _adminSessionToken.value
        if (token == null) {
            _userMessage.value = "Access Denied: Please log in as administrator."
            return
        }
        viewModelScope.launch {
            try {
                repository.adminCreateTournament(token, tournament)
                _userMessage.value = "Tournament '${tournament.title}' created and published!"
            } catch (e: SecurityException) {
                _adminSessionToken.value = null
                _userMessage.value = e.localizedMessage ?: "Session expired. Please log in again."
            }
        }
    }

    fun adminReleaseRoom(tournamentId: String, roomId: String, roomPass: String) {
        val token = _adminSessionToken.value
        if (token == null) {
            _userMessage.value = "Access Denied: Please log in as administrator."
            return
        }
        viewModelScope.launch {
            try {
                repository.adminUpdateTournamentStatus(token, tournamentId, "ROOM_RELEASED", roomId, roomPass)
                _userMessage.value = "Room credentials released to registered players!"
            } catch (e: SecurityException) {
                _adminSessionToken.value = null
                _userMessage.value = e.localizedMessage ?: "Session expired. Please log in again."
            }
        }
    }

    fun adminFinalizeScores(tournamentId: String, rank1UserId: String, rank2UserId: String?, rank3UserId: String?) {
        val token = _adminSessionToken.value
        if (token == null) {
            _userMessage.value = "Access Denied: Please log in as administrator."
            return
        }
        viewModelScope.launch {
            try {
                repository.adminFinalizeScores(token, tournamentId, rank1UserId, rank2UserId, rank3UserId)
                _userMessage.value = "Scores finalized and prizes automatically distributed!"
            } catch (e: SecurityException) {
                _adminSessionToken.value = null
                _userMessage.value = e.localizedMessage ?: "Session expired. Please log in again."
            }
        }
    }

    fun adminReviewWithdrawal(requestId: String, approve: Boolean, note: String) {
        val token = _adminSessionToken.value
        if (token == null) {
            _userMessage.value = "Access Denied: Please log in as administrator."
            return
        }
        viewModelScope.launch {
            try {
                repository.adminReviewWithdrawal(token, requestId, approve, note)
                _userMessage.value = if (approve) "Withdrawal approved and paid!" else "Withdrawal rejected and refunded!"
            } catch (e: SecurityException) {
                _adminSessionToken.value = null
                _userMessage.value = e.localizedMessage ?: "Session expired. Please log in again."
            }
        }
    }

    fun adminUpdatePrizeConfig(config: LeaderboardPrizeConfigEntity) {
        val token = _adminSessionToken.value
        if (token == null) {
            _userMessage.value = "Access Denied: Please log in as administrator."
            return
        }
        viewModelScope.launch {
            try {
                repository.adminUpdatePrizeConfig(token, config)
                _userMessage.value = "Leaderboard prize configuration updated."
            } catch (e: SecurityException) {
                _adminSessionToken.value = null
                _userMessage.value = e.localizedMessage ?: "Session expired. Please log in again."
            }
        }
    }
}
