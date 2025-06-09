package com.cladiousgame.attackergame.data

/**
 * New fill-in-the-blanks smart contract security challenge
 */
data class SmartContractChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val vulnerableCode: String,
    val vulnerabilityType: VulnerabilityType,
    val difficulty: Difficulty,
    val pointsReward: Int,
    val explanation: String,
    val gaps: List<CodeGap>, // New: fill-in-the-blank gaps
    val timeLimit: Int = 60, // New: time pressure!
    val specialEffects: List<String> = emptyList() // New: fun effects
)

/**
 * Represents a gap that needs to be filled in the code
 */
data class CodeGap(
    val id: Int,
    val placeholder: String, // e.g., "____SECURITY_CHECK____"
    val correctAnswer: String,
    val options: List<String>, // Multiple choice options
    val type: GapType,
    val hint: String = "",
    val points: Int = 10
)

/**
 * Types of code gaps for different interaction methods
 */
enum class GapType {
    MULTIPLE_CHOICE,    // Select from dropdown
    DRAG_DROP,          // Drag code blocks
    TEXT_INPUT,         // Type short answer
    SLIDER,             // Adjust values
    TOGGLE             // Boolean switches
}

/**
 * Types of smart contract vulnerabilities with fun names
 */
enum class VulnerabilityType(val displayName: String, val emoji: String, val description: String) {
    ACCESS_CONTROL("Access Control", "🔓", "Missing permission checks - anyone can call!"),
    REENTRANCY("Reentrancy Attack", "🔄", "Function calls itself before finishing!"),
    INTEGER_OVERFLOW("Integer Overflow", "📈", "Numbers getting too big and breaking!"),
    UNCHECKED_CALL("Unchecked External Calls", "❌", "Calls failing silently!"),
    TIMESTAMP_DEPENDENCE("Timestamp Dependence", "⏰", "Relying on predictable time!"),
    DENIAL_OF_SERVICE("Denial of Service", "🚫", "Code that can freeze contracts!"),
    FRONT_RUNNING("Front Running", "🏃", "Transactions being intercepted!"),
    RANDOMNESS("Weak Randomness", "🎲", "Predictable random numbers!")
}

/**
 * Challenge difficulty levels with progression rewards
 */
enum class Difficulty(val displayName: String, val multiplier: Double, val emoji: String) {
    ROOKIE("Rookie", 1.0, "🌱"),
    HACKER("Hacker", 1.5, "⚡"),
    EXPERT("Expert", 2.0, "🔥"),
    LEGENDARY("Legendary", 3.0, "💀")
}

/**
 * Player progress with new gaming features
 */
data class PlayerProgress(
    val totalPoints: Int = 0,
    val completedChallenges: Set<Int> = emptySet(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val vulnerabilitiesFound: Map<VulnerabilityType, Int> = emptyMap(),
    val powerUps: Map<PowerUpType, Int> = emptyMap(), // New: power-ups!
    val achievements: Set<String> = emptySet(), // New: achievement tracking
    val dailyChallengeStreak: Int = 0, // New: daily challenges
    val battleWins: Int = 0, // New: battle mode wins
    val perfectSolves: Int = 0, // New: perfect solutions (no hints)
    val speedRecords: Map<Int, Long> = emptyMap() // New: speed records per challenge
) {
    // Calculate level based on total points
    val level: Int
        get() = when {
            totalPoints < 500 -> 1      // Level 1: 0-499 points
            totalPoints < 1200 -> 2     // Level 2: 500-1199 points
            totalPoints < 2000 -> 3     // Level 3: 1200-1999 points
            totalPoints < 3000 -> 4     // Level 4: 2000-2999 points
            totalPoints < 4500 -> 5     // Level 5: 3000-4499 points
            totalPoints < 6500 -> 6     // Level 6: 4500-6499 points
            totalPoints < 9000 -> 7     // Level 7: 6500-8999 points
            totalPoints < 12000 -> 8    // Level 8: 9000-11999 points
            totalPoints < 16000 -> 9    // Level 9: 12000-15999 points
            totalPoints < 21000 -> 10   // Level 10: 16000-20999 points
            totalPoints < 27000 -> 11   // Level 11: 21000-26999 points
            totalPoints < 34000 -> 12   // Level 12: 27000-33999 points
            totalPoints < 42000 -> 13   // Level 13: 34000-41999 points
            totalPoints < 51000 -> 14   // Level 14: 42000-50999 points
            totalPoints < 61000 -> 15   // Level 15: 51000-60999 points
            else -> 15 + ((totalPoints - 61000) / 10000) // Level 16+: every 10k points
        }
    
    // Points needed for current level
    val pointsForCurrentLevel: Int
        get() = when (level) {
            1 -> 0
            2 -> 500
            3 -> 1200
            4 -> 2000
            5 -> 3000
            6 -> 4500
            7 -> 6500
            8 -> 9000
            9 -> 12000
            10 -> 16000
            11 -> 21000
            12 -> 27000
            13 -> 34000
            14 -> 42000
            15 -> 51000
            else -> 61000 + ((level - 15) * 10000)
        }
    
    // Points needed for next level
    val pointsForNextLevel: Int
        get() = when (level) {
            1 -> 500
            2 -> 1200
            3 -> 2000
            4 -> 3000
            5 -> 4500
            6 -> 6500
            7 -> 9000
            8 -> 12000
            9 -> 16000
            10 -> 21000
            11 -> 27000
            12 -> 34000
            13 -> 42000
            14 -> 51000
            15 -> 61000
            else -> 61000 + ((level - 14) * 10000)
        }
    
    // Progress to next level (0.0 to 1.0)
    val progressToNextLevel: Float
        get() {
            val currentLevelPoints = pointsForCurrentLevel
            val nextLevelPoints = pointsForNextLevel
            val progressPoints = totalPoints - currentLevelPoints
            val neededPoints = nextLevelPoints - currentLevelPoints
            return if (neededPoints > 0) {
                (progressPoints.toFloat() / neededPoints.toFloat()).coerceIn(0f, 1f)
            } else 1f
        }
        
    val rank: String
        get() = when (level) {
            1 -> "🌱 Security Rookie"
            in 2..4 -> "⚡ Code Guardian" 
            in 5..7 -> "🔥 Exploit Hunter"
            in 8..10 -> "💀 Security Legend"
            in 11..15 -> "👑 Blockchain Master"
            else -> "🚀 Cyber God"
        }
}

/**
 * Power-ups for enhanced gameplay
 */
enum class PowerUpType(val displayName: String, val emoji: String, val description: String) {
    TIME_FREEZE("Time Freeze", "❄️", "Stop the timer for 30 seconds"),
    HINT_REVEALER("Hint Revealer", "💡", "Reveal one hint for free"),
    DOUBLE_POINTS("Double Points", "💎", "Double points for next challenge"),
    SKIP_PENALTY("Skip Shield", "🛡️", "Skip without losing streak"),
    CODE_HIGHLIGHTER("Code Highlighter", "🔍", "Highlight vulnerable lines"),
    LUCKY_GUESS("Lucky Guess", "🍀", "50% chance to auto-solve one gap")
}

/**
 * Statistics about challenge completion and progress
 */
data class ChallengeStats(
    val totalChallenges: Int,
    val completedChallenges: Int,
    val totalPoints: Int,
    val earnedPoints: Int,
    val challengesByDifficulty: Map<Difficulty, Int>,
    val challengesByVulnerability: Map<VulnerabilityType, Int>,
    val completionPercentage: Float
)

/**
 * Battle mode challenge for competitive play
 */
data class BattleChallenge(
    val baseChallenge: SmartContractChallenge,
    val player1Progress: Map<Int, String> = emptyMap(),
    val player2Progress: Map<Int, String> = emptyMap(),
    val winner: String? = null,
    val battleType: BattleType
)

enum class BattleType {
    SPEED_RACE,     // Who solves faster
    ACCURACY_DUEL,  // Who makes fewer mistakes
    ELIMINATION,    // Wrong answer eliminates you
    SURVIVAL        // Multiple rounds, last standing wins
} 