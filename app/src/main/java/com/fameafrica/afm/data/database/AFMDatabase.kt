package com.fameafrica.afm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.database.dao.*

@Database(
    entities = [
        PlayersEntity::class,
        TeamsEntity::class,
        ManagersEntity::class,
        FixturesEntity::class,
        LeaguesEntity::class,
        CupsEntity::class,
        CareersEntity::class,
        ArchetypeTraitsEntity::class,
        BoardEvaluationEntity::class,
        BoardRequestsEntity::class,
        ClubLegendsEntity::class,
        CommunityShieldEntity::class,
        CupBracketsEntity::class,
        CupGroupStandingsEntity::class,
        CurrencyExchangeRatesEntity::class,
        EloHistoryEntity::class,
        FanExpectationsEntity::class,
        FanReactionsEntity::class,
        FinancesEntity::class,
        FixturesResultsEntity::class,
        GameSettingsEntity::class,
        GameStatesEntity::class,
        InfrastructureUpgradesEntity::class,
        InterviewsEntity::class,
        JournalistsEntity::class,
        KnockoutMatchesEntity::class,
        LeagueStandingsEntity::class,
        ManagerOffersEntity::class,
        ManagerOffersForRetiredPlayersEntity::class,
        MatchCommentaryEntity::class,
        MatchEventsEntity::class,
        MatchFixingCasesEntity::class,
        NationalTeamPlayersEntity::class,
        NationalTeamsEntity::class,
        NationalitiesEntity::class,
        NewsEntity::class,
        NotificationsEntity::class,
        ObjectivesEntity::class,
        PersonalityTypesEntity::class,
        PlayerAgentsEntity::class,
        AgentClientsEntity::class,
        PlayerContractsEntity::class,
        PlayerLoansEntity::class,
        PlayerReactionsEntity::class,
        PlayerTrainingEntity::class,
        PreseasonScheduleEntity::class,
        PressConferencesEntity::class,
        PrizesCupEntity::class,
        PrizesLeaguesEntity::class,
        RefereesEntity::class,
        RegionalSettingsEntity::class,
        ScoutAssignmentsEntity::class,
        ScoutingMissionsEntity::class,
        SeasonAwardsEntity::class,
        SeasonHistoryEntity::class,
        SettingsHistoryEntity::class,
        SponsorsEntity::class,
        StaffEntity::class,
        StoryEventsEntity::class,
        TacticsEntity::class,
        TransferFundingRequestEntity::class,
        TransferWindowsEntity::class,
        TransfersEntity::class,
        TrophiesEntity::class,
        UserAnalyticsEntity::class,
        UserPreferencesEntity::class,
        ShortlistEntity::class,
        PlayerFilterPresetEntity::class,
        IdentityMappingEntity::class,
        ClubVisionEntity::class,
        WorldStateEntity::class,
        LeagueContextEntity::class,
        ClubDNAEntity::class,
        RivalryEntity::class,
        PlayerFormEntity::class,
        ChairmanEntity::class,
        TrainingScheduleEntity::class,
        TrainingDayEntity::class,
        RankingsCacheEntity::class,
        PurchaseHistoryEntity::class,
        SponsorshipDealEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AFMDatabase : RoomDatabase() {
    abstract fun playersDao(): PlayersDao
    abstract fun teamsDao(): TeamsDao
    abstract fun managersDao(): ManagersDao
    abstract fun fixturesDao(): FixturesDao
    abstract fun leaguesDao(): LeaguesDao
    abstract fun cupsDao(): CupsDao
    abstract fun identityMappingDao(): IdentityMappingDao
    abstract fun careersDao(): CareersDao
    abstract fun archetypeTraitsDao(): ArchetypeTraitsDao
    abstract fun boardEvaluationDao(): BoardEvaluationDao
    abstract fun boardRequestsDao(): BoardRequestsDao
    abstract fun clubLegendsDao(): ClubLegendsDao
    abstract fun communityShieldDao(): CommunityShieldDao
    abstract fun cupBracketsDao(): CupBracketsDao
    abstract fun cupGroupStandingsDao(): CupGroupStandingsDao
    abstract fun currencyExchangeRatesDao(): CurrencyExchangeRatesDao
    abstract fun eloHistoryDao(): EloHistoryDao
    abstract fun fanExpectationsDao(): FanExpectationsDao
    abstract fun fanReactionsDao(): FanReactionsDao
    abstract fun financesDao(): FinancesDao
    abstract fun fixturesResultsDao(): FixturesResultsDao
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun gameStatesDao(): GameStatesDao
    abstract fun infrastructureUpgradesDao(): InfrastructureUpgradesDao
    abstract fun interviewsDao(): InterviewsDao
    abstract fun journalistsDao(): JournalistsDao
    abstract fun knockoutMatchesDao(): KnockoutMatchesDao
    abstract fun leagueStandingsDao(): LeagueStandingsDao
    abstract fun managerOffersDao(): ManagerOffersDao
    abstract fun managerOffersForRetiredPlayersDao(): ManagerOffersForRetiredPlayersDao
    abstract fun matchCommentaryDao(): MatchCommentaryDao
    abstract fun matchEventsDao(): MatchEventsDao
    abstract fun matchFixingCasesDao(): MatchFixingCasesDao
    abstract fun nationalTeamPlayersDao(): NationalTeamPlayersDao
    abstract fun nationalTeamsDao(): NationalTeamsDao
    abstract fun nationalitiesDao(): NationalitiesDao
    abstract fun newsDao(): NewsDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun objectivesDao(): ObjectivesDao
    abstract fun personalityTypesDao(): PersonalityTypesDao
    abstract fun playerAgentsDao(): PlayerAgentsDao
    abstract fun agentClientsDao(): AgentClientsDao
    abstract fun playerContractsDao(): PlayerContractsDao
    abstract fun playerLoansDao(): PlayerLoansDao
    abstract fun playerReactionsDao(): PlayerReactionsDao
    abstract fun playerTrainingDao(): PlayerTrainingDao
    abstract fun preseasonScheduleDao(): PreseasonScheduleDao
    abstract fun pressConferencesDao(): PressConferencesDao
    abstract fun prizesCupDao(): PrizesCupDao
    abstract fun prizesLeaguesDao(): PrizesLeaguesDao
    abstract fun refereesDao(): RefereesDao
    abstract fun regionalSettingsDao(): RegionalSettingsDao
    abstract fun scoutAssignmentsDao(): ScoutAssignmentsDao
    abstract fun scoutingMissionsDao(): ScoutingMissionsDao
    abstract fun seasonAwardsDao(): SeasonAwardsDao
    abstract fun seasonHistoryDao(): SeasonHistoryDao
    abstract fun settingsHistoryDao(): SettingsHistoryDao
    abstract fun sponsorsDao(): SponsorsDao
    abstract fun staffDao(): StaffDao
    abstract fun storyEventsDao(): StoryEventsDao
    abstract fun tacticsDao(): TacticsDao
    abstract fun transferFundingRequestsDao(): TransferFundingRequestsDao
    abstract fun transferWindowsDao(): TransferWindowsDao
    abstract fun transfersDao(): TransfersDao
    abstract fun trophiesDao(): TrophiesDao
    abstract fun userAnalyticsDao(): UserAnalyticsDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun shortlistDao(): ShortlistDao
    abstract fun playerFilterPresetDao(): PlayerFilterPresetDao
    abstract fun playerFormDao(): PlayerFormDao
    abstract fun clubVisionDao(): ClubVisionDao
    abstract fun worldStateDao(): WorldStateDao
    abstract fun leagueContextDao(): LeagueContextDao
    abstract fun clubDNADao(): ClubDNADao
    abstract fun chairmanDao(): ChairmanDao
    abstract fun rivalryDao(): RivalryDao
    abstract fun trainingScheduleDao(): TrainingScheduleDao
    abstract fun rankingsDao(): RankingsDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
    abstract fun sponsorshipDealDao(): SponsorshipDealDao
}
