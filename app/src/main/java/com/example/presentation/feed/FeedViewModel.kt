package com.example.presentation.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StickerDatabase
import com.example.data.repository.StickerRepository
import com.example.data.storage.StickerFileManager
import com.example.data.whatsapp.WhatsAppExporter
import com.example.domain.model.Category
import com.example.domain.model.StickerPack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedUiState(
    val packs: List<StickerPack> = emptyList(),
    val filteredPacks: List<StickerPack> = emptyList(),
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

sealed class FeedEvent {
    data class ShowToast(val message: String) : FeedEvent()
    data class LaunchWhatsAppIntent(val pack: StickerPack, val isBusiness: Boolean = false) : FeedEvent()
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StickerDatabase.getInstance(application)
    private val fileManager = StickerFileManager(application)
    private val repository = StickerRepository(application, database.stickerDao(), fileManager)
    val exporter = WhatsAppExporter(application)

    private val _selectedCategory = MutableStateFlow("all")
    private val _searchQuery = MutableStateFlow("")

    private val _events = MutableSharedFlow<FeedEvent>()
    val events: SharedFlow<FeedEvent> = _events.asSharedFlow()

    val uiState: StateFlow<FeedUiState> = combine(
        repository.communityPacks,
        _selectedCategory,
        _searchQuery
    ) { packs, category, query ->
        val filtered = packs.filter { pack ->
            val matchesCategory = when (category) {
                "all" -> true
                "trending" -> pack.downloadsCount > 5000
                "memes" -> pack.category.equals("Memes", ignoreCase = true)
                "reactions" -> pack.category.equals("Reações", ignoreCase = true)
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                pack.name.contains(query, ignoreCase = true) ||
                pack.publisher.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }

        FeedUiState(
            packs = packs,
            filteredPacks = filtered,
            selectedCategory = category,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeedUiState(isLoading = true))

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded()
        }
    }

    fun onCategorySelected(categoryId: String) {
        _selectedCategory.value = categoryId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onExportPackToWhatsApp(pack: StickerPack, isBusiness: Boolean = false) {
        val error = exporter.validatePack(pack)
        if (error != null) {
            viewModelScope.launch {
                _events.emit(FeedEvent.ShowToast(error))
            }
            return
        }

        viewModelScope.launch {
            _events.emit(FeedEvent.LaunchWhatsAppIntent(pack, isBusiness))
        }
    }
}
