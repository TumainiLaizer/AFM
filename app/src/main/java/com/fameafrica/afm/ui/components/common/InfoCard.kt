// ui/components/common/InfoCard.kt
package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    icon: @Composable (() -> Unit)? = null
) {
    GlassPanel(
        modifier = modifier,
        alpha = 0.7f,
        cornerRadius = 16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke() ?: Spacer(modifier = Modifier.width(Dimensions.sm))

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = FameColors.MutedParchment
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FameColors.WarmIvory
                )
            }
        }
    }
}

@Composable
fun InfoCardWithAction(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    actionText: String,
    onActionClick: () -> Unit,
    icon: ImageVector? = null
) {
    GlassPanel(
        modifier = modifier,
        alpha = 0.7f,
        cornerRadius = 16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = FameColors.ChampionsGold,
                        modifier = Modifier.size(Dimensions.iconMd)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimensions.xs)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = FameColors.MutedParchment
                    )
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FameColors.WarmIvory
                    )
                }
            }

            ActionButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}

@Composable
fun StatsInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    stats: List<Pair<String, String>>,
    columns: Int = 2
) {
    GlassPanel(
        modifier = modifier,
        alpha = 0.7f,
        cornerRadius = 16
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.md)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = FameColors.ChampionsGold
            )

            StatGrid(
                stats = stats,
                columns = columns
            )
        }
    }
}

@Composable
fun AlertInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    alertType: AlertType = AlertType.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = when (alertType) {
        AlertType.Success -> Triple(FameColors.Success, FameColors.Success.copy(alpha = 0.2f), FameColors.WarmIvory)
        AlertType.Warning -> Triple(FameColors.Warning, FameColors.Warning.copy(alpha = 0.2f), FameColors.WarmIvory)
        AlertType.Error -> Triple(FameColors.Error, FameColors.Error.copy(alpha = 0.2f), FameColors.WarmIvory)
        AlertType.Info -> Triple(FameColors.Info, FameColors.Info.copy(alpha = 0.2f), FameColors.WarmIvory)
    }

    GlassPanel(
        modifier = modifier,
        alpha = 0.8f,
        cornerRadius = 16
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.first)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.first
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.third
                )
            }

            if (actionText != null && onActionClick != null) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.first,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimensions.sm))
                        .clickable { onActionClick() }
                        .padding(Dimensions.sm)
                )
            }
        }
    }
}

enum class AlertType {
    Success,
    Warning,
    Error,
    Info
}

// Simple ActionButton composable for InfoCardWithAction
@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.sm))
            .clickable { onClick() }
            .background(FameColors.PitchGreen.copy(alpha = 0.2f))
            .padding(horizontal = Dimensions.md, vertical = Dimensions.sm)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = FameColors.ChampionsGold
        )
    }
}