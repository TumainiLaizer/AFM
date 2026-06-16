package com.fameafrica.afm.domain.manager

import com.fameafrica.afm.data.database.entities.ChairmanEntity
import com.fameafrica.afm.data.database.entities.FinancialBehavior
import com.fameafrica.afm.data.repository.*
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class ChairmanSystem @Inject constructor(
    private val managersRepository: ManagersRepository,
    private val teamsRepository: TeamsRepository,
    private val clubDNARepository: ClubDNARepository,
    private val chairmanRepository: ChairmanRepository,
    private val newsRepository: NewsRepository
) {
    /**
     * Checks for potential chairmen takeovers across the league.
     */
    suspend fun processWeeklyChairmenChanges() {
        // Optimize: Only check a random selection of 3 teams each week
        val candidateTeams = teamsRepository.getAllTeams().firstOrNull()?.shuffled()?.take(3) ?: return
        
        for (team in candidateTeams) {
            // Lower chances to keep takeovers special and reduce processing
            val takeoverChance = when (team.financialBehavior) {
                FinancialBehavior.UNSTABLE -> 0.02
                FinancialBehavior.RISKY -> 0.01
                else -> 0.001
            }

            if (Random.nextDouble() < takeoverChance) {
                performTakeover(team.id, team.name)
            }
        }
    }

    private suspend fun performTakeover(teamId: Int, teamName: String) {
        val oldChairman = chairmanRepository.getChairmanByTeam(teamId)
        val clubDNA = clubDNARepository.getClubDNA(teamId)
        val region = clubDNA?.region ?: "EAST_AFRICA"

        // 1. Try to fetch from predefined Chairman pool first
        val poolCandidates = chairmanRepository.getAvailableChairmenByRegion(region)
        val selectedCandidate = poolCandidates.randomOrNull()

        val newChairman = if (selectedCandidate != null) {
            // Use predefined chairman
            chairmanRepository.markAsUnavailable(selectedCandidate.id)
            selectedCandidate.copy(teamId = teamId, isAvailable = false, entryMode = "TAKEOVER")
        } else {
            // Fallback to regionalized random generation
            generateRegionalRandomChairman(teamId, region)
        }
        
        // 2. Insert New Chairman
        chairmanRepository.insertChairman(newChairman)
        
        // 3. Mark old chairman as no longer at the club (if exists)
        oldChairman?.let {
            chairmanRepository.updateChairman(it.copy(teamId = null, isAvailable = true))
        }

        // 4. Reset/Update Club DNA based on new chairman personality
        clubDNA?.let { dna ->
            val newBehavior = when {
                newChairman.wealthLevel > 85 -> FinancialBehavior.SPENDER
                newChairman.businessSkill > 80 -> FinancialBehavior.CORPORATE_STRUCTURED
                newChairman.wealthLevel < 30 -> FinancialBehavior.FRUGAL
                else -> FinancialBehavior.values().filter { 
                    it != FinancialBehavior.UNSTABLE && it != FinancialBehavior.RISKY 
                }.random()
            }
            
            clubDNARepository.updateClubDNA(dna.copy(
                financialBehavior = newBehavior,
                identityStrength = (dna.identityStrength - 25).coerceAtLeast(5) // Identity takes a hit during takeover
            ))
            
            // Sync team entity behavior too
            teamsRepository.updateFinancialBehavior(teamId, newBehavior)
        }

        // 5. Trigger News
        val narrative = when {
            newChairman.wealthLevel > 85 -> "The billionaire's arrival promises a massive injection of funds into the transfer market."
            newChairman.businessSkill > 80 -> "The new owner is expected to implement a highly structured corporate model."
            newChairman.patienceLevel < 25 -> "Known for a short fuse, the new chairman may demand instant results."
            else -> "Fans are hopeful for a bright future under the new regime."
        }

        newsRepository.createNewsArticle(
            headline = "TAKEOVER: New Era at $teamName",
            content = "Business mogul ${newChairman.name} has completed the acquisition of $teamName. $narrative",
            category = "TAKEOVER",
            relatedTeamId = teamId,
            relatedTeam = teamName,
            isTopNews = true
        )
    }

    private fun generateRegionalRandomChairman(teamId: Int, region: String): ChairmanEntity {
        val (names, surnames) = getRegionalNamePools(region)
        
        return ChairmanEntity(
            name = "${names.random()} ${surnames.random()}",
            teamId = teamId,
            wealthLevel = (20..100).random(),
            patienceLevel = (10..90).random(),
            ambitionLevel = (40..100).random(),
            businessSkill = (30..95).random(),
            footballKnowledge = (10..80).random(),
            entryMode = "TAKEOVER",
            age = (40..75).random(),
            nationality = getRegionalNationality(region),
            isAvailable = false,
            preferredRegion = region
        )
    }

    private fun getRegionalNamePools(region: String): Pair<List<String>, List<String>> {
        return when (region) {
            "WEST_AFRICA" -> Pair(
                listOf("Kwame", "Olumide", "Kofi", "Moussa", "Abdoulaye", "Yaw", "Chinedu", "Ibrahim", "Tunde", "Femi"),
                listOf("Mensah", "Okonkwo", "Sow", "Diallo", "Toure", "Bello", "Adeyemi", "Kone", "Gaye", "Diop")
            )
            "NORTH_AFRICA" -> Pair(
                listOf("Ahmed", "Mohamed", "Youssef", "Omar", "Hassan", "Tarek", "Karim", "Mustafa", "Amine", "Walid"),
                listOf("Mansour", "Sawiris", "El-Sayed", "Ben Ali", "Trabelsi", "Brahimi", "Ghazouani", "Haddad", "Salem", "Zaki")
            )
            "EAST_AFRICA" -> Pair(
                listOf("Juma", "Kassim", "Tumaini", "Joseph", "Samuel", "Peter", "David", "John", "Moses", "Daniel"),
                listOf("Mbeki", "Kagame", "Odinga", "Kenyatta", "Njoroge", "Kamau", "Mwangi", "Maina", "Otieno", "Ochieng")
            )
            "SOUTHERN_AFRICA" -> Pair(
                listOf("Thabo", "Sipho", "Lindiwe", "Bongani", "Zanele", "Musa", "Themba", "Sibusiso", "Mandla", "Zweli"),
                listOf("Dlamini", "Zuma", "Mbeki", "Motsepe", "Rupert", "Oppenheimer", "Khoza", "Motaung", "Kadalie", "Molefe")
            )
            "CENTRAL_AFRICA" -> Pair(
                listOf("Jean", "Pierre", "Dieudonné", "Pascal", "Felix", "Blaise", "Guy", "Lucien", "Emery", "Michel"),
                listOf("Bongo", "Nguesso", "Kabila", "Mobutu", "Tshisekedi", "Biya", "Deby", "Sassou", "Obiang", "Patassé")
            )
            else -> Pair(
                listOf("Aliko", "Nassef", "Johann", "Patrice", "Nicky", "Strive", "Issad", "Mohamed", "Naguib", "Mike"),
                listOf("Dangote", "Sawiris", "Rupert", "Motsepe", "Oppenheimer", "Masiyiwa", "Rebrab", "Mansour", "Adenuga", "Rabiu")
            )
        }
    }

    private fun getRegionalNationality(region: String): String {
        return when (region) {
            "WEST_AFRICA" -> listOf("Nigeria", "Ghana", "Senegal", "Ivory Coast", "Mali").random()
            "NORTH_AFRICA" -> listOf("Egypt", "Morocco", "Algeria", "Tunisia", "Libya").random()
            "EAST_AFRICA" -> listOf("Tanzania", "Kenya", "Uganda", "Ethiopia", "Rwanda").random()
            "SOUTHERN_AFRICA" -> listOf("South Africa", "Zimbabwe", "Zambia", "Botswana", "Namibia").random()
            "CENTRAL_AFRICA" -> listOf("Cameroon", "DR Congo", "Gabon", "Congo", "CAR").random()
            else -> "Tanzania"
        }
    }
}
