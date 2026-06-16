package com.fameafrica.afm.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val FameShapes = Shapes(

    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

object ComponentShapes {

    val card = RoundedCornerShape(14.dp)

    val glassPanel = RoundedCornerShape(16.dp)

    val playerCard = RoundedCornerShape(18.dp)

    val matchCard = RoundedCornerShape(16.dp)

    val chip = RoundedCornerShape(50.dp)

    val pitch = RoundedCornerShape(0.dp)
}