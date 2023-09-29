package xyz.xfqlittlefan.winnitodo.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import xyz.xfqlittlefan.winnitodo.data.AppDatabase

val LocalAppDatabase =
    staticCompositionLocalOf<AppDatabase> { error("No AppDatabase found!") }

val LocalNavController =
    staticCompositionLocalOf<NavController> { error("No NavController found!") }
