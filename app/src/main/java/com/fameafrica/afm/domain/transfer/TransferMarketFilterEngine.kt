package com.fameafrica.afm.domain.transfer

import com.fameafrica.afm.data.database.entities.ScoutAssignmentsEntity
import com.fameafrica.afm.data.database.model.PlayerSummary
import com.fameafrica.afm.data.model.PlayerFilter
import com.fameafrica.afm.data.model.PlayerSortOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferMarketFilterEngine @Inject constructor() {

    fun filterPlayers(
        players: List<PlayerSummary>,
        filter: PlayerFilter,
        scoutReports: Map<Int, ScoutAssignmentsEntity> = emptyMap()
    ): List<PlayerSummary> {
        return players.asSequence()
            .filter { player ->
                // Identity
                (filter.name == null || player.name.contains(filter.name, ignoreCase = true)) &&
                (filter.nationalities.isEmpty() || filter.nationalities.contains(player.nationality)) &&
                (filter.regions.isEmpty() || (player.region != null && filter.regions.contains(player.region))) &&
                
                // Position
                (filter.positions.isEmpty() || filter.positions.contains(player.position)) &&
                (filter.positionCategories.isEmpty() || filter.positionCategories.contains(player.positionCategory)) &&
                
                // Age
                (filter.minAge == null || player.age >= filter.minAge) &&
                (filter.maxAge == null || player.age <= filter.maxAge) &&
                
                // Potential
                (filter.minPotential == null || player.potential >= filter.minPotential) &&
                (filter.maxPotential == null || player.potential <= filter.maxPotential) &&
                
                // Financial
                (filter.minValue == null || player.marketValue >= filter.minValue) &&
                (filter.maxValue == null || player.marketValue <= filter.maxValue) &&
                (filter.minWage == null || player.salary >= filter.minWage) &&
                (filter.maxWage == null || player.salary <= filter.maxWage) &&
                
                // Status
                (filter.isFreeAgent == null || player.freeAgent == filter.isFreeAgent) &&
                (filter.isTransferListed == null || player.isTransferListed == filter.isTransferListed) &&
                (filter.isLoanListed == null || player.isLoanListed == filter.isLoanListed) &&
                
                // Contract
                (filter.maxContractMonths == null || player.contractExpiryWeek <= (filter.maxContractMonths * 4)) &&
                
                // Attributes
                (filter.minRating == null || player.rating >= filter.minRating) &&
                (filter.minPace == null || player.pace >= filter.minPace) &&
                (filter.minPassing == null || player.passing >= filter.minPassing) &&
                (filter.minFinishing == null || player.finishing >= filter.minFinishing) &&
                (filter.minDefending == null || player.defending >= filter.minDefending) &&
                (filter.minPhysical == null || player.strength >= filter.minPhysical) &&
                
                // Scouting
                (filter.scoutRatingMin == null || (scoutReports[player.id]?.scoutRating ?: 0) >= filter.scoutRatingMin) &&
                (filter.recommendationLevels.isEmpty() || filter.recommendationLevels.contains(scoutReports[player.id]?.verdict))
            }
            .sortedWith(getComparator(filter.sortBy, scoutReports))
            .toList()
    }

    private fun getComparator(
        sortBy: PlayerSortOption,
        scoutReports: Map<Int, ScoutAssignmentsEntity>
    ): Comparator<PlayerSummary> {
        return when (sortBy) {
            PlayerSortOption.RATING_DESC -> compareByDescending { it.rating }
            PlayerSortOption.RATING_ASC -> compareBy { it.rating }
            PlayerSortOption.VALUE_DESC -> compareByDescending { it.marketValue }
            PlayerSortOption.VALUE_ASC -> compareBy { it.marketValue }
            PlayerSortOption.WAGE_DESC -> compareByDescending { it.salary }
            PlayerSortOption.WAGE_ASC -> compareBy { it.salary }
            PlayerSortOption.POTENTIAL_DESC -> compareByDescending { it.potential }
            PlayerSortOption.AGE_ASC -> compareBy { it.age }
            PlayerSortOption.SCOUT_RATING_DESC -> compareByDescending { scoutReports[it.id]?.scoutRating ?: 0 }
            PlayerSortOption.NAME_ASC -> compareBy { it.name }
        }
    }
}
