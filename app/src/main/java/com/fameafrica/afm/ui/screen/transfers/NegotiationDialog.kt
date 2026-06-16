package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun NegotiationDialog(
    player: TransferPlayerUiModel,
    onDismiss: () -> Unit,
    onConfirm: (ContractOffer) -> Unit
) {
    var wage by remember { mutableFloatStateOf(player.wage.toFloat()) }
    var years by remember { mutableIntStateOf(3) }
    var role by remember { mutableStateOf(PlayingTimeRole.IMPORTANT_PLAYER) }
    var signingBonus by remember { mutableFloatStateOf(0f) }
    var agentFee by remember { mutableFloatStateOf(0f) }
    var releaseClause by remember { mutableStateOf<Int?>(null) }
    
    // Negotiation Phase: 0 = Approach Agent, 1 = Formal Terms
    var phase by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Handshake, null, tint = FameColors.TrophyGold)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (phase == 0) "APPROACH AGENT" else "FORMAL CONTRACT TERMS", 
                    style = AFMTextStyles.textLG.copy(fontSize = 18.sp), 
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Player Header
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(player.name.uppercase(), style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = Color.White)
                            Text("${player.age} • ${player.position}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                        }
                    }
                }

                if (phase == 0) {
                    // Phase 0: Approach Agent
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Discuss the player's interest and playing time expectations before making a formal offer.",
                            style = AFMTextStyles.textXS,
                            color = FameColors.MutedParchment
                        )
                        
                        Column {
                            Text("PROPOSED PLAYING TIME", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlayingTimeRole.entries.forEach { r ->
                                    FilterChip(
                                        selected = role == r,
                                        onClick = { role = r },
                                        label = { Text(r.name.replace("_", " ").lowercase().capitalize(), fontSize = 10.sp) },
                                        shape = RoundedCornerShape(2.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = FameColors.TrophyGold,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Agent feedback mockup
                        Surface(
                            color = FameColors.Info.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, FameColors.Info.copy(alpha = 0.3f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = FameColors.Info, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "The agent indicates the player is ${player.interestLevel.name.replace("_", " ").lowercase()} in joining as a ${role.name.replace("_", " ").lowercase()}.",
                                    style = AFMTextStyles.textXXS,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Phase 1: Formal Terms
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Wage with Tooltip/Context
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("WEEKLY WAGE", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                                Text("$${wage.toInt()}/WK", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = FameColors.TrophyGold)
                            }
                            Slider(
                                value = wage,
                                onValueChange = { wage = it },
                                valueRange = (player.wage * 0.5f)..(player.wage * 3f),
                                colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                            )
                            Text(
                                "TEAM HIGHEST EARNER: $24K/WK", 
                                style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), 
                                color = FameColors.MutedParchment,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Signing Bonus & Agent Fee
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SIGNING BONUS", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                                Slider(
                                    value = signingBonus,
                                    onValueChange = { signingBonus = it },
                                    valueRange = 0f..500000f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.3f))
                                )
                                Text("$${signingBonus.toInt()}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AGENT FEE", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                                Slider(
                                    value = agentFee,
                                    onValueChange = { agentFee = it },
                                    valueRange = 0f..100000f,
                                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White.copy(alpha = 0.3f))
                                )
                                Text("$${agentFee.toInt()}", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                            }
                        }

                        // Duration
                        Column {
                            Text("CONTRACT DURATION", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..5).forEach { y ->
                                    Surface(
                                        onClick = { years = y },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(2.dp),
                                        color = if (years == y) FameColors.TrophyGold else Color.White.copy(alpha = 0.05f),
                                        border = BorderStroke(0.5.dp, if (years == y) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${y}Y", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = if (years == y) Color.Black else Color.White)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Release Clause (Toggle/Simple)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("RELEASE CLAUSE", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(modifier = Modifier.weight(1f))
                                Switch(
                                    checked = releaseClause != null,
                                    onCheckedChange = { if(it) releaseClause = (player.value * 2).toInt() else releaseClause = null },
                                    colors = SwitchDefaults.colors(checkedThumbColor = FameColors.TrophyGold)
                                )
                            }
                            if (releaseClause != null) {
                                Text("$${releaseClause!! / 1000000}M", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phase == 0) phase = 1
                    else onConfirm(ContractOffer(wage.toInt(), years, signingBonus.toInt(), agentFee.toInt(), role, releaseClause, null, null, null))
                },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold, contentColor = Color.Black)
            ) {
                Text(if (phase == 0) "CONTINUE TO TERMS" else "SUBMIT OFFER", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontWeight = FontWeight.Bold, color = FameColors.MutedParchment)
            }
        },
        containerColor = FameColors.DeepNavyBlack,
        shape = RoundedCornerShape(4.dp)
    )
}

data class ContractOffer(
    val weeklyWage: Int,
    val contractYears: Int,
    val signingBonus: Int,
    val agentFee: Int,
    val playingTimeRole: PlayingTimeRole,
    val releaseClause: Int?,
    val appearanceBonusPerStart: Int?,
    val goalBonus: Int?,
    val sellOnPercentage: Int?
)

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
