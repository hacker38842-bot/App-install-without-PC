package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AdminDao
import com.example.data.local.dao.AnnouncementDao
import com.example.data.local.dao.DepositDao
import com.example.data.local.dao.LeaderboardPrizeDao
import com.example.data.local.dao.ReferralDao
import com.example.data.local.dao.RegistrationDao
import com.example.data.local.dao.RewardDao
import com.example.data.local.dao.TournamentDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.dao.WithdrawalDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WalletAccountEntity::class,
        WalletTransactionEntity::class,
        TournamentEntity::class,
        RegistrationEntity::class,
        WithdrawalRequestEntity::class,
        RewardEventEntity::class,
        ReferralEntity::class,
        ReferralRewardEntity::class,
        ReferralConfigEntity::class,
        DepositRequestEntity::class,
        SuspiciousReferralEntity::class,
        LeaderboardPrizeConfigEntity::class,
        AnnouncementEntity::class,
        AdminCredentialEntity::class,
        AdminSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun registrationDao(): RegistrationDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun rewardDao(): RewardDao
    abstract fun referralDao(): ReferralDao
    abstract fun leaderboardPrizeDao(): LeaderboardPrizeDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun adminDao(): AdminDao
    abstract fun depositDao(): DepositDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arena_x_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(db: AppDatabase) {
            val now = System.currentTimeMillis()

            // 1. Initialize Player Account with zero state (no fabricated matches or points)
            val currentUser = UserEntity(
                id = "usr_player_me",
                displayName = "Contender",
                inGameName = "",
                freeFireUid = "",
                email = "player@arenax.gg",
                referralCode = "ARENA9X",
                referredByCode = null,
                avatarUrl = "",
                joinDateMs = now,
                matchesPlayed = 0,
                matchesWon = 0,
                totalKills = 0,
                points = 0,
                isAdmin = false
            )
            db.userDao().insertUser(currentUser)

            // 2. Initialize Player Wallet with zero balance (no fabricated money)
            val wallet = WalletAccountEntity(
                userId = "usr_player_me",
                balancePaise = 0L,
                pendingWithdrawalPaise = 0L,
                totalDepositedPaise = 0L,
                totalWithdrawnPaise = 0L,
                totalWonPaise = 0L,
                bonusPoints = 0L
            )
            db.walletDao().insertWalletAccount(wallet)

            // 3. Initialize Administrator Account with PBKDF2 Salt and Hash (never plaintext password)
            val adminCred = AdminCredentialEntity(
                id = "admin_primary",
                usernameNormalized = AdminAuthUtil.ADMIN_USERNAME_NORMALIZED,
                displayName = AdminAuthUtil.ADMIN_DISPLAY_NAME,
                salt = AdminAuthUtil.ADMIN_SALT_HEX,
                passwordHash = AdminAuthUtil.ADMIN_PASSWORD_HASH_HEX,
                failedAttempts = 0,
                lockoutUntilMs = 0L
            )
            db.adminDao().insertAdminCredentials(adminCred)

            // 4. Welcome Announcement
            db.announcementDao().insertAnnouncement(
                AnnouncementEntity(
                    id = "ann_1",
                    title = "⚡ Welcome to ARENA X",
                    message = "Official competitive esports tournament platform. Tournaments will appear here as soon as published by administrators.",
                    timestampMs = now,
                    priority = "NORMAL"
                )
            )

            // 5. Official Leaderboard Prize Configurations
            val prizeConfigs = listOf(
                LeaderboardPrizeConfigEntity(
                    id = "ACTIVE_PLAYERS_WEEKLY",
                    leaderboardType = "ACTIVE_PLAYERS",
                    period = "WEEKLY",
                    rank1PrizePaise = 250000L, // ₹2,500
                    rank2PrizePaise = 150000L, // ₹1,500
                    rank3PrizePaise = 100000L, // ₹1,000
                    top10PrizePaise = 25000L,  // ₹250
                    totalBudgetPaise = 675000L
                ),
                LeaderboardPrizeConfigEntity(
                    id = "TOP_REFERRERS_MONTHLY",
                    leaderboardType = "TOP_REFERRERS",
                    period = "MONTHLY",
                    rank1PrizePaise = 500000L, // ₹5,000
                    rank2PrizePaise = 300000L, // ₹3,000
                    rank3PrizePaise = 200000L, // ₹2,000
                    top10PrizePaise = 50000L,  // ₹500
                    totalBudgetPaise = 1350000L
                ),
                LeaderboardPrizeConfigEntity(
                    id = "TOP_DEPOSITORS_MONTHLY",
                    leaderboardType = "TOP_DEPOSITORS",
                    period = "MONTHLY",
                    rank1PrizePaise = 300000L, // ₹3,000
                    rank2PrizePaise = 200000L, // ₹2,000
                    rank3PrizePaise = 100000L, // ₹1,000
                    top10PrizePaise = 25000L,  // ₹250
                    totalBudgetPaise = 775000L
                )
            )
            prizeConfigs.forEach { db.leaderboardPrizeDao().insertPrizeConfig(it) }

            // 5. Initialize Referral Configuration (Enabled, ₹5.00 reward, ₹10.00 min deposit)
            val existingRefConfig = db.referralDao().getReferralConfig()
            if (existingRefConfig == null) {
                db.referralDao().insertReferralConfig(
                    ReferralConfigEntity(
                        id = "default_config",
                        isEnabled = true,
                        rewardAmountPaise = 500L, // ₹5.00
                        minQualifyingDepositPaise = 1000L, // ₹10.00
                        updatedAtMs = now
                    )
                )
            }
        }
    }
}
