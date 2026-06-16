package com.fameafrica.afm.ui.screen.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FameBackground
import com.fameafrica.afm.ui.theme.FameColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FFPDashboardScreen(
    onBack: () -> Unit,
    viewModel: BoardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val finance = uiState.financeDashboard
    
    AFM2026Theme {
        FameBackground {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "FINANCIAL STABILITY",
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
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { FFPComplianceCard(finance.ffpStatus) }
                    item { SectionHeader(title = "INVESTMENT VS REVENUE", icon = Icons.AutoMirrored.Filled.TrendingUp) }
                    item { InfrastructureInvestmentCard(finance) }
                    item { SectionHeader(title = "ORGANIC REVENUE STREAMS", icon = Icons.Default.AccountBalance) }
                    item { OrganicRevenueCard(finance.revenueBreakdown) }
                }
            }
        }
    }
}

@Composable
fun FFPComplianceCard(ffpStatus: com.fameafrica.afm.data.repository.FFPStatus) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FFP STATUS", style = AFMTextStyles.tableHeader, color = FameColors.ChampionsGold)
                Surface(
                    color = (if (ffpStatus.isCompliant) FameColors.PitchGreen else FameColors.KenteRed).copy(alpha = 0.2f), 
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (ffpStatus.isCompliant) "COMPLIANT" else "NON-COMPLIANT",
                        style = AFMTextStyles.statLabel,
                        color = if (ffpStatus.isCompliant) FameColors.PitchGreen else FameColors.KenteRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Text(
                "Your club is ${if (ffpStatus.isCompliant) "within" else "exceeding"} the $${String.format("%.1f", ffpStatus.allowedLoss / 1000000.0)}M allowed loss threshold over a 3-year rolling period.",
                style = AFMTextStyles.denseText,
                color = FameColors.MutedParchment
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FFPMetricRow(label = "Net Result (3y)", value = "${if (ffpStatus.ffpProfitLoss >= 0) "+" else ""}$${String.format("%.1f", ffpStatus.ffpProfitLoss / 1000000.0)}M", isPositive = ffpStatus.ffpProfitLoss >= 0)
            FFPMetricRow(label = "Wage/Turnover Ratio", value = "${String.format("%.1f", ffpStatus.wageTurnoverRatio * 100)}%", isPositive = ffpStatus.wageTurnoverRatio <= 0.7)
            FFPMetricRow(label = "Infrastructure Exemptions", value = "$${String.format("%.1f", ffpStatus.infrastructureExemptions / 1000000.0)}M", isPositive = true)
        }
    }
}

@Composable
fun FFPMetricRow(label: String, value: String, isPositive: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AFMTextStyles.statLabel, color = FameColors.WarmIvory)
        Text(value, style = AFMTextStyles.tableCell, color = if (isPositive) FameColors.PitchGreen else FameColors.KenteRed, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfrastructureInvestmentCard(finance: com.fameafrica.afm.data.repository.TeamFinanceDashboard) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Infrastructure investments are exempt from FFP calculations to encourage long-term growth.",
                style = AFMTextStyles.denseText,
                color = FameColors.MutedParchment
            )
            
            val infraSpending = finance.expenseBreakdown["Infrastructure"] ?: 0L
            val stadiumSpending = (infraSpending * 0.6).toLong()
            val youthSpending = (infraSpending * 0.3).toLong()
            val medicalSpending = (infraSpending * 0.1).toLong()

            InvestmentBar(label = "Stadium & Fan Zone", amount = "$${String.format("%.1f", stadiumSpending / 1000000.0)}M", progress = (stadiumSpending / 5000000.0).toFloat().coerceIn(0f, 1f))
            InvestmentBar(label = "Youth & Training", amount = "$${String.format("%.1f", youthSpending / 1000000.0)}M", progress = (youthSpending / 3000000.0).toFloat().coerceIn(0f, 1f))
            InvestmentBar(label = "Medical & Science", amount = "$${String.format("%.1f", medicalSpending / 1000000.0)}M", progress = (medicalSpending / 1000000.0).toFloat().coerceIn(0f, 1f))
        }
    }
}

@Composable
fun InvestmentBar(label: String, amount: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AFMTextStyles.statLabel, color = FameColors.WarmIvory)
            Text(amount, style = AFMTextStyles.statLabel, color = FameColors.ChampionsGold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = FameColors.AfroSunOrange,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun OrganicRevenueCard(revenueBreakdown: Map<String, Long>) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RevenueItem(
                label = "Matchday Revenue", 
                value = "$${String.format("%.1f", (revenueBreakdown["Matchday"] ?: 0L) / 1000.0)}k", 
                description = "Tickets, Hospitality & Fan Zone"
            )
            RevenueItem(
                label = "Commercial & Merch", 
                value = "$${String.format("%.1f", (revenueBreakdown["Commercial"] ?: 0L) / 1000.0)}k", 
                description = "Regional Brand Expansion"
            )
            RevenueItem(
                label = "Player Sales", 
                value = "$${String.format("%.1f", (revenueBreakdown["Player Sales"] ?: 0L) / 1000000.0)}M", 
                description = "Revenue from Academy Products"
            )
        }
    }
}

@Composable
fun RevenueItem(label: String, value: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = FameColors.ChampionsGold)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = AFMTextStyles.tableCell, color = Color.White, fontWeight = FontWeight.Bold)
                Text(value, style = AFMTextStyles.tableCell, color = FameColors.PitchGreen)
            }
            Text(description, style = AFMTextStyles.tickerText, color = FameColors.MutedParchment)
        }
    }
}
