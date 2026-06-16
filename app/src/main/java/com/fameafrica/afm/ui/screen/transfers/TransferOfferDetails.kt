package com.fameafrica.afm.ui.screen.transfers

import com.fameafrica.afm.data.database.entities.SquadRole
import com.fameafrica.afm.data.database.entities.TransferType

data class TransferOfferDetails(
    val type: TransferType,
    val fee: Long,
    val wage: Long,
    val years: Int,
    val installments: Int = 0,
    val sellOn: Int = 0,
    val goalBonus: Long = 0,
    val signingBonus: Long = 0,
    val role: SquadRole = SquadRole.ROTATION
)
