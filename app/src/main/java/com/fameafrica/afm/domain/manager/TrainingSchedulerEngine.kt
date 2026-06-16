package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.*
import com.fameafrica.afm.data.repository.*
import com.fameafrica.afm.utils.GameDateManager
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingSchedulerEngine @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val playerTrainingRepository: PlayerTrainingRepository,
    private val staffRepository: StaffRepository,
    private val playersRepository: PlayersRepository,
    private val fixturesRepository: FixturesRepository,
    private val gameDateManager: GameDateManager
) {

    data class TrainingDepartment(
        val tacticalRating: Int,
        val technicalRating: Int,
        val physicalRating: Int,
        val mentalRating: Int,
        val recoveryRating: Int,
        val youthDevelopment: Int
    )

    /**
     * Calculates the aggregate ratings for a team's training department based on hired staff.
     */
    suspend fun calculateDepartmentRatings(teamId: Int): TrainingDepartment {
        val staffList = staffRepository.getStaffByTeam(teamId).firstOrNull() ?: emptyList()
        
        fun avgImpact(role: String) = staffList.filter { it.role == role }.map { it.impactRating }.average().takeIf { !it.isNaN() }?.toInt() ?: 40

        return TrainingDepartment(
            tacticalRating = avgImpact("TACTICAL_COACH").coerceAtLeast(avgImpact("ASSISTANT_MANAGER") - 10),
            technicalRating = avgImpact("TECHNICAL_COACH"),
            physicalRating = avgImpact("FITNESS_COACH"),
            mentalRating = avgImpact("ASSISTANT_MANAGER"),
            recoveryRating = avgImpact("SPORTS_SCIENTIST").coerceAtLeast(avgImpact("PHYSIOTHERAPIST") - 5),
            youthDevelopment = avgImpact("YOUTH_COACH")
        )
    }

    /**
     * Generates a monthly training schedule for a team.
     */
    suspend fun generateMonthlySchedule(
        teamId: Int,
        month: Int,
        year: Int,
        intensity: String = "NORMAL",
        focus: String = "BALANCED",
        isApproved: Boolean = true,
        generatedBy: String? = "Head of Performance"
    ): Long {
        val existing = trainingRepository.getSchedule(teamId, month, year)
        if (existing != null) {
            // If already exists but we want to re-generate (e.g. changed focus)
            trainingRepository.deleteDaysBySchedule(existing.id)
            val updated = existing.copy(
                globalIntensity = intensity,
                primaryFocus = focus,
                isApproved = isApproved,
                generatedBy = generatedBy
            )
            trainingRepository.updateSchedule(updated)
        }

        val scheduleId = existing?.id?.toLong() ?: trainingRepository.createSchedule(
            TrainingScheduleEntity(
                teamId = teamId, month = month, year = year,
                globalIntensity = intensity, primaryFocus = focus,
                isApproved = isApproved,
                generatedBy = generatedBy
            )
        )

        val daysInMonth = getDaysInMonth(month, year)
        val fixtures = fixturesRepository.getFixturesByTeam(teamId).firstOrNull() ?: emptyList()
        val matchDates = fixtures.map { it.matchDate.split(" ").first() }.toSet()

        val trainingDays = mutableListOf<TrainingDayEntity>()
        
        // Weekly focus logic: Week 1: Fitness, Week 2: Tactical, Week 3: Technical, Week 4: Match Prep
        // If focus is BALANCED, we rotate. If focus is specific, we stick more to it.
        
        for (day in 1..daysInMonth) {
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
            val weekNum = (day - 1) / 7 + 1
            val isMatchDay = matchDates.contains(dateStr)
            val dayBeforeMatch = matchDates.contains(String.format(Locale.US, "%04d-%02d-%02d", year, month, day + 1))
            val dayAfterMatch = matchDates.contains(String.format(Locale.US, "%04d-%02d-%02d", year, month, day - 1))

            val weeklyFocus = when (weekNum) {
                1 -> if (focus == "BALANCED") "PHYSICAL" else focus
                2 -> if (focus == "BALANCED") "TACTICAL" else focus
                3 -> if (focus == "BALANCED") "TECHNICAL" else focus
                else -> if (focus == "BALANCED") "MATCH_PREP" else focus
            }

            val (m, a, e) = when {
                isMatchDay -> Triple("REST", "MATCH_DAY", "RECOVERY")
                dayAfterMatch -> Triple("RECOVERY", "REST", "REST")
                dayBeforeMatch -> Triple("TACTICAL_SHAPE", "MATCH_PREP", "REST")
                else -> generateNormalDaySessions(weeklyFocus)
            }

            trainingDays.add(
                TrainingDayEntity(
                    scheduleId = scheduleId.toInt(),
                    date = dateStr,
                    morningSession = m,
                    afternoonSession = a,
                    eveningSession = e
                )
            )
        }

        trainingRepository.insertDays(trainingDays)
        return scheduleId
    }

    /**
     * Proposes a monthly schedule (called at the start of each month).
     */
    suspend fun proposeMonthlySchedule(teamId: Int, month: Int, year: Int): Long {
        val staffList = staffRepository.getStaffByTeam(teamId).firstOrNull() ?: emptyList()
        val assistant = staffList.find { it.role == "ASSISTANT_MANAGER" }?.name ?: "Assistant Manager"
        
        // Staff logic: if team is tired, propose RECOVERY focus. If preseason, PHYSICAL.
        val focus = "BALANCED" // In a real engine, this would be dynamic
        
        val scheduleId = generateMonthlySchedule(teamId, month, year, "NORMAL", focus, isApproved = false, generatedBy = assistant)
        
        // Return scheduleId
        return scheduleId
    }

    /**
     * Approves the proposed schedule.
     */
    suspend fun approveSchedule(scheduleId: Int) {
        val schedule = trainingRepository.getAllSchedulesForTeam(-1).firstOrNull()?.find { it.id == scheduleId } ?: return
        trainingRepository.updateSchedule(schedule.copy(isApproved = true))
    }

    private fun generateNormalDaySessions(focus: String): Triple<String, String, String> {
        return when (focus) {
            "TACTICAL" -> Triple("TACTICAL_SHAPE", "DEFENSIVE_ORGANIZATION", "REST")
            "TECHNICAL" -> Triple("TECHNICAL_DRILLS", "ATTACKING_MOVEMENT", "REST")
            "PHYSICAL" -> Triple("PHYSICAL_CONDITIONING", "GYM_WORK", "RECOVERY")
            "RECOVERY" -> Triple("RECOVERY", "REST", "REST")
            else -> Triple("TECHNICAL_DRILLS", "TACTICAL_SHAPE", "REST")
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Simulates a single training day for all teams.
     */
    suspend fun simulateTrainingDay(date: String) {
        val dailyTrainings = trainingRepository.getTrainingWithScheduleByDate(date)
        
        dailyTrainings.forEach { item ->
            if (!item.schedule.isApproved) return@forEach // Only train if approved

            val teamId = item.schedule.teamId
            val squad = playersRepository.getPlayersByTeamId(teamId).firstOrNull() ?: return@forEach
            val dept = calculateDepartmentRatings(teamId)
            
            val sessions = listOf(item.day.morningSession, item.day.afternoonSession, item.day.eveningSession)
            
            squad.forEach { player ->
                if (!player.isAvailable) return@forEach
                
                var staminaChange = 0
                var sharpnessChange = 0
                var moraleChange = 0
                val attributeGrowths = mutableMapOf<String, Double>()
                
                sessions.forEach { session ->
                    when (session) {
                        "RECOVERY" -> {
                            staminaChange += (10 + (dept.recoveryRating / 10))
                            sharpnessChange -= 2
                        }
                        "PHYSICAL_CONDITIONING" -> {
                            staminaChange -= (15 - (dept.physicalRating / 20))
                            sharpnessChange += 5
                            attributeGrowths["stamina"] = 0.05
                            attributeGrowths["strength"] = 0.05
                        }
                        "TECHNICAL_DRILLS" -> {
                            staminaChange -= 8
                            sharpnessChange += 6
                            attributeGrowths["passing"] = 0.08
                            attributeGrowths["dribbling"] = 0.08
                        }
                        "TACTICAL_SHAPE" -> {
                            staminaChange -= 6
                            sharpnessChange += 4
                            attributeGrowths["positioning"] = 0.06
                            attributeGrowths["decisions"] = 0.06
                        }
                        "MATCH_PREP" -> {
                            sharpnessChange += 15
                            staminaChange -= 5
                        }
                        "REST" -> {
                            staminaChange += 15
                            sharpnessChange -= 5
                        }
                        "TEAM_BONDING" -> {
                            moraleChange += 5
                            staminaChange += 2
                        }
                        "GYM_WORK" -> {
                            staminaChange -= 12
                            attributeGrowths["strength"] = 0.1
                        }
                    }
                }
                
                // Intensity Modifiers
                val intensityMult = when(item.schedule.globalIntensity) {
                    "LOW" -> 0.6f
                    "HIGH" -> 1.4f
                    "EXTREME" -> 1.8f
                    else -> 1.0f
                }
                
                // Injury Prevention from Staff
                val injuryChance = if (staminaChange < 0) {
                    val baseChance = (Math.abs(staminaChange) * intensityMult).toInt()
                    (baseChance - (dept.recoveryRating / 10)).coerceAtLeast(0)
                } else 0
                
                // Apply changes
                val finalStamina = (player.stamina + (staminaChange * intensityMult).toInt()).coerceIn(0, 100)
                val finalSharpness = (player.sharpness + (sharpnessChange * intensityMult).toInt()).coerceIn(0, 100)
                
                // Individual Development Plan (IDP) boost
                val activeIdp = playerTrainingRepository.getActiveTrainingForPlayer(player.name)
                if (activeIdp != null) {
                    val focusAttr = activeIdp.specificAttribute?.lowercase()
                    if (focusAttr != null) {
                        attributeGrowths[focusAttr] = (attributeGrowths[focusAttr] ?: 0.0) + 0.1
                    }
                }
                
                // Youth Development Track
                val youthBoost = if (player.age < 21) {
                    (dept.youthDevelopment / 100.0)
                } else 0.0

                // Growth logic
                val baseGrowthMult = if (player.age < 21) 2.0 else if (player.age < 25) 1.2 else 0.5
                val growthMult = baseGrowthMult + youthBoost
                val deptMult = dept.technicalRating / 50.0
                
                val updates = mutableMapOf<String, Int>()
                attributeGrowths.forEach { (attr, growth) ->
                    val totalGrowth = growth * growthMult * deptMult * intensityMult
                    if (Random().nextDouble() < totalGrowth / 5.0) {
                        val currentVal = getPlayerAttribute(player, attr)
                        if (currentVal < 99) {
                            updates[attr] = currentVal + 1
                        }
                    }
                }

                if (updates.isNotEmpty()) {
                    playersRepository.updatePlayerAttributes(player.id, updates, gameDateManager.getDayIndexFromDate(date))
                }

                playersRepository.updatePlayer(
                    player.copy(
                        stamina = finalStamina,
                        sharpness = finalSharpness,
                        morale = (player.morale + moraleChange).coerceIn(0, 100)
                    )
                )
            }
        }
    }

    private fun getPlayerAttribute(player: PlayersEntity, attr: String): Int {
        return when (attr.lowercase()) {
            "stamina" -> player.stamina
            "strength" -> player.strength
            "passing" -> player.passing
            "dribbling" -> player.dribbling
            "positioning" -> player.positioning
            "decisions" -> player.decisions
            "finishing" -> player.finishing
            "defending" -> player.defending
            else -> 50
        }
    }
}
