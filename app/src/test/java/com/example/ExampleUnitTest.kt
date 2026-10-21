package com.example

import com.example.core.WhatsAppConstants
import com.example.domain.model.Sticker
import com.example.domain.model.StickerPack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun whatsappConstants_areCompliant() {
        assertEquals(512, WhatsAppConstants.STICKER_DIMENSION_PX)
        assertEquals(96, WhatsAppConstants.TRAY_IMAGE_DIMENSION_PX)
        assertEquals(16, WhatsAppConstants.STICKER_SAFETY_MARGIN_PX)
        assertEquals(100 * 1024, WhatsAppConstants.MAX_STICKER_FILE_SIZE_BYTES)
        assertEquals(50 * 1024, WhatsAppConstants.MAX_TRAY_FILE_SIZE_BYTES)
        assertEquals(3, WhatsAppConstants.MIN_STICKERS_PER_PACK)
        assertEquals(30, WhatsAppConstants.MAX_STICKERS_PER_PACK)
    }

    @Test
    fun stickerPackValidation_detectsUnderMinCount() {
        val pack = StickerPack(
            id = "test_pack",
            name = "Test",
            publisher = "Tester",
            trayImageFileName = "tray.webp",
            trayImagePath = "/path/tray.webp",
            stickers = listOf(
                Sticker("1", "test_pack", "s1.webp", "/path/s1.webp"),
                Sticker("2", "test_pack", "s2.webp", "/path/s2.webp")
            )
        )

        assertFalse(pack.canExportToWhatsApp)
        assertNotNull(pack.validationErrorMessage)
        assertTrue(pack.validationErrorMessage!!.contains("pelo menos 3"))
    }

    @Test
    fun stickerPackValidation_acceptsValidPack() {
        val stickers = (1..5).map {
            Sticker("$it", "test_pack", "s$it.webp", "/path/s$it.webp")
        }
        val pack = StickerPack(
            id = "test_pack",
            name = "Test",
            publisher = "Tester",
            trayImageFileName = "tray.webp",
            trayImagePath = "/path/tray.webp",
            stickers = stickers
        )

        assertTrue(pack.canExportToWhatsApp)
        assertNull(pack.validationErrorMessage)
    }
}
