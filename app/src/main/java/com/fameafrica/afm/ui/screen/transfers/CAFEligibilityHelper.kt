package com.fameafrica.afm.ui.screen.transfers

object CAFEligibilityHelper {
    
    fun getEligibilityStatus(player: TransferPlayerUiModel): String {
        return when (player.cafStatus) {
            CAFStatus.ITC_PENDING -> "ITC PENDING"
            CAFStatus.ITC_COMPLETE -> "ITC APPROVED"
            CAFStatus.CHAMPIONS_LEAGUE_TIED -> "CL TIED (ELIGIBLE)"
            CAFStatus.CONFEDERATION_TIED -> "CC TIED (ELIGIBLE)"
            CAFStatus.ELIGIBLE -> "ELIGIBLE"
            CAFStatus.CONTRACT_EXPIRED -> "EXPIRED (CHAN OK)"
        }
    }

    fun canRegisterForContinental(player: TransferPlayerUiModel): Boolean {
        // Modern 2025 rule: Tied players can still register for new club
        return player.cafStatus != CAFStatus.ITC_PENDING
    }
    
    fun getItcWarning(player: TransferPlayerUiModel): String? {
        return if (player.cafStatus == CAFStatus.ITC_PENDING) {
            "Player lacks ITC for continental competition. Approval takes 14-30 days."
        } else null
    }
}
