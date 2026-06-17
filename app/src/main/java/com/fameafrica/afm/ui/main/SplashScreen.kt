package com.fameafrica.afm.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.AfricanBackground
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.ImmersiveModeManager
import com.fameafrica.afm.utils.ImmersiveModeManager.immersiveRoot
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    ImmersiveModeManager.ImmersiveScreen()
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "AlphaAnimation"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        onSplashFinished()
    }

    AfricanBackground(
        modifier = Modifier.immersiveRoot(),
        backgroundColor = FameColors.DeepNavyBlack
    ) {
        // Use safeDrawingPadding to handle status bar, navigation bar, and cutouts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alphaAnim.value)
            ) {
                // High Fidelity Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_round),    
                    contentDescription = "AFM Logo",
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AFM 2026",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = FameColors.ChampionsGold
                )

                Text(
                    text = "AFRICAN FOOTBALL MANAGER",
                    style = AFMTextStyles.tableHeader,
                    color = FameColors.WarmIvory,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                GlassPanel(
                    modifier = Modifier.width(240.dp),
                    cornerRadius = 20
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PREPARING AFRICAN TALENT",
                            style = AFMTextStyles.statLabel,
                            color = FameColors.ChampionsGold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(CircleShape),
                            color = FameColors.ChampionsGold,
                            trackColor = FameColors.PitchGreen.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Professional Footer
            Text(
                text = "FAME AFRICA™ | LICENSED BY CAF",
                style = AFMTextStyles.statLabel,
                color = FameColors.MutedParchment,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .alpha(alphaAnim.value)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    AFM2026Theme {
        SplashScreen(onSplashFinished = {})
    }
}
