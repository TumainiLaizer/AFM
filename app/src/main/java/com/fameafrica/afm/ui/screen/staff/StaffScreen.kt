package com.fameafrica.afm.ui.screen.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.ui.components.*
import com.fameafrica.afm.ui.theme.*
import com.fameafrica.afm.utils.StaffAssetUtils

@Composable
fun StaffScreen(
    onBack: () -> Unit,
    onHireStaff: () -> Unit,
    onStaffClick: (Int) -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf("GRID") } // GRID or HIERARCHY

    AFM2026Theme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0))) {
            // Top Header (Image 7 style)
            Surface(
                color = Color(0xFF003366),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.currentTeamName.uppercase(), color = Color(0xFF00CCFF), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("STAFF MANAGEMENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Row {
                        IconButton(onClick = { viewMode = "GRID" }) {
                            Icon(Icons.Default.GridView, "Grid", tint = if (viewMode == "GRID") FameColors.AfroSunOrange else Color.White.copy(0.6f))
                        }
                        IconButton(onClick = { viewMode = "HIERARCHY" }) {
                            Icon(Icons.Default.AccountTree, "Hierarchy", tint = if (viewMode == "HIERARCHY") FameColors.AfroSunOrange else Color.White.copy(0.6f))
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (viewMode == "GRID") {
                    StaffGridContent(uiState, onStaffClick)
                } else {
                    StaffHierarchyView(uiState, onStaffClick)
                }
            }

            // Bottom Button
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Button(
                    onClick = onHireStaff,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FameColors.AfroSunOrange),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("HIRE STAFF", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun StaffGridContent(
    uiState: StaffUiState,
    onStaffClick: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF003366))
            }
        } else {
            val staffList = uiState.dashboard?.staffList ?: emptyList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(staffList) { staff ->
                    StaffGridCard(staff, onStaffClick)
                }
            }
        }
    }
}

@Composable
fun StaffHierarchyView(
    uiState: StaffUiState,
    onStaffClick: (Int) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF003366))
        }
        return
    }

    val assistants = uiState.coaches.filter { it.role == "ASSISTANT_MANAGER" }
    val coaches = uiState.coaches.filter { it.role != "ASSISTANT_MANAGER" }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. User Manager
        item {
            HierarchyNode(
                name = uiState.managerName,
                role = "MANAGER",
                image = uiState.managerAvatar ?: "file:///android_asset/manager_faces/default_manager.jpg",
                isMain = true,
                rating = 90
            )
        }

        // Branch to Assistants
        if (assistants.isNotEmpty()) {
            item { HierarchyBranch(count = assistants.size) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    assistants.forEach { assistant ->
                        HierarchyNode(
                            name = assistant.name,
                            role = assistant.roleDisplay,
                            image = StaffAssetUtils.getStaffFace(assistant.id, assistant.role),
                            rating = assistant.impactRating,
                            onClick = { onStaffClick(assistant.id) }
                        )
                        if (assistant != assistants.last()) Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }

        // Branch to Coaches
        if (coaches.isNotEmpty()) {
            item { HierarchyBranch(count = 1) }
            item {
                Text(
                    "COACHING DEPARTMENT",
                    style = AFMTextStyles.textXS,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF003366),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    coaches.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            row.forEach { coach ->
                                HierarchyNode(
                                    name = coach.name,
                                    role = coach.roleDisplay,
                                    image = StaffAssetUtils.getStaffFace(coach.id, coach.role),
                                    rating = coach.impactRating,
                                    onClick = { onStaffClick(coach.id) }
                                )
                                if (coach != row.last()) Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Other Departments
        val departments = listOf(
            "SCOUTING" to uiState.scouts,
            "MEDICAL" to uiState.medical,
            "ADMIN" to uiState.admin
        ).filter { it.second.isNotEmpty() }

        departments.forEach { (dept, staff) ->
            item { HierarchyBranch(count = 1) }
            item {
                Text(
                    "$dept DEPARTMENT",
                    style = AFMTextStyles.textXS,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF003366),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    staff.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            row.forEach { member ->
                                HierarchyNode(
                                    name = member.name,
                                    role = member.roleDisplay,
                                    image = StaffAssetUtils.getStaffFace(member.id, member.role),
                                    rating = member.impactRating,
                                    onClick = { onStaffClick(member.id) }
                                )
                                if (member != row.last()) Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HierarchyNode(
    name: String,
    role: String,
    image: String,
    rating: Int,
    isMain: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AsyncImage(
                model = image,
                contentDescription = name,
                modifier = Modifier
                    .size(if (isMain) 70.dp else 56.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isMain) FameColors.AfroSunOrange else Color(0xFF003366),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            RatingBadge(
                rating = rating,
                modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            name.uppercase(),
            style = AFMTextStyles.textXS,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            role.uppercase(),
            style = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF003366),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HierarchyBranch(count: Int) {
    Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
        val centerX = size.width / 2
        
        // Vertical line down from parent
        drawLine(
            color = Color(0xFF003366).copy(alpha = 0.5f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, 15f),
            strokeWidth = 2.dp.toPx()
        )
        
        if (count > 1) {
            // Horizontal bar
            val width = size.width * 0.6f
            val startX = centerX - (width / 2)
            val endX = centerX + (width / 2)
            drawLine(
                color = Color(0xFF003366).copy(alpha = 0.5f),
                start = Offset(startX, 15f),
                end = Offset(endX, 15f),
                strokeWidth = 2.dp.toPx()
            )
            
            // Vertical lines to children
            val spacing = if (count > 1) width / (count - 1) else 0f
            for (i in 0 until count) {
                val x = startX + (i * spacing)
                drawLine(
                    color = Color(0xFF003366).copy(alpha = 0.5f),
                    start = Offset(x, 15f),
                    end = Offset(x, 30f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        } else {
            // Single vertical line
            drawLine(
                color = Color(0xFF003366).copy(alpha = 0.5f),
                start = Offset(centerX, 15f),
                end = Offset(centerX, 30f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun StaffGridCard(staff: StaffEntity, onClick: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick(staff.id) },
        color = Color(0xFF003399),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.width(12.dp).height(4.dp).background(Color.Red))
                    Box(modifier = Modifier.width(12.dp).height(4.dp).background(Color.Red))
                    Box(modifier = Modifier.width(12.dp).height(4.dp).background(Color.Red))
                }
                
                RatingBadge(rating = staff.impactRating)
            }

            // Avatar
            Box(contentAlignment = Alignment.BottomStart) {
                AsyncImage(
                    model = StaffAssetUtils.getStaffFace(staff.id, staff.role),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Icon(Icons.Default.SentimentSatisfied, null, tint = Color.Yellow, modifier = Modifier.size(16.dp).padding(2.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Name & Role
            Text(
                text = staff.name.uppercase(),
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = staff.roleDisplay.uppercase(),
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "2026",
                color = Color.Yellow,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun StaffScreenPreview() {
    val staff = listOf(
        StaffEntity(id = 1, name = "X. Alonso", role = "MANAGER", staffType = "COACHING", teamId = 1, teamName = "West Ham", specialization = "Tactical", impactRating = 89, salary = 500000),
        StaffEntity(id = 2, name = "J. Neves", role = "ASSISTANT_MANAGER", staffType = "COACHING", teamId = 1, teamName = "West Ham", specialization = "General", impactRating = 83, salary = 300000),
        StaffEntity(id = 3, name = "L. McCulloch", role = "ASSISTANT_MANAGER", staffType = "COACHING", teamId = 1, teamName = "West Ham", specialization = "General", impactRating = 83, salary = 300000)
    )
    AFM2026Theme {
        val uiState = StaffUiState(
            isLoading = false,
            currentTeamName = "West Ham",
            managerName = "User Manager",
            coaches = staff
        )
        StaffScreenContentPreview(uiState)
    }
}

@Composable
fun StaffScreenContentPreview(uiState: StaffUiState) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0))) {
        StaffHierarchyView(uiState, {})
    }
}
