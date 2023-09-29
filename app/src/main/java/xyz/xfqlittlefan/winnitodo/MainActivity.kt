package xyz.xfqlittlefan.winnitodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.xfqlittlefan.winnitodo.data.AppDatabase
import xyz.xfqlittlefan.winnitodo.ui.LocalAppDatabase
import xyz.xfqlittlefan.winnitodo.ui.LocalNavController
import xyz.xfqlittlefan.winnitodo.ui.pages.HomePage
import xyz.xfqlittlefan.winnitodo.ui.theme.WinniTODOTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDatabase = AppDatabase.getInstance(this)
        setContent {
            val navController = rememberNavController()

            WinniTODOTheme {
                CompositionLocalProvider(
                    LocalAppDatabase provides appDatabase,
                    LocalNavController provides navController
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                    ) {
                        composable(route = "home") { HomePage() }
                    }
                }
            }
        }
    }
}
