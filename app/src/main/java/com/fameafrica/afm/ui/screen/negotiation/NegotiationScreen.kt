package com.fameafrica.afm.ui.screen.negotiation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.ui.components.common.SidebarCard
import com.fameafrica.afm.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationScreen(
    transferId: Int,
    onBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NegotiationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transferId) {
        if (transferId != 0) {
            viewModel.onEvent(NegotiationEvent.SelectTransfer(transferId))
        }
    }

    NegotiationBackground {
        NegotiationContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onClearSnackbar = viewModel::clearSnackbar,
            onDismissDialog = viewModel::dismissDialog,
            onBack = onBack,
            onNavigateToPlayer = onNavigateToPlayer,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationContent(
    uiState: NegotiationUiState,
    onEvent: (NegotiationEvent) -> Unit,
    onClearSnackbar: () -> Unit,
    onDismissDialog: () -> Unit,
    onBack: () -> Unit,
    onNavigateToPlayer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf("CONTRACTS", "TRANSFERS", "LOANS", "DASHBOARD")

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "NEGOTIATION ROOM",
                        style = AFMTextStyles.textLG,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = FameColors.HeaderDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .background(FameColors.DeepNavyBlack)
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = FameColors.TrophyGold,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = FameColors.TrophyGold,
                        height = 1.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                title,
                                style = AFMTextStyles.textXS.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> ContractTab(uiState.contracts, uiState.expiringContracts) { onEvent(NegotiationEvent.SelectContract(it)) }
                    1 -> TransferTab(uiState.pendingTransfers, uiState.activeTransfers) { onEvent(NegotiationEvent.SelectTransfer(it)) }
                    2 -> LoanTab(uiState.activeLoans, uiState.pendingLoans) { onEvent(NegotiationEvent.SelectLoan(it)) }
                    3 -> NegotiationDashboardTab(uiState)
                }
            }
        }
    }

    if (uiState.showDetailDialog) {
        NegotiationDetailDialog(
            uiState = uiState,
            onDismiss = onDismissDialog,
            onAcceptTransfer = { id -> onEvent(NegotiationEvent.AcceptTransfer(id)) },
            onRejectTransfer = { id -> onEvent(NegotiationEvent.RejectTransfer(id)) },
            onNavigateToPlayer = onNavigateToPlayer
        )
    }
}

@Composable
fun ContractTab(contracts: List<PlayerContractsEntity>, expiring: List<PlayerContractsEntity>, onClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (expiring.isNotEmpty()) {
            item { SectionHeader("CRITICAL EXPIRIES", Icons.Default.Warning) }
            items(expiring, key = { it.id }) { ContractCard(it, true) { onClick(it.id) } }
        }
        item { SectionHeader("ACTIVE ROSTER", Icons.Default.Group) }
        items(contracts, key = { it.id }) { ContractCard(it, false) { onClick(it.id) } }
    }
}

@Composable
fun ContractCard(contract: PlayerContractsEntity, isWarning: Boolean, onClick: () -> Unit) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        borderColor = if (isWarning) FameColors.AlertRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(1.dp)).background(if (isWarning) FameColors.AlertRed.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Description, null, tint = if (isWarning) FameColors.AlertRed else FameColors.TrophyGold, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contract.playerName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text("EXPIRES: ${contract.contractEndDate.uppercase()}", style = AFMTextStyles.textXS, color = if (isWarning) FameColors.AlertRed else Color.White.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(contract.salary.toLong()), style = AFMTextStyles.textSM, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
                Text("ANNUAL WAGE", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun TransferTab(pending: List<TransfersEntity>, active: List<TransfersEntity>, onClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pending.isNotEmpty()) {
            item { SectionHeader("PENDING OFFERS", Icons.Default.MoveToInbox) }
            items(pending, key = { it.id }) { TransferCard(it) { onClick(it.id) } }
        }
        if (active.isNotEmpty()) {
            item { SectionHeader("ACTIVE NEGOTIATIONS", Icons.Default.SyncAlt) }
            items(active, key = { it.id }) { TransferCard(it) { onClick(it.id) } }
        }
        if (pending.isEmpty() && active.isEmpty()) item { EmptyState("NO TRANSFER ACTIVITY") }
    }
}

@Composable
fun TransferCard(transfer: TransfersEntity, onClick: () -> Unit) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SwapHoriz, null, tint = FameColors.TrophyGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transfer.playerName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text("${transfer.currentTeam.uppercase()} \u2192 ${transfer.targetTeam.uppercase()}", style = AFMTextStyles.textXS, color = FameColors.TrophyGold)
            }
            Text(formatCurrency(transfer.transferFee), style = AFMTextStyles.textSM, color = FameColors.GrowthGreen, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun LoanTab(active: List<PlayerLoansEntity>, pending: List<PlayerLoansEntity>, onClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (pending.isNotEmpty()) {
            item { SectionHeader("LOAN PROPOSALS", Icons.Default.Pending) }
            items(pending, key = { it.id }) { LoanCard(it) { onClick(it.id) } }
        }
        if (active.isNotEmpty()) {
            item { SectionHeader("ACTIVE LOAN SPELLS", Icons.Default.AssignmentInd) }
            items(active, key = { it.id }) { LoanCard(it) { onClick(it.id) } }
        }
    }
}

@Composable
fun LoanCard(loan: PlayerLoansEntity, onClick: () -> Unit) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, tint = FameColors.TrophyGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(loan.playerName.uppercase(), style = AFMTextStyles.textSM, color = Color.White, fontWeight = FontWeight.Black)
                Text("${loan.loaningTeam.uppercase()} \u2192 ${loan.receivingTeam.uppercase()}", style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
            }
            Text("${loan.duration} MOS", style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun NegotiationDashboardTab(uiState: NegotiationUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(
            start = 16.dp, 
            end = 16.dp, 
            top = 16.dp, 
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ), 
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader("NOTIFICATIONS", Icons.Default.NotificationsActive)
        }

        // Critical Expiry Alert
        if (uiState.expiringContracts.isNotEmpty()) {
            item {
                WarningCard(
                    title = "${uiState.expiringContracts.size} CONTRACTS EXPIRING",
                    subtitle = "Action required to prevent players leaving on free transfers.",
                    color = FameColors.AlertRed
                )
            }
        }

        // Pending Transfer Alert
        if (uiState.pendingTransfers.isNotEmpty()) {
            item {
                WarningCard(
                    title = "${uiState.pendingTransfers.size} INCOMING OFFERS",
                    subtitle = "Review and respond to transfer bids for your players.",
                    color = FameColors.TrophyGold
                )
            }
        }

        item {
            Text("FINANCIAL OUTLOOK", style = AFMTextStyles.textXS, color = FameColors.TrophyGold, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        item {
            SidebarCard(
                modifier = Modifier.fillMaxWidth(),
                borderWidth = 0.5.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("MONTHLY WAGE BILL", formatCurrency(uiState.contracts.sumOf { it.salary.toLong() } / 12))
                    DetailRow("TRANSFER BUDGET", formatCurrency(50_000_000L))
                    DetailRow("LOAN INCOME", formatCurrency(uiState.activeLoans.size * 5000L))
                }
            }
        }
    }
}

@Composable
fun WarningCard(title: String, subtitle: String, color: Color) {
    SidebarCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = color.copy(alpha = 0.5f),
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PriorityHigh, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title.uppercase(), style = AFMTextStyles.textXS, color = color, fontWeight = FontWeight.Black)
                Text(subtitle.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FameColors.TrophyGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title.uppercase(), style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Black)
    }
}

@Composable
fun NegotiationDetailDialog(
    uiState: NegotiationUiState,
    onDismiss: () -> Unit,
    onAcceptTransfer: (Int) -> Unit,
    onRejectTransfer: (Int) -> Unit,
    onNavigateToPlayer: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FameColors.HeaderDark,
        shape = RoundedCornerShape(2.dp),
        title = { Text("NEGOTIATION DETAILS", style = AFMTextStyles.textSM, color = FameColors.TrophyGold, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.selectedContract?.let { contract ->
                    DetailRow("PLAYER", contract.playerName.uppercase())
                    DetailRow("TEAM", contract.teamName.uppercase())
                    DetailRow("SALARY", formatCurrency(contract.salary.toLong()))
                    DetailRow("ENDS", contract.contractEndDate.uppercase())
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToPlayer(contract.playerId) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold), 
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("VIEW PROFILE", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
                    }
                }
                
                uiState.selectedTransfer?.let { transfer ->
                    DetailRow("PLAYER", transfer.playerName.uppercase())
                    DetailRow("FEE", formatCurrency(transfer.transferFee))
                    DetailRow("FROM", transfer.currentTeam.uppercase())
                    DetailRow("TO", transfer.targetTeam.uppercase())
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onAcceptTransfer(transfer.id) }, 
                            modifier = Modifier.weight(1f), 
                            colors = ButtonDefaults.buttonColors(containerColor = FameColors.GrowthGreen), 
                            shape = RoundedCornerShape(2.dp)
                        ) { 
                            Text("ACCEPT", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black) 
                        }
                        Button(
                            onClick = { onRejectTransfer(transfer.id) }, 
                            modifier = Modifier.weight(1f), 
                            colors = ButtonDefaults.buttonColors(containerColor = FameColors.AlertRed), 
                            shape = RoundedCornerShape(2.dp)
                        ) { 
                            Text("REJECT", style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black) 
                        }
                    }
                }

                uiState.selectedLoan?.let { loan ->
                    DetailRow("PLAYER", loan.playerName.uppercase())
                    DetailRow("OWNER", loan.loaningTeam.uppercase())
                    DetailRow("BORROWER", loan.receivingTeam.uppercase())
                    DetailRow("DURATION", "${loan.duration} MOS")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onNavigateToPlayer(loan.playerId) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        colors = ButtonDefaults.buttonColors(containerColor = FameColors.TrophyGold), 
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("VIEW PROFILE", style = AFMTextStyles.textXS, color = FameColors.DeepNavyBlack, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        confirmButton = { 
            TextButton(onClick = onDismiss) { 
                Text("CLOSE", color = FameColors.TrophyGold, style = AFMTextStyles.textXS, fontWeight = FontWeight.Black) 
            } 
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label.uppercase(), style = AFMTextStyles.textXS, color = Color.White.copy(alpha = 0.6f))
        Text(value.uppercase(), style = AFMTextStyles.textXS, color = Color.White, fontWeight = FontWeight.Black)
    }
}

fun formatCurrency(amount: Long): String {
    val absAmount = Math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    return when {
        absAmount >= 1_000_000 -> "$sign€${absAmount / 1_000_000}M"
        absAmount >= 1_000 -> "$sign€${absAmount / 1_000}K"
        else -> "$sign€$absAmount"
    }
}

@Composable
fun NegotiationBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FameColors.DeepNavyBlack)
    ) {
        content()
    }
}
