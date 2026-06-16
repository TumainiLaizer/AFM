package com.fameafrica.afm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
) {
    val baseRoute: String get() = route.substringBefore("?")

    // ============ LAUNCH FLOW ============
    object Splash : Screen(route = "splash", title = "", icon = Icons.Default.Home)
    object Main : Screen(route = "main", title = "Main Menu", icon = Icons.Default.Home)
    object ManagerCareer : Screen(route = "manager_career", title = "Manager Career", icon = Icons.Default.History)
    object CareerSetup : Screen(route = "new_career_setup", title = "New Career", icon = Icons.Default.PlayArrow)

    // ============ GAME CONTAINER ============
    object GameMain : Screen(
        route = "game_main?careerId={careerId}",
        title = "AFM 2026",
        icon = Icons.Default.SportsSoccer
    )

    // ============ PRIMARY TABS (Used within GameMain Pager) ============
    object Dashboard : Screen(route = "dashboard", title = "HOME", icon = Icons.Default.Home)
    object Squad : Screen(route = "squad", title = "SQUAD", icon = Icons.Default.Groups)
    object Matches : Screen(route = "matches", title = "MATCHES", icon = Icons.Default.SportsScore)
    object Transfers : Screen(route = "transfers", title = "TRANSFERS", icon = Icons.Default.SwapHoriz)
    object Manager : Screen(route = "manager", title = "MANAGER", icon = Icons.Default.Person)
    object Club : Screen(route = "club", title = "CLUB", icon = Icons.Default.Business)
    object World : Screen(route = "world", title = "WORLD", icon = Icons.Default.Public)

    // ============ NESTED SCREENS ============
    object PlayerDetail : Screen(route = "player/{playerId}", title = "Player", icon = Icons.Default.Person)
    object Tactics : Screen(route = "tactics", title = "Tactics", icon = Icons.Default.SportsSoccer)
    object Training : Screen(route = "training", title = "Training", icon = Icons.Default.FitnessCenter)
    object Scout : Screen(route = "scout", title = "Scout", icon = Icons.Default.Visibility)
    object Negotiation : Screen(route = "negotiation/{transferId}", title = "Negotiation", icon = Icons.Default.AttachMoney)
    object Finances : Screen(route = "finances?tab={tab}", title = "Finances", icon = Icons.AutoMirrored.Filled.TrendingUp)
    object Infrastructure : Screen(route = "infrastructure", title = "Infrastructure", icon = Icons.Default.Build)
    object History : Screen(route = "history", title = "History", icon = Icons.Default.History)
    object LeagueTable : Screen(route = "league/{leagueName}", title = "League", icon = Icons.Default.FormatListNumbered)
    object CupDraw : Screen(route = "cups", title = "Cups", icon = Icons.Default.EmojiEvents)
    object CupDetail : Screen(route = "cup_detail/{cupName}", title = "Cup Detail", icon = Icons.Default.EmojiEvents)
    
    // ============ DEDICATED CUP SCREENS ============
    object CAFChampionsLeague : Screen(route = "cup/champions_league", title = "CAF Champions League", icon = Icons.Default.EmojiEvents)
    object CAFFederationCup : Screen(route = "cup/federation_cup", title = "CAF Confederation Cup", icon = Icons.Default.EmojiEvents)
    object DomesticCup : Screen(route = "cup/domestic", title = "Domestic Cup", icon = Icons.Default.EmojiEvents)
    object WorldCup : Screen(route = "cup/world_cup", title = "World Cup", icon = Icons.Default.Public)
    object AFCON : Screen(route = "cup/afcon", title = "AFCON", icon = Icons.Default.Flag)
    object Match : Screen(route = "match/{matchId}", title = "Match", icon = Icons.Default.SportsScore)
    object Settings : Screen(route = "settings", title = "Settings", icon = Icons.Default.Settings)
    object Media : Screen(route = "media", title = "Media", icon = Icons.Default.Mic)
    object News : Screen(route = "news", title = "News", icon = Icons.Default.Newspaper)
    object NewsDetail : Screen(route = "news_detail/{newsId}", title = "Article", icon = Icons.Default.Newspaper)
    object Drama : Screen(route = "drama", title = "Drama", icon = Icons.Default.TheaterComedy)
    object Staff : Screen(route = "staff", title = "Staff", icon = Icons.Default.AssignmentInd)
    object StaffDetail : Screen(route = "staff_detail/{staffId}", title = "Staff Detail", icon = Icons.Default.Person)
    object JobCentre : Screen(route = "job_centre", title = "Job Centre", icon = Icons.Default.Work)
    object Board : Screen(route = "board", title = "Boardroom", icon = Icons.Default.Gavel)
    object FFP : Screen(route = "ffp", title = "FFP Dashboard", icon = Icons.Default.AccountBalance)
    object National : Screen(route = "national", title = "National Teams", icon = Icons.Default.Flag)
    object Notifications : Screen(route = "notifications", title = "Notifications", icon = Icons.Default.Notifications)
    object Fans : Screen(route = "fans/{teamId}/{teamName}", title = "Fan Hub", icon = Icons.Default.People)
    object PreseasonTour : Screen(route = "preseason_tour", title = "Preseason Tour", icon = Icons.Default.Flight)
    object LeagueKickoff : Screen(route = "league_kickoff", title = "Season Kickoff", icon = Icons.Default.Flag)
    object DeadlineDay : Screen(route = "deadline_day", title = "Deadline Day", icon = Icons.Default.Timer)
    object SeasonReview : Screen(route = "season_review", title = "Season Review", icon = Icons.Default.Assessment)
    object AwardsGala : Screen(route = "awards_gala", title = "Awards Gala", icon = Icons.Default.EmojiEvents)
    object SponsorNegotiation : Screen(route = "sponsor_negotiation/{sponsorId}", title = "Sponsor Negotiation", icon = Icons.Default.Handshake)
    object YouthAcademy : Screen(route = "youth_academy", title = "Youth Academy", icon = Icons.Default.School)
    object Shop : Screen(route = "shop", title = "Shop", icon = Icons.Default.ShoppingCart)
    object AzamPesaPayment : Screen(route = "payment_azampesa/{bundleId}", title = "AzamPesa", icon = Icons.Default.Payment)
    object CardPayment : Screen(route = "payment_card/{bundleId}", title = "Card Payment", icon = Icons.Default.CreditCard)
    object Sponsorships : Screen(route = "sponsorships", title = "Sponsorships", icon = Icons.Default.Handshake)

    fun withArgs(vararg args: String): String {
        var result = route
        args.forEach { arg ->
            val startIndex = result.indexOf("{")
            val endIndex = result.indexOf("}")
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                result = result.substring(0, startIndex) + arg + result.substring(endIndex + 1)
            }
        }
        return result
    }

    fun withQueryArgs(vararg params: Pair<String, String>): String {
        var result = baseRoute
        if (params.isNotEmpty()) {
            result += "?" + params.joinToString("&") { "${it.first}=${it.second}" }
        }
        return result
    }

    companion object {
        val mainTabs = listOf(Dashboard, Squad, Matches, Transfers, Club, World)
    }
}
