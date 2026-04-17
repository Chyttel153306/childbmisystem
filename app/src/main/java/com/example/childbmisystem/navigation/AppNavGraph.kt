package com.example.childbmisystem.navigation
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.FirebaseRepository
import com.example.childbmisystem.screens.bhwscreen.BhwDashboardScreen
import com.example.childbmisystem.screens.parentscreen.ChildHistoryScreen
import com.example.childbmisystem.screens.bhwscreen.DeleteChildProfileScreen
import com.example.childbmisystem.screens.bhwscreen.SendStatusAlertScreen
import com.example.childbmisystem.screens.bhwscreen.UpdateChildProfileScreen
import com.example.childbmisystem.screens.commonscreen.LoginScreen
import com.example.childbmisystem.screens.commonscreen.EvidencePreviewScreen
import com.example.childbmisystem.screens.commonscreen.ProfileScreen
import com.example.childbmisystem.screens.commonscreen.RegistrationScreen
import com.example.childbmisystem.screens.bhwscreen.ViewChildProfileScreen
import com.example.childbmisystem.screens.parentscreen.CreateChildProfileScreen
import com.example.childbmisystem.screens.parentscreen.ParentDashboardScreen

object Routes {

    const val LOGIN = "login"
    const val REGISTER = "register"

    const val PARENT_DASHBOARD = "parent_dashboard"
    const val PARENT_PROFILE = "parent_profile"

    const val BHW_DASHBOARD = "bhw_dashboard"
    const val BHW_PROFILE = "bhw_profile"

    const val CREATE_CHILD = "create_child"
    const val VIEW_CHILD = "view_child/{childId}"
    const val UPDATE_CHILD = "update_child/{childId}"
    const val CHILD_HISTORY = "child_history/{childId}"
    const val EVIDENCE_PREVIEW = "evidence_preview"
    const val SEND_ALERT = "send_alert?childId={childId}"
    const val DELETE_CHILD = "delete_child/{childId}"

    fun viewChild(id: String) = "view_child/$id"
    fun updateChild(id: String) = "update_child/$id"
    fun childHistory(id: String) = "child_history/$id"
    fun deleteChild(id: String) = "delete_child/$id"
    fun sendAlert(childId: String? = null) =
        if (childId.isNullOrBlank()) "send_alert" else "send_alert?childId=$childId"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {

    LaunchedEffect(Unit) {
        val uid = FirebaseRepository.currentUid
        if (uid != null && AppData.currentUser.value == null) {
            val user = FirebaseRepository.getUser(uid)
            if (user != null) {
                AppData.currentUser.value = user
                AppData.loadData()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(
                    initialOffsetX = { it / 6 },
                    animationSpec = tween(260)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(
                    targetOffsetX = { -it / 10 },
                    animationSpec = tween(220)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(
                    initialOffsetX = { -it / 6 },
                    animationSpec = tween(260)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(
                    targetOffsetX = { it / 10 },
                    animationSpec = tween(220)
                )
        }
    ) {

        // ================= AUTH =================

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegistrationScreen(navController)
        }

        // ================= DASHBOARDS =================

        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(navController)
        }

        composable(Routes.BHW_DASHBOARD) {
            BhwDashboardScreen(navController)
        }

        // ================= SHARED PROFILE =================
        // Both routes point to the same ProfileScreen.
        // It detects the user's role internally and shows
        // the correct content + bottom bar destination.

        composable(Routes.PARENT_PROFILE) {
            ProfileScreen(navController)
        }

        composable(Routes.BHW_PROFILE) {
            ProfileScreen(navController)
        }

        // ================= CHILD MANAGEMENT =================

        composable(Routes.CREATE_CHILD) {
            CreateChildProfileScreen(navController)
        }

        composable(
            route = Routes.SEND_ALERT,
            arguments = listOf(
                navArgument("childId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            SendStatusAlertScreen(
                navController = navController,
                preselectedChildId = backStackEntry.arguments?.getString("childId")
            )
        }

        // ================= VIEW CHILD =================

        composable(
            route     = Routes.VIEW_CHILD,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            ViewChildProfileScreen(navController, childId)
        }

        // ================= UPDATE CHILD =================

        composable(
            route     = Routes.UPDATE_CHILD,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            UpdateChildProfileScreen(navController, childId)
        }

        // ================= CHILD HISTORY =================

        composable(
            route     = Routes.CHILD_HISTORY,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            ChildHistoryScreen(navController, childId)
        }

        composable(Routes.EVIDENCE_PREVIEW) {
            val imageUrl = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("evidence_url")
                ?: return@composable
            EvidencePreviewScreen(navController, imageUrl)
        }

        // ================= DELETE CHILD =================

        composable(
            route     = Routes.DELETE_CHILD,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
            DeleteChildProfileScreen(navController, childId)
        }
    }
}
