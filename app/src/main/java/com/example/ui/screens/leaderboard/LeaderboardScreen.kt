package com.example.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeaderboardPrizeConfigEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyFormatter

enum class LeaderboardTab {
    ACTIVE_PLAYERS,
    TOP_REFERRERS,
    TOP_DEPOSITORS
}

@Composable
fun LeaderboardScreen(
    activePlayers: List<UserEntity>,
    topReferrers: List<Pair<UserEntity, Int>>,
    topDepositors: List<Pair<UserEntity, Long>>,
    prizeConfigs: List<LeaderboardPrizeConfigEntity>
) {
    var selectedTab by remember { mutableStateOf(LeaderboardTab.ACTIVE_PLAYERS) }
    var selectedPeriod by remember { mutableStateOf("WEEKLY") }
    val periods = listOf("DAILY", "WEEKLY", "MONTHLY", "ALL_TIME")

    // Match prize config for selected tab and period
    val activeConfig = prizeConfigs.find {
        it.leaderboardType == selectedTab.name && it.period == selectedPeriod
    } ?: prizeConfigs.find { it.leaderboardType == selectedTab.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Selector Bar
        Surface(color = DarkSurface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LeaderboardTabButton(
                        title = "Active",
                        icon = Icons.Default.Whatshot,
                        isSelected = selectedTab == LeaderboardTab.ACTIVE_PLAYERS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = LeaderboardTab.ACTIVE_PLAYERS }
                    )
                    LeaderboardTabButton(
                        title = "Referrers",
                        icon = Icons.Default.Groups,
                        isSelected = selectedTab == LeaderboardTab.TOP_REFERRERS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = LeaderboardTab.TOP_REFERRERS }
                    )
                    LeaderboardTabButton(
                        title = "Depositors",
                        icon = Icons.Default.Payments,
                        isSelected = selectedTab == LeaderboardTab.TOP_DEPOSITORS,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = LeaderboardTab.TOP_DEPOSITORS }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Period Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEach { period ->
                        val isSelected = selectedPeriod == period
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ElectricViolet else DarkSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPeriod = period }
                        ) {
                            Text(
                                text = period.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Prize Pool Announcement Box
            activeConfig?.let { config ->
                item {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EsportsGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = EsportsGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🏆 $selectedPeriod PRIZE POOL REWARDS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EsportsGold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PrizeTag(rank = "1st", amount = CurrencyFormatter.formatPaise(config.rank1PrizePaise), color = EsportsGold)
                                PrizeTag(rank = "2nd", amount = CurrencyFormatter.formatPaise(config.rank2PrizePaise), color = Color.LightGray)
                                PrizeTag(rank = "3rd", amount = CurrencyFormatter.formatPaise(config.rank3PrizePaise), color = Color(0xFFCD7F32))
                                PrizeTag(rank = "Top 10", amount = CurrencyFormatter.formatPaise(config.top10PrizePaise), color = CyberCyan)
                            }
                        }
                    }
                }
            }

            // Render Rows based on selected tab
            when (selectedTab) {
                LeaderboardTab.ACTIVE_PLAYERS -> {
                    if (activePlayers.isEmpty()) {
                        item {
                            LeaderboardEmptyState(
                                icon = Icons.Default.SportsEsports,
                                title = "No Qualifying Players Yet",
                                description = "Leaderboard rankings will appear here once players complete and verify tournament matches. Only verified matches qualify.",
                                qualifyingCountText = "0 qualifying players (max 100)"
                            )
                        }
                    } else {
                        itemsIndexed(activePlayers) { index, player ->
                            LeaderboardUserRow(
                                rank = index + 1,
                                name = player.displayName,
                                subtext = "${player.matchesPlayed} Matches • ${player.matchesWon} Wins",
                                score = "${player.points} PTS",
                                scoreColor = CyberCyan
                            )
                        }
                    }
                }
                LeaderboardTab.TOP_REFERRERS -> {
                    if (topReferrers.isEmpty()) {
                        item {
                            LeaderboardEmptyState(
                                icon = Icons.Default.Groups,
                                title = "No Qualified Referrals Yet",
                                description = "Invite fellow competitors with your referral code. Players with qualified, verified referrals will rank here.",
                                qualifyingCountText = "0 qualifying referrers (max 10)"
                            )
                        }
                    } else {
                        itemsIndexed(topReferrers) { index, pair ->
                            LeaderboardUserRow(
                                rank = index + 1,
                                name = pair.first.displayName,
                                subtext = "Verified Code: ${pair.first.referralCode}",
                                score = "${pair.second} Referrals",
                                scoreColor = EmeraldGreen
                            )
                        }
                    }
                }
                LeaderboardTab.TOP_DEPOSITORS -> {
                    if (topDepositors.isEmpty()) {
                        item {
                            LeaderboardEmptyState(
                                icon = Icons.Default.Payments,
                                title = "No Qualifying Depositors Yet",
                                description = "Rankings reflect verified net deposits, excluding pending, failed, refunded, and reversed transactions.",
                                qualifyingCountText = "0 qualifying depositors (max 10)"
                            )
                        }
                    } else {
                        itemsIndexed(topDepositors) { index, pair ->
                            LeaderboardUserRow(
                                rank = index + 1,
                                name = pair.first.displayName,
                                subtext = "Verified Net Contender",
                                score = CurrencyFormatter.formatPaise(pair.second),
                                scoreColor = EsportsGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    qualifyingCountText: String
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("leaderboard_empty_state")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = qualifyingCountText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LeaderboardTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ElectricViolet else DarkSurfaceElevated,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

@Composable
private fun PrizeTag(rank: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = rank, fontSize = 11.sp, color = TextSecondary)
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun LeaderboardUserRow(
    rank: Int,
    name: String,
    subtext: String,
    score: String,
    scoreColor: Color
) {
    val rankBadgeBg = when (rank) {
        1 -> EsportsGold
        2 -> Color.LightGray
        3 -> Color(0xFFCD7F32)
        else -> DarkSurfaceElevated
    }
    val rankTextColor = if (rank in 1..3) Color.Black else TextSecondary

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rank == 1) EsportsGold.copy(alpha = 0.5f) else BorderColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(rankBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = rankTextColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtext,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = score,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor
            )
        }
    }
}
