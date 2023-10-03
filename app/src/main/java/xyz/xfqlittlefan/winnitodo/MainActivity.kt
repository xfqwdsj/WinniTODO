package xyz.xfqlittlefan.winnitodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import xyz.xfqlittlefan.winnitodo.data.AppDatabase
import xyz.xfqlittlefan.winnitodo.ui.AppRoutes
import xyz.xfqlittlefan.winnitodo.ui.AppViewModel
import xyz.xfqlittlefan.winnitodo.ui.LocalAppViewModel
import xyz.xfqlittlefan.winnitodo.ui.pages.homePage
import xyz.xfqlittlefan.winnitodo.ui.pages.taskDetailsPage
import xyz.xfqlittlefan.winnitodo.ui.theme.WinniTODOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDatabase = AppDatabase.getInstance(this)
        setContent {
            val navController = rememberNavController()
            val viewModel = viewModel<AppViewModel>(
                factory = viewModelFactory {
                    addInitializer(AppViewModel::class) {
                        AppViewModel(appDatabase, navController)
                    }
                },
            )

            WinniTODOTheme {
                CompositionLocalProvider(LocalAppViewModel provides viewModel) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.default,
                    ) {
                        homePage()
                        taskDetailsPage()
                    }
                }
            }
        }
    }
}
