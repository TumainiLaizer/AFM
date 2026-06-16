package com.fameafrica.afm.ui.screen.career

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.data.database.entities.LeaguesEntity

@Composable
fun CountryInfoPanel(

    countryName: String?,
    leagues: List<LeaguesEntity>,
    onLeagueSelected: (LeaguesEntity) -> Unit

) {

    if (countryName == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B5E20)
        )
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = countryName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {

                items(leagues.size) { index ->

                    val league = leagues[index]

                    Button(
                        onClick = { onLeagueSelected(league) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {

                        Text(league.name)

                    }

                }

            }

        }

    }

}