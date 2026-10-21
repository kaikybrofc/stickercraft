package com.example.presentation.packdetail

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.WhatsAppConstants
import com.example.data.local.StickerDatabase
import com.example.data.repository.StickerRepository
import com.example.data.storage.StickerFileManager
import com.example.data.whatsapp.WhatsAppExporter
import com.example.domain.model.Sticker
import com.example.domain.model.StickerPack
import com.example.presentation.components.CheckerboardBackground
import com.example.presentation.components.WhatsAppAddButton
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackDetailScreen(
    packId: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { StickerDatabase.getInstance(context) }
    val fileManager = remember { StickerFileManager(context) }
    val repository = remember { StickerRepository(context, database.stickerDao(), fileManager) }
    val exporter = remember { WhatsAppExporter(context) }

    var pack by remember { mutableStateOf<StickerPack?>(null) }
    var selectedStickerForDialog by remember { mutableStateOf<Sticker?>(null) }

    LaunchedEffect(packId) {
        pack = repository.getPackById(packId)
    }

    fun exportToWhatsApp(isBusiness: Boolean) {
        val currentPack = pack ?: return
        val error = exporter.validatePack(currentPack)
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = exporter.createAddStickerPackIntent(currentPack, isBusiness)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isBusiness) "WhatsApp Business não encontrado neste aparelho." else "WhatsApp não encontrado neste aparelho.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pack?.name ?: "Detalhes do Pacote", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        val currentPack = pack
        if (currentPack == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Carregando pacote...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = File(currentPack.trayImagePath),
                                    contentDescription = "Ícone da Bandeja",
                                    modifier = Modifier.size(48.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = currentPack.name,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Criado por ${currentPack.publisher}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${currentPack.stickers.size} figurinhas • WhatsApp 512x512 WEBP",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WhatsAppAddButton(
                                modifier = Modifier.weight(1f),
                                isBusiness = false,
                                enabled = currentPack.canExportToWhatsApp,
                                onClick = { exportToWhatsApp(isBusiness = false) }
                            )

                            OutlinedButton(
                                onClick = { exportToWhatsApp(isBusiness = true) },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("WA Business", fontSize = 13.sp)
                            }
                        }

                        if (!currentPack.canExportToWhatsApp) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentPack.validationErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Grid of Stickers
                Text(
                    text = "Figurinhas do Pacote (${currentPack.stickers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentPack.stickers, key = { it.id }) { sticker ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedStickerForDialog = sticker }
                        ) {
                            CheckerboardBackground(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = File(sticker.filePath),
                                    contentDescription = "Figurinha",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sticker Details Dialog
    selectedStickerForDialog?.let { sticker ->
        AlertDialog(
            onDismissRequest = { selectedStickerForDialog = null },
            title = { Text("Especificações WhatsApp", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        CheckerboardBackground(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = File(sticker.filePath),
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text("• Resolução: 512 x 512 px (16px margem)", fontSize = 13.sp)
                        Text("• Formato: WEBP transparente", fontSize = 13.sp)
                        Text("• Tamanho: ${sticker.fileSizeBytes / 1024} KB (< 100 KB)", fontSize = 13.sp)
                        Text("• Arquivo: ${sticker.fileName}", fontSize = 13.sp)
                        if (sticker.emojis.isNotEmpty()) {
                            Text("• Emojis associados: ${sticker.emojis.joinToString(" ")}", fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedStickerForDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}
