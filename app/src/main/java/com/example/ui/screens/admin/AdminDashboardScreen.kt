package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeaderboardPrizeConfigEntity
import com.example.data.local.entity.TournamentEntity
import com.example.data.local.entity.WithdrawalRequestEntity
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CrimsonRed
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
import com.example.util.DateUtils
import java.util.UUID

enum class AdminSection {
    OVERVIEW,
    TOURNAMENTS,
    WITHDRAWALS,
    PRIZES,
    CLOUD_CONFIG
}

@Composable
fun AdminDashboardScreen(
    tournaments: List<TournamentEntity>,
    withdrawals: List<WithdrawalRequestEntity>,
    prizeConfigs: List<LeaderboardPrizeConfigEntity>,
    onBack: () -> Unit,
    onCreateTournament: (TournamentEntity) -> Unit,
    onReleaseRoom: (tournamentId: String, roomId: String, roomPass: String) -> Unit,
    onFinalizeScores: (tournamentId: String, rank1Uid: String, rank2Uid: String?, rank3Uid: String?) -> Unit,
    onReviewWithdrawal: (requestId: String, approve: Boolean, note: String) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    var selectedSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var releaseRoomTournament by remember { mutableStateOf<TournamentEntity?>(null) }
    var finalizeScoreTournament by remember { mutableStateOf<TournamentEntity?>(null) }

    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING_REVIEW" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // App Bar
        Surface(color = DarkSurface, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ARENA X Admin Control",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Text("Logout", color = CrimsonRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section Tabs
        Surface(color = DarkSurfaceElevated, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AdminTabButton(
                    title = "KPIs",
                    isSelected = selectedSection == AdminSection.OVERVIEW,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSection = AdminSection.OVERVIEW }
                )
                AdminTabButton(
                    title = "Tournaments",
                    isSelected = selectedSection == AdminSection.TOURNAMENTS,
                    modifier = Modifier.weight(1.3f),
                    onClick = { selectedSection = AdminSection.TOURNAMENTS }
                )
                AdminTabButton(
                    title = "Withdrawals (${pendingWithdrawals.size})",
                    isSelected = selectedSection == AdminSection.WITHDRAWALS,
                    modifier = Modifier.weight(1.5f),
                    onClick = { selectedSection = AdminSection.WITHDRAWALS }
                )
                AdminTabButton(
                    title = "Cloud Ops",
                    isSelected = selectedSection == AdminSection.CLOUD_CONFIG,
                    modifier = Modifier.weight(1.2f),
                    onClick = { selectedSection = AdminSection.CLOUD_CONFIG }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSection) {
                AdminSection.OVERVIEW -> {
                    // KPI Metric Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(
                                title = "Active Tourneys",
                                value = "${tournaments.count { it.status == "REGISTRATION_OPEN" || it.status == "LIVE" }}",
                                color = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Pending Payouts",
                                value = "${pendingWithdrawals.size}",
                                color = if (pendingWithdrawals.isNotEmpty()) CrimsonRed else EmeraldGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val totalPrizePool = tournaments.sumOf { it.prizePoolPaise }
                            KpiCard(
                                title = "Total Prize Volume",
                                value = CurrencyFormatter.formatPaise(totalPrizePool),
                                color = EsportsGold,
                                modifier = Modifier.weight(1f)
                            )
                            val totalPendingPaise = pendingWithdrawals.sumOf { it.amountPaise }
                            KpiCard(
                                title = "Pending Payouts Value",
                                value = CurrencyFormatter.formatPaise(totalPendingPaise),
                                color = ElectricViolet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Quick Management Shortcuts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_quick_create_tourney")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create & Publish New Tournament", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                AdminSection.TOURNAMENTS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tournaments (${tournaments.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    if (tournaments.isEmpty()) {
                        item {
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("No tournaments created yet.", color = TextSecondary, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Tap '+ New' above to publish the first tournament.", color = CyberCyan, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(tournaments) { tour ->
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(tour.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${tour.gameTitle} • ${tour.format} • ${tour.map}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = DarkSurfaceElevated) {
                                        Text(tour.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan, modifier = Modifier.padding(4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { releaseRoomTournament = tour },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Set Room ID", color = CyberCyan, fontSize = 11.sp)
                                    }

                                    if (tour.status != "COMPLETED") {
                                        Button(
                                            onClick = { finalizeScoreTournament = tour },
                                            colors = ButtonDefaults.buttonColors(containerColor = EsportsGold.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = EsportsGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Award Prizes", color = EsportsGold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }

                AdminSection.WITHDRAWALS -> {
                    item {
                        Text(
                            text = "Pending Withdrawal Payouts (${pendingWithdrawals.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (pendingWithdrawals.isEmpty()) {
                        item {
                            Surface(color = DarkSurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No pending withdrawal requests. All settled!", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(pendingWithdrawals) { req ->
                            Surface(
                                color = DarkSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.fillMaxWidth().testTag("pending_withdrawal_${req.id}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "User: ${req.userId}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Target UPI: ${req.upiId}",
                                                fontSize = 12.sp,
                                                color = CyberCyan
                                            )
                                            Text(
                                                text = DateUtils.formatMatchTime(req.createdAtMs),
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Text(
                                            text = CurrencyFormatter.formatPaise(req.amountPaise),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EsportsGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { onReviewWithdrawal(req.id, true, "Paid via UPI IMPS") },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("approve_payout_button")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve & Pay", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { onReviewWithdrawal(req.id, false, "Verification Failed / Invalid UPI") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("reject_payout_button")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject & Refund", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminSection.CLOUD_CONFIG -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Backend & Cloud Infrastructure Status", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Status: Real Device-Local Persistence (Room Database active).\n" +
                                            "Cloud Sync & Firebase Backend: Pending User Consent.\n" +
                                            "Security Rules: Zero-Trust rules defined in /firestore.rules\n" +
                                            "Schema: /firebase-blueprint.json complete and validated.\n" +
                                            "Payment Gateway: UPI Sandbox interface active.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Create Tournament Dialog
    if (showCreateDialog) {
        var title by remember { mutableStateOf("Bermuda Super Blitz #105") }
        var format by remember { mutableStateOf("Solo") }
        var map by remember { mutableStateOf("Bermuda") }
        var feeInr by remember { mutableStateOf("25") }
        var prizeInr by remember { mutableStateOf("1000") }
        var slotsText by remember { mutableStateOf("48") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = DarkSurface,
            title = { Text("Publish New Tournament", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tournament Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = format,
                            onValueChange = { format = it },
                            label = { Text("Format (Solo/Duo/Squad)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = map,
                            onValueChange = { map = it },
                            label = { Text("Map") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = feeInr,
                            onValueChange = { feeInr = it },
                            label = { Text("Entry Fee (₹)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prizeInr,
                            onValueChange = { prizeInr = it },
                            label = { Text("Prize Pool (₹)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = slotsText,
                        onValueChange = { slotsText = it },
                        label = { Text("Total Slots") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feePaise = (feeInr.toLongOrNull() ?: 0L) * 100L
                        val prizePaise = (prizeInr.toLongOrNull() ?: 0L) * 100L
                        val slots = slotsText.toIntOrNull() ?: 48
                        val newTour = TournamentEntity(
                            id = "tour_ff_${UUID.randomUUID().toString().take(6)}",
                            title = title,
                            gameTitle = "Free Fire MAX",
                            format = format,
                            map = map,
                            mode = "Battle Royale",
                            status = "REGISTRATION_OPEN",
                            entryFeePaise = feePaise,
                            prizePoolPaise = prizePaise,
                            firstPrizePaise = (prizePaise * 0.5).toLong(),
                            secondPrizePaise = (prizePaise * 0.3).toLong(),
                            thirdPrizePaise = (prizePaise * 0.2).toLong(),
                            perKillPaise = 500L,
                            totalSlots = slots,
                            filledSlots = 0,
                            startTimeMs = System.currentTimeMillis() + (3 * 3600 * 1000L),
                            rules = "1. Mobile only.\n2. Fair play strictly enforced."
                        )
                        onCreateTournament(newTour)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Publish Tournament", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Release Room Dialog
    releaseRoomTournament?.let { tour ->
        var roomId by remember { mutableStateOf(tour.roomId ?: "") }
        var roomPass by remember { mutableStateOf(tour.roomPassword ?: "") }

        AlertDialog(
            onDismissRequest = { releaseRoomTournament = null },
            containerColor = DarkSurface,
            title = { Text("Release Room Credentials", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = { Text("Custom Room ID") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = roomPass,
                        onValueChange = { roomPass = it },
                        label = { Text("Room Password") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReleaseRoom(tour.id, roomId, roomPass)
                        releaseRoomTournament = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Release to Players", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { releaseRoomTournament = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Finalize Score Dialog
    finalizeScoreTournament?.let { tour ->
        var rank1User by remember { mutableStateOf("usr_player_me") }
        var rank2User by remember { mutableStateOf("usr_comp_1") }
        var rank3User by remember { mutableStateOf("usr_comp_2") }

        AlertDialog(
            onDismissRequest = { finalizeScoreTournament = null },
            containerColor = DarkSurface,
            title = { Text("Finalize Match & Award Prizes", color = Color.White) },
            text = {
                Column {
                    Text("Enter Winner User IDs to disburse prizes automatically into their wallets:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rank1User,
                        onValueChange = { rank1User = it },
                        label = { Text("1st Place Winner (${CurrencyFormatter.formatPaise(tour.firstPrizePaise)})") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rank2User,
                        onValueChange = { rank2User = it },
                        label = { Text("2nd Place Winner (${CurrencyFormatter.formatPaise(tour.secondPrizePaise)})") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rank3User,
                        onValueChange = { rank3User = it },
                        label = { Text("3rd Place Winner (${CurrencyFormatter.formatPaise(tour.thirdPrizePaise)})") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onFinalizeScores(tour.id, rank1User, rank2User.ifBlank { null }, rank3User.ifBlank { null })
                        finalizeScoreTournament = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EsportsGold)
                ) {
                    Text("Disburse Prizes & Complete", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { finalizeScoreTournament = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ElectricViolet else DarkSurface,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun KpiCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
