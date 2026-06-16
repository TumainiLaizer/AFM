package com.fameafrica.afm.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sponsorship_deals")
data class SponsorshipDealEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "team_id")
    val teamId: Int,

    @ColumnInfo(name = "sponsor_name")
    val sponsorName: String,

    @ColumnInfo(name = "sponsor_logo")
    val sponsorLogo: String? = null,

    @ColumnInfo(name = "type")
    val type: String, // SHIRT, STADIUM, MEDIA

    @ColumnInfo(name = "duration_months")
    val durationMonths: Int,

    @ColumnInfo(name = "start_date")
    val startDate: String,

    @ColumnInfo(name = "end_date")
    val endDate: String,

    @ColumnInfo(name = "payout_per_month")
    val payoutPerMonth: Long,

    @ColumnInfo(name = "performance_bonuses")
    val performanceBonuses: String? = null, // JSON encoded bonuses

    @ColumnInfo(name = "objectives")
    val objectives: String? = null, // JSON encoded objectives

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
