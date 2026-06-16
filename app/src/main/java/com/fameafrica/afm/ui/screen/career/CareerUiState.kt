package com.fameafrica.afm.ui.screen.career

import com.fameafrica.afm.data.database.entities.LeaguesEntity
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.database.entities.ManagersEntity
import com.fameafrica.afm.data.database.entities.ClubDNAEntity
import com.fameafrica.afm.data.database.entities.ChairmanEntity
import kotlinx.serialization.Serializable

data class CareerUiState(
    // ============ STEP 0: Initialization ============
    val currentStep: CareerStep = CareerStep.INITIALIZING,
    val initializationProgress: Float = 0f,
    val initializationStatus: String = "Preparing African Football Universe...",

    // ============ STEP 1: Manager Selection ============
    val managerSelectionMode: ManagerSelectionMode = ManagerSelectionMode.CREATE_NEW,
    val availableManagers: List<ManagersEntity> = emptyList(),
    val selectedExistingManager: ManagersEntity? = null,
    val isLoadingManagers: Boolean = false,

    // ============ STEP 2: Manager Creation (Conditional) ============
    val managerName: String = "Joseph Laizer",
    val managerNameError: String? = null,
    val managerNationality: String = "Tanzania",
    val managerAge: Int = 30,
    val managerBirthYear: Int = 1995,
    val managerBirthMonth: Int = 1,
    val managerBirthDay: Int = 1,
    val managerStyle: ManagerialStyle = ManagerialStyle.BALANCED,
    val coachingLicense: CoachingLicense = CoachingLicense.CAF_D,
    val preferredFormation: String = "4-4-2",
    val specialAbility: String = "GENERAL",

    // Avatar Selection
    val selectedAvatar: String = "coach_male_east", // Default avatar
    val selectedAvatarRegion: String = "EAST_AFRICA",

    // Manager Attributes (auto-generated)
    val tacticalFlexibility: Int = 50,
    val playerMotivation: Int = 50,
    val youthDevelopmentFocus: Int = 50,
    val financialSavvy: Int = 50,
    val mediaHandling: Int = 50,
    val disciplineLevel: Int = 50,
    val adaptability: Int = 50,
    val managerReputationLevel: String = "Local",

    val managerBackground: ManagerBackground? = null,
    val countryModifier: CountryModifierValues = CountryModifierValues(
        1f, 1f, 1f, 1f, 1f,
        1f, 1f, "Standard"
    ),
    val personalityProfile: PersonalityProfile? = null,
    val assignedAgent: Agent? = null,
    val availableAgents: List<Agent> = emptyList(),
    val filteredAgents: List<Agent> = emptyList(),
    val agentSearchQuery: String = "",
    val agentPersonalityFilter: String? = null,
    val isLoadingAgents: Boolean = false,
    val careerVision: CareerVision? = null,

    val favoriteTactics: TacticProfile = TacticProfile("4-4-2", "Balanced"),
    val matchesManaged: Int = 0,

    // ============ STEP 3: Country Selection ============
    val selectedCountryId: Int? = null,
    val selectedCountryName: String? = null,
    val availableLeagues: List<LeaguesEntity> = emptyList(),
    val leaguesByLevel: Map<Int, List<LeaguesEntity>> = emptyMap(),

    // ============ STEP 4: League Selection ============
    val selectedLeague: LeaguesEntity? = null,

    // ============ STEP 5: Club Selection ============
    val availableClubs: List<TeamsEntity> = emptyList(),
    val availableDivisions: List<DivisionInfo> = emptyList(),
    val selectedDivisionId: Int? = null,
    val filteredClubs: List<TeamsEntity> = emptyList(),
    val selectedClub: TeamsEntity? = null,
    val selectedClubDNA: ClubDNAEntity? = null,
    val selectedClubChairman: ChairmanEntity? = null,
    val showDNADialog: Boolean = false,
    val showOwnershipDialog: Boolean = false,

    // ============ STEP 6: Difficulty Selection ============
    val selectedDifficulty: Difficulty? = null,

    // ============ PREPARING GAME ============
    val preparationProgress: Float = 0f,
    val preparationStatus: String = "Preparing your office...",

    // ============ GAME STATE ============
    val gameMode: GameMode = GameMode.MANAGER,
    val isFirstCareer: Boolean = true,
    val canStartCareer: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val setupComplete: Boolean = false,
    val careerId: Long? = null,
    val leaguesByCountry: Map<String, List<LeaguesEntity>> = emptyMap(),
    val showOverwriteWarning: Boolean = false,
) {
    /**
     * Centralized logic for determining if the "CONTINUE" button should be enabled.
     * Derived property to ensure the UI is always in sync with the state.
     */
    val canProceedToNextStep: Boolean
        get() = when (currentStep) {
            CareerStep.INITIALIZING -> initializationProgress >= 1.0f
            CareerStep.MODE_SELECTION -> true // Can always proceed from mode selection (default is Manager)
            CareerStep.MANAGER_SELECTION -> true // Mode selected, proceed to creation or selection
            CareerStep.EXISTING_MANAGER_SELECTION -> selectedExistingManager != null
            CareerStep.MANAGER_CREATION -> managerName.isNotBlank() && managerNameError == null
            CareerStep.AVATAR_CUSTOMIZATION -> selectedAvatar.isNotBlank()
            CareerStep.AGENT_SELECTION -> assignedAgent != null
            CareerStep.CAREER_VISION -> careerVision != null
            CareerStep.COUNTRY_SELECTION -> selectedCountryId != null
            CareerStep.LEAGUE_SELECTION -> selectedLeague != null
            CareerStep.CLUB_SELECTION -> selectedClub != null
            CareerStep.DIFFICULTY_SELECTION -> selectedDifficulty != null
            CareerStep.CAREER_SUMMARY -> true
            CareerStep.PREPARING_GAME -> false
        }

    val careerStage: String
        get() = when {
            managerAge <= 35 -> "Young Manager"
            managerAge <= 45 -> "Prime Years"
            managerAge <= 55 -> "Experienced"
            managerAge <= 65 -> "Veteran"
            else -> "Legend"
        }

    val reputationDescription: String
        get() = when (managerReputationLevel) {
            "Local" -> "Local Coach"
            "National" -> "National Coach"
            "Continental" -> "Continental Coach"
            "World Class" -> "World Class Coach"
            else -> managerReputationLevel
        }

    val experienceLevel: String
        get() = when {
            matchesManaged >= 500 -> "Legendary"
            matchesManaged >= 300 -> "Elite"
            matchesManaged >= 200 -> "Experienced"
            matchesManaged >= 100 -> "Established"
            matchesManaged >= 50 -> "Developing"
            else -> "Rookie"
        }
}

enum class CareerStep {
    INITIALIZING,
    MODE_SELECTION,

    // MANAGER FLOW
    MANAGER_SELECTION,
    EXISTING_MANAGER_SELECTION,
    MANAGER_CREATION,
    AVATAR_CUSTOMIZATION,
    AGENT_SELECTION,
    CAREER_VISION,
    COUNTRY_SELECTION,
    LEAGUE_SELECTION,
    CLUB_SELECTION,
    DIFFICULTY_SELECTION,
    CAREER_SUMMARY,
    PREPARING_GAME
}

@Serializable
data class ChairmanProfile(
    val name: String,
    val age: Int,
    val nationality: String,
    val wealthLevel: Int,        // 0–100
    val ambition: Int,           // 0–100
    val patience: Int,           // affects sack speed
    val businessSkill: Int,      // sponsorships
    val footballKnowledge: Int   // hiring quality
)

enum class GameMode {
    MANAGER, CHAIRMAN
}

enum class ManagerSelectionMode {
    CREATE_NEW,
    USE_EXISTING
}

enum class ManagerialStyle(val displayName: String, val description: String) {
    BALANCED("Balanced", "All-round manager with no particular specialty"),
    ATTACKING("Attacking", "Focuses on offensive play and scoring goals"),
    DEFENSIVE("Defensive", "Prioritizes defensive organization and clean sheets"),
    POSSESSION("Possession", "Controls games through ball retention"),
    COUNTER("Counter Attack", "Specializes in quick transitions and counter-attacks"),
    YOUTH_DEVELOPMENT("Youth Dev", "Focuses on developing young players"),
    TACTICIAN("Tactician", "Excellent tactical flexibility and adaptability"),
    MOTIVATOR("Motivator", "Gets the best out of players through man-management"),
    DISCIPLINARIAN("Disciplinarian", "Strict with players, maintains high standards"),
    MEDIA_FRIENDLY("Media Friendly", "Handles press conferences exceptionally well")
}

enum class CoachingLicense(val displayName: String, val reputationBonus: Int) {
    CAF_D("CAF D", 1),
    CAF_C("CAF C", 5),
    CAF_B("CAF B", 10),
    CAF_A("CAF A", 15),
    CAF_PRO("CAF Pro", 20)
}

enum class SpecialAbility(val displayName: String, val description: String) {
    GENERAL("General", "Balanced attributes across all areas"),
    TACTICAL_GENIUS("Tactical Genius", "Higher tactical flexibility and game reading"),
    MOTIVATOR("Motivator", "Higher player motivation and squad harmony"),
    YOUTH_DEVELOPER("Youth Developer", "Focuses on academy integration and player growth"),
    DEFENSIVE_SPECIALIST("Defensive Specialist", "Expert at defensive organization and clean sheets"),
    ATTACKING_SPECIALIST("Attacking Specialist", "Focuses on goal scoring and high-pressure play"),
    SET_PIECE_GURU("Set Piece Guru", "Specializes in dead-ball situations and organization")
}

enum class Difficulty(
    val displayName: String,
    val description: String,
    val boardExpectationModifier: Float,
    val fanPatienceModifier: Float,
    val mediaPressureModifier: Float,
    val transferFlexibilityModifier: Float,
    val reputationGainModifier: Float
) {
    EASY(
        "Easy",
        "Forgiving board, patient fans, low pressure - perfect for beginners",
        0.7f, 1.5f, 0.5f, 1.3f, 1.2f
    ),
    NORMAL(
        "Normal",
        "Balanced experience - the way the game is meant to be played",
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f
    ),
    HARD(
        "Hard",
        "Strict board, impatient fans, high pressure - for experienced managers",
        1.3f, 0.7f, 1.5f, 0.8f, 0.9f
    ),
    LEGEND(
        "Legend",
        "Extreme pressure, must win immediately - only for the brave",
        1.8f, 0.4f, 2.0f, 0.5f, 0.8f
    );

    fun getExpectationText(): String = when (this) {
        EASY -> "Comfortable mid-table finish"
        NORMAL -> "Top half finish"
        HARD -> "Challenge for the title"
        LEGEND -> "Win the league or you're out!"
    }

    fun getPatienceText(): String = when (this) {
        EASY -> "Very Patient (10+ bad results)"
        NORMAL -> "Patient (6-8 bad results)"
        HARD -> "Impatient (3-5 bad results)"
        LEGEND -> "Extremely Impatient (1-2 bad results)"
    }

    fun getPressureText(): String = when (this) {
        EASY -> "Low - Media ignores most results"
        NORMAL -> "Medium - Media attention after big games"
        HARD -> "High - Constant media scrutiny"
        LEGEND -> "Extreme - Every decision is analyzed"
    }
}

data class ModifierValues(
    val tacticalMod: Float,
    val managementMod: Float,
    val youthMod: Float,
    val financialMod: Float,
    val mediaMod: Float,
    val disciplineMod: Float,
    val adaptabilityMod: Float
)

data class CountryModifierValues(
    val tacticalMod: Float,
    val managementMod: Float,
    val youthMod: Float,
    val financialMod: Float,
    val mediaMod: Float,
    val disciplineMod: Float,
    val adaptabilityMod: Float,
    val description: String
)

data class ManagerBackground(
    val type: String, // "Ex-Player", "Grassroots Coach", "Elite Academy"
    val tacticalBonus: Int,
    val reputationBoost: Int,
    val description: String
)

data class Agent(
    val id: Int,
    val name: String,
    val agency: String,
    val reputation: Int,
    val negotiationSkill: Int,
    val personality: String, // "LOYAL", "SHARK", "NETWORKER", "HARD_BARGAINER"
    val commissionRate: Float,
    val connections: List<String>,
    val nationality: String = "Tanzania",
    val nationalityFlag: String? = null
)

@Serializable
data class PersonalityProfile(
    val temperament: Int, // 30-90 - affects press conferences, reactions
    val ambition: Int,    // 40-95 - affects career moves, contract demands
    val loyalty: Int,     // 20-90 - affects club commitment
    val ego: Int,         // 30-95 - affects player relationships, media
    val professionalism: Int, // 40-95 - affects training, discipline
    val pressure: Int,    // 35-90 - affects big matches, interviews
    val adaptability: Int // 30-90 - affects new country/league adjustment
)

enum class CareerVision(
    val displayName: String,
    val description: String,
    val primaryFocus: String,
    val timeHorizon: String
) {
    WIN_TROPHIES(
        "Win Trophies Immediately",
        "Build a winning team focused on silverware from day one",
        "Short-term success",
        "1-2 seasons"
    ),
    BUILD_YOUTH_ACADEMY(
        "Build Youth Academy",
        "Develop homegrown talent and create a sustainable future",
        "Long-term development",
        "5+ seasons"
    ),
    BECOME_TRANSFER_KING(
        "Become Transfer King",
        "Master the market, buy low, sell high, build wealth",
        "Market dominance",
        "3-4 seasons"
    ),
    BUILD_AFRICAN_DOMINANCE(
        "Build African Dominance",
        "Create the most dominant African team in history",
        "Continental legacy",
        "4-6 seasons"
    )
}

data class TacticProfile(
    val formation: String,
    val style: String,
    val pressing: String? = null,
    val buildUp: String? = null,
    val defensiveLine: String? = null
)

data class DivisionInfo(
    val id: Int,
    val name: String,
    val leagueName: String,
    val level: Int
)

data class CountryInfo(
    val id: Int,
    val databaseId: Int,
    val slug: String,
    val displayName: String,
    val flagEmoji: String,
    val isPlayable: Boolean = true
)

val PLAYABLE_COUNTRIES = listOf(
    CountryInfo(1, 1, "tanzania", "Tanzania", "🇹🇿"),
    CountryInfo(2, 25, "egypt", "Egypt", "🇪🇬"),
    CountryInfo(3, 19, "south_africa", "South Africa", "🇿🇦"),
    CountryInfo(4, 24, "tunisia", "Tunisia", "🇹🇳"),
    CountryInfo(5, 6, "congo_drc", "Congo DRC", "🇨🇩"),
    CountryInfo(6, 23, "algeria", "Algeria", "🇩🇿"),
    CountryInfo(7, 26, "angola", "Angola", "🇦🇴"),
    CountryInfo(8, 17, "morocco", "Morocco", "🇲🇦"),
    CountryInfo(9, 12, "nigeria", "Nigeria", "🇳🇬"),
    CountryInfo(10, 13, "cameroon", "Cameroon", "🇨🇲"),
    CountryInfo(11, 4, "rwanda", "Rwanda", "🇷🇼"),
    CountryInfo(12, 2, "kenya", "Kenya", "🇰🇪")
)
