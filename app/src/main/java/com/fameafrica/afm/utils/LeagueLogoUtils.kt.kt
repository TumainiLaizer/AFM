package com.fameafrica.afm.utils

import android.content.Context
import com.fameafrica.afm.utils.constants.AfricanFootballDataHelper

object LeagueLogoUtils {

    /**
     * Maps specific league and cup names to their exact drawable resource names.
     */
    private val specialCaseLeagues = mapOf(
        "Tanzania Premier League" to "tanzania_premier_league",
        "Tunisia Ligue 1" to "tunisia_league_1",
        "Egyptian Premier League" to "egyptian_premier_league",
        "South African PSL" to "south_african_psl",
        "Algeria League 1" to "algeria_league_1",
        "Morocco Botola Pro" to "morocco_botola_pro",
        "Tanzania Championship League" to "tanzania_championship_league",
        "Tanzania First League" to "tanzania_first_league",
        "Tanzania Regional Champions League" to "tanzania_regional_champions_league",
        "Tanzania Regional Division League 2" to "tanzania_regional_division_league_2",
        "Zanzibar Premier League" to "zanzibar_premier_league",
        "Zanzibar Championship League" to "zanzibar_championship_league",
        "Congo DR Super League" to "congo_dr_super_league",
        "Kenyan Premier League" to "kenyan_premier_league",
        "Congo Premier League" to "congo_premier_league",
        "Angola Girabola" to "angola_girabola",
        
        // Cups
        "CAF Champions League" to "caf_champions_league",
        "CAF Confederation Cup" to "caf_confederation_cup",
        "CAF Super Cup" to "caf_super_cup",
        "CRDB Federation Cup" to "crdb_federation_cup",
        "Fame Africa Cup" to "fame_africa_cup",
        "Muungano Cup" to "muungano_cup"
    )

    /**
     * Gets the logo for a league or cup.
     * 
     * Priority:
     * 1. Special case mapping (Tanzania Premier League -> tanzania_premier_league.webp)
     * 2. Sanitized name matching (Egyptian Premier League -> egyptian_premier_league.webp)
     * 3. Country Flag fallback (via NationalityUtils)
     * 
     * @return Any? - Either an Int (DrawableRes) or a String (Asset URL)
     */
    fun getLeagueLogo(context: Context, competitionName: String): Any? {
        val resources = context.resources
        val packageName = context.packageName

        // 1. Check Special Cases
        val specialName = specialCaseLeagues[competitionName]
        if (specialName != null) {
            val resId = resources.getIdentifier(specialName, "drawable", packageName)
            if (resId != 0) return resId
        }

        // 2. Check Sanitized Name (lowercase_with_underscores)
        val sanitizedName = competitionName.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .filter { it.isLetterOrDigit() || it == '_' }

        val sanitizedResId = resources.getIdentifier(sanitizedName, "drawable", packageName)
        if (sanitizedResId != 0) return sanitizedResId

        // 3. Fallback to Country Flag
        // Look in Leagues first, then Cups
        val country = AfricanFootballDataHelper.LEAGUES[competitionName]?.country
            ?: AfricanFootballDataHelper.CUPS[competitionName]?.country

        if (country != null) {
            // Using your NationalityUtils logic for Asset URL
            return NationalityUtils.getFlagUrl(country)
        }

        // Final fallback: A generic trophy or logo if nothing is found
        return null
    }
}