package com.fameafrica.afm.ui.screen.transfers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.ui.common.formatCompactCurrency
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.formatters.CurrencyFormatter

@Composable
fun TransferPlayerTile(
    player: TransferPlayerUiModel,
    currencyContext: CurrencyFormatter.CurrencyContext?,
    onScout: () -> Unit,
    onBid: () -> Unit,
    onShortlist: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = FameColors.HeaderDark.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    AsyncImage(
                        model = player.clubLogo,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = AFMTextStyles.textXS,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${player.age} • ${player.position}",
                        style = AFMTextStyles.textXXS.copy(fontSize = 9.sp),
                        color = FameColors.MutedParchment
                    )
                }
                Text(
                    player.rating.toString(),
                    style = AFMTextStyles.textSM,
                    color = FameColors.TrophyGold,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("VALUE", style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.DisabledText)
                    Text(
                        formatCompactCurrency(player.value, context = currencyContext),
                        style = AFMTextStyles.textXS,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WAGE", style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = FameColors.DisabledText)
                    Text(
                        "${formatCompactCurrency(player.wage, context = currencyContext)}/wk",
                        style = AFMTextStyles.textXS,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onShortlist,
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                ) {
                    Icon(Icons.Default.StarBorder, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
                IconButton(
                    onClick = onScout,
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
                Button(
                    onClick = onBid,
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.TransferBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("BID", style = AFMTextStyles.textXXS, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
