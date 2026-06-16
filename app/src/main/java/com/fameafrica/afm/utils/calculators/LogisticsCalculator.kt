package com.fameafrica.afm.utils.calculators

import kotlin.math.*

/**
 * Utility for calculating travel friction across the African continent.
 */
object LogisticsCalculator {

    enum class InfrastructureTier(val costMultiplier: Double, val fatigueMultiplier: Double) {
        ELITE(1.2, 0.7),        // North Africa (Morocco, Egypt, etc.)
        ADVANCED(1.0, 0.9),     // South Africa, Nigeria
        DEVELOPING(0.8, 1.2),   // East Africa (Tanzania, Kenya)
        CHALLENGING(0.6, 1.8)   // Central Africa, Rural regions
    }

    data class TravelImpact(val staminaDrop: Int, val financialCost: Long)

    /**
     * Calculates the distance between two points on Earth using the Haversine formula.
     * Returns distance in kilometers.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Calculates travel impact based on distance, region, and flight type.
     */
    fun calculateTravelImpact(
        distanceKm: Double,
        isCharterFlight: Boolean,
        isInternational: Boolean,
        homeRegion: String,
        awayRegion: String,
        month: Int,
        altitudeHome: Int = 0 // In meters
    ): TravelImpact {
        // 1. Base Fatigue & Cost
        var staminaDrop = ((distanceKm / 800.0) * 4).toDouble() // Slightly harsher base
        var financialCost = (distanceKm * 60).toLong() // Base cost $60 per km

        // 2. Regional Tier Impact
        val tier = getTierForRegion(awayRegion)
        staminaDrop *= tier.fatigueMultiplier
        financialCost = (financialCost * tier.costMultiplier).toLong()

        // 3. African Realism: Border Friction
        if (isInternational) {
            staminaDrop += 2.0 // Extra fatigue for immigration/delays
            financialCost += 5000 // Fixed visa/processing costs
        }

        // 4. Regional Friction (North to South, East to West)
        if (homeRegion != awayRegion) {
            staminaDrop += 3.0
            financialCost += 2000
        }

        // 5. Weather Engine (Heat & Humidity)
        val weatherFatigue = calculateWeatherImpact(awayRegion, month)
        staminaDrop += weatherFatigue

        // 6. Altitude Impact (If playing at high altitude)
        if (altitudeHome > 1500) {
            staminaDrop += 2.0 // Extra strain at altitude
        }

        // 7. Flight Type
        if (isCharterFlight) {
            staminaDrop *= 0.5
            financialCost *= 4 // Charters are expensive in Africa
        }

        return TravelImpact(staminaDrop.roundToInt(), financialCost)
    }

    private fun getTierForRegion(region: String): InfrastructureTier {
        return when (region.uppercase()) {
            "NORTH AFRICA" -> InfrastructureTier.ELITE
            "SOUTHERN AFRICA" -> InfrastructureTier.ADVANCED
            "WEST AFRICA" -> InfrastructureTier.ADVANCED
            "EAST AFRICA" -> InfrastructureTier.DEVELOPING
            "CENTRAL AFRICA" -> InfrastructureTier.CHALLENGING
            else -> InfrastructureTier.DEVELOPING
        }
    }

    private fun calculateWeatherImpact(region: String, month: Int): Double {
        return when (region.uppercase()) {
            "NORTH AFRICA" -> if (month in 6..8) 4.0 else 0.0 // Extreme summer heat
            "WEST AFRICA", "CENTRAL AFRICA" -> 2.5 // Constant humidity
            "EAST AFRICA" -> if (month in 3..5) 1.5 else 0.0 // Rainy season pitches
            else -> 0.0
        }
    }
}
