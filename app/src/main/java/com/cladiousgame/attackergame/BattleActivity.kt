package com.cladiousgame.attackergame

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cladiousgame.attackergame.data.*
import kotlin.random.Random

/**
 * 🎮⚔️ BATTLE MODE - Real-time competitive security challenges!
 */
class BattleActivity : AppCompatActivity() {
    
    private lateinit var repository: ChallengeRepository
    private lateinit var currentBattle: BattleChallenge
    private lateinit var playerProgress: PlayerProgress
    
    // UI Components
    private lateinit var battleTitle: TextView
    private lateinit var timerText: TextView
    private lateinit var player1Name: TextView
    private lateinit var player2Name: TextView
    private lateinit var player1Score: TextView
    private lateinit var player2Score: TextView
    private lateinit var player1Progress: ProgressBar
    private lateinit var player2Progress: ProgressBar
    private lateinit var codeDisplay: TextView
    private lateinit var gapsContainer: LinearLayout
    private lateinit var submitButton: Button
    private lateinit var powerUpButtons: LinearLayout
    
    // Battle State
    private var currentGapAnswers = mutableMapOf<Int, String>()
    private var isPlayer1 = true // Simulating two players
    private var player1Solutions = mutableMapOf<Int, String>()
    private var player2Solutions = mutableMapOf<Int, String>()
    private var battleTimer: CountDownTimer? = null
    private var timeRemaining = 120000L // 2 minutes
    private var player1Points = 0
    private var player2Points = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)
        
        repository = ChallengeRepository(this)
        playerProgress = repository.loadProgress()
        
        initializeViews()
        setupBattle()
        startBattleTimer()
        
        // Epic entrance animation
        playBattleStartAnimation()
    }
    
    private fun initializeViews() {
        battleTitle = findViewById(R.id.battleTitle)
        timerText = findViewById(R.id.timerText)
        player1Name = findViewById(R.id.player1Name)
        player2Name = findViewById(R.id.player2Name)
        player1Score = findViewById(R.id.player1Score)
        player2Score = findViewById(R.id.player2Score)
        player1Progress = findViewById(R.id.player1Progress)
        player2Progress = findViewById(R.id.player2Progress)
        codeDisplay = findViewById(R.id.codeDisplay)
        gapsContainer = findViewById(R.id.gapsContainer)
        submitButton = findViewById(R.id.submitButton)
        powerUpButtons = findViewById(R.id.powerUpButtons)
        
        submitButton.setOnClickListener { submitBattleSolution() }
    }
    
    private fun setupBattle() {
        val battleType = BattleType.values().random()
        val baseChallenge = repository.getAllChallenges().random()
        
        currentBattle = BattleChallenge(
            baseChallenge = baseChallenge,
            battleType = battleType
        )
        
        battleTitle.text = "⚔️ ${battleType.name.replace("_", " ")} BATTLE!"
        player1Name.text = "🔥 ${getRandomPlayerName()}"
        player2Name.text = "💀 ${getRandomPlayerName()}"
        
        setupBattleCode()
        setupBattleGaps()
        setupBattlePowerUps()
        
        // Initialize progress bars
        val totalGaps = currentBattle.baseChallenge.gaps.size
        player1Progress.max = totalGaps
        player2Progress.max = totalGaps
        player1Progress.progress = 0
        player2Progress.progress = 0
    }
    
    private fun setupBattleCode() {
        var codeWithPlaceholders = currentBattle.baseChallenge.vulnerableCode
        
        currentBattle.baseChallenge.gaps.forEachIndexed { index, gap ->
            val styledBlank = """<font color="#FF0000"><b>[BATTLE GAP ${index + 1}]</b></font>"""
            codeWithPlaceholders = codeWithPlaceholders.replace(gap.placeholder, styledBlank)
        }
        
        // Battle syntax highlighting (more aggressive)
        codeWithPlaceholders = codeWithPlaceholders
            .replace(Regex("\\b(function|contract|modifier)\\b"), "<font color='#FF6B6B'><b><u>$1</u></b></font>")
            .replace(Regex("\\b(require|assert)\\b"), "<font color='#00FF00'><b>$1</b></font>")
            .replace(Regex("\\b(public|private|payable)\\b"), "<font color='#4ECDC4'><b>$1</b></font>")
            
        codeDisplay.text = android.text.Html.fromHtml(codeWithPlaceholders, android.text.Html.FROM_HTML_MODE_LEGACY)
        
        // Battle typewriter effect (faster)
        animateBattleTypewriter()
    }
    
    private fun setupBattleGaps() {
        gapsContainer.removeAllViews()
        
        currentBattle.baseChallenge.gaps.forEachIndexed { index, gap ->
            val gapView = createBattleGapInput(gap, index + 1)
            gapsContainer.addView(gapView)
            
            // Staggered entrance animation
            gapView.alpha = 0f
            gapView.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay((index * 50).toLong())
                .start()
        }
    }
    
    private fun createBattleGapInput(gap: CodeGap, number: Int): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundResource(R.drawable.battle_gap_bg)
        }
        
        // Battle gap title with VS indicator
        val titleView = TextView(this).apply {
            text = "⚔️ Gap $number - ${gap.points} pts - VS MODE!"
            textSize = 14f
            setTextColor(Color.parseColor("#FF0000"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        container.addView(titleView)
        
        // Competitive spinner with battle effects
        when (gap.type) {
            GapType.MULTIPLE_CHOICE -> {
                val spinner = Spinner(this).apply {
                    adapter = ArrayAdapter(
                        this@BattleActivity,
                        android.R.layout.simple_spinner_item,
                        gap.options
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            currentGapAnswers[gap.id] = gap.options[position]
                            playBattleSelectionEffect()
                            simulateOpponentProgress()
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
                container.addView(spinner)
            }
            
            else -> {
                val editText = EditText(this).apply {
                    hint = "⚔️ Battle answer..."
                    setTextColor(Color.WHITE)
                    setHintTextColor(Color.GRAY)
                    setBackgroundResource(R.drawable.battle_edit_text_bg)
                    
                    addTextChangedListener(object : android.text.TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: android.text.Editable?) {
                            currentGapAnswers[gap.id] = s.toString()
                            simulateOpponentProgress()
                        }
                    })
                }
                container.addView(editText)
            }
        }
        
        return container
    }
    
    private fun setupBattlePowerUps() {
        powerUpButtons.removeAllViews()
        
        // Battle-specific power-ups
        val battlePowerUps = listOf(
            "⚡ Speed Boost" to { speedBoost() },
            "🛡️ Defense" to { activateDefense() },
            "💥 Sabotage" to { sabotageOpponent() },
            "🔍 Peek" to { peekOpponentAnswer() }
        )
        
        battlePowerUps.forEach { (name, action) ->
            val powerUpButton = Button(this).apply {
                text = name
                textSize = 12f
                setBackgroundResource(R.drawable.battle_power_up_bg)
                setOnClickListener { 
                    action()
                    playPowerUpEffect(this)
                }
            }
            powerUpButtons.addView(powerUpButton)
        }
    }
    
    private fun startBattleTimer() {
        battleTimer = object : CountDownTimer(timeRemaining, 100) { // Update every 100ms for dramatic effect
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val secs = seconds % 60
                
                timerText.text = "⏰ ${String.format("%02d:%02d", minutes, secs)}"
                
                // Battle timer effects
                when {
                    seconds <= 10 -> {
                        timerText.setTextColor(Color.RED)
                        if (seconds % 2 == 0L) pulseBattleTimer()
                    }
                    seconds <= 30 -> {
                        timerText.setTextColor(Color.YELLOW)
                    }
                    else -> {
                        timerText.setTextColor(Color.WHITE)
                    }
                }
                
                // Simulate opponent activity
                if (Random.nextFloat() < 0.3f) {
                    simulateOpponentProgress()
                }
            }
            
            override fun onFinish() {
                timeRemaining = 0
                timerText.text = "⏰ BATTLE OVER!"
                timerText.setTextColor(Color.RED)
                playBattleEndEffect()
                endBattle()
            }
        }.start()
    }
    
    private fun simulateOpponentProgress() {
        // Simulate opponent solving gaps
        if (Random.nextFloat() < 0.2f) {
            val currentOpponentProgress = player2Progress.progress
            val maxProgress = player2Progress.max
            
            if (currentOpponentProgress < maxProgress) {
                player2Progress.progress = currentOpponentProgress + 1
                player2Points += Random.nextInt(20, 50)
                updateScores()
                
                // Visual effects for opponent progress
                playOpponentProgressEffect()
                
                if (player2Progress.progress >= maxProgress) {
                    // Opponent finished!
                    showOpponentWinning()
                }
            }
        }
    }
    
    private fun submitBattleSolution() {
        var correctAnswers = 0
        var totalPoints = 0
        
        currentBattle.baseChallenge.gaps.forEach { gap ->
            val userAnswer = currentGapAnswers[gap.id] ?: ""
            val isCorrect = repository.isGapAnswerCorrect(currentBattle.baseChallenge.id, gap.id, userAnswer)
            
            if (isCorrect) {
                correctAnswers++
                totalPoints += gap.points
            }
        }
        
        player1Points += totalPoints
        player1Progress.progress = correctAnswers
        updateScores()
        
        // Check for battle end
        if (correctAnswers >= currentBattle.baseChallenge.gaps.size) {
            // Player 1 wins!
            endBattle(winner = "Player 1")
        } else {
            playSubmissionEffect()
        }
    }
    
    private fun endBattle(winner: String? = null) {
        battleTimer?.cancel()
        
        val finalWinner = winner ?: when {
            player1Points > player2Points -> "🔥 You Win!"
            player2Points > player1Points -> "💀 Opponent Wins!"
            else -> "🤝 Draw!"
        }
        
        showBattleResult(finalWinner)
    }
    
    private fun showBattleResult(winner: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_battle_result, null)
        
        val winnerIcon = dialogView.findViewById<TextView>(R.id.winnerIcon)
        val winnerText = dialogView.findViewById<TextView>(R.id.winnerText)
        val finalScoreText = dialogView.findViewById<TextView>(R.id.finalScoreText)
        val battleStatsText = dialogView.findViewById<TextView>(R.id.battleStatsText)
        val rematchButton = dialogView.findViewById<Button>(R.id.rematchButton)
        val exitButton = dialogView.findViewById<Button>(R.id.exitButton)
        
        when {
            winner.contains("You Win") -> {
                winnerIcon.text = "👑"
                winnerText.text = "VICTORY!"
                winnerText.setTextColor(Color.parseColor("#FFD700"))
                playVictoryExplosion()
                
                // Update battle wins
                val newProgress = playerProgress.copy(
                    battleWins = playerProgress.battleWins + 1,
                    totalPoints = playerProgress.totalPoints + 200
                )
                repository.saveProgress(newProgress)
            }
            winner.contains("Opponent") -> {
                winnerIcon.text = "💀"
                winnerText.text = "DEFEAT"
                winnerText.setTextColor(Color.parseColor("#FF0000"))
                playDefeatEffect()
            }
            else -> {
                winnerIcon.text = "🤝"
                winnerText.text = "DRAW"
                winnerText.setTextColor(Color.parseColor("#FFA500"))
            }
        }
        
        finalScoreText.text = "Final Score: $player1Points vs $player2Points"
        
        val stats = buildString {
            append("⚔️ Battle Type: ${currentBattle.battleType.name.replace("_", " ")}\n")
            append("🎯 Your Gaps Solved: ${player1Progress.progress}/${player1Progress.max}\n")
            append("👻 Opponent Gaps: ${player2Progress.progress}/${player2Progress.max}\n")
            append("🏆 Battle Wins: ${playerProgress.battleWins + (if (winner.contains("You Win")) 1 else 0)}")
        }
        battleStatsText.text = stats
        
        rematchButton.setOnClickListener {
            // Start new battle
            recreate()
        }
        
        exitButton.setOnClickListener {
            finish()
        }
        
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.show()
        
        // Epic dialog animation
        dialogView.alpha = 0f
        dialogView.scaleX = 0.5f
        dialogView.scaleY = 0.5f
        dialogView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .start()
    }
    
    private fun updateScores() {
        player1Score.text = "🔥 $player1Points pts"
        player2Score.text = "💀 $player2Points pts"
        
        // Animate score changes
        if (player1Points > player2Points) {
            player1Score.setTextColor(Color.parseColor("#00FF00"))
            player2Score.setTextColor(Color.parseColor("#FF6B6B"))
        } else if (player2Points > player1Points) {
            player1Score.setTextColor(Color.parseColor("#FF6B6B"))
            player2Score.setTextColor(Color.parseColor("#00FF00"))
        } else {
            player1Score.setTextColor(Color.WHITE)
            player2Score.setTextColor(Color.WHITE)
        }
    }
    
    // Battle Power-ups
    private fun speedBoost() {
        Toast.makeText(this, "⚡ SPEED BOOST ACTIVATED!", Toast.LENGTH_SHORT).show()
        // Simulate faster typing/selection
        android.os.Handler(mainLooper).postDelayed({
            if (currentGapAnswers.size < currentBattle.baseChallenge.gaps.size) {
                val randomGap = currentBattle.baseChallenge.gaps.random()
                currentGapAnswers[randomGap.id] = randomGap.correctAnswer
                Toast.makeText(this, "⚡ Auto-solved a gap!", Toast.LENGTH_SHORT).show()
            }
        }, 1000)
    }
    
    private fun activateDefense() {
        Toast.makeText(this, "🛡️ DEFENSE ACTIVATED! Immune to sabotage!", Toast.LENGTH_SHORT).show()
        // Temporary immunity effect
    }
    
    private fun sabotageOpponent() {
        Toast.makeText(this, "💥 SABOTAGE! Opponent slowed down!", Toast.LENGTH_SHORT).show()
        // Slow down opponent progress
        player2Progress.progress = maxOf(0, player2Progress.progress - 1)
        playOpponentSabotageEffect()
    }
    
    private fun peekOpponentAnswer() {
        val opponentProgress = "👻 Opponent solved ${player2Progress.progress}/${player2Progress.max} gaps"
        Toast.makeText(this, "🔍 $opponentProgress", Toast.LENGTH_LONG).show()
    }
    
    // Battle Animation Effects
    private fun playBattleStartAnimation() {
        val views = listOf(battleTitle, player1Name, player2Name, codeDisplay)
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.scaleX = 0.5f
            view.scaleY = 0.5f
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay((index * 100).toLong())
                .start()
        }
    }
    
    private fun animateBattleTypewriter() {
        // Fast typewriter for battle tension
        val text = codeDisplay.text.toString()
        codeDisplay.text = ""
        var index = 0
        val handler = android.os.Handler(mainLooper)
        
        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    codeDisplay.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, 15) // Faster than normal game
                }
            }
        }
        handler.post(runnable)
    }
    
    private fun playBattleSelectionEffect() {
        val flash = ObjectAnimator.ofFloat(gapsContainer, "alpha", 1f, 0.7f, 1f)
        flash.duration = 50
        flash.start()
    }
    
    private fun pulseBattleTimer() {
        val pulse = ObjectAnimator.ofFloat(timerText, "scaleX", 1f, 1.3f, 1f)
        pulse.duration = 100
        pulse.start()
    }
    
    private fun playOpponentProgressEffect() {
        val flash = ObjectAnimator.ofFloat(player2Progress, "alpha", 1f, 0.5f, 1f)
        flash.duration = 200
        flash.start()
        
        // Screen border flash
        findViewById<View>(android.R.id.content).setBackgroundColor(Color.parseColor("#33FF0000"))
        android.os.Handler(mainLooper).postDelayed({
            findViewById<View>(android.R.id.content).setBackgroundColor(Color.TRANSPARENT)
        }, 100)
    }
    
    private fun playOpponentSabotageEffect() {
        val shake = ObjectAnimator.ofFloat(player2Progress, "translationX", 0f, 25f, -25f, 15f, -15f, 0f)
        shake.duration = 300
        shake.start()
    }
    
    private fun playSubmissionEffect() {
        val scale = ObjectAnimator.ofFloat(submitButton, "scaleX", 1f, 1.2f, 1f)
        scale.duration = 200
        scale.start()
    }
    
    private fun playBattleEndEffect() {
        // Dramatic screen flash
        val flash = ObjectAnimator.ofInt(findViewById<View>(android.R.id.content), "backgroundColor", 
            Color.TRANSPARENT, Color.RED, Color.TRANSPARENT)
        flash.duration = 500
        flash.setEvaluator(android.animation.ArgbEvaluator())
        flash.start()
    }
    
    private fun playVictoryExplosion() {
        // Epic victory confetti
        repeat(20) {
            val confetti = TextView(this).apply {
                text = listOf("🎉", "⭐", "💎", "🏆", "👑").random()
                textSize = 30f
                x = Random.nextFloat() * resources.displayMetrics.widthPixels
                y = Random.nextFloat() * resources.displayMetrics.heightPixels
            }
            
            (findViewById<View>(android.R.id.content) as android.view.ViewGroup).addView(confetti)
            
            confetti.animate()
                .translationY(confetti.y + 300)
                .rotation(360f)
                .alpha(0f)
                .setDuration(2000)
                .withEndAction { 
                    (findViewById<View>(android.R.id.content) as android.view.ViewGroup).removeView(confetti) 
                }
                .start()
        }
    }
    
    private fun playDefeatEffect() {
        val shake = ObjectAnimator.ofFloat(findViewById<View>(android.R.id.content), "translationX", 
            0f, 20f, -20f, 15f, -15f, 10f, -10f, 0f)
        shake.duration = 800
        shake.start()
    }
    
    private fun playPowerUpEffect(button: Button) {
        val glow = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.3f, 1f)
        glow.duration = 200
        glow.start()
    }
    
    private fun showOpponentWinning() {
        Toast.makeText(this, "💀 OPPONENT IS WINNING! HURRY UP!", Toast.LENGTH_SHORT).show()
        playOpponentProgressEffect()
    }
    
    private fun getRandomPlayerName(): String {
        val names = listOf(
            "CodeNinja", "HackMaster", "SecurityKing", "CyberWolf", "ByteWarrior",
            "CryptoLord", "BlockchainBeast", "SmartHacker", "TechSamurai", "DigitalKnight"
        )
        return names.random()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        battleTimer?.cancel()
    }
} 