# AFM2026: Phase 6 — Master Orchestration & Event-Driven World

This phase refactors AFM2026 into a high-performance, immersive football universe. We are centralizing control into `WorldSimulationEngine` and using a tiered simulation approach to ensure speed and depth.

## User Review Required

> [!IMPORTANT]
> - **Consolidation**: `ContinentalSimulationEngine` will be merged into `WorldSimulationEngine` to reduce system duplication.
> - **Daily Master Tick**: The game advances one day at a time, but stops only for high-impact "Stop Conditions."
> - **Simulation Tiers**: We will implement 3 tiers of simulation depth to maintain 60fps performance on low-end devices.

## Proposed Changes

### Master Orchestration

#### [WorldSimulationEngine.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/domain/manager/WorldSimulationEngine.kt)
- **Role**: The Heart of the Game.
- **New Method**: `advanceOneDay()`
    - 1. Update Calendar
    - 2. Process Tiered Matches (Full/Quick/Weighted)
    - 3. Process Economy & Transfers
    - 4. Collect `SimulationEvent` objects
    - 5. Return Stop Condition recommendation
- **Consolidation**: absorb logic from `ContinentalSimulationEngine`.

#### [NEW] [SimulationEvent.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/domain/model/SimulationEvent.kt)
- Sealed class representing all world occurrences (e.g., `MatchPlayed`, `TransferOffer`, `Injury`, `BoardMeeting`).
- These events drive the **News Feed** and **Stop Logic**.

#### [GameManager.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/domain/manager/GameManager.kt)
- Refactor to handle the **Daily Tick**.
- Implement **Stop Conditions**: Evaluates `SimulationEvent` objects to decide if the "Advance" animation should pause.

---

### Tiered Simulation (Performance)

#### [LeagueSimulator.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/domain/manager/LeagueSimulator.kt)
- Implement **Tier 3 (Weighted Random)**: For background leagues.
- Implement **Tier 2 (Quick Sim)**: For viewed leagues.
- Use existing **Tier 1 (Full Sim)**: Only for user league and rivals.

---

### IMMERSIVE News & World Feed

#### [NEW] [WorldNewsGenerator.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/domain/manager/WorldNewsGenerator.kt)
- Stateless helper that converts `SimulationEvent` into dramatic headlines (e.g., "Simba SC fans furious after derby collapse").

#### [WorldScreen.kt](file:///C:/Users/HomePC/AndroidStudioProjects/AFM/app/src/main/java/com/fameafrica/afm/ui/screen/world/WorldScreen.kt)
- Refactor into a **Football Social Feed**.
- Prioritize high-impact stories (Transfer rumors, continental thrills, manager pressure).

## Verification Plan

### Automated Tests
- `SimulationTierTest`: Verify that Tier 3 simulation is at least 10x faster than Tier 1.
- `EventOrchestrationTest`: Verify that a `TransferOffer` event correctly triggers a "Stop Condition."

### Manual Verification
- **One More Day Loop**: Advance 10 days. Verify the UI only stops for your match or a transfer offer, and news updates silently in the background.
- **Drama Check**: Ensure the news feed feels like "Sky Sports + Twitter," not a spreadsheet export.
- **Performance**: Simulating a full month in background leagues should take < 5 seconds.
