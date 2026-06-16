package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.components.*
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.PlayerFilterPresetEntity
import com.fameafrica.afm.data.database.model.PlayerFilter
import com.fameafrica.afm.ui.common.*
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.formatters.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    onNegotiationClick: (Int) -> Unit,
    onPlayerClick: (Int) -> Unit,
    viewModel: TransfersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    TransfersScreenInternal(
        uiState = uiState,
        filter = filter,
        onPlayerClick = onPlayerClick,
        onTabSelected = viewModel::selectTab,
        onMarketViewChange = viewModel::selectMarketView,
        onRespondToOffer = viewModel::respondToOffer,
        onRemoveFromShortlist = viewModel::removeFromShortlist,
        onUpdateFilter = viewModel::updateFilter,
        onResetFilter = viewModel::resetFilter,
        onSavePreset = viewModel::saveCurrentFilter,
        onLoadPreset = viewModel::loadPreset,
        onDeletePreset = viewModel::deletePreset,
        onAdjustBudget = viewModel::adjustBudget,
        onScout = viewModel::addToShortlist, // Or specific scouting logic
        onBid = { /* TODO: Trigger Bid Dialog */ },
        onShortlist = viewModel::addToShortlist
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransfersScreenInternal(
    uiState: TransfersUiState,
    filter: PlayerFilter,
    onPlayerClick: (Int) -> Unit,
    onTabSelected: (Int) -> Unit,
    onMarketViewChange: (MarketView) -> Unit,
    onRespondToOffer: (Int, TransferAction) -> Unit,
    onRemoveFromShortlist: (Int) -> Unit,
    onUpdateFilter: (PlayerFilter) -> Unit,
    onResetFilter: () -> Unit,
    onSavePreset: (String) -> Unit,
    onLoadPreset: (PlayerFilterPresetEntity) -> Unit,
    onDeletePreset: (PlayerFilterPresetEntity) -> Unit,
    onAdjustBudget: (Float) -> Unit,
    onScout: (Int) -> Unit,
    onBid: (Int) -> Unit,
    onShortlist: (Int) -> Unit
) {
    AFM2026Theme(themePreset = FootballThemePreset.MANAGER_MODE) {
        FameBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { UniversalIdentityBanner(uiState) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    TransferStatsBar(uiState)
                    TransferWindowBanner(uiState)
                    TransferAlertsRow()
                    
                    Surface(color = FameColors.DeepNavyBlack) {
                        TransferMainTabs(uiState.selectedTab, onTabSelected)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = uiState.selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "TransferTabs"
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> TransferMarketTab(
                                    uiState = uiState,
                                    filter = filter,
                                    onPlayerClick = onPlayerClick,
                                    onSearchQueryChange = { name: String -> onUpdateFilter(filter.copy(name = name)) },
                                    onFilterClick = { /* TODO */ },
                                    onMarketViewChange = onMarketViewChange,
                                    selectedMarketView = uiState.selectedMarketView,
                                    onScout = onScout,
                                    onBid = onBid,
                                    onShortlist = onShortlist
                                )
                                1 -> ScoutingTab(uiState, { /* TODO: onAddInstruction */ }, onPlayerClick)
                                4 -> TransferHistoryTab(uiState)
                                5 -> BudgetManagementTab(uiState, onAdjustBudget)
                                else -> TransferMarketTab(
                                    uiState = uiState,
                                    filter = filter,
                                    onPlayerClick = onPlayerClick,
                                    onSearchQueryChange = { name: String -> onUpdateFilter(filter.copy(name = name)) },
                                    onFilterClick = { /* TODO */ },
                                    onMarketViewChange = onMarketViewChange,
                                    selectedMarketView = uiState.selectedMarketView,
                                    onScout = onScout,
                                    onBid = onBid,
                                    onShortlist = onShortlist
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UniversalIdentityBanner(uiState: TransfersUiState) {
    Surface(
        color = FameColors.DeepNavyBlack.copy(alpha = 0.98f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            // Main Banner Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manager/Chairman Portrait
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.5.dp, FameColors.TrophyGold.copy(alpha = 0.5f), CircleShape)
                ) {
                    AsyncImage(
                        model = uiState.managerPortrait,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.default_manager)
                    )
                    // Role Badge
                    Surface(
                        color = FameColors.TransferBlue,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 4.dp)
                    ) {
                        Text(
                            text = "Manager",
                            style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Black),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.currentTeamName,
                            style = AFMTextStyles.textMD,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ReputationBadge(reputation = uiState.reputation)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.season,
                            style = AFMTextStyles.textXXS,
                            color = FameColors.MutedParchment,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(FameColors.MutedParchment))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.teamLocation,
                            style = AFMTextStyles.textXXS,
                            color = FameColors.MutedParchment,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Club Crest
                AsyncImage(
                    model = uiState.teamLogo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    placeholder = painterResource(R.drawable.default_club)
                )
            }

            // Status Bar (Confidence & Form)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoardConfidenceWidget(confidence = uiState.boardConfidence, modifier = Modifier.weight(1f))
                FanSentimentWidget(sentiment = uiState.fanConfidence, modifier = Modifier.weight(1f))
                ClubFormWidget(form = uiState.clubForm)
            }
        }
    }
}

@Composable
fun ReputationStars(reputation: Int) {
    val stars = (reputation / 20).coerceIn(1, 5)
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = if (index < stars) FameColors.TrophyGold else Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun BoardConfidenceWidget(confidence: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Board", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("$confidence%", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = Color.White, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { confidence / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = if (confidence > 70) FameColors.GrowthGreen else if (confidence > 40) FameColors.TrophyGold else FameColors.AlertRed,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun FanSentimentWidget(sentiment: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fans", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("$sentiment%", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = Color.White, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { sentiment / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = if (sentiment > 70) FameColors.GrowthGreen else if (sentiment > 40) FameColors.TrophyGold else FameColors.AlertRed,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ClubFormWidget(form: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        form.takeLast(5).forEach { result ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when (result) {
                            "W" -> FameColors.GrowthGreen
                            "D" -> FameColors.TrophyGold
                            "L" -> FameColors.AlertRed
                            else -> Color.White.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result,
                    style = AFMTextStyles.textXXS.copy(fontSize = 8.sp, fontWeight = FontWeight.Black),
                    color = if (result == "D") Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun TransferStatsBar(uiState: TransfersUiState) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f))
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            DashboardStatItem(
                label = "Transfer Budget",
                value = formatCurrency(uiState.transferBudget, uiState.currencyContext),
                valueColor = FameColors.TrophyGold
            )
        }
        item {
            DashboardStatItem(
                label = "Wage Budget",
                value = "${formatCurrency(uiState.wageBudget, uiState.currencyContext)} /wk",
                valueColor = FameColors.Info
            )
        }
        item {
            DashboardStatItem(
                label = "Scouting Knowledge",
                value = "${uiState.scoutingKnowledge}%",
                icon = Icons.Default.Visibility,
                valueColor = Color.White
            )
        }
        item {
            CAFRegistrationWidget(count = uiState.registrationCount, limit = uiState.registrationLimit)
        }
    }
}

@Composable
fun DashboardStatItem(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    icon: ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label, 
            style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), 
            color = FameColors.MutedParchment, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(12.dp), tint = valueColor.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(value, style = AFMTextStyles.statValue.copy(fontSize = 14.sp), color = valueColor)
        }
    }
}

@Composable
fun CAFRegistrationWidget(count: Int, limit: Int) {
    Column(horizontalAlignment = Alignment.Start) {
        Text("CAF Registration", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Person,
                null,
                modifier = Modifier.size(12.dp),
                tint = FameColors.AfroSunOrange.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("$count / $limit", style = AFMTextStyles.statValue.copy(fontSize = 14.sp), color = FameColors.AfroSunOrange)
        }
    }
}

@Composable
fun TransferWindowBanner(uiState: TransfersUiState) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "TRANSFER WINDOW",
                        style = AFMTextStyles.textSM,
                        fontWeight = FontWeight.Black,
                        color = FameColors.TrophyGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = FameColors.GrowthGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            "OPEN",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = AFMTextStyles.textXXS,
                            color = FameColors.GrowthGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CountdownItem("12", "HRS")
                    Text(":", color = FameColors.TrophyGold, modifier = Modifier.padding(horizontal = 6.dp), fontWeight = FontWeight.Black)
                    CountdownItem("08", "MIN")
                    Text(":", color = FameColors.TrophyGold, modifier = Modifier.padding(horizontal = 6.dp), fontWeight = FontWeight.Black)
                    CountdownItem("47", "SEC")
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Column {
                        Text("DEADLINE DAY", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                        Text(uiState.deadlineDay.uppercase(), style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    }
                }
            }
            
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("WINDOW INFO", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun CountdownItem(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AFMTextStyles.textLG.copy(fontSize = 18.sp), color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Text(unit, style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransferAlertsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertItem(Icons.Default.Gavel, "Work Permit", "2", FameColors.AlertRed)
        AlertItem(Icons.Default.Public, "Foreign Limit", "3", FameColors.GrowthGreen)
        AlertItem(Icons.Default.Lock, "Expiring Contracts", "7", FameColors.TrophyGold)
        AlertItem(Icons.Default.Autorenew, "Loan Recalls", "1", FameColors.GrowthGreen)
        AlertItem(Icons.Default.Groups, "Player Demands", "4", FameColors.AlertRed)
        
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp), tint = FameColors.MutedParchment)
    }
}

@Composable
fun AlertItem(icon: ImageVector, label: String, count: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = FameColors.MutedParchment)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = AFMTextStyles.textXXS, color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(count, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AFMFilterChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}

@Composable
fun ShimmerPlayerCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.02f))
    )
}

@Composable
fun TransferHistoryTab(uiState: TransfersUiState) {
    val incoming = uiState.completedDeals.filter { it.toTeam == uiState.currentTeamName }
    val outgoing = uiState.completedDeals.filter { it.fromTeam == uiState.currentTeamName }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HistoryHeader() }
        
        item { 
            Spacer(modifier = Modifier.height(8.dp))
            Text("INCOMING", style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = FameColors.TrophyGold) 
        }
        
        items(incoming) { deal ->
            TransferHistoryCard(deal, uiState.currencyContext)
        }
        
        if (incoming.isEmpty()) {
            item { EmptyHistoryItem("No incoming transfers this window") }
        }

        item { 
            Spacer(modifier = Modifier.height(12.dp))
            Text("OUTGOING", style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = FameColors.AlertRed) 
        }
        
        items(outgoing) { deal ->
            TransferHistoryCard(deal, uiState.currencyContext)
        }
        
        if (outgoing.isEmpty()) {
            item { EmptyHistoryItem("No outgoing transfers this window") }
        }

        item { 
            Spacer(modifier = Modifier.height(12.dp))
            HistoryFinancialSummary(uiState) 
        }
    }
}

@Composable
fun BudgetManagementTab(uiState: TransfersUiState, onAdjustBudget: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BudgetOverviewCard(uiState)
        Spacer(modifier = Modifier.height(20.dp))
        TransferWageSlider(uiState, onAdjustBudget)
        Spacer(modifier = Modifier.height(24.dp))
        UpcomingRenewalsWidget(uiState)
        Spacer(modifier = Modifier.height(24.dp))
        AfricanEconomyFeaturesWidget(uiState)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TransferHistoryCard(deal: TransferOfferUiModel, currencyContext: CurrencyFormatter.CurrencyContext?) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    deal.playerName.uppercase(),
                    style = AFMTextStyles.textMD,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${deal.fromTeam} → ${deal.toTeam}",
                    style = AFMTextStyles.textXXS,
                    color = FameColors.MutedParchment,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatCompactCurrency(deal.fee, context = currencyContext),
                    style = AFMTextStyles.textMD,
                    color = FameColors.TrophyGold,
                    fontWeight = FontWeight.Black
                )
            }
            
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(deal.playerNationality, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(" • ", color = FameColors.MutedParchment)
                Text(deal.playerPosition, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(" • ", color = FameColors.MutedParchment)
                Text("${deal.contractYears} years", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(" • ", color = FameColors.MutedParchment)
                Text("${formatCompactCurrency(deal.weeklyWage, context = currencyContext)}/wk", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                
                Spacer(modifier = Modifier.weight(1f))
                
                RatingBadge(deal.potentialRange)
            }
            
            if (deal.installments > 0 || deal.isDelayedPayment || deal.sponsorContribution > 0 || deal.thirdPartyInterest > 0) {
                Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (deal.installments > 0) {
                        EconomyBadge("INSTALLMENTS: ${deal.installments}Y", FameColors.Info)
                    }
                    if (deal.isDelayedPayment) {
                        EconomyBadge("DELAYED PAYMENT", FameColors.TrophyGold)
                    }
                    if (deal.sponsorContribution > 0) {
                        EconomyBadge("SPONSOR FUNDED", FameColors.GrowthGreen)
                    }
                    if (deal.thirdPartyInterest > 0) {
                        EconomyBadge("TPO: ${deal.thirdPartyInterest}%", FameColors.AlertRed)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("TRANSFER HISTORY - Summer 2026 Window", style = AFMTextStyles.textSM, fontWeight = FontWeight.Bold, color = Color.White)
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(4.dp),
            onClick = { /* TODO */ }
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("FILTERS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Icon(Icons.Default.ArrowDropDown, null, tint = FameColors.MutedParchment, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HistoryFinancialSummary(uiState: TransfersUiState) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TOTAL SPENT", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(formatCompactCurrency(uiState.totalSpent, context = uiState.currencyContext), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
            }
            Column {
                Text("TOTAL RECEIVED", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(formatCompactCurrency(uiState.totalReceived, context = uiState.currencyContext), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NET SPEND", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(
                    formatCompactCurrency(uiState.netSpend, context = uiState.currencyContext),
                    style = AFMTextStyles.textMD,
                    color = if (uiState.netSpend >= 0) FameColors.GrowthGreen else FameColors.AlertRed,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun BudgetOverviewCard(uiState: TransfersUiState) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BUDGET OVERVIEW", style = AFMTextStyles.textSM, fontWeight = FontWeight.Black, color = Color.White)
                Text("ADJUST SLIDER", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            BudgetRow("TRANSFER BUDGET", formatCurrency(uiState.transferBudget, uiState.currencyContext), FameColors.TrophyGold)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetRow("WAGE BUDGET", "${formatCurrency(uiState.wageBudget, uiState.currencyContext)}/wk", FameColors.Info)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = Color.White.copy(alpha = 0.1f)
            )

            val wageUsage = if (uiState.wageBudget > 0) (uiState.currentWageSpend.toFloat() / uiState.wageBudget.toFloat()) else 0f
            BudgetRow("CURRENT WAGE SPEND", "${formatCurrency(uiState.currentWageSpend, uiState.currencyContext)}/wk (${(wageUsage * 100).toInt()}%)", Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            BudgetRow("AVAILABLE WAGE BUDGET", "${formatCurrency(uiState.wageBudget - uiState.currentWageSpend, uiState.currencyContext)}/wk", FameColors.GrowthGreen)
        }
    }
}

@Composable
fun BudgetRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AFMTextStyles.textXS, color = FameColors.MutedParchment)
        Text(value, style = AFMTextStyles.textXS, color = valueColor, fontWeight = FontWeight.Black)
    }
}

@Composable
fun TransferWageSlider(uiState: TransfersUiState, onAdjustBudget: (Float) -> Unit) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TRANSFER BUDGET", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text("WAGE BUDGET", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            
            Slider(
                value = sliderPosition,
                onValueChange = { 
                    sliderPosition = it
                    onAdjustBudget(it)
                },
                colors = SliderDefaults.colors(
                    thumbColor = FameColors.TrophyGold,
                    activeTrackColor = FameColors.TrophyGold,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatCompactCurrency(uiState.transferBudget, context = uiState.currencyContext), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text("${formatCompactCurrency(uiState.wageBudget, context = uiState.currencyContext)}/wk", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun UpcomingRenewalsWidget(uiState: TransfersUiState) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("UPCOMING CONTRACT RENEWALS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text("${uiState.upcomingRenewalsCount} players", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
            Text("Estimated additional wage: ${formatCompactCurrency(uiState.upcomingRenewalCost, context = uiState.currencyContext)}/wk", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("MANAGE RENEWALS", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AfricanEconomyFeaturesWidget(uiState: TransfersUiState) {
    Column {
        Text("AFRICAN ECONOMY INSIGHTS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EconomyFeatureCard("Sponsor-Backed", "Local petroleum giant funding strikers", Icons.Default.Business, Modifier.weight(1f))
            EconomyFeatureCard("Govt Influence", "Infrastructure grant approved", Icons.Default.AccountBalance, Modifier.weight(1f))
        }
    }
}

@Composable
fun EconomyFeatureCard(title: String, desc: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title.uppercase(), style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
                Text(desc, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun RatingBadge(range: String) {
    Surface(
        color = FameColors.GrowthGreen.copy(alpha = 0.1f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, FameColors.GrowthGreen.copy(alpha = 0.3f))
    ) {
        Text(
            "⭐$range",
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = AFMTextStyles.textXXS,
            color = FameColors.GrowthGreen,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun EconomyBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = AFMTextStyles.textXXS.copy(fontSize = 7.sp),
            color = color,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun EmptyHistoryItem(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = AFMTextStyles.textXXS, color = FameColors.DisabledText)
    }
}

@Composable
fun TransferMainTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        TabInfo("MARKET", Icons.Default.SwapHoriz),
        TabInfo("SCOUTING", Icons.Default.Search),
        TabInfo("SHORTLIST", Icons.Default.Star, "12"),
        TabInfo("NEGOTIATIONS", Icons.Default.Handshake, "3"),
        TabInfo("HISTORY", Icons.Default.History),
        TabInfo("FINANCES", Icons.Default.Payments)
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = FameColors.TrophyGold,
        edgePadding = 16.dp,
        divider = {},
        indicator = {}
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) FameColors.TransferBlue.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) FameColors.TransferBlue else Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Icon(
                            tab.icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) Color.White else FameColors.MutedParchment
                        )
                        if (tab.badge != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 10.dp, y = (-8).dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(FameColors.TrophyGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tab.badge, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        tab.label.uppercase(),
                        style = AFMTextStyles.textXXS.copy(fontSize = 8.sp),
                        color = if (isSelected) Color.White else FameColors.MutedParchment,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class TabInfo(val label: String, val icon: ImageVector, val badge: String? = null)

@Composable
fun TransferFilterButton() {
    Surface(
        onClick = { /* TODO */ },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("FILTERS", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp), tint = Color.White)
        }
    }
}

@Composable
fun TransferSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(44.dp),
        placeholder = { Text("Search player...", style = AFMTextStyles.textXS, color = FameColors.DisabledText) },
        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = FameColors.DisabledText) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = FameColors.TrophyGold
        ),
        shape = RoundedCornerShape(4.dp),
        textStyle = AFMTextStyles.textXS.copy(color = Color.White),
        singleLine = true
    )
}

@Composable
fun RegionSelector() {
    Surface(
        onClick = { /* TODO */ },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ALL REGIONS", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(14.dp), tint = Color.White)
        }
    }
}

@Composable
fun SortSelector() {
    Surface(
        onClick = { /* TODO */ },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("OVR", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(14.dp), tint = Color.White)
        }
    }
}

@Composable
fun RecruitmentSidebar(
    filter: PlayerFilter,
    onUpdateFilter: (PlayerFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SidebarSection("QUICK FILTERS") {
            QuickFilterItem("All Players", "12,458", false)
            QuickFilterItem("Top Deals", "1,234", true)
            QuickFilterItem("Wonderkids", "842", false)
            QuickFilterItem("Free Agents", "573", false)
            QuickFilterItem("African Talents", "3,245", false)
            QuickFilterItem("CAF Champions Lg", "1,012", false)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SidebarSection("REGIONS") {
            val context = LocalContext.current
            val packageName = context.packageName
            listOf(
                "East Africa" to "ic_eastafrica",
                "West Africa" to "ic_westafrica",
                "North Africa" to "ic_northafrica",
                "Central Africa" to "ic_centralafrica",
                "Southern Africa" to "ic_southernafrica"
            ).forEach { (label, resName) ->
                val iconRes = remember(resName) {
                    context.resources.getIdentifier(resName, "drawable", packageName)
                }
                RegionFilterItem(label, iconRes)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SidebarSection("POSITIONS") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PositionButton("GK", false, Modifier.weight(1f))
                PositionButton("DEF", false, Modifier.weight(1f))
                PositionButton("MID", false, Modifier.weight(1f))
                PositionButton("FWD", false, Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SidebarSection("AGE") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("16", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold)
                Text("35+", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold)
            }
            RangeSlider(
                value = 16f..35f,
                onValueChange = {},
                valueRange = 15f..45f,
                colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold, inactiveTrackColor = Color.White.copy(alpha = 0.1f))
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SidebarSection("VALUE") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$0", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$15M+", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Bold)
            }
            RangeSlider(
                value = 0f..15f,
                onValueChange = {},
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold, inactiveTrackColor = Color.White.copy(alpha = 0.1f))
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        SidebarSection("LEAGUES", hasAdd = true) {
            LeagueCheckbox("Tanzanian Premier League", true)
            LeagueCheckbox("CAF Champions League", true)
            LeagueCheckbox("Nigerian Professional Lg", false)
            LeagueCheckbox("Egyptian Premier League", false)
            LeagueCheckbox("Moroccan Botola", false)
            
            Text(
                "VIEW ALL LEAGUES",
                style = AFMTextStyles.textXXS,
                color = FameColors.TrophyGold,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SidebarSection(title: String, hasAdd: Boolean = false, content: @Composable () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = AFMTextStyles.textXXS.copy(fontSize = 9.sp), color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
            if (hasAdd) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = FameColors.MutedParchment)
            } else {
                Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(14.dp), tint = FameColors.MutedParchment)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun QuickFilterItem(label: String, count: String, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) FameColors.TrophyGold.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isSelected) Icons.Default.Star else Icons.Default.Groups,
            null,
            modifier = Modifier.size(14.dp),
            tint = if (isSelected) FameColors.TrophyGold else FameColors.MutedParchment
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            label,
            style = AFMTextStyles.textXXS,
            color = if (isSelected) FameColors.TrophyGold else Color.White,
            modifier = Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
        )
        Text(count, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontSize = 8.sp)
    }
}

@Composable
fun RegionFilterItem(label: String, iconRes: Int) {
    if (iconRes == 0) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PositionButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isSelected) FameColors.TrophyGold else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 6.dp),
            style = AFMTextStyles.textXXS.copy(fontSize = 8.sp),
            color = if (isSelected) Color.Black else FameColors.MutedParchment,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun LeagueCheckbox(label: String, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isChecked) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) Icon(Icons.Default.Check, null, modifier = Modifier.size(10.dp), tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun TransferPlayerCard(
    player: TransferPlayerUiModel,
    currencyContext: CurrencyFormatter.CurrencyContext?,
    onClick: () -> Unit,
    onScout: () -> Unit,
    onBid: () -> Unit,
    onShortlist: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Player Image Placeholder
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.BottomCenter).size(40.dp), tint = Color.White.copy(alpha = 0.2f))
                    if (player.isWonderkid) {
                        Surface(
                            color = FameColors.TransferBlue,
                            shape = CircleShape,
                            modifier = Modifier.align(Alignment.TopStart).offset(x = (-4).dp, y = (-4).dp).size(18.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.padding(3.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = AFMTextStyles.textSM,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(player.nationality, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                        Text(" • ", color = FameColors.MutedParchment, fontSize = 10.sp)
                        Text(player.position, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                        Text(" • ", color = FameColors.MutedParchment, fontSize = 10.sp)
                        Text(player.club, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = player.rating.toString(),
                        style = AFMTextStyles.textLG.copy(fontSize = 20.sp),
                        color = if (player.rating >= 75) FameColors.GrowthGreen else if (player.rating >= 65) FameColors.TrophyGold else Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text("Rating", style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.MutedParchment)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = Color.White.copy(alpha = 0.05f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Market Value", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment)
                    Text(
                        formatCompactCurrency(player.value, context = currencyContext),
                        style = AFMTextStyles.textXS,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Wage", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment)
                    Text(
                        "${formatCompactCurrency(player.wage, context = currencyContext)}/wk",
                        style = AFMTextStyles.textXS,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onShortlist,
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.StarBorder, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                    IconButton(
                        onClick = onScout,
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                    Surface(
                        color = FameColors.TransferBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        onClick = onBid
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BID", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingIndicator(rating: Double, color: Color, text: String) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Circle, null, modifier = Modifier.size(10.dp), tint = color)
            Spacer(modifier = Modifier.width(6.dp))
            Text(rating.toString(), style = AFMTextStyles.textXXS.copy(fontSize = 11.sp), color = color, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text.uppercase(), style = AFMTextStyles.textXXS.copy(fontSize = 9.sp), color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
        Text(value, style = AFMTextStyles.textLG.copy(fontSize = 20.sp), color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SmallInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.DisabledText, fontWeight = FontWeight.Bold)
        Text(value, style = AFMTextStyles.textXXS.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ViewMorePlayersButton(count: Int) {
    Surface(
        onClick = { /* TODO */ },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VIEW MORE PLAYERS", style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
                Text("$count players available", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
        }
    }
}

@Composable
fun TransferBottomWidgets(uiState: TransfersUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScoutingNetworkWidget()
        TopTargetsWidget()
        TransferNewsWidget()
    }
}

@Composable
fun ScoutingNetworkWidget(modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SCOUTING NETWORK", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
            
            Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                // Map Placeholder
                Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Public, null, modifier = Modifier.fillMaxSize(), tint = FameColors.GrowthGreen.copy(alpha = 0.2f))
                    // Pulsing dots would go here
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp).border(3.dp, FameColors.GrowthGreen, CircleShape)) {
                        Text("78%", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    }
                    Text("COVERAGE", style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment, modifier = Modifier.padding(top = 6.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendItem("Strong", FameColors.GrowthGreen)
                LegendItem("Average", FameColors.TrophyGold)
                LegendItem("Weak", FameColors.AlertRed)
            }
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TransferBlue),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("MANAGE SCOUTS", style = AFMTextStyles.textXXS, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.MutedParchment)
    }
}

@Composable
fun TopTargetsWidget(modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TOP TARGETS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
                Surface(color = FameColors.TrophyGold, shape = CircleShape) {
                    Text("12", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = AFMTextStyles.textXXS.copy(fontSize = 9.sp), color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            listOf(
                TargetInfo("I. Sangaré", "ASEC Mimosas", "78"),
                TargetInfo("A. Mahmoud", "Zamalek SC", "75"),
                TargetInfo("P. Mwangi", "Gor Mahia FC", "73")
            ).forEach { target ->
                Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(target.name, style = AFMTextStyles.textXXS.copy(fontSize = 10.sp), color = Color.White, fontWeight = FontWeight.Black)
                        Text(target.club, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.DisabledText, fontWeight = FontWeight.Bold)
                    }
                    Text(target.score, style = AFMTextStyles.textXXS.copy(fontSize = 11.sp), color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "VIEW SHORTLIST",
                style = AFMTextStyles.textXXS,
                color = FameColors.TrophyGold,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
}

data class TargetInfo(val name: String, val club: String, val score: String)

@Composable
fun TransferNewsWidget(modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TRANSFER NEWS", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment, fontWeight = FontWeight.Black)
                Text("See All", style = AFMTextStyles.textXXS, color = FameColors.DisabledText, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NewsItem("RUMOUR", "Al Ahly SC interested in Y. Belaili from MC Alger", "10m ago", Icons.Default.Campaign)
            NewsItem("OFFICIAL", "Simba SC sign Clatous Chama from TP Mazembe", "35m ago", Icons.Default.HistoryEdu)
            NewsItem("REPORT", "European scouts watching I. Sangaré (ASEC Mimosas)", "1h ago", Icons.Default.Visibility)
        }
    }
}

@Composable
fun NewsItem(tag: String, text: String, time: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = if(tag == "OFFICIAL") FameColors.GrowthGreen else if(tag == "RUMOUR") FameColors.AlertRed else FameColors.Info)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tag,
                    style = AFMTextStyles.textXXS.copy(fontSize = 8.sp),
                    color = if(tag == "OFFICIAL") FameColors.GrowthGreen else if(tag == "RUMOUR") FameColors.AlertRed else FameColors.Info,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(time, style = AFMTextStyles.textXXS.copy(fontSize = 8.sp), color = FameColors.DisabledText)
            }
            Text(text, style = AFMTextStyles.textXXS.copy(fontSize = 9.sp), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420", showSystemUi = true)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,dpi=420", showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TransfersScreenPreview() {
    AFM2026Theme {
        TransfersScreenInternal(
            uiState = TransfersUiState(
                currentTeamName = "Young Africans SC",
                teamLocation = "Dar Es Salaam, Tanzania",
                transferBudget = 2450000,
                wageBudget = 1120000,
                boardConfidence = 87,
                scoutingKnowledge = 78,
                registrationCount = 23,
                registrationLimit = 30,
                deadlineDay = "31 AUG 2025",
                currencyContext = null,
                marketPlayers = listOf(
                    TransferPlayerUiModel(
                        id = 1, name = "Ibrahim Sangaré", age = 21, position = "AM (RLC), ST",
                        nationality = "MLI", club = "ASEC Mimosas", rating = 78, potential = 89,
                        value = 1800000, wage = 18000, morale = 85, form = 8,
                        injuryStatus = "Healthy", role = "Inside Forward", height = 178, foot = "Right"
                    ),
                    TransferPlayerUiModel(
                        id = 2, name = "Abdallah Mahmoud", age = 23, position = "DM, M (C)",
                        nationality = "EGY", club = "Zamalek SC", rating = 75, potential = 85,
                        value = 1200000, wage = 16000, morale = 80, form = 7,
                        injuryStatus = "Healthy", role = "Deep Lying Playmaker", height = 182, foot = "Right"
                    )
                )
            ),
            filter = PlayerFilter(),
            onPlayerClick = {},
            onTabSelected = {},
            onRespondToOffer = { _, _ -> },
            onRemoveFromShortlist = {},
            onUpdateFilter = {},
            onResetFilter = {},
            onSavePreset = {},
            onLoadPreset = {},
            onDeletePreset = {},
            onAdjustBudget = {},
            onScout = {},
            onBid = {},
            onShortlist = {},
            onMarketViewChange = {}
        )
    }
}
