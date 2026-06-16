package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.AFMTextStyles

@Composable
fun LoansTab(
    uiState: TransfersUiState,
    onPlayerClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "OUTGOING LOANS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (uiState.loanOutgoing.isEmpty()) {
            item {
                TransferEmptyState(
                    Icons.Default.SwapHoriz,
                    "NO OUTGOING LOANS",
                    "LIST PLAYERS FOR LOAN TO GIVE THEM FIRST-TEAM EXPERIENCE ELSEWHERE."
                )
            }
        } else {
            items(uiState.loanOutgoing) { loan ->
                OfferStatusCard(loan, uiState.currencyContext, onPlayerClick)
            }
        }

        item {
            Text(
                "INCOMING LOAN TARGETS",
                style = AFMTextStyles.textXS,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (uiState.loanIncoming.isEmpty()) {
            item {
                TransferEmptyState(
                    Icons.Default.SwapHoriz,
                    "NO INCOMING LOANS",
                    "BORROW TALENT FROM BIGGER CLUBS TO STRENGTHEN YOUR SQUAD ON A BUDGET."
                )
            }
        } else {
            items(uiState.loanIncoming) { loan ->
                OfferStatusCard(loan, uiState.currencyContext, onPlayerClick)
            }
        }
    }
}
