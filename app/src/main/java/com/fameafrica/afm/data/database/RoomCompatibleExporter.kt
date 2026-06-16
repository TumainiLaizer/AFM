package com.fameafrica.afm.data.database

import android.util.Log
import com.fameafrica.afm.data.database.entities.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCompatibleExporter @Inject constructor() {
    private val moshi = Moshi.Builder()
        .add(BooleanAdapter())
        .add(IntAdapter())
        .add(LongAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Exports all tables from the current database to JSON files in the specified directory.
     * Respects foreign key order (exporting parent tables first).
     */
    suspend fun exportToJson(
        database: AFMDatabase,
        outputDir: File,
        onProgress: (tableName: String, current: Int, total: Int, recordCount: Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val tablesToExport = getExportOrder(database)
            val totalTables = tablesToExport.size

            tablesToExport.forEachIndexed { index, table ->
                val fileName = "${table.tableName}.json"
                val outputFile = File(outputDir, fileName)
                
                val recordCount = table.exportAction(outputFile)
                onProgress(table.tableName, index + 1, totalTables, recordCount)
            }

            Result.success(outputDir.absolutePath)
        } catch (e: Exception) {
            Log.e("AFM_EXPORT", "Export failed", e)
            Result.failure(e)
        }
    }

    private interface TableExporter {
        val tableName: String
        suspend fun exportAction(outputFile: File): Int
    }

    private inline fun <reified T> createExporter(
        tableName: String,
        crossinline fetchAction: suspend () -> List<T>
    ): TableExporter {
        return object : TableExporter {
            override val tableName = tableName
            override suspend fun exportAction(outputFile: File): Int = withContext(Dispatchers.IO) {
                val data = fetchAction()
                val type = Types.newParameterizedType(List::class.java, T::class.java)
                val adapter = moshi.adapter<List<T>>(type)
                val json = adapter.toJson(data)
                
                outputFile.outputStream().use { fos ->
                    fos.write(json.toByteArray())
                }
                data.size
            }
        }
    }

    private fun getExportOrder(database: AFMDatabase): List<TableExporter> {
        return listOf(
            // Level 0: Static & Independent
            createExporter<NationalitiesEntity>("nationalities") { database.nationalitiesDao().getAllStatic() },
            createExporter<RegionalSettingsEntity>("regional_settings") { database.regionalSettingsDao().getAllStatic() },
            createExporter<PersonalityTypesEntity>("personality_types") { database.personalityTypesDao().getAllStatic() },
            createExporter<ArchetypeTraitsEntity>("archetype_traits") { database.archetypeTraitsDao().getAllStatic() },
            createExporter<JournalistsEntity>("journalists") { database.journalistsDao().getAllStatic() },
            createExporter<RefereesEntity>("referees") { database.refereesDao().getAllStatic() },
            createExporter<SponsorsEntity>("sponsors") { database.sponsorsDao().getAllStatic() },
            createExporter<CurrencyExchangeRatesEntity>("currency_exchange_rates") { database.currencyExchangeRatesDao().getAllStatic() },
            createExporter<GameSettingsEntity>("game_settings") { database.gameSettingsDao().getAllStatic() },
            createExporter<UserPreferencesEntity>("user_preferences") { database.userPreferencesDao().getAllStatic() },
            createExporter<IdentityMappingEntity>("identity_mappings") { database.identityMappingDao().getAllStatic() },
            createExporter<WorldStateEntity>("world_state") { database.worldStateDao().getAllStatic() },
            createExporter<GameStatesEntity>("game_states") { database.gameStatesDao().getAllStatic() },
            createExporter<CareersEntity>("careers") { database.careersDao().getAllStatic() },
            createExporter<UserAnalyticsEntity>("user_analytics") { database.userAnalyticsDao().getAllStatic() },
            createExporter<NewsEntity>("news") { database.newsDao().getAllStatic() },
            createExporter<NotificationsEntity>("notifications") { database.notificationsDao().getAllStatic() },
            createExporter<ShortlistEntity>("shortlist") { database.shortlistDao().getAllStatic() },
            createExporter<PlayerFilterPresetEntity>("player_filter_presets") { database.playerFilterPresetDao().getAllStatic() },

            // Level 1: Primary Entities
            createExporter<LeaguesEntity>("leagues") { database.leaguesDao().getAllStatic() },
            createExporter<CupsEntity>("cups") { database.cupsDao().getAllStatic() },
            createExporter<ClubDNAEntity>("club_dna") { database.clubDNADao().getAllStatic() },
            createExporter<LeagueContextEntity>("league_context") { database.leagueContextDao().getAllStatic() },
            createExporter<NationalTeamsEntity>("national_teams") { database.nationalTeamsDao().getAllStatic() },
            createExporter<PlayerAgentsEntity>("player_agents") { database.playerAgentsDao().getAllStatic() },
            createExporter<TransferWindowsEntity>("transfer_windows") { database.transferWindowsDao().getAllStatic() },

            // Level 2: Teams & Vision
            createExporter<TeamsEntity>("teams") { database.teamsDao().getAllStatic() },
            createExporter<ClubVisionEntity>("club_vision") { database.clubVisionDao().getAllStatic() },

            // Level 3: Personnel
            createExporter<ChairmanEntity>("chairman") { database.chairmanDao().getAllStatic() },
            createExporter<ManagersEntity>("managers") { database.managersDao().getAllStatic() },
            createExporter<PlayersEntity>("players") { database.playersDao().getAllStatic() },
            createExporter<StaffEntity>("staff") { database.staffDao().getAllStatic() },
            createExporter<AgentClientsEntity>("agent_clients") { database.agentClientsDao().getAllStatic() },
            createExporter<ClubLegendsEntity>("club_legends") { database.clubLegendsDao().getAllStatic() },
            createExporter<RivalryEntity>("rivalries") { database.rivalryDao().getAllStatic() },

            // Level 4: Match & Scheduling
            createExporter<FixturesEntity>("fixtures") { database.fixturesDao().getAllStatic() },
            createExporter<FixturesResultsEntity>("fixtures_results") { database.fixturesResultsDao().getAllStatic() },
            createExporter<MatchEventsEntity>("match_events") { database.matchEventsDao().getAllStatic() },
            createExporter<MatchCommentaryEntity>("match_commentary") { database.matchCommentaryDao().getAllStatic() },
            createExporter<KnockoutMatchesEntity>("knockout_matches") { database.knockoutMatchesDao().getAllStatic() },
            createExporter<CupBracketsEntity>("cup_brackets") { database.cupBracketsDao().getAllStatic() },
            createExporter<CupGroupStandingsEntity>("cup_group_standings") { database.cupGroupStandingsDao().getAllStatic() },
            createExporter<LeagueStandingsEntity>("league_standings") { database.leagueStandingsDao().getAllStatic() },

            // Level 5: Player & Contract Dynamics
            createExporter<PlayerContractsEntity>("player_contracts") { database.playerContractsDao().getAllStatic() },
            createExporter<PlayerLoansEntity>("player_loans") { database.playerLoansDao().getAllStatic() },
            createExporter<PlayerReactionsEntity>("player_reactions") { database.playerReactionsDao().getAllStatic() },
            createExporter<PlayerTrainingEntity>("player_training") { database.playerTrainingDao().getAllStatic() },
            createExporter<PlayerFormEntity>("player_form") { database.playerFormDao().getAllStatic() },
            createExporter<NationalTeamPlayersEntity>("national_team_players") { database.nationalTeamPlayersDao().getAllStatic() },

            // Level 6: Story, Finance & Misc
            createExporter<FinancesEntity>("finances") { database.financesDao().getAllStatic() },
            createExporter<TransferFundingRequestEntity>("transfer_funding_requests") { database.transferFundingRequestsDao().getAllStatic() },
            createExporter<TransfersEntity>("transfers") { database.transfersDao().getAllStatic() },
            createExporter<TrophiesEntity>("trophies") { database.trophiesDao().getAllStatic() },
            createExporter<SeasonAwardsEntity>("season_awards") { database.seasonAwardsDao().getAllStatic() },
            createExporter<SeasonHistoryEntity>("season_history") { database.seasonHistoryDao().getAllStatic() },
            createExporter<EloHistoryEntity>("elo_history") { database.eloHistoryDao().getAllStatic() },
            createExporter<BoardEvaluationEntity>("board_evaluation") { database.boardEvaluationDao().getAllStatic() },
            createExporter<BoardRequestsEntity>("board_requests") { database.boardRequestsDao().getAllStatic() },
            createExporter<InfrastructureUpgradesEntity>("infrastructure_upgrades") { database.infrastructureUpgradesDao().getAllStatic() },
            createExporter<ObjectivesEntity>("objectives") { database.objectivesDao().getAllStatic() },
            createExporter<InterviewsEntity>("interviews") { database.interviewsDao().getAllStatic() },
            createExporter<StoryEventsEntity>("story_events") { database.storyEventsDao().getAllStatic() },
            createExporter<PressConferencesEntity>("press_conferences") { database.pressConferencesDao().getAllStatic() },
            createExporter<PreseasonScheduleEntity>("preseason_schedule") { database.preseasonScheduleDao().getAllStatic() },
            createExporter<ScoutAssignmentsEntity>("scout_assignments") { database.scoutAssignmentsDao().getAllStatic() },
            createExporter<ScoutingMissionsEntity>("scouting_missions") { database.scoutingMissionsDao().getAllStatic() },
            createExporter<SettingsHistoryEntity>("settings_history") { database.settingsHistoryDao().getAllStatic() },
            createExporter<TacticsEntity>("tactics") { database.tacticsDao().getAllStatic() },
            createExporter<PrizesCupEntity>("prizes_cup") { database.prizesCupDao().getAllStatic() },
            createExporter<PrizesLeaguesEntity>("prizes_leagues") { database.prizesLeaguesDao().getAllStatic() },
            createExporter<CommunityShieldEntity>("community_shield") { database.communityShieldDao().getAllStatic() },
            createExporter<MatchFixingCasesEntity>("match_fixing_cases") { database.matchFixingCasesDao().getAllStatic() },
            createExporter<ManagerOffersEntity>("manager_offers") { database.managerOffersDao().getAllStatic() },
            createExporter<ManagerOffersForRetiredPlayersEntity>("manager_offers_for_retired_players") { database.managerOffersForRetiredPlayersDao().getAllStatic() },
            createExporter<FanExpectationsEntity>("fan_expectations") { database.fanExpectationsDao().getAllStatic() },
            createExporter<FanReactionsEntity>("fan_reactions") { database.fanReactionsDao().getAllStatic() }
        )
    }
}
