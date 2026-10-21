package com.example.presentation.editor

import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CutoutMode
import com.example.domain.model.StickerPack
import com.example.presentation.components.CheckerboardBackground
import com.example.ui.theme.WhatsAppGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerEditorScreen(
    viewModel: StickerEditorViewModel,
    onNavigateToMyPacks: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPacks by viewModel.userPacks.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSaveModal by remember { mutableStateOf(false) }
    var showCreatePackDialog by remember { mutableStateOf(false) }

    var newPackName by remember { mutableStateOf("") }
    var newPackPublisher by remember { mutableStateOf("Eu") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelectedFromUri(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is EditorEvent.StickerSaved -> {
                    showSaveModal = false
                    showCreatePackDialog = false
                    onNavigateToMyPacks()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor de Figurinhas", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = { showSaveModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_sticker_top_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Source Image Selector & Starter templates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("import_gallery_button")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeria", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "doge" to "🐶 Doge",
                        "cat" to "😺 Gatinho",
                        "fire" to "🔥 Fogo",
                        "star" to "⭐ Estrela"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadStarterTemplate(key) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The 512x512 Interactive Sticker Canvas Preview
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                CheckerboardBackground(modifier = Modifier.fillMaxSize()) {
                    val previewBitmap = viewModel.buildFinalStickerBitmap()
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Preview da Figurinha",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )

                    if (uiState.isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Processando IA...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WhatsApp Technical Limits Pill
            val sizeKb = uiState.estimatedSizeBytes / 1024
            val isWithinLimit = uiState.estimatedSizeBytes <= 100 * 1024
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isWithinLimit) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isWithinLimit) Icons.Default.CheckCircle else Icons.Default.Crop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isWithinLimit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "WhatsApp: 512x512 • WEBP • ${sizeKb} KB / 100 KB • 16px margem",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWithinLimit) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tool Tabs (Recorte, Borda, Texto, Acessórios)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Recorte", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Borda", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.FormatPaint, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Texto", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Acessórios", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Text("👑", fontSize = 16.sp) }
                )
            }

            // Tab Panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> CutoutTabContent(uiState, viewModel)
                    1 -> BorderTabContent(uiState, viewModel)
                    2 -> TextTabContent(uiState, viewModel)
                    3 -> AccessoriesTabContent(uiState, viewModel)
                }
            }
        }
    }

    // Save To Pack BottomSheet
    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            title = { Text("Salvar Figurinha no Pacote", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Selecione o pacote onde deseja salvar esta figurinha:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (userPacks.isEmpty()) {
                        Text(
                            text = "Você ainda não criou nenhum pacote próprio. Crie um agora!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        userPacks.forEach { pack ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.saveStickerToPack(pack.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📦", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pack.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${pack.stickers.size} figurinhas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showCreatePackDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_new_pack_dialog_trigger")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Criar Novo Pacote")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSaveModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Create New Pack Dialog
    if (showCreatePackDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePackDialog = false },
            title = { Text("Novo Pacote de Figurinhas", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPackName,
                        onValueChange = { newPackName = it },
                        label = { Text("Nome do Pacote (ex: Meus Memes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPackPublisher,
                        onValueChange = { newPackPublisher = it },
                        label = { Text("Nome do Criador (ex: Kaiky)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Mínimo 3 figurinhas exigidas pelo WhatsApp para exportar.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPackName.isNotBlank() && newPackPublisher.isNotBlank()) {
                            viewModel.createNewPackAndSave(newPackName.trim(), newPackPublisher.trim())
                        }
                    },
                    enabled = newPackName.isNotBlank()
                ) {
                    Text("Criar e Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePackDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CutoutTabContent(uiState: StickerEditorUiState, viewModel: StickerEditorViewModel) {
    Column {
        Text("Modos de Recorte & IA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                CutoutMode.AUTO_CUT,
                CutoutMode.CIRCLE_CROP,
                CutoutMode.SQUARE_CROP,
                CutoutMode.ORIGINAL
            ).forEach { mode ->
                val isSelected = uiState.cutoutMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCutoutMode(mode) },
                    label = { Text("${mode.icon} ${mode.label}", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).testTag("cutout_chip_${mode.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "O modo 'Auto Cut (IA)' usa o algoritmo de segmentação ML Kit para detectar o objeto principal e remover o fundo automaticamente.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun BorderTabContent(uiState: StickerEditorUiState, viewModel: StickerEditorViewModel) {
    Column {
        Text("Borda Estilo Sticker WhatsApp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Espessura: ${uiState.strokeWidth.toInt()} px", fontSize = 13.sp)
            Spacer(modifier = Modifier.weight(1f))
            if (uiState.strokeWidth > 0f) {
                TextButton(onClick = { viewModel.setStrokeWidth(0f) }) {
                    Text("Sem Borda")
                }
            }
        }

        Slider(
            value = uiState.strokeWidth,
            onValueChange = { viewModel.setStrokeWidth(it) },
            valueRange = 0f..24f,
            steps = 11,
            modifier = Modifier.fillMaxWidth().testTag("border_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Cor da Borda:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val colors = listOf(
                AndroidColor.WHITE to "Branco",
                AndroidColor.YELLOW to "Amarelo",
                AndroidColor.CYAN to "Ciano",
                0xFFF43F5E.toInt() to "Rosa",
                AndroidColor.BLACK to "Preto"
            )

            colors.forEach { (colorInt, name) ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .border(
                            width = if (uiState.strokeColor == colorInt) 3.dp else 1.dp,
                            color = if (uiState.strokeColor == colorInt) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { viewModel.setStrokeColor(colorInt) },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.strokeColor == colorInt) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = name,
                            tint = if (colorInt == AndroidColor.WHITE) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextTabContent(uiState: StickerEditorUiState, viewModel: StickerEditorViewModel) {
    Column {
        Text("Texto da Figurinha", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.stickerText,
            onValueChange = { viewModel.setText(it) },
            label = { Text("Digite uma frase de meme...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("sticker_text_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Posição Vertical:", fontSize = 13.sp)
        Slider(
            value = uiState.textYPercent,
            onValueChange = { viewModel.setTextYPercent(it) },
            valueRange = 0.2f..0.95f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text("Cor do Texto:", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                AndroidColor.WHITE to "Branco",
                AndroidColor.YELLOW to "Amarelo",
                0xFF22C55E.toInt() to "Verde",
                0xFFEF4444.toInt() to "Vermelho"
            ).forEach { (colorInt, _) ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .border(2.dp, if (uiState.textColor == colorInt) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        .clickable { viewModel.setTextColor(colorInt) }
                )
            }
        }
    }
}

@Composable
private fun AccessoriesTabContent(uiState: StickerEditorUiState, viewModel: StickerEditorViewModel) {
    Column {
        Text("Acessórios & Emojis", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text("Toque para adicionar ou remover do sticker:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))

        val emojis = listOf(
            "🕶️", "👑", "🥳", "😂", "🔥", "💖", "☕", "💸", "🐱", "🚀", "🎉", "⚡"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            emojis.forEach { emoji ->
                val isSelected = uiState.selectedAccessories.contains(emoji)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.toggleAccessory(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 26.sp)
                }
            }
        }
    }
}
