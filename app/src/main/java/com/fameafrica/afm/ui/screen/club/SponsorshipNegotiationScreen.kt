package com.fameafrica.afm.ui.screen.club

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.StadiumBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorshipNegotiationScreen(
    onBack: () -> Unit,
    viewModel: SponsorshipViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    StadiumBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("SPONSORSHIP DEALS", style = AFMTextStyles.sectionHeader, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (uiState.availableOffers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Handshake, null, modifier = Modifier.size(64.dp), tint = FameColors.MutedParchment.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("NO ACTIVE OFFERS", style = AFMTextStyles.textSM, color = FameColors.MutedParchment)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.generateOffers() },
                            colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("SEARCH FOR SPONSORS", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.availableOffers) { offer ->
                        SponsorOfferCard(offer) {
                            viewModel.acceptOffer(offer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SponsorOfferCard(offer: SponsorOffer, onAccept: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(offer.name.uppercase(), style = AFMTextStyles.textMD.copy(color = Color.White, fontWeight = FontWeight.Black))
                Surface(color = FameColors.ChampionsGold.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp)) {
                    Text(
                        offer.type.uppercase(), 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = AFMTextStyles.textXXS, 
                        color = FameColors.ChampionsGold,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("MONTHLY PAYOUT", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text("TZS ${offer.payout}", style = AFMTextStyles.textSM, color = FameColors.GrowthGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DURATION", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                    Text("${offer.duration} MONTHS", style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("OBJECTIVE", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
            Text(offer.objective.uppercase(), style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
            
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("ACCEPT DEAL", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}
