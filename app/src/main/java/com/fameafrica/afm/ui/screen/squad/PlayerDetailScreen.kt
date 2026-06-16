package com.fameafrica.afm.ui.screen.squad

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.ui.theme.FameColors
import androidx.compose.ui.graphics.painter.Painter
import com.fameafrica.afm.data.repository.PlayerReactionsDashboard
import com.fameafrica.afm.data.repository.TeamContractDashboard
import com.fameafrica.afm.data.repository.TeamForm
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FootballThemePreset
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.screen.transfers.TransferOfferDetails
import com.fameafrica.afm.utils.PlayerAssetUtils
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.ui.viewinterop.AndroidView
import com.fameafrica.afm.utils.NationalityUtils
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import com.fameafrica.afm.ui.components.RatingBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    playerId: Int,
    onBack: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    viewModel: PlayerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showContractDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showSquadStatusDialog by remember { mutableStateOf(false) }
    var showBidDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playerId) {
        viewModel.loadPlayer(playerId)
    }

    AFM2026Theme(themePreset = FootballThemePreset.CHAIRMAN_MODE) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column(
                    modifier = Modifier
                        .background(FameColors.HeaderDark)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        if (uiState.player != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.player!!.name.uppercase(),
                                    style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${uiState.player!!.position} • #${uiState.player!!.shirtNumber}".uppercase(),
                                    style = AFMTextStyles.textXS,
                                    color = FameColors.TrophyGold
                                )
                            }
                        } else {
                            Text("PLAYER DETAILS", style = AFMTextStyles.textMD, color = Color.White)
                        }

                        if (uiState.player != null) {
                            IconButton(onClick = { viewModel.toggleShortlist() }) {
                                Icon(
                                    if (uiState.isShortlisted) Default.Star else Default.StarBorder,
                                    contentDescription = "Shortlist",
                                    tint = if (uiState.isShortlisted) FameColors.TrophyGold else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            if (uiState.isUserPlayer && !uiState.player!!.isCaptain) {
                                IconButton(onClick = { showTransferDialog = true }) {
                                    Icon(Default.SwapHoriz, contentDescription = "Transfer", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            } else if (!uiState.isUserPlayer) {
                                IconButton(onClick = { showBidDialog = true }) {
                                    Icon(Default.AddShoppingCart, contentDescription = "Make Offer", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            if (uiState.isUserPlayer) {
                                IconButton(onClick = { showContractDialog = true }) {
                                    Icon(Default.Description, contentDescription = "Contract", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = FameColors.TrophyGold,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 2.dp,
                                color = FameColors.TrophyGold
                            )
                        }
                    ) {
                        listOf("ATTRIBUTES", "STATISTICS", "FORM", "CAREER", "RELATIONS").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                        color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (uiState.isUserPlayer && uiState.playerStatus?.squadStatus != SquadStatus.SURPLUS) {
                    FloatingActionButton(
                        onClick = { showSquadStatusDialog = true },
                        containerColor = FameColors.TrophyGold,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("MANAGE PLAYER", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black))
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FameColors.TrophyGold
                    )
                } else if (uiState.player == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PLAYER NOT FOUND",
                            style = AFMTextStyles.textMD,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp, start = 12.dp, end = 12.dp, top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            PlayerHeroSection(
                                player = uiState.player!!,
                                playerStatus = uiState.playerStatus,
                                teamForm = uiState.teamForm,
                                currentGameDate = uiState.currentGameDate
                            )
                        }

                        item {
                            QuickStatsRow(
                                condition = uiState.playerStatus?.condition ?: 0,
                                fatigue = uiState.playerStatus?.fatigue ?: 0,
                                form = uiState.player!!.form,
                                morale = uiState.player!!.morale,
                                formattedValue = uiState.player!!.formattedValue
                            )
                        }

                        item {
                            DevelopmentArcSection(potential = uiState.player!!.potential, rating = uiState.player!!.overallRating)
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    BoardConfidenceWidget(
                                        confidence = 85, // Mock for now
                                        pressure = "LOW",
                                        expectation = "Essential Player"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    FanSentimentWidget(
                                        happiness = (uiState.player!!.morale),
                                        sentimentText = "Fan Favorite"
                                    )
                                }
                            }
                        }

                        item {
                            PersonalitySection(personality = uiState.player!!.personality)
                        }

                        item {
                            FanStatusSection()
                        }

                        when (selectedTab) {
                            0 -> {
                                item { 
                                    AttributeSection(
                                        attributes = uiState.attributes, 
                                        attributeTrends = uiState.attributeTrends,
                                        positionCategory = uiState.player!!.positionCategory
                                    ) 
                                }
                                item { TraitSection(player = uiState.player!!) }
                            }
                            1 -> {
                                item { StatisticsSection(stats = uiState.seasonStats, player = uiState.player!!) }
                                item { RecentMatchesSection(matches = uiState.recentMatches) }
                            }
                            2 -> {
                                item { FormHistorySection(formHistory = uiState.formHistory) }
                                item { InjuryHistorySection(injuries = uiState.injuryHistory, currentGameDate = uiState.currentGameDate) }
                            }
                            3 -> {
                                item { CareerAwardsSection(awards = uiState.careerAwards) }
                                item { TransferInterestSection(interests = uiState.transferInterest) }
                            }
                            4 -> {
                                item { MediaSection(interviews = uiState.recentInterviews, reactions = uiState.playerReactions) }
                            }
                        }

                        item {
                            ContractInfoSection(
                                contract = uiState.contract,
                                contractDashboard = uiState.contractDashboard,
                                agent = uiState.agent
                            )
                        }

                        if (uiState.activeLoan != null) {
                            item {
                                LoanInfoSection(loan = uiState.activeLoan!!)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showContractDialog && uiState.player != null) {
        ContractDialog(
            player = uiState.player!!,
            currentContract = uiState.contract,
            onDismiss = { showContractDialog = false },
            onRenew = { wage, years, bonus ->
                viewModel.offerNewContract(uiState.player!!.id, wage, years, bonus)
                showContractDialog = false
            },
            formatCurrency = viewModel::formatCurrency
        )
    }

    if (showTransferDialog && uiState.player != null) {
        TransferDialog(
            player = uiState.player!!,
            onDismiss = { showTransferDialog = false },
            onListPlayer = { price ->
                viewModel.addToTransferList(uiState.player!!.id, price.toLong())
                showTransferDialog = false
            },
            onRemoveFromList = {
                viewModel.removeFromTransferList(uiState.player!!.id)
                showTransferDialog = false
            },
            formatCurrency = viewModel::formatCurrency
        )
    }

    if (showSquadStatusDialog && uiState.player != null) {
        SquadStatusDialog(
            currentStatus = uiState.playerStatus?.squadStatus ?: SquadStatus.ROTATION,
            onDismiss = { showSquadStatusDialog = false },
            onStatusChange = { status ->
                viewModel.setSquadStatus(uiState.player!!.id, status)
                showSquadStatusDialog = false
            }
        )
    }

    if (showBidDialog && uiState.player != null) {
        BidDialog(
            player = uiState.player!!,
            onDismiss = { showBidDialog = false },
            onSubmitBid = { offer ->
                viewModel.submitTransferOffer(offer)
                showBidDialog = false
            },
            formatCurrency = viewModel::formatCurrency
        )
    }
}

@Composable
fun PlayerHeroSection(
    player: PlayerDetailUiModel,
    playerStatus: PlayerStatusUiModel?,
    teamForm: TeamForm?,
    currentGameDate: String
) {
    val ratingColor = when {
        player.overallRating >= 80 -> FameColors.GrowthGreen
        player.overallRating >= 65 -> FameColors.TrophyGold
        player.overallRating >= 50 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }

    val faceUrl = PlayerAssetUtils.getPlayerFace(player.id, player.nationality)
    val regionCardBg = PlayerAssetUtils.getRegionCardBackground(player.nationality)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Large Background Region Card
        AsyncImage(
            model = regionCardBg,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Player Face with Glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, ratingColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            ) {
                AsyncImage(
                    model = faceUrl,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Nationality Overlay
                AsyncImage(
                    model = NationalityUtils.getFlagUrl(player.nationality),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                Text(
                    text = player.name.uppercase(),
                    style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black, fontSize = 24.sp),
                    color = Color.White
                )
                Text(
                    text = "${player.position} • ${player.nationality.uppercase()}",
                    style = AFMTextStyles.textSM,
                    color = FameColors.TrophyGold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBadge(
                        rating = player.overallRating,
                        textStyle = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (player.isCaptain) {
                        Icon(Default.Stars, "Captain", tint = FameColors.TrophyGold, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun DevelopmentArcSection(potential: Int, rating: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    SidebarCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DEVELOPMENT ARC", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("STAR PROGRESSION", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    StarProgression(rating)
                }
            }
            RatingBadge(rating = potential)
        }
        
        Spacer(Modifier.height(12.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f))) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(rating / 100f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(FameColors.GrowthGreen.copy(alpha = 0.7f), FameColors.GrowthGreen)
                        )
                    )
            )
            // Potential overlay
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(potential / 100f)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            )
        }
        
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("CURRENT: $rating", style = AFMTextStyles.textXXS, color = Color.White.copy(alpha = 0.6f))
            Text("CEILING: $potential", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold)
        }
    }
}

@Composable
fun StarProgression(rating: Int) {
    val stars = (rating / 20f).coerceIn(0f, 5f)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            val starIndex = index + 1
            val icon = when {
                stars >= starIndex -> Default.Star
                stars >= starIndex - 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = FameColors.TrophyGold
            )
        }
    }
}

@Composable
fun BoardConfidenceWidget(confidence: Int, pressure: String, expectation: String) {
    Surface(
        color = FameColors.HeaderDark.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("BOARD CONFIDENCE", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$confidence%", style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black), color = Color.White)
                Spacer(Modifier.width(8.dp))
                StatusBadge(pressure, if (pressure == "LOW") FameColors.GrowthGreen else FameColors.AlertRed)
            }
            Text(expectation.uppercase(), style = AFMTextStyles.textXXS, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun FanSentimentWidget(happiness: Int, sentimentText: String) {
    Surface(
        color = FameColors.HeaderDark.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("FAN SENTIMENT", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FameColors.AlertRed
                )
                Spacer(Modifier.width(8.dp))
                Text("$happiness%", style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
            Text(sentimentText.uppercase(), style = AFMTextStyles.textXXS, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun PersonalitySection(personality: String) {
    SidebarCard {
        Text("PERSONALITY & LEADERSHIP", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(personality.uppercase(), style = AFMTextStyles.textMD, color = Color.White, fontWeight = FontWeight.Black)
        Text("Highly ambitious and disciplined professional.", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun FanStatusSection() {
    SidebarCard {
        Text("MEDIA & FAN STATUS", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge("FAN FAVORITE", FameColors.PitchGreen)
            StatusBadge("WONDERKID", FameColors.ChampionsGold)
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color), shape = RoundedCornerShape(2.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, fontSize = 9.sp), color = color)
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            } else if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun QuickStatsRow(condition: Int, fatigue: Int, form: Int, morale: Int, formattedValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatItem(
            icon = Default.FitnessCenter,
            label = "Condition",
            value = condition.toString(),
            unit = "%",
            color = getConditionColor(condition)
        )
        QuickStatItem(
            icon = Default.BatteryAlert,
            label = "Fatigue",
            value = fatigue.toString(),
            unit = "%",
            color = getFatigueColor(fatigue)
        )
        QuickStatItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "Form",
            value = form.toString(),
            unit = "/10",
            color = getFormColor(form)
        )
        QuickStatItem(
            icon = Default.SentimentSatisfied,
            label = "Morale",
            value = morale.toString(),
            unit = "%",
            color = getMoraleColor(morale)
        )
        QuickStatItem(
            icon = Default.AttachMoney,
            label = "Value",
            value = formattedValue,
            unit = "",
            color = Color(0xFFFFC107)
        )
    }
}

@Composable
fun RowScope.QuickStatItem(icon: ImageVector, label: String, value: String, unit: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value$unit",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AttributeSection(
    attributes: PlayerAttributesUiModel?,
    attributeTrends: Map<String, AttributeTrend>,
    positionCategory: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = FameColors.HeaderDark.copy(alpha = 0.4f),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left side: Radar Chart
                Box(modifier = Modifier.weight(1f).height(200.dp)) {
                    if (attributes != null) {
                        PlayerRadarChart(attributes)
                    }
                }
                
                // Right side: Group summary
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TECH CAP", style = AFMTextStyles.textXS, color = FameColors.ChampionsGold, fontWeight = FontWeight.Black)
                    AttributeMiniRow("FINISHING", attributes?.finishing ?: 0)
                    AttributeMiniRow("PASSING", attributes?.passing ?: 0)
                    AttributeMiniRow("DRIBBLING", attributes?.dribbling ?: 0)
                    AttributeMiniRow("DEFENDING", attributes?.defending ?: 0)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (attributes != null) {
                // FM Style Grid of attributes
                AttributeGridDetailed(attributes, attributeTrends)
            }
        }
    }
}

@Composable
fun PlayerRadarChart(attr: PlayerAttributesUiModel) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.textColor = android.graphics.Color.WHITE
                xAxis.textSize = 6f
                yAxis.isEnabled = false
                webColor = android.graphics.Color.DKGRAY
                webColorInner = android.graphics.Color.GRAY
                webAlpha = 100
                setTouchEnabled(false)
            }
        },
        update = { chart: RadarChart ->
            val entries = listOf(
                RadarEntry(attr.finishing.toFloat()),
                RadarEntry(attr.passing.toFloat()),
                RadarEntry(attr.pace.toFloat()),
                RadarEntry(attr.defending.toFloat()),
                RadarEntry(attr.stamina.toFloat()),
                RadarEntry(attr.vision.toFloat())
            )
            val dataSet = RadarDataSet(entries, "").apply {
                setColor(primaryColor)
                setDrawFilled(true)
                setFillColor(primaryColor)
                setFillAlpha(120)
                lineWidth = 2f
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(listOf("FIN", "PAS", "PAC", "DEF", "STA", "VIS"))
            chart.data = RadarData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun AttributeMiniRow(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 8.sp, color = Color.White.copy(0.4f), modifier = Modifier.weight(1f))
        Text(value.toString(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = getAttributeColor(value))
    }
}

@Composable
fun AttributeGridDetailed(attr: PlayerAttributesUiModel, trends: Map<String, AttributeTrend>) {
    // FM Style 3-column attribute layout
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            AttributeSubHeader("TECHNICAL")
            AttributefmRow("Crossing", attr.crossing)
            AttributefmRow("Dribbling", attr.dribbling)
            AttributefmRow("Finishing", attr.finishing)
            AttributefmRow("Heading", attr.heading)
            AttributefmRow("Passing", attr.passing)
            AttributefmRow("Defending", attr.defending)
        }
        Column(modifier = Modifier.weight(1f)) {
            AttributeSubHeader("MENTAL")
            AttributefmRow("Aggression", attr.aggression)
            AttributefmRow("Anticipation", attr.anticipation)
            AttributefmRow("Composure", attr.composure)
            AttributefmRow("Decisions", attr.decisions)
            AttributefmRow("Leadership", attr.leadership)
            AttributefmRow("Vision", attr.vision)
        }
        Column(modifier = Modifier.weight(1f)) {
            AttributeSubHeader("PHYSICAL")
            AttributefmRow("Acceleration", attr.acceleration)
            AttributefmRow("Agility", attr.agility)
            AttributefmRow("Pace", attr.pace)
            AttributefmRow("Stamina", attr.stamina)
            AttributefmRow("Strength", attr.strength)
        }
    }
}

@Composable
fun AttributeSubHeader(label: String) {
    Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
fun AttributefmRow(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
        Text(
            value.toString(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            modifier = Modifier.background(getAttributeColor(value), RoundedCornerShape(1.dp)).padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun AttributeGroup(
    title: String,
    attributes: List<Pair<String, Int>>,
    trends: Map<String, AttributeTrend>
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        attributes.forEach { (name, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontSize = 12.sp,
                    modifier = Modifier.width(100.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(value / 100f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(getAttributeColor(value))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$value",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End
                )

                trends[name.lowercase()]?.let { trend ->
                    Icon(
                        when (trend.trend) {
                            TrendDirection.IMPROVING -> Default.ArrowUpward
                            TrendDirection.DECLINING -> Default.ArrowDownward
                            else -> Default.Remove
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = when (trend.trend) {
                            TrendDirection.IMPROVING -> Color(0xFF4CAF50)
                            TrendDirection.DECLINING -> Color(0xFFF44336)
                            else -> Color(0xFFFF9800)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TraitSection(player: PlayerDetailUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personality & Traits",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TraitChip(
                    icon = Default.Person,
                    label = "Personality",
                    value = player.personality
                )
                player.archetype?.let {
                    TraitChip(
                        icon = Default.Star,
                        label = "Archetype",
                        value = it
                    )
                }
                TraitChip(
                    icon = Default.Pets,
                    label = "Preferred Foot",
                    value = player.preferredFoot
                )
            }
        }
    }
}

@Composable
fun RowScope.TraitChip(icon: ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatisticsSection(stats: SeasonStatsUiModel?, player: PlayerDetailUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Season Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (stats != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Appearances",
                        value = stats.matches.toString(),
                        icon = Default.SportsSoccer
                    )
                    StatCard(
                        label = "Goals",
                        value = stats.goals.toString(),
                        icon = Default.RadioButtonChecked
                    )
                    StatCard(
                        label = "Assists",
                        value = stats.assists.toString(),
                        icon = Default.Handshake
                    )
                    StatCard(
                        label = "Man of Match",
                        value = stats.manOfMatch.toString(),
                        icon = Default.Star
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Yellow Cards",
                        value = stats.yellowCards.toString(),
                        icon = Default.Warning
                    )
                    StatCard(
                        label = "Red Cards",
                        value = stats.redCards.toString(),
                        icon = Default.Dangerous
                    )
                    StatCard(
                        label = "Pass Accuracy",
                        value = "${stats.passAccuracy}%",
                        icon = Icons.AutoMirrored.Filled.CompareArrows
                    )
                    StatCard(
                        label = "Goal Conv.",
                        value = "${stats.goalConversionRate}%",
                        icon = Default.Adjust
                    )
                }
            } else {
                Text(
                    text = "No statistics available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RowScope.StatCard(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentMatchesSection(matches: List<MatchPerformanceUiModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Matches",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (matches.isNotEmpty()) {
                matches.take(5).forEach { match ->
                    MatchRow(match = match)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "No match data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MatchRow(match: MatchPerformanceUiModel) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.opponent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = match.date,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.goals > 0) {
                    MatchStatChip(
                        icon = Default.RadioButtonChecked,
                        value = match.goals.toString()
                    )
                }
                if (match.assists > 0) {
                    MatchStatChip(
                        icon = Default.Handshake,
                        value = match.assists.toString()
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getRatingColor(match.rating * 10)
                ) {
                    Text(
                        text = "${match.rating}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (match.motm) {
                    Icon(
                        Default.Star,
                        contentDescription = "Man of the Match",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MatchStatChip(icon: ImageVector, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FormHistorySection(formHistory: List<FormHistoryEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Form",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (formHistory.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(formHistory.take(10)) { match ->
                        FormCard(match = match)
                    }
                }
            } else {
                Text(
                    text = "No form history available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FormCard(match: FormHistoryEntry) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = getRatingColor(match.rating * 10),
        modifier = Modifier.size(60.dp, 80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${match.rating}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = match.opponent.take(3),
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            if (match.goals > 0 || match.assists > 0) {
                Text(
                    text = "${match.goals}G ${match.assists}A",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun InjuryHistorySection(injuries: List<InjuryUiModel>, currentGameDate: String) {
    if (injuries.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Injury History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                injuries.forEach { injury ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = injury.type,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Recovery: ${injury.recoveryTime} days",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "As of: $currentGameDate",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CareerAwardsSection(awards: List<AwardUiModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Career Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (awards.isNotEmpty()) {
                awards.forEach { award ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = award.awardType,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = award.category,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = award.season,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = "No awards yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TransferInterestSection(interests: List<TransferInterestUiModel>) {
    if (interests.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Transfer Interest",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                interests.forEach { interest ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = interest.clubName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = interest.formattedAmount,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFC107)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (interest.status) {
                                    InterestStatus.ACCEPTED -> Color(0xFF4CAF50)
                                    InterestStatus.NEGOTIATING -> Color(0xFFFF9800)
                                    else -> Color(0xFF9E9E9E)
                                }.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = interest.status.name,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = when (interest.status) {
                                        InterestStatus.ACCEPTED -> Color(0xFF4CAF50)
                                        InterestStatus.NEGOTIATING -> Color(0xFFFF9800)
                                        else -> Color(0xFF9E9E9E)
                                    }
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
fun MediaSection(interviews: List<InterviewUiModel>, reactions: PlayerReactionsDashboard?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Media & Reactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (interviews.isNotEmpty()) {
                Text(
                    text = "Recent Interviews",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                interviews.forEach { interview ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = interview.journalistName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = interview.date,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = interview.topic,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (interview.impactOnMorale != 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        if (interview.impactOnMorale > 0) Default.ArrowUpward else Default.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (interview.impactOnMorale > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                    Text(
                                        text = "Morale ${if (interview.impactOnMorale > 0) "+" else ""}${interview.impactOnMorale}%",
                                        fontSize = 10.sp,
                                        color = if (interview.impactOnMorale > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            reactions?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fan Sentiment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SentimentCard(
                        label = "Positive",
                        value = it.positiveReactions,
                        color = Color(0xFF4CAF50)
                    )
                    SentimentCard(
                        label = "Neutral",
                        value = it.neutralReactions,
                        color = Color(0xFFFF9800)
                    )
                    SentimentCard(
                        label = "Negative",
                        value = it.negativeReactions,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.SentimentCard(label: String, value: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}

@Composable
fun ContractInfoSection(
    contract: ContractUiModel?,
    contractDashboard: TeamContractDashboard?,
    agent: AgentUiModel?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Contract Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (contract != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Weekly Wage",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contract.formattedSalary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "Expires",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contract.expiry,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (contract.isExpiring) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column {
                        Text(
                            text = "Release Clause",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = contract.formattedReleaseClause,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            agent?.let {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Agent: ${it.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${it.agency} • ${it.yearsExperience} years experience",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Negotiation Power: ${it.negotiationPower}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LoanInfoSection(loan: LoanUiModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "Out on Loan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Loaned to: ${loan.receivingTeam}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Duration: ${loan.duration} months",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (loan.optionToBuy) {
                Text(
                    text = "Option to buy: ${loan.formattedBuyOptionFee}",
                    fontSize = 12.sp,
                    color = Color(0xFFFFC107)
                )
            }
        }
    }
}

// Helper functions
fun getRatingColor(rating: Int): Color {
    return when (rating) {
        in 0..40 -> FameColors.KenteRed
        in 41..60 -> FameColors.AfroSunOrange
        in 61..79 -> FameColors.ChampionsGold
        else -> FameColors.Success
    }
}

fun getAttributeColor(value: Int): Color {
    return when (value) {
        in 0..49 -> FameColors.KenteRed
        in 50..64 -> FameColors.AfroSunOrange
        in 65..79 -> FameColors.ChampionsGold
        else -> FameColors.Success
    }
}

fun getConditionColor(condition: Int): Color {
    return when {
        condition >= 80 -> FameColors.GrowthGreen
        condition >= 65 -> FameColors.GrowthGreen.copy(alpha = 0.7f)
        condition >= 50 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}

fun getFatigueColor(fatigue: Int): Color {
    return when {
        fatigue <= 20 -> FameColors.GrowthGreen
        fatigue <= 40 -> FameColors.GrowthGreen.copy(alpha = 0.7f)
        fatigue <= 60 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}

fun getFormColor(form: Int): Color {
    return when (form) {
        in 8..10 -> FameColors.GrowthGreen
        in 6..7 -> FameColors.GrowthGreen.copy(alpha = 0.7f)
        in 4..5 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}

fun getMoraleColor(morale: Int): Color {
    return when {
        morale >= 70 -> FameColors.GrowthGreen
        morale >= 50 -> FameColors.GrowthGreen.copy(alpha = 0.7f)
        morale >= 30 -> FameColors.AfroSunOrange
        else -> FameColors.AlertRed
    }
}

@Composable
fun ContractDialog(
    player: PlayerDetailUiModel,
    currentContract: ContractUiModel?,
    onDismiss: () -> Unit,
    onRenew: (Double, Int, Int) -> Unit,
    formatCurrency: (Double) -> String
) {
    var wage by remember { mutableStateOf(currentContract?.salary?.toDouble() ?: player.wage) }
    var years by remember { mutableStateOf(3) }
    var bonus by remember { mutableStateOf(50000) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = { 
            Text(
                "RENEW CONTRACT", 
                style = AFMTextStyles.textLG, 
                color = FameColors.TrophyGold, 
                fontWeight = FontWeight.Black
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SidebarCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("PLAYER", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
                        Text(player.name.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("CURRENT WAGE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
                        Text(player.formattedWage.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }

                Column {
                    Text("NEW WEEKLY WAGE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = (wage / 1000).toFloat(),
                        onValueChange = { wage = (it * 1000).toDouble() },
                        valueRange = (player.wage / 1000).toFloat()..(player.wage / 1000 * 5).toFloat(),
                        steps = 50,
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text(formatCurrency(wage).uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Column {
                    Text("CONTRACT LENGTH (YEARS)", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = years.toFloat(),
                        onValueChange = { years = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text("$years YEARS", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Column {
                    Text("SIGNING BONUS", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = (bonus / 1000).toFloat(),
                        onValueChange = { bonus = it.toInt() * 1000 },
                        valueRange = 0f..1000f,
                        steps = 100,
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text(formatCurrency(bonus.toDouble()).uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRenew(wage, years, bonus) },
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("OFFER CONTRACT", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = AFMTextStyles.textXS, color = FameColors.AlertRed, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun TransferDialog(
    player: PlayerDetailUiModel,
    onDismiss: () -> Unit,
    onListPlayer: (Int) -> Unit,
    onRemoveFromList: () -> Unit,
    formatCurrency: (Double) -> String
) {
    var askingPrice by remember { mutableStateOf(player.marketValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = { 
            Text(
                "TRANSFER OPTIONS", 
                style = AFMTextStyles.textLG, 
                color = FameColors.TrophyGold, 
                fontWeight = FontWeight.Black
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SidebarCard {
                    Column {
                        Text("CURRENT VALUE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
                        Text(player.formattedValue.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }

                Column {
                    Text("ASKING PRICE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = (askingPrice / 1_000).toFloat(),
                        onValueChange = { askingPrice = (it * 1_000).toInt() },
                        valueRange = (player.marketValue / 2000f)..(player.marketValue * 5 / 1000f),
                        steps = 100,
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text(formatCurrency(askingPrice.toDouble()).uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Button(
                    onClick = { onRemoveFromList() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, FameColors.AlertRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("REMOVE FROM TRANSFER LIST", style = AFMTextStyles.textXS, color = FameColors.AlertRed, fontWeight = FontWeight.Black)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onListPlayer(askingPrice) },
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("LIST FOR TRANSFER", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun SquadStatusDialog(
    currentStatus: SquadStatus,
    onDismiss: () -> Unit,
    onStatusChange: (SquadStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = { 
            Text(
                "SQUAD STATUS", 
                style = AFMTextStyles.textLG, 
                color = FameColors.TrophyGold, 
                fontWeight = FontWeight.Black
            ) 
        },
        text = {
            Column {
                SquadStatus.entries.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatusChange(status) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentStatus == status,
                            onClick = { onStatusChange(status) },
                            colors = RadioButtonDefaults.colors(selectedColor = FameColors.TrophyGold, unselectedColor = Color.White.copy(alpha = 0.4f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = status.name.replace("_", " "),
                                style = AFMTextStyles.textSM,
                                color = if (currentStatus == status) FameColors.TrophyGold else Color.White,
                                fontWeight = if (currentStatus == status) FontWeight.Black else FontWeight.Bold
                            )
                            Text(
                                text = getSquadStatusDescription(status).uppercase(),
                                style = AFMTextStyles.textXS,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
            }
        }
    )
}

@Composable
fun BidDialog(
    player: PlayerDetailUiModel,
    onDismiss: () -> Unit,
    onSubmitBid: (TransferOfferDetails) -> Unit,
    formatCurrency: (Double) -> String
) {
    var fee by remember { mutableLongStateOf(player.marketValue.toLong()) }
    var wage by remember { mutableLongStateOf(player.wage.toLong()) }
    var years by remember { mutableIntStateOf(3) }
    var type by remember { mutableStateOf(TransferType.BUY) }
    var squadRole by remember { mutableStateOf(SquadRole.ROTATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = {
            Text(
                "TRANSFER OFFER",
                style = AFMTextStyles.textLG,
                color = FameColors.TrophyGold,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SidebarCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TARGET PLAYER", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
                        Text(player.name.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ESTIMATED VALUE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
                        Text(player.formattedValue.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }

                Column {
                    Text("OFFER TYPE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TransferType.entries.filter { it != TransferType.FREE }.forEach { t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { type = t }
                            ) {
                                RadioButton(
                                    selected = type == t,
                                    onClick = { type = t },
                                    colors = RadioButtonDefaults.colors(selectedColor = FameColors.TrophyGold, unselectedColor = Color.White.copy(alpha = 0.4f))
                                )
                                Text(
                                    t.value.uppercase(),
                                    style = AFMTextStyles.textXS,
                                    color = if (type == t) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                if (type == TransferType.BUY) {
                    Column {
                        Text("TRANSFER FEE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                        Slider(
                            value = fee.toFloat(),
                            onValueChange = { fee = it.toLong() },
                            valueRange = (player.marketValue * 0.5f)..(player.marketValue * 2.0f),
                            colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                        )
                        Text(formatCurrency(fee.toDouble()).uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                Column {
                    Text("WEEKLY WAGE OFFER", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = wage.toFloat(),
                        onValueChange = { wage = it.toLong() },
                        valueRange = (player.wage * 0.5f).toFloat()..(player.wage * 3.0f).toFloat(),
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text(formatCurrency(wage.toDouble()).uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Column {
                    Text("CONTRACT LENGTH", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Slider(
                        value = years.toFloat(),
                        onValueChange = { years = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = FameColors.TrophyGold, activeTrackColor = FameColors.TrophyGold)
                    )
                    Text("$years YEARS", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Column {
                    Text("PROPOSED SQUAD ROLE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    SquadRole.entries.forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { squadRole = role }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = squadRole == role,
                                onClick = { squadRole = role },
                                colors = RadioButtonDefaults.colors(selectedColor = FameColors.TrophyGold, unselectedColor = Color.White.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                role.value.uppercase(),
                                style = AFMTextStyles.textXS,
                                color = if (squadRole == role) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitBid(TransferOfferDetails(type, fee, wage, years, role = squadRole)) },
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("SUBMIT OFFER", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Black)
            }
        }
    )
}

fun getSquadStatusDescription(status: SquadStatus): String {
    return when (status) {
        SquadStatus.STAR_PLAYER -> "Plays almost every game. High expectations."
        SquadStatus.FIRST_TEAM -> "Regular starter. Key member of the squad."
        SquadStatus.ROTATION -> "Plays regularly but rotates with others."
        SquadStatus.BACKUP -> "Cover for first team. Limited minutes."
        SquadStatus.SURPLUS -> "Not in plans. Will be sold or loaned."
    }
}
