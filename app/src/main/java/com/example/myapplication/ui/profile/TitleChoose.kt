package com.example.myapplication.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.database.TitleBadge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.myapplication.data.database.Tags
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





// TitlePickerDialog completamente rinnovato e più carino
@Composable
fun TitlePickerDialog(
    titles: List<TitleBadge>,
    activeTitleId: Int?,
    onDismiss: () -> Unit,
    onSelect: (TitleBadge) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header elegante
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scegli un titolo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Bottone chiudi elegante
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista dei titoli con design migliorato
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(titles) { title ->
                        val isActive = title.id == activeTitleId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(title) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = rarityToColor(title.rarity).copy(alpha = 0.1f)
                            ),
                            border = if (isActive) {
                                BorderStroke(3.dp, rarityToColor(title.rarity))
                            } else {
                                BorderStroke(1.dp, rarityToColor(title.rarity).copy(alpha = 0.3f))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Indicatore di rarità
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            rarityToColor(title.rarity),
                                            CircleShape
                                        )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Testo del titolo
                                Text(
                                    text = title.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) {
                                        rarityToColor(title.rarity)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                // Icona di selezione
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selezionato",
                                        tint = rarityToColor(title.rarity),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    // Placeholder per mantenere allineamento
                                    Spacer(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sezione info (opzionale)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "💡 Tocca un titolo per selezionarlo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



@Composable
fun TagPickerDialog(
    tags: List<Tags>,
    selectedTagIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    var selection by remember { mutableStateOf(selectedTagIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(selection); onDismiss() }
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        },
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleziona i tag")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${selection.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = selection.contains(tag.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selection = if (isSelected) {
                                    selection - tag.id
                                } else {
                                    selection + tag.id
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                selection = if (it) {
                                    selection + tag.id
                                } else {
                                    selection - tag.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tag.name,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
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




