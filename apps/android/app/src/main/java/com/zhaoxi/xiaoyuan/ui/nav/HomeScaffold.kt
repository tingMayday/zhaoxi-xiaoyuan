package com.zhaoxi.xiaoyuan.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zhaoxi.xiaoyuan.data.AppContainer
import com.zhaoxi.xiaoyuan.ui.archive.ArchiveScreen
import com.zhaoxi.xiaoyuan.ui.archive.ArchiveViewModel
import com.zhaoxi.xiaoyuan.ui.buy.BuyScreen
import com.zhaoxi.xiaoyuan.ui.buy.BuyViewModel
import com.zhaoxi.xiaoyuan.ui.fitness.FitnessScreen
import com.zhaoxi.xiaoyuan.ui.fitness.FitnessViewModel
import com.zhaoxi.xiaoyuan.ui.habits.HabitsScreen
import com.zhaoxi.xiaoyuan.ui.habits.HabitsViewModel
import com.zhaoxi.xiaoyuan.ui.ledger.LedgerScreen
import com.zhaoxi.xiaoyuan.ui.ledger.LedgerViewModel
import com.zhaoxi.xiaoyuan.ui.media.MediaScreen
import com.zhaoxi.xiaoyuan.ui.media.MediaViewModel
import com.zhaoxi.xiaoyuan.ui.more.MoreScreen
import com.zhaoxi.xiaoyuan.ui.schedule.ScheduleScreen
import com.zhaoxi.xiaoyuan.ui.schedule.ScheduleViewModel
import com.zhaoxi.xiaoyuan.ui.settings.SettingsScreen
import com.zhaoxi.xiaoyuan.ui.settings.SettingsViewModel
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card
import com.zhaoxi.xiaoyuan.ui.theme.Muted
import com.zhaoxi.xiaoyuan.ui.theme.Paper
import com.zhaoxi.xiaoyuan.ui.today.TodayScreen
import com.zhaoxi.xiaoyuan.ui.today.TodayViewModel

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun HomeScaffold(
    container: AppContainer,
    onUnauthorized: () -> Unit,
) {
    val navController = rememberNavController()
    val tabs = listOf(
        TabItem(Routes.Today, "今日", Icons.Outlined.Home),
        TabItem(Routes.Ledger, "记账", Icons.Outlined.Payments),
        TabItem(Routes.Habits, "习惯", Icons.Outlined.CheckCircle),
        TabItem(Routes.Fitness, "健身", Icons.Outlined.ShowChart),
        TabItem(Routes.More, "更多", Icons.Outlined.MoreHoriz),
    )
    val tabRoutes = tabs.map { it.route }.toSet()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottomBar = current in tabRoutes

    Scaffold(
        containerColor = Card,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Paper) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                                indicatorColor = Card,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Today,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Today) {
                val vm: TodayViewModel = viewModel(
                    factory = TodayViewModel.factory(
                        todayRepository = container.todayRepository,
                        onUnauthorized = {
                            container.authRepository.logout()
                            onUnauthorized()
                        },
                    ),
                )
                TodayScreen(vm)
            }
            composable(Routes.Ledger) {
                val vm: LedgerViewModel = viewModel(factory = LedgerViewModel.factory(container.ledgerRepository))
                LedgerScreen(vm)
            }
            composable(Routes.Habits) {
                val vm: HabitsViewModel = viewModel(factory = HabitsViewModel.factory(container.habitsRepository))
                HabitsScreen(vm)
            }
            composable(Routes.Fitness) {
                val vm: FitnessViewModel = viewModel(factory = FitnessViewModel.factory(container.fitnessRepository))
                FitnessScreen(vm)
            }
            composable(Routes.More) {
                MoreScreen(
                    authRepository = container.authRepository,
                    onOpenSchedule = { navController.navigate(Routes.Schedule) },
                    onOpenBuy = { navController.navigate(Routes.Buy) },
                    onOpenMedia = { navController.navigate(Routes.Media) },
                    onOpenArchive = { navController.navigate(Routes.Archive) },
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                    onLoggedOut = onUnauthorized,
                )
            }
            composable(Routes.Schedule) {
                val vm: ScheduleViewModel = viewModel(factory = ScheduleViewModel.factory(container.scheduleRepository))
                ScheduleScreen(vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.Buy) {
                val vm: BuyViewModel = viewModel(factory = BuyViewModel.factory(container.buyRepository))
                BuyScreen(vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.Media) {
                val vm: MediaViewModel = viewModel(factory = MediaViewModel.factory(container.mediaRepository))
                MediaScreen(vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.Archive) {
                val vm: ArchiveViewModel = viewModel(factory = ArchiveViewModel.factory(container.archiveRepository))
                ArchiveScreen(vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.Settings) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container.authRepository))
                SettingsScreen(vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
