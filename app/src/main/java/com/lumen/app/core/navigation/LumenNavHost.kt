package com.lumen.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lumen.app.ui.businesshours.BusinessHoursScreen
import com.lumen.app.ui.documentdetail.DocumentDetailScreen
import com.lumen.app.ui.home.HomeScreen
import com.lumen.app.ui.notebook.NotebookListScreen
import com.lumen.app.ui.notedetail.NoteDetailScreen
import com.lumen.app.ui.receipt.ReceiptDetailScreen
import com.lumen.app.ui.scan.ScanScreen
import com.lumen.app.ui.search.SearchScreen
import com.lumen.app.ui.settings.SettingsScreen
import com.lumen.app.ui.table.TableDetailScreen

@Composable
fun LumenNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onQuickScan = { navController.navigate(Screen.NotebookList.route) },
                onOpenSearch = { navController.navigate(Screen.Search.route) },
                onOpenDocument = { navController.navigate(Screen.DocumentDetail.buildRoute(it)) },
                onOpenNote = { navController.navigate(Screen.NoteDetail.buildRoute(it)) },
                onOpenReceipt = { navController.navigate(Screen.ReceiptDetail.buildRoute(it)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.NotebookList.route) {
            NotebookListScreen(
                onOpenNotebook = { notebookId -> navController.navigate("scan/$notebookId") },
                onBack = { navController.popBackStack() },
            )
        }

        composable("scan/{notebookId}") { backStackEntry ->
            val notebookId = backStackEntry.arguments?.getString("notebookId") ?: return@composable
            ScanScreen(
                notebookId = notebookId,
                onScanComplete = { documentId -> navController.navigate(Screen.DocumentDetail.buildRoute(documentId)) { popUpTo(Screen.Home.route) } },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.DocumentDetail.route) {
            DocumentDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.NoteDetail.route) {
            NoteDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ReceiptDetail.route) {
            ReceiptDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.TableDetail.route) {
            TableDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenNote = { navController.navigate(Screen.NoteDetail.buildRoute(it)) },
                onOpenDocument = { navController.navigate(Screen.DocumentDetail.buildRoute(it)) },
                onOpenReceipt = { navController.navigate(Screen.ReceiptDetail.buildRoute(it)) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenBusinessHours = { navController.navigate(Screen.BusinessHours.route) },
            )
        }

        composable(Screen.BusinessHours.route) {
            BusinessHoursScreen(onBack = { navController.popBackStack() })
        }
    }
}
