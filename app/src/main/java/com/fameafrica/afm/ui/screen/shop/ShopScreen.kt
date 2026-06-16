package com.fameafrica.afm.ui.screen.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FootballThemePreset
import com.fameafrica.afm.ui.theme.FameColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AFM2026Theme(themePreset = FootballThemePreset.CHAIRMAN_MODE) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("AFM SHOP", style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Shop", tint = FameColors.TrophyGold)
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "PREMIUM COIN BUNDLES",
                            style = AFMTextStyles.textXS.copy(color = FameColors.TrophyGold, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        )
                    }

                    items(coinBundles) { bundle ->
                        ShopBundleCard(bundle) {
                            viewModel.onBundleSelected(bundle)
                        }
                    }
                }
            }

            if (uiState.showPaymentOptions) {
                PaymentOptionsDialog(
                    bundle = uiState.selectedBundle!!,
                    onDismiss = { viewModel.resetState() },
                    onPaymentSelected = { viewModel.onPaymentMethodSelected(it) }
                )
            }
        }
    }
}

@Composable
fun ShopBundleCard(
    bundle: ShopBundle,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (bundle.badge != null) {
                    Surface(
                        color = FameColors.TrophyGold,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            bundle.badge.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = AFMTextStyles.textXXS.copy(color = Color.Black, fontWeight = FontWeight.Black)
                        )
                    }
                }
                Text(bundle.name.uppercase(), style = AFMTextStyles.textMD.copy(color = Color.White, fontWeight = FontWeight.Black))
                Text(bundle.description, style = AFMTextStyles.textXS.copy(color = FameColors.MutedParchment))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FameColors.TrophyGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${bundle.coins} COINS", style = AFMTextStyles.textLG.copy(color = FameColors.TrophyGold, fontWeight = FontWeight.Black))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("TZS ${bundle.priceTZS}", style = AFMTextStyles.textMD.copy(color = Color.White, fontWeight = FontWeight.Black))
                Text("($${bundle.priceUSD})", style = AFMTextStyles.textXS.copy(color = FameColors.MutedParchment))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.GrowthGreen),
                    shape = RoundedCornerShape(2.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("BUY", style = AFMTextStyles.textSM.copy(color = Color.White, fontWeight = FontWeight.Black))
                }
            }
        }
    }
}

@Composable
fun PaymentOptionsDialog(
    bundle: ShopBundle,
    onDismiss: () -> Unit,
    onPaymentSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "SELECT PAYMENT METHOD", 
                style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black),
                color = FameColors.TrophyGold
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentMethodRow("AzamPesa (TZS)", "Mobile Money - East Africa") { onPaymentSelected("AZAMPESA") }
                PaymentMethodRow("Visa/Mastercard (USD)", "Global Credit/Debit Card") { onPaymentSelected("VISA") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = FameColors.AlertRed, fontWeight = FontWeight.Black)
            }
        },
        containerColor = FameColors.DeepNavyBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
fun PaymentMethodRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title.uppercase(), style = AFMTextStyles.textSM.copy(color = Color.White, fontWeight = FontWeight.Black))
            Text(subtitle, style = AFMTextStyles.textXXS.copy(color = FameColors.MutedParchment))
        }
    }
}
