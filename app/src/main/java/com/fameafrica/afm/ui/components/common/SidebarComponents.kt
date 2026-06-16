package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fameafrica.afm.ui.theme.ClubThemeConfig
import com.fameafrica.afm.ui.theme.FameColors
import java.util.Locale

/* =========================
   HEADER (Broadcast Style)
========================= */

@Composable
fun SidebarBroadcastHeader(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    clubTheme: ClubThemeConfig? = null
) {
    val primaryColor = clubTheme?.primaryColor ?: MaterialTheme.colorScheme.primary

    Surface(
        tonalElevation = 0.dp,
        color = FameColors.HeaderDark,
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Square (Chairman Mode Style)
            Surface(
                modifier = Modifier.size(36.dp),
                color = FameColors.DeepNavyBlack,
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                subtitle?.let {
                    Text(
                        text = it.uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = primaryColor.copy(alpha = 0.8f)
                        ),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Optional actions
            actions?.invoke(this)
        }
    }
}

/* =========================
   SECTION TITLE
========================= */

@Composable
fun SidebarSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    clubTheme: ClubThemeConfig? = null
) {
    val secondaryColor = clubTheme?.secondaryColor ?: MaterialTheme.colorScheme.secondary
    
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                letterSpacing = 1.5.sp
            ),
            color = secondaryColor,
            fontWeight = FontWeight.ExtraBold
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 2.dp).width(24.dp),
            thickness = 2.dp,
            color = secondaryColor
        )
    }
}

/* =========================
   INFO ROW (Reusable)
========================= */

@Composable
fun SidebarInfoRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    clubTheme: ClubThemeConfig? = null
) {
    val secondaryColor = clubTheme?.secondaryColor ?: MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = if (highlight) secondaryColor else Color.White,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/* =========================
   CARD BLOCK (NEW)
========================= */

@Composable
fun SidebarCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.03f),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    borderWidth: Dp = 0.5.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            content()
        }
    }
}
