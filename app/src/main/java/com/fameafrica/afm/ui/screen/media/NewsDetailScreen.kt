package com.fameafrica.afm.ui.screen.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.data.repository.NewsRepository
import com.fameafrica.afm.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {
    private val _newsArticle = MutableStateFlow<NewsEntity?>(null)
    val newsArticle: StateFlow<NewsEntity?> = _newsArticle

    fun loadArticle(id: Int) {
        viewModelScope.launch {
            _newsArticle.value = newsRepository.getNewsById(id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: Int,
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    val article by viewModel.newsArticle.collectAsState()

    LaunchedEffect(newsId) {
        viewModel.loadArticle(newsId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "NEWS ARTICLE",
                        style = AFMTextStyles.sectionHeader.copy(fontSize = 16.sp, letterSpacing = 1.sp),
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share logic */ }) {
                        Icon(Icons.Default.Share, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        article?.let { item ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
            ) {
                // Header Image
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Category and Date Masthead
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            style = AFMTextStyles.tickerText.copy(
                                color = Color(0xFFCC0000),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = item.formattedTimestamp.uppercase(),
                            style = AFMTextStyles.tickerText.copy(color = Color.Gray, fontSize = 10.sp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Black
                    )

                    // Headline
                    Text(
                        text = item.headline.uppercase(),
                        style = AFMTextStyles.sectionHeader.copy(
                            fontSize = 28.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Byline
                    Text(
                        text = "BY ${item.journalistName?.uppercase() ?: "FAME SPORTS DESK"}",
                        style = AFMTextStyles.tickerText.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Content Body - Multi-column feel or just very clean
                    Text(
                        text = item.content,
                        style = AFMTextStyles.denseText.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify,
                            color = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    HorizontalDivider(thickness = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "© 2026 AFRICAN FOOTBALL MANAGER NEWS NETWORK",
                        style = AFMTextStyles.tickerText.copy(fontSize = 9.sp, color = Color.Gray),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFCC0000))
        }
    }
}
