package com.fameafrica.afm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.Dimensions
import com.fameafrica.afm.ui.theme.FameBackground

/**
 * A shared layout for Dashboard screens.
 * Provides a standardized structure for headers, hero panels, and columns.
 * Supports tablet/desktop scaling via slot-based injection.
 */
@Composable
fun SharedDashboardLayout(
    header: @Composable () -> Unit,
    advanceButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    heroPanel: (@Composable () -> Unit)? = null,
    leftColumn: (LazyListScope.() -> Unit)? = null,
    rightColumn: (LazyListScope.() -> Unit)? = null,
    bottomPanel: (@Composable () -> Unit)? = null,
    content: (LazyListScope.() -> Unit)? = null
) {
    FameBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimensions.md)
        ) {
            header()
            
            Spacer(modifier = Modifier.height(Dimensions.xs))
            
            advanceButton()

            Spacer(modifier = Modifier.height(Dimensions.sm))

            if (heroPanel != null) {
                heroPanel()
                Spacer(modifier = Modifier.height(Dimensions.md))
            }

            Row(modifier = Modifier.weight(1f)) {
                // Adaptive layout: if both columns are provided, they share space on tablet
                // For now, we focus on vertical feed for mobile
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.md),
                    contentPadding = PaddingValues(bottom = Dimensions.xl)
                ) {
                    leftColumn?.invoke(this)
                    content?.invoke(this)
                }

                if (rightColumn != null) {
                    Spacer(modifier = Modifier.width(Dimensions.md))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.md),
                        contentPadding = PaddingValues(bottom = Dimensions.xl)
                    ) {
                        rightColumn.invoke(this)
                    }
                }
            }

            if (bottomPanel != null) {
                bottomPanel()
                Spacer(modifier = Modifier.height(Dimensions.md))
            }
        }
    }
}

/**
 * Standard Advance Button for Dashboard.
 */
@Composable
fun DashboardAdvanceButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = AFMTextStyles.textLG,
            fontWeight = FontWeight.Black
        )
    }
}
