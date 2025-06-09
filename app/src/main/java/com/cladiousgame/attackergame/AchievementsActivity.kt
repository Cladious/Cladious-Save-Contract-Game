package com.cladiousgame.attackergame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Button
import com.cladiousgame.attackergame.data.*
import com.cladiousgame.attackergame.managers.AchievementManager

/**
 * Activity to display user achievements
 */
class AchievementsActivity : AppCompatActivity() {
    
    private lateinit var achievementManager: AchievementManager
    private lateinit var repository: ChallengeRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // For now, just show a simple message and close
        // In a full implementation, this would have its own layout
        val textView = TextView(this).apply {
            text = "🏆 Achievements Coming Soon!"
            textSize = 24f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(64, 64, 64, 64)
        }
        
        setContentView(textView)
        
        // Initialize managers
        repository = ChallengeRepository(this)
        achievementManager = AchievementManager(this)
        
        // Auto close after 2 seconds
        textView.postDelayed({
            finish()
        }, 2000)
    }
} 