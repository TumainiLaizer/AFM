package com.fameafrica.afm.ui.screen.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.ui.components.common.HeroNewspaperStory
import com.fameafrica.afm.ui.components.common.NewspaperArticleCard
import com.fameafrica.afm.ui.components.common.NewspaperColumnLayout
import com.fameafrica.afm.ui.theme.*

@Composable
fun NewsScreen(
    onBack: () -> Unit,
    onArticleClick: (Int) -> Unit,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NewsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onArticleClick = onArticleClick,
        onRefresh = { viewModel.refresh() },
        onCategorySelected = { viewModel.setCategory(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreenContent(
    uiState: NewsUiState,
    onBack: () -> Unit,
    onArticleClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MEDIA CENTER",
                            style = AFMTextStyles.sectionHeader.copy(fontSize = 18.sp),
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "AFRICAN FOOTBALL NEWS NETWORK",
                            style = AFMTextStyles.tickerText.copy(fontSize = 8.sp, color = Color.Gray)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F5) // Newspaper neutral background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            CategoryFilters(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFCC0000))
                }
            } else {
                NewspaperColumnLayout {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val topNews = uiState.allNews.firstOrNull { it.isTopNews == 1 }
                        
                        if (topNews != null && uiState.selectedCategory == "ALL") {
                            item {
                                HeroNewspaperStory(
                                    headline = topNews.headline,
                                    subHeadline = topNews.content.take(120) + "...",
                                    content = topNews.content,
                                    imageRes = getNewsImageRes(topNews),
                                    imageUrl = getNewsImageUrl(topNews),
                                    category = topNews.category,
                                    date = topNews.formattedTimestamp,
                                    modifier = Modifier.clickable { onArticleClick(topNews.id) }
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 2.dp, color = Color.Black)
                                    Text(
                                        " MORE STORIES ",
                                        style = AFMTextStyles.tickerText.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                                        color = Color.Black
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), thickness = 2.dp, color = Color.Black)
                                }
                            }
                        }

                        val filteredNews = if (uiState.selectedCategory == "ALL") {
                            uiState.allNews.filter { it.isTopNews != 1 }
                        } else {
                            uiState.allNews.filter { it.category == uiState.selectedCategory }
                        }

                        if (filteredNews.isEmpty() && topNews == null) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Newspaper,
                                            null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "NO RECENT UPDATES",
                                            style = AFMTextStyles.tableHeader,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredNews, key = { it.id }) { article ->
                                NewspaperArticleCard(
                                    headline = article.headline,
                                    content = article.content,
                                    category = article.category,
                                    date = article.formattedTimestamp,
                                    isBreaking = article.isTopNews == 1,
                                    imageRes = getNewsImageRes(article),
                                    imageUrl = getNewsImageUrl(article),
                                    modifier = Modifier.clickable { onArticleClick(article.id) }
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
fun CategoryFilters(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("ALL", "MATCH", "TRANSFER", "INJURY", "INTERVIEW", "BOARD", "AWARD")
    
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                
                Surface(
                    onClick = { onCategorySelected(category) },
                    color = if (isSelected) Color(0xFFCC0000) else Color.Transparent,
                    shape = RoundedCornerShape(2.dp),
                    border = if (!isSelected) BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)) else null
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AFMTextStyles.tickerText.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun getNewsImageUrl(article: NewsEntity): String? {
    // If we have an explicit imageUrl in the entity, use it (if it looks like a path)
    if (article.imageUrl != null && (article.imageUrl.contains("/") || article.imageUrl.contains(":"))) {
        val path = article.imageUrl
        return when {
            path.startsWith("team_logo:") -> "file:///android_asset/team_logos/${path.substringAfter("team_logo:").lowercase().replace(" ", "_")}.png"
            path.startsWith("league_logo:") -> "file:///android_asset/league_logos/${path.substringAfter("league_logo:").lowercase().replace(" ", "_")}.png"
            path.startsWith("cup_logo:") -> "file:///android_asset/cup_logos/${path.substringAfter("cup_logo:").lowercase().replace(" ", "_")}.png"
            path.startsWith("country_flag:") -> "file:///android_asset/flags/${path.substringAfter("country_flag:")}.png"
            else -> article.imageUrl
        }
    }

    // fallback mapping logic
    return when (article.category.uppercase()) {
        "MATCH" -> if (article.relatedTeam != null) {
            "file:///android_asset/team_logos/${article.relatedTeam.lowercase().replace(" ", "_")}.png"
        } else "soccer_ball.webp"
        "TRANSFER" -> if (article.relatedPlayer != null) {
            "file:///android_asset/player_faces/default_face.png"
        } else "player_superstar.webp"
        "INTERVIEW" -> if (article.relatedManager != null) {
            "file:///android_asset/manager_faces/${article.relatedManager.lowercase().replace(" ", "_")}.png"
        } else "coach_male_north.webp"
        "BOARD" -> "file:///android_asset/media_logos/fame_africa.png"
        else -> "default_club.webp"
    }
}

@Composable
fun getNewsImageRes(article: NewsEntity): Int? {
    return when (article.category.uppercase()) {
        "MATCH" -> R.drawable.stadium_bg
        "TRANSFER" -> R.drawable.player_superstar
        "INJURY" -> R.drawable.player_dribbling
        "INTERVIEW" -> if (article.relatedManager != null) R.drawable.coach_male_east else R.drawable.default_player
        "BOARD" -> R.drawable.board_bg
        "AWARD" -> R.drawable.trophy
        "WORLD" -> R.drawable.africa_map
        "WEATHER" -> R.drawable.rainy
        else -> R.drawable.ic_launcher
    }
}

@Preview(showBackground = true, name = "News Screen - Success")
@Composable
fun NewsScreenContentSuccessPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(
                allNews = listOf(
                    NewsEntity(
                        id = 1,
                        headline = "REKINDLED RIVALRY: KANO PILLARS VS EYIMBA",
                        content = "The Nigerian Professional Football League prepares for one of its biggest fixtures as the two giants face off at the Sani Abacha Stadium. Both teams are fighting for the top spot in the league table.",
                        category = "MATCH",
                        journalistName = "AFN News",
                        journalistLogo = null,
                        timestamp = "2023-10-27 10:00:00",
                        isTopNews = 1
                    ),
                    NewsEntity(
                        id = 2,
                        headline = "TRANSFER RUMOR: VICTOR OSIMHEN TO LIVERPOOL?",
                        content = "Reports are surfacing that the Super Eagles star might be heading to Anfield in the winter transfer window. Personal terms are yet to be discussed.",
                        category = "TRANSFER",
                        journalistName = "Transfer Insider",
                        journalistLogo = null,
                        timestamp = "2023-10-27 09:00:00",
                        isTopNews = 0
                    ),
                    NewsEntity(
                        id = 3,
                        headline = "INJURY BLOW FOR MAMELODI SUNDOWNS",
                        content = "Key midfielder sidelined for 3 weeks after picking up a knock in training. This comes as a major blow ahead of their CAF Champions League fixture.",
                        category = "INJURY",
                        journalistName = "Med Center",
                        journalistLogo = null,
                        timestamp = "2023-10-27 08:30:00",
                        isTopNews = 0
                    )
                ),
                selectedCategory = "ALL"
            ),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryFiltersPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        CategoryFilters(
            selectedCategory = "ALL",
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "News Screen - Loading")
@Composable
fun NewsScreenContentLoadingPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(isLoading = true),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "News Screen - Empty")
@Composable
fun NewsScreenContentEmptyPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(allNews = emptyList()),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "News Screen - Match Filter")
@Composable
fun NewsScreenMatchFilterPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(
                allNews = listOf(
                    NewsEntity(
                        id = 1,
                        headline = "KANO PILLARS VS ENYIMBA: THE NORTHERN DERBY",
                        content = "One of the most anticipated matches of the season is here. Kano Pillars host Eyimba at the Sani Abacha Stadium in a clash that could decide the league title path.",
                        category = "MATCH",
                        relatedTeam = "Kano Pillars",
                        imageUrl = "team_logo:Kano Pillars",
                        journalistName = "AFN News",
                        journalistLogo = null,
                        timestamp = "2023-10-27 10:00:00",
                        isTopNews = 1
                    ),
                    NewsEntity(
                        id = 2,
                        headline = "ZAMALEK CLINCH LATE WIN IN CAIRO",
                        content = "A 95th minute header was enough for Zamalek to secure all three points in a hard-fought battle against Al Masry.",
                        category = "MATCH",
                        relatedTeam = "Zamalek SC",
                        imageUrl = "team_logo:Zamalek SC",
                        journalistName = "Med Center",
                        journalistLogo = null,
                        timestamp = "2023-10-27 09:30:00",
                        isTopNews = 0
                    )
                ),
                selectedCategory = "MATCH"
            ),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "News Screen - Transfer Filter")
@Composable
fun NewsScreenTransferFilterPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(
                allNews = listOf(
                    NewsEntity(
                        id = 1,
                        headline = "RECORD BREAKING DEAL: OSIMHEN TO MAN CITY?",
                        content = "Reports from England suggest that Manchester City are preparing a massive bid for the Nigerian superstar. Personal terms are rumored to be agreed.",
                        category = "TRANSFER",
                        relatedPlayer = "Victor Osimhen",
                        imageUrl = "file:///android_asset/player_faces/default_face.png",
                        journalistName = "Transfer Guru",
                        journalistLogo = null,
                        timestamp = "2023-10-27 11:00:00",
                        isTopNews = 1
                    ),
                    NewsEntity(
                        id = 2,
                        headline = "YOUNG TANZANIAN STARLET HEADING TO BELGIUM",
                        content = "A 17-year-old attacking midfielder from Azam FC is set to join Genk's youth academy after an impressive scouting tournament.",
                        category = "TRANSFER",
                        relatedTeam = "Azam FC",
                        imageUrl = "team_logo:Azam FC",
                        journalistName = "Scout Report",
                        journalistLogo = null,
                        timestamp = "2023-10-27 08:00:00",
                        isTopNews = 0
                    )
                ),
                selectedCategory = "TRANSFER"
            ),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}

@Preview(showBackground = true, name = "News Screen - Interview & Board")
@Composable
fun NewsScreenInterviewBoardPreview() {
    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        NewsScreenContent(
            uiState = NewsUiState(
                allNews = listOf(
                    NewsEntity(
                        id = 1,
                        headline = "COACH JEREMIAH: \"WE ARE READY FOR THE CHALLENGE\"",
                        content = "In an exclusive interview, the Simba SC manager speaks about his team's preparations and his vision for the upcoming continental campaign.",
                        category = "INTERVIEW",
                        relatedManager = "J. Jeremiah",
                        imageUrl = "coach_male_official.webp",
                        journalistName = "AFN News",
                        journalistLogo = null,
                        timestamp = "2023-10-27 12:00:00",
                        isTopNews = 1
                    ),
                    NewsEntity(
                        id = 2,
                        headline = "BOARD APPROVES STADIUM EXPANSION PLAN",
                        content = "The club board has officially signed off on the phase 2 expansion of the youth academy and training facilities.",
                        category = "BOARD",
                        imageUrl = "file:///android_asset/media_logos/fame_africa.png",
                        journalistName = "Club Official",
                        journalistLogo = null,
                        timestamp = "2023-10-27 07:00:00",
                        isTopNews = 0
                    )
                ),
                selectedCategory = "ALL"
            ),
            onBack = {},
            onArticleClick = {},
            onRefresh = {},
            onCategorySelected = {}
        )
    }
}
