package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AdvancedContainer
import com.example.ui.theme.AdvancedRuby
import com.example.ui.theme.BeginnerContainer
import com.example.ui.theme.BeginnerEmerald
import com.example.ui.theme.IntermediateAmber
import com.example.ui.theme.IntermediateContainer

enum class DifficultyLevel(
    val koreanTitle: String,
    val englishTitle: String,
    val subtitle: String,
    val sampleChain: String,
    val turnSeconds: Int,
    val basePoints: Int,
    val accentColor: Color,
    val containerColor: Color
) {
    BEGINNER(
        koreanTitle = "초급",
        englishTitle = "Beginner",
        subtitle = "일상 필수 기초 영단어 (A1-A2)",
        sampleChain = "apple → elephant → tiger",
        turnSeconds = 30,
        basePoints = 10,
        accentColor = BeginnerEmerald,
        containerColor = BeginnerContainer
    ),
    INTERMEDIATE(
        koreanTitle = "중급",
        englishTitle = "Intermediate",
        subtitle = "실용·비즈니스·교양 어휘 (B1-B2)",
        sampleChain = "adventure → explore → energy",
        turnSeconds = 25,
        basePoints = 20,
        accentColor = IntermediateAmber,
        containerColor = IntermediateContainer
    ),
    ADVANCED(
        koreanTitle = "고급",
        englishTitle = "Advanced",
        subtitle = "심화 학술·고득점 어휘 (C1-C2)",
        sampleChain = "ephemeral → lucid → diligent",
        turnSeconds = 20,
        basePoints = 35,
        accentColor = AdvancedRuby,
        containerColor = AdvancedContainer
    );

    companion object {
        fun fromName(name: String?): DifficultyLevel {
            return entries.firstOrNull { it.name == name } ?: BEGINNER
        }
    }
}

data class WordEntry(
    val word: String,
    val koreanMeaning: String,
    val partOfSpeech: String,
    val level: DifficultyLevel
)

data class ChainTurn(
    val id: Int,
    val word: String,
    val koreanMeaning: String,
    val partOfSpeech: String,
    val wordLevel: DifficultyLevel,
    val isPlayer: Boolean,
    val pointsEarned: Int
)

enum class GameStatus {
    PLAYING,
    PLAYER_WON,
    GAME_OVER
}

data class GameUiState(
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val chain: List<ChainTurn> = emptyList(),
    val requiredChar: Char = 'a',
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val hintsRemaining: Int = 3,
    val activeHint: WordEntry? = null,
    val inputText: String = "",
    val inputError: String? = null,
    val isBotThinking: Boolean = false,
    val secondsRemaining: Int = 30,
    val timerEnabled: Boolean = true,
    val strictLevelMode: Boolean = false,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val endReason: String? = null
)
