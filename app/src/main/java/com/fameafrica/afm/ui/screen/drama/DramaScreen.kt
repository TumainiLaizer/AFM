package com.fameafrica.afm.ui.screen.drama

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.MatchFixingCasesEntity
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun DramaScreen(
    onBack: () -> Unit,
    viewModel: DramaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    NegotiationBackground {
        DramaContent(
            uiState = uiState,
            onBack = onBack,
            onAction = viewModel::handleDilemmaChoice
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DramaContent(
    uiState: DramaUiState,
    onBack: () -> Unit,
    onAction: (Int, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CONTINENTAL TRIBUNAL",
                        style = MaterialTheme.typography.headlineSmall,
                        color = FameColors.WarmIvory,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = FameColors.WarmIvory)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues) // Edge-to-Edge: Proper inset handling
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FameColors.KenteRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, 
                        end = 16.dp, 
                        top = 16.dp, 
                        bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GlassPanel {
                            Text(
                                "Investigate sensitive matters involving continental football integrity. Decisions carry extreme weight.",
                                style = AFMTextStyles.tableCell,
                                color = FameColors.WarmIvory,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(uiState.matchFixingCases, key = { it.id }) { case -> // Performance: Keyed items
                        DramaCard(case = case, onAction = onAction)
                    }

                    if (uiState.matchFixingCases.isEmpty()) {
                        item {
                            EmptyDramaState(message = "No active investigations in the tribunal.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DramaCard(case: MatchFixingCasesEntity, onAction: (Int, Boolean) -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, null, tint = FameColors.KenteRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CORRUPTION INVESTIGATION",
                    style = AFMTextStyles.tableHeader,
                    color = FameColors.KenteRed,
                    fontWeight = FontWeight.Black
                )
            }

            Column {
                Text(
                    text = "${case.leagueName} | TIER ${case.leagueLevel}".uppercase(),
                    style = AFMTextStyles.statLabel,
                    color = FameColors.ChampionsGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "\"${case.allegationDetails}\"",
                    style = AFMTextStyles.tableCell,
                    color = FameColors.WarmIvory,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (case.isInvestigating) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onAction(case.id, true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FameColors.KenteRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ACCEPT OFFER", style = AFMTextStyles.tableHeader)
                    }
                    OutlinedButton(
                        onClick = { onAction(case.id, false) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, FameColors.PitchGreen),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FameColors.PitchGreen)
                    ) {
                        Text("REPORT CASE", style = AFMTextStyles.tableHeader)
                    }
                }
            } else {
                val verdictColor = if (case.verdict == "Guilty") FameColors.KenteRed else FameColors.PitchGreen
                Surface(color = verdictColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(0.5.dp, verdictColor)) {
                    Text(
                        "VERDICT: ${case.verdict?.uppercase() ?: "PENDING"}",
                        style = AFMTextStyles.statLabel,
                        color = verdictColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black
                    )
                }
                case.punishment?.let {
                    Text("SANCTION: $it", style = AFMTextStyles.statLabel, color = FameColors.WarmIvory)
                }
            }
        }
    }
}

@Composable
fun EmptyDramaState(message: String) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            style = AFMTextStyles.tableCell,
            color = FameColors.MutedParchment,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        )
    }
}
