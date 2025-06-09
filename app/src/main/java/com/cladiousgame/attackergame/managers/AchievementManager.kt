package com.cladiousgame.attackergame.managers

import android.content.Context
import android.content.SharedPreferences
import com.cladiousgame.attackergame.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Manages all achievement unlocking and tracking
 */
class AchievementManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Get all available achievements
     */
    fun getAllAchievements(): List<Achievement> = listOf(
        // BEGINNER ACHIEVEMENTS
        Achievement(
            "first_steps",              // id
            "first_steps",              // name
            "🎯 First Steps",           // title
            "Complete your first challenge", // description
            "🎯",                       // icon
            50,                         // pointsReward
            AchievementRarity.COMMON,   // rarity
            AchievementCondition.FirstChallenge // condition
        ),
        
        Achievement(
            "rising_star",
            "rising_star",
            "⭐ Rising Star",
            "Reach Level 5",
            "⭐",
            100,
            AchievementRarity.COMMON,
            AchievementCondition.ReachLevel(5)
        ),
        
        Achievement(
            "point_collector",
            "point_collector",
            "💰 Point Collector",
            "Earn 1000 total points",
            "💰",
            150,
            AchievementRarity.COMMON,
            AchievementCondition.EarnPoints(1000)
        ),
        
        // INTERMEDIATE ACHIEVEMENTS
        Achievement(
            "streak_master",
            "streak_master",
            "🔥 Streak Master",
            "Get a 10-challenge streak",
            "🔥",
            200,
            AchievementRarity.RARE,
            AchievementCondition.GetStreak(10)
        ),
        
        Achievement(
            "combo_king",
            "combo_king",
            "👑 Combo King",
            "Get a 5x combo in one challenge",
            "👑",
            250,
            AchievementRarity.RARE,
            AchievementCondition.GetCombo(5)
        ),
        
        Achievement(
            "perfectionist",
            "perfectionist",
            "✨ Perfectionist",
            "Complete 5 challenges without using hints",
            "✨",
            300,
            AchievementRarity.RARE,
            AchievementCondition.CompleteWithoutHints(5)
        ),
        
        Achievement(
            "speed_demon",
            "speed_demon",
            "⚡ Speed Demon",
            "Complete a challenge in under 30 seconds",
            "⚡",
            350,
            AchievementRarity.EPIC,
            AchievementCondition.SpeedRunner
        ),
        
        // ADVANCED ACHIEVEMENTS
        Achievement(
            "rookie_graduate",
            "rookie_graduate",
            "🎓 Rookie Graduate",
            "Complete all Rookie challenges",
            "🎓",
            500,
            AchievementRarity.EPIC,
            AchievementCondition.CompleteAllRookie
        ),
        
        Achievement(
            "hacker_elite",
            "hacker_elite",
            "💀 Hacker Elite",
            "Complete all Hacker challenges",
            "💀",
            750,
            AchievementRarity.EPIC,
            AchievementCondition.CompleteAllHacker
        ),
        
        Achievement(
            "expert_master",
            "expert_master",
            "🧠 Expert Master",
            "Complete all Expert challenges",
            "🧠",
            1000,
            AchievementRarity.LEGENDARY,
            AchievementCondition.CompleteAllExpert
        ),
        
        Achievement(
            "legendary_champion",
            "legendary_champion",
            "🏆 Legendary Champion",
            "Complete all Legendary challenges",
            "🏆",
            1500,
            AchievementRarity.LEGENDARY,
            AchievementCondition.CompleteAllLegendary
        ),
        
        // SPECIAL ACHIEVEMENTS
        Achievement(
            "night_owl",
            "night_owl",
            "🦉 Night Owl",
            "Complete a challenge between 11PM-5AM",
            "🦉",
            200,
            AchievementRarity.RARE,
            AchievementCondition.NightOwl
        ),
        
        Achievement(
            "early_bird",
            "early_bird",
            "🐦 Early Bird",
            "Complete a challenge between 5AM-9AM",
            "🐦",
            200,
            AchievementRarity.RARE,
            AchievementCondition.EarlyBird
        ),
        
        Achievement(
            "reentrancy_hunter",
            "reentrancy_hunter",
            "🔄 Reentrancy Hunter",
            "Find 10 reentrancy vulnerabilities",
            "🔄",
            400,
            AchievementRarity.EPIC,
            AchievementCondition.FindVulnerability(VulnerabilityType.REENTRANCY, 10)
        ),
        
        Achievement(
            "overflow_detector",
            "overflow_detector",
            "📈 Overflow Detector",
            "Find 10 integer overflow vulnerabilities",
            "📈",
            400,
            AchievementRarity.EPIC,
            AchievementCondition.FindVulnerability(VulnerabilityType.INTEGER_OVERFLOW, 10)
        ),
        
        Achievement(
            "access_guardian",
            "access_guardian",
            "🛡️ Access Guardian",
            "Find 10 access control vulnerabilities",
            "🛡️",
            400,
            AchievementRarity.EPIC,
            AchievementCondition.FindVulnerability(VulnerabilityType.ACCESS_CONTROL, 10)
        ),
        
        // MYTHIC ACHIEVEMENTS
        Achievement(
            "ultimate_hacker",
            "ultimate_hacker",
            "💎 Ultimate Hacker",
            "Complete 100 challenges",
            "💎",
            2000,
            AchievementRarity.MYTHIC,
            AchievementCondition.CompleteChallenges(100)
        ),
        
        Achievement(
            "security_god",
            "security_god",
            "🌟 Security God",
            "Reach Level 50",
            "🌟",
            3000,
            AchievementRarity.MYTHIC,
            AchievementCondition.ReachLevel(50)
        )
    )
    
    /**
     * Check and unlock achievements based on current progress
     */
    fun checkAndUnlockAchievements(
        progress: PlayerProgress,
        challengeRepository: ChallengeRepository,
        additionalContext: Map<String, Any> = emptyMap()
    ): List<Achievement> {
        val unlockedAchievements = getUnlockedAchievements()
        val newlyUnlocked = mutableListOf<Achievement>()
        
        for (achievement in getAllAchievements()) {
            if (achievement.id !in unlockedAchievements.map { it.id } && 
                isAchievementUnlocked(achievement, progress, challengeRepository, additionalContext)) {
                
                val unlockedAchievement = achievement.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )
                newlyUnlocked.add(unlockedAchievement)
                saveUnlockedAchievement(unlockedAchievement)
            }
        }
        
        return newlyUnlocked
    }
    
    /**
     * Check if specific achievement condition is met
     */
    private fun isAchievementUnlocked(
        achievement: Achievement,
        progress: PlayerProgress,
        challengeRepository: ChallengeRepository,
        additionalContext: Map<String, Any>
    ): Boolean {
        return when (achievement.condition) {
            AchievementCondition.FirstChallenge -> 
                progress.completedChallenges.isNotEmpty()
            
            is AchievementCondition.CompleteChallenges -> 
                progress.completedChallenges.size >= achievement.condition.count
            
            is AchievementCondition.ReachLevel -> 
                progress.level >= achievement.condition.level
            
            is AchievementCondition.GetStreak -> 
                progress.streak >= achievement.condition.streak
            
            is AchievementCondition.EarnPoints -> 
                progress.totalPoints >= achievement.condition.points
            
            is AchievementCondition.CompleteWithoutHints -> 
                progress.perfectSolves >= achievement.condition.count
            
            is AchievementCondition.GetCombo -> 
                (additionalContext["maxCombo"] as? Int ?: 0) >= achievement.condition.combo
            
            AchievementCondition.CompleteAllRookie -> {
                val rookieChallenges = challengeRepository.getAllChallenges()
                    .filter { it.difficulty == Difficulty.ROOKIE }
                rookieChallenges.all { it.id in progress.completedChallenges }
            }
            
            AchievementCondition.CompleteAllHacker -> {
                val hackerChallenges = challengeRepository.getAllChallenges()
                    .filter { it.difficulty == Difficulty.HACKER }
                hackerChallenges.all { it.id in progress.completedChallenges }
            }
            
            AchievementCondition.CompleteAllExpert -> {
                val expertChallenges = challengeRepository.getAllChallenges()
                    .filter { it.difficulty == Difficulty.EXPERT }
                expertChallenges.all { it.id in progress.completedChallenges }
            }
            
            AchievementCondition.CompleteAllLegendary -> {
                val legendaryChallenges = challengeRepository.getAllChallenges()
                    .filter { it.difficulty == Difficulty.LEGENDARY }
                legendaryChallenges.all { it.id in progress.completedChallenges }
            }
            
            AchievementCondition.SpeedRunner -> 
                (additionalContext["completionTime"] as? Long ?: Long.MAX_VALUE) < 30000
            
            AchievementCondition.NightOwl -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                hour >= 23 || hour < 5
            }
            
            AchievementCondition.EarlyBird -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                hour in 5..8
            }
            
            is AchievementCondition.FindVulnerability -> {
                val count = progress.vulnerabilitiesFound[achievement.condition.type] ?: 0
                count >= achievement.condition.count
            }
            
            is AchievementCondition.CompleteInTime -> {
                // Implementation for time-based completion
                false // Placeholder
            }
            
            AchievementCondition.PerfectWeek -> {
                // Implementation for perfect week
                false // Placeholder
            }
            
            else -> false // Handle any other cases
        }
    }
    
    /**
     * Get all unlocked achievements
     */
    fun getUnlockedAchievements(): List<Achievement> {
        val json = prefs.getString("unlocked_achievements", null)
        return if (json != null) {
            val type = object : TypeToken<List<Achievement>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    
    /**
     * Save unlocked achievement
     */
    private fun saveUnlockedAchievement(achievement: Achievement) {
        val current = getUnlockedAchievements().toMutableList()
        current.add(achievement)
        val json = gson.toJson(current)
        prefs.edit().putString("unlocked_achievements", json).apply()
    }
    
    /**
     * Get achievement progress percentage
     */
    fun getAchievementProgress(): Float {
        val total = getAllAchievements().size
        val unlocked = getUnlockedAchievements().size
        return if (total > 0) (unlocked.toFloat() / total.toFloat()) * 100f else 0f
    }
    
    /**
     * Get total achievement points earned
     */
    fun getTotalAchievementPoints(): Int {
        return getUnlockedAchievements().sumBy { it.pointsReward }
    }
    
    /**
     * Get achievements by rarity
     */
    fun getAchievementsByRarity(rarity: AchievementRarity): List<Achievement> {
        return getAllAchievements().filter { it.rarity == rarity }
    }
    
    /**
     * Get recent achievements (last 7 days)
     */
    fun getRecentAchievements(): List<Achievement> {
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return getUnlockedAchievements().filter { 
            (it.unlockedAt ?: 0) > weekAgo 
        }
    }
} 