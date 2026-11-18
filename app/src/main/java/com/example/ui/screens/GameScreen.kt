package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChainTurn
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.HighlightedChainWord
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.OutfitFontFamily

@Composable
fun GameScreen(
    state: GameUiState,
    onInputChanged: (String) -> Unit,
    onSubmitWord: () -> Unit,
    onRequestHint: () -> Unit,
    onApplyHint: () -> Unit,
    onRestartGame: () -> Unit,
    onSurrender: () -> Unit,
    onNavigateBack: () -> Unit,
    onPauseTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    DisposableEffect(Unit) {
        onDispose { onPauseTimer() }
    }

    LaunchedEffect(state.chain.size, state.isBotThinking) {
        if (state.chain.isNotEmpty()) {
            listState.animateScrollToItem(state.chain.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            GameTopBar(
                state = state,
                onBack = onNavigateBack,
                onRestart = onRestartGame,
                onSurrender = onSurrender
            )

            TargetLetterSpotlight(
                state = state,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.chain, key = { it.id }) { turn ->
                    ChainTurnBubble(turn = turn)
                }

                if (state.isBotThinking) {
                    item(key = "bot_thinking") {
                        BotThinkingBubble()
                    }
                }
            }

            AnimatedVisibility(
                visible = state.activeHint != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                state.activeHint?.let { hint ->
                    HintBanner(
                        word = hint.word,
                        koreanMeaning = hint.koreanMeaning,
                        partOfSpeech = hint.partOfSpeech,
                        onApply = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onApplyHint()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            WordInputBar(
                state = state,
                onInputChanged = onInputChanged,
                onSubmitWord = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSubmitWord()
                },
                onRequestHint = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRequestHint()
                }
            )
        }
    }

    if (state.gameStatus != GameStatus.PLAYING) {
        GameSummaryDialog(
            state = state,
            onPlayAgain = onRestartGame,
            onReturnHome = onNavigateBack
        )
    }
}

@Composable
private fun GameTopBar(
    state: GameUiState,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onSurrender: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "홈으로 돌아가기"
                )
            }
            DifficultyBadge(level = state.difficulty)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "${state.score}점",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (state.streak >= 2) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "${state.streak}연속",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            IconButton(
                onClick = onSurrender,
                enabled = state.gameStatus == GameStatus.PLAYING,
                modifier = Modifier.testTag("surrender_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = "게임 종료 및 결과 보기"
                )
            }

            IconButton(
                onClick = onRestart,
                modifier = Modifier.testTag("restart_game_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "새 게임 시작"
                )
            }
        }
    }
}

@Composable
private fun TargetLetterSpotlight(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    val lastTurn = state.chain.lastOrNull()
    val progress by animateFloatAsState(
        targetValue = if (state.timerEnabled && state.difficulty.turnSeconds > 0) {
            state.secondsRemaining.toFloat() / state.difficulty.turnSeconds.toFloat()
        } else {
            1f
        },
        label = "timerProgress"
    )
    val isLowTime = state.timerEnabled && state.secondsRemaining <= 6

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLowTime) CoralAccent else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.requiredChar.uppercaseChar().toString(),
                        fontFamily = OutfitFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "'${state.requiredChar.uppercaseChar()}' 로 시작하는 단어를 입력하세요",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lastTurn != null) {
                        Text(
                            text = "이전: ${lastTurn.word} (${lastTurn.koreanMeaning})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                if (state.timerEnabled) {
                    Surface(
                        color = if (isLowTime) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${state.secondsRemaining}s",
                            fontFamily = OutfitFontFamily,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isLowTime) CoralAccent else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (state.timerEnabled) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = if (isLowTime) CoralAccent else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChainTurnBubble(turn: ChainTurn) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (turn.isPlayer) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (turn.isPlayer) 18.dp else 4.dp,
                bottomEnd = if (turn.isPlayer) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (turn.isPlayer) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                1.dp,
                if (turn.isPlayer) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (turn.isPlayer) "나 · #${turn.id}" else "WordBot · #${turn.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DifficultyBadge(level = turn.wordLevel)
                    if (turn.pointsEarned > 0) {
                        Text(
                            text = "+${turn.pointsEarned}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CoralAccent
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HighlightedChainWord(
                        word = turn.word,
                        firstLetterColor = MaterialTheme.colorScheme.primary,
                        lastLetterColor = CoralAccent,
                        bodyColor = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp
                    )
                    Text(
                        text = turn.partOfSpeech,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = turn.koreanMeaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BotThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "WordBot이 단어를 고르는 중...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HintBanner(
    word: String,
    koreanMeaning: String,
    partOfSpeech: String,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefix = word.take(2)
    val masked = prefix + " " + "_ ".repeat((word.length - 2).coerceAtLeast(1)).trim()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        border = BorderStroke(1.dp, CoralAccent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = CoralAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$masked (${word.length}글자 · $partOfSpeech)",
                        fontFamily = OutfitFontFamily,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = "뜻: $koreanMeaning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            TextButton(
                onClick = onApply,
                modifier = Modifier.testTag("apply_hint_button")
            ) {
                Text("채우기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WordInputBar(
    state: GameUiState,
    onInputChanged: (String) -> Unit,
    onSubmitWord: () -> Unit,
    onRequestHint: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.inputError != null) {
                Text(
                    text = state.inputError,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    onClick = onRequestHint,
                    enabled = state.gameStatus == GameStatus.PLAYING && !state.isBotThinking && state.hintsRemaining > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hint_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (state.hintsRemaining > 0) {
                                Badge { Text(state.hintsRemaining.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = "힌트 보기 (남은 횟수 ${state.hintsRemaining})"
                        )
                    }
                }

                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = onInputChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("word_input_field"),
                    enabled = state.gameStatus == GameStatus.PLAYING && !state.isBotThinking,
                    placeholder = {
                        Text("'${state.requiredChar.uppercaseChar()}'로 시작하는 영단어")
                    },
                    singleLine = true,
                    isError = state.inputError != null,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSubmitWord() }
                    )
                )

                FilledIconButton(
                    onClick = onSubmitWord,
                    enabled = state.gameStatus == GameStatus.PLAYING &&
                        !state.isBotThinking &&
                        state.inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("submit_word_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "단어 제출"
                    )
                }
            }
        }
    }
}

@Composable
private fun GameSummaryDialog(
    state: GameUiState,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    val isVictory = state.gameStatus == GameStatus.PLAYER_WON
    val playerWordsCount = state.chain.count { it.isPlayer }

    AlertDialog(
        onDismissRequest = {},
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isVictory) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = if (isVictory) MaterialTheme.colorScheme.primary else CoralAccent,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = if (isVictory) "축하해요! 승리했습니다!" else "끝말잇기 결과",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.endReason != null) {
                    Text(
                        text = state.endReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryStat(label = "최종 점수", value = "${state.score}점")
                        SummaryStat(label = "내가 이은 단어", value = "${playerWordsCount}개")
                        SummaryStat(label = "최고 연속", value = "${state.maxStreak}회")
                    }
                }

                if (state.chain.isNotEmpty()) {
                    Text(
                        text = "이번 판 단어 복습 (최근 4개)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.chain.takeLast(4).forEach { turn ->
                            Text(
                                text = "• ${turn.word}: ${turn.koreanMeaning}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.testTag("play_again_button")
            ) {
                Text("다시 도전")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReturnHome,
                modifier = Modifier.testTag("return_home_button")
            ) {
                Text("홈으로")
            }
        }
    )
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
