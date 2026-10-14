package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TournamentEntity
import com.example.data.local.entity.UserEntity
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

@Composable
fun JoinTournamentDialog(
    tournament: TournamentEntity,
    user: UserEntity?,
    userBalancePaise: Long,
    onDismiss: () -> Unit,
    onConfirmJoin: (ign: String, uid: String) -> Unit
) {
    var inGameName by remember { mutableStateOf(user?.inGameName ?: "") }
    var freeFireUid by remember { mutableStateOf(user?.freeFireUid ?: "") }
    var agreedToRules by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val hasSufficientBalance = userBalancePaise >= tournament.entryFeePaise

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Column {
                Text(
                    text = "Confirm Registration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = tournament.title,
                    fontSize = 13.sp,
                    color = CyberCyan
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary Box
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Entry Fee", fontSize = 11.sp, color = TextSecondary)
                            val feeStr = if (tournament.entryFeePaise == 0L) "FREE" else CurrencyFormatter.formatPaise(tournament.entryFeePaise)
                            Text(text = feeStr, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EsportsGold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Your Balance", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = CurrencyFormatter.formatPaise(userBalancePaise),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasSufficientBalance) EmeraldGreen else CrimsonRed
                            )
                        }
                    }
                }

                if (!hasSufficientBalance && tournament.entryFeePaise > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Insufficient balance. Please add funds to your wallet.",
                        color = CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In-Game Name Input
                OutlinedTextField(
                    value = inGameName,
                    onValueChange = { inGameName = it },
                    label = { Text("Free Fire In-Game Name (IGN)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ElectricViolet,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_ign_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Free Fire Player UID Input
                OutlinedTextField(
                    value = freeFireUid,
                    onValueChange = { freeFireUid = it },
                    label = { Text("Free Fire Player UID (Numbers)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ElectricViolet,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("join_uid_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Agreement Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { agreedToRules = !agreedToRules }
                ) {
                    Checkbox(
                        checked = agreedToRules,
                        onCheckedChange = { agreedToRules = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ElectricViolet,
                            uncheckedColor = TextSecondary
                        )
                    )
                    Text(
                        text = "I agree to fair play rules. Mobile players only. No emulators/hacks.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = it, color = CrimsonRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inGameName.isBlank()) {
                        errorText = "Please enter your Free Fire In-Game Name"
                        return@Button
                    }
                    if (freeFireUid.isBlank() || freeFireUid.length < 5) {
                        errorText = "Please enter a valid numeric Free Fire UID"
                        return@Button
                    }
                    if (!agreedToRules) {
                        errorText = "You must agree to tournament rules"
                        return@Button
                    }
                    if (!hasSufficientBalance && tournament.entryFeePaise > 0) {
                        errorText = "Insufficient wallet balance"
                        return@Button
                    }
                    onConfirmJoin(inGameName, freeFireUid)
                },
                enabled = hasSufficientBalance || tournament.entryFeePaise == 0L,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_join_button")
            ) {
                Text("Confirm & Join", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun RoomCredentialsDialog(
    tournament: TournamentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val releaseWindowMs = tournament.checkInWindowMinutes * 60 * 1000L
    val isRoomReleased = tournament.status == "ROOM_RELEASED" ||
            tournament.status == "LIVE" ||
            (tournament.startTimeMs - now <= releaseWindowMs)

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRoomReleased) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isRoomReleased) EmeraldGreen else CrimsonRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRoomReleased) "Match Room Credentials" else "Room Credentials Locked",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isRoomReleased && !tournament.roomId.isNullOrBlank()) {
                    Text(
                        text = "Match is ready. Open Free Fire MAX and join the Custom Room below:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Room ID Card
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "CUSTOM ROOM ID", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = tournament.roomId,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyberCyan
                                )
                            }
                            Button(
                                onClick = { copyToClipboard("Room ID", tournament.roomId) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = CyberCyan, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Room Password Card
                    Surface(
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ROOM PASSWORD", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = tournament.roomPassword ?: "None",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EsportsGold
                                )
                            }
                            Button(
                                onClick = { copyToClipboard("Room Password", tournament.roomPassword ?: "") },
                                colors = ButtonDefaults.buttonColors(containerColor = EsportsGold.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EsportsGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = EsportsGold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ Do NOT share credentials with non-registered players. Spectating or leaking causes permanent ban.",
                        fontSize = 11.sp,
                        color = CrimsonRed
                    )
                } else {
                    // Locked State
                    Surface(
                        color = CrimsonRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Security Protection Active", color = CrimsonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To prevent room leakage and ghosting, credentials unlock automatically ${tournament.checkInWindowMinutes} minutes before the match start.",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Expected Unlock: ${DateUtils.formatMatchTime(tournament.startTimeMs - releaseWindowMs)}",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Got It", color = Color.White)
            }
        }
    )
}
