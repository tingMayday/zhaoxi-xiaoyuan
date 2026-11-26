package com.zhaoxi.xiaoyuan.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zhaoxi.xiaoyuan.data.AppContainer
import com.zhaoxi.xiaoyuan.notify.DeepLink
import com.zhaoxi.xiaoyuan.ui.archive.ArchiveScreen
import com.zhaoxi.xiaoyuan.ui.archive.ArchiveViewModel
import com.zhaoxi.xiaoyuan.ui.buy.BuyScreen
import com.zhaoxi.xiaoyuan.ui.buy.BuyViewModel
import com.zhaoxi.xiaoyuan.ui.common.FloatingHomeButton
import com.zhaoxi.xiaoyuan.ui.fitness.FitnessScreen
import com.zhaoxi.xiaoyuan.ui.fitness.FitnessViewModel
import com.zhaoxi.xiaoyuan.ui.habits.HabitsScreen
import com.zhaoxi.xiaoyuan.ui.habits.HabitsViewModel
import com.zhaoxi.xiaoyuan.ui.ledger.LedgerScreen
import com.zhaoxi.xiaoyuan.ui.ledger.LedgerViewModel
import com.zhaoxi.xiaoyuan.ui.media.MediaScreen
import com.zhaoxi.xiaoyuan.ui.media.MediaViewModel
import com.zhaoxi.xiaoyuan.ui.schedule.ScheduleScreen
import com.zhaoxi.xiaoyuan.ui.schedule.ScheduleViewModel
import com.zhaoxi.xiaoyuan.ui.settings.SettingsScreen
import com.zhaoxi.xiaoyuan.ui.settings.SettingsViewModel
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.today.TodayScreen
import com.zhaoxi.xiaoyuan.ui.today.TodayViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScaffold(
    container: AppContainer,
    onUnauthorized: () -> Unit,
    deepLinkOpen: String? = null,
    deepLinkScheduleId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var pendingScheduleId by remember { mutableStateOf<String?>(null) }
    val goBack: () -> Unit = {
        val current = navController.currentDestination?.route
        if (current != null && current != Routes.Today) {
            navController.popBackStack(Routes.Today, inclusive = false)
        }
    }
    val open: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.Today) { saveState = true }
        }
    }

    LaunchedEffect(deepLinkOpen, deepLinkScheduleId) {
        when (deepLinkOpen) {
            DeepLink.OPEN_SCHEDULE -> {
                pendingScheduleId = deepLinkScheduleId
                open(Routes.Schedule)
                onDeepLinkConsumed()
            }
            null -> Unit
            else -> onDeepLinkConsumed()
        }
    }

    Scaffold(
        containerColor = Card,
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Today,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.Today) {
                val vm: TodayViewModel = viewModel(
                    factory = TodayViewModel.factory(
                        todayRepository = container.todayRepository,
                        ledgerRepository = container.ledgerRepository,
                        scheduleRepository = container.scheduleRepository,
                        fitnessRepository = container.fitnessRepository,
                        buyRepository = container.buyRepository,
                        habitsRepository = container.habitsRepository,
                        reminders = container.reminderScheduler,
                        onUnauthorized = {
                            container.authRepository.logout()
                            onUnauthorized()
                        },
                    ),
                )
                TodayScreen(
                    viewModel = vm,
                    onOpenLedger = { open(Routes.Ledger) },
                    onOpenHabits = { open(Routes.Habits) },
                    onOpenFitness = { open(Routes.Fitness) },
                    onOpenSchedule = { open(Routes.Schedule) },
                    onOpenBuy = { open(Routes.Buy) },
                    onOpenMedia = { open(Routes.Media) },
                    onOpenArchive = { open(Routes.Archive) },
                    onOpenSettings = { open(Routes.Settings) },
                    onLogout = {
                        scope.launch {
                            container.authRepository.logout()
                            onUnauthorized()
                        }
                    },
                )
            }
            composable(Routes.Ledger) {
                val vm: LedgerViewModel = viewModel(factory = LedgerViewModel.factory(container.ledgerRepository))
                SubPage(onBack = goBack) { LedgerScreen(vm) }
            }
            composable(Routes.Habits) {
                val vm: HabitsViewModel = viewModel(factory = HabitsViewModel.factory(container.habitsRepository))
                SubPage(onBack = goBack) { HabitsScreen(vm) }
            }
            composable(Routes.Fitness) {
                val vm: FitnessViewModel = viewModel(factory = FitnessViewModel.factory(container.fitnessRepository))
                SubPage(onBack = goBack) { FitnessScreen(vm) }
            }
            composable(Routes.Schedule) {
                val vm: ScheduleViewModel = viewModel(
                    factory = ScheduleViewModel.factory(
                        container.scheduleRepository,
                        container.reminderScheduler,
                    ),
                )
                SubPage(onBack = goBack) { ScheduleScreen(vm, highlightId = pendingScheduleId) }
            }
            composable(Routes.Buy) {
                val vm: BuyViewModel = viewModel(factory = BuyViewModel.factory(container.buyRepository))
                SubPage(onBack = goBack) { BuyScreen(vm) }
            }
            composable(Routes.Media) {
                val vm: MediaViewModel = viewModel(factory = MediaViewModel.factory(container.mediaRepository))
                SubPage(onBack = goBack) { MediaScreen(vm) }
            }
            composable(Routes.Archive) {
                val vm: ArchiveViewModel = viewModel(factory = ArchiveViewModel.factory(container.archiveRepository))
                SubPage(onBack = goBack) { ArchiveScreen(vm) }
            }
            composable(Routes.Settings) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container.authRepository))
                SubPage(onBack = goBack) { SettingsScreen(vm) }
            }
        }
    }
}

@Composable
private fun SubPage(onBack: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        FloatingHomeButton(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
                .zIndex(2f),
        )
    }
}
