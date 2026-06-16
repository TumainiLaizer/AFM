package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.StaffEntity
import com.fameafrica.afm.data.database.entities.StaffRole
import com.fameafrica.afm.data.database.entities.TeamsEntity
import com.fameafrica.afm.data.repository.StaffRepository
import com.fameafrica.afm.data.repository.TeamsRepository
import com.fameafrica.afm.utils.NameGenerator
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class StaffGenerator @Inject constructor(
    private val teamsRepository: TeamsRepository,
    private val staffRepository: StaffRepository,
    private val clubDNARepository: com.fameafrica.afm.data.repository.ClubDNARepository,
    private val nameGenerator: NameGenerator
) {

    suspend fun generateDefaultStaffForAllClubs(onProgress: (Float) -> Unit = {}) {
        val teams = teamsRepository.getAllTeamsSync()
        val totalTeams = teams.size

        teams.forEachIndexed { index, team ->
            val staffCount = staffRepository.getStaffByTeamSync(team.id).size
            if (staffCount == 0) {
                val dna = clubDNARepository.getClubDNA(team.id)
                val staffList = generateFullStaffForTeam(team, dna)
                staffRepository.insertAllStaff(staffList)
            }
            onProgress((index + 1).toFloat() / totalTeams)
        }
    }

    private fun generateFullStaffForTeam(team: TeamsEntity, dna: com.fameafrica.afm.data.database.entities.ClubDNAEntity?): List<StaffEntity> {
        val usedNames = mutableSetOf<String>()
        return StaffRole.entries.map { role ->
            var name = nameGenerator.generateName(team.country)
            var attempts = 0
            while (usedNames.contains(name) && attempts < 50) {
                name = nameGenerator.generateName(team.country)
                attempts++
            }
            usedNames.add(name)
            generateStaffForRole(team, role, name, dna)
        }
    }

    private fun generateStaffForRole(
        team: TeamsEntity,
        role: StaffRole,
        name: String,
        dna: com.fameafrica.afm.data.database.entities.ClubDNAEntity?
    ): StaffEntity {
        val nationality = team.country
        val rating = (40 + Random.nextInt(40) + (team.reputation / 4)).coerceIn(30, 95)
        val age = 35 + Random.nextInt(30)

        val specialization = determineSpecialization(role, dna)
        
        return StaffEntity(
            name = name,
            role = role.value,
            staffType = role.staffType,
            teamId = team.id,
            teamName = team.name,
            specialization = specialization,
            impactRating = rating,
            salary = (rating * 20000).coerceAtLeast(500000),
            experienceLevel = (rating / 5).coerceIn(0, 20),
            nationality = nationality,
            age = age,
            isHeadOfDepartment = role.name.contains("CHIEF") || role.name.contains("HEAD") || role.name.contains("DIRECTOR")
        )
    }

    private fun determineSpecialization(role: StaffRole, dna: com.fameafrica.afm.data.database.entities.ClubDNAEntity?): String {
        // 1. Role-specific forced specializations
        val roleMapping = when (role) {
            StaffRole.GOALKEEPER_COACH -> com.fameafrica.afm.data.database.entities.Specialization.GOALKEEPING
            StaffRole.FITNESS_COACH -> com.fameafrica.afm.data.database.entities.Specialization.FITNESS
            StaffRole.YOUTH_COACH -> com.fameafrica.afm.data.database.entities.Specialization.YOUTH_DEVELOPMENT
            StaffRole.TECHNICAL_COACH -> com.fameafrica.afm.data.database.entities.Specialization.TECHNICAL
            StaffRole.SET_PIECE_COACH -> com.fameafrica.afm.data.database.entities.Specialization.SET_PIECES
            StaffRole.ATTACKING_COACH -> com.fameafrica.afm.data.database.entities.Specialization.ATTACKING
            StaffRole.DEFENSIVE_COACH -> com.fameafrica.afm.data.database.entities.Specialization.DEFENSIVE
            StaffRole.YOUTH_SCOUT -> com.fameafrica.afm.data.database.entities.Specialization.YOUTH
            StaffRole.SPORTS_SCIENTIST -> com.fameafrica.afm.data.database.entities.Specialization.SPORTS_SCIENCE
            StaffRole.NUTRITIONIST -> com.fameafrica.afm.data.database.entities.Specialization.NUTRITION
            StaffRole.MEDIA_OFFICER -> com.fameafrica.afm.data.database.entities.Specialization.MEDIA
            StaffRole.ACADEMY_DIRECTOR, StaffRole.HEAD_OF_YOUTH -> com.fameafrica.afm.data.database.entities.Specialization.YOUTH_DEVELOPMENT
            else -> null
        }

        if (roleMapping != null) return roleMapping.value

        // 2. DNA Influenced specializations for general roles
        val playStyle = dna?.playStyle?.uppercase()
        val influencedSpecs = mutableListOf<com.fameafrica.afm.data.database.entities.Specialization>()
        
        when (playStyle) {
            "POSSESSION" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.TACTICAL, com.fameafrica.afm.data.database.entities.Specialization.TECHNICAL, com.fameafrica.afm.data.database.entities.Specialization.MIDFIELD))
            "GEGENPRESS" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.FITNESS, com.fameafrica.afm.data.database.entities.Specialization.TACTICAL))
            "DIRECT_PHYSICAL" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.FITNESS, com.fameafrica.afm.data.database.entities.Specialization.SET_PIECES, com.fameafrica.afm.data.database.entities.Specialization.DEFENSIVE))
            "FLAIR_EXPRESSIVE" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.ATTACKING, com.fameafrica.afm.data.database.entities.Specialization.TECHNICAL))
            "WING_PLAY" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.ATTACKING, com.fameafrica.afm.data.database.entities.Specialization.TACTICAL))
            "TACTICAL_DISCIPLINE" -> influencedSpecs.addAll(listOf(com.fameafrica.afm.data.database.entities.Specialization.DEFENSIVE, com.fameafrica.afm.data.database.entities.Specialization.TACTICAL, com.fameafrica.afm.data.database.entities.Specialization.OPPOSITION))
        }

        if ((dna?.youthPriority ?: 50) > 70) {
            influencedSpecs.add(com.fameafrica.afm.data.database.entities.Specialization.YOUTH_DEVELOPMENT)
        }

        // 70% chance to pick from DNA-influenced list if it exists
        if (influencedSpecs.isNotEmpty() && Random.nextInt(100) < 70) {
            return influencedSpecs.random().value
        }

        // 3. Fallback to random specialization from all available ones
        return com.fameafrica.afm.data.database.entities.Specialization.entries.random().value
    }
}
