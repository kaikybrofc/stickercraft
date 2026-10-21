package com.example.presentation.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ImageUtils
import com.example.data.local.StickerDatabase
import com.example.data.ml.AutoCutEngine
import com.example.data.repository.StickerRepository
import com.example.data.storage.StickerFileManager
import com.example.domain.model.CutoutMode
import com.example.domain.model.StickerPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StickerEditorUiState(
    val baseBitmap: Bitmap? = null,
    val processedBitmap: Bitmap? = null,
    val cutoutMode: CutoutMode = CutoutMode.ORIGINAL,
    val strokeWidth: Float = 10f,
    val strokeColor: Int = Color.WHITE,
    val stickerText: String = "NOVO STICKER",
    val textColor: Int = Color.WHITE,
    val textStrokeColor: Int = Color.BLACK,
    val textSizeSp: Float = 36f,
    val textYPercent: Float = 0.85f, // 0.0 top, 1.0 bottom
    val selectedAccessories: List<String> = emptyList(),
    val isProcessing: Boolean = false,
    val estimatedSizeBytes: Int = 38000,
    val availablePacks: List<StickerPack> = emptyList()
)

sealed class EditorEvent {
    data class ShowToast(val message: String) : EditorEvent()
    data class StickerSaved(val packId: String, val stickerId: String) : EditorEvent()
}

class StickerEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StickerDatabase.getInstance(application)
    private val fileManager = StickerFileManager(application)
    private val repository = StickerRepository(application, database.stickerDao(), fileManager)
    private val autoCutEngine = AutoCutEngine()

    private val _uiState = MutableStateFlow(StickerEditorUiState())
    val uiState: StateFlow<StickerEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    val userPacks: StateFlow<List<StickerPack>> = repository.userPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStarterTemplate()
    }

    fun loadStarterTemplate(templateName: String = "doge") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val bitmap = when (templateName) {
                "cat" -> fileManager.createSampleStickerBitmap("MIAU", "😺", 0xFF6366F1.toInt())
                "fire" -> fileManager.createSampleStickerBitmap("BRABO", "🔥", 0xFFEF4444.toInt())
                "star" -> fileManager.createSampleStickerBitmap("TOP", "⭐", 0xFFF59E0B.toInt())
                else -> fileManager.createSampleStickerBitmap("DOGE", "🐶", 0xFFF59E0B.toInt())
            }
            _uiState.value = _uiState.value.copy(
                baseBitmap = bitmap,
                processedBitmap = bitmap,
                isProcessing = false
            )
            renderFinalPreview()
        }
    }

    fun onImageSelectedFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val context = getApplication<Application>()
            val bitmap = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }

            if (bitmap != null) {
                _uiState.value = _uiState.value.copy(
                    baseBitmap = bitmap,
                    processedBitmap = bitmap,
                    cutoutMode = CutoutMode.ORIGINAL,
                    isProcessing = false
                )
                renderFinalPreview()
            } else {
                _uiState.value = _uiState.value.copy(isProcessing = false)
                _events.emit(EditorEvent.ShowToast("Não foi possível carregar a imagem selecionada."))
            }
        }
    }

    fun setCutoutMode(mode: CutoutMode) {
        val base = _uiState.value.baseBitmap ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, cutoutMode = mode)
            val processed = when (mode) {
                CutoutMode.ORIGINAL -> base
                CutoutMode.AUTO_CUT -> autoCutEngine.removeBackground(base)
                CutoutMode.CIRCLE_CROP -> autoCutEngine.cropCircle(base)
                CutoutMode.SQUARE_CROP -> autoCutEngine.cropSquare(base)
                CutoutMode.LASSO_FREE -> autoCutEngine.removeBackground(base)
            }
            _uiState.value = _uiState.value.copy(
                processedBitmap = processed,
                isProcessing = false
            )
            renderFinalPreview()
        }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(strokeWidth = width)
        renderFinalPreview()
    }

    fun setStrokeColor(color: Int) {
        _uiState.value = _uiState.value.copy(strokeColor = color)
        renderFinalPreview()
    }

    fun setText(text: String) {
        _uiState.value = _uiState.value.copy(stickerText = text)
        renderFinalPreview()
    }

    fun setTextColor(color: Int) {
        _uiState.value = _uiState.value.copy(textColor = color)
        renderFinalPreview()
    }

    fun setTextYPercent(percent: Float) {
        _uiState.value = _uiState.value.copy(textYPercent = percent)
        renderFinalPreview()
    }

    fun toggleAccessory(emoji: String) {
        val current = _uiState.value.selectedAccessories.toMutableList()
        if (current.contains(emoji)) {
            current.remove(emoji)
        } else {
            current.add(emoji)
        }
        _uiState.value = _uiState.value.copy(selectedAccessories = current)
        renderFinalPreview()
    }

    private fun renderFinalPreview() {
        val state = _uiState.value
        val source = state.processedBitmap ?: state.baseBitmap ?: return

        // Compute simulated compressed size
        viewModelScope.launch(Dispatchers.Default) {
            val formatted = ImageUtils.formatForWhatsAppSticker(source)
            val compressedBytes = ImageUtils.compressToWebpWithinLimit(formatted)
            _uiState.value = _uiState.value.copy(estimatedSizeBytes = compressedBytes.size)
        }
    }

    /**
     * Renders the complete layered sticker onto a final 512x512 Bitmap.
     */
    fun buildFinalStickerBitmap(): Bitmap {
        val state = _uiState.value
        val base = state.processedBitmap ?: state.baseBitmap ?: Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

        // 1. Add outline if requested
        val withOutline = if (state.strokeWidth > 0f) {
            ImageUtils.addStickerOutline(base, state.strokeWidth, state.strokeColor)
        } else {
            base
        }

        // 2. Format to exact 512x512
        val finalBitmap = ImageUtils.formatForWhatsAppSticker(withOutline)
        val canvas = Canvas(finalBitmap)

        // 3. Draw accessories
        if (state.selectedAccessories.isNotEmpty()) {
            val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 90f
                textAlign = Paint.Align.CENTER
            }
            state.selectedAccessories.forEachIndexed { index, emoji ->
                val x = 120f + (index * 130f)
                val y = 110f
                canvas.drawText(emoji, x, y, emojiPaint)
            }
        }

        // 4. Draw customized text
        if (state.stickerText.isNotBlank()) {
            val textPaintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = state.textSizeSp * 2.2f
                color = state.textStrokeColor
                style = Paint.Style.STROKE
                strokeWidth = 10f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val textPaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = state.textSizeSp * 2.2f
                color = state.textColor
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val x = 256f // center of 512
            val y = 512f * state.textYPercent

            canvas.drawText(state.stickerText, x, y, textPaintStroke)
            canvas.drawText(state.stickerText, x, y, textPaintFill)
        }

        return finalBitmap
    }

    fun saveStickerToPack(packId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val finalBitmap = buildFinalStickerBitmap()
            val emojis = if (_uiState.value.selectedAccessories.isNotEmpty()) {
                _uiState.value.selectedAccessories.take(3)
            } else {
                listOf("✨")
            }

            val sticker = repository.addStickerToPack(packId, finalBitmap, emojis)
            _uiState.value = _uiState.value.copy(isProcessing = false)
            _events.emit(EditorEvent.ShowToast("Figurinha salva no pacote com sucesso!"))
            _events.emit(EditorEvent.StickerSaved(packId, sticker.id))
        }
    }

    fun createNewPackAndSave(packName: String, publisher: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val finalBitmap = buildFinalStickerBitmap()
            val newPack = repository.createPack(packName, publisher, "Criadas por Mim", finalBitmap)
            val sticker = repository.addStickerToPack(newPack.id, finalBitmap, listOf("✨"))
            _uiState.value = _uiState.value.copy(isProcessing = false)
            _events.emit(EditorEvent.ShowToast("Pacote '$packName' criado e figurinha adicionada!"))
            _events.emit(EditorEvent.StickerSaved(newPack.id, sticker.id))
        }
    }
}
