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
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.domain.manager.PaymentResult

@Composable
fun AzamPesaPaymentScreen(
    bundle: ShopBundle,
    onSuccess: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

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
                "AzamPesa Payment".uppercase(), 
                style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black),
                color = FameColors.TrophyGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "PAYING TZS ${bundle.priceTZS} FOR ${bundle.name.uppercase()}", 
                style = AFMTextStyles.textXS.copy(color = FameColors.MutedParchment, fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("PHONE NUMBER (e.g. 07xx...)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isProcessing,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FameColors.TrophyGold,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (uiState.isProcessing) {
                CircularProgressIndicator(color = FameColors.TrophyGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "WAITING FOR PIN PROMPT ON YOUR PHONE...", 
                    style = AFMTextStyles.textXS.copy(color = FameColors.TrophyGold, fontWeight = FontWeight.Black),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Button(
                    onClick = { viewModel.processPayment("AZAMPESA", mapOf("phone" to phoneNumber)) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = phoneNumber.length >= 10,
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.GrowthGreen),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text("PAY NOW (TZS)", style = AFMTextStyles.textMD.copy(fontWeight = FontWeight.Black))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onCancel) {
                    Text("ABORT TRANSACTION", color = FameColors.AlertRed, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black)
                }
            }

            if (uiState.paymentResult is PaymentResult.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    (uiState.paymentResult as PaymentResult.Error).message.uppercase(),
                    color = FameColors.AlertRed,
                    style = AFMTextStyles.textXS,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
