package com.fameafrica.afm.ui.screen.world

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.model.*
import com.fameafrica.afm.domain.model.*
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.components.common.*
import com.fameafrica.afm.ui.screen.dashboard.NewsUiModel
import com.fameafrica.afm.ui.theme.*
import java.util.Locale

@Composable
fun WorldScreen(
    onNavigateToLeague: (String) -> Unit,
    onNavigateToCup: (String) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    viewModel: WorldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme {
        StadiumBackground {
            WorldContent(
                uiState = uiState,
                onNavigateToLeague = onNavigateToLeague,
                onNavigateToCup = onNavigateToCup,
                onNavigateToMatch = onNavigateToMatch,
                onContinentSelected = { viewModel.selectContinent(it) }
            )
        }
    }
}

@Composable
fun WorldContent(
    uiState: WorldUiState,
    onNavigateToLeague: (String) -> Unit,
    onNavigateToCup: (String) -> Unit,
    onNavigateToMatch: (Int) -> Unit,
    onContinentSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WorldBroadcastHeaderAAA(uiState.selectedContinent)
        
        // --- FEATURED NEWS SECTION ---
        val featuredNews = uiState.latestNews.firstOrNull()
        if (featuredNews != null) {
            FeaturedNewsCard(featuredNews)
        }

        WorldTabSelectorAAA(0) { /* Handle tab logic */ }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SidebarSectionTitle("LEAGUE RANKINGS") }
            items(uiState.globalLeagueRankings.take(5)) { ranking ->
                LeagueRankItem(ranking, onNavigateToLeague)
            }
            
            item { SidebarSectionTitle("CLUB RANKINGS") }
            items(uiState.globalClubRankings.take(5)) { ranking ->
                ClubRankItem(ranking)
            }
        }
    }
}

@Composable
fun FeaturedNewsCard(news: NewsUiModel) {
    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://example.com/placeholder.jpg",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(color = Color.Black.copy(alpha = 0.4f), modifier = Modifier.fillMaxSize()) {}
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Badge(containerColor = FameColors.AfroSunOrange) { 
                    Text(news.category.uppercase(Locale.US), color = Color.Black, fontWeight = FontWeight.Bold) 
                }
                Spacer(Modifier.height(8.dp))
                Text(news.title.uppercase(Locale.US), style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
            }
        }
    }
}

@Composable
fun LeagueRankItem(ranking: GlobalLeagueRanking, onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate(ranking.leagueName) },
        color = FameColors.HeaderDark,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${ranking.rank}", style = AFMTextStyles.textSM, color = FameColors.TrophyGold)
            Spacer(Modifier.width(12.dp))
            AsyncImage(
                model = ranking.logoPath,
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                error = painterResource(com.fameafrica.afm.R.drawable.default_premier_league)
            )
            Spacer(Modifier.width(12.dp))
            Text(ranking.leagueName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, modifier = Modifier.weight(1f))
            Text("${(ranking.averageRating * 20).toInt()}", style = AFMTextStyles.textSM, color = FameColors.GrowthGreen)
        }
    }
}

@Composable
fun ClubRankItem(ranking: GlobalClubRanking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FameColors.HeaderDark,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${ranking.rank}", style = AFMTextStyles.textSM, color = FameColors.TrophyGold)
            Spacer(Modifier.width(12.dp))
            TeamLogo(ranking.clubName, Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(ranking.clubName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, modifier = Modifier.weight(1f))
            Text("€${ranking.totalMarketValue / 1_000_000}M", style = AFMTextStyles.textSM, color = FameColors.TrophyGold)
        }
    }
}
// Placeholder components kept for compatibility
@Composable fun WorldBroadcastHeaderAAA(region: String) {}
@Composable fun WorldTabSelectorAAA(selected: Int, onSelected: (Int) -> Unit) {}
