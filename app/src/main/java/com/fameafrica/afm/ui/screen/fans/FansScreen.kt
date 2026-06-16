package com.fameafrica.afm.ui.screen.fans

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.FanReactionsEntity
import com.fameafrica.afm.data.repository.FanExpectationsDashboard
import com.fameafrica.afm.data.repository.FanReactionsDashboard
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun FansScreen(
    teamId: Int,
    teamName: String,
    onBack: () -> Unit,
    viewModel: FansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId, teamName) {
        viewModel.loadFanData(teamId, teamName)
    }

    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        FansScreenContent(
            uiState = uiState,
            onBack = onBack,
            onRefresh = viewModel::refreshData
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FansScreenContent(
    uiState: FansUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Background {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "FAN VOICE",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    if (isLandscape) {
                        FansLandscapeLayout(uiState)
                    } else {
                        FansPortraitLayout(uiState)
                    }
                }
            }
        }
    }
}

@Composable
fun FansPortraitLayout(uiState: FansUiState) {
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
        item { FanSentimentCard(uiState.dashboard, uiState.expectations) }
        item { FanDistributionChart(uiState.dashboard) }
        item { SectionHeader("COMMUNITY FEED", Icons.Default.ChatBubble) }
        uiState.dashboard?.recentReactions?.let { reactions ->
            items(reactions, key = { it.id }) { reaction -> 
                FanReactionCard(reaction)
            }
        }
    }
}

@Composable
fun FansLandscapeLayout(uiState: FansUiState) {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Column
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FanSentimentCard(uiState.dashboard, uiState.expectations)
            FanDistributionChart(uiState.dashboard)
        }

        // Feed Column
        Column(modifier = Modifier.weight(1.2f)) {
            SectionHeader("COMMUNITY FEED", Icons.Default.ChatBubble)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                uiState.dashboard?.recentReactions?.let { reactions ->
                    items(reactions, key = { it.id }) { reaction -> 
                        FanReactionCard(reaction)
                    }
                }
            }
        }
    }
}

@Composable
fun FanDistributionChart(dashboard: FanReactionsDashboard?) {
    if (dashboard == null || dashboard.totalReactions == 0) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("SENTIMENT ANALYSIS", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.primary)
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setHoleColor(android.graphics.Color.TRANSPARENT)
                        setTransparentCircleColor(android.graphics.Color.TRANSPARENT)
                        setEntryLabelColor(android.graphics.Color.WHITE)
                        setEntryLabelTextSize(10f)
                        animateY(1000)
                    }
                },
                update = { chart ->
                    val entries = listOf(
                        PieEntry(dashboard.positiveReactions.toFloat(), "Positive"),
                        PieEntry(dashboard.neutralReactions.toFloat(), "Neutral"),
                        PieEntry(dashboard.negativeReactions.toFloat(), "Negative")
                    )
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = listOf(primaryColor.toArgb(), neutralColor.toArgb(), errorColor.toArgb())
                        sliceSpace = 2f
                        setDrawValues(false)
                    }
                    chart.data = PieData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun FanSentimentCard(dashboard: FanReactionsDashboard?, expectations: FanExpectationsDashboard?) {
    val sentimentColor = when {
        (dashboard?.sentimentScore ?: 0.0) >= 70 -> MaterialTheme.colorScheme.primary
        (dashboard?.sentimentScore ?: 0.0) >= 40 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FAN SATISFACTION", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.primary)
                Surface(color = sentimentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp), border = BorderStroke(0.5.dp, sentimentColor)) {
                    Text(
                        text = expectations?.overallMood?.uppercase() ?: "STABLE",
                        style = AFMTextStyles.statLabel,
                        color = sentimentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GLOBAL STANDING", style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurface)
                    Text("${dashboard?.sentimentScore?.toInt()}%", style = AFMTextStyles.statValue, color = sentimentColor)
                }
                LinearProgressIndicator(
                    progress = { (dashboard?.sentimentScore?.toFloat() ?: 0f) / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = sentimentColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SentimentMetric("CONFIDENCE", "${expectations?.confidenceLevel ?: 50}%", Icons.Default.Bolt, MaterialTheme.colorScheme.primary)
                SentimentMetric("BOARD TRUST", "${expectations?.boardTrust ?: 50}%", Icons.Default.Shield, MaterialTheme.colorScheme.primary)
                SentimentMetric("LOYALTY", "88%", Icons.Default.Favorite, MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun SentimentMetric(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(value, style = AFMTextStyles.statValue, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FanReactionCard(reaction: FanReactionsEntity) {
    val color = when (reaction.sentiment) {
        "Positive" -> MaterialTheme.colorScheme.primary
        "Negative" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Text("💬", fontSize = 20.sp) // Fallback emoji
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reaction.reaction, style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(reaction.timestamp ?: "Just now", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                Text(reaction.sentiment.uppercase(), style = AFMTextStyles.statLabel.copy(fontSize = 8.sp), color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Black)
    }
}

// ============ PREVIEWS ============

private fun getMockFansState() = FansUiState(
    isLoading = false,
    teamName = "Young Africans SC",
    dashboard = FanReactionsDashboard(
        sentimentScore = 82.0,
        positiveReactions = 450,
        neutralReactions = 120,
        negativeReactions = 30,
        totalReactions = 600,
        recentReactions = listOf(
            FanReactionsEntity(id = 1, teamId = 2, teamName = "Young Africans SC", reaction = "Great win today! The football was flowing.", sentiment = "Positive"),
            FanReactionsEntity(id = 2, teamId = 2, teamName = "Young Africans SC", reaction = "Another clean sheet. Defensive solidity is back.", sentiment = "Positive"),
            FanReactionsEntity(id = 3, teamId = 2, teamName = "Young Africans SC", reaction = "Tactics were okay, but we need more goals.", sentiment = "Neutral")
        ),
        teamName = "Young Africans SC"
    ),
    expectations = FanExpectationsDashboard(
        teamName = "Young Africans SC",
        overallMood = "Optimistic",
        confidenceLevel = 85,
        boardTrust = 90,
        confidenceLevelString = "High",
        trustLevelString = "Solid",
        isPositive = true,
        isNegative = false,
        isCritical = false
    )
)

@Preview(showBackground = true, name = "Fan Voice - Portrait")
@Composable
fun FansScreenPreview() {
    AFM2026Theme {
        FansScreenContent(
            uiState = getMockFansState(),
            onBack = {},
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, name = "Fan Voice - Landscape", device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
fun FansScreenLandscapePreview() {
    AFM2026Theme {
        FansScreenContent(
            uiState = getMockFansState(),
            onBack = {},
            onRefresh = {}
        )
    }
}
