package com.fameafrica.afm.ui.screen.manager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.ManagerOffersEntity
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.ui.components.common.TeamLogo
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.NationalityUtils
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ManagerScreen(
    onBack: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToJobCentre: () -> Unit,
    managerViewModel: ManagerViewModel = hiltViewModel()
) {
    val uiState by managerViewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme {
        ManagerScreenContent(
            manager = uiState.manager,
            jobOffers = uiState.jobOffers,
            agentName = uiState.agentName,
            onAcceptOffer = { managerViewModel.acceptJobOffer(it) },
            onRejectOffer = { managerViewModel.rejectJobOffer(it) },
            onBack = onBack,
            onNavigateToStaff = onNavigateToStaff,
            onNavigateToJobCentre = onNavigateToJobCentre
        )
    }
}

@Composable
fun ManagerScreenContent(
    manager: ManagersEntity?,
    jobOffers: List<ManagerOffersEntity> = emptyList(),
    agentName: String? = null,
    onAcceptOffer: (Int) -> Unit = {},
    onRejectOffer: (Int) -> Unit = {},
    onBack: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToJobCentre: () -> Unit
) {
    if (manager == null) return

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Profile Name Header
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
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = NationalityUtils.getFlagUrl(manager.nationality),
                    contentDescription = manager.nationality,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = manager.name.uppercase(),
                        style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = manager.reputationDescription.uppercase(),
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = FameColors.ChampionsGold,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = manager.overallRating.toString(),
                    style = AFMTextStyles.textLG.copy(fontSize = 32.sp, fontWeight = FontWeight.Black),
                    color = Color.White,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        // Main Content Area
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Upper Info Section (Fixed)
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                // Left: Large Portrait with Overlay
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.default_manager),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay Gradient for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        FameColors.DeepNavyBlack.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // Radar Chart Overlayed on the lower part
                    ManagerRadarChart(
                        manager = manager,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // Right: Info Cards
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Nationality/Club Title Card
                    Surface(
                        color = Color(0xFF003366),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = NationalityUtils.getFlagUrl(manager.nationality),
                                contentDescription = manager.nationality,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(manager.nationality.uppercase(), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White)
                                Text("Joined: 01/07/2025".uppercase(), style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Contract Info
                    val salaryFormatted = NumberFormat.getCurrencyInstance(Locale.UK).apply {
                        maximumFractionDigits = 0
                    }.format(manager.salary ?: 0)
                    
                    InfoMiniCard(label = "Salary", value = salaryFormatted, icon = R.drawable.money)
                    InfoMiniCard(label = "Expiry", value = "${manager.contractEndDate ?: "2025"}", icon = R.drawable.calendar)
                    InfoMiniCard(label = "Agent", value = formatDisplayValue(agentName ?: "Freelance"), icon = R.drawable.agent)

                    // Status Bar
                    Surface(
                        color = if (manager.isEmployed) Color(0xFF009933) else Color(0xFFCC3300),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (manager.isEmployed) "UNDER CONTRACT" else "UNEMPLOYED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }

                    // License & Style
                    LicensePill(formatDisplayValue(manager.coachingLicense ?: "None"))
                    StylePill(formatDisplayValue(manager.style), "Management Style")
                    StylePill(formatDisplayValue(manager.spendingHabits), "Spending Habits")

                    // Attributes List
                    AttributeListItem("Reputation", manager.reputation)
                    AttributeListItem("Tactical", manager.tacticalKnowledge)
                    AttributeListItem("Player Development", manager.judgingPlayerPotential)
                    AttributeListItem("Coaching", manager.technicalCoaching)
                    AttributeListItem("Man Management", manager.manManagement)

                    // Tactical Preferrence
                    Surface(
                        color = Color(0xFF003366),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(manager.preferredFormation, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text(formatDisplayValue(manager.style).uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                         }
                    }
                }
            }

            // Swipeable Section Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = FameColors.DeepNavyBlack,
                contentColor = Color.White,
                divider = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = FameColors.ChampionsGold
                        )
                    }
                }
            ) {
                val tabs = listOf("CAREER STATS", "JOB CENTER")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                    )
                }
            }

            // Horizontal Pager for Career Stats and Job Center
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> CareerStatsView(manager)
                    1 -> JobCenterPanel(
                        offers = jobOffers,
                        onAccept = onAcceptOffer,
                        onReject = onRejectOffer,
                        onNavigateToJobMarket = onNavigateToJobCentre
                    )
                }
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToStaff,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.HeaderDark),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("STAFF HUB", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
            Button(
                onClick = { /* Action */ },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.HeaderDark),
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text("ACTION", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
        }
    }
}

@Composable
fun CareerStatsView(manager: ManagersEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column {
                Surface(color = Color(0xFF003366), modifier = Modifier.fillMaxWidth().height(24.dp)) {
                    Text("OVERALL CAREER RECORD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    StatLabelValue("MATCHES", manager.matchesManaged.toString(), Modifier.weight(1f))
                    StatLabelValue("W", manager.wins.toString(), Modifier.weight(1f))
                    StatLabelValue("D", manager.draws.toString(), Modifier.weight(1f))
                    StatLabelValue("L", manager.losses.toString(), Modifier.weight(1f))
                    StatLabelValue("PPG", String.format(Locale.getDefault(), "%.1f", manager.ppg), Modifier.weight(1f))
                    StatLabelValue("WIN %", "${manager.winPercentage.toInt()}%", Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column {
                Surface(color = Color(0xFF003366), modifier = Modifier.fillMaxWidth().height(24.dp)) {
                    Text("HONOURS & ACHIEVEMENTS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    StatLabelValue("LEAGUE TITLES", manager.leagueTitles.toString(), Modifier.weight(1f))
                    StatLabelValue("CUP WINS", manager.cupWins.toString(), Modifier.weight(1f))
                    StatLabelValue("PROMOTIONS", manager.promotions.toString(), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    StatLabelValue("RELEGATIONS", manager.relegations.toString(), Modifier.weight(1f))
                    StatLabelValue("CLUBS", manager.clubsManaged.toString(), Modifier.weight(1f))
                    StatLabelValue("AWARDS", manager.totalAwards.toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ManagerRadarChart(
    manager: ManagersEntity,
    modifier: Modifier = Modifier
) {
    val attributes = listOf(
        "ATT" to manager.attackingCoaching,
        "DEF" to manager.defendingCoaching,
        "TAC" to manager.tacticalKnowledge,
        "TEC" to manager.technicalCoaching,
        "MAN" to manager.manManagement,
        "POT" to manager.judgingPlayerPotential
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(160.dp) // Smaller for overlay
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.75f
                val numAttributes = attributes.size
                val angleStep = (2 * Math.PI / numAttributes).toFloat()

                // Draw background circles/polygons - subtle for overlay
                for (i in 1..5) {
                    val r = radius * (i / 5f)
                    val path = Path()
                    for (j in 0 until numAttributes) {
                        val angle = j * angleStep - Math.PI.toFloat() / 2
                        val x = center.x + r * cos(angle.toDouble()).toFloat()
                        val y = center.y + r * sin(angle.toDouble()).toFloat()
                        if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, Color.White.copy(alpha = 0.15f), style = Stroke(width = 1.dp.toPx()))
                }

                // Draw attribute polygon
                val attributePath = Path()
                for (i in 0 until numAttributes) {
                    val attrValue = attributes[i].second.toFloat().coerceIn(0f, 100f) / 100f
                    val r = radius * attrValue
                    val angle = i * angleStep - Math.PI.toFloat() / 2
                    val x = center.x + r * cos(angle.toDouble()).toFloat()
                    val y = center.y + r * sin(angle.toDouble()).toFloat()
                    if (i == 0) attributePath.moveTo(x, y) else attributePath.lineTo(x, y)
                }
                attributePath.close()
                drawPath(attributePath, Color(0xFFCC9933).copy(alpha = 0.5f))
                drawPath(attributePath, Color(0xFFCC9933), style = Stroke(width = 2.dp.toPx()))
            }

            // Labels around the chart - compact
            attributes.forEachIndexed { index, pair ->
                val angle = index * (2 * Math.PI / attributes.size) - Math.PI / 2
                val radiusFactor = 1.15f
                val xOffset = (cos(angle) * 60 * radiusFactor).dp
                val yOffset = (sin(angle) * 60 * radiusFactor).dp

                Column(
                    modifier = Modifier.offset(x = xOffset, y = yOffset),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pair.first,
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black, fontSize = 8.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = pair.second.toString(),
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                        color = FameColors.ChampionsGold
                    )
                }
            }
        }
    }
}

@Composable
fun JobCenterPanel(
    offers: List<ManagerOffersEntity>,
    onAccept: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onNavigateToJobMarket: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Surface(color = Color(0xFF003366), modifier = Modifier.fillMaxWidth().height(24.dp)) {
                Text("JOB CENTER - ACTIVE OFFERS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
            }
            if (offers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text("NO ACTIVE OFFERS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    offers.forEach { offer ->
                        JobOfferItem(offer, onAccept, onReject)
                    }
                }
            }
            
            Button(
                onClick = onNavigateToJobMarket,
                modifier = Modifier.fillMaxWidth().padding(8.dp).height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)),
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("BROWSE JOB MARKET / APPLY", fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun JobOfferItem(offer: ManagerOffersEntity, onAccept: (Int) -> Unit, onReject: (Int) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TeamLogo(teamName = offer.offeringTeam, modifier = Modifier.size(40.dp).padding(end = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(offer.offeringTeam.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${offer.leagueName} (${offer.leagueTier})", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Salary: £${offer.offeredSalary} | ${offer.contractYears} Years", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { onReject(offer.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.height(24.dp).width(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("REJECT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Button(
                    onClick = { onAccept(offer.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006633)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.height(24.dp).width(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("ACCEPT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun InfoMiniCard(label: String, value: String, icon: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().height(42.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(value.uppercase(), style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White)
                Text(label.uppercase(), style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AttributeListItem(label: String, value: Int) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        modifier = Modifier.fillMaxWidth().height(26.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(label.uppercase(), style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
            Surface(color = FameColors.ChampionsGold, shape = RoundedCornerShape(2.dp)) {
                Text(value.toString(), style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black), color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
            }
        }
    }
}

@Composable
fun LicensePill(text: String) {
    Surface(
        color = Color(0xFF666699).copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth().height(26.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text.uppercase(), color = Color.White, style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black, fontSize = 8.sp))
        }
    }
}

@Composable
fun StylePill(value: String, label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth().height(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(value.uppercase(), style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Black, fontSize = 8.sp), color = Color.White)
            Text(label.uppercase(), style = AFMTextStyles.textXXS.copy(fontSize = 6.sp, fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun StatLabelValue(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AFMTextStyles.textXXS.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.5f))
        Text(value, style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Black), color = Color.White)
    }
}

fun formatDisplayValue(value: String?): String {
    if (value == null) return "N/A"
    return value.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() } }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ManagerProfilePreview() {
    AFM2026Theme {
        ManagerScreenContent(
            manager = ManagersEntity(
                name = "T. Joseph",
                nationality = "Tanzania",
                matchesManaged = 909,
                wins = 619,
                draws = 143,
                losses = 147,
                attackingCoaching = 85,
                defendingCoaching = 70,
                tacticalKnowledge = 90,
                technicalCoaching = 80,
                manManagement = 95,
                judgingPlayerPotential = 88,
                salary = 120000,
                contractEndDate = 2045,
                style = "ATTACKING_FLUID",
                preferredFormation = "4-3-3",
                coachingLicense = "CAF_PRO_LICENCE",
                spendingHabits = "BALANCED"
            ),
            jobOffers = listOf(
                ManagerOffersEntity(
                    id = 1,
                    managerId = 0,
                    managerName = "T. Joseph",
                    offeringTeam = "Young Africans SC",
                    offeringTeamId = 10,
                    leagueName = "Tanzania Premier League",
                    leagueLevel = 1,
                    offeredSalary = 120000,
                    contractYears = 2,
                    offerType = "HEAD_COACH",
                    expiryDate = System.currentTimeMillis() + 604800000
                )
            ),
            agentName = "William Mwakisu",
            onBack = {},
            onNavigateToStaff = {},
            onNavigateToJobCentre = {}
        )
    }
}
