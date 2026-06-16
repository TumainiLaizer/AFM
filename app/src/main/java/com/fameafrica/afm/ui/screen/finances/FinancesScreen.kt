package com.fameafrica.afm.ui.screen.finances

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.formatters.CurrencyFormatter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun FinancesScreen(
    onBack: () -> Unit,
    onUpgradeInfrastructure: () -> Unit,
    initialTab: Int = 0,
    viewModel: FinancesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        FinancesContent(
            uiState = uiState,
            initialTab = initialTab,
            onBack = onBack,
            onUpgradeInfrastructure = onUpgradeInfrastructure,
            onRequestBudget = { viewModel.requestBudgetIncrease() },
            onRenegotiateSponsor = { viewModel.renegotiateSponsor(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesContent(
    uiState: FinancesUiState,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onUpgradeInfrastructure: () -> Unit,
    onRequestBudget: () -> Unit,
    onRenegotiateSponsor: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    FameBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SidebarBroadcastHeader(
                    "FINANCIAL MANAGEMENT".uppercase(),
                    Icons.Default.AccountBalanceWallet,
                    null,
                    {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                val context = uiState.currencyContext
                if (uiState.isLoading || context == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FinanceTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                0 -> OverviewTab(uiState, context, onUpgradeInfrastructure, onRequestBudget = onRequestBudget)
                                1 -> RevenueTab(uiState, context)
                                2 -> ExpensesTab(uiState, context)
                                3 -> SponsorsTab(uiState, context, onRenegotiate = onRenegotiateSponsor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("OVERVIEW", "REVENUE", "EXPENSES", "SPONSORS")
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = FameColors.HeaderDark,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = MaterialTheme.colorScheme.primary,
                height = 2.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        style = AFMTextStyles.textXS,
                        fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Normal,
                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else FameColors.WarmIvory.copy(alpha = 0.6f)
                    )
                }
            )
        }
    }
}

@Composable
fun OverviewTab(uiState: FinancesUiState, context: CurrencyFormatter.CurrencyContext, onUpgradeInfrastructure: () -> Unit, onRequestBudget: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item { FinancialSummaryCard(uiState.financialSummary, context) }
        item { BudgetOverviewCard(uiState.budget, uiState.wageBill, context) }
        item { FinancialHealthCard("FINANCIAL TIER", uiState.financialTier, uiState.isProfitable) }
        item { 
            ProfitLossBarChartCard(
                uiState.profitLossHistory,
                context
            )
        }
        item { FinancialActionButtons(onUpgradeInfrastructure, onRequestBudget) }
    }
}

@Composable
fun FinanceSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    SidebarCard(modifier = modifier) {
        content()
    }
}

@Composable
fun FinancialSummaryCard(summary: FinancialSummaryUiModel?, context: CurrencyFormatter.CurrencyContext) {
    FinanceSectionCard {
        Text("CASH POSITION", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FinancialFigure("BALANCE", formatCurrency(summary?.bankBalance ?: 0L, context), FameColors.WarmIvory)
            FinancialFigure("MONTHLY P/L", formatCurrency(summary?.profitLoss ?: 0L, context), 
                if (summary?.isProfitable == true) MaterialTheme.colorScheme.primary else FameColors.AlertRed)
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FinancialFigure("REVENUE", formatCurrency(summary?.revenue ?: 0L, context), FameColors.WarmIvory.copy(alpha = 0.7f))
            FinancialFigure("EXPENSES", formatCurrency(summary?.expenses ?: 0L, context), FameColors.AlertRed.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun FinancialFigure(label: String, value: String, valueColor: Color) {
    Column {
        Text(label.uppercase(), style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.5f))
        Text(value, style = AFMTextStyles.textSM, color = valueColor, fontWeight = FontWeight.Black)
    }
}

@Composable
fun BudgetOverviewCard(budget: Long, wageBill: Long, context: CurrencyFormatter.CurrencyContext) {
    FinanceSectionCard {
        Text("BUDGET ALLOCATION", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        BudgetRow("TRANSFER BUDGET", formatCurrency(budget, context), FameColors.WarmIvory)
        Spacer(modifier = Modifier.height(8.dp))
        BudgetRow("ANNUAL WAGE BILL", formatCurrency(wageBill, context), FameColors.WarmIvory.copy(alpha = 0.7f))
    }
}

@Composable
fun BudgetRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label.uppercase(), style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.6f))
        Text(value, style = AFMTextStyles.textSM, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BreakdownCard(title: String, items: Map<String, Long>, total: Long, icon: ImageVector, accentColor: Color, context: CurrencyFormatter.CurrencyContext) {
    FinanceSectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(32.dp),
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.padding(6.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title.uppercase(), style = AFMTextStyles.textSM, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        items.forEach { (label, amount) ->
            val percentage = if (total > 0) (amount.toFloat() / total * 100).toInt() else 0
            BreakdownRow(label, amount, percentage, accentColor, context)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun BreakdownRow(label: String, amount: Long, percentage: Int, color: Color, context: CurrencyFormatter.CurrencyContext) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.uppercase(), style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.7f))
            Text(formatCurrency(amount, context), style = AFMTextStyles.textXS, color = FameColors.WarmIvory, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        AFMProgressBar(
            progress = percentage / 100f,
            modifier = Modifier.fillMaxWidth(),
            color = color
        )
    }
}

@Composable
fun ProfitLossBarChartCard(history: List<ProfitLossEntry>, context: CurrencyFormatter.CurrencyContext) {
    FinanceSectionCard {
        Text("PERFORMANCE TREND", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        val primaryColorInt = MaterialTheme.colorScheme.primary.toArgb()
        val alertRedInt = FameColors.AlertRed.toArgb()
        val warmIvoryInt = FameColors.WarmIvory.toArgb()

        AndroidView(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            factory = { ctx ->
                BarChart(ctx).apply {
                    description.isEnabled = false
                    legend.isEnabled = false
                    setDrawGridBackground(false)
                    setTouchEnabled(false)
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        setDrawGridLines(false)
                        textColor = warmIvoryInt
                        textSize = 8f
                    }
                    axisLeft.apply {
                        setDrawGridLines(true)
                        gridColor = Color.White.copy(alpha = 0.05f).toArgb()
                        textColor = warmIvoryInt
                        textSize = 8f
                    }
                    axisRight.isEnabled = false
                }
            },
            update = { chart ->
                val entries = history.mapIndexed { index, entry -> 
                    BarEntry(index.toFloat(), entry.amount.toFloat()) 
                }
                val dataSet = BarDataSet(entries, "Monthly P/L").apply {
                    colors = history.map { if (it.amount >= 0) primaryColorInt else alertRedInt }
                    valueTextColor = warmIvoryInt
                    valueTextSize = 8f
                }
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(history.map { it.label })
                chart.data = BarData(dataSet)
                chart.invalidate()
            }
        )
    }
}

@Composable
fun FinancialHealthCard(label: String, value: String, isPositive: Boolean) {
    FinanceSectionCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(label, style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.6f))
                Text(value.uppercase(), style = AFMTextStyles.textSM, color = if (isPositive) MaterialTheme.colorScheme.primary else FameColors.AlertRed, fontWeight = FontWeight.Black)
            }
            Icon(
                if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                null,
                tint = if (isPositive) MaterialTheme.colorScheme.primary else FameColors.AlertRed,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun RevenueTab(uiState: FinancesUiState, context: CurrencyFormatter.CurrencyContext) {
    val revenueItems = uiState.revenueBreakdown
    val totalRevenue = revenueItems.values.sum()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item { BreakdownCard("REVENUE SOURCES", revenueItems, totalRevenue, Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.primary, context) }
    }
}

@Composable
fun ExpensesTab(uiState: FinancesUiState, context: CurrencyFormatter.CurrencyContext) {
    val expenseItems = uiState.expenseBreakdown
    val totalExpenses = expenseItems.values.sum()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item { BreakdownCard("EXPENDITURE", expenseItems, totalExpenses, Icons.AutoMirrored.Filled.TrendingDown, FameColors.AlertRed, context) }
    }
}

@Composable
fun SponsorsTab(uiState: FinancesUiState, context: CurrencyFormatter.CurrencyContext, onRenegotiate: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(uiState.sponsors) { sponsor ->
            SponsorItem(sponsor, context, onRenegotiate = { onRenegotiate(sponsor.id) })
        }
        item { LeagueComparisonCard(uiState.bankBalance, uiState.leagueAverageRevenue, uiState.leagueHighestRevenue, context) }
    }
}

@Composable
fun SponsorItem(sponsor: SponsorUiModel, context: CurrencyFormatter.CurrencyContext, onRenegotiate: () -> Unit) {
    SidebarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = FameColors.DeepNavyBlack,
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(sponsor.name.uppercase(), style = AFMTextStyles.textSM, color = FameColors.WarmIvory, fontWeight = FontWeight.Black)
                Text(sponsor.type.uppercase(), style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary)
                Text("${sponsor.yearsRemaining} YEARS REMAINING", style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.5f))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(sponsor.annualValue, context), style = AFMTextStyles.textSM, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text("PER ANNUM", style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun LeagueComparisonCard(clubBudget: Long, avgBudget: Long, maxBudget: Long, context: CurrencyFormatter.CurrencyContext) {
    FinanceSectionCard {
        Text("LEAGUE COMPARISON", style = AFMTextStyles.textXS, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        ComparisonBar("YOUR CLUB", clubBudget, maxBudget, MaterialTheme.colorScheme.primary, context)
        Spacer(modifier = Modifier.height(12.dp))
        ComparisonBar("LEAGUE AVG", avgBudget, maxBudget, FameColors.WarmIvory.copy(alpha = 0.4f), context)
        Spacer(modifier = Modifier.height(12.dp))
        ComparisonBar("LEAGUE MAX", maxBudget, maxBudget, FameColors.WarmIvory.copy(alpha = 0.2f), context)
    }
}

@Composable
fun ComparisonBar(label: String, value: Long, max: Long, color: Color, context: CurrencyFormatter.CurrencyContext) {
    val ratio = if (max > 0) value.toFloat() / max else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.uppercase(), style = AFMTextStyles.textXS, color = FameColors.WarmIvory.copy(alpha = 0.7f))
            Text(formatCurrency(value, context), style = AFMTextStyles.textXS, color = FameColors.WarmIvory)
        }
        Spacer(modifier = Modifier.height(4.dp))
        AFMProgressBar(
            progress = ratio,
            modifier = Modifier.fillMaxWidth(),
            color = color
        )
    }
}

@Composable
fun FinancialActionButtons(onUpgradeInfrastructure: () -> Unit, onRequestBudget: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onUpgradeInfrastructure,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(2.dp)
        ) {
            Text("UPGRADE FACILITIES", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
        }
        
        OutlinedButton(
            onClick = onRequestBudget,
            modifier = Modifier.weight(1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("REQUEST BUDGET", style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
        }
    }
}

fun formatCurrency(amount: Long, context: CurrencyFormatter.CurrencyContext): String {
    val isNegative = amount < 0
    val absAmount = amount.absoluteValue
    val formatted = if (absAmount >= 1_000_000) {
        String.format(Locale.getDefault(), "%.1fM", absAmount / 1_000_000.0)
    } else if (absAmount >= 1_000) {
        String.format(Locale.getDefault(), "%.0fK", absAmount / 1_000.0)
    } else {
        absAmount.toString()
    }
    return (if (isNegative) "-" else "") + context.symbol + formatted
}

// ============ PREVIEWS ============

private fun getMockFinancesState(): FinancesUiState {
    return FinancesUiState(
        isLoading = false,
        currencyContext = CurrencyFormatter.CurrencyContext("EUR", "€", 1.0),
        financialTier = "Rich",
        budget = 25000000,
        bankBalance = 45000000,
        wageBill = 12000000,
        financialSummary = FinancialSummaryUiModel(
            revenue = 15000000,
            expenses = 10000000,
            profitLoss = 5000000,
            bankBalance = 45000000,
            isProfitable = true
        ),
        revenueBreakdown = mapOf("Ticket Sales" to 5000000, "Merchandise" to 2000000, "Sponsorship" to 8000000),
        expenseBreakdown = mapOf("Wages" to 1000000, "Maintenance" to 500000, "Travel" to 200000),
        profitLossHistory = listOf(
            ProfitLossEntry("AUG", 500000),
            ProfitLossEntry("SEP", -200000),
            ProfitLossEntry("OCT", 1200000)
        ),
        sponsors = listOf(
            SponsorUiModel(1, "Emirates", "Main Kit", 5000000, 3),
            SponsorUiModel(2, "Adidas", "Technical", 3000000, 2)
        ),
        leagueAverageRevenue = 15000000,
        leagueHighestRevenue = 80000000
    )
}

@Preview(showBackground = true, name = "Finances - Portrait")
@Composable
fun FinancesScreenPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.CHAIRMAN_MODE) {
        FinancesContent(
            uiState = getMockFinancesState(),
            onBack = {},
            onUpgradeInfrastructure = {},
            onRequestBudget = {},
            onRenegotiateSponsor = {}
        )
    }
}

@Preview(showBackground = true, name = "Finances - Landscape", device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun FinancesScreenLandscapePreview() {
    AFM2026Theme(themePreset = FootballThemePreset.CHAIRMAN_MODE) {
        FinancesContent(
            uiState = getMockFinancesState(),
            onBack = {},
            onUpgradeInfrastructure = {},
            onRequestBudget = {},
            onRenegotiateSponsor = {}
        )
    }
}
