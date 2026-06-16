package com.fameafrica.afm.ui.screen.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.InterviewsEntity
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.database.entities.PressConferencesEntity
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MediaScreen(
    onBack: () -> Unit,
    onNewsClick: (Int) -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AFM2026Theme(themePreset = FootballThemePreset.NEWS_MODE) {
        MediaBackground {
            MediaScreenContent(
                uiState = uiState,
                onBack = onBack,
                onNewsClick = onNewsClick,
                onSubmitPressResponse = { id, response -> viewModel.submitPressResponse(id, response) },
                onHandleInterview = { id, resp, type -> viewModel.handleInterview(id, resp, type) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreenContent(
    uiState: MediaUiState,
    onBack: () -> Unit,
    onNewsClick: (Int) -> Unit,
    onSubmitPressResponse: (Int, String) -> Unit,
    onHandleInterview: (Int, String, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("NEWS FEED", "PRESS ROOM", "INTERVIEWS")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CONTINENTAL MEDIA",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 2.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                title,
                                style = AFMTextStyles.tableHeader,
                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    beyondViewportPageCount = 1,
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> NewsFeedPage(uiState.news, onNewsClick)
                        1 -> PressRoomPage(uiState.pendingPressConferences, onSubmitPressResponse)
                        2 -> InterviewsPage(uiState.pendingInterviews, onHandleInterview)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsFeedPage(news: List<NewsEntity>, onNewsClick: (Int) -> Unit) {
    if (news.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Newspaper, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("NO RECENT UPDATES", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(news, key = { it.id }) { article ->
                NewsArticleCard(article = article, onClick = { onNewsClick(article.id) })
            }
        }
    }
}

@Composable
fun NewsArticleCard(article: NewsEntity, onClick: () -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(article.category)
                if (article.isTopNews == 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text("TOP STORY", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(article.formattedTimestamp, style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(article.headline, style = AFMTextStyles.tableHeader.copy(fontSize = 16.sp), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(article.content, style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun PressRoomPage(pcs: List<PressConferencesEntity>, onResponse: (Int, String) -> Unit) {
    if (pcs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No active press conferences. Consistent performance keeps the spotlight on.", style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            contentPadding = PaddingValues(16.dp), 
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pcs, key = { it.id }) { pc ->
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(pc.journalistName.uppercase(), style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onSurface)
                                Text(pc.questionCategory.replace("_", " "), style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Text(
                            text = "\"${pc.question}\"", 
                            style = AFMTextStyles.tableCell.copy(fontSize = 15.sp), 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontStyle = FontStyle.Italic,
                            lineHeight = 22.sp
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(pc.optionA, pc.optionB, pc.optionC).forEach { option ->
                                Button(
                                    onClick = { onResponse(pc.id, option) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Text(
                                        text = option, 
                                        style = AFMTextStyles.tableCell, 
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewsPage(interviews: List<InterviewsEntity>, onComplete: (Int, String, String) -> Unit) {
    if (interviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No pending interview requests.", style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            contentPadding = PaddingValues(16.dp), 
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(interviews, key = { it.id }) { interview ->
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("EXCLUSIVE: ${interview.topic.uppercase()}", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(interview.question, style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onComplete(interview.id, "Positive response", "POSITIVE") }, 
                                modifier = Modifier.weight(1f), 
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), 
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("ACCEPT", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Button(
                                onClick = { /* Decline */ }, 
                                modifier = Modifier.weight(1f), 
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), 
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("DECLINE", style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val color = when (category) {
        "TRANSFER" -> FameColors.AfroSunOrange
        "MATCH" -> FameColors.PitchGreen
        "INTERVIEW" -> FameColors.ChampionsGold
        "BOARD", "INJURY" -> FameColors.KenteRed
        else -> FameColors.MutedParchment
    }
    Surface(color = color.copy(alpha = 0.1f), border = BorderStroke(0.5.dp, color), shape = RoundedCornerShape(4.dp)) {
        Text(category, style = AFMTextStyles.statLabel, color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}
