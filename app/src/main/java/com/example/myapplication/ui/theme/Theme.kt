package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,

    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,

    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,

    background = BackGroundDark,
    surface = BackGroundDark,

)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,

//onBackground = Color(0xFF1C1B1F),
    //onSurface = Color(0xFF1C1B1F),
    //surface = Color(0xFFFFFBFE),
    background = BackGround,

    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onTertiary = OnTertiary,
    primaryContainer = PrimaryContainer,
    secondaryContainer = SecondaryContainer,
    tertiaryContainer = TertiaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    onTertiaryContainer = OnTertiaryContainer
)
val LightTitleColors = TitleColors(
    mythic = Mythic,
    legendary = Leggendary,
    epic = Epic,
    rare = Rare,
    superRare = SuperRare,
    nonCommon = NonCommon,
    common = Common
)

val DarkTitleColors = TitleColors(
    mythic = MythicDark,
    legendary = LeggendaryDark,
    epic = EpicDark,
    rare = RareDark,
    superRare = SuperRareDark,
    nonCommon = NonCommonDark,
    common = CommonDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val titleColors = if (darkTheme) DarkTitleColors else LightTitleColors

    androidx.compose.runtime.CompositionLocalProvider(
        LocalTitleColors provides titleColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}