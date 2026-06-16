package com.fameafrica.afm.ui.screen.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.ManagerOffersEntity
import com.fameafrica.afm.ui.components.common.SidebarBroadcastHeader
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.theme.*

@Composable
fun JobCentreScreen(
    onBack: () -> Unit,
    viewModel: JobMarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    JobCentreContent(
        uiState = uiState,
        onBack = onBack,
        onResign = { viewModel.resign() },
        onApply = { viewModel.applyForJob(it) }
    )
}

@Composable
fun JobCentreContent(
    uiState: JobMarketUiState,
    onBack: () -> Unit,
    onResign: () -> Unit,
    onApply: (Int) -> Unit
) {
    AFM2026Theme {
        Scaffold(
            topBar = {
                SidebarBroadcastHeader(
                    title = "JOB CENTRE",
                    icon = Icons.Default.Work,
                    subtitle = if (uiState.isEmployed) "CURRENTLY EMPLOYED" else "SEEKING NEW CHALLENGE",
                    actions = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = FameColors.WarmIvory)
                        }
                    }
                )
            },
            containerColor = FameColors.DeepNavyBlack
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (uiState.isEmployed) {
                    EmployedStatusCard(
                        teamName = uiState.currentTeamName ?: "Unknown",
                        onResign = onResign
                    )
                }

                if (uiState.myApplications.isNotEmpty()) {
                    Text(
                        "MY APPLICATIONS",
                        style = AFMTextStyles.textXS,
                        color = FameColors.TrophyGold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.myApplications) { app ->
                            ApplicationCard(app)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    "VACANCIES",
                    style = AFMTextStyles.textXS,
                    color = FameColors.TrophyGold,
                    modifier = Modifier.padding(16.dp)
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FameColors.TrophyGold)
                    }
                } else if (uiState.vacancies.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No current vacancies in the network.", color = FameColors.MutedParchment)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.vacancies) { vacancy ->
                            VacancyCard(
                                vacancy = vacancy,
                                onApply = { onApply(vacancy.offeringTeamId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployedStatusCard(teamName: String, onResign: () -> Unit) {
    var showConfirmResign by remember { mutableStateOf(false) }

    SidebarCard(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, tint = FameColors.Info)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("CURRENT CLUB", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text(teamName.uppercase(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (!showConfirmResign) {
                Button(
                    onClick = { showConfirmResign = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("HAND IN RESIGNATION", color = FameColors.AlertRed, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onResign,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("CONFIRM", color = Color.White, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
                    }
                    OutlinedButton(
                        onClick = { showConfirmResign = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("CANCEL", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(app: ManagerOffersEntity) {
    SidebarCard {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.offeringTeam.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text(app.leagueName.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            
            Surface(
                color = when (app.status) {
                    "interview" -> FameColors.TrophyGold.copy(alpha = 0.2f)
                    "rejected" -> FameColors.AlertRed.copy(alpha = 0.2f)
                    else -> FameColors.GrowthGreen.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    app.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = AFMTextStyles.textXXS,
                    fontWeight = FontWeight.Black,
                    color = when (app.status) {
                        "interview" -> FameColors.TrophyGold
                        "rejected" -> FameColors.AlertRed
                        else -> FameColors.GrowthGreen
                    }
                )
            }
        }
    }
}

@Composable
fun VacancyCard(vacancy: ManagerOffersEntity, onApply: () -> Unit) {
    SidebarCard {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(vacancy.offeringTeam.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    Text(vacancy.leagueName.uppercase(), style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("TIER", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text(vacancy.leagueLevel.toString(), style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold, contentColor = Color.Black)
            ) {
                Text("APPLY FOR POSITION", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun JobCentreScreenPreview() {
    JobCentreContent(
        uiState = JobMarketUiState(
            isLoading = false,
            isEmployed = true,
            currentTeamName = "Young Africans SC",
            vacancies = listOf(
                ManagerOffersEntity(
                    id = 1,
                    managerId = 0,
                    managerName = "Sample Manager",
                    offeringTeam = "Simba SC",
                    offeringTeamId = 2,
                    leagueName = "Tanzania Premier League",
                    leagueLevel = 1,
                    offeredSalary = 150000,
                    contractYears = 3,
                    offerType = "HEAD_COACH",
                    expiryDate = System.currentTimeMillis() + 604800000,
                    isVacancy = true,
                    status = "open"
                ),
                ManagerOffersEntity(
                    id = 2,
                    managerId = 0,
                    managerName = "Sample Manager",
                    offeringTeam = "Azam FC",
                    offeringTeamId = 3,
                    leagueName = "Tanzania Premier League",
                    leagueLevel = 1,
                    offeredSalary = 130000,
                    contractYears = 2,
                    offerType = "HEAD_COACH",
                    expiryDate = System.currentTimeMillis() + 604800000,
                    isVacancy = true,
                    status = "open"
                )
            ),
            myApplications = listOf(
                ManagerOffersEntity(
                    id = 3,
                    managerId = 1,
                    managerName = "Sample Manager",
                    offeringTeam = "Gor Mahia",
                    offeringTeamId = 4,
                    leagueName = "Kenyan Premier League",
                    leagueLevel = 1,
                    offeredSalary = 80000,
                    contractYears = 2,
                    offerType = "HEAD_COACH",
                    expiryDate = System.currentTimeMillis() + 604800000,
                    status = "interview",
                    isApplication = true
                )
            )
        ),
        onBack = {},
        onResign = {},
        onApply = {}
    )
}
