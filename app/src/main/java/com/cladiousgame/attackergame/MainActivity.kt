package com.cladiousgame.attackergame

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cladiousgame.attackergame.data.ChallengeRepository
import com.cladiousgame.attackergame.data.Difficulty
import com.cladiousgame.attackergame.data.PlayerProgress
import com.cladiousgame.attackergame.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main activity for the Smart Contract Security Game
 * Welcome screen with player progress and level selection
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ChallengeRepository
    private lateinit var playerProgress: PlayerProgress
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = ChallengeRepository(this)
        playerProgress = repository.loadProgress()
        
        setupUI()
        setupClickListeners()
        animateEntry()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh progress when returning from game
        playerProgress = repository.loadProgress()
        updateProgressDisplay()
    }
    
    private fun setupUI() {
        hideSystemUi()
        updateProgressDisplay()
        updateLevelButtons()
    }
    
    private fun updateProgressDisplay() {
        binding.apply {
            playerLevelText.text = "Level ${playerProgress.level}"
            playerPointsText.text = "${playerProgress.totalPoints} pts"
            playerRankText.text = playerProgress.rank
            
            // Animate progress bar
            val progress = (playerProgress.progressToNextLevel * 100).toInt()
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progress).apply {
                duration = 1000
                interpolator = DecelerateInterpolator()
                start()
            }
            
            // Show current points towards next level
            val currentLevelPoints = playerProgress.totalPoints - playerProgress.pointsForCurrentLevel
            val neededForNext = playerProgress.pointsForNextLevel - playerProgress.pointsForCurrentLevel
            progressText.text = "$currentLevelPoints / $neededForNext"
            
            // Streak display
            if (playerProgress.streak > 0) {
                streakContainer.visibility = View.VISIBLE
                streakText.text = "🔥 ${playerProgress.streak} Streak!"
            } else {
                streakContainer.visibility = View.GONE
            }
            
            // Best streak
            bestStreakText.text = "Best: ${playerProgress.bestStreak}"
        }
    }
    
    private fun updateLevelButtons() {
        binding.apply {
            // Rookie always unlocked
            beginnerButton.isEnabled = true
            beginnerButton.alpha = 1.0f
            val rookieChallenges = repository.getChallengesByDifficulty(Difficulty.ROOKIE)
            beginnerButton.text = "${Difficulty.ROOKIE.emoji} ${Difficulty.ROOKIE.displayName} (${rookieChallenges.size} left)"
            
            // Hacker unlocked at level 3
            val hackerUnlocked = playerProgress.level >= 3
            intermediateButton.isEnabled = hackerUnlocked
            intermediateButton.alpha = if (hackerUnlocked) 1.0f else 0.5f
            if (hackerUnlocked) {
                val hackerChallenges = repository.getChallengesByDifficulty(Difficulty.HACKER)
                intermediateButton.text = "${Difficulty.HACKER.emoji} ${Difficulty.HACKER.displayName} (${hackerChallenges.size} left)"
            } else {
                intermediateButton.text = "🔒 Unlocks at Level 3"
            }
            
            // Expert unlocked at level 6
            val expertUnlocked = playerProgress.level >= 6
            advancedButton.isEnabled = expertUnlocked
            advancedButton.alpha = if (expertUnlocked) 1.0f else 0.5f
            if (expertUnlocked) {
                val expertChallenges = repository.getChallengesByDifficulty(Difficulty.EXPERT)
                advancedButton.text = "${Difficulty.EXPERT.emoji} ${Difficulty.EXPERT.displayName} (${expertChallenges.size} left)"
            } else {
                advancedButton.text = "🔒 Unlocks at Level 6"
            }
            
            // Legendary unlocked at level 10
            val legendaryUnlocked = playerProgress.level >= 10
            expertButton.isEnabled = legendaryUnlocked
            expertButton.alpha = if (legendaryUnlocked) 1.0f else 0.5f
            if (legendaryUnlocked) {
                val legendaryChallenges = repository.getChallengesByDifficulty(Difficulty.LEGENDARY)
                expertButton.text = "${Difficulty.LEGENDARY.emoji} ${Difficulty.LEGENDARY.displayName} (${legendaryChallenges.size} left)"
            } else {
                expertButton.text = "🔒 Unlocks at Level 10"
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // CardView click listeners (for the special challenge cards)
            dailyCard.setOnClickListener {
                val dailyChallenge = repository.getDailyChallenge()
                if (dailyChallenge != null) {
                    startChallenge(dailyChallenge.id)
                } else {
                    Toast.makeText(this@MainActivity, "🎉 All challenges completed! You're amazing!", Toast.LENGTH_LONG).show()
                }
            }
            
            battleCard.setOnClickListener {
                val availableChallenges = repository.getAvailableChallenges()
                if (availableChallenges.isNotEmpty()) {
                    startBattleMode()
                } else {
                    Toast.makeText(this@MainActivity, "🎉 All challenges completed! Battle mode unavailable.", Toast.LENGTH_LONG).show()
                }
            }
            
            // Level buttons with filtering
            beginnerButton.setOnClickListener {
                val availableChallenges = repository.getChallengesByDifficulty(Difficulty.ROOKIE)
                if (availableChallenges.isNotEmpty()) {
                    val challenge = availableChallenges.random()
                    startChallenge(challenge.id)
                } else {
                    showNoMoreChallengesDialog(Difficulty.ROOKIE)
                }
            }
            
            intermediateButton.setOnClickListener {
                val availableChallenges = repository.getChallengesByDifficulty(Difficulty.HACKER)
                if (availableChallenges.isNotEmpty()) {
                    val challenge = availableChallenges.random()
                    startChallenge(challenge.id)
                } else {
                    showNoMoreChallengesDialog(Difficulty.HACKER)
                }
            }
            
            advancedButton.setOnClickListener {
                val availableChallenges = repository.getChallengesByDifficulty(Difficulty.EXPERT)
                if (availableChallenges.isNotEmpty()) {
                    val challenge = availableChallenges.random()
                    startChallenge(challenge.id)
                } else {
                    showNoMoreChallengesDialog(Difficulty.EXPERT)
                }
            }
            
            expertButton.setOnClickListener {
                val availableChallenges = repository.getChallengesByDifficulty(Difficulty.LEGENDARY)
                if (availableChallenges.isNotEmpty()) {
                    val challenge = availableChallenges.random()
                    startChallenge(challenge.id)
                } else {
                    showNoMoreChallengesDialog(Difficulty.LEGENDARY)
                }
            }
            
            // Daily challenge button
            dailyButton.setOnClickListener {
                val dailyChallenge = repository.getDailyChallenge()
                if (dailyChallenge != null) {
                    startChallenge(dailyChallenge.id)
                } else {
                    Toast.makeText(this@MainActivity, "🎉 All challenges completed! You're amazing!", Toast.LENGTH_LONG).show()
                }
            }
            
            // Battle mode button
            battleButton.setOnClickListener {
                val availableChallenges = repository.getAvailableChallenges()
                if (availableChallenges.isNotEmpty()) {
                    startBattleMode()
                } else {
                    Toast.makeText(this@MainActivity, "🎉 All challenges completed! Battle mode unavailable.", Toast.LENGTH_LONG).show()
                }
            }
            
            // Statistics and Settings buttons
            statisticsButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, StatisticsActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
            
            settingsButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
            
            // Achievement button
            achievementsButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, AchievementsActivity::class.java))
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
    
    private fun showNoMoreChallengesDialog(difficulty: Difficulty) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("🎉 All ${difficulty.displayName} Challenges Completed!")
            .setMessage("You've mastered all ${difficulty.emoji} ${difficulty.displayName} challenges! Try a different difficulty level or wait for new content.")
            .setPositiveButton("Try Next Level") { _, _ ->
                // Suggest next difficulty
                val nextDifficulty = when (difficulty) {
                    Difficulty.ROOKIE -> Difficulty.HACKER
                    Difficulty.HACKER -> Difficulty.EXPERT
                    Difficulty.EXPERT -> Difficulty.LEGENDARY
                    Difficulty.LEGENDARY -> null
                }
                
                if (nextDifficulty != null) {
                    val nextChallenges = repository.getChallengesByDifficulty(nextDifficulty)
                    if (nextChallenges.isNotEmpty()) {
                        val challenge = nextChallenges.random()
                        startChallenge(challenge.id)
                    } else {
                        Toast.makeText(this, "🎉 All challenges completed! You're a security master!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "🎉 All challenges completed! You're a security master!", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun startChallenge(challengeId: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("challenge_id", challengeId)
        startActivity(intent)
    }
    
    private fun startBattleMode() {
        // Start battle activity with a random available challenge
        val availableChallenges = repository.getAvailableChallenges()
        if (availableChallenges.isNotEmpty()) {
            val challenge = availableChallenges.random()
            val intent = Intent(this, BattleActivity::class.java)
            intent.putExtra("challenge_id", challenge.id)
            startActivity(intent)
        }
    }
    
    private fun animateEntry() {
        // Animate title entry
        binding.gameTitle.apply {
            alpha = 0f
            translationY = -100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        // Animate buttons with stagger
        val buttons = listOf(
            binding.beginnerButton,
            binding.intermediateButton,
            binding.advancedButton,
            binding.expertButton
        )
        
        buttons.forEachIndexed { index, button ->
            button.alpha = 0f
            button.translationX = 300f
            
            lifecycleScope.launch {
                delay(200L * index)
                button.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(600)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
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