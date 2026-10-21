package com.example.domain.model

data class StickerPack(
    val id: String,                         // Unique identifier (UUID or slug)
    val name: String,                       // Display title
    val publisher: String,                  // Author / Creator
    val trayImageFileName: String,          // e.g. "tray_icon.webp" (96x96)
    val trayImagePath: String,              // Local path
    val publisherEmail: String = "",
    val publisherWebsite: String = "",
    val privacyPolicyWebsite: String = "",
    val licenseAgreementWebsite: String = "",
    val imageDataVersion: String = "1",
    val avoidCache: Boolean = false,
    val isAnimated: Boolean = false,
    val stickers: List<Sticker> = emptyList(),
    val isUserCreated: Boolean = false,     // Created locally by user
    val category: String = "Geral",
    val downloadsCount: Int = 0,
    val likesCount: Int = 0,
    val tags: List<String> = emptyList()
) {
    val canExportToWhatsApp: Boolean
        get() = stickers.size in 3..30

    val validationErrorMessage: String?
        get() = when {
            stickers.size < 3 -> "Adicione pelo menos 3 figurinhas para exportar (atual: ${stickers.size})."
            stickers.size > 30 -> "O WhatsApp permite no máximo 30 figurinhas por pacote (atual: ${stickers.size})."
            else -> null
        }
}
