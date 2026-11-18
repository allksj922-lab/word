package com.example.data

import android.content.Context
import com.example.model.DifficultyLevel
import com.example.model.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class WordRepository(private val context: Context) {

    private var allWords: List<WordEntry> = emptyList()
    private var wordMap: Map<String, WordEntry> = emptyMap()
    private var byStartChar: Map<Char, List<WordEntry>> = emptyMap()

    suspend fun ensureLoaded() {
        if (allWords.isNotEmpty()) return
        withContext(Dispatchers.IO) {
            if (allWords.isNotEmpty()) return@withContext
            val jsonText = context.assets.open("words.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonText)
            val parsed = LinkedHashMap<String, WordEntry>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val word = obj.getString("w").trim().lowercase()
                if (word.length < 2 || !word.all { it in 'a'..'z' }) continue
                if (!parsed.containsKey(word)) {
                    parsed[word] = WordEntry(
                        word = word,
                        koreanMeaning = obj.getString("k"),
                        partOfSpeech = obj.getString("p"),
                        level = DifficultyLevel.fromName(obj.getString("l"))
                    )
                }
            }
            val list = parsed.values.sortedBy { it.word }
            allWords = list
            wordMap = parsed
            byStartChar = list.groupBy { it.word.first() }
        }
    }

    fun getAllWords(): List<WordEntry> = allWords

    fun getWordCountsByLevel(): Map<DifficultyLevel, Int> {
        return DifficultyLevel.entries.associateWith { level ->
            allWords.count { it.level == level }
        }
    }

    fun findWord(rawWord: String): WordEntry? {
        return wordMap[rawWord.trim().lowercase()]
    }

    fun pickStarterWord(level: DifficultyLevel): WordEntry {
        val levelWords = allWords.filter { it.level == level }
        val pool = levelWords.filter { candidate ->
            val nextChar = candidate.word.last()
            val outgoingCount = (byStartChar[nextChar]?.count { it.level == level } ?: 0)
            outgoingCount >= 3
        }.ifEmpty { levelWords }.ifEmpty { allWords }
        return pool.random()
    }

    fun pickBotWord(
        startsWith: Char,
        level: DifficultyLevel,
        usedWords: Set<String>,
        strictLevelMode: Boolean
    ): WordEntry? {
        val candidates = byStartChar[startsWith.lowercaseChar()].orEmpty()
            .filter { it.word !in usedWords }

        if (candidates.isEmpty()) return null

        val levelCandidates = candidates.filter { it.level == level }
        val pool = when {
            levelCandidates.isNotEmpty() -> levelCandidates
            strictLevelMode -> candidates.filter { it.level.ordinal >= level.ordinal }
            else -> candidates
        }

        if (pool.isEmpty()) return null

        // Prefer words that leave valid next moves for the player so rounds feel lively
        val playablePool = pool.filter { entry ->
            val nextChar = entry.word.last()
            byStartChar[nextChar].orEmpty().any { next ->
                next.word !in usedWords && next.word != entry.word
            }
        }

        return (playablePool.ifEmpty { pool }).random()
    }

    fun pickHintWord(
        startsWith: Char,
        level: DifficultyLevel,
        usedWords: Set<String>,
        strictLevelMode: Boolean
    ): WordEntry? {
        val candidates = byStartChar[startsWith.lowercaseChar()].orEmpty()
            .filter { it.word !in usedWords }

        if (candidates.isEmpty()) return null

        val preferred = if (strictLevelMode) {
            candidates.filter { it.level.ordinal >= level.ordinal }
        } else {
            candidates.filter { it.level == level }.ifEmpty { candidates }
        }

        return preferred.ifEmpty { null }?.random()
    }

    fun searchWords(
        levelFilter: DifficultyLevel?,
        query: String,
        letterFilter: Char?
    ): List<WordEntry> {
        val q = query.trim().lowercase()
        return allWords.filter { entry ->
            val matchesLevel = levelFilter == null || entry.level == levelFilter
            val matchesLetter = letterFilter == null || entry.word.first() == letterFilter.lowercaseChar()
            val matchesQuery = q.isEmpty() ||
                entry.word.contains(q) ||
                entry.koreanMeaning.contains(q)
            matchesLevel && matchesLetter && matchesQuery
        }
    }
}
