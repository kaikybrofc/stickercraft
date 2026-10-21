package com.example.presentation.mypacks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.domain.model.StickerPack
import com.example.presentation.components.CheckerboardBackground
import com.example.presentation.components.WhatsAppAddButton
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPacksScreen(
    viewModel: MyPacksViewModel,
    onPackClick: (String) -> Unit,
    onNavigateToEditor: () -> Unit
) {
    val packs by viewModel.packs.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var packToDelete by remember { mutableStateOf<StickerPack?>(null) }
    var newPackName by remember { mutableStateOf("") }
    var newPackAuthor by remember { mutableStateOf("Eu") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPacksEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MyPacksEvent.LaunchWhatsAppIntent -> {
                    val intent = viewModel.exporter.createAddStickerPackIntent(event.pack, event.isBusiness)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "WhatsApp não encontrado. Verifique se o aplicativo está instalado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Pacotes", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_pack_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Pacote")
            }
        }
    ) { padding ->
        if (packs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.FolderSpecial,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Nenhum pacote criado ainda", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Crie seu primeiro pacote personalizado para o WhatsApp!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Criar Novo Pacote")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(packs, key = { it.id }) { pack ->
                    MyPackItemCard(
                        pack = pack,
                        onPackClick = { onPackClick(pack.id) },
                        onExportToWhatsApp = { viewModel.exportToWhatsApp(pack, isBusiness = false) },
                        onDeleteClick = { packToDelete = pack }
                    )
                }
            }
        }
    }

    // Create Pack Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Novo Pacote de Figurinhas", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPackName,
                        onValueChange = { newPackName = it },
                        label = { Text("Nome do Pacote (ex: Amigos do Futebol)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_pack_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPackAuthor,
                        onValueChange = { newPackAuthor = it },
                        label = { Text("Autor (ex: Carlos)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_pack_author_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPackName.isNotBlank() && newPackAuthor.isNotBlank()) {
                            viewModel.createNewPack(newPackName, newPackAuthor)
                            newPackName = ""
                            showCreateDialog = false
                        }
                    },
                    enabled = newPackName.isNotBlank()
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete confirmation dialog
    packToDelete?.let { pack ->
        AlertDialog(
            onDismissRequest = { packToDelete = null },
            title = { Text("Excluir Pacote?") },
            text = { Text("Tem certeza que deseja excluir o pacote '${pack.name}' e todas as suas ${pack.stickers.size} figurinhas?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePack(pack.id)
                        packToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { packToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MyPackItemCard(
    pack: StickerPack,
    onPackClick: () -> Unit,
    onExportToWhatsApp: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPackClick() }
            .testTag("my_pack_card_${pack.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (pack.trayImagePath.isNotBlank()) {
                        AsyncImage(
                            model = File(pack.trayImagePath),
                            contentDescription = "Ícone",
                            modifier = Modifier.size(38.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("⭐", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Criado por ${pack.publisher}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir Pacote",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // WhatsApp 3-30 sticker compliance indicator
            val count = pack.stickers.size
            val progress = (count.toFloat() / 30f).coerceIn(0f, 1f)
            val isReady = count in 3..30

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isReady) "$count / 30 figurinhas (Pronto para o WhatsApp! 🎉)"
                    else "$count / 3 figurinhas mínimas (Faltam ${3 - count})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sticker Preview row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val previewStickers = pack.stickers.take(4)
                previewStickers.forEach { sticker ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        CheckerboardBackground(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = File(sticker.filePath),
                                contentDescription = "Figurinha",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
                for (i in 0 until (4 - previewStickers.size)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            WhatsAppAddButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = isReady,
                onClick = onExportToWhatsApp
            )
        }
    }
}
