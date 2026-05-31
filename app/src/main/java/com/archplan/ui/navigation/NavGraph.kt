package com.archplan.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.archplan.ui.screens.HomeScreen
import com.archplan.ui.screens.PlanInputScreen
import com.archplan.ui.screens.PlanOutputScreen
import com.archplan.ui.screens.SavedPlansScreen
import com.archplan.ui.screens.SettingsScreen
import com.archplan.ui.screens.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNewPlan = {
                    navController.navigate(Screen.PlanInput.route)
                },
                onNavigateToSavedPlans = {
                    navController.navigate(Screen.SavedPlans.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onOpenPlan = { planId ->
                    navController.navigate(Screen.PlanOutput.createRoute(planId))
                }
            )
        }

        composable(Screen.PlanInput.route) {
            PlanInputScreen(
                onNavigateToOutput = { planId ->
                    navController.navigate(Screen.PlanOutput.createRoute(planId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PlanOutput.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            PlanOutputScreen(
                planId = planId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.popBackStack(Screen.PlanInput.route, inclusive = false)
                }
            )
        }

        composable(Screen.SavedPlans.route) {
            SavedPlansScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenPlan = { planId ->
                    navController.navigate(Screen.PlanOutput.createRoute(planId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
