package com.fameafrica.afm.ui.components.career

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFMTextStyles
import com.fameafrica.afm.ui.theme.FameColors
import com.fameafrica.afm.utils.FootballRegion
import com.fameafrica.afm.utils.NationalityItem
import com.fameafrica.afm.utils.NationalityUtils
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun NationalityWheel(
    selectedNationality: String,
    onNationalitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    radius: Dp = 115.dp
) {
    var selectedRegion by remember {
        mutableStateOf(
            NationalityUtils.getNationalityItem(selectedNationality)?.region ?: FootballRegion.EAST_AFRICA
        )
    }

    val regionalItems = remember(selectedRegion) {
        NationalityUtils.nationalityItems.filter { it.region == selectedRegion }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Regional Filter Tabs
        FederationTabs(
            selectedRegion = selectedRegion,
            onRegionSelected = { region ->
                selectedRegion = region
                // When switching region, pick the first country if current selection isn't in it
                val currentItem = NationalityUtils.getNationalityItem(selectedNationality)
                if (currentItem?.region != region) {
                    onNationalitySelected(regionalItems.firstOrNull()?.country ?: "")
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. The Interactive Wheel
        NationalityCarouselWheel(
            items = regionalItems,
            selectedNationality = selectedNationality,
            onNationalitySelected = onNationalitySelected,
            radius = radius
        )
    }
}

@Composable
fun FederationTabs(
    selectedRegion: FootballRegion,
    onRegionSelected: (FootballRegion) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedRegion.ordinal,
        containerColor = Color.Transparent,
        contentColor = FameColors.TrophyGold,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedRegion.ordinal]),
                color = FameColors.TrophyGold,
                height = 3.dp
            )
        }
    ) {
        FootballRegion.entries.forEach { region ->
            Tab(
                selected = selectedRegion == region,
                onClick = { onRegionSelected(region) },
                text = {
                    Text(
                        text = region.displayName.uppercase(),
                        style = AFMTextStyles.textXS,
                        fontWeight = if (selectedRegion == region) FontWeight.Black else FontWeight.Normal,
                        color = if (selectedRegion == region) FameColors.TrophyGold else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun NationalityCarouselWheel(
    items: List<NationalityItem>,
    selectedNationality: String,
    onNationalitySelected: (String) -> Unit,
    radius: Dp
) {
    val count = items.size
    if (count == 0) return

    val anglePerItem = 360f / count
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Find index of selected item in current list
    val selectedIndex = items.indexOfFirst { it.country == selectedNationality }.coerceAtLeast(0)
    val rotationAnimatable = remember { Animatable(-selectedIndex * anglePerItem) }

    // Sync when selection changes
    LaunchedEffect(selectedNationality, items) {
        val newIndex = items.indexOfFirst { it.country == selectedNationality }
        if (newIndex != -1) {
            val targetRotation = -newIndex * anglePerItem
            var diff = (targetRotation - rotationAnimatable.value) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            
            rotationAnimatable.animateTo(
                targetValue = rotationAnimatable.value + diff,
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BallRotation")
    val ballRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ballAngle"
    )

    Box(
        modifier = Modifier
            .size(radius * 2 + 120.dp)
            .pointerInput(items) {
                detectDragGestures(
                    onDragEnd = {
                        val currentRot = rotationAnimatable.value
                        val normalizedRot = ((-currentRot % 360f) + 360f) % 360f
                        val nearestIndex = (normalizedRot / anglePerItem).roundToInt() % count
                        
                        val snappedRot = -nearestIndex * anglePerItem
                        var diff = (snappedRot - currentRot) % 360f
                        if (diff > 180f) diff -= 360f
                        if (diff < -180f) diff += 360f
                        
                        scope.launch {
                            rotationAnimatable.animateTo(
                                currentRot + diff,
                                spring(stiffness = Spring.StiffnessMediumLow)
                            )
                            onNationalitySelected(items[nearestIndex].country)
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val dragFactor = 0.25f
                    val delta = dragAmount.x * dragFactor
                    scope.launch {
                        rotationAnimatable.snapTo(rotationAnimatable.value + delta)
                        
                        val normalizedRot = ((-(rotationAnimatable.value + delta) % 360f) + 360f) % 360f
                        val currentIndex = (normalizedRot / anglePerItem).roundToInt() % count
                        if (items[currentIndex].country != selectedNationality) {
                            onNationalitySelected(items[currentIndex].country)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Decorative background glow
        Box(
            modifier = Modifier
                .size(radius * 2)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FameColors.TrophyGold.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Central Soccer Ball (Interactive centerpiece)
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    rotationZ = ballRotation
                    shadowElevation = 40f
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.soccer_ball),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            
            // Premium Reflection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            center = androidx.compose.ui.geometry.Offset(50f, 50f)
                        ),
                        CircleShape
                    )
            )
            
            // Confederation text on the ball
            val selectedItem = items.getOrNull(selectedIndex)
            if (selectedItem != null) {
                Text(
                    text = selectedItem.confederation,
                    style = AFMTextStyles.textXS.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = FameColors.TrophyGold.copy(alpha = 0.4f)
                )
            }
        }

        // 4. Rotating Flag Arc
        items.forEachIndexed { index, item ->
            val relativeAngle = (index * anglePerItem + rotationAnimatable.value) % 360f
            val normalizedAngle = (relativeAngle + 360f) % 360f
            
            // Focus is at 0 degrees (Top)
            val angleFromTop = if (normalizedAngle > 180) 360 - normalizedAngle else normalizedAngle
            
            if (angleFromTop < 80f) {
                val radian = Math.toRadians(normalizedAngle.toDouble() - 90.0)
                val radiusPx = with(density) { radius.toPx() }
                
                val x = (radiusPx * cos(radian)).toFloat()
                val y = (radiusPx * sin(radian)).toFloat()
                
                val isSelected = item.country == selectedNationality
                val proximity = (1f - (angleFromTop / 80f)).coerceAtLeast(0f)
                
                // Enhanced Visuals
                val scale = if (isSelected) 1.5f else 0.7f + (0.3f * proximity)
                val alpha = if (isSelected) 1f else 0.1f + (0.4f * proximity)
                val blurAmount = if (isSelected) 0.dp else (3.dp * (1f - proximity))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .blur(blurAmount),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Flag with Glow/Highlight
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(FameColors.TrophyGold.copy(alpha = 0.4f), Color.Transparent)
                                            ),
                                            CircleShape
                                        )
                                )
                            }
                            
                            Surface(
                                modifier = Modifier.size(if (isSelected) 52.dp else 44.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(2.dp, FameColors.TrophyGold) else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shadowElevation = if (isSelected) 12.dp else 0.dp
                            ) {
                                AsyncImage(
                                    model = NationalityUtils.getWavingFlagUrl(item.country),
                                    contentDescription = item.country,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.default_flag)
                                )
                            }
                        }
                        
                        // Country Name positioned between flag and football
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = item.country.uppercase(),
                                style = AFMTextStyles.textSM.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 13.sp
                                ),
                                color = FameColors.TrophyGold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Surface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.ui.graphics.RectangleShape,
    color: Color = Color.Transparent,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shadowElevation = shadowElevation.toPx()
                this.shape = shape
                this.clip = true
            }
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(color, shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
