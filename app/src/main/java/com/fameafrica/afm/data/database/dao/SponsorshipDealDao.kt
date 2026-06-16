package com.fameafrica.afm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fameafrica.afm.data.database.entities.SponsorshipDealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SponsorshipDealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deal: SponsorshipDealEntity): Long

    @Query("SELECT * FROM sponsorship_deals WHERE team_id = :teamId AND is_active = 1")
    fun getActiveDealsByTeam(teamId: Int): Flow<List<SponsorshipDealEntity>>

    @Query("SELECT * FROM sponsorship_deals WHERE team_id = :teamId")
    suspend fun getAllByTeamSync(teamId: Int): List<SponsorshipDealEntity>

    @Query("UPDATE sponsorship_deals SET is_active = 0 WHERE id = :dealId")
    suspend fun deactivateDeal(dealId: Int)
}
