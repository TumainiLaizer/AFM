package com.fameafrica.afm.ui.screen.squad

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.AFM2026Theme

@Preview(showBackground = true, name = "Player Detail Content Preview")
@Composable
fun PlayerDetailContentPreview() {
    AFM2026Theme {
        Surface {
            PlayerDetailContent(
                uiState = createMockPlayerDetailUiState()
            )
        }
    }
}

@Composable
fun PlayerDetailContent(
    uiState: PlayerDetailUiState
) {
    Scaffold(
        topBar = {
            Text(text = "Player Details", modifier = Modifier.padding(16.dp))
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            uiState.player?.let { player ->
                item {
                    PlayerHeroSection(
                        player = player,
                        playerStatus = uiState.playerStatus,
                        teamForm = uiState.teamForm,
                        currentGameDate = uiState.currentGameDate
                    )
                }
                item {
                    QuickStatsRow(
                        condition = uiState.playerStatus?.condition ?: 0,
                        fatigue = uiState.playerStatus?.fatigue ?: 0,
                        form = player.form,
                        morale = player.morale,
                        formattedValue = player.formattedValue
                    )
                }
                item {
                    AttributeSection(
                        attributes = uiState.attributes,
                        attributeTrends = uiState.attributeTrends,
                        positionCategory = uiState.player.positionCategory
                    )
                }
            }
        }
    }
}

private fun createMockPlayerDetailUiState(): PlayerDetailUiState {
    val mockPlayer = PlayerDetailUiModel(
        id = 1,
        name = "Victor Osimhen",
        age = 25,
        height = 185,
        position = "ST",
        nationality = "Nigeria",
        nationalityFlag = "assets/flags/Nigeria.webp",
        shirtNumber = 9,
        preferredFoot = "Right",
        overallRating = 88,
        potential = 92,
        form = 8,
        morale = 85,
        appearances = 24,
        goals = 18,
        assists = 4,
        cleanSheets = 0,
        isCaptain = true,
        isViceCaptain = false,
        marketValue = 110000000,
        formattedValue = "€110M",
        wage = 150000.0,
        formattedWage = "€150K",
        contractExpiry = "2028-06-30",
        injuryStatus = "Bruised Ankle",
        personality = "Professional",
        archetype = "Target Man",
        experience = 450,
        positionCategory = "Forward",
        primaryRole = "ST",
        secondaryRole = "SS"
    )

    val mockStatus = PlayerStatusUiModel(
        condition = 85,
        fatigue = 15,
        sharpness = 90,
        happiness = HappinessLevel.VERY_HAPPY,
        squadStatus = SquadStatus.STAR_PLAYER,
        isOnTransferList = false,
        isLoanedOut = false,
        parentClub = null,
        daysSinceLastMatch = 3,
        consecutiveStarts = 5,
        formTrend = FormTrend.RISING
    )

    val mockAttributes = PlayerAttributesUiModel(
        finishing = 92, passing = 75, dribbling = 84, crossing = 65, heading = 90,
        longShots = 78, defending = 45, skill = 80, pace = 94, stamina = 88,
        strength = 85, acceleration = 93, agility = 86, composure = 85, decisions = 82,
        leadership = 80, vision = 76, workRate = "High/Medium", positioning = 94,
        anticipation = 91, creativity = 72, teamwork = 78, aggression = 82,
        goalkeeping = 10, aerialAbility = 10, reflexes = 10, handling = 10, commandOfArea = 10,
        kicking = 10
    )

    return PlayerDetailUiState(
        isLoading = false,
        player = mockPlayer,
        playerStatus = mockStatus,
        attributes = mockAttributes,
        seasonStats = SeasonStatsUiModel(
            matches = 24, goals = 18, assists = 4, manOfMatch = 6,
            yellowCards = 2, redCards = 0, passAccuracy = 78, tackles = 5,
            shots = 62, shotsOnTarget = 45, fouls = 12, offsides = 8,
            minutesPlayed = 2100, expectedGoals = 16.5, expectedAssists = 3.2,
            goalConversionRate = 29, cleanSheets = 0
        ),
        injuryHistory = listOf(
            InjuryUiModel(
                type = "Bruised Ankle",
                severity = "Minor",
                date = "2025-10-15",
                days = 5,
                injuryStatus = "INJURED",
                recoveryTime = 3
            )
        )
    )
}
