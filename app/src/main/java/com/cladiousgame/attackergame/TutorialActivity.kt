package com.cladiousgame.attackergame

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cladiousgame.attackergame.databinding.ActivityTutorialBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tutorial activity introducing new players to smart contract security concepts
 */
class TutorialActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTutorialBinding
    private var currentStep = 0
    
    private val tutorialSteps = listOf(
        TutorialStep(
            title = "Welcome to Cladious Contract Game! 🛡️",
            description = "You are a smart contract security expert. Your mission is to find and fix vulnerabilities in smart contracts to protect the DeFi ecosystem!",
            codeExample = null,
            tips = listOf("Each challenge tests different vulnerability types", "You earn points for correct solutions", "Use hints when you're stuck!")
        ),
        TutorialStep(
            title = "Understanding Smart Contracts 📋",
            description = "Smart contracts are self-executing contracts with terms directly written into code. However, bugs in these contracts can lead to millions of dollars in losses!",
            codeExample = """
                contract SimpleVault {
                    mapping(address => uint256) public balances;
                    
                    function deposit() public payable {
                        balances[msg.sender] += msg.value;
                    }
                    
                    function withdraw(uint256 amount) public {
                        require(balances[msg.sender] >= amount);
                        balances[msg.sender] -= amount;
                        payable(msg.sender).transfer(amount);
                    }
                }
            """.trimIndent(),
            tips = listOf("This looks safe, but can you spot the vulnerability?", "Think about what happens if someone calls this function multiple times", "The order of operations matters!")
        ),
        TutorialStep(
            title = "Common Vulnerabilities 🔍",
            description = "There are several types of vulnerabilities you'll encounter:",
            codeExample = null,
            tips = listOf(
                "🔓 Access Control: Missing permission checks",
                "🔄 Reentrancy: Functions that can be called recursively",
                "📈 Integer Overflow: Numbers exceeding their limits",
                "❌ Unchecked Calls: External calls without error handling",
                "🎲 Weak Randomness: Predictable random number generation",
                "⏰ Timestamp Dependence: Relying on block.timestamp",
                "🚫 Denial of Service: Code that can be exploited to freeze contracts",
                "🏃 Front Running: Transactions vulnerable to miners"
            )
        ),
        TutorialStep(
            title = "How to Play 🎮",
            description = "For each challenge, you'll see vulnerable code and need to write a secure version:",
            codeExample = """
                // VULNERABLE CODE:
                function emergencyWithdraw() public {
                    payable(msg.sender).transfer(address(this).balance);
                }
                
                // YOUR SECURE SOLUTION:
                address public owner;
                
                modifier onlyOwner() {
                    require(msg.sender == owner, "Only owner allowed");
                    _;
                }
                
                function emergencyWithdraw() public onlyOwner {
                    payable(msg.sender).transfer(address(this).balance);
                }
            """.trimIndent(),
            tips = listOf("Read the vulnerability description carefully", "Think about who should be able to call each function", "Test your understanding with the provided hints")
        ),
        TutorialStep(
            title = "Scoring System 🏆",
            description = "Earn points and level up as you secure more contracts:",
            codeExample = null,
            tips = listOf(
                "💯 Base points per challenge completion",
                "⚡ Speed bonus for fast solutions (under 1-2 minutes)",
                "💡 Hint penalty (-10 points per hint used)",
                "🔥 Streaks multiply your learning momentum",
                "🏅 Unlock achievements for milestones",
                "🔓 Higher difficulty levels unlock as you progress"
            )
        ),
        TutorialStep(
            title = "Ready to Begin! 🚀",
            description = "You now have the knowledge to start protecting the blockchain! Remember: every vulnerability you fix makes DeFi safer for everyone.",
            codeExample = null,
            tips = listOf("Start with Beginner challenges to build confidence", "Don't hesitate to use hints - learning is the goal!", "Check your statistics to track progress", "Have fun and happy hacking! 🎉")
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        displayStep(0)
    }
    
    private fun setupUI() {
        hideSystemUi()
        
        binding.apply {
            backButton.setOnClickListener { finish() }
            nextButton.setOnClickListener { nextStep() }
            prevButton.setOnClickListener { previousStep() }
            skipButton.setOnClickListener { finish() }
        }
    }
    
    private fun displayStep(stepIndex: Int) {
        if (stepIndex >= tutorialSteps.size) {
            finish()
            return
        }
        
        currentStep = stepIndex
        val step = tutorialSteps[stepIndex]
        
        binding.apply {
            stepIndicator.text = "${stepIndex + 1} / ${tutorialSteps.size}"
            stepTitle.text = step.title
            stepDescription.text = step.description
            
            // Handle code example
            if (step.codeExample != null) {
                codeExampleCard.visibility = View.VISIBLE
                codeExampleText.text = step.codeExample
            } else {
                codeExampleCard.visibility = View.GONE
            }
            
            // Handle tips
            val tipsText = step.tips.joinToString("\n\n")
            binding.tipsText.text = tipsText
            
            // Update navigation buttons
            prevButton.isEnabled = stepIndex > 0
            prevButton.alpha = if (stepIndex > 0) 1.0f else 0.5f
            
            if (stepIndex == tutorialSteps.size - 1) {
                nextButton.text = "🚀 Start Playing!"
            } else {
                nextButton.text = "Next →"
            }
            
            // Animate content entry
            animateStepEntry()
        }
    }
    
    private fun nextStep() {
        if (currentStep >= tutorialSteps.size - 1) {
            finish()
        } else {
            displayStep(currentStep + 1)
        }
    }
    
    private fun previousStep() {
        if (currentStep > 0) {
            displayStep(currentStep - 1)
        }
    }
    
    private fun animateStepEntry() {
        val views = listOf(
            binding.stepTitle,
            binding.stepDescription,
            binding.codeExampleCard,
            binding.tipsCard
        )
        
        views.forEachIndexed { index, view ->
            if (view.visibility == View.VISIBLE) {
                view.alpha = 0f
                view.translationY = 50f
                
                lifecycleScope.launch {
                    delay(100L * index)
                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .start()
                }
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
    
    data class TutorialStep(
        val title: String,
        val description: String,
        val codeExample: String?,
        val tips: List<String>
    )
} 