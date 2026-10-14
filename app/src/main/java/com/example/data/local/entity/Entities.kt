package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val inGameName: String,
    val freeFireUid: String,
    val email: String,
    val referralCode: String,
    val referredByCode: String?,
    val avatarUrl: String,
    val joinDateMs: Long,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val totalKills: Int = 0,
    val points: Int = 0,
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val suspensionReason: String? = null
)

@Entity(tableName = "wallet_accounts")
data class WalletAccountEntity(
    @PrimaryKey val userId: String,
    val balancePaise: Long = 0L,
    val pendingWithdrawalPaise: Long = 0L,
    val totalDepositedPaise: Long = 0L,
    val totalWithdrawnPaise: Long = 0L,
    val totalWonPaise: Long = 0L,
    val bonusPoints: Long = 0L
)

@Entity(tableName = "wallet_ledger")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountPaise: Long, // positive for credit, negative for debit
    val type: String, // DEPOSIT, WITHDRAWAL, ENTRY_FEE, PRIZE_PAYOUT, REFERRAL_BONUS, AD_REWARD, REFUND
    val status: String, // SUCCESS, PENDING, REJECTED, REFUNDED
    val title: String,
    val description: String,
    val referenceId: String? = null,
    val timestampMs: Long
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val gameTitle: String, // Free Fire MAX, BGMI, etc.
    val format: String, // Solo, Duo, Squad
    val map: String, // Bermuda, Purgatory, Kalahari, Alpine
    val mode: String, // Battle Royale, Clash Squad
    val status: String, // REGISTRATION_OPEN, FULL, CHECK_IN, ROOM_RELEASED, LIVE, UNDER_REVIEW, COMPLETED, CANCELLED
    val entryFeePaise: Long,
    val prizePoolPaise: Long,
    val firstPrizePaise: Long,
    val secondPrizePaise: Long,
    val thirdPrizePaise: Long,
    val perKillPaise: Long,
    val totalSlots: Int,
    val filledSlots: Int,
    val startTimeMs: Long,
    val checkInWindowMinutes: Int = 15,
    val roomId: String? = null,
    val roomPassword: String? = null,
    val rules: String,
    val disputeStatus: String? = null,
    val bannerUrl: String? = null
)

@Entity(tableName = "registrations")
data class RegistrationEntity(
    @PrimaryKey val id: String, // "${tournamentId}_${userId}"
    val tournamentId: String,
    val userId: String,
    val inGameName: String,
    val freeFireUid: String,
    val teamName: String? = null,
    val slotNumber: Int,
    val registeredAtMs: Long,
    val checkedIn: Boolean = false,
    val kills: Int = 0,
    val placement: Int = 0,
    val points: Int = 0,
    val prizeWonPaise: Long = 0L,
    val proofScreenshotUri: String? = null,
    val resultVerified: Boolean = false
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amountPaise: Long,
    val upiId: String,
    val status: String, // PENDING_REVIEW, APPROVED_PAID, REJECTED
    val createdAtMs: Long,
    val processedAtMs: Long? = null,
    val adminNote: String? = null
)

@Entity(tableName = "reward_events")
data class RewardEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val rewardPaise: Long,
    val timestampMs: Long,
    val cooldownUntilMs: Long,
    val adUnitId: String
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String,
    val referrerUserId: String,
    val referredUserId: String,
    val referredUserName: String,
    val status: String, // PENDING_MATCH, QUALIFIED_PAID
    val bonusPaise: Long,
    val createdAtMs: Long
)

@Entity(tableName = "leaderboard_prize_configs")
data class LeaderboardPrizeConfigEntity(
    @PrimaryKey val id: String, // "${leaderboardType}_${period}"
    val leaderboardType: String, // ACTIVE_PLAYERS, TOP_REFERRERS, TOP_DEPOSITORS
    val period: String, // DAILY, WEEKLY, MONTHLY, ALL_TIME
    val rank1PrizePaise: Long,
    val rank2PrizePaise: Long,
    val rank3PrizePaise: Long,
    val top10PrizePaise: Long,
    val isFrozen: Boolean = false,
    val lastPaidPeriod: String? = null,
    val totalBudgetPaise: Long
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestampMs: Long,
    val priority: String = "NORMAL"
)

@Entity(tableName = "admin_credentials")
data class AdminCredentialEntity(
    @PrimaryKey val id: String = "admin_primary",
    val usernameNormalized: String,
    val displayName: String,
    val salt: String, // Hex-encoded cryptographic salt
    val passwordHash: String, // Hex-encoded PBKDF2 HMAC-SHA256 hash
    val failedAttempts: Int = 0,
    val lockoutUntilMs: Long = 0L,
    val lastLoginMs: Long? = null
)

@Entity(tableName = "admin_sessions")
data class AdminSessionEntity(
    @PrimaryKey val token: String,
    val username: String,
    val createdAtMs: Long,
    val expiresAtMs: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "referral_rewards")
data class ReferralRewardEntity(
    @PrimaryKey val id: String, // "ref_reward_${referredUid}"
    val referralId: String,
    val referrerUid: String,
    val referredUid: String,
    val firstQualifyingDepositId: String? = null,
    val rewardAmountPaise: Long = 500L, // 500 paise = ₹5.00
    val rewardStatus: String = "PENDING", // PENDING, PAID, REJECTED
    val rewardedAtMs: Long? = null,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "referral_configs")
data class ReferralConfigEntity(
    @PrimaryKey val id: String = "default_config",
    val isEnabled: Boolean = true,
    val rewardAmountPaise: Long = 500L, // 500 paise = ₹5.00
    val minQualifyingDepositPaise: Long = 1000L, // 1000 paise = ₹10.00
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "deposit_requests")
data class DepositRequestEntity(
    @PrimaryKey val id: String, // "dep_${UUID}"
    val userId: String,
    val userDisplayName: String,
    val amountPaise: Long,
    val utrOrOrderId: String,
    val status: String, // PENDING_VERIFICATION, VERIFIED_SUCCESS, REJECTED
    val createdAtMs: Long,
    val verifiedAtMs: Long? = null,
    val adminNote: String? = null
)

@Entity(tableName = "suspicious_referrals")
data class SuspiciousReferralEntity(
    @PrimaryKey val id: String,
    val attemptingUserId: String,
    val attemptedCode: String,
    val reason: String, // SELF_REFERRAL_ATTEMPT, DUPLICATE_CLAIM_ATTEMPT, UNKNOWN_CODE
    val timestampMs: Long
)

