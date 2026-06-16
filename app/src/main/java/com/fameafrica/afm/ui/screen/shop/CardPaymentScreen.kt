package com.fameafrica.afm.ui.screen.shop

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.domain.manager.PaymentResult
import com.fameafrica.afm.ui.theme.*

@Composable
fun CardPaymentScreen(
    bundle: ShopBundle,
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    var cardNumber by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.paymentResult) {
        if (uiState.paymentResult is PaymentResult.Success) {
            onSuccess((uiState.paymentResult as PaymentResult.Success).transactionId)
        }
    }

    AFM2026Theme(themePreset = FootballThemePreset.CHAIRMAN_MODE) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "SECURE CARD PAYMENT", 
                style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black),
                color = FameColors.TrophyGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "PAYING $${bundle.priceUSD} FOR ${bundle.name.uppercase()}", 
                style = AFMTextStyles.textXS.copy(color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("CARD NUMBER") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FameColors.TrophyGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.isProcessing) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
            } else {
                Button(
                    onClick = { viewModel.processPayment("VISA", mapOf("card" to cardNumber)) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = cardNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.GrowthGreen),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("PAY NOW ($)", style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onCancel) {
                    Text("ABORT", color = FameColors.AlertRed, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
                }
            }

            if (uiState.paymentResult is PaymentResult.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    (uiState.paymentResult as PaymentResult.Error).message.uppercase(),
                    color = FameColors.AlertRed,
                    style = AFMTextStyles.textXS,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
