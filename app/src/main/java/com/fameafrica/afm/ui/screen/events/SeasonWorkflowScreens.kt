package com.fameafrica.afm.ui.screen.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.AFMTextStyles

@Composable
fun LeagueKickoffScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(FameColors.StadiumBlack)) {
        AfricanGeometricPattern(modifier = Modifier.fillMaxSize().alpha(0.05f), color = FameColors.TrophyGold)
        
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("NBC PREMIER LEAGUE", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, letterSpacing = 4.sp)
            Text("SEASON KICKOFF", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Add some "Predictions" or "Objectives" here later
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("START SEASON", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DeadlineDayScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AfricanGeometricPattern(modifier = Modifier.fillMaxSize().alpha(0.05f), color = FameColors.AlertRed)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("6 HOURS REMAINING", style = AFMTextStyles.textXS, color = FameColors.AlertRed, letterSpacing = 4.sp)
            Text("TRANSFER DEADLINE", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("OPEN TRANSFER HUB", color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SeasonReviewScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(FameColors.StadiumBlack)) {
        AfricanGeometricPattern(modifier = Modifier.fillMaxSize().alpha(0.05f), color = FameColors.TrophyGold)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SEASON 2024/25", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, letterSpacing = 4.sp)
            Text("SEASON REVIEW", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun AwardsGalaScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(FameColors.StadiumBlack)) {
        AfricanGeometricPattern(modifier = Modifier.fillMaxSize().alpha(0.05f), color = FameColors.TrophyGold)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("AFRICAN FOOTBALL AWARDS", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, letterSpacing = 4.sp)
            Text("AWARDS GALA", style = AFMTextStyles.textLG, color = Color.White, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("COMPLETE SEASON", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}
