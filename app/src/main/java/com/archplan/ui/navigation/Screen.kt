package com.archplan.ui.navigation

/**
 * Navigation route definitions for the app.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object PlanInput : Screen("plan_input")
    data object PlanOutput : Screen("plan_output/{planId}") {
        fun createRoute(planId: String) = "plan_output/$planId"
    }
    data object SavedPlans : Screen("saved_plans")
    data object Settings : Screen("settings")
}
