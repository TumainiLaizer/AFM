# Tasks - Phase 6: Master Orchestration & Event-Driven World

- [ ] Core Orchestration Refactor
	- [ ] Create `SimulationEvent.kt`
	- [ ] Consolidate `ContinentalSimulationEngine` into `WorldSimulationEngine`
	- [ ] Implement `WorldSimulationEngine.advanceOneDay()`
	- [ ] Refactor `GameManager.kt` for daily master tick and stop conditions
- [ ] Tiered Simulation Performance
	- [ ] Implement Tier 3 (Weighted Random) in `LeagueSimulator.kt`
	- [ ] Implement Tier 2 (Quick Sim) for viewed leagues
	- [ ] Optimize Tier 1 (Full Sim) for user match and rivals
- [ ] Immersive World Feed
	- [ ] Create `WorldNewsGenerator.kt` (Stateless helper)
	- [ ] Refactor `WorldScreen.kt` to a high-density "Football Social Feed"
- [ ] Verification
	- [ ] Run performance unit tests (Tier 1 vs Tier 3)
	- [ ] Manual verification of the "One More Day" advancement loop
