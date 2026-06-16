package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    showAction: Boolean = actionText != null && onActionClick != null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.xs, vertical = Dimensions.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = FameColors.ChampionsGold,
            modifier = Modifier
                .padding(vertical = Dimensions.xs)
        )

        if (showAction) {
            Text(
                text = actionText ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = FameColors.AfroSunOrange,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onActionClick?.invoke() }
                    .padding(vertical = Dimensions.xs, horizontal = Dimensions.sm)
            )
        }
    }
}

@Composable
fun SectionHeaderWithIcon(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.xs, vertical = Dimensions.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.invoke()
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = FameColors.ChampionsGold
            )
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = FameColors.AfroSunOrange,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onActionClick() }
                    .padding(vertical = Dimensions.xs, horizontal = Dimensions.sm)
            )
        }
    }
}