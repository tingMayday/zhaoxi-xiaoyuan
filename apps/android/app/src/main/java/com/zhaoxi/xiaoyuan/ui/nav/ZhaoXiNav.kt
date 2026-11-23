package com.zhaoxi.xiaoyuan.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zhaoxi.xiaoyuan.data.AppContainer
import com.zhaoxi.xiaoyuan.ui.login.LoginScreen
import com.zhaoxi.xiaoyuan.ui.login.LoginViewModel
import com.zhaoxi.xiaoyuan.ui.session.SessionViewModel
import com.zhaoxi.xiaoyuan.ui.theme.Accent
import com.zhaoxi.xiaoyuan.ui.theme.Card

@Composable
fun ZhaoXiNav(container: AppContainer) {
    val sessionVm: SessionViewModel = viewModel(factory = SessionViewModel.factory(container.authRepository))
    val session by sessionVm.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    if (!session.ready) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Card),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
        return
    }

    val start = if (session.loggedIn) Routes.Home else Routes.Login

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.Login) {
            val loginVm: LoginViewModel = viewModel(factory = LoginViewModel.factory(container.authRepository))
            LoginScreen(
                viewModel = loginVm,
                onLoggedIn = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Home) {
            HomeScaffold(
                container = container,
                onUnauthorized = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
            )
        }
    }
}
