package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Modello per i colori dei titoli
data class TitleColors(
    val mythic: Color,
    val legendary: Color,
    val epic: Color,
    val rare: Color,
    val superRare: Color,
    val nonCommon: Color,
    val common: Color
)

// Local per accedere ai TitleColors nel tema
val LocalTitleColors = staticCompositionLocalOf { LightTitleColors }


