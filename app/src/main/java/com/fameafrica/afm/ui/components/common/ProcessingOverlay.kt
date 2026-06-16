package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun ProcessingOverlay(
    modifier: Modifier = Modifier
) {
    val coachDrawables = remember {
        listOf(
            R.drawable.coach_female,
            R.drawable.coach_male_charismatic,
            R.drawable.coach_male_east,
            R.drawable.coach_male_north,
            R.drawable.coach_male_official,
            R.drawable.coach_male_south,
            R.drawable.coach_male_west
        )
    }
    
    val selectedCoach = remember { coachDrawables.random() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = selectedCoach),
                contentDescription = "Coach",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = FameColors.TrophyGold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("PROCESSING...", style = AFMTextStyles.sectionHeader, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        }
    }
}
