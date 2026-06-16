package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Premium Glass Panel UI component for AFM2026.
 * Features: Frosted glass effect, subtle border, and professional styling.
 */

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    alpha: Float = 0.05f,
    cornerRadius: Int = 2,
    border: BorderStroke? = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor.copy(alpha = alpha),
        shape = RoundedCornerShape(cornerRadius.dp),
        border = border,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}
