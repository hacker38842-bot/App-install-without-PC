package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE matchesPlayed > 0 ORDER BY matchesPlayed DESC, matchesWon DESC, points DESC LIMIT 100")
    fun getActivePlayersLeaderboard(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_accounts WHERE userId = :userId")
    fun getWalletAccountFlow(userId: String): Flow<WalletAccountEntity?>

    @Query("SELECT * FROM wallet_accounts WHERE userId = :userId")
    suspend fun getWalletAccount(userId: String): WalletAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletAccount(account: WalletAccountEntity)

    @Update
    suspend fun updateWalletAccount(account: WalletAccountEntity)

    @Query("SELECT * FROM wallet_ledger WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getTransactionsFlow(userId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Query("SELECT * FROM wallet_ledger ORDER BY timestampMs DESC")
    fun getAllTransactionsFlow(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_ledger WHERE userId = :userId AND type = 'DEPOSIT' AND status = 'SUCCESS' ORDER BY timestampMs ASC")
    suspend fun getVerifiedDepositsForUser(userId: String): List<WalletTransactionEntity>

    @Query("SELECT * FROM wallet_ledger WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): WalletTransactionEntity?

    @Query("SELECT * FROM wallet_ledger WHERE userId = :userId AND type = 'REFERRAL_BONUS' ORDER BY timestampMs DESC")
    fun getReferralBonusTransactionsFlow(userId: String): Flow<List<WalletTransactionEntity>>
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY startTimeMs ASC")
    fun getAllTournamentsFlow(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    fun getTournamentFlow(tournamentId: String): Flow<TournamentEntity?>

    @Query("SELECT * FROM tournaments WHERE id = :tournamentId")
    suspend fun getTournament(tournamentId: String): TournamentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Update
    suspend fun updateTournament(tournament: TournamentEntity)

    @Query("DELETE FROM tournaments WHERE id = :id")
    suspend fun deleteTournament(id: String)
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId")
    fun getTournamentRegistrationsFlow(tournamentId: String): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE tournamentId = :tournamentId AND userId = :userId")
    suspend fun getRegistration(tournamentId: String, userId: String): RegistrationEntity?

    @Query("SELECT * FROM registrations WHERE userId = :userId")
    fun getUserRegistrationsFlow(userId: String): Flow<List<RegistrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: RegistrationEntity)

    @Update
    suspend fun updateRegistration(registration: RegistrationEntity)

    @Query("DELETE FROM registrations WHERE id = :id")
    suspend fun deleteRegistration(id: String)

    @Query("SELECT COUNT(*) FROM registrations WHERE tournamentId = :tournamentId")
    suspend fun getRegistrationCount(tournamentId: String): Int
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawal_requests WHERE userId = :userId ORDER BY createdAtMs DESC")
    fun getUserWithdrawalsFlow(userId: String): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests ORDER BY createdAtMs DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalRequestEntity>>

    @Query("SELECT * FROM withdrawal_requests WHERE id = :id LIMIT 1")
    suspend fun getWithdrawal(id: String): WithdrawalRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalRequestEntity)

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalRequestEntity)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_events WHERE userId = :userId ORDER BY timestampMs DESC")
    fun getRewardsFlow(userId: String): Flow<List<RewardEventEntity>>

    @Query("SELECT COUNT(*) FROM reward_events WHERE userId = :userId AND timestampMs >= :sinceMs")
    suspend fun getRewardCountSince(userId: String, sinceMs: Long): Int

    @Query("SELECT * FROM reward_events WHERE userId = :userId ORDER BY timestampMs DESC LIMIT 1")
    suspend fun getLatestReward(userId: String): RewardEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEventEntity)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerUserId = :referrerId ORDER BY createdAtMs DESC")
    fun getReferralsFlow(referrerId: String): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals")
    fun getAllReferralsFlow(): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE referredUserId = :referredUserId LIMIT 1")
    suspend fun getReferralByReferredUser(referredUserId: String): ReferralEntity?

    @Query("SELECT * FROM referrals WHERE referrerUserId = :referrerId")
    suspend fun getReferralsByReferrer(referrerId: String): List<ReferralEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity)

    @Update
    suspend fun updateReferral(referral: ReferralEntity)

    // Referral Rewards
    @Query("SELECT * FROM referral_rewards WHERE id = :id LIMIT 1")
    suspend fun getReward(id: String): ReferralRewardEntity?

    @Query("SELECT * FROM referral_rewards WHERE referredUid = :referredUid LIMIT 1")
    suspend fun getRewardByReferredUid(referredUid: String): ReferralRewardEntity?

    @Query("SELECT * FROM referral_rewards WHERE referrerUid = :referrerUid ORDER BY createdAtMs DESC")
    fun getRewardsFlow(referrerUid: String): Flow<List<ReferralRewardEntity>>

    @Query("SELECT * FROM referral_rewards ORDER BY createdAtMs DESC")
    fun getAllRewardsFlow(): Flow<List<ReferralRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: ReferralRewardEntity)

    @Update
    suspend fun updateReward(reward: ReferralRewardEntity)

    // Referral Config
    @Query("SELECT * FROM referral_configs WHERE id = 'default_config' LIMIT 1")
    suspend fun getReferralConfig(): ReferralConfigEntity?

    @Query("SELECT * FROM referral_configs WHERE id = 'default_config' LIMIT 1")
    fun getReferralConfigFlow(): Flow<ReferralConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralConfig(config: ReferralConfigEntity)

    // Suspicious Referrals
    @Query("SELECT * FROM suspicious_referrals ORDER BY timestampMs DESC")
    fun getSuspiciousReferralsFlow(): Flow<List<SuspiciousReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuspiciousReferral(suspicious: SuspiciousReferralEntity)
}

@Dao
interface LeaderboardPrizeDao {
    @Query("SELECT * FROM leaderboard_prize_configs")
    fun getAllPrizeConfigsFlow(): Flow<List<LeaderboardPrizeConfigEntity>>

    @Query("SELECT * FROM leaderboard_prize_configs WHERE leaderboardType = :type AND period = :period")
    suspend fun getPrizeConfig(type: String, period: String): LeaderboardPrizeConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrizeConfig(config: LeaderboardPrizeConfigEntity)

    @Update
    suspend fun updatePrizeConfig(config: LeaderboardPrizeConfigEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestampMs DESC")
    fun getAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM admin_credentials WHERE id = 'admin_primary' LIMIT 1")
    suspend fun getAdminCredentials(): com.example.data.local.entity.AdminCredentialEntity?

    @Query("SELECT * FROM admin_credentials WHERE id = 'admin_primary' LIMIT 1")
    fun getAdminCredentialsFlow(): Flow<com.example.data.local.entity.AdminCredentialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminCredentials(credentials: com.example.data.local.entity.AdminCredentialEntity)

    @Update
    suspend fun updateAdminCredentials(credentials: com.example.data.local.entity.AdminCredentialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.example.data.local.entity.AdminSessionEntity)

    @Query("SELECT * FROM admin_sessions WHERE token = :token AND isActive = 1 LIMIT 1")
    suspend fun getActiveSession(token: String): com.example.data.local.entity.AdminSessionEntity?

    @Query("UPDATE admin_sessions SET isActive = 0 WHERE token = :token")
    suspend fun invalidateSession(token: String)

    @Query("UPDATE admin_sessions SET isActive = 0")
    suspend fun invalidateAllSessions()
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposit_requests WHERE status = 'PENDING_VERIFICATION' ORDER BY createdAtMs DESC")
    fun getPendingDepositsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests ORDER BY createdAtMs DESC")
    fun getAllDepositsFlow(): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE userId = :userId ORDER BY createdAtMs DESC")
    fun getUserDepositsFlow(userId: String): Flow<List<DepositRequestEntity>>

    @Query("SELECT * FROM deposit_requests WHERE id = :id LIMIT 1")
    suspend fun getDeposit(id: String): DepositRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositRequestEntity)

    @Update
    suspend fun updateDeposit(deposit: DepositRequestEntity)
}

