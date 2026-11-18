package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.viewmodel.WordChainViewModel

const val ROUTE_HOME = "home"
const val ROUTE_DICTIONARY = "dictionary"
const val ROUTE_GAME = "game"

@Composable
fun WordChainApp(
    viewModel: WordChainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val dictState by viewModel.dictionaryUiState.collectAsStateWithLifecycle()

    val showTopLevelNav = currentRoute == ROUTE_HOME || currentRoute == ROUTE_DICTIONARY

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showTopLevelNav && !useRail) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_HOME,
                            onClick = {
                                navController.navigate(ROUTE_HOME) {
                                    launchSingleTop = true
                                    popUpTo(ROUTE_HOME) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "홈"
                                )
                            },
                            label = { Text("홈") },
                            modifier = Modifier.testTag("nav_home_tab")
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_DICTIONARY,
                            onClick = {
                                navController.navigate(ROUTE_DICTIONARY) {
                                    launchSingleTop = true
                                    popUpTo(ROUTE_HOME) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_DICTIONARY) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = "단어장"
                                )
                            },
                            label = { Text("단어장") },
                            modifier = Modifier.testTag("nav_dictionary_tab")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useRail) {
                    NavigationRail {
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_HOME,
                            onClick = {
                                navController.navigate(ROUTE_HOME) {
                                    launchSingleTop = true
                                    popUpTo(ROUTE_HOME) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "홈"
                                )
                            },
                            label = { Text("홈") }
                        )
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_DICTIONARY,
                            onClick = {
                                navController.navigate(ROUTE_DICTIONARY) {
                                    launchSingleTop = true
                                    popUpTo(ROUTE_HOME) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_DICTIONARY) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = "단어장"
                                )
                            },
                            label = { Text("단어장") }
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            state = homeState,
                            onSelectDifficulty = viewModel::selectDifficulty,
                            onToggleTimer = viewModel::setTimerEnabled,
                            onToggleStrictMode = viewModel::setStrictLevelMode,
                            onStartGame = {
                                viewModel.startNewGame()
                                navController.navigate(ROUTE_GAME)
                            },
                            onOpenDictionary = {
                                navController.navigate(ROUTE_DICTIONARY) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.statusBarsPadding()
                        )
                    }

                    composable(ROUTE_DICTIONARY) {
                        DictionaryScreen(
                            state = dictState,
                            onSelectLevel = viewModel::updateDictionaryLevel,
                            onQueryChange = viewModel::updateDictionaryQuery,
                            onSelectLetter = viewModel::updateDictionaryLetter,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }

                    composable(ROUTE_GAME) {
                        GameScreen(
                            state = gameState,
                            onInputChanged = viewModel::onInputTextChanged,
                            onSubmitWord = viewModel::submitPlayerWord,
                            onRequestHint = viewModel::requestHint,
                            onApplyHint = viewModel::applyHintWord,
                            onRestartGame = {
                                viewModel.startNewGame(
                                    difficulty = gameState.difficulty,
                                    timerEnabled = gameState.timerEnabled,
                                    strictLevelMode = gameState.strictLevelMode
                                )
                            },
                            onSurrender = viewModel::surrenderGame,
                            onNavigateBack = {
                                viewModel.pauseTimerOnExit()
                                navController.popBackStack()
                            },
                            onPauseTimer = viewModel::pauseTimerOnExit
                        )
                    }
                }
            }
        }
    }
}
