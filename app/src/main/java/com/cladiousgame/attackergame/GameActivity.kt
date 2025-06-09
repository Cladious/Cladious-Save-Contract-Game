package com.cladiousgame.attackergame

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.cladiousgame.attackergame.data.*
import com.cladiousgame.attackergame.databinding.ActivityGameBinding
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced interactive game activity with animations and feedback
 */
class GameActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGameBinding
    private lateinit var repository: ChallengeRepository
    private lateinit var currentChallenge: SmartContractChallenge
    private lateinit var playerProgress: PlayerProgress
    private lateinit var vibrator: Vibrator
    
    // Game State
    private var currentGapAnswers = mutableMapOf<Int, String>()
    private var hintsUsed = 0
    private var startTime = 0L
    private var countdown: CountDownTimer? = null
    private var timeRemaining = 0L
    private var activePowerUps = mutableSetOf<PowerUpType>()
    private var currentGapIndex = 0
    private var correctAnswers = 0
    private var totalGaps = 0
    private var combo = 0
    private var maxCombo = 0
    
    // Animation states
    private var isAnimating = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = ChallengeRepository(this)
        playerProgress = repository.loadProgress()
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        
        val challengeId = intent.getIntExtra("challenge_id", 1)
        currentChallenge = repository.getChallengeById(challengeId) ?: return finish()
        
        initializeViews()
        loadChallenge()
        setupGameUI()
        startTimer()
        
        // Cool entrance animation
        playEntranceAnimation()
    }
    
    private fun initializeViews() {
        binding.hintButton.setOnClickListener { showHint() }
        binding.quitButton.setOnClickListener { finish() }
        binding.submitButton.setOnClickListener { checkAnswers() }
    }
    
    private fun loadChallenge() {
        startTime = System.currentTimeMillis()
        timeRemaining = currentChallenge.timeLimit * 1000L
        totalGaps = currentChallenge.gaps.size
    }
    
    private fun setupGameUI() {
        // Title with emoji
        binding.challengeTitleText.text = currentChallenge.title
        binding.challengeTitleText.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        
        // Description
        binding.challengeDescriptionText.text = currentChallenge.description
        
        // Badges with emojis
        binding.vulnerabilityTypeText.text = "${currentChallenge.vulnerabilityType.emoji} ${currentChallenge.vulnerabilityType.displayName}"
        binding.vulnerabilityTypeText.setBackgroundColor(getVulnerabilityColor(currentChallenge.vulnerabilityType))
        
        binding.difficultyText.text = "${currentChallenge.difficulty.emoji} ${currentChallenge.difficulty.displayName}"
        binding.difficultyText.setBackgroundColor(getDifficultyColor(currentChallenge.difficulty))
        
        // Code display with syntax highlighting
        setupCodeDisplay()
        
        // Player stats
        updatePlayerStats()
        
        // Power-ups
        setupPowerUps()
    }
    
    private fun setupCodeDisplay() {
        var codeWithPlaceholders = currentChallenge.vulnerableCode
        
        // Fix newlines and add proper formatting
        codeWithPlaceholders = codeWithPlaceholders
            .replace("\\n", "\n")
            .trim()
        
        // Add proper indentation for better readability
        val lines = codeWithPlaceholders.split("\n")
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0
        
        lines.forEach { line ->
            val trimmedLine = line.trim()
            
            if (trimmedLine.isEmpty()) {
                formattedLines.add("")
                return@forEach
            }
            
            // Decrease indent for closing braces
            if (trimmedLine.startsWith("}")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            // Add proper indentation
            val indentedLine = "    ".repeat(indentLevel) + trimmedLine
            formattedLines.add(indentedLine)
            
            // Increase indent for opening braces
            if (trimmedLine.endsWith("{")) {
                indentLevel++
            }
        }
        
        codeWithPlaceholders = formattedLines.joinToString("\n")
        
        // Replace gaps with highly visible placeholders
        currentChallenge.gaps.forEachIndexed { index, gap ->
            val gapPlaceholder = "\n\n    🔥🔥🔥 GAP ${index + 1} - CLICK BUTTON BELOW TO FILL 🔥🔥🔥\n\n"
            codeWithPlaceholders = codeWithPlaceholders.replace(gap.placeholder, gapPlaceholder)
        }
        
        // Simple text display without complex HTML
        binding.smartContractCodeText.text = codeWithPlaceholders
        
        // Set monospace font and better styling
        binding.smartContractCodeText.typeface = android.graphics.Typeface.MONOSPACE
        binding.smartContractCodeText.textSize = 14f
        binding.smartContractCodeText.setLineSpacing(6f, 1.3f) // Better line spacing
        binding.smartContractCodeText.setTextColor(android.graphics.Color.parseColor("#00FF00")) // Green text
        
        // Create gap buttons below the code
        setupGapButtons()
    }
    
    private fun setupGapButtons() {
        binding.gapsContainer.removeAllViews()
        
        currentChallenge.gaps.forEachIndexed { index, gap ->
            val gapCard = createGapButton(gap, index + 1)
            binding.gapsContainer.addView(gapCard)
            
            // Animate gap appearance
            gapCard.alpha = 0f
            gapCard.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }
    
    private fun createGapButton(gap: CodeGap, gapNumber: Int): View {
        val cardView = androidx.cardview.widget.CardView(this).apply {
            radius = 12f
            cardElevation = 8f
            setCardBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"))
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setLayoutParams(layoutParams)
        }
        
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Gap title
        val titleView = TextView(this).apply {
            text = "🔍 Gap $gapNumber in Code (${gap.points} points)"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#00FF00"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
        }
        container.addView(titleView)
        
        // Current status
        val statusView = TextView(this).apply {
            text = if (currentGapAnswers.containsKey(gap.id)) 
                "✅ Selected: ${currentGapAnswers[gap.id]}" 
            else "❌ Not filled yet"
            textSize = 14f
            setTextColor(
                if (currentGapAnswers.containsKey(gap.id)) 
                    android.graphics.Color.parseColor("#00FF00")
                else android.graphics.Color.parseColor("#FF6B6B")
            )
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        container.addView(statusView)
        
        // Big clickable button
        val gapButton = Button(this).apply {
            text = "🔥 FILL GAP $gapNumber 🔥"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundResource(
                if (currentGapAnswers.containsKey(gap.id))
                    R.drawable.submit_button_bg
                else R.drawable.hint_button_bg
            )
            setPadding(32, 16, 32, 16)
            
            setOnClickListener {
                showGapOptions(gap, gapNumber)
            }
        }
        container.addView(gapButton)
        
        // Hint button
        val hintButton = Button(this).apply {
            text = "💡 Hint for Gap $gapNumber"
            textSize = 12f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundResource(R.drawable.button_secondary_bg)
            setPadding(16, 8, 16, 8)
            
            setOnClickListener {
                showGapHint(gap)
            }
        }
        
        val hintLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8, 0, 0)
        }
        hintButton.layoutParams = hintLayoutParams
        container.addView(hintButton)
        
        cardView.addView(container)
        return cardView
    }
    
    private fun updateGapInCodeDisplay(gap: CodeGap, gapNumber: Int, selectedAnswer: String) {
        // Update the code display
        val currentText = binding.smartContractCodeText.text.toString()
        val gapPlaceholder = "\n\n    🔥🔥🔥 GAP $gapNumber - CLICK BUTTON BELOW TO FILL 🔥🔥🔥\n\n"
        val styledAnswer = "\n\n    ✅ GAP $gapNumber: $selectedAnswer ✅\n\n"
        
        val updatedText = currentText.replace(gapPlaceholder, styledAnswer)
        binding.smartContractCodeText.text = updatedText
        
        // Update the gap buttons
        setupGapButtons()
        
        // Check if all gaps are filled
        checkAllGapsFilled()
    }
    
    private fun checkAllGapsFilled() {
        val allFilled = currentChallenge.gaps.all { gap ->
            currentGapAnswers.containsKey(gap.id) && currentGapAnswers[gap.id]?.isNotEmpty() == true
        }
        
        if (allFilled) {
            binding.submitButton.text = "🎯 SUBMIT ANSWERS"
            binding.submitButton.setBackgroundResource(R.drawable.submit_button_bg)
            
            // Pulse submit button to draw attention
            pulseView(binding.submitButton)
            
            Toast.makeText(this, "🎉 All gaps filled! Ready to submit!", Toast.LENGTH_SHORT).show()
        } else {
            binding.submitButton.text = "❌ Fill all gaps first"
            binding.submitButton.setBackgroundResource(R.drawable.button_secondary_bg)
        }
    }
    
    private fun setupPowerUps() {
        binding.powerUpContainer.removeAllViews()
        
        playerProgress.powerUps.forEach { (powerUpType, count) ->
            if (count > 0) {
                val powerUpButton = Button(this).apply {
                    text = "${powerUpType.emoji} ${powerUpType.displayName} ($count)"
                    setBackgroundResource(R.drawable.power_up_button_bg)
                    textSize = 12f
                    setOnClickListener { activatePowerUp(powerUpType) }
                }
                binding.powerUpContainer.addView(powerUpButton)
            }
        }
    }
    
    private fun startTimer() {
        countdown = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val secs = seconds % 60
                
                binding.timerText.text = "⏰ ${String.format("%02d:%02d", minutes, secs)}"
                
                // Warning effects
                when {
                    seconds <= 10 -> {
                        binding.timerText.setTextColor(Color.RED)
                        if (seconds % 2 == 0L) pulseView(binding.timerText)
                    }
                    seconds <= 30 -> binding.timerText.setTextColor(Color.YELLOW)
                    else -> binding.timerText.setTextColor(Color.WHITE)
                }
            }
            
            override fun onFinish() {
                timeRemaining = 0
                binding.timerText.text = "⏰ Time's Up!"
                binding.timerText.setTextColor(Color.RED)
                playTimeUpEffect()
                checkAnswers()
            }
        }.start()
    }
    
    private fun checkAnswers() {
        // Check if all gaps are filled
        val allFilled = currentChallenge.gaps.all { gap ->
            currentGapAnswers.containsKey(gap.id) && currentGapAnswers[gap.id]?.isNotEmpty() == true
        }
        
        if (!allFilled) {
            Toast.makeText(this, "❌ Please fill all gaps before submitting!", Toast.LENGTH_SHORT).show()
            return
        }
        
        countdown?.cancel()
        
        var correctAnswers = 0
        var totalPoints = 0
        var gapResults = mutableListOf<Pair<CodeGap, Boolean>>()
        
        currentChallenge.gaps.forEach { gap ->
            val userAnswer = currentGapAnswers[gap.id] ?: ""
            val isCorrect = repository.isGapAnswerCorrect(currentChallenge.id, gap.id, userAnswer)
            
            gapResults.add(gap to isCorrect)
            
            if (isCorrect) {
                correctAnswers++
                totalPoints += gap.points
            }
        }
        
        // Calculate final score with bonuses
        var finalScore = totalPoints
        
        // Speed bonus
        val timeBonus = if (timeRemaining > currentChallenge.timeLimit * 500) {
            (totalPoints * 0.5).toInt()
        } else if (timeRemaining > currentChallenge.timeLimit * 250) {
            (totalPoints * 0.25).toInt()
        } else 0
        
        // Hint penalty
        val hintPenalty = hintsUsed * 10
        
        // Power-up bonus
        val powerUpBonus = if (activePowerUps.contains(PowerUpType.DOUBLE_POINTS)) totalPoints else 0
        
        finalScore = finalScore + timeBonus - hintPenalty + powerUpBonus
        
        // Update progress
        updatePlayerProgress(correctAnswers == currentChallenge.gaps.size, finalScore)
        
        // Show results with awesome effects
        showResults(gapResults, finalScore, timeBonus, hintPenalty)
    }
    
    private fun updatePlayerProgress(perfect: Boolean, points: Int) {
        val newProgress = playerProgress.copy(
            totalPoints = playerProgress.totalPoints + points,
            completedChallenges = playerProgress.completedChallenges + currentChallenge.id,
            streak = if (perfect) playerProgress.streak + 1 else 0,
            bestStreak = maxOf(playerProgress.bestStreak, if (perfect) playerProgress.streak + 1 else 0),
            perfectSolves = if (perfect && hintsUsed == 0) playerProgress.perfectSolves + 1 else playerProgress.perfectSolves,
            speedRecords = if (perfect) {
                val timeSpent = currentChallenge.timeLimit * 1000L - timeRemaining
                playerProgress.speedRecords + (currentChallenge.id to timeSpent)
            } else playerProgress.speedRecords
        )
        
        repository.saveProgress(newProgress)
        playerProgress = newProgress
    }
    
    private fun showResults(
        gapResults: List<Pair<CodeGap, Boolean>>, 
        finalScore: Int, 
        timeBonus: Int, 
        hintPenalty: Int
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_result, null)
        
        val resultIcon = dialogView.findViewById<TextView>(R.id.resultIcon)
        val resultTitle = dialogView.findViewById<TextView>(R.id.resultTitle)
        val scoreText = dialogView.findViewById<TextView>(R.id.scoreText)
        val detailsText = dialogView.findViewById<TextView>(R.id.detailsText)
        val explanationText = dialogView.findViewById<TextView>(R.id.explanationText)
        val nextButton = dialogView.findViewById<Button>(R.id.nextButton)
        val retryButton = dialogView.findViewById<Button>(R.id.retryButton)
        
        val correctCount = gapResults.count { it.second }
        val totalGaps = gapResults.size
        
        // Set result based on performance
        when {
            correctCount == totalGaps -> {
                resultIcon.text = if (hintsUsed == 0) "👑" else "🏆"
                resultTitle.text = if (hintsUsed == 0) "PERFECT!" else "EXCELLENT!"
                resultTitle.setTextColor(Color.parseColor("#FFD700"))
                playVictoryEffect()
            }
            correctCount >= totalGaps * 0.7 -> {
                resultIcon.text = "⭐"
                resultTitle.text = "GREAT JOB!"
                resultTitle.setTextColor(Color.parseColor("#00FF00"))
                playSuccessEffect()
            }
            correctCount >= totalGaps * 0.5 -> {
                resultIcon.text = "👍"
                resultTitle.text = "NOT BAD!"
                resultTitle.setTextColor(Color.parseColor("#FFA500"))
            }
            else -> {
                resultIcon.text = "😅"
                resultTitle.text = "TRY AGAIN!"
                resultTitle.setTextColor(Color.parseColor("#FF6B6B"))
                playFailureEffect()
            }
        }
        
        scoreText.text = "🎯 Score: $finalScore points"
        
        val details = buildString {
            append("✅ Correct: $correctCount/$totalGaps\n")
            if (timeBonus > 0) append("⚡ Speed Bonus: +$timeBonus\n")
            if (hintPenalty > 0) append("💡 Hint Penalty: -$hintPenalty\n")
            append("🔥 Current Streak: ${playerProgress.streak}")
        }
        detailsText.text = details
        
        explanationText.text = currentChallenge.explanation
        
        nextButton.setOnClickListener {
            // Go to next challenge or main menu
            finish()
        }
        
        retryButton.setOnClickListener {
            // Restart this challenge
            recreate()
        }
        
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.show()
        
        // Animate dialog appearance
        dialogView.alpha = 0f
        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        dialogView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(BounceInterpolator())
            .start()
    }
    
    private fun showHint() {
        if (hintsUsed >= 3) {
            Toast.makeText(this, "💡 No more hints available!", Toast.LENGTH_SHORT).show()
            return
        }
        
        hintsUsed++
        val hint = when (hintsUsed) {
            1 -> currentChallenge.vulnerabilityType.description
            2 -> "🎯 Look for the ${currentChallenge.vulnerabilityType.displayName} vulnerability pattern!"
            else -> "🔍 Think about: ${currentChallenge.explanation}"
        }
        
        showHintDialog(hint)
        updatePlayerStats()
    }
    
    private fun showGapHint(gap: CodeGap) {
        if (gap.hint.isNotEmpty()) {
            showHintDialog("💡 Gap Hint: ${gap.hint}")
            hintsUsed++
            updatePlayerStats()
        } else {
            showHintDialog("💡 General Hint: ${currentChallenge.vulnerabilityType.description}")
            hintsUsed++
            updatePlayerStats()
        }
    }
    
    private fun showHintDialog(hint: String) {
        AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setTitle("💡 Hint")
            .setMessage(hint)
            .setPositiveButton("Got it!") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun activatePowerUp(powerUpType: PowerUpType) {
        when (powerUpType) {
            PowerUpType.TIME_FREEZE -> {
                countdown?.cancel()
                Toast.makeText(this, "❄️ Time frozen for 30 seconds!", Toast.LENGTH_SHORT).show()
                playPowerUpEffect()
                
                // Resume timer after 30 seconds
                android.os.Handler(mainLooper).postDelayed({
                    startTimer()
                }, 30000)
            }
            
            PowerUpType.HINT_REVEALER -> {
                showHint()
                Toast.makeText(this, "💡 Free hint revealed!", Toast.LENGTH_SHORT).show()
                hintsUsed-- // Don't count this hint
                playPowerUpEffect()
            }
            
            PowerUpType.DOUBLE_POINTS -> {
                activePowerUps.add(PowerUpType.DOUBLE_POINTS)
                Toast.makeText(this, "💎 Next challenge gives double points!", Toast.LENGTH_SHORT).show()
                playPowerUpEffect()
            }
            
            PowerUpType.CODE_HIGHLIGHTER -> {
                highlightVulnerableLines()
                Toast.makeText(this, "🔍 Vulnerable lines highlighted!", Toast.LENGTH_SHORT).show()
                playPowerUpEffect()
            }
            
            PowerUpType.LUCKY_GUESS -> {
                if (Random.nextBoolean()) {
                    // Auto-solve a random gap
                    val unsolved = currentChallenge.gaps.filter { gap ->
                        currentGapAnswers[gap.id].isNullOrEmpty()
                    }
                    if (unsolved.isNotEmpty()) {
                        val randomGap = unsolved.random()
                        currentGapAnswers[randomGap.id] = randomGap.correctAnswer
                        Toast.makeText(this, "🍀 Lucky! Gap solved automatically!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "🍀 Not so lucky this time!", Toast.LENGTH_SHORT).show()
                }
                playPowerUpEffect()
            }
            
            else -> {
                Toast.makeText(this, "${powerUpType.emoji} Power-up activated!", Toast.LENGTH_SHORT).show()
                playPowerUpEffect()
            }
        }
        
        // Use the power-up
        playerProgress = repository.usePowerUp(powerUpType, playerProgress)
        repository.saveProgress(playerProgress)
        setupPowerUps() // Refresh power-up display
    }
    
    private fun updatePlayerStats() {
        binding.pointsText.text = "💎 ${playerProgress.totalPoints} pts"
        binding.streakText.text = "🔥 ${playerProgress.streak} streak"
        binding.hintButton.text = "💡 Hints (${3 - hintsUsed} left)"
    }
    
    // Animation Effects
    private fun playEntranceAnimation() {
        val views = listOf(binding.challengeTitleText, binding.challengeDescriptionText, binding.vulnerabilityTypeText, binding.difficultyText)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 100f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }
    
    private fun playSelectionEffect() {
        // Quick flash effect
        val flash = ObjectAnimator.ofFloat(findViewById<View>(android.R.id.content), "alpha", 1f, 0.8f, 1f)
        flash.duration = 100
        flash.start()
    }
    
    private fun playVictoryEffect() {
        // Confetti-like effect
        repeat(10) {
            val star = TextView(this).apply {
                text = "⭐"
                textSize = 24f
                x = Random.nextFloat() * resources.displayMetrics.widthPixels
                y = Random.nextFloat() * resources.displayMetrics.heightPixels
            }
            
            (findViewById<View>(android.R.id.content) as android.view.ViewGroup).addView(star)
            
            star.animate()
                .translationY(star.y + 200)
                .alpha(0f)
                .setDuration(1000)
                .withEndAction { 
                    (findViewById<View>(android.R.id.content) as android.view.ViewGroup).removeView(star) 
                }
                .start()
        }
    }
    
    private fun playSuccessEffect() {
        pulseView(findViewById(android.R.id.content))
    }
    
    private fun playFailureEffect() {
        shakeView(findViewById(android.R.id.content))
    }
    
    private fun playTimeUpEffect() {
        val flash = ObjectAnimator.ofInt(findViewById<View>(android.R.id.content), "backgroundColor", 
            Color.TRANSPARENT, Color.RED, Color.TRANSPARENT)
        flash.duration = 300
        flash.setEvaluator(android.animation.ArgbEvaluator())
        flash.start()
    }
    
    private fun playPowerUpEffect() {
        val scale = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.powerUpContainer, "scaleX", 1f, 1.2f, 1f),
                ObjectAnimator.ofFloat(binding.powerUpContainer, "scaleY", 1f, 1.2f, 1f)
            )
            duration = 300
        }
        scale.start()
    }
    
    private fun pulseView(view: View) {
        val pulse = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f)
        pulse.duration = 200
        pulse.start()
    }
    
    private fun shakeView(view: View) {
        val shake = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shake.duration = 500
        shake.start()
    }
    
    private fun animateTypewriter(textView: TextView, text: String) {
        textView.text = ""
        var index = 0
        val handler = android.os.Handler(mainLooper)
        
        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    textView.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, 30)
                }
            }
        }
        handler.post(runnable)
    }
    
    private fun highlightVulnerableLines() {
        // Add special highlighting to code display
        val highlightedCode = currentChallenge.vulnerableCode
            .replace("public", "<font color='#FF0000'><b>public</b></font>")
            .replace("call{value:", "<font color='#FF0000'><b>call{value:</b></font>")
        
        binding.smartContractCodeText.text = android.text.Html.fromHtml(syntaxHighlight(highlightedCode), android.text.Html.FROM_HTML_MODE_LEGACY)
    }
    
    // Color helpers
    private fun getVulnerabilityColor(type: VulnerabilityType): Int {
        return when (type) {
            VulnerabilityType.ACCESS_CONTROL -> Color.parseColor("#FF6B6B")
            VulnerabilityType.REENTRANCY -> Color.parseColor("#4ECDC4")
            VulnerabilityType.INTEGER_OVERFLOW -> Color.parseColor("#45B7D1")
            VulnerabilityType.UNCHECKED_CALL -> Color.parseColor("#F39C12")
            VulnerabilityType.RANDOMNESS -> Color.parseColor("#9B59B6")
            VulnerabilityType.TIMESTAMP_DEPENDENCE -> Color.parseColor("#1ABC9C")
            VulnerabilityType.DENIAL_OF_SERVICE -> Color.parseColor("#E74C3C")
            VulnerabilityType.FRONT_RUNNING -> Color.parseColor("#F1C40F")
        }
    }
    
    private fun getDifficultyColor(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.ROOKIE -> Color.parseColor("#2ECC71")
            Difficulty.HACKER -> Color.parseColor("#F39C12")
            Difficulty.EXPERT -> Color.parseColor("#E74C3C")
            Difficulty.LEGENDARY -> Color.parseColor("#9B59B6")
        }
    }
    
    private fun syntaxHighlight(code: String): String {
        // Return plain text to avoid HTML conflicts
        return code
    }
    
    private fun showGapOptions(gap: CodeGap, gapNumber: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_gap_selection, null)
        
        val gapTitle = dialogView.findViewById<TextView>(R.id.gapTitle)
        val gapDescription = dialogView.findViewById<TextView>(R.id.gapDescription)
        val optionsContainer = dialogView.findViewById<LinearLayout>(R.id.optionsContainer)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val hintButton = dialogView.findViewById<Button>(R.id.gapHintButton)
        
        var selectedOption = ""
        
        gapTitle.text = "🔍 Gap $gapNumber (${gap.points} points)"
        gapDescription.text = "Choose the correct option to fill this gap:"
        
        // Create option buttons
        gap.options.forEach { option ->
            val optionButton = Button(this).apply {
                text = option
                textSize = 14f
                setBackgroundResource(R.drawable.button_secondary_bg)
                setTextColor(android.graphics.Color.WHITE)
                setPadding(24, 16, 24, 16)
                
                setOnClickListener {
                    selectedOption = option
                    // Update all option buttons appearance
                    optionsContainer.children.forEach { child ->
                        if (child is Button) {
                            child.setBackgroundResource(R.drawable.button_secondary_bg)
                            child.setTextColor(android.graphics.Color.WHITE)
                        }
                    }
                    // Highlight selected option
                    setBackgroundResource(R.drawable.submit_button_bg)
                    setTextColor(android.graphics.Color.WHITE)
                    playSelectionEffect()
                }
            }
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            optionButton.layoutParams = layoutParams
            optionsContainer.addView(optionButton)
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        confirmButton.setOnClickListener {
            if (selectedOption.isNotEmpty()) {
                currentGapAnswers[gap.id] = selectedOption
                updateGapInCodeDisplay(gap, gapNumber, selectedOption)
                dialog.dismiss()
                
                Toast.makeText(this, "✅ Gap $gapNumber filled: $selectedOption", Toast.LENGTH_SHORT).show()
                playSelectionEffect()
            } else {
                Toast.makeText(this, "Please select an option first!", Toast.LENGTH_SHORT).show()
            }
        }
        
        hintButton.setOnClickListener {
            showGapHint(gap)
        }
        
        dialog.show()
        
        // Animate dialog appearance
        dialogView.alpha = 0f
        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        dialogView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(BounceInterpolator())
            .start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countdown?.cancel()
    }
} 