package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.data.database.model.PlayerFilter
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun TransferMarketTab(
    uiState: TransfersUiState,
    filter: PlayerFilter,
    onPlayerClick: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onMarketViewChange: (MarketView) -> Unit,
    selectedMarketView: MarketView = MarketView.ALL,
    onScout: (Int) -> Unit,
    onBid: (Int) -> Unit,
    onShortlist: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Search Bar
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = filter.name ?: "",
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("SEARCH PLAYER DATABASE...", style = AFMTextStyles.textXS) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = FameColors.TrophyGold) },
                shape = RoundedCornerShape(2.dp),
                singleLine = true,
                textStyle = AFMTextStyles.textSM.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FameColors.TrophyGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Surface(
                modifier = Modifier.size(52.dp).clickable { onFilterClick() },
                shape = RoundedCornerShape(2.dp),
                color = if (filter != PlayerFilter()) FameColors.TrophyGold else Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (filter != PlayerFilter()) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Tune, 
                        null, 
                        tint = if (filter != PlayerFilter()) Color.Black else Color.White
                    )
                }
            }
        }

        // 2. Market View Options (FCM26 style)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MarketView.entries) { view ->
                val label = view.label.replace("_", " ").uppercase()
                FilterChip(
                    selected = selectedMarketView == view,
                    onClick = { onMarketViewChange(view) },
                    label = { Text(label, style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FameColors.TrophyGold,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.White.copy(alpha = 0.05f),
                        labelColor = FameColors.MutedParchment
                    ),
                    border = BorderStroke(0.5.dp, if (selectedMarketView == view) FameColors.TrophyGold else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(2.dp)
                )
            }
        }

        // 3. Grid of Players
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
            }
        } else if (uiState.marketPlayers.isEmpty()) {
            TransferEmptyState(Icons.Default.SearchOff, "NO PLAYERS FOUND", "ADJUST YOUR FILTERS OR CLEAR YOUR SEARCH TO SEE MORE PLAYERS.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Results header inside grid
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SEARCH RESULTS - ${uiState.marketPlayers.size} PLAYERS",
                            style = AFMTextStyles.textXXS.copy(fontSize = 9.sp),
                            color = FameColors.MutedParchment,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SORT \u25BC", style = AFMTextStyles.textXXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(uiState.marketPlayers, key = { it.id }) { player ->
                    TransferPlayerTile(
                        player = player,
                        currencyContext = uiState.currencyContext,
                        onScout = { onScout(player.id) },
                        onBid = { onBid(player.id) },
                        onShortlist = { onShortlist(player.id) },
                        onClick = { onPlayerClick(player.id) }
                    )
                }
            }
        }
    }
}

enum class MarketView(val label: String) {
    ALL("All"),
    FOR_SALE("For Sale"),
    EXPIRING("Expiring"),
    LOAN_LISTED("Loan Listed"),
    FREE_AGENTS("Free Agents"),
    CAF_WINDOW("CAF Window")
}
