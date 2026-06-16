package com.fameafrica.afm.ui.screen.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.ui.components.common.HeroNewspaperStory
import com.fameafrica.afm.ui.components.common.NewspaperArticleCard
import com.fameafrica.afm.ui.components.common.NewspaperColumnLayout
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FootballThemePreset
import com.fameafrica.afm.ui.theme.FameColors

/**
 * NewspaperScreen (Continental Gazette)
 * High-density news layout inspired by FM2026 / FCM style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewspaperScreen(
    onBack: () -> Unit,
    onArticleClick: (Int) -> Unit,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "CONTINENTAL GAZETTE",
                            style = AFMTextStyles.sectionHeader.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FameColors.TrophyGold)
                }

                NewspaperColumnLayout {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val heroArticle = uiState.allNews.firstOrNull { it.isTopNews == 1 }
                        val secondaryArticles = uiState.allNews.filter { it.id != heroArticle?.id }

                        if (heroArticle != null) {
                            item {
                                HeroNewspaperStory(
                                    headline = heroArticle.headline,
                                    subHeadline = heroArticle.content.take(100) + "...",
                                    content = heroArticle.content,
                                    imageRes = null, // Logic in NewsScreen handles image mapping
                                    imageUrl = getNewsImageUrl(heroArticle),
                                    category = heroArticle.category,
                                    date = heroArticle.formattedTimestamp,
                                    modifier = Modifier.clickable { onArticleClick(heroArticle.id) }
                                )
                            }
                        }

                        items(secondaryArticles) { article ->
                            NewspaperArticleCard(
                                headline = article.headline,
                                content = article.content,
                                category = article.category,
                                date = article.formattedTimestamp,
                                isBreaking = article.isTopNews == 1,
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
