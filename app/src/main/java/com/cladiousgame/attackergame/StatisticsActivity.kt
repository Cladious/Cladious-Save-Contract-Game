package com.cladiousgame.attackergame

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cladiousgame.attackergame.data.ChallengeRepository
import com.cladiousgame.attackergame.data.PlayerProgress
import com.cladiousgame.attackergame.data.VulnerabilityType
import com.cladiousgame.attackergame.databinding.ActivityStatisticsBinding

/**
 * Statistics activity showing player progress and achievements
 */
class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var repository: ChallengeRepository
    private lateinit var playerProgress: PlayerProgress
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = ChallengeRepository(this)
        playerProgress = repository.loadProgress()
        
        setupUI()
        displayStats()
    }
    
    private fun setupUI() {
        hideSystemUi()
        
        binding.backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun displayStats() {
        binding.apply {
            // Basic stats
            currentLevelText.text = "Level ${playerProgress.level}"
            totalPointsText.text = "${playerProgress.totalPoints} Points"
            completedChallengesText.text = "${playerProgress.completedChallenges.size} Challenges Completed"
            
            // Streak stats
            currentStreakText.text = "${playerProgress.streak}"
            bestStreakText.text = "${playerProgress.bestStreak}"
            
            // Progress to next level
            val pointsToNextLevel = playerProgress.pointsForNextLevel - playerProgress.totalPoints
            nextLevelText.text = "$pointsToNextLevel points to Level ${playerProgress.level + 1}"
            
            // Progress bar
            val progressPercent = (playerProgress.progressToNextLevel * 100).toInt()
            levelProgressBar.progress = progressPercent
            progressPercentText.text = "$progressPercent%"
            
            // Vulnerability stats
            displayVulnerabilityStats()
            
            // Achievements
            displayAchievements()
        }
    }
    
    private fun displayVulnerabilityStats() {
        val vulnerabilityStats = StringBuilder()
        
        VulnerabilityType.values().forEach { type ->
            val count = playerProgress.vulnerabilitiesFound[type] ?: 0
            vulnerabilityStats.append("${type.displayName}: $count found\n")
        }
        
        binding.vulnerabilityStatsText.text = vulnerabilityStats.toString().trim()
    }
    
    private fun displayAchievements() {
        val achievements = mutableListOf<String>()
        
        // Level achievements
        when {
            playerProgress.level >= 20 -> achievements.add("🏆 Master Guardian - Reached Level 20")
            playerProgress.level >= 15 -> achievements.add("🥇 Elite Defender - Reached Level 15")
            playerProgress.level >= 10 -> achievements.add("🥈 Expert Analyst - Reached Level 10")
            playerProgress.level >= 5 -> achievements.add("🥉 Security Scout - Reached Level 5")
        }
        
        // Points achievements
        when {
            playerProgress.totalPoints >= 10000 -> achievements.add("💎 Point Collector - Earned 10,000+ points")
            playerProgress.totalPoints >= 5000 -> achievements.add("💰 High Scorer - Earned 5,000+ points")
            playerProgress.totalPoints >= 1000 -> achievements.add("🎯 Point Hunter - Earned 1,000+ points")
        }
        
        // Streak achievements
        when {
            playerProgress.bestStreak >= 20 -> achievements.add("🔥 Unstoppable - 20+ challenge streak")
            playerProgress.bestStreak >= 10 -> achievements.add("⚡ Streak Master - 10+ challenge streak")
            playerProgress.bestStreak >= 5 -> achievements.add("🌟 On Fire - 5+ challenge streak")
        }
        
        // Challenge completion achievements
        when {
            playerProgress.completedChallenges.size >= 25 -> achievements.add("🎓 Security Expert - Completed 25+ challenges")
            playerProgress.completedChallenges.size >= 15 -> achievements.add("🛡️ Defender - Completed 15+ challenges")
            playerProgress.completedChallenges.size >= 5 -> achievements.add("🔍 Investigator - Completed 5+ challenges")
        }
        
        // Vulnerability type achievements
        val totalVulnTypes = playerProgress.vulnerabilitiesFound.size
        when {
            totalVulnTypes >= 8 -> achievements.add("🧠 Vulnerability Master - Found all vulnerability types")
            totalVulnTypes >= 5 -> achievements.add("🔎 Pattern Recognizer - Found 5+ vulnerability types")
            totalVulnTypes >= 3 -> achievements.add("👁️ Sharp Eye - Found 3+ vulnerability types")
        }
        
        if (achievements.isEmpty()) {
            achievements.add("🌱 Start completing challenges to earn achievements!")
        }
        
        binding.achievementsText.text = achievements.joinToString("\n\n")
    }
    
    private fun hideSystemUi() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
} 