package com.fameafrica.afm.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.AutoSaveFrequency
import com.fameafrica.afm.data.database.entities.DifficultyLevel
import com.fameafrica.afm.data.database.entities.GameSettingsEntity
import com.fameafrica.afm.ui.components.common.GlassPanel
import com.fameafrica.afm.ui.theme.*

@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentTheme = uiState.settings?.themePreset ?: FootballThemePreset.MANAGER_MODE

    AFM2026Theme(themePreset = currentTheme) {
        Surface(color = Color.Transparent) {
            SettingScreenContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onUpdateDifficulty = { viewModel.updateDifficulty(it) },
                onUpdateMatchSpeed = { viewModel.updateMatchSpeed(it) },
                onToggleMusic = { viewModel.toggleMusic(it) },
                onToggleSound = { viewModel.toggleSound(it) },
                onUpdateMusicVolume = { viewModel.updateMusicVolume(it) },
                onUpdateAmbienceVolume = { viewModel.updateAmbienceVolume(it) },
                onToggleAnimations = { viewModel.toggleAnimations(it) },
                onUpdateLanguage = { viewModel.updateLanguage(it) },
                onUpdateCurrency = { viewModel.updateCurrency(it) },
                onToggleAutosave = { viewModel.toggleAutosave(it) },
                onUpdateAutosaveFrequency = { viewModel.updateAutosaveFrequency(it) },
                onToggleNotifications = { viewModel.toggleNotifications(it) },
                onUpdateThemePreset = { viewModel.updateThemePreset(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onUpdateDifficulty: (String) -> Unit,
    onUpdateMatchSpeed: (Int) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onUpdateMusicVolume: (Float) -> Unit,
    onUpdateAmbienceVolume: (Float) -> Unit,
    onToggleAnimations: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onToggleAutosave: (Boolean) -> Unit,
    onUpdateAutosaveFrequency: (Int) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateThemePreset: (FootballThemePreset) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "PREFERENCES", 
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FameColors.ChampionsGold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                uiState.settings?.let { settings ->
                    
                    SettingsSection("VISUAL THEME", Icons.Default.Palette) {
                        ThemeSelector(
                            selectedTheme = settings.themePreset,
                            onThemeSelected = onUpdateThemePreset
                        )
                    }

                    SettingsSection("GAMEPLAY ENGINE", Icons.Default.SportsSoccer) {
                        DropdownSetting(
                            label = "DIFFICULTY LEVEL",
                            options = DifficultyLevel.entries.map { it.value },
                            selectedOption = settings.difficulty,
                            onOptionSelected = onUpdateDifficulty
                        )
                        DropdownSetting(
                            label = "MATCH SIMULATION SPEED",
                            options = listOf("Slow", "Normal", "Fast", "Instant"),
                            selectedOption = settings.matchSpeedText,
                            onOptionSelected = { 
                                val speed = when(it) {
                                    "Slow" -> 0
                                    "Normal" -> 1
                                    "Fast" -> 2
                                    "Instant" -> 3
                                    else -> 1
                                }
                                onUpdateMatchSpeed(speed)
                            }
                        )
                    }

                    SettingsSection("AUDIO & EFFECTS", Icons.AutoMirrored.Filled.VolumeUp) {
                        SwitchSetting("Background Music", settings.music, onToggleMusic, Icons.Default.MusicNote)
                        if (settings.music) {
                            VolumeSliderSetting("Music Volume", uiState.musicVolume, onUpdateMusicVolume)
                        }
                        
                        SwitchSetting("Match Ambience", settings.soundEnabled, onToggleSound, Icons.AutoMirrored.Filled.VolumeUp)
                        if (settings.soundEnabled) {
                            VolumeSliderSetting("Crowd Volume", uiState.ambienceVolume, onUpdateAmbienceVolume)
                        }
                        
                        SwitchSetting("UI Animations", settings.animationsEnabled, onToggleAnimations, Icons.Default.Animation)
                    }

                    SettingsSection("LOCALIZATION", Icons.Default.Language) {
                        DropdownSetting(
                            label = "SYSTEM LANGUAGE",
                            options = listOf("English", "Swahili", "French", "Portuguese"),
                            selectedOption = when(settings.language) {
                                "en" -> "English"
                                "sw" -> "Swahili"
                                "fr" -> "French"
                                "pt" -> "Portuguese"
                                else -> "English"
                            },
                            onOptionSelected = {
                                val code = when(it) {
                                    "English" -> "en"
                                    "Swahili" -> "sw"
                                    "French" -> "fr"
                                    "Portuguese" -> "pt"
                                    else -> "en"
                                }
                                onUpdateLanguage(code)
                            }
                        )
                        DropdownSetting(
                            label = "CURRENCY DISPLAY",
                            options = listOf("EUR", "USD", "TZS", "NGN", "ZAR"),
                            selectedOption = settings.currency,
                            onOptionSelected = onUpdateCurrency
                        )
                    }

                    SettingsSection("DATA & SAVING", Icons.Default.CloudUpload) {
                        SwitchSetting("Auto-Save", settings.autosave, onToggleAutosave, Icons.Default.CloudUpload)
                        if (settings.autosave) {
                            DropdownSetting(
                                label = "AUTO-SAVE FREQUENCY",
                                options = AutoSaveFrequency.entries.map { it.description },
                                selectedOption = AutoSaveFrequency.entries.find { it.value == settings.autoSaveFrequency }?.description ?: "Weekly",
                                onOptionSelected = { desc ->
                                    val freq = AutoSaveFrequency.entries.find { it.description == desc }?.value ?: 2
                                    onUpdateAutosaveFrequency(freq)
                                }
                            )
                        }
                        SwitchSetting("Push Notifications", settings.notifications, onToggleNotifications, Icons.Default.NotificationsActive)
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AFRICAN FOOTBALL MANAGER 2026", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.primary)
                        Text("PRO EDITION v1.0.0", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = AFMTextStyles.tableHeader, color = MaterialTheme.colorScheme.onBackground)
        }
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelector(selectedTheme: FootballThemePreset, onThemeSelected: (FootballThemePreset) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("SELECT ATMOSPHERE", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedTheme.displayName,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(getThemeIcon(selectedTheme), null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                textStyle = AFMTextStyles.tableCell,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                FootballThemePreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        leadingIcon = { Icon(getThemeIcon(preset), null, tint = if (preset == selectedTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                        text = { Text(preset.displayName, style = AFMTextStyles.tableCell, color = if (preset == selectedTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                        onClick = { onThemeSelected(preset); expanded = false }
                    )
                }
            }
        }
    }
}

fun getThemeIcon(preset: FootballThemePreset): ImageVector {
    return when (preset) {
        FootballThemePreset.DEFAULT -> Icons.Default.Stadium
        FootballThemePreset.CHAIRMAN_MODE -> Icons.Default.Chair
        FootballThemePreset.MANAGER_MODE -> Icons.Default.Person
        FootballThemePreset.NEWS_MODE -> Icons.Default.Newspaper
        FootballThemePreset.CLASSIC_PITCH -> Icons.Default.Grass
        FootballThemePreset.GOLDEN_ERA -> Icons.Default.EmojiEvents
        FootballThemePreset.STREET_SOCCER -> Icons.Default.LocationCity
        FootballThemePreset.MIDNIGHT_STADIUM -> Icons.Default.NightsStay
        FootballThemePreset.RETRO_MANIA -> Icons.Default.History
    }
}

@Composable
fun SwitchSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(label, style = AFMTextStyles.tableCell, color = MaterialTheme.colorScheme.onSurface)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun VolumeSliderSetting(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(value * 100).toInt()}%", style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSetting(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = AFMTextStyles.statLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                textStyle = AFMTextStyles.tableCell,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = AFMTextStyles.tableCell, color = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                        onClick = { onOptionSelected(option); expanded = false }
                    )
                }
            }
        }
    }
}

// ============ PREVIEWS ============

@Preview(showBackground = true, name = "Default Theme Settings")
@Composable
fun PreviewSettingsDefault() {
    AFM2026Theme(themePreset = FootballThemePreset.DEFAULT) {
        Surface(color = FameColors.StadiumBlack) {
            SettingScreenContent(
                uiState = SettingsUiState(settings = GameSettingsEntity(themePreset = FootballThemePreset.DEFAULT), isLoading = false),
                onBackClick = {}, onUpdateDifficulty = {}, onUpdateMatchSpeed = {}, onToggleMusic = {}, onToggleSound = {},
                onUpdateMusicVolume = {}, onUpdateAmbienceVolume = {}, onToggleAnimations = {}, onUpdateLanguage = {},
                onUpdateCurrency = {}, onToggleAutosave = {}, onUpdateAutosaveFrequency = {}, onToggleNotifications = {}, onUpdateThemePreset = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Golden Era Theme Settings")
@Composable
fun PreviewSettingsGolden() {
    AFM2026Theme(themePreset = FootballThemePreset.GOLDEN_ERA) {
        Surface(color = FootballThemeColors.GoldenEraBackground) {
            SettingScreenContent(
                uiState = SettingsUiState(settings = GameSettingsEntity(themePreset = FootballThemePreset.GOLDEN_ERA), isLoading = false),
                onBackClick = {}, onUpdateDifficulty = {}, onUpdateMatchSpeed = {}, onToggleMusic = {}, onToggleSound = {},
                onUpdateMusicVolume = {}, onUpdateAmbienceVolume = {}, onToggleAnimations = {}, onUpdateLanguage = {},
                onUpdateCurrency = {}, onToggleAutosave = {}, onUpdateAutosaveFrequency = {}, onToggleNotifications = {}, onUpdateThemePreset = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Street Soccer Theme Settings")
@Composable
fun PreviewSettingsStreet() {
    AFM2026Theme(themePreset = FootballThemePreset.STREET_SOCCER) {
        Surface(color = FootballThemeColors.StreetSoccerBackground) {
            SettingScreenContent(
                uiState = SettingsUiState(settings = GameSettingsEntity(themePreset = FootballThemePreset.STREET_SOCCER), isLoading = false),
                onBackClick = {}, onUpdateDifficulty = {}, onUpdateMatchSpeed = {}, onToggleMusic = {}, onToggleSound = {},
                onUpdateMusicVolume = {}, onUpdateAmbienceVolume = {}, onToggleAnimations = {}, onUpdateLanguage = {},
                onUpdateCurrency = {}, onToggleAutosave = {}, onUpdateAutosaveFrequency = {}, onToggleNotifications = {}, onUpdateThemePreset = {}
            )
        }
    }
}
