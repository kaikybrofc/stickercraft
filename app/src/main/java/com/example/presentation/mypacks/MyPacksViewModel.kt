package com.example.presentation.mypacks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StickerDatabase
import com.example.data.repository.StickerRepository
import com.example.data.storage.StickerFileManager
import com.example.data.whatsapp.WhatsAppExporter
import com.example.domain.model.StickerPack
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class MyPacksEvent {
    data class ShowToast(val message: String) : MyPacksEvent()
    data class LaunchWhatsAppIntent(val pack: StickerPack, val isBusiness: Boolean = false) : MyPacksEvent()
}

class MyPacksViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StickerDatabase.getInstance(application)
    private val fileManager = StickerFileManager(application)
    private val repository = StickerRepository(application, database.stickerDao(), fileManager)
    val exporter = WhatsAppExporter(application)

    val packs: StateFlow<List<StickerPack>> = repository.userPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<MyPacksEvent>()
    val events: SharedFlow<MyPacksEvent> = _events.asSharedFlow()

    fun createNewPack(name: String, publisher: String) {
        viewModelScope.launch {
            repository.createPack(name.trim(), publisher.trim(), "Geral")
            _events.emit(MyPacksEvent.ShowToast("Pacote '$name' criado! Crie figurinhas para adicioná-las."))
        }
    }

    fun deletePack(packId: String) {
        viewModelScope.launch {
            repository.deletePack(packId)
            _events.emit(MyPacksEvent.ShowToast("Pacote removido."))
        }
    }

    fun exportToWhatsApp(pack: StickerPack, isBusiness: Boolean = false) {
        val error = exporter.validatePack(pack)
        if (error != null) {
            viewModelScope.launch {
                _events.emit(MyPacksEvent.ShowToast(error))
            }
            return
        }

        viewModelScope.launch {
            _events.emit(MyPacksEvent.LaunchWhatsAppIntent(pack, isBusiness))
        }
    }
}
