package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

// Task colors (mantenuti uguali)
val EasyTask = Color(0xFF4C7744)
val MediumTask = Color(0xFF9E8C3C)
val DifficulTask = Color(0xFF8C3C3C)

// LIGHT THEME COLORS - Versione più morbida
val BackGround = Color(0xFFFFFFFF)
val Primary = Color(0xFF1E88E5) // Blu leggermente più tenue
val OnPrimary = Color(0xFFFFFFFF) // Bianco per contrasto ottimale
val PrimaryContainer = Color(0xFFE3F2FD) // Container blu chiaro
val OnPrimaryContainer = Color(0xFF0D47A1) // Blu scuro per testo su container

val Secondary = Color(0xFF00796B) // Verde teal leggermente più morbido
val OnSecondary = Color(0xFFFFFFFF) // Bianco per contrasto
val SecondaryContainer = Color(0xFFB2DFDB) // Container verde chiaro (mantenuto)
val OnSecondaryContainer = Color(0xFF004D40) // Verde scuro per testo

val Tertiary = Color(0xFF8E24AA) // Viola leggermente più chiaro e morbido
val OnTertiary = Color(0xFFFFFFFF) // Bianco per contrasto
val TertiaryContainer = Color(0xFFE1BEE7) // Container viola chiaro
val OnTertiaryContainer = Color(0xFF4A148C) // Viola scuro per testo

val UnpressableButton = Color(0xFFBDBDBD) // Mantenuto

// DARK THEME COLORS - Migliorati
val EasyTaskLogo = Color(
 red = (0x4C / 255f * 1.1f).coerceAtMost(1f),
 green = (0x77 / 255f * 1.6f).coerceAtMost(1f),
 blue = (0x44 / 255f)
)
val MediumTaskLogo = Color(
 red = (158 / 255f * 1.12f).coerceAtMost(1f),
 green = (140 / 255f * 1.4f).coerceAtMost(1f),
 blue = (60 / 255f * 1.05f).coerceAtMost(1f)
)
val DifficulTaskLogo = Color(
 red = (140 / 255f * 1.3f).coerceAtMost(1f),
 green = (60 / 255f * 1.8f).coerceAtMost(1f),
 blue = (60 / 255f)
)

val BackGroundDark = Color(0xFF121212) // Mantenuto
val PrimaryDark = Color(0xFF42A5F5) // Blu più chiaro e vivace per dark theme
val OnPrimaryDark = Color(0xFF000000) // Nero per contrasto ottimale
val PrimaryContainerDark = Color(0xFF1565C0) // Container blu scuro
val OnPrimaryContainerDark = Color(0xFFE3F2FD) // Blu chiaro per testo

val SecondaryDark = Color(0xFF26A69A) // Teal più vivace
val OnSecondaryDark = Color(0xFF000000) // Nero per contrasto
val SecondaryContainerDark = Color(0xFF00695C) // Container verde scuro
val OnSecondaryContainerDark = Color(0xFFB2DFDB) // Verde chiaro per testo

val TertiaryDark = Color(0xFFBA68C8) // Viola/Purple chiaro per dark theme
val OnTertiaryDark = Color(0xFF000000) // Nero per contrasto
val TertiaryContainerDark = Color(0xFF7B1FA2) // Container viola scuro
val OnTertiaryContainerDark = Color(0xFFE1BEE7) // Viola chiaro per testo

val UnpressableButtonDark = Color(0xFF757575) // Mantenuto

// Rarity colors (mantenuti uguali)
val Mythic = Color(0xFFFE0000)
val MythicDark = Color(0xFFB71C1C)
val Leggendary = Color(0xFFFFC501)
val LeggendaryDark = Color(0xFFFFA000)
val Epic = Color(0xFFFF40FF)
val EpicDark = Color(0xFF9C27B0)
val Rare = Color(0xFF0088FE)
val RareDark = Color(0xFF1976D2)
val SuperRare = Color(0xFF1EF009)
val SuperRareDark = Color(0xFF00C853)
val NonCommon = Color(0xFF10F7FF)
val NonCommonDark = Color(0xFF00B8D4)
val Common = Color(0xFFD7D9D4)
val CommonDark = Color(0xFFBDBDBD)