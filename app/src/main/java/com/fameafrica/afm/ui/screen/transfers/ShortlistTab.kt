package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.AFMTextStyles

@Composable
fun ShortlistTab(
    uiState: TransfersUiState,
    onPlayerClick: (Int) -> Unit,
    onRemoveFromShortlist: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "TRACKED PLAYERS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                "${uiState.shortlist.size} PLAYERS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.shortlist.isEmpty()) {
            TransferEmptyState(
                Icons.Default.StarBorder,
                "SHORTLIST IS EMPTY",
                "ADD PLAYERS TO TRACK THEIR PROGRESS, VALUE CHANGES, AND AVAILABILITY."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.shortlist, key = { it.id }) { player ->
                    PlayerTransferCard(player, uiState.currencyContext, onPlayerClick)
                }
            }
        }
    }
}
