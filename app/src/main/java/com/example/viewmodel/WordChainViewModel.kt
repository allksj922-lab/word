package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import com.example.data.WordRepository
import com.example.model.ChainTurn
import com.example.model.DifficultyLevel
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.WordEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isReady: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val wordCounts: Map<DifficultyLevel, Int> = emptyMap()
)

data class DictionaryUiState(
    val levelFilter: DifficultyLevel? = null,
    val query: String = "",
    val letterFilter: Char? = null,
    val words: List<WordEntry> = emptyList(),
    val totalCount: Int = 0
)

class WordChainViewModel(application: Application) : AndroidViewModel(application) {

    private val wordRepository = WordRepository(application.applicationContext)
    private val prefsRepository = UserPreferencesRepository(application.applicationContext)

    private val _isDictionaryLoaded = MutableStateFlow(false)
    private val _wordCounts = MutableStateFlow<Map<DifficultyLevel, Int>>(emptyMap())

    val homeUiState: StateFlow<HomeUiState> = combine(
        _isDictionaryLoaded,
        prefsRepository.preferencesFlow,
        _wordCounts
    ) { loaded, prefs, counts ->
        HomeUiState(
            isReady = loaded,
            preferences = prefs,
            wordCounts = counts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private val _dictLevelFilter = MutableStateFlow<DifficultyLevel?>(null)
    private val _dictQuery = MutableStateFlow("")
    private val _dictLetterFilter = MutableStateFlow<Char?>(null)

    val dictionaryUiState: StateFlow<DictionaryUiState> = combine(
        _isDictionaryLoaded,
        _dictLevelFilter,
        _dictQuery,
        _dictLetterFilter
    ) { loaded, level, query, letter ->
        if (!loaded) {
            DictionaryUiState()
        } else {
            DictionaryUiState(
                levelFilter = level,
                query = query,
                letterFilter = letter,
                words = wordRepository.searchWords(level, query, letter),
                totalCount = wordRepository.getAllWords().size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DictionaryUiState()
    )

    private var timerJob: Job? = null
    private var botTurnJob: Job? = null

    init {
        viewModelScope.launch {
            wordRepository.ensureLoaded()
            _wordCounts.value = wordRepository.getWordCountsByLevel()
            _isDictionaryLoaded.value = true
        }
    }

    fun selectDifficulty(level: DifficultyLevel) {
        viewModelScope.launch {
            prefsRepository.setSelectedDifficulty(level)
        }
    }

    fun setTimerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setTimerEnabled(enabled)
        }
    }

    fun setStrictLevelMode(strict: Boolean) {
        viewModelScope.launch {
            prefsRepository.setStrictLevelMode(strict)
        }
    }

    fun updateDictionaryLevel(level: DifficultyLevel?) {
        _dictLevelFilter.value = level
    }

    fun updateDictionaryQuery(query: String) {
        _dictQuery.value = query
    }

    fun updateDictionaryLetter(letter: Char?) {
        _dictLetterFilter.value = if (_dictLetterFilter.value == letter) null else letter
    }

    fun startNewGame(
        difficulty: DifficultyLevel = homeUiState.value.preferences.selectedDifficulty,
        timerEnabled: Boolean = homeUiState.value.preferences.timerEnabled,
        strictLevelMode: Boolean = homeUiState.value.preferences.strictLevelMode
    ) {
        timerJob?.cancel()
        botTurnJob?.cancel()

        viewModelScope.launch {
            wordRepository.ensureLoaded()
            _wordCounts.value = wordRepository.getWordCountsByLevel()
            _isDictionaryLoaded.value = true

            val starter = wordRepository.pickStarterWord(difficulty)
            val firstTurn = ChainTurn(
                id = 1,
                word = starter.word,
                koreanMeaning = starter.koreanMeaning,
                partOfSpeech = starter.partOfSpeech,
                wordLevel = starter.level,
                isPlayer = false,
                pointsEarned = 0
            )
            _gameState.value = GameUiState(
                difficulty = difficulty,
                chain = listOf(firstTurn),
                requiredChar = starter.word.last(),
                score = 0,
                streak = 0,
                maxStreak = 0,
                hintsRemaining = 3,
                activeHint = null,
                inputText = "",
                inputError = null,
                isBotThinking = false,
                secondsRemaining = difficulty.turnSeconds,
                timerEnabled = timerEnabled,
                strictLevelMode = strictLevelMode,
                gameStatus = GameStatus.PLAYING,
                endReason = null
            )
            if (timerEnabled) {
                startTurnTimer()
            }
        }
    }

    fun onInputTextChanged(text: String) {
        val sanitized = text.filter { it in 'a'..'z' || it in 'A'..'Z' }.lowercase()
        _gameState.update {
            it.copy(inputText = sanitized, inputError = null)
        }
    }

    fun submitPlayerWord() {
        val current = _gameState.value
        if (current.gameStatus != GameStatus.PLAYING || current.isBotThinking) return

        val candidate = current.inputText.trim().lowercase()
        if (candidate.isEmpty()) {
            _gameState.update { it.copy(inputError = "영어 단어를 입력해 주세요.") }
            return
        }
        if (candidate.length < 2) {
            _gameState.update { it.copy(inputError = "2글자 이상의 영어 단어를 입력해 주세요.") }
            return
        }
        if (candidate.first() != current.requiredChar) {
            _gameState.update {
                it.copy(
                    inputError = "'${current.requiredChar.uppercaseChar()}'로 시작하는 단어를 입력해야 해요."
                )
            }
            return
        }
        if (current.chain.any { it.word == candidate }) {
            _gameState.update { it.copy(inputError = "이미 사용한 단어예요. 다른 단어를 떠올려 보세요!") }
            return
        }

        val found = wordRepository.findWord(candidate)
        if (found == null) {
            _gameState.update {
                it.copy(inputError = "사전에 등록되지 않은 단어예요. 힌트 버튼을 활용해 보세요!")
            }
            return
        }

        if (current.strictLevelMode && found.level.ordinal < current.difficulty.ordinal) {
            _gameState.update {
                it.copy(
                    inputError = "엄격 모드에서는 ${current.difficulty.koreanTitle} 이상 단어만 사용할 수 있어요."
                )
            }
            return
        }

        // Valid move!
        timerJob?.cancel()
        val newStreak = current.streak + 1
        val levelBonus = if (found.level.ordinal >= current.difficulty.ordinal) found.level.basePoints else 8
        val lengthBonus = (found.word.length - 3).coerceAtLeast(0) * 2
        val streakBonus = (newStreak - 1) * 3
        val pointsEarned = levelBonus + lengthBonus + streakBonus

        val playerTurn = ChainTurn(
            id = current.chain.size + 1,
            word = found.word,
            koreanMeaning = found.koreanMeaning,
            partOfSpeech = found.partOfSpeech,
            wordLevel = found.level,
            isPlayer = true,
            pointsEarned = pointsEarned
        )

        val updatedChain = current.chain + playerTurn
        val nextRequired = found.word.last()

        _gameState.update {
            it.copy(
                chain = updatedChain,
                requiredChar = nextRequired,
                score = it.score + pointsEarned,
                streak = newStreak,
                maxStreak = maxOf(it.maxStreak, newStreak),
                activeHint = null,
                inputText = "",
                inputError = null,
                isBotThinking = true
            )
        }

        triggerBotTurn(nextRequired, updatedChain)
    }

    private fun triggerBotTurn(startsWith: Char, currentChain: List<ChainTurn>) {
        botTurnJob?.cancel()
        botTurnJob = viewModelScope.launch {
            delay(480)
            val state = _gameState.value
            if (state.gameStatus != GameStatus.PLAYING) return@launch

            val used = currentChain.map { it.word }.toSet()
            val botWord = wordRepository.pickBotWord(
                startsWith = startsWith,
                level = state.difficulty,
                usedWords = used,
                strictLevelMode = state.strictLevelMode
            )

            if (botWord == null) {
                _gameState.update {
                    it.copy(
                        isBotThinking = false,
                        score = it.score + 50,
                        gameStatus = GameStatus.PLAYER_WON,
                        endReason = "AI 봇이 '${startsWith.uppercaseChar()}'로 시작하는 단어를 찾지 못했어요! 승리 보너스 +50점!"
                    )
                }
                return@launch
            }

            val botTurn = ChainTurn(
                id = currentChain.size + 1,
                word = botWord.word,
                koreanMeaning = botWord.koreanMeaning,
                partOfSpeech = botWord.partOfSpeech,
                wordLevel = botWord.level,
                isPlayer = false,
                pointsEarned = 0
            )

            _gameState.update {
                it.copy(
                    chain = currentChain + botTurn,
                    requiredChar = botWord.word.last(),
                    isBotThinking = false,
                    secondsRemaining = it.difficulty.turnSeconds
                )
            }

            if (_gameState.value.timerEnabled) {
                startTurnTimer()
            }
        }
    }

    private fun startTurnTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val current = _gameState.value
                if (current.gameStatus != GameStatus.PLAYING || current.isBotThinking || !current.timerEnabled) {
                    break
                }
                val nextSec = current.secondsRemaining - 1
                if (nextSec <= 0) {
                    _gameState.update {
                        it.copy(
                            secondsRemaining = 0,
                            gameStatus = GameStatus.GAME_OVER,
                            endReason = "제한 시간 ${it.difficulty.turnSeconds}초가 지났어요!"
                        )
                    }
                    break
                } else {
                    _gameState.update { it.copy(secondsRemaining = nextSec) }
                }
            }
        }
    }

    fun requestHint() {
        val current = _gameState.value
        if (current.gameStatus != GameStatus.PLAYING || current.isBotThinking) return
        if (current.activeHint != null) return
        if (current.hintsRemaining <= 0) {
            _gameState.update { it.copy(inputError = "이번 게임의 힌트를 모두 사용했어요.") }
            return
        }

        val used = current.chain.map { it.word }.toSet()
        val hint = wordRepository.pickHintWord(
            startsWith = current.requiredChar,
            level = current.difficulty,
            usedWords = used,
            strictLevelMode = current.strictLevelMode
        )

        if (hint == null) {
            _gameState.update { it.copy(inputError = "더 이상 연결할 수 있는 단어가 없어요.") }
        } else {
            _gameState.update {
                it.copy(
                    hintsRemaining = it.hintsRemaining - 1,
                    activeHint = hint,
                    inputError = null
                )
            }
        }
    }

    fun applyHintWord() {
        val hint = _gameState.value.activeHint ?: return
        _gameState.update {
            it.copy(inputText = hint.word, inputError = null)
        }
    }

    fun surrenderGame() {
        timerJob?.cancel()
        botTurnJob?.cancel()
        _gameState.update {
            if (it.gameStatus == GameStatus.PLAYING) {
                it.copy(
                    isBotThinking = false,
                    gameStatus = GameStatus.GAME_OVER,
                    endReason = "게임을 종료하고 학습 결과를 정리했어요."
                )
            } else {
                it
            }
        }
    }

    fun pauseTimerOnExit() {
        timerJob?.cancel()
        botTurnJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        botTurnJob?.cancel()
    }
}
