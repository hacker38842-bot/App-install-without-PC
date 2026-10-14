package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminCredentialEntity
import com.example.data.local.entity.AdminSessionEntity
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.DepositRequestEntity
import com.example.data.local.entity.LeaderboardPrizeConfigEntity
import com.example.data.local.entity.ReferralConfigEntity
import com.example.data.local.entity.ReferralEntity
import com.example.data.local.entity.ReferralRewardEntity
import com.example.data.local.entity.RegistrationEntity
import com.example.data.local.entity.RewardEventEntity
import com.example.data.local.entity.SuspiciousReferralEntity
import com.example.data.local.entity.TournamentEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletAccountEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.local.entity.WithdrawalRequestEntity
import com.example.util.AdminAuthUtil
import com.example.util.FirebaseConfigManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class ArenaXRepository(
    private val context: Context,
    private val db: AppDatabase
) {

    companion object {
        const val DAILY_AD_LIMIT = 5
        const val AD_COOLDOWN_MS = 60_000L // 60 seconds
        const val MIN_WITHDRAWAL_PAISE = 5000L // ₹50.00
        const val MAX_WITHDRAWAL_PAISE = 500000L // ₹5,000.00
        const val ADMIN_SESSION_DURATION_MS = 30 * 60 * 1000L // 30 minutes session timeout
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes lockout
    }

    constructor(context: Context) : this(context, AppDatabase.getDatabase(context))

    private val auth: FirebaseAuth = FirebaseConfigManager.getAuth(context)

    // Observable Firebase Authentication User State
    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    // Observable Auth Initializing State (for initial auth check screen)
    private val _isAuthInitializing = MutableStateFlow(true)
    val isAuthInitializing: StateFlow<Boolean> = _isAuthInitializing.asStateFlow()

    // Track active user ID dynamically
    val currentUserIdFlow: Flow<String?> = _firebaseUser.map { it?.uid }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _firebaseUser.value = user
            _isAuthInitializing.value = false
        }
    }

    fun getCurrentUid(): String? = auth.currentUser?.uid

    // ==============================================================
    // 1. FIREBASE AUTHENTICATION METHODS
    // ==============================================================

    /**
     * Real Firebase Authentication: signInWithEmailAndPassword()
     */
    suspend fun signInWithEmailAndPassword(
        email: String,
        pass: String,
        rememberMe: Boolean
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw IllegalStateException("Authentication failed: User is null")
            _firebaseUser.value = user

            // Sync/Verify User Profile in Database with UID
            ensureUserProfileExists(user, null)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    /**
     * Real Firebase Authentication: createUserWithEmailAndPassword()
     */
    suspend fun createUserWithEmailAndPassword(
        fullName: String,
        email: String,
        pass: String,
        referralCode: String? = null
    ): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw IllegalStateException("Registration failed: User is null")
            _firebaseUser.value = user

            // Create initial user profile and zero-balance wallet with UID as permanent identifier
            ensureUserProfileExists(user, fullName.trim(), referralCode)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    /**
     * Real Firebase Authentication: sendPasswordResetEmail()
     */
    suspend fun sendPasswordResetEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success("If an account exists with $email, a password reset link has been sent.")
        } catch (e: Exception) {
            Result.failure(mapAuthException(e))
        }
    }

    /**
     * Real Firebase Authentication: signOut()
     */
    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        auth.signOut()
        _firebaseUser.value = null
    }

    /**
     * Checks if current authenticated user has verified administrator authorization.
     * Uses Firebase custom claims or server administrator records.
     */
    suspend fun verifyAdminAuthorization(): Boolean = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext false
        try {
            val tokenResult = user.getIdToken(true).await()
            val claims = tokenResult.claims
            val isAdminClaim = claims["admin"] == true || claims["role"] == "admin"
            if (isAdminClaim) return@withContext true

            // Also check local database admin flag mapped to this account
            val userEntity = db.userDao().getUser(user.uid)
            return@withContext userEntity?.isAdmin == true
        } catch (e: Exception) {
            val userEntity = db.userDao().getUser(user.uid)
            return@withContext userEntity?.isAdmin == true
        }
    }

    /**
     * Guarantees user profile exists for Firebase UID, preserving existing balances and history.
     */
    suspend fun ensureUserProfileExists(user: FirebaseUser, displayNameFallback: String?, referralCodeInput: String? = null) {
        val uid = user.uid
        val existing = db.userDao().getUser(uid)
        val now = System.currentTimeMillis()

        if (existing == null) {
            // New user registered: initialize with 0 matches, 0 points, zero wallet
            val name = displayNameFallback?.ifBlank { null }
                ?: user.displayName?.ifBlank { null }
                ?: user.email?.substringBefore("@")?.replace(".", " ")?.capitalize()
                ?: "Player"

            val randomSuffix = (1000..9999).random()
            val refCode = "ARENA" + uid.take(4).uppercase() + randomSuffix

            var permanentReferredByCode: String? = null
            val cleanRefCode = referralCodeInput?.trim()?.uppercase()?.ifBlank { null }
            if (cleanRefCode != null) {
                val referrer = db.userDao().getUserByReferralCode(cleanRefCode)
                if (referrer != null && referrer.id != uid) {
                    permanentReferredByCode = referrer.referralCode
                    val refId = "ref_${referrer.id}_$uid"
                    db.referralDao().insertReferral(
                        ReferralEntity(
                            id = refId,
                            referrerUserId = referrer.id,
                            referredUserId = uid,
                            referredUserName = name,
                            status = "AWAITING_DEPOSIT", // Not qualified yet until first verified qualifying deposit!
                            bonusPaise = 0L, // Registration alone gives ₹0 reward
                            createdAtMs = now
                        )
                    )
                    val rewardId = "ref_reward_$uid"
                    db.referralDao().insertReward(
                        ReferralRewardEntity(
                            id = rewardId,
                            referralId = refId,
                            referrerUid = referrer.id,
                            referredUid = uid,
                            firstQualifyingDepositId = null,
                            rewardAmountPaise = 500L,
                            rewardStatus = "PENDING",
                            rewardedAtMs = null,
                            createdAtMs = now
                        )
                    )
                } else if (referrer?.id == uid || cleanRefCode == refCode) {
                    // Self-referral attempt! Log suspicious and reject
                    db.referralDao().insertSuspiciousReferral(
                        SuspiciousReferralEntity(
                            id = "sus_${UUID.randomUUID().toString().take(8)}",
                            attemptingUserId = uid,
                            attemptedCode = cleanRefCode,
                            reason = "SELF_REFERRAL_ATTEMPT",
                            timestampMs = now
                        )
                    )
                }
            }

            val newUser = UserEntity(
                id = uid,
                displayName = name,
                inGameName = "",
                freeFireUid = "",
                email = user.email ?: "",
                referralCode = refCode,
                referredByCode = permanentReferredByCode,
                avatarUrl = user.photoUrl?.toString() ?: "",
                joinDateMs = now,
                matchesPlayed = 0,
                matchesWon = 0,
                totalKills = 0,
                points = 0,
                isAdmin = false
            )
            db.userDao().insertUser(newUser)

            // Initialize zero-balance wallet
            val existingWallet = db.walletDao().getWalletAccount(uid)
            if (existingWallet == null) {
                val newWallet = WalletAccountEntity(
                    userId = uid,
                    balancePaise = 0L,
                    pendingWithdrawalPaise = 0L,
                    totalDepositedPaise = 0L,
                    totalWithdrawnPaise = 0L,
                    totalWonPaise = 0L,
                    bonusPoints = 0L
                )
                db.walletDao().insertWalletAccount(newWallet)
            }
        } else {
            // Existing user: Preserve existing wallet balance, tournament history, and referrals!
            // Update non-financial profile fields if necessary
            var updated = existing
            if (existing.email.isBlank() && !user.email.isNullOrBlank()) {
                updated = updated.copy(email = user.email!!)
            }
            if (displayNameFallback != null && existing.displayName.isBlank()) {
                updated = updated.copy(displayName = displayNameFallback)
            }
            if (updated != existing) {
                db.userDao().updateUser(updated)
            }
        }
    }

    private fun mapAuthException(e: Exception): Exception {
        val msg = e.message ?: ""
        return when {
            msg.contains("ERROR_INVALID_CREDENTIAL", ignoreCase = true) ||
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) ->
                Exception("Invalid email or password. Please verify and try again.")

            msg.contains("email-already-in-use", ignoreCase = true) ||
            msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                Exception("This email address is already registered. Please log in instead.")

            msg.contains("invalid-email", ignoreCase = true) ||
            msg.contains("INVALID_EMAIL", ignoreCase = true) ->
                Exception("The email address format is invalid.")

            msg.contains("weak-password", ignoreCase = true) ||
            msg.contains("WEAK_PASSWORD", ignoreCase = true) ->
                Exception("Password is too weak. Please use at least 6 characters.")

            msg.contains("user-disabled", ignoreCase = true) ||
            msg.contains("USER_DISABLED", ignoreCase = true) ->
                Exception("This account has been disabled by administrators.")

            msg.contains("too-many-requests", ignoreCase = true) ||
            msg.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
                Exception("Too many failed attempts. Please wait a moment and try again.")

            msg.contains("network", ignoreCase = true) ||
            msg.contains("Timeout", ignoreCase = true) ->
                Exception("Network connection failure. Please check your internet connection.")

            else -> Exception(e.localizedMessage ?: "Authentication operation failed.")
        }
    }

    // ==============================================================
    // 2. USER PROFILE
    // ==============================================================

    fun observeCurrentUser(): Flow<UserEntity?> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.userDao().getUserFlow(uid)
        } else {
            flowOf(null)
        }
    }

    suspend fun updateProfile(displayName: String, inGameName: String, freeFireUid: String) {
        val uid = getCurrentUid() ?: return
        val user = db.userDao().getUser(uid) ?: return
        db.userDao().updateUser(
            user.copy(
                displayName = displayName.trim(),
                inGameName = inGameName.trim(),
                freeFireUid = freeFireUid.trim()
            )
        )
    }

    // ==============================================================
    // 3. WALLET & TRANSACTIONS
    // ==============================================================

    fun observeWallet(): Flow<WalletAccountEntity?> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.walletDao().getWalletAccountFlow(uid)
        } else {
            flowOf(null)
        }
    }

    fun observeTransactions(): Flow<List<WalletTransactionEntity>> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.walletDao().getTransactionsFlow(uid)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun processDeposit(amountPaise: Long, gatewayOrderId: String): Result<String> {
        val uid = getCurrentUid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        if (amountPaise <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        return try {
            db.withTransaction {
                val wallet = db.walletDao().getWalletAccount(uid)
                    ?: WalletAccountEntity(uid)
                val updatedWallet = wallet.copy(
                    balancePaise = wallet.balancePaise + amountPaise,
                    totalDepositedPaise = wallet.totalDepositedPaise + amountPaise
                )
                db.walletDao().insertWalletAccount(updatedWallet)

                val tx = WalletTransactionEntity(
                    id = "tx_${UUID.randomUUID().toString().take(8)}",
                    userId = uid,
                    amountPaise = amountPaise,
                    type = "DEPOSIT",
                    status = "SUCCESS",
                    title = "UPI Deposit Verified",
                    description = "Added via Payment Gateway: $gatewayOrderId",
                    referenceId = gatewayOrderId,
                    timestampMs = System.currentTimeMillis()
                )
                db.walletDao().insertTransaction(tx)

                // Check and Award one-time ₹5 referral reward if first qualifying deposit
                checkAndAwardReferralReward(uid, tx.id, amountPaise)
            }
            Result.success("Deposit of ₹${amountPaise / 100} successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submits a deposit request that remains PENDING until verified by administrator.
     * Registration or submitting a UTR number alone does NOT credit money or trigger rewards.
     */
    suspend fun submitDepositRequest(amountPaise: Long, utrOrOrderId: String): Result<String> {
        val uid = getCurrentUid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        if (amountPaise <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        val cleanUtr = utrOrOrderId.trim()
        if (cleanUtr.length < 4) return Result.failure(IllegalArgumentException("Please enter a valid UTR or Order ID"))

        return try {
            val user = db.userDao().getUser(uid) ?: throw IllegalStateException("User profile not found")
            val depId = "dep_${UUID.randomUUID().toString().take(8)}"
            val now = System.currentTimeMillis()

            db.withTransaction {
                val depRequest = DepositRequestEntity(
                    id = depId,
                    userId = uid,
                    userDisplayName = user.displayName,
                    amountPaise = amountPaise,
                    utrOrOrderId = cleanUtr,
                    status = "PENDING_VERIFICATION",
                    createdAtMs = now
                )
                db.depositDao().insertDeposit(depRequest)

                val tx = WalletTransactionEntity(
                    id = "tx_$depId",
                    userId = uid,
                    amountPaise = amountPaise,
                    type = "DEPOSIT",
                    status = "PENDING",
                    title = "Deposit (Pending Verification)",
                    description = "UTR: $cleanUtr awaiting admin approval",
                    referenceId = depId,
                    timestampMs = now
                )
                db.walletDao().insertTransaction(tx)
            }
            Result.success("Deposit of ₹${amountPaise / 100} submitted. Awaiting verification.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genuine payment verification and approval by authorized administrator.
     * Executes atomic wallet credit and evaluates one-time ₹5 referral reward.
     */
    suspend fun verifyAndApproveDeposit(depositId: String, adminNote: String? = null): Result<String> {
        val isAdmin = verifyAdminAuthorization()
        if (!isAdmin) {
            return Result.failure(SecurityException("Unauthorized: Admin credentials required to verify deposits"))
        }

        return try {
            db.withTransaction {
                val deposit = db.depositDao().getDeposit(depositId)
                    ?: throw IllegalArgumentException("Deposit request $depositId not found")

                if (deposit.status == "VERIFIED_SUCCESS") {
                    return@withTransaction Result.success("Deposit already verified (idempotent)")
                }
                if (deposit.status == "REJECTED") {
                    throw IllegalStateException("Cannot approve a rejected deposit")
                }

                val now = System.currentTimeMillis()
                // Update deposit request
                db.depositDao().updateDeposit(
                    deposit.copy(
                        status = "VERIFIED_SUCCESS",
                        verifiedAtMs = now,
                        adminNote = adminNote ?: "Approved by admin"
                    )
                )

                // Update ledger transaction
                val tx = db.walletDao().getTransactionById("tx_${deposit.id}")
                if (tx != null) {
                    db.walletDao().insertTransaction(
                        tx.copy(
                            status = "SUCCESS",
                            title = "UPI Deposit Verified",
                            description = "Verified and credited: ${deposit.utrOrOrderId}"
                        )
                    )
                }

                // Credit user wallet
                val wallet = db.walletDao().getWalletAccount(deposit.userId)
                    ?: WalletAccountEntity(deposit.userId)
                val updatedWallet = wallet.copy(
                    balancePaise = wallet.balancePaise + deposit.amountPaise,
                    totalDepositedPaise = wallet.totalDepositedPaise + deposit.amountPaise
                )
                db.walletDao().insertWalletAccount(updatedWallet)

                // Check and Award one-time ₹5 referral reward to referrer
                checkAndAwardReferralReward(
                    depositingUserId = deposit.userId,
                    depositId = deposit.id,
                    depositAmountPaise = deposit.amountPaise
                )

                Result.success("Deposit of ₹${deposit.amountPaise / 100} verified and approved successfully")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rejects an unverified or fraudulent deposit request.
     */
    suspend fun rejectDeposit(depositId: String, reason: String): Result<String> {
        val isAdmin = verifyAdminAuthorization()
        if (!isAdmin) {
            return Result.failure(SecurityException("Unauthorized: Admin credentials required"))
        }

        return try {
            db.withTransaction {
                val deposit = db.depositDao().getDeposit(depositId)
                    ?: throw IllegalArgumentException("Deposit request not found")

                val now = System.currentTimeMillis()
                db.depositDao().updateDeposit(
                    deposit.copy(
                        status = "REJECTED",
                        verifiedAtMs = now,
                        adminNote = reason
                    )
                )

                val tx = db.walletDao().getTransactionById("tx_${deposit.id}")
                if (tx != null) {
                    db.walletDao().insertTransaction(
                        tx.copy(
                            status = "REJECTED",
                            title = "Deposit Rejected",
                            description = "Rejected: $reason"
                        )
                    )
                }
            }
            Result.success("Deposit rejected")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Core Idempotent One-Time Referral Reward Rule:
     * - Referring user receives ₹5 ONLY ONCE for each eligible referred user.
     * - Triggered when the referred user completes their FIRST qualifying verified deposit.
     * - Registration alone never triggers reward.
     * - Pending deposits never trigger reward.
     * - Subsequent deposits from same user yield exactly ₹0 additional reward.
     * - Self-referrals strictly rejected.
     */
    suspend fun checkAndAwardReferralReward(
        depositingUserId: String,
        depositId: String,
        depositAmountPaise: Long
    ) {
        val config = db.referralDao().getReferralConfig() ?: ReferralConfigEntity()
        if (!config.isEnabled) return

        if (depositAmountPaise < config.minQualifyingDepositPaise) {
            // Does not meet minimum qualifying deposit
            return
        }

        val user = db.userDao().getUser(depositingUserId) ?: return
        val refCode = user.referredByCode ?: return
        val referrer = db.userDao().getUserByReferralCode(refCode) ?: return

        // Prevent self-referral
        if (referrer.id == user.id || referrer.id == depositingUserId) {
            return
        }

        val rewardRecordId = "ref_reward_${depositingUserId}"
        val existingReward = db.referralDao().getReward(rewardRecordId)
        if (existingReward?.rewardStatus == "PAID") {
            // Already paid! Exactly ₹0 additional reward.
            return
        }

        val rewardTxId = "ref_reward_tx_${depositingUserId}"
        val existingTx = db.walletDao().getTransactionById(rewardTxId)
        if (existingTx != null) {
            // Already credited!
            return
        }

        // Check verified deposits history to ensure this is the FIRST qualifying deposit
        val verifiedDeposits = db.walletDao().getVerifiedDepositsForUser(depositingUserId)
        val qualifyingDeposits = verifiedDeposits.filter { it.amountPaise >= config.minQualifyingDepositPaise }
        if (qualifyingDeposits.size > 1) {
            val earliest = qualifyingDeposits.minByOrNull { it.timestampMs }
            if (earliest != null && existingReward?.firstQualifyingDepositId != null && existingReward.firstQualifyingDepositId != earliest.id) {
                return
            }
        }

        val rewardAmountPaise = config.rewardAmountPaise // Exactly ₹5.00 (500L)

        // 1. Credit referring user's wallet
        val referrerWallet = db.walletDao().getWalletAccount(referrer.id) ?: WalletAccountEntity(referrer.id)
        val updatedReferrerWallet = referrerWallet.copy(
            balancePaise = referrerWallet.balancePaise + rewardAmountPaise
        )
        db.walletDao().insertWalletAccount(updatedReferrerWallet)

        // 2. Insert unique referral reward transaction in wallet_ledger
        val rewardTx = WalletTransactionEntity(
            id = rewardTxId,
            userId = referrer.id,
            amountPaise = rewardAmountPaise,
            type = "REFERRAL_BONUS",
            status = "SUCCESS",
            title = "Referral Reward (₹${rewardAmountPaise / 100})",
            description = "One-time bonus: ${user.displayName} completed first qualifying deposit",
            referenceId = depositId,
            timestampMs = System.currentTimeMillis()
        )
        db.walletDao().insertTransaction(rewardTx)

        // 3. Mark reward status as PAID
        val updatedReward = ReferralRewardEntity(
            id = rewardRecordId,
            referralId = "ref_${referrer.id}_${depositingUserId}",
            referrerUid = referrer.id,
            referredUid = depositingUserId,
            firstQualifyingDepositId = depositId,
            rewardAmountPaise = rewardAmountPaise,
            rewardStatus = "PAID",
            rewardedAtMs = System.currentTimeMillis()
        )
        db.referralDao().insertReward(updatedReward)

        // 4. Update referral entity status to QUALIFIED_PAID
        val refEntity = db.referralDao().getReferralByReferredUser(depositingUserId)
        if (refEntity != null) {
            db.referralDao().updateReferral(
                refEntity.copy(
                    status = "QUALIFIED_PAID",
                    bonusPaise = rewardAmountPaise
                )
            )
        }
    }

    /**
     * Admin configuration for referral system.
     */
    suspend fun updateReferralConfig(
        isEnabled: Boolean,
        rewardAmountInr: Long,
        minQualifyingDepositInr: Long
    ): Result<String> {
        val isAdmin = verifyAdminAuthorization()
        if (!isAdmin) {
            return Result.failure(SecurityException("Unauthorized: Admin credentials required to modify referral configuration"))
        }
        if (rewardAmountInr < 0 || minQualifyingDepositInr < 0) {
            return Result.failure(IllegalArgumentException("Amounts cannot be negative"))
        }

        return try {
            val config = ReferralConfigEntity(
                id = "default_config",
                isEnabled = isEnabled,
                rewardAmountPaise = rewardAmountInr * 100L,
                minQualifyingDepositPaise = minQualifyingDepositInr * 100L,
                updatedAtMs = System.currentTimeMillis()
            )
            db.referralDao().insertReferralConfig(config)
            Result.success("Referral configuration updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestWithdrawal(amountPaise: Long, upiId: String): Result<String> {
        val uid = getCurrentUid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        if (amountPaise < MIN_WITHDRAWAL_PAISE) {
            return Result.failure(IllegalArgumentException("Minimum withdrawal is ₹50"))
        }
        if (amountPaise > MAX_WITHDRAWAL_PAISE) {
            return Result.failure(IllegalArgumentException("Maximum withdrawal is ₹5,000"))
        }
        if (!upiId.contains("@") || upiId.length < 5) {
            return Result.failure(IllegalArgumentException("Invalid UPI ID format (e.g. name@okhdfcbank)"))
        }

        return try {
            db.withTransaction {
                val wallet = db.walletDao().getWalletAccount(uid)
                    ?: throw IllegalStateException("Wallet not found")

                if (wallet.balancePaise < amountPaise) {
                    throw IllegalStateException("Insufficient balance. Available: ₹${wallet.balancePaise / 100}")
                }

                // Deduct from available balance, add to pending
                val updatedWallet = wallet.copy(
                    balancePaise = wallet.balancePaise - amountPaise,
                    pendingWithdrawalPaise = wallet.pendingWithdrawalPaise + amountPaise
                )
                db.walletDao().updateWalletAccount(updatedWallet)

                val reqId = "wdr_${UUID.randomUUID().toString().take(8)}"
                val request = WithdrawalRequestEntity(
                    id = reqId,
                    userId = uid,
                    amountPaise = amountPaise,
                    upiId = upiId.trim(),
                    status = "PENDING_REVIEW",
                    createdAtMs = System.currentTimeMillis()
                )
                db.withdrawalDao().insertWithdrawal(request)

                val tx = WalletTransactionEntity(
                    id = "tx_${UUID.randomUUID().toString().take(8)}",
                    userId = uid,
                    amountPaise = -amountPaise,
                    type = "WITHDRAWAL",
                    status = "PENDING",
                    title = "UPI Withdrawal Request",
                    description = "To UPI: ${upiId.trim()} (Pending Admin Review)",
                    referenceId = reqId,
                    timestampMs = System.currentTimeMillis()
                )
                db.walletDao().insertTransaction(tx)
            }
            Result.success("Withdrawal request submitted for review")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==============================================================
    // 4. TOURNAMENTS
    // ==============================================================

    fun observeTournaments(): Flow<List<TournamentEntity>> = db.tournamentDao().getAllTournamentsFlow()

    fun observeTournament(tournamentId: String): Flow<TournamentEntity?> =
        db.tournamentDao().getTournamentFlow(tournamentId)

    fun observeUserRegistrations(): Flow<List<RegistrationEntity>> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.registrationDao().getUserRegistrationsFlow(uid)
        } else {
            flowOf(emptyList())
        }
    }

    fun observeTournamentRegistrations(tournamentId: String): Flow<List<RegistrationEntity>> =
        db.registrationDao().getTournamentRegistrationsFlow(tournamentId)

    suspend fun registerForTournament(tournamentId: String, inGameName: String, freeFireUid: String): Result<String> {
        val uid = getCurrentUid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        return try {
            db.withTransaction {
                val tournament = db.tournamentDao().getTournament(tournamentId)
                    ?: throw IllegalArgumentException("Tournament not found")

                if (tournament.status != "REGISTRATION_OPEN") {
                    throw IllegalStateException("Tournament registration is closed (Status: ${tournament.status})")
                }

                if (tournament.filledSlots >= tournament.totalSlots) {
                    throw IllegalStateException("Tournament slots are completely full")
                }

                val existing = db.registrationDao().getRegistration(tournamentId, uid)
                if (existing != null) {
                    throw IllegalStateException("You are already registered for this tournament")
                }

                val wallet = db.walletDao().getWalletAccount(uid)
                    ?: throw IllegalStateException("Wallet not found")

                if (tournament.entryFeePaise > 0) {
                    if (wallet.balancePaise < tournament.entryFeePaise) {
                        throw IllegalStateException("Insufficient balance. Entry fee: ₹${tournament.entryFeePaise / 100}, Balance: ₹${wallet.balancePaise / 100}")
                    }
                    // Deduct entry fee
                    db.walletDao().updateWalletAccount(
                        wallet.copy(balancePaise = wallet.balancePaise - tournament.entryFeePaise)
                    )
                    // Record debit transaction
                    db.walletDao().insertTransaction(
                        WalletTransactionEntity(
                            id = "tx_${UUID.randomUUID().toString().take(8)}",
                            userId = uid,
                            amountPaise = -tournament.entryFeePaise,
                            type = "ENTRY_FEE",
                            status = "SUCCESS",
                            title = "Tournament Entry Fee",
                            description = "Joined ${tournament.title}",
                            referenceId = tournamentId,
                            timestampMs = System.currentTimeMillis()
                        )
                    )
                }

                val newSlotNumber = tournament.filledSlots + 1
                val reg = RegistrationEntity(
                    id = "${tournamentId}_$uid",
                    tournamentId = tournamentId,
                    userId = uid,
                    inGameName = inGameName.ifBlank { "Player_${uid.takeLast(4)}" },
                    freeFireUid = freeFireUid.ifBlank { "000000000" },
                    slotNumber = newSlotNumber,
                    registeredAtMs = System.currentTimeMillis(),
                    checkedIn = true
                )
                db.registrationDao().insertRegistration(reg)

                val newFilled = tournament.filledSlots + 1
                val newStatus = if (newFilled >= tournament.totalSlots) "FULL" else "REGISTRATION_OPEN"
                db.tournamentDao().updateTournament(
                    tournament.copy(filledSlots = newFilled, status = newStatus)
                )

                // Update user stats
                val user = db.userDao().getUser(uid)
                if (user != null) {
                    db.userDao().updateUser(user.copy(matchesPlayed = user.matchesPlayed + 1))
                }
            }
            Result.success("Successfully registered! Check Room details 15m before match start.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==============================================================
    // 5. WATCH & EARN REWARDS
    // ==============================================================

    fun observeRewardHistory(): Flow<List<RewardEventEntity>> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.rewardDao().getRewardsFlow(uid)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun claimAdReward(): Result<Long> {
        val uid = getCurrentUid() ?: return Result.failure(IllegalStateException("User not authenticated"))
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 24 * 3600 * 1000L

        return try {
            val countToday = db.rewardDao().getRewardCountSince(uid, oneDayAgo)
            if (countToday >= DAILY_AD_LIMIT) {
                return Result.failure(IllegalStateException("Daily ad limit reached ($DAILY_AD_LIMIT/$DAILY_AD_LIMIT). Resets in 24 hours."))
            }

            val latest = db.rewardDao().getLatestReward(uid)
            if (latest != null && latest.cooldownUntilMs > now) {
                val remainingSec = (latest.cooldownUntilMs - now) / 1000
                return Result.failure(IllegalStateException("Cooldown active: please wait $remainingSec seconds before next ad."))
            }

            // Secure random reward between ₹0.50 (50 paise) and ₹2.00 (200 paise)
            val rewardPaise = (Random.nextInt(50, 201) / 10) * 10L

            db.withTransaction {
                val event = RewardEventEntity(
                    id = "rwd_${UUID.randomUUID().toString().take(8)}",
                    userId = uid,
                    rewardPaise = rewardPaise,
                    timestampMs = now,
                    cooldownUntilMs = now + AD_COOLDOWN_MS,
                    adUnitId = "ca-app-pub-arenax/rewarded-99182"
                )
                db.rewardDao().insertReward(event)

                val wallet = db.walletDao().getWalletAccount(uid)
                    ?: WalletAccountEntity(uid)
                db.walletDao().updateWalletAccount(
                    wallet.copy(
                        balancePaise = wallet.balancePaise + rewardPaise,
                        bonusPoints = wallet.bonusPoints + (rewardPaise / 10)
                    )
                )

                db.walletDao().insertTransaction(
                    WalletTransactionEntity(
                        id = "tx_${UUID.randomUUID().toString().take(8)}",
                        userId = uid,
                        amountPaise = rewardPaise,
                        type = "AD_REWARD",
                        status = "SUCCESS",
                        title = "Watch & Earn Reward",
                        description = "Earned ₹${rewardPaise / 100.0} from verified rewarded ad video",
                        referenceId = event.id,
                        timestampMs = now
                    )
                )
            }
            Result.success(rewardPaise)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==============================================================
    // 6. REFERRALS & LEADERBOARDS
    // ==============================================================

    fun observeUserReferrals(): Flow<List<ReferralEntity>> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid
        if (uid != null) {
            db.referralDao().getReferralsFlow(uid)
        } else {
            flowOf(emptyList())
        }
    }

    fun observeActivePlayersLeaderboard(): Flow<List<UserEntity>> = db.userDao().getActivePlayersLeaderboard()

    fun observeTopReferrersLeaderboard(): Flow<List<Pair<UserEntity, Int>>> =
        combine(db.userDao().getAllUsersFlow(), db.referralDao().getAllReferralsFlow()) { users, referrals ->
            val qualifiedRefs = referrals.filter { it.status == "QUALIFIED_PAID" }
            val refCountByUser = qualifiedRefs.groupBy { it.referrerUserId }.mapValues { it.value.size }
            users.mapNotNull { user ->
                val count = refCountByUser[user.id] ?: 0
                if (count > 0) user to count else null
            }
                .sortedByDescending { it.second }
                .take(10)
        }

    fun observeTopDepositorsLeaderboard(): Flow<List<Pair<UserEntity, Long>>> =
        combine(db.userDao().getAllUsersFlow(), db.walletDao().getAllTransactionsFlow()) { users, transactions ->
            val depositsByUser = transactions
                .filter { it.type == "DEPOSIT" && it.status == "SUCCESS" }
                .groupBy { it.userId }
                .mapValues { entry -> entry.value.sumOf { it.amountPaise } }
            val refundsByUser = transactions
                .filter { (it.type == "REFUND" || it.type == "CHARGEBACK") && it.status == "SUCCESS" }
                .groupBy { it.userId }
                .mapValues { entry -> entry.value.sumOf { it.amountPaise } }

            users.mapNotNull { user ->
                val gross = depositsByUser[user.id] ?: 0L
                val refunds = refundsByUser[user.id] ?: 0L
                val net = gross - refunds
                if (net > 0L) user to net else null
            }
                .sortedByDescending { it.second }
                .take(10)
        }

    // ==============================================================
    // 7. ANNOUNCEMENTS
    // ==============================================================

    fun observeAnnouncements(): Flow<List<AnnouncementEntity>> = db.announcementDao().getAnnouncementsFlow()

    // ==============================================================
    // 8. ADMIN AUTHENTICATION & PRIVILEGED ACTIONS
    // ==============================================================

    suspend fun adminLogin(usernameInput: String, passwordInput: String): Result<String> {
        val now = System.currentTimeMillis()
        var cred = db.adminDao().getAdminCredentials()
        if (cred == null) {
            cred = AdminCredentialEntity(
                id = "admin_primary",
                usernameNormalized = AdminAuthUtil.ADMIN_USERNAME_NORMALIZED,
                displayName = AdminAuthUtil.ADMIN_DISPLAY_NAME,
                salt = AdminAuthUtil.ADMIN_SALT_HEX,
                passwordHash = AdminAuthUtil.ADMIN_PASSWORD_HASH_HEX,
                failedAttempts = 0,
                lockoutUntilMs = 0L
            )
            db.adminDao().insertAdminCredentials(cred)
        }

        // Check if temporarily locked out
        if (cred.lockoutUntilMs > now) {
            val remainingSec = (cred.lockoutUntilMs - now) / 1000
            return Result.failure(SecurityException("Administrator account locked due to excessive failed attempts. Please retry in ${remainingSec}s."))
        }

        val normalizedInput = AdminAuthUtil.normalizeUsername(usernameInput)
        val isUsernameValid = normalizedInput == cred.usernameNormalized
        val isPasswordValid = AdminAuthUtil.verifyPassword(passwordInput, cred.salt, cred.passwordHash)

        if (!isUsernameValid || !isPasswordValid) {
            val updatedFailed = cred.failedAttempts + 1
            val lockoutUntil = if (updatedFailed >= MAX_FAILED_ATTEMPTS) now + LOCKOUT_DURATION_MS else 0L
            db.adminDao().updateAdminCredentials(
                cred.copy(failedAttempts = updatedFailed, lockoutUntilMs = lockoutUntil)
            )
            return if (lockoutUntil > 0L) {
                Result.failure(SecurityException("Too many failed attempts. Temporary 5-minute security lockout activated."))
            } else {
                val remaining = MAX_FAILED_ATTEMPTS - updatedFailed
                Result.failure(SecurityException("Invalid administrator credentials. $remaining attempts remaining before lockout."))
            }
        }

        // Authentication Successful: Reset counters and issue secure session token
        db.adminDao().updateAdminCredentials(
            cred.copy(failedAttempts = 0, lockoutUntilMs = 0L, lastLoginMs = now)
        )
        val sessionToken = "adm_${UUID.randomUUID()}"
        val session = AdminSessionEntity(
            token = sessionToken,
            username = cred.displayName,
            createdAtMs = now,
            expiresAtMs = now + ADMIN_SESSION_DURATION_MS,
            isActive = true
        )
        db.adminDao().insertSession(session)
        return Result.success(sessionToken)
    }

    suspend fun adminLogout(sessionToken: String) {
        db.adminDao().invalidateSession(sessionToken)
    }

    suspend fun validateAdminSession(sessionToken: String?): Boolean {
        if (sessionToken.isNullOrBlank()) return false
        val session = db.adminDao().getActiveSession(sessionToken) ?: return false
        val now = System.currentTimeMillis()
        if (now > session.expiresAtMs || !session.isActive) {
            db.adminDao().invalidateSession(sessionToken)
            return false
        }
        return true
    }

    suspend fun getAdminLockoutRemainingSec(): Long {
        val cred = db.adminDao().getAdminCredentials() ?: return 0L
        val now = System.currentTimeMillis()
        return if (cred.lockoutUntilMs > now) (cred.lockoutUntilMs - now) / 1000 else 0L
    }

    private suspend fun requireAdminSession(sessionToken: String) {
        if (!validateAdminSession(sessionToken)) {
            throw SecurityException("Access Denied: Unauthenticated or expired administrator session.")
        }
    }

    fun observeAllWithdrawals(): Flow<List<WithdrawalRequestEntity>> = db.withdrawalDao().getAllWithdrawalsFlow()

    fun observePrizeConfigs(): Flow<List<LeaderboardPrizeConfigEntity>> = db.leaderboardPrizeDao().getAllPrizeConfigsFlow()

    suspend fun adminCreateTournament(sessionToken: String, tournament: TournamentEntity) {
        requireAdminSession(sessionToken)
        db.tournamentDao().insertTournament(tournament)
    }

    suspend fun adminUpdateTournamentStatus(
        sessionToken: String,
        tournamentId: String,
        status: String,
        roomId: String?,
        roomPass: String?
    ) {
        requireAdminSession(sessionToken)
        val t = db.tournamentDao().getTournament(tournamentId) ?: return
        db.tournamentDao().updateTournament(
            t.copy(
                status = status,
                roomId = roomId ?: t.roomId,
                roomPassword = roomPass ?: t.roomPassword
            )
        )
    }

    suspend fun adminFinalizeScores(
        sessionToken: String,
        tournamentId: String,
        rank1UserId: String,
        rank2UserId: String?,
        rank3UserId: String?
    ) {
        requireAdminSession(sessionToken)
        db.withTransaction {
            val tournament = db.tournamentDao().getTournament(tournamentId)
                ?: throw IllegalArgumentException("Tournament not found")
            db.tournamentDao().updateTournament(tournament.copy(status = "COMPLETED"))

            if (tournament.firstPrizePaise > 0) {
                creditPrize(rank1UserId, tournament.firstPrizePaise, tournament, 1)
            }
            if (rank2UserId != null && tournament.secondPrizePaise > 0) {
                creditPrize(rank2UserId, tournament.secondPrizePaise, tournament, 2)
            }
            if (rank3UserId != null && tournament.thirdPrizePaise > 0) {
                creditPrize(rank3UserId, tournament.thirdPrizePaise, tournament, 3)
            }
        }
    }

    private suspend fun creditPrize(userId: String, prizePaise: Long, tournament: TournamentEntity, rank: Int) {
        val wallet = db.walletDao().getWalletAccount(userId) ?: WalletAccountEntity(userId)
        db.walletDao().updateWalletAccount(
            wallet.copy(
                balancePaise = wallet.balancePaise + prizePaise,
                totalWonPaise = wallet.totalWonPaise + prizePaise
            )
        )
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = "tx_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                amountPaise = prizePaise,
                type = "PRIZE_PAYOUT",
                status = "SUCCESS",
                title = "Rank #$rank Prize: ${tournament.title}",
                description = "Earned ₹${prizePaise / 100} for Rank #$rank finish in ${tournament.title}",
                referenceId = tournament.id,
                timestampMs = System.currentTimeMillis()
            )
        )
        val user = db.userDao().getUser(userId)
        if (user != null) {
            val pointsBonus = if (rank == 1) 300 else if (rank == 2) 180 else 100
            db.userDao().updateUser(
                user.copy(
                    matchesWon = if (rank == 1) user.matchesWon + 1 else user.matchesWon,
                    points = user.points + pointsBonus
                )
            )
        }
    }

    suspend fun adminReviewWithdrawal(sessionToken: String, requestId: String, approve: Boolean, note: String) {
        requireAdminSession(sessionToken)
        db.withTransaction {
            val req = db.withdrawalDao().getWithdrawal(requestId) ?: return@withTransaction
            if (req.status != "PENDING_REVIEW") return@withTransaction

            val now = System.currentTimeMillis()
            val wallet = db.walletDao().getWalletAccount(req.userId) ?: return@withTransaction

            if (approve) {
                db.withdrawalDao().updateWithdrawal(
                    req.copy(
                        status = "APPROVED_PAID",
                        processedAtMs = now,
                        adminNote = note.ifBlank { "Payout initiated via UPI IMPS" }
                    )
                )
                db.walletDao().updateWalletAccount(
                    wallet.copy(
                        pendingWithdrawalPaise = (wallet.pendingWithdrawalPaise - req.amountPaise).coerceAtLeast(0L),
                        totalWithdrawnPaise = wallet.totalWithdrawnPaise + req.amountPaise
                    )
                )
            } else {
                db.withdrawalDao().updateWithdrawal(
                    req.copy(
                        status = "REJECTED",
                        processedAtMs = now,
                        adminNote = note.ifBlank { "Rejected by admin: Invalid UPI ID / verification failed" }
                    )
                )
                db.walletDao().updateWalletAccount(
                    wallet.copy(
                        balancePaise = wallet.balancePaise + req.amountPaise,
                        pendingWithdrawalPaise = (wallet.pendingWithdrawalPaise - req.amountPaise).coerceAtLeast(0L)
                    )
                )
                db.walletDao().insertTransaction(
                    WalletTransactionEntity(
                        id = "tx_${UUID.randomUUID().toString().take(8)}",
                        userId = req.userId,
                        amountPaise = req.amountPaise,
                        type = "REFUND",
                        status = "SUCCESS",
                        title = "Withdrawal Refunded",
                        description = "Rejected withdrawal of ₹${req.amountPaise / 100} restored to balance. Reason: ${note.ifBlank { "Verification failed" }}",
                        referenceId = req.id,
                        timestampMs = now
                    )
                )
            }
        }
    }

    suspend fun adminUpdatePrizeConfig(sessionToken: String, config: LeaderboardPrizeConfigEntity) {
        requireAdminSession(sessionToken)
        db.leaderboardPrizeDao().insertPrizeConfig(config)
    }

    fun observeReferralSummary(): Flow<ReferralDashboardSummary?> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid ?: return@flatMapLatest flowOf(null)
        combine(
            db.userDao().getUserFlow(uid),
            db.referralDao().getReferralsFlow(uid),
            db.referralDao().getRewardsFlow(uid),
            db.referralDao().getReferralConfigFlow()
        ) { userEntity, referrals, rewards, config ->
            if (userEntity == null) return@combine null

            val refId = userEntity.referralCode
            val domain = FirebaseConfigManager.AUTH_DOMAIN
            val link = "https://$domain/?ref=$refId"

            val totalRegistered = referrals.size
            val qualified = referrals.count { it.status == "QUALIFIED_PAID" }
            val awaiting = referrals.count { it.status != "QUALIFIED_PAID" }
            val paidRewards = rewards.filter { it.rewardStatus == "PAID" }
            val rewardsCount = paidRewards.size
            val totalEarnings = paidRewards.sumOf { it.rewardAmountPaise }
            val rewardPerUser = config?.rewardAmountPaise ?: 500L

            ReferralDashboardSummary(
                referralId = refId,
                referralLink = link,
                totalRegisteredReferrals = totalRegistered,
                referralsAwaitingDeposit = awaiting,
                qualifiedReferrals = qualified,
                rewardsPaidCount = rewardsCount,
                totalEarningsPaise = totalEarnings,
                rewardPerEligibleUserPaise = rewardPerUser
            )
        }
    }

    fun observeReferralBonusTransactions(): Flow<List<WalletTransactionEntity>> = _firebaseUser.flatMapLatest { user ->
        val uid = user?.uid ?: return@flatMapLatest flowOf(emptyList())
        db.walletDao().getReferralBonusTransactionsFlow(uid)
    }

    fun observePendingDeposits(): Flow<List<DepositRequestEntity>> = db.depositDao().getPendingDepositsFlow()
    fun observeAllDeposits(): Flow<List<DepositRequestEntity>> = db.depositDao().getAllDepositsFlow()
    fun observeReferralRewards(): Flow<List<ReferralRewardEntity>> = db.referralDao().getAllRewardsFlow()
    fun observeReferralConfig(): Flow<ReferralConfigEntity> = db.referralDao().getReferralConfigFlow().map { it ?: ReferralConfigEntity() }
    fun observeSuspiciousReferrals(): Flow<List<SuspiciousReferralEntity>> = db.referralDao().getSuspiciousReferralsFlow()
}

data class ReferralDashboardSummary(
    val referralId: String,
    val referralLink: String,
    val totalRegisteredReferrals: Int,
    val referralsAwaitingDeposit: Int,
    val qualifiedReferrals: Int,
    val rewardsPaidCount: Int,
    val totalEarningsPaise: Long,
    val rewardPerEligibleUserPaise: Long = 500L
)
