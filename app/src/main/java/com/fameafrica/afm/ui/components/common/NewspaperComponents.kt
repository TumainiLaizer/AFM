package com.fameafrica.afm.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFMTextStyles

@Composable
fun HeroNewspaperStory(
    headline: String,
    subHeadline: String,
    content: String,
    imageRes: Int?,
    category: String,
    date: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Masthead - Magazine style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "THE ",
                        style = AFMTextStyles.tickerText.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "CONTINENTAL ",
                        style = AFMTextStyles.sectionHeader.copy(
                            color = Color(0xFFCC0000), // News Red
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "GAZETTE",
                        style = AFMTextStyles.sectionHeader.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }
                
                HorizontalDivider(
                    thickness = 3.dp, 
                    color = Color.Black, 
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.uppercase(), 
                        style = AFMTextStyles.tickerText.copy(
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFCC0000)
                        )
                    )
                    Text(
                        text = "EST. 2026", 
                        style = AFMTextStyles.tickerText.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    )
                    Text(
                        text = date.uppercase(), 
                        style = AFMTextStyles.tickerText.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                HorizontalDivider(
                    thickness = 1.dp, 
                    color = Color.Black, 
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Main Headline - VERY LARGE and BOLD
            Text(
                text = headline.uppercase(),
                style = AFMTextStyles.sectionHeader.copy(
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-headline / Teaser
            Text(
                text = subHeadline,
                style = AFMTextStyles.denseText.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                    color = Color.DarkGray
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Featured Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .background(Color.LightGray)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = imageRes ?: R.drawable.player_superstar),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Caption
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "EXCLUSIVE: " + headline.take(50) + "...",
                        style = AFMTextStyles.tickerText.copy(color = Color.White, fontSize = 9.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-column body text
            val halfLength = content.length / 2
            val column1 = content.take(halfLength).substringBeforeLast(" ")
            val column2 = content.substring(column1.length).trim()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = column1,
                        style = AFMTextStyles.denseText.copy(fontSize = 10.sp, textAlign = TextAlign.Justify, color = Color.Black, lineHeight = 13.sp)
                    )
                }
                VerticalDivider(modifier = Modifier.height(80.dp), thickness = 0.5.dp, color = Color.LightGray)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = column2,
                        style = AFMTextStyles.denseText.copy(fontSize = 10.sp, textAlign = TextAlign.Justify, color = Color.Black, lineHeight = 13.sp)
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 1.dp, color = Color.Black)
        }
    }
}

@Composable
fun NewspaperArticleCard(
    headline: String,
    content: String,
    category: String,
    date: String,
    modifier: Modifier = Modifier,
    isBreaking: Boolean = false,
    imageRes: Int? = null,
    imageUrl: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreaking) Color(0xFFFFFBE6) else Color(0xFFF9F9F9)
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.1f))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.uppercase(),
                    style = AFMTextStyles.tickerText.copy(
                        color = if (isBreaking) Color(0xFFB71C1C) else Color.DarkGray,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = date,
                    style = AFMTextStyles.tickerText.copy(color = Color.Gray, fontSize = 9.sp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headline.uppercase(),
                        style = AFMTextStyles.sectionHeader.copy(
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (imageUrl != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).border(0.5.dp, Color.Black.copy(0.2f)),
                        contentScale = ContentScale.Crop
                    )
                } else if (imageRes != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).border(0.5.dp, Color.Black.copy(0.2f)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Text(
                text = content,
                style = AFMTextStyles.denseText.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Justify,
                    color = Color.DarkGray
                ),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            
            if (isBreaking) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFB71C1C))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BREAKING NEWS",
                        style = AFMTextStyles.tickerText.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun NewspaperColumnLayout(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp)
    ) {
        content()
    }
}
