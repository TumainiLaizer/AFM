package com.fameafrica.afm.di

import com.fameafrica.afm.data.database.AFMDatabase
import com.fameafrica.afm.data.database.CareerDatabaseProvider
import com.fameafrica.afm.data.database.StaticDatabaseProvider
import com.fameafrica.afm.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStaticDatabase(provider: StaticDatabaseProvider): AFMDatabase {
        return provider.getStaticDatabase()
    }

    // Provide DAOs with lazy initialization to avoid premature database access
    @Provides
    fun providePlayersDao(provider: CareerDatabaseProvider): PlayersDao {
        return provider.getActiveDatabase().playersDao()
    }

    @Provides
    fun provideTeamsDao(provider: CareerDatabaseProvider): TeamsDao {
        return provider.getActiveDatabase().teamsDao()
    }

    @Provides
    fun provideManagersDao(provider: CareerDatabaseProvider): ManagersDao {
        return provider.getActiveDatabase().managersDao()
    }

    @Provides
    fun provideFixturesDao(provider: CareerDatabaseProvider): FixturesDao {
        return provider.getActiveDatabase().fixturesDao()
    }

    @Provides
    fun provideLeaguesDao(provider: CareerDatabaseProvider): LeaguesDao {
        return provider.getActiveDatabase().leaguesDao()
    }

    @Provides
    fun provideCupsDao(provider: CareerDatabaseProvider): CupsDao {
        return provider.getActiveDatabase().cupsDao()
    }

    @Provides
    fun provideCareersDao(provider: CareerDatabaseProvider): CareersDao {
        return provider.getActiveDatabase().careersDao()
    }

    @Provides
    fun provideArchetypeTraitsDao(provider: CareerDatabaseProvider): ArchetypeTraitsDao {
        return provider.getActiveDatabase().archetypeTraitsDao()
    }

    @Provides
    fun provideBoardEvaluationDao(provider: CareerDatabaseProvider): BoardEvaluationDao {
        return provider.getActiveDatabase().boardEvaluationDao()
    }

    @Provides
    fun provideBoardRequestsDao(provider: CareerDatabaseProvider): BoardRequestsDao {
        return provider.getActiveDatabase().boardRequestsDao()
    }

    @Provides
    fun provideClubLegendsDao(provider: CareerDatabaseProvider): ClubLegendsDao {
        return provider.getActiveDatabase().clubLegendsDao()
    }

    @Provides
    fun provideCommunityShieldDao(provider: CareerDatabaseProvider): CommunityShieldDao {
        return provider.getActiveDatabase().communityShieldDao()
    }

    @Provides
    fun provideCupBracketsDao(provider: CareerDatabaseProvider): CupBracketsDao {
        return provider.getActiveDatabase().cupBracketsDao()
    }

    @Provides
    fun provideCupGroupStandingsDao(provider: CareerDatabaseProvider): CupGroupStandingsDao {
        return provider.getActiveDatabase().cupGroupStandingsDao()
    }

    @Provides
    fun provideCurrencyExchangeRatesDao(provider: CareerDatabaseProvider): CurrencyExchangeRatesDao {
        return provider.getActiveDatabase().currencyExchangeRatesDao()
    }

    @Provides
    fun provideEloHistoryDao(provider: CareerDatabaseProvider): EloHistoryDao {
        return provider.getActiveDatabase().eloHistoryDao()
    }

    @Provides
    fun provideFanExpectationsDao(provider: CareerDatabaseProvider): FanExpectationsDao {
        return provider.getActiveDatabase().fanExpectationsDao()
    }

    @Provides
    fun provideFanReactionsDao(provider: CareerDatabaseProvider): FanReactionsDao {
        return provider.getActiveDatabase().fanReactionsDao()
    }

    @Provides
    fun provideFinancesDao(provider: CareerDatabaseProvider): FinancesDao {
        return provider.getActiveDatabase().financesDao()
    }

    @Provides
    fun provideFixturesResultsDao(provider: CareerDatabaseProvider): FixturesResultsDao {
        return provider.getActiveDatabase().fixturesResultsDao()
    }

    @Provides
    fun provideGameSettingsDao(provider: CareerDatabaseProvider): GameSettingsDao {
        return provider.getActiveDatabase().gameSettingsDao()
    }

    @Provides
    fun provideGameStatesDao(provider: CareerDatabaseProvider): GameStatesDao {
        return provider.getActiveDatabase().gameStatesDao()
    }

    @Provides
    fun provideInfrastructureUpgradesDao(provider: CareerDatabaseProvider): InfrastructureUpgradesDao {
        return provider.getActiveDatabase().infrastructureUpgradesDao()
    }

    @Provides
    fun provideInterviewsDao(provider: CareerDatabaseProvider): InterviewsDao {
        return provider.getActiveDatabase().interviewsDao()
    }

    @Provides
    fun provideJournalistsDao(provider: CareerDatabaseProvider): JournalistsDao {
        return provider.getActiveDatabase().journalistsDao()
    }

    @Provides
    fun provideKnockoutMatchesDao(provider: CareerDatabaseProvider): KnockoutMatchesDao {
        return provider.getActiveDatabase().knockoutMatchesDao()
    }

    @Provides
    fun provideLeagueStandingsDao(provider: CareerDatabaseProvider): LeagueStandingsDao {
        return provider.getActiveDatabase().leagueStandingsDao()
    }

    @Provides
    fun provideManagerOffersDao(provider: CareerDatabaseProvider): ManagerOffersDao {
        return provider.getActiveDatabase().managerOffersDao()
    }

    @Provides
    fun provideManagerOffersForRetiredPlayersDao(provider: CareerDatabaseProvider): ManagerOffersForRetiredPlayersDao {
        return provider.getActiveDatabase().managerOffersForRetiredPlayersDao()
    }

    @Provides
    fun provideMatchCommentaryDao(provider: CareerDatabaseProvider): MatchCommentaryDao {
        return provider.getActiveDatabase().matchCommentaryDao()
    }

    @Provides
    fun provideMatchEventsDao(provider: CareerDatabaseProvider): MatchEventsDao {
        return provider.getActiveDatabase().matchEventsDao()
    }

    @Provides
    fun provideMatchFixingCasesDao(provider: CareerDatabaseProvider): MatchFixingCasesDao {
        return provider.getActiveDatabase().matchFixingCasesDao()
    }

    @Provides
    fun provideNationalTeamPlayersDao(provider: CareerDatabaseProvider): NationalTeamPlayersDao {
        return provider.getActiveDatabase().nationalTeamPlayersDao()
    }

    @Provides
    fun provideNationalTeamsDao(provider: CareerDatabaseProvider): NationalTeamsDao {
        return provider.getActiveDatabase().nationalTeamsDao()
    }

    @Provides
    fun provideNationalitiesDao(provider: CareerDatabaseProvider): NationalitiesDao {
        return provider.getActiveDatabase().nationalitiesDao()
    }

    @Provides
    fun provideNewsDao(provider: CareerDatabaseProvider): NewsDao {
        return provider.getActiveDatabase().newsDao()
    }

    @Provides
    fun provideNotificationsDao(provider: CareerDatabaseProvider): NotificationsDao {
        return provider.getActiveDatabase().notificationsDao()
    }

    @Provides
    fun provideObjectivesDao(provider: CareerDatabaseProvider): ObjectivesDao {
        return provider.getActiveDatabase().objectivesDao()
    }

    @Provides
    fun providePersonalityTypesDao(provider: CareerDatabaseProvider): PersonalityTypesDao {
        return provider.getActiveDatabase().personalityTypesDao()
    }

    @Provides
    fun providePlayerAgentsDao(provider: CareerDatabaseProvider): PlayerAgentsDao {
        return provider.getActiveDatabase().playerAgentsDao()
    }

    @Provides
    fun provideAgentClientsDao(provider: CareerDatabaseProvider): AgentClientsDao {
        return provider.getActiveDatabase().agentClientsDao()
    }

    @Provides
    fun providePlayerContractsDao(provider: CareerDatabaseProvider): PlayerContractsDao {
        return provider.getActiveDatabase().playerContractsDao()
    }

    @Provides
    fun providePlayerLoansDao(provider: CareerDatabaseProvider): PlayerLoansDao {
        return provider.getActiveDatabase().playerLoansDao()
    }

    @Provides
    fun providePlayerReactionsDao(provider: CareerDatabaseProvider): PlayerReactionsDao {
        return provider.getActiveDatabase().playerReactionsDao()
    }

    @Provides
    fun providePlayerTrainingDao(provider: CareerDatabaseProvider): PlayerTrainingDao {
        return provider.getActiveDatabase().playerTrainingDao()
    }

    @Provides
    fun providePreseasonScheduleDao(provider: CareerDatabaseProvider): PreseasonScheduleDao {
        return provider.getActiveDatabase().preseasonScheduleDao()
    }

    @Provides
    fun providePressConferencesDao(provider: CareerDatabaseProvider): PressConferencesDao {
        return provider.getActiveDatabase().pressConferencesDao()
    }

    @Provides
    fun providePrizesCupDao(provider: CareerDatabaseProvider): PrizesCupDao {
        return provider.getActiveDatabase().prizesCupDao()
    }

    @Provides
    fun providePrizesLeaguesDao(provider: CareerDatabaseProvider): PrizesLeaguesDao {
        return provider.getActiveDatabase().prizesLeaguesDao()
    }

    @Provides
    fun provideRefereesDao(provider: CareerDatabaseProvider): RefereesDao {
        return provider.getActiveDatabase().refereesDao()
    }

    @Provides
    fun provideRegionalSettingsDao(provider: CareerDatabaseProvider): RegionalSettingsDao {
        return provider.getActiveDatabase().regionalSettingsDao()
    }

    @Provides
    fun provideScoutAssignmentsDao(provider: CareerDatabaseProvider): ScoutAssignmentsDao {
        return provider.getActiveDatabase().scoutAssignmentsDao()
    }

    @Provides
    fun provideScoutingMissionsDao(provider: CareerDatabaseProvider): ScoutingMissionsDao {
        return provider.getActiveDatabase().scoutingMissionsDao()
    }

    @Provides
    fun provideSeasonAwardsDao(provider: CareerDatabaseProvider): SeasonAwardsDao {
        return provider.getActiveDatabase().seasonAwardsDao()
    }

    @Provides
    fun provideSeasonHistoryDao(provider: CareerDatabaseProvider): SeasonHistoryDao {
        return provider.getActiveDatabase().seasonHistoryDao()
    }

    @Provides
    fun provideSettingsHistoryDao(provider: CareerDatabaseProvider): SettingsHistoryDao {
        return provider.getActiveDatabase().settingsHistoryDao()
    }

    @Provides
    fun provideSponsorsDao(provider: CareerDatabaseProvider): SponsorsDao {
        return provider.getActiveDatabase().sponsorsDao()
    }

    @Provides
    fun provideStaffDao(provider: CareerDatabaseProvider): StaffDao {
        return provider.getActiveDatabase().staffDao()
    }

    @Provides
    fun provideStoryEventsDao(provider: CareerDatabaseProvider): StoryEventsDao {
        return provider.getActiveDatabase().storyEventsDao()
    }

    @Provides
    fun provideTacticsDao(provider: CareerDatabaseProvider): TacticsDao {
        return provider.getActiveDatabase().tacticsDao()
    }

    @Provides
    fun provideTransferWindowsDao(provider: CareerDatabaseProvider): TransferWindowsDao {
        return provider.getActiveDatabase().transferWindowsDao()
    }

    @Provides
    fun provideTransfersDao(provider: CareerDatabaseProvider): TransfersDao {
        return provider.getActiveDatabase().transfersDao()
    }

    @Provides
    fun provideTrophiesDao(provider: CareerDatabaseProvider): TrophiesDao {
        return provider.getActiveDatabase().trophiesDao()
    }

    @Provides
    fun provideUserAnalyticsDao(provider: CareerDatabaseProvider): UserAnalyticsDao {
        return provider.getActiveDatabase().userAnalyticsDao()
    }

    @Provides
    fun provideUserPreferencesDao(provider: CareerDatabaseProvider): UserPreferencesDao {
        return provider.getActiveDatabase().userPreferencesDao()
    }

    @Provides
    fun provideTransferFundingRequestsDao(provider: CareerDatabaseProvider): TransferFundingRequestsDao {
        return provider.getActiveDatabase().transferFundingRequestsDao()
    }

    @Provides
    fun provideShortlistDao(provider: CareerDatabaseProvider): ShortlistDao {
        return provider.getActiveDatabase().shortlistDao()
    }

    @Provides
    fun providePlayerFilterPresetDao(provider: CareerDatabaseProvider): PlayerFilterPresetDao {
        return provider.getActiveDatabase().playerFilterPresetDao()
    }

    @Provides
    fun provideClubDNADao(provider: CareerDatabaseProvider): ClubDNADao {
        return provider.getActiveDatabase().clubDNADao()
    }

    @Provides
    fun provideClubVisionDao(provider: CareerDatabaseProvider): ClubVisionDao {
        return provider.getActiveDatabase().clubVisionDao()
    }

    @Provides
    fun provideWorldStateDao(provider: CareerDatabaseProvider): WorldStateDao {
        return provider.getActiveDatabase().worldStateDao()
    }

    @Provides
    fun provideLeagueContextDao(provider: CareerDatabaseProvider): LeagueContextDao {
        return provider.getActiveDatabase().leagueContextDao()
    }

    @Provides
    fun provideChairmanDao(provider: CareerDatabaseProvider): ChairmanDao {
        return provider.getActiveDatabase().chairmanDao()
    }

    @Provides
    fun provideTrainingScheduleDao(provider: CareerDatabaseProvider): TrainingScheduleDao {
        return provider.getActiveDatabase().trainingScheduleDao()
    }

    @Provides
    fun provideRankingsDao(provider: CareerDatabaseProvider): RankingsDao {
        return provider.getActiveDatabase().rankingsDao()
    }

    @Provides
    fun providePurchaseHistoryDao(provider: CareerDatabaseProvider): PurchaseHistoryDao {
        return provider.getActiveDatabase().purchaseHistoryDao()
    }

    @Provides
    fun provideSponsorshipDealDao(provider: CareerDatabaseProvider): SponsorshipDealDao {
        return provider.getActiveDatabase().sponsorshipDealDao()
    }
}