package com.fameafrica.afm.ui.screen.infrastructure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.data.database.entities.InfrastructureUpgradesEntity
import com.fameafrica.afm.data.database.entities.UpgradeType
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.theme.*
import java.util.Locale
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfrastructureContent(
    uiState: InfrastructureUiState,
    onBack: () -> Unit,
    onUpgrade: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "CLUB INFRASTRUCTURE",
                            style = AFMTextStyles.textLG.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FameColors.HeaderDark
                    )
                )
                // Sub-header/status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        StadiumSummaryCard(
                            name = uiState.stadiumName,
                            capacity = uiState.stadiumCapacity
                        )
                    }

                    val activeUpgrades = uiState.upgrades.filter { it.isInProgress || it.isPending }
                    if (activeUpgrades.isNotEmpty()) {
                        item { InfrastructureSectionHeader(title = "ACTIVE PROJECTS") }
                        items(activeUpgrades, key = { it.id }) { upgrade ->
                            ActiveUpgradeCard(upgrade = upgrade)
                        }
                    }

                    item { InfrastructureSectionHeader(title = "FACILITY MANAGEMENT") }

                    item {
                        AvailableUpgradeItem(
                            title = "STADIUM EXPANSION",
                            description = "Increase seating capacity to boost matchday revenue.",
                            icon = Icons.Default.Stadium,
                            level = uiState.levels["STADIUM"] ?: 1,
                            onUpgrade = { onUpgrade(UpgradeType.STADIUM.value) }
                        )
                    }

                    item {
                        AvailableUpgradeItem(
                            title = "TRAINING FACILITIES",
                            description = "Better equipment improves player development and attribute growth.",
                            icon = Icons.Default.FitnessCenter,
                            level = uiState.levels["TRAINING_FACILITY"] ?: 1,
                            onUpgrade = { onUpgrade(UpgradeType.TRAINING_FACILITY.value) }
                        )
                    }

                    item {
                        AvailableUpgradeItem(
                            title = "YOUTH ACADEMY",
                            description = "Higher level academies produce more talented regens.",
                            icon = Icons.Default.School,
                            level = uiState.levels["YOUTH_ACADEMY"] ?: 1,
                            onUpgrade = { onUpgrade(UpgradeType.YOUTH_ACADEMY.value) }
                        )
                    }

                    item {
                        AvailableUpgradeItem(
                            title = "MEDICAL CENTER",
                            description = "Reduce injury duration and improve recovery rates.",
                            icon = Icons.Default.LocalHospital,
                            level = uiState.levels["MEDICAL_CENTER"] ?: 1,
                            onUpgrade = { onUpgrade(UpgradeType.MEDICAL_CENTER.value) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfrastructureSectionHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun StadiumSummaryCard(name: String, capacity: Int) {
    SidebarCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Stadium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name.uppercase(),
                    style = AFMTextStyles.textMD,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "STADIUM CAPACITY: ${NumberFormat.getNumberInstance(Locale.US).format(capacity)}",
                    style = AFMTextStyles.textXS,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ActiveUpgradeCard(upgrade: InfrastructureUpgradesEntity) {
    SidebarCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = upgrade.upgradeName.uppercase(),
                    style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Surface(
                    color = if (upgrade.isPending) Color(0xFFFFA000).copy(alpha = 0.1f) else FameColors.GrowthGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(0.5.dp, if (upgrade.isPending) Color(0xFFFFA000) else FameColors.GrowthGreen)
                ) {
                    Text(
                        text = if (upgrade.isPending) "PENDING" else "IN PROGRESS",
                        style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
                        color = if (upgrade.isPending) Color(0xFFFFA000) else FameColors.GrowthGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "COMPLETION DATE",
                    style = AFMTextStyles.textXS,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = upgrade.completionDate,
                    style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { 0.5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun AvailableUpgradeItem(
    title: String,
    description: String,
    icon: ImageVector,
    level: Int,
    onUpgrade: () -> Unit
) {
    SidebarCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title.uppercase(),
                        style = AFMTextStyles.textSM.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "CURRENT LEVEL: $level/5",
                        style = AFMTextStyles.textXXS,
                        color = FameColors.ChampionsGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = AFMTextStyles.textXS,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (level < 5) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INITIATE UPGRADE",
                            style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    color = FameColors.ChampionsGold.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, FameColors.ChampionsGold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "MAX LEVEL REACHED",
                            style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Bold),
                            color = FameColors.ChampionsGold
                        )
                    }
                }
            }
        }
    }
}
