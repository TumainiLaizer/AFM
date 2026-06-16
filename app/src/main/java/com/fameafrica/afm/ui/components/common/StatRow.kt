package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun StatRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    valueColor: Color = if (isHighlighted) FameColors.ChampionsGold else FameColors.WarmIvory,
    icon: @Composable (() -> Unit)? = null,
    showDivider: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.invoke()
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FameColors.MutedParchment
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                modifier = Modifier.padding(start = Dimensions.sm)
            )
        }

        if (showDivider) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(vertical = Dimensions.xs)
                    .background(FameColors.BaobabBrown.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
fun StatRowWithProgress(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    maxValue: Int = 100,
    showPercentage: Boolean = true,
    progressColor: Color = FameColors.PitchGreen
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = FameColors.MutedParchment
            )

            if (showPercentage) {
                Text(
                    text = "$value%",
                    style = MaterialTheme.typography.titleSmall,
                    color = FameColors.WarmIvory
                )
            }
        }

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(FameColors.SurfaceLight)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(value.toFloat() / maxValue)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(progressColor)
            )
        }
    }
}

@Composable
fun StatGrid(
    modifier: Modifier = Modifier,
    stats: List<Pair<String, String>>,
    columns: Int = 2
) {
    val rows = stats.chunked(columns)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.md)
    ) {
        rows.forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.md)
            ) {
                rowStats.forEach { stat ->
                    StatCell(
                        label = stat.first,
                        value = stat.second,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Add empty spaces if row is incomplete
                repeat(columns - rowStats.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Dimensions.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.xs)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = FameColors.ChampionsGold,
                    modifier = Modifier.size(Dimensions.iconSm)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FameColors.MutedParchment
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = FameColors.WarmIvory
        )
    }
}

@Composable
fun StatBadge(
    modifier: Modifier = Modifier,
    value: String,
    label: String? = null,
    backgroundColor: Color = FameColors.SurfaceLight.copy(alpha = 0.5f)
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = Dimensions.md, vertical = Dimensions.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.xs)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = FameColors.ChampionsGold
        )

        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = FameColors.MutedParchment
            )
        }
    }
}