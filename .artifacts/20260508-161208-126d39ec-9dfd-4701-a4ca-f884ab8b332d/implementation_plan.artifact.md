# AFM2026 Architecture Refactor & living World Simulation Plan

This plan outlines the refactor of AFM2026 to create a more immersive and "alive" football simulation, combining Chairman and Manager modes and ensuring a persistent global engine.

## User Review Required

> [!IMPORTANT]
> - **Database Migration**: The refactor to use IDs for all relations will require careful migration of existing saves or a reset of the database schema.
> - **Performance**: Simulating all leagues weekly will increase processing time between turns. Optimization strategies like parallel simulation will be considered.

## Proposed Changes

### Domain Layer (Simulation & Generation)

#### [PlayerGenerator.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/domain/manager/PlayerGenerator.kt)

- Align player ratings and potential with `LeagueRankings.kt` IFFHS hierarchy.
- Implement a granular league factor system based on country rank and division level.

```kotlin
// Proposed logic for baseRating calculation
val countryRankMultiplier = LeagueRankings.getQualityMultiplier(team.country)
val baseReputationRating = (team.reputation * 0.7 + (6 - (league?.level ?: 3)) * 6).coerceIn(20, 95)
val baseRating = (baseReputationRating * countryRankMultiplier).toInt().coerceIn(20, 95)

// Max rating cap based on league rank
val countryRank = LeagueRankings.getRank(team.country)
val levelPenalty = ((league?.level ?: 1) - 1) * 8
val maxRating = (when {
    countryRank <= 3 -> 90 // Elite (Egypt, Morocco, SA)
    countryRank <= 10 -> 82 // Top (Algeria, Tunisia, Tanzania)
    countryRank <= 20 -> 75 // High
    countryRank <= 35 -> 65 // Medium
    else -> 55 // Lower
} - levelPenalty).coerceAtLeast(45)
```

#### [WorldSimulationEngine.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/domain/manager/WorldSimulationEngine.kt)

- Enhance `simulateWeek` to ensure global simulation of all leagues.
- Integrate advanced News Engine triggers for global events (big results in other leagues, major transfers).
- Implement persistent world state updates for rising/falling clubs across the continent.

#### [LeagueSimulator.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/domain/manager/LeagueSimulator.kt)

- Ensure `simulateAllLeagues` processes all fixtures scheduled for the current week across all competitions.
- Improve match importance detection (Derbies, Title Deciders) to trigger specific news and morale impacts.

---

### Data Layer (Database Refactor)

#### [TeamsEntity.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/data/database/entities/TeamsEntity.kt)
#### [FixturesEntity.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/data/database/entities/FixturesEntity.kt)
#### [LeaguesEntity.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/data/database/entities/LeaguesEntity.kt)

- Enforce ID-based relations.
- Remove redundant name fields where possible, or mark them as secondary cache fields.
- Update DAOs to support ID-based joins for all relevant queries.

---

### UI Layer (Dense Information & Dual Mode)

#### [DashboardViewModel.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM2026/app/src/main/java/com/fameafrica/afm2026/ui/screen/dashboard/DashboardViewModel.kt)

- Implement `CareerMode` check (Chairman vs Manager) to toggle dashboard layout.
- Chairman: Focus on Finances, Board, Infrastructure, Sponsorships.
- Manager: Focus on Matches, Tactics, Squad, Training.

#### UI Components (CAF Premium Palette)

- Implement a "Dense Information" design system (11-13sp text for stats and tables).
- Adopt the CAF-inspired palettes (Chairman: Gold/Amber, Manager: Blue/Green/Grey, News: Black/White/Red).

---

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure compilation after refactor.
- Implement unit tests for `PlayerGenerator` to verify rating distribution across different leagues.
- Verify ID-based relation consistency in `TeamsDao` and `FixturesDao`.

### Manual Verification
- Start a new career and verify player generation quality for various countries (Egypt vs Chad).
- Progress through 5 weeks and verify that other leagues are being simulated and news is generated.
- Toggle between Manager and Chairman modes (if possible via debug settings) to verify UI layout shifts.
- Inspect the Dashboard for information density and color palette compliance.
