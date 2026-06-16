package com.fameafrica.afm.ui.screen.squad

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.R
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.components.common.SidebarSectionTitle
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.extensions.toTitleCase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Locale

@Composable
fun SquadScreen(
    currentGameState: GameManager.GameState,
    onPlayerClick: (Int) -> Unit,
    onTacticsClick: () -> Unit,
    onTrainingClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: SquadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val teamName = uiState.teamName.ifEmpty { 
        (currentGameState as? GameManager.GameState.Active)?.context?.teamName ?: "Squad"
    }

    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        SquadContent(
            uiState = uiState,
            teamName = teamName,
            onPlayerClick = onPlayerClick,
            onTacticsClick = onTacticsClick,
            onTrainingClick = onTrainingClick,
            onTabSelected = viewModel::selectTab,
            onSortClick = viewModel::updateSortOption,
            onFormationChange = viewModel::selectFormation,
            onBack = onBack
        )
    }
}

@Composable
fun SquadContent(
    uiState: SquadUiState,
    teamName: String,
    onPlayerClick: (Int) -> Unit,
    onTacticsClick: () -> Unit,
    onTrainingClick: () -> Unit,
    onTabSelected: (String) -> Unit,
    onSortClick: (SortOption) -> Unit,
    onFormationChange: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFormationMenu by remember { mutableStateOf(false) }

    FameBackground {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // Refined Header with Quick Actions
            Surface(
                color = FameColors.DeepNavyBlack,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text(
                        text = "SQUAD",
                        style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Tactics Quick Action
                    IconButton(onClick = onTacticsClick) {
                        Icon(Icons.Default.SportsSoccer, "Tactics", tint = FameColors.ChampionsGold)
                    }

                    // Training Quick Action
                    IconButton(onClick = onTrainingClick) {
                        Icon(Icons.Default.FitnessCenter, "Training", tint = Color.White)
                    }
                    
                    IconButton(onClick = { showFormationMenu = true }) {
                        Icon(Icons.Default.GridView, "Formation", tint = FameColors.ChampionsGold)
                    }
                    
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "Sort", tint = Color.White)
                    }

                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            FormationDropdown(
                expanded = showFormationMenu,
                onDismiss = { showFormationMenu = false },
                onFormationSelected = onFormationChange,
                selectedFormation = uiState.formation
            )

            SortDropdown(
                expanded = showSortMenu,
                onDismiss = { showSortMenu = false },
                onSortSelected = onSortClick,
                selectedSort = uiState.sortBy
            )

            Spacer(modifier = Modifier.height(8.dp))

            SquadStatsHeader(uiState.squadStats)

            Spacer(modifier = Modifier.height(12.dp))

            SquadTabs(
                selectedTab = uiState.selectedTab,
                onTabSelected = onTabSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FameColors.ChampionsGold)
                }
            } else {
                if (uiState.selectedTab == "ANALYSIS") {
                    SquadAnalysisView(uiState.analysedData)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.xs),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    ) {
                        items(uiState.filteredPlayers, key = { it.id }) { player ->
                            PlayerItem(
                                player = player,
                                clubName = teamName,
                                currencyContext = uiState.currencyContext,
                                onClick = { onPlayerClick(player.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormationDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onFormationSelected: (String) -> Unit,
    selectedFormation: String
) {
    val formations = listOf("4-4-2", "4-3-3", "3-5-2", "5-3-2", "4-2-3-1", "4-5-1", "3-4-3")
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(FameColors.DeepNavyBlack).border(1.dp, Color.White.copy(0.1f))
    ) {
        formations.forEach { formation ->
            DropdownMenuItem(
                text = { 
                    Text(
                        formation, 
                        color = if (formation == selectedFormation) FameColors.ChampionsGold else Color.White,
                        fontWeight = if (formation == selectedFormation) FontWeight.Black else FontWeight.Normal
                    ) 
                },
                onClick = {
                    onFormationSelected(formation)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun SortDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit,
    selectedSort: SortOption
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(FameColors.DeepNavyBlack).border(1.dp, Color.White.copy(0.1f))
    ) {
        SortOption.entries.forEach { option ->
            DropdownMenuItem(
                text = { 
                    Text(
                        option.name.replace("_", " ").toTitleCase(), 
                        color = if (option == selectedSort) FameColors.ChampionsGold else Color.White,
                        fontWeight = if (option == selectedSort) FontWeight.Black else FontWeight.Normal
                    ) 
                },
                onClick = {
                    onSortSelected(option)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun SquadStatsHeader(stats: SquadStatsUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatBox("Players", stats.totalPlayers.toString(), Modifier.weight(1f))
        StatBox("Avg Ovr", String.format(Locale.ROOT, "%.1f", stats.averageRating), Modifier.weight(1f))
        val formattedValue = String.format(Locale.ROOT, "€%.1fM", stats.totalMarketValue / 1_000_000.0)
        StatBox("Value", formattedValue, Modifier.weight(1f))
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(label.uppercase(), style = AFMTextStyles.textXS.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            Text(value, style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SquadTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("All", "GK", "Def", "Mid", "Fwd", "Analysis")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(2.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab.equals(tab, ignoreCase = true)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isSelected) FameColors.ChampionsGold else Color.Transparent)
                    .clickable { onTabSelected(tab.uppercase()) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    tab,
                    style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun PlayerItem(
    player: PlayerUiModel,
    clubName: String,
    currencyContext: com.fameafrica.afm.utils.formatters.CurrencyFormatter.CurrencyContext? = null,
    onClick: () -> Unit
) {
    DynamicPlayerCard(
        player = player,
        clubName = clubName,
        currencyContext = currencyContext,
        onClick = onClick
    )
}

@Composable
fun SquadAnalysisView(data: SquadAnalysedData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SquadSectionCard {
                SidebarSectionTitle("Age Distribution")
                Spacer(modifier = Modifier.height(16.dp))
                AgeDistributionChart(data.ageDistribution)
            }
        }

        item {
            SquadSectionCard {
                SidebarSectionTitle("Squad Value by Position")
                Spacer(modifier = Modifier.height(12.dp))
                PositionValuePieChart(data.valueByPosition)
            }
        }

        item {
            SquadSectionCard {
                SidebarSectionTitle("Rating vs Potential")
                Spacer(modifier = Modifier.height(12.dp))
                RatingPotentialScatterChart(data.potentialVsRating)
            }
        }

        item {
            SquadSectionCard {
                SidebarSectionTitle("Squad Balance")
                Spacer(modifier = Modifier.height(16.dp))
                AttributesRadarChart(data.averageAttributes)
            }
        }
    }
}

@Composable
fun SquadSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
fun RatingPotentialScatterChart(scatter: List<Pair<Int, Int>>) {
    val primaryColor = FameColors.ChampionsGold.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        factory = { context ->
            ScatterChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = android.graphics.Color.WHITE
                axisLeft.textColor = android.graphics.Color.WHITE
                axisRight.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setNoDataText("Analyzing squad...")
            }
        },
        update = { chart ->
            if (scatter.isEmpty()) {
                chart.data = null
                chart.invalidate()
                return@AndroidView
            }
            
            val entries = scatter.map { Entry(it.first.toFloat(), it.second.toFloat()) }
            val dataSet = ScatterDataSet(entries, "Players").apply {
                setDrawValues(false)
                color = primaryColor
                setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                scatterShapeSize = 10f
            }
            chart.data = ScatterData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun AgeDistributionChart(distribution: Map<String, Int>) {
    val secondaryColor = FameColors.AfroSunOrange.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = android.graphics.Color.WHITE
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.isGranularityEnabled = true
                axisLeft.textColor = android.graphics.Color.WHITE
                axisLeft.axisMinimum = 0f
                axisLeft.granularity = 1f
                axisRight.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setFitBars(true)
            }
        },
        update = { chart ->
            if (distribution.isEmpty()) {
                chart.data = null
                chart.invalidate()
                return@AndroidView
            }
            
            val labels = distribution.keys.toList()
            val entries = distribution.values.mapIndexed { index, i -> BarEntry(index.toFloat(), i.toFloat()) }
            val dataSet = BarDataSet(entries, "").apply {
                color = secondaryColor
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 10f
                setDrawValues(true)
            }
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                labelCount = labels.size
            }
            chart.data = BarData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun PositionValuePieChart(values: Map<String, Long>) {
    val color1 = FameColors.ChampionsGold.toArgb()
    val color2 = FameColors.AfroSunOrange.toArgb()
    val color3 = FameColors.GrowthGreen.toArgb()
    val color4 = FameColors.AlertRed.toArgb()
    
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setHoleColor(android.graphics.Color.TRANSPARENT)
                setEntryLabelColor(android.graphics.Color.WHITE)
                setDrawEntryLabels(false)
                legend.apply {
                    isEnabled = true
                    textColor = android.graphics.Color.WHITE
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    yEntrySpace = 5f
                }
                holeRadius = 40f
                transparentCircleRadius = 45f
            }
        },
        update = { chart ->
            if (values.isEmpty()) {
                chart.data = null
                chart.invalidate()
                return@AndroidView
            }
            
            val entries = values.entries.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(color1, color2, color3, color4)
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 11f
                setDrawValues(true)
            }
            chart.data = PieData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun AttributesRadarChart(attributes: Map<String, Double>) {
    val primaryColor = FameColors.ChampionsGold.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                xAxis.textColor = android.graphics.Color.WHITE
                yAxis.isEnabled = false
                webColor = Color.White.copy(alpha = 0.2f).toArgb()
                legend.isEnabled = false
            }
        },
        update = { chart ->
            if (attributes.isEmpty()) {
                chart.data = null
                chart.invalidate()
                return@AndroidView
            }
            
            val entries = attributes.values.map { RadarEntry(it.toFloat()) }
            val dataSet = RadarDataSet(entries, "Squad Avg").apply {
                color = primaryColor
                fillColor = primaryColor
                setDrawFilled(true)
                fillAlpha = 100
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(attributes.keys.toList())
            chart.data = RadarData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PreviewSquadContent() {
    val sampleStats = SquadStatsUiModel(
        totalPlayers = 25,
        averageRating = 78.5,
        totalMarketValue = 450_000_000L
    )
    
    val sampleUiState = SquadUiState(
        isLoading = false,
        teamName = "Yanga SC",
        squadStats = sampleStats,
        selectedTab = "ALL"
    )

    AFM2026Theme {
        SquadContent(
            uiState = sampleUiState,
            teamName = "Yanga SC",
            onPlayerClick = {},
            onTacticsClick = {},
            onTrainingClick = {},
            onTabSelected = {},
            onSortClick = {},
            onFormationChange = {},
            onBack = {}
        )
    }
}
