package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WordRepository
import com.example.model.DifficultyLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches WordChain`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WordChain", appName)
  }

  @Test
  fun `word repository loads beginner intermediate and advanced words`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = WordRepository(context)
    repository.ensureLoaded()

    val counts = repository.getWordCountsByLevel()
    assertTrue((counts[DifficultyLevel.BEGINNER] ?: 0) >= 200)
    assertTrue((counts[DifficultyLevel.INTERMEDIATE] ?: 0) >= 200)
    assertTrue((counts[DifficultyLevel.ADVANCED] ?: 0) >= 200)

    val starter = repository.pickStarterWord(DifficultyLevel.INTERMEDIATE)
    assertEquals(DifficultyLevel.INTERMEDIATE, starter.level)

    val botReply = repository.pickBotWord(
      startsWith = starter.word.last(),
      level = DifficultyLevel.INTERMEDIATE,
      usedWords = setOf(starter.word),
      strictLevelMode = false
    )
    assertNotNull(botReply)
    assertEquals(starter.word.last(), botReply!!.word.first())
  }
}
