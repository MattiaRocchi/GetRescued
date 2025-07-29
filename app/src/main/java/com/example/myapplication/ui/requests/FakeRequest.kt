package com.example.myapplication.ui.requests

data class FakeRequest(
    val id: String,
    val title: String,
    val description: String,
    val status: String, // "available" o "in_progress"
    val difficulty: String // "easy", "medium", "hard"
)

object FakeRequestDataSource {
    val requests = listOf(
        FakeRequest(
            id = "1",
            title = "Riparazione rubinetto",
            description = "Perdita in cucina, urgente",
            status = "available",
            difficulty = "medium"
        ),
        FakeRequest(
            id = "2",
            title = "Aiuto trasloco",
            description = "Carico/scarico mobili",
            status = "in_progress",
            difficulty = "hard"
        ),
        FakeRequest(
            id = "3",
            title = "Lezioni di piano",
            description = "Per principiante adulto",
            status = "available",
            difficulty = "easy"
        )
    )
}