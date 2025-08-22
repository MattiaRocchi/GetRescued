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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
            Column {
                titles.forEach { title ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(title) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = title.id == activeTitleId,
                            onClick = { onSelect(title) }
                        )
                        Text(title.name)
                    }
                }
            }
        }
    )
}

fun loadTitlesFromJson(context: Context): List<TitleBadge> {
    val json = context.assets.open("titles.json")
        .bufferedReader()
        .use { it.readText() }

    val type = object : TypeToken<List<TitleBadge>>() {}.type
    return Gson().fromJson(json, type)
}