package com.example.domain.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String
) {
    companion object {
        val ALL = listOf(
            Category("all", "Todos", "🌟"),
            Category("trending", "Em Alta", "🔥"),
            Category("memes", "Memes", "😂"),
            Category("anime", "Anime & Games", "⚡"),
            Category("reactions", "Reações", "🐱"),
            Category("phrases", "Frases & Bom Dia", "💬"),
            Category("romantic", "Amor & Fofo", "💖")
        )
    }
}
