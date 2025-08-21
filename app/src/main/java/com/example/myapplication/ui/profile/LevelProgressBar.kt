package com.example.myapplication.ui.profile

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.example.myapplication.ui.profile.LevelUtils.levelToTotalExp


@Composable
fun LevelProgressBar(exp: Int, modifier: Modifier = Modifier) {
    val level = LevelUtils.expToLevel(exp)
    val progress = LevelUtils.expProgress(exp)
    val color = getLevelColor(level)
    Column(modifier = modifier) {
        Text("Livello $level")
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),

            color = color
        )
        Text("${(exp)}/${levelToTotalExp(level)}")
    }
}
object LevelUtils {

    /**
     * Calcola l’exp totale necessaria per raggiungere il livello specificato.
     * Esempio: per livello 3 ritorna exp necessaria per arrivare dal 1 → 3.
     */
    fun levelToTotalExp(level: Int): Int {
        var total = 0
        for (n in 1..level) {
            total += 40 * n * n
        }
        return total
    }

    /**
     * Ritorna il livello corrispondente all’exp totale accumulata.
     */
    fun expToLevel(exp: Int): Int {
        var level = 0
        var requiredExp: Int
        var total = 0

        do {
            level++
            requiredExp = 40 * level * level
            total += requiredExp
        } while (exp >= total)

        return level
    }

    /**
     * Restituisce la percentuale di avanzamento (0f..1f) verso il prossimo livello.
     */
    fun expProgress(exp: Int): Float {
        val currentLevel = expToLevel(exp)
        val expForCurrentLevel = levelToTotalExp(currentLevel - 1)
        val expForNextLevel = levelToTotalExp(currentLevel)

        return (exp - expForCurrentLevel).toFloat() /
                (expForNextLevel - expForCurrentLevel).toFloat()
    }
}
fun getLevelColor(level: Int): Color {
    val checkpoints = listOf(
        1 to Color(0xFF4CAF50),   // Verde
        5 to Color(0xFFFFEB3B),   // Giallo
        9 to Color(0xFFFF9800),   // Arancione
        13 to Color(0xFFF44336),  // Rosso
        17 to Color(0xFF9C27B0)   // Viola
    )

    // Trova due checkpoint fra cui si trova il livello attuale
    val (startLevel, startColor) = checkpoints.last { it.first <= level }
    val (endLevel, endColor) = checkpoints.firstOrNull { it.first > level }
        ?: checkpoints.last()

    // Se siamo oltre l'ultimo checkpoint → colore fisso
    when {
        (level >= endLevel) ->
            return endColor
        else -> {
            // Interpolazione lineare tra i due colori
            val fraction = (level - startLevel).toFloat() / (endLevel - startLevel).toFloat()
            return lerp(startColor, endColor, fraction.coerceIn(0f, 1f))
        }
    }


}