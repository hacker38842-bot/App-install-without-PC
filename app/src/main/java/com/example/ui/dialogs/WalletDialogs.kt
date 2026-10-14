package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.UUID

@Composable
fun DepositDialog(
    onDismiss: () -> Unit,
    onConfirmDeposit: (amountPaise: Long, orderId: String) -> Unit
) {
    var selectedAmountInr by remember { mutableStateOf("50") }
    val quickOptions = listOf("20", "50", "100", "200", "500")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Add Cash to Wallet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select or enter deposit amount (INR):",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickOptions.forEach { opt ->
                        val isSelected = selectedAmountInr == opt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ElectricViolet else DarkSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAmountInr = opt }
                        ) {
                            Text(
                                text = "₹$opt",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Text Field
                OutlinedTextField(
                    value = selectedAmountInr,
                    onValueChange = { selectedAmountInr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gateway & Compliance note
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Supports UPI (GPay, PhonePe, Paytm, BHIM) and NetBanking with instant automated ledger verification.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountInr = selectedAmountInr.toLongOrNull() ?: 0L
                    if (amountInr > 0) {
                        val orderId = "order_upi_${UUID.randomUUID().toString().take(8)}"
                        onConfirmDeposit(amountInr * 100L, orderId)
                    }
                },
                enabled = (selectedAmountInr.toLongOrNull() ?: 0L) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_deposit_button")
            ) {
                Text(
                    text = "Pay ₹${selectedAmountInr.ifBlank { "0" }}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun WithdrawDialog(
    availableBalancePaise: Long,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (amountPaise: Long, upiId: String) -> Unit
) {
    var amountInrText by remember { mutableStateOf("100") }
    var upiIdText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Withdraw Winnings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Available to Withdraw", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = CurrencyFormatter.formatPaise(availableBalancePaise),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EsportsGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInrText,
                    onValueChange = { amountInrText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Withdraw Amount (Min ₹50)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = upiIdText,
                    onValueChange = { upiIdText = it },
                    label = { Text("UPI ID (e.g. mobile@paytm, user@okaxis)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_upi_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🔒 Direct IMPS payout to your UPI ID. 0% withdrawal fee. Processed securely within 15-30 minutes.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = CrimsonRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountInr = amountInrText.toLongOrNull() ?: 0L
                    val amountPaise = amountInr * 100L
                    if (amountInr < 50) {
                        errorText = "Minimum withdrawal is ₹50"
                        return@Button
                    }
                    if (amountPaise > availableBalancePaise) {
                        errorText = "Amount exceeds available balance"
                        return@Button
                    }
                    if (!upiIdText.contains("@") || upiIdText.length < 5) {
                        errorText = "Please enter a valid UPI ID (e.g. name@okhdfcbank)"
                        return@Button
                    }
                    onConfirmWithdraw(amountPaise, upiIdText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_withdraw_button")
            ) {
                Text("Request Payout", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
