package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.data.database.entities.NewsEntity
import com.fameafrica.afm.ui.theme.AFMTextStyles

@Composable
fun HistoryTab(
    uiState: TransfersUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "COMPLETED DEALS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (uiState.completedDeals.isEmpty()) {
            item {
                TransferEmptyState(
                    Icons.Default.History,
                    "NO HISTORY FOUND",
                    "ALL COMPLETED INCOMING AND OUTGOING TRANSFERS WILL BE LISTED HERE."
                )
            }
        } else {
            items(uiState.completedDeals) { deal ->
                DealSummaryCard(deal, uiState.currencyContext)
            }
        }

        item {
            Text(
                "TRANSFER NEWS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        items(uiState.transferNews) { news ->
            NewsCard(news)
        }
    }
}

@Composable
fun NewsCard(news: NewsEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    news.category.uppercase(),
                    style = AFMTextStyles.textXXS,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    news.formattedTimestamp,
                    style = AFMTextStyles.textXXS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                news.headline.uppercase(),
                style = AFMTextStyles.textSM,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                news.content,
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}
