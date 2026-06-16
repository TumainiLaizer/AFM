package com.fameafrica.afm.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.domain.manager.GameManager
import com.fameafrica.afm.ui.components.common.AfricanBackground
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.ui.theme.AFMTextStyles

data class SidebarItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int = 0
)

data class SidebarSection(
    val title: String,
    val items: List<SidebarItem>
)

@Composable
fun SidebarDrawerContent(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    gameState: GameManager.GameState = GameManager.GameState.Loading,
    manager: ManagersEntity? = null,
    managerName: String = "Manager",
    clubName: String = "Club",
    reputation: String = "Local",
    balance: String = "€0",
    nextMatch: String = "No fixture",
    notifications: Int = 0,
    clubTheme: ClubThemeConfig? = null
) {
    val activeState = gameState as? GameManager.GameState.Active
    val effectiveManagerName = manager?.name ?: managerName
    val effectiveClubId = activeState?.context?.teamId ?: 0
    val effectiveClubName = activeState?.context?.teamName ?: clubName
    val effectiveReputation = manager?.reputationLevel ?: reputation
    val domesticCup = activeState?.context?.domesticCupName ?: "FA Cup"
    val leagueName = activeState?.context?.leagueName ?: "Premier League"
    val managerAvatar = manager?.faceImage

    // Phase 1 Optimization: Remember sections to avoid recreation on every recomposition
    val sections = remember(notifications, effectiveClubId, effectiveClubName, leagueName, domesticCup) {
        listOf(
            SidebarSection(
                "MANAGEMENT", listOf(
                    SidebarItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard.route),
                    SidebarItem("Inbox", Icons.Default.Email, Screen.Notifications.route, badgeCount = notifications),
                    SidebarItem("Boardroom", Icons.Default.Work, Screen.Board.route),
                    SidebarItem("News Feed", Icons.Default.Newspaper, Screen.News.route)
                )
            ),
            SidebarSection(
                "SQUAD & TRAINING", listOf(
                    SidebarItem("Team Tactics", Icons.Default.SportsSoccer, Screen.Tactics.route),
                    SidebarItem("Training Center", Icons.Default.FitnessCenter, Screen.Training.route),
                    SidebarItem("Squad List", Icons.Default.Groups, Screen.Squad.route),
                    SidebarItem("Youth Academy", Icons.Default.School, Screen.YouthAcademy.route),
                    SidebarItem("Staff Hub", Icons.Default.AssignmentInd, Screen.Staff.route)
                )
            ),
            SidebarSection(
                "FINANCE & FACILITIES", listOf(
                    SidebarItem("Club Finances", Icons.AutoMirrored.Filled.TrendingUp, Screen.Finances.route),
                    SidebarItem("Infrastructure", Icons.Default.Build, Screen.Infrastructure.route),
                    SidebarItem("Sponsorships", Icons.Default.Handshake, Screen.SponsorNegotiation.withArgs("0")),
                    SidebarItem("Fan Hub", Icons.Default.People, Screen.Fans.withArgs(effectiveClubId.toString(), effectiveClubName))
                )
            ),
            SidebarSection(
                "TRANSFERS", listOf(
                    SidebarItem("Transfer Market", Icons.Default.SwapHoriz, Screen.Transfers.route),
                    SidebarItem("Scouting Network", Icons.Default.Visibility, Screen.Scout.route),
                    SidebarItem("Negotiations", Icons.Default.AttachMoney, Screen.Negotiation.withArgs("0")),
                    SidebarItem("Shortlist", Icons.Default.Star, Screen.Scout.route)
                )
            ),
            SidebarSection(
                "COMPETITIONS", listOf(
                    SidebarItem("League Table", Icons.Default.FormatListNumbered, Screen.LeagueTable.withArgs(leagueName)),
                    SidebarItem("Cup Competitions", Icons.Default.EmojiEvents, Screen.CupDraw.withArgs(domesticCup)),
                    SidebarItem("World News", Icons.Default.Public, Screen.World.route),
                    SidebarItem("National Teams", Icons.Default.Flag, Screen.National.route)
                )
            ),
            SidebarSection(
                "SYSTEM", listOf(
                    SidebarItem("Load Game", Icons.Default.FolderOpen, Screen.Main.route),
                    SidebarItem("Settings", Icons.Default.Settings, Screen.Settings.route),
                    SidebarItem("Save Game", Icons.Default.Save, Screen.Main.route)
                )
            )
        )
    }

    AfricanBackground(
        backgroundColor = FameColors.DeepNavyBlack,
        showPatterns = true,
        showMaasaiBorders = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ManagerProfileCard(
                managerName = effectiveManagerName,
                managerAvatar = managerAvatar,
                clubName = effectiveClubName,
                reputation = effectiveReputation,
                balance = balance,
                nextMatch = nextMatch,
                clubTheme = clubTheme
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                sections.forEach { section: SidebarSection ->
                    item {
                        Text(
                            text = section.title,
                            style = AFMTextStyles.textXXS,
                            color = FameColors.TrophyGold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    items(section.items) { item: SidebarItem ->
                        SidebarRow(
                            item = item,
                            isSelected = currentRoute == item.route,
                            onClick = { onItemClick(item.route) },
                            clubTheme = clubTheme
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ManagerProfileCard(
    managerName: String,
    managerAvatar: String?,
    clubName: String,
    reputation: String,
    balance: String,
    nextMatch: String,
    clubTheme: ClubThemeConfig? = null
) {
    val context = LocalContext.current
    val avatarResId = if (managerAvatar != null) context.resources.getIdentifier(managerAvatar.substringBefore("."), "drawable", context.packageName) else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, FameColors.TrophyGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (avatarResId != 0) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = FameColors.TrophyGold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = managerName.uppercase(),
                    style = AFMTextStyles.textMD,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = clubName.uppercase(),
                    style = AFMTextStyles.textXXS,
                    color = FameColors.TrophyGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reputation.uppercase(),
                    style = AFMTextStyles.textXXS,
                    color = FameColors.MutedParchment
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("BANK BALANCE", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(balance, style = AFMTextStyles.statValue, color = FameColors.GrowthGreen)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NEXT FIXTURE", style = AFMTextStyles.textXXS, color = FameColors.MutedParchment)
                Text(nextMatch.uppercase(), style = AFMTextStyles.statValue, color = Color.White, maxLines = 1)
            }
        }
    }
}

@Composable
fun SidebarRow(
    item: SidebarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    clubTheme: ClubThemeConfig? = null
) {
    val activeColor = FameColors.TrophyGold
    val inactiveColor = FameColors.MutedParchment

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Color.White.copy(alpha = 0.02f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else inactiveColor.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.title.uppercase(),
            style = AFMTextStyles.textXS,
            color = if (isSelected) Color.White else inactiveColor,
            modifier = Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
        )
        if (item.badgeCount > 0) {
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = FameColors.AlertRed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.badgeCount.toString(),
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
