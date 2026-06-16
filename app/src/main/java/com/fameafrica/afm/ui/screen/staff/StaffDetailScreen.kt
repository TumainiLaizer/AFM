package com.fameafrica.afm.ui.screen.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.R
import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.ui.screen.manager.AttributeListItem
import com.fameafrica.afm.ui.screen.manager.InfoMiniCard
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.NationalityUtils
import com.fameafrica.afm.utils.StaffAssetUtils

@Composable
fun StaffDetailScreen(
    staffId: Int,
    onBack: () -> Unit,
    viewModel: StaffDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(staffId) {
        viewModel.loadStaff(staffId)
    }

    AFM2026Theme {
        StaffDetailContent(
            staff = uiState.staff,
            isLoading = uiState.isLoading,
            onBack = onBack,
            onTerminate = {
                viewModel.terminateContract()
                onBack()
            },
            currencyContext = uiState.currencyContext
        )
    }
}

@Composable
fun StaffDetailContent(
    staff: StaffEntity?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onTerminate: () -> Unit,
    currencyContext: com.fameafrica.afm.utils.formatters.CurrencyFormatter.CurrencyContext?
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FameColors.ChampionsGold)
        }
        return
    }

    if (staff == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Staff not found", color = Color.White)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Surface(
            color = FameColors.HeaderDark,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = NationalityUtils.getFlagUrl(staff.nationality ?: "Tanzania"),
                    contentDescription = staff.nationality,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = staff.name.uppercase(),
                        style = AFMTextStyles.textLG.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = staff.roleDisplay.uppercase(),
                        style = AFMTextStyles.textXXS.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RatingBadge(
                    rating = staff.impactRating,
                    textStyle = AFMTextStyles.textLG.copy(fontSize = 24.sp, fontWeight = FontWeight.Black)
                )
            }
        }

        // Main Content
        Row(modifier = Modifier.weight(1f)) {
            // Left: Image
            Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                AsyncImage(
                    model = StaffAssetUtils.getStaffFace(staff.id, staff.role),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.default_manager)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, FameColors.DeepNavyBlack.copy(alpha = 0.8f))
                            )
                        )
                )
            }

            // Right: Info
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = Color(0xFF003366),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(staff.teamName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("Joined: ${staff.contractEndDate ?: "N/A"}".uppercase(), style = AFMTextStyles.textXXS.copy(fontSize = 7.sp), color = Color.White.copy(alpha = 0.7f))
                    }
                }

                val salary = if (currencyContext != null) {
                    "${currencyContext.symbol}${staff.salary / 1000}K"
                } else "£${staff.salary}"

                InfoMiniCard(label = "Weekly Wage", value = salary, icon = R.drawable.money)
                InfoMiniCard(label = "Specialization", value = staff.specializationDisplay, icon = R.drawable.agent)
                InfoMiniCard(label = "Age", value = "${staff.age ?: "30"}", icon = R.drawable.calendar)

                Spacer(modifier = Modifier.height(8.dp))

                AttributeListItem("Impact", staff.impactRating)
                AttributeListItem("Experience", staff.experienceLevel)
                AttributeListItem("Loyalty", staff.loyalty)
                AttributeListItem("Adaptability", staff.adaptability)
                AttributeListItem("Mentoring", staff.mentoringAbility)
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTerminate,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("TERMINATE", style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
        }
    }
}
