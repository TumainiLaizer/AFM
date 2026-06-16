package com.fameafrica.afm.data.database.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerFilter(
    val name: String? = null,
    val nationalities: List<String> = emptyList(),
    val regions: List<String> = emptyList(),
    val isHomegrown: Boolean? = null,
    
    val positions: List<String> = emptyList(),
    val positionCategories: List<String> = emptyList(),
    val roles: List<String> = emptyList(),
    
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val ageGroups: List<String> = emptyList(),
    
    val minPotential: Int? = null,
    val maxPotential: Int? = null,
    
    val minValue: Long? = null,
    val maxValue: Long? = null,
    val minWage: Long? = null,
    val maxWage: Long? = null,
    
    val isFreeAgent: Boolean? = null,
    val isTransferListed: Boolean? = null,
    val isLoanListed: Boolean? = null,
    val hasReleaseClause: Boolean? = null,
    
    val minRating: Int? = null,
    val maxRating: Int? = null,
    
    val maxContractMonths: Int? = null,
    
    val minPace: Int? = null,
    val minPassing: Int? = null,
    val minFinishing: Int? = null,
    val minDefending: Int? = null,
    val minPhysical: Int? = null,
    
    val scoutRatingMin: Int? = null,
    val recommendationLevels: List<String> = emptyList(),
    val isWonderkid: Boolean? = null,
    
    val clubId: Int? = null,
    val leagueId: Int? = null,
    
    val sortBy: PlayerSortOption = PlayerSortOption.RATING_DESC
)

@Serializable
enum class PlayerSortOption {
    RATING_DESC, RATING_ASC,
    VALUE_DESC, VALUE_ASC,
    WAGE_DESC, WAGE_ASC,
    POTENTIAL_DESC, AGE_ASC,
    SCOUT_RATING_DESC,
    NAME_ASC
}