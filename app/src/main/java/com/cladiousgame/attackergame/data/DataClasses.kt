package com.cladiousgame.attackergame.data

/**
 * Achievement system for gamification
 */
data class Achievement(
    val id: String,
    val name: String,
    val title: String,
    val description: String,
    val icon: String,
    val pointsReward: Int,
    val rarity: AchievementRarity,
    val condition: AchievementCondition,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

enum class AchievementRarity(val displayName: String, val color: String, val emoji: String) {
    COMMON("Common", "#A0A0A0", "🥉"),
    RARE("Rare", "#0099FF", "🥈"),
    EPIC("Epic", "#9933FF", "🥇"),
    LEGENDARY("Legendary", "#FF9900", "🏆"),
    MYTHIC("Mythic", "#FF0066", "💎")
}

sealed class AchievementCondition {
    object FirstChallenge : AchievementCondition()
    data class CompleteChallenges(val count: Int) : AchievementCondition()
    data class ReachLevel(val level: Int) : AchievementCondition()
    data class GetStreak(val streak: Int) : AchievementCondition()
    data class EarnPoints(val points: Int) : AchievementCondition()
    data class CompleteWithoutHints(val count: Int) : AchievementCondition()
    data class CompleteInTime(val timePercent: Int, val count: Int) : AchievementCondition()
    data class FindVulnerability(val type: VulnerabilityType, val count: Int) : AchievementCondition()
    object CompleteAllRookie : AchievementCondition()
    object CompleteAllHacker : AchievementCondition()
    object CompleteAllExpert : AchievementCondition()
    object CompleteAllLegendary : AchievementCondition()
    data class GetCombo(val combo: Int) : AchievementCondition()
    object PerfectWeek : AchievementCondition() // Complete daily challenges for 7 days
    object SpeedRunner : AchievementCondition() // Complete challenge in under 30 seconds
    object NightOwl : AchievementCondition() // Complete challenge between 11PM-5AM
    object EarlyBird : AchievementCondition() // Complete challenge between 5AM-9AM
}

/**
 * Sound effect types for better user experience
 */
enum class SoundEffect(val fileName: String) {
    CORRECT_ANSWER("correct_answer.mp3"),
    WRONG_ANSWER("wrong_answer.mp3"),
    LEVEL_UP("level_up.mp3"),
    ACHIEVEMENT_UNLOCK("achievement.mp3"),
    COMBO_SOUND("combo.mp3"),
    BUTTON_CLICK("button_click.mp3"),
    GAME_START("game_start.mp3"),
    CHALLENGE_COMPLETE("challenge_complete.mp3"),
    STREAK_BONUS("streak_bonus.mp3"),
    TIME_WARNING("time_warning.mp3")
}

/**
 * Visual effect types for animations
 */
enum class VisualEffect {
    PARTICLE_EXPLOSION,
    SCREEN_SHAKE,
    GLOW_PULSE,
    SPARKLE_TRAIL,
    FIRE_ANIMATION,
    ELECTRIC_SPARK,
    RAINBOW_BURST,
    MATRIX_RAIN,
    HOLOGRAM_FLICKER,
    NEON_FLASH
} 