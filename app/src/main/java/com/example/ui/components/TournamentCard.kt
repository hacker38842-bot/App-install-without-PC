package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TournamentEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun TournamentCard(
    tournament: TournamentEntity,
    isUserRegistered: Boolean,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit,
    onViewRoomClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUserRegistered) ElectricViolet else BorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("tournament_card_${tournament.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Header: Game & Format Badges + Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Game badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Text(
                            text = tournament.gameTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Format badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Text(
                            text = "${tournament.format.uppercase()} • ${tournament.map}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Status Tag
                val (statusText, statusBg, statusColor) = when (tournament.status) {
                    "LIVE" -> Triple("● LIVE NOW", CrimsonRed.copy(alpha = 0.2f), CrimsonRed)
                    "REGISTRATION_OPEN" -> Triple("JOIN OPEN", EmeraldGreen.copy(alpha = 0.2f), EmeraldGreen)
                    "FULL" -> Triple("FULL", Color.Gray.copy(alpha = 0.2f), Color.LightGray)
                    "COMPLETED" -> Triple("FINISHED", Color.DarkGray, Color.Gray)
                    else -> Triple(tournament.status.replace("_", " "), DarkSurfaceElevated, TextSecondary)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tournament Title
            Text(
                text = tournament.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time & Schedule Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = DateUtils.formatMatchTime(tournament.startTimeMs),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = DateUtils.formatCountdown(tournament.startTimeMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Matrix: Prize Pool, 1st Prize, Per Kill, Entry Fee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIZE POOL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatPaise(tournament.prizePoolPaise),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EsportsGold
                    )
                }

                Column {
                    Text(
                        text = "1ST PLACE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatPaise(tournament.firstPrizePaise),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (tournament.perKillPaise > 0) {
                    Column {
                        Text(
                            text = "PER KILL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatPaise(tournament.perKillPaise),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ENTRY FEE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    val feeText = if (tournament.entryFeePaise == 0L) "FREE" else CurrencyFormatter.formatPaise(tournament.entryFeePaise)
                    val feeColor = if (tournament.entryFeePaise == 0L) EmeraldGreen else Color.White
                    Text(
                        text = feeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = feeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slots Progress Bar
            val progress = (tournament.filledSlots.toFloat() / tournament.totalSlots.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slots: ${tournament.filledSlots}/${tournament.totalSlots} joined",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                val remaining = (tournament.totalSlots - tournament.filledSlots).coerceAtLeast(0)
                Text(
                    text = if (remaining > 0) "$remaining spots left" else "Filled",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining <= 5) CrimsonRed else CyberCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 0.9f) CrimsonRed else ElectricViolet,
                trackColor = DarkSurfaceElevated
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            if (isUserRegistered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Joined",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }

                    Button(
                        onClick = onViewRoomClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("view_room_button_${tournament.id}")
                    ) {
                        Icon(
                            imageVector = if (tournament.status == "ROOM_RELEASED" || tournament.status == "LIVE")
                                Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room ID & Pass",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            } else {
                Button(
                    onClick = onJoinClick,
                    enabled = tournament.status == "REGISTRATION_OPEN" && tournament.filledSlots < tournament.totalSlots,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricViolet,
                        disabledContainerColor = DarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("join_tournament_button_${tournament.id}")
                ) {
                    val label = when {
                        tournament.status != "REGISTRATION_OPEN" -> "Registration Closed"
                        tournament.filledSlots >= tournament.totalSlots -> "Tournament Full"
                        tournament.entryFeePaise == 0L -> "Join Tournament (FREE)"
                        else -> "Join Tournament (${CurrencyFormatter.formatPaise(tournament.entryFeePaise)})"
                    }
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
