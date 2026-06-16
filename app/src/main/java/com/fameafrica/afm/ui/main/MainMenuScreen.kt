package com.fameafrica.afm.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.components.common.AfricanBackground
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.ui.util.ImmersiveModeManager
import com.fameafrica.afm.ui.util.ImmersiveModeManager.immersiveRoot

@Composable
fun MainMenuScreen(
    hasActiveCareer: Boolean = false,
    onContinueClick: () -> Unit,
    onNewCareerClick: () -> Unit,
    onLoadGameClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var showOverwriteWarning by remember { mutableStateOf(false) }

    if (showOverwriteWarning) {
        AlertDialog(
            onDismissRequest = { showOverwriteWarning = false },
            title = { Text("NEW CAREER", color = FameColors.AlertRed, fontWeight = FontWeight.Black) },
            text = { Text("Starting a new career will overwrite your current save. This cannot be undone. Are you sure you want to proceed?") },
            confirmButton = {
                TextButton(onClick = { 
                    showOverwriteWarning = false
                    onNewCareerClick()
                }) {
                    Text("OVERWRITE", color = FameColors.AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteWarning = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = FameColors.StadiumBlack,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    ImmersiveModeManager.ImmersiveScreen()
    AfricanBackground(
        modifier = Modifier.immersiveRoot(),
        backgroundColor = FameColors.DeepNavyBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // Professional Logo and Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AFM",
                    style = AFMTextStyles.textLG.copy(
                        fontSize = 84.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-4).sp
                    ),
                    color = Color(0xFFE5B134),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "AFRICAN FOOTBALL MANAGER",
                    style = AFMTextStyles.textXS,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Center Tagline (Grouped with Logo for better hierarchy)
                Text(
                    text = stringResource(R.string.tagline).uppercase(),
                    style = AFMTextStyles.textXS.copy(fontSize = 10.sp),
                    color = Color(0xFFE5B134),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Professional Menu Section
            SidebarCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MainMenuButton(
                        text = "CONTINUE CAREER",
                        onClick = onContinueClick,
                        isPrimary = true
                    )

                    MainMenuButton(
                        text = "NEW CAREER",
                        onClick = {
                            if (hasActiveCareer) showOverwriteWarning = true
                            else onNewCareerClick()
                        },
                        isPrimary = false
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MainMenuButton(
                            text = "LOAD GAME",
                            onClick = onLoadGameClick,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                        MainMenuButton(
                            text = "SETTINGS",
                            onClick = onSettingsClick,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Version Info
            Text(
                text = "FAME AFRICA™ | VERSION 2026.1 | MANAGER EDITION",
                style = AFMTextStyles.textXS.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MainMenuButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isPrimary) FameColors.TrophyGold else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isPrimary) Color.Black else FameColors.WarmIvory
    val borderColor = if (isPrimary) FameColors.TrophyGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = AFMTextStyles.textSM,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    AFM2026Theme {
        MainMenuScreen(
            hasActiveCareer = true,
            onContinueClick = {},
            onNewCareerClick = {},
            onLoadGameClick = {},
            onSettingsClick = {}
        )
    }
}
