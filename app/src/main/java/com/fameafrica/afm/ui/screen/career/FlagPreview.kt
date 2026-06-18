package com.fameafrica.afm.ui.screen.career

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fameafrica.afm.R
import com.fameafrica.afm.ui.theme.AFM2026Theme
import com.fameafrica.afm.ui.theme.FameColors

@Composable
fun FlagGrid(countries: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().background(FameColors.StadiumBlack)
    ) {
        items(countries) { country ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/flags/$country.webp")
                        .build(),
                    contentDescription = country,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_flag)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = country,
                    color = FameColors.WarmIvory,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun RequestedFlagsPreview() {
    val requested = listOf(
        "Egypt", "Tunisia", "Morocco", "South Africa", "Tanzania", "Algeria", 
        "Angola", "Congo DRC", "Nigeria", "Madagascar", "Central African Republic", 
        "Kenya", "Rwanda", "Uganda", "Zambia", "Sudan", "South Sudan", 
        "Ivory Coast", "Cameroon", "Ghana", "Niger", "Mozambique", "Zimbabwe"
    )
    AFM2026Theme {
        Surface {
            FlagGrid(requested)
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
fun AllFlagsPreview() {
    val allFlags = listOf(
        "Albania", "Algeria", "Andorra", "Angola", "Argentina", "Australia", "Austria",
        "Belarus", "Belgium", "Benin", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil",
        "Bulgaria", "Burkina Faso", "Burundi", "Cameroon", "Canada", "Cape Verde", "Central African Republic",
        "Chad", "Chile", "China", "Colombia", "Comoros", "Congo DRC", "Congo Republic", "Costa Rica",
        "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominican Republic",
        "Ecuador", "Egypt", "El Salvador", "England", "Equatorial Guinea", "Eritrea", "Estonia",
        "Eswatini", "Ethiopia", "Finland", "France", "Gabon", "Gambia", "Germany", "Ghana",
        "Greece", "Guatemala", "Guinea", "Guinea-Bissau", "Haiti", "Honduras", "Hungary", "Iceland",
        "India", "Iran", "Iraq", "Israel", "Italy", "Ivory Coast", "Jamaica", "Japan", "Kenya",
        "Latvia", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
        "Madagascar", "Malawi", "Mali", "Malta", "Mauritania", "Mauritius", "Mexico", "Montenegro",
        "Morocco", "Mozambique", "Namibia", "Netherlands", "New Zealand", "Niger", "Nigeria",
        "North Macedonia", "Northern Ireland", "Norway", "Panama", "Paraguay", "Peru", "Philippines",
        "Poland", "Portugal", "Republic of Ireland", "Romania", "Russia", "Rwanda", "San Marino",
        "Sao Tome and Principe", "Saudi Arabia", "Scotland", "Senegal", "Serbia", "Seychelles",
        "Sierra Leone", "Slovakia", "Slovenia", "Somalia", "South Africa", "South Korea", "South Sudan",
        "Spain", "Sudan", "Sweden", "Switzerland", "Tanzania", "Togo", "Trinidad and Tobago", "Tunisia",
        "Türkiye", "Uganda", "Ukraine", "United States", "Uruguay", "Venezuela", "Wales", "Zambia", "Zimbabwe"
    ).sorted()
    
    AFM2026Theme {
        Surface {
            FlagGrid(allFlags)
        }
    }
}
