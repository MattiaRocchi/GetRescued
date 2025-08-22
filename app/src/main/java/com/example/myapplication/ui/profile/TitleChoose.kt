package com.example.myapplication.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.database.TitleBadge
import io.ktor.websocket.Frame
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.ui.theme.Common
import com.example.myapplication.ui.theme.CommonDark
import com.example.myapplication.ui.theme.Epic
import com.example.myapplication.ui.theme.EpicDark
import com.example.myapplication.ui.theme.Leggendary
import com.example.myapplication.ui.theme.LeggendaryDark
import com.example.myapplication.ui.theme.Mythic
import com.example.myapplication.ui.theme.MythicDark
import com.example.myapplication.ui.theme.NonCommon
import com.example.myapplication.ui.theme.NonCommonDark
import com.example.myapplication.ui.theme.Rare
import com.example.myapplication.ui.theme.RareDark
import com.example.myapplication.ui.theme.SuperRare
import com.example.myapplication.ui.theme.SuperRareDark
import com.example.myapplication.ui.theme.UnpressableButtonDark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

@Composable
fun TitlePickerDialog(
    titles: List<TitleBadge>,
    activeTitleId: Int?,
    onDismiss: () -> Unit,
    onSelect: (TitleBadge) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},

        title = { Frame.Text("Scegli un titolo") },
        text = {
            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // limita l'altezza massima
                        .verticalScroll(rememberScrollState())
                    ){
                titles.forEach { title ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(title) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Button(
                            onClick = { onSelect(title) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = rarityToColor(title.rarity),
                                contentColor = if (title.id == activeTitleId) Color.White else Color.DarkGray
                            ),
                            border = if (title.id == activeTitleId) BorderStroke(2.dp, Color.Black) else null
                        ) {
                            Text(
                                text = title.name,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun rarityToColor(rarity: String): Color {
    val darkTheme = isSystemInDarkTheme()

    return when (rarity) {
        "Common" -> if (darkTheme) CommonDark else Common
        "Uncommon" -> if (darkTheme) NonCommonDark else NonCommon
        "Rare" -> if (darkTheme) RareDark else Rare
        "SuperRare" -> if (darkTheme) SuperRareDark else SuperRare
        "Epic" -> if (darkTheme) EpicDark else Epic
        "Mythic" -> if (darkTheme) MythicDark else Mythic
        "Legendary" -> if (darkTheme) LeggendaryDark else Leggendary
        else -> Color.Gray
    }
}




fun loadTitleBadgesFromRaw(context: Context): List<TitleBadge> {
    return try {
        val inputStream = context.resources.openRawResource(
            com.example.myapplication.R.raw.titles
        )
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<TitleBadge>>() {}.type
        Gson().fromJson<List<TitleBadge>>(reader, type)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}