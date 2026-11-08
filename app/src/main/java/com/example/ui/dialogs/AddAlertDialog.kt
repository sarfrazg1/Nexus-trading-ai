package com.example.ui.dialogs

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AlertDirection
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyHover
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddAlertDialog(
    defaultSymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, targetPrice: Double, direction: AlertDirection, note: String) -> Unit
) {
    val supportedSymbols = listOf(
        "BTC/USDT" to "Bitcoin",
        "XAU/USD" to "Gold Spot",
        "XAG/USD" to "Silver Spot",
        "WTI/USD" to "Crude Oil WTI",
        "ETH/USDT" to "Ethereum"
    )

    var selectedSymbol by remember { mutableStateOf(if (supportedSymbols.any { it.first == defaultSymbol }) defaultSymbol else "BTC/USDT") }
    var targetPriceText by remember { mutableStateOf("") }
    var selectedDirection by remember { mutableStateOf(AlertDirection.ABOVE) }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_alert_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "CREATE PRICE ALERT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NexusGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Asset Selector Chips
                Text("Select Asset", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedSymbols.take(4).forEach { (sym, _) ->
                        val isSel = selectedSymbol == sym
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NexusGold else NavySurfaceVariant)
                                .clickable { selectedSymbol = sym }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym.split("/")[0],
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) NavyCard else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direction Toggle (Above or Below)
                Text("Trigger Condition", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlertDirection.values().forEach { dir ->
                        val isSel = selectedDirection == dir
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NavyHover else NavySurfaceVariant)
                                .border(1.dp, if (isSel) NexusGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedDirection = dir }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${dir.symbol} ${dir.label}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSel) NexusGold else TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Price Input
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = {
                        targetPriceText = it
                        errorMessage = null
                    },
                    label = { Text("Target Price ($)") },
                    placeholder = { Text("e.g. 68500.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("target_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusGold,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurfaceVariant,
                        unfocusedContainerColor = NavySurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note Input
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("e.g. Breakout retest confirmation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("alert_note_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexusGold,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurfaceVariant,
                        unfocusedContainerColor = NavySurfaceVariant
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF5252), fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dialog Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val price = targetPriceText.toDoubleOrNull()
                            if (price == null || price <= 0) {
                                errorMessage = "Please enter a valid target price"
                            } else {
                                val assetName = supportedSymbols.firstOrNull { it.first == selectedSymbol }?.second ?: selectedSymbol
                                onConfirm(selectedSymbol, assetName, price, selectedDirection, noteText)
                            }
                        },
                        modifier = Modifier.testTag("confirm_create_alert_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NexusGold, contentColor = NavyCard)
                    ) {
                        Text("Set Alert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
