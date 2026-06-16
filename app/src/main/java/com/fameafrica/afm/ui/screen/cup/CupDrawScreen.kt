package com.fameafrica.afm.ui.screen.cup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.entities.CupsEntity
import com.fameafrica.afm.data.repository.CupsRepository
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.AFM2026Typography
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.StadiumBackground
import com.fameafrica.afm.utils.LeagueLogoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.NumberFormat
import javax.inject.Inject

@HiltViewModel
class CupSelectionViewModel @Inject constructor(
    private val cupsRepository: CupsRepository,
) : ViewModel() {
    private val _cups = MutableStateFlow<List<CupsEntity>>(emptyList())
    val cups: StateFlow<List<CupsEntity>> = _cups.asStateFlow()

    init {
        loadCups()
    }

    private fun loadCups() {
        viewModelScope.launch {
            _cups.value = cupsRepository.getAllCups().first()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CupDrawScreen(
    onBack: () -> Unit,
    onCupClick: (String) -> Unit,
    viewModel: CupSelectionViewModel = hiltViewModel(),
) {
    val cups by viewModel.cups.collectAsStateWithLifecycle()
    val tabs = listOf("DOMESTIC", "CONTINENTAL", "INTERNATIONAL")
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    StadiumBackground {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "COMPETITIONS",
                                style = AFM2026Typography.headlineSmall,
                                color = FameColors.WarmIvory,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    PrimaryTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = FameColors.ChampionsGold,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                color = FameColors.ChampionsGold
                            )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val icon = when(index) {
                                0 -> Icons.Default.EmojiEvents
                                1 -> Icons.Default.Stars
                                else -> Icons.Default.Public
                            }
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            title,
                                            style = AFM2026Typography.labelLarge,
                                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(padding).fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val category = when (page) {
                    0 -> "Domestic"
                    1 -> "Continental"
                    else -> "International"
                }
                
                val filteredCups = cups.filter { it.type == category }
                
                if (filteredCups.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No $category competitions found", color = FameColors.MutedParchment)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCups, key = { it.id }) { cup ->
                            CupItemRow(cup = cup) { onCupClick(cup.name) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CupItemRow(cup: CupsEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    val cupLogo = LeagueLogoUtils.getLeagueLogo(context, cup.name)

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (cupLogo) {
                    is Int -> Icon(
                        painter = painterResource(cupLogo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )
                    is String -> AsyncImage(
                        model = cupLogo,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = FameColors.ChampionsGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    cup.name.uppercase(Locale.ROOT),
                    style = AFM2026Typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    cup.country ?: "International",
                    style = AFM2026Typography.labelSmall,
                    color = FameColors.MutedParchment,
                    letterSpacing = 1.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (cup.prizeMoney > 0) {
                    Text(
                        "€${NumberFormat.getNumberInstance(Locale.US).format(cup.prizeMoney)}",
                        style = AFM2026Typography.labelLarge,
                        color = FameColors.ChampionsGold,
                        fontWeight = FontWeight.Black
                    )
                }
                Text(
                    "VIEW DETAILS",
                    style = AFM2026Typography.labelSmall,
                    color = FameColors.PitchGreen,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
