package com.cladiousgame.attackergame

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.cladiousgame.attackergame.data.ChallengeRepository
import com.cladiousgame.attackergame.databinding.ActivitySettingsBinding
import com.cladiousgame.attackergame.managers.AchievementManager

/**
 * Professional settings screen for game customization
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var repository: ChallengeRepository
    private lateinit var achievementManager: AchievementManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = getSharedPreferences("game_settings", MODE_PRIVATE)
        repository = ChallengeRepository(this)
        achievementManager = AchievementManager(this)
        
        setupUI()
        loadSettings()
        setupClickListeners()
    }
    
    private fun setupUI() {
        hideSystemUi()
        
        binding.apply {
            // Setup headers and descriptions
            settingsTitle.text = "⚙️ Game Settings"
            backButton.text = "← Back"
            
            // Version info
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionText.text = "Version ${packageInfo.versionName}"
            
            // Achievement progress
            val progress = achievementManager.getAchievementProgress()
            achievementProgressText.text = "Achievement Progress: ${String.format("%.1f", progress)}%"
            
            // Game stats
            val playerProgress = repository.loadProgress()
            val totalChallenges = repository.getAllChallenges().size
            val completedChallenges = playerProgress.completedChallenges.size
            
            gameStatsText.text = buildString {
                append("📊 Game Statistics\n")
                append("• Challenges Completed: $completedChallenges/$totalChallenges\n")
                append("• Total Points: ${playerProgress.totalPoints}\n")
                append("• Current Level: ${playerProgress.level}\n")
                append("• Best Streak: ${playerProgress.bestStreak}\n")
                append("• Perfect Solves: ${playerProgress.perfectSolves}")
            }
        }
    }
    
    private fun loadSettings() {
        binding.apply {
            // Load saved settings
            soundEffectsSwitch.isChecked = prefs.getBoolean("sound_effects", true)
            vibrationSwitch.isChecked = prefs.getBoolean("vibration", true)
            animationsSwitch.isChecked = prefs.getBoolean("animations", true)
            darkModeSwitch.isChecked = prefs.getBoolean("dark_mode", true)
            autoHintSwitch.isChecked = prefs.getBoolean("auto_hint", false)
            difficultProgressSwitch.isChecked = prefs.getBoolean("difficult_progress", false)
            
            // Difficulty selection
            val difficulty = prefs.getString("preferred_difficulty", "MIXED")
            when (difficulty) {
                "ROOKIE" -> difficultyRookieRadio.isChecked = true
                "HACKER" -> difficultyHackerRadio.isChecked = true
                "EXPERT" -> difficultyExpertRadio.isChecked = true
                "LEGENDARY" -> difficultyLegendaryRadio.isChecked = true
                else -> difficultyMixedRadio.isChecked = true
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Back button
            backButton.setOnClickListener {
                finish()
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
            
            // Sound settings
            soundEffectsSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("sound_effects", isChecked).apply()
            }
            
            vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("vibration", isChecked).apply()
            }
            
            // Visual settings
            animationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("animations", isChecked).apply()
            }
            
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("dark_mode", isChecked).apply()
                // Apply theme change
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES 
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
            
            // Gameplay settings
            autoHintSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("auto_hint", isChecked).apply()
            }
            
            difficultProgressSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("difficult_progress", isChecked).apply()
            }
            
            // Difficulty preferences
            difficultyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val difficulty = when (checkedId) {
                    R.id.difficultyRookieRadio -> "ROOKIE"
                    R.id.difficultyHackerRadio -> "HACKER"
                    R.id.difficultyExpertRadio -> "EXPERT"
                    R.id.difficultyLegendaryRadio -> "LEGENDARY"
                    else -> "MIXED"
                }
                prefs.edit().putString("preferred_difficulty", difficulty).apply()
            }
            
            // Action buttons
            resetProgressButton.setOnClickListener {
                showResetProgressDialog()
            }
            
            viewAchievementsButton.setOnClickListener {
                startActivity(Intent(this@SettingsActivity, AchievementsActivity::class.java))
            }
            
            shareGameButton.setOnClickListener {
                shareGame()
            }
            
            rateAppButton.setOnClickListener {
                rateApp()
            }
            
            aboutButton.setOnClickListener {
                showAboutDialog()
            }
            
            privacyPolicyButton.setOnClickListener {
                showPrivacyPolicy()
            }
            
            exportDataButton.setOnClickListener {
                exportGameData()
            }
            
            importDataButton.setOnClickListener {
                importGameData()
            }
        }
    }
    
    private fun showResetProgressDialog() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("⚠️ Reset Progress")
            .setMessage("Are you sure you want to reset ALL your game progress? This action cannot be undone!\n\n" +
                       "This will delete:\n" +
                       "• All completed challenges\n" +
                       "• Level progress and points\n" +
                       "• Achievements and streaks\n" +
                       "• Game statistics")
            .setPositiveButton("Reset Everything") { _, _ ->
                resetAllProgress()
            }
            .setNegativeButton("Cancel") { dialog, _ -> 
                dialog.dismiss() 
            }
            .show()
    }
    
    private fun resetAllProgress() {
        // Clear all shared preferences
        getSharedPreferences("game_progress", MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences("achievements", MODE_PRIVATE).edit().clear().apply()
        
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("✅ Progress Reset")
            .setMessage("Your game progress has been completely reset. Restart the app to begin fresh!")
            .setPositiveButton("Restart App") { _, _ ->
                // Restart the app
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finishAffinity()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun shareGame() {
        val playerProgress = repository.loadProgress()
        val achievementProgress = achievementManager.getAchievementProgress()
        
        val shareText = buildString {
            append("🎮 I'm mastering Smart Contract Security! 🛡️\n\n")
            append("My Progress:\n")
            append("🏆 Level: ${playerProgress.level}\n")
            append("💎 Points: ${playerProgress.totalPoints}\n")
            append("🔥 Best Streak: ${playerProgress.bestStreak}\n")
            append("⭐ Achievements: ${String.format("%.1f", achievementProgress)}%\n\n")
            append("Join me in learning blockchain security!\n")
            append("#SmartContract #Blockchain #CyberSecurity #GameDev")
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Smart Contract Security Game - My Progress!")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share your progress"))
    }
    
    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            startActivity(intent)
        }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("🎮 About Smart Contract Security Game")
            .setMessage(buildString {
                append("Version: ${packageManager.getPackageInfo(packageName, 0).versionName}\n\n")
                append("🎯 Mission: Learn blockchain security through interactive challenges\n\n")
                append("🛡️ Features:\n")
                append("• 26+ Real-world smart contract challenges\n")
                append("• 4 Difficulty levels with progression system\n")
                append("• Achievement system with 18+ unlockable rewards\n")
                append("• Interactive code-filling gameplay\n")
                append("• Beautiful cyberpunk UI with animations\n\n")
                append("💝 Created with passion for blockchain education\n")
                append("🌟 Help spread security awareness!")
            })
            .setPositiveButton("Cool!") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Share App") { _, _ -> shareGame() }
            .show()
    }
    
    private fun showPrivacyPolicy() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("🔒 Privacy Policy")
            .setMessage(buildString {
                append("Your Privacy Matters\n\n")
                append("📱 Data We Store:\n")
                append("• Game progress (locally on your device)\n")
                append("• Settings preferences\n")
                append("• Achievement unlocks\n\n")
                append("🚫 Data We DON'T Collect:\n")
                append("• Personal information\n")
                append("• Location data\n")
                append("• Device identifiers\n")
                append("• Usage analytics\n\n")
                append("🛡️ All data stays on YOUR device.\n")
                append("🗑️ Uninstalling removes all data.\n\n")
                append("Questions? Contact us through app store.")
            })
            .setPositiveButton("Understood") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun exportGameData() {
        // Export game progress as JSON
        val gameData = buildString {
            append("=== SMART CONTRACT SECURITY GAME DATA ===\n")
            append("Export Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")
            
            val progress = repository.loadProgress()
            append("GAME PROGRESS:\n")
            append("Level: ${progress.level}\n")
            append("Total Points: ${progress.totalPoints}\n")
            append("Completed Challenges: ${progress.completedChallenges.size}\n")
            append("Best Streak: ${progress.bestStreak}\n")
            append("Perfect Solves: ${progress.perfectSolves}\n\n")
            
            val achievements = achievementManager.getUnlockedAchievements()
            append("ACHIEVEMENTS (${achievements.size} unlocked):\n")
            achievements.forEach { achievement ->
                append("• ${achievement.icon} ${achievement.title}\n")
            }
        }
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, gameData)
            putExtra(Intent.EXTRA_SUBJECT, "My Smart Contract Security Game Data")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Export Game Data"))
    }
    
    private fun importGameData() {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("📥 Import Data")
            .setMessage("Import functionality is coming soon!\n\nFor now, you can manually restore progress by:\n\n" +
                       "1. Completing challenges again\n" +
                       "2. Re-unlocking achievements\n" +
                       "3. Building up your level\n\n" +
                       "This keeps the game fair and fun! 🎮")
            .setPositiveButton("Got it!") { dialog, _ -> dialog.dismiss() }
            .show()
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