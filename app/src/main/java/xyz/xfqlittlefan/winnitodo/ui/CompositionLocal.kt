package xyz.xfqlittlefan.winnitodo.ui

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppViewModel =
    staticCompositionLocalOf<AppViewModel> { error("No AppViewModel found!") }
