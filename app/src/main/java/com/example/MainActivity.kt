package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.presentation.architecture.BackendArchitectureScreen
import com.example.presentation.editor.StickerEditorScreen
import com.example.presentation.editor.StickerEditorViewModel
import com.example.presentation.feed.FeedScreen
import com.example.presentation.feed.FeedViewModel
import com.example.presentation.mypacks.MyPacksScreen
import com.example.presentation.mypacks.MyPacksViewModel
import com.example.presentation.packdetail.PackDetailScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainNavDestination(val label: String) {
    FEED("Explorar"),
    EDITOR("Criar"),
    MY_PACKS("Meus Pacotes"),
    ARCHITECTURE("Arquitetura")
}

class MainActivity : ComponentActivity() {

    private val feedViewModel by viewModels<FeedViewModel>()
    private val editorViewModel by viewModels<StickerEditorViewModel>()
    private val myPacksViewModel by viewModels<MyPacksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(MainNavDestination.FEED) }
                var activePackDetailId by remember { mutableStateOf<String?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (activePackDetailId == null) {
                            NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                                NavigationBarItem(
                                    selected = currentTab == MainNavDestination.FEED,
                                    onClick = { currentTab = MainNavDestination.FEED },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainNavDestination.FEED) Icons.Filled.Explore else Icons.Outlined.Explore,
                                            contentDescription = "Explorar"
                                        )
                                    },
                                    label = { Text("Explorar") },
                                    modifier = Modifier.testTag("nav_item_feed")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainNavDestination.EDITOR,
                                    onClick = { currentTab = MainNavDestination.EDITOR },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainNavDestination.EDITOR) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                            contentDescription = "Criar"
                                        )
                                    },
                                    label = { Text("Criar") },
                                    modifier = Modifier.testTag("nav_item_editor")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainNavDestination.MY_PACKS,
                                    onClick = { currentTab = MainNavDestination.MY_PACKS },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainNavDestination.MY_PACKS) Icons.Filled.Folder else Icons.Outlined.Folder,
                                            contentDescription = "Meus Pacotes"
                                        )
                                    },
                                    label = { Text("Meus Pacotes") },
                                    modifier = Modifier.testTag("nav_item_my_packs")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainNavDestination.ARCHITECTURE,
                                    onClick = { currentTab = MainNavDestination.ARCHITECTURE },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainNavDestination.ARCHITECTURE) Icons.Filled.Layers else Icons.Outlined.Layers,
                                            contentDescription = "Arquitetura"
                                        )
                                    },
                                    label = { Text("Arquitetura") },
                                    modifier = Modifier.testTag("nav_item_architecture")
                                )
                            }
                        }
                    }
                ) { padding ->
                    if (activePackDetailId != null) {
                        PackDetailScreen(
                            packId = activePackDetailId!!,
                            onBack = { activePackDetailId = null }
                        )
                    } else {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            label = "tab_content"
                        ) { tab ->
                            when (tab) {
                                MainNavDestination.FEED -> {
                                    FeedScreen(
                                        viewModel = feedViewModel,
                                        onPackClick = { packId -> activePackDetailId = packId }
                                    )
                                }
                                MainNavDestination.EDITOR -> {
                                    StickerEditorScreen(
                                        viewModel = editorViewModel,
                                        onNavigateToMyPacks = { currentTab = MainNavDestination.MY_PACKS }
                                    )
                                }
                                MainNavDestination.MY_PACKS -> {
                                    MyPacksScreen(
                                        viewModel = myPacksViewModel,
                                        onPackClick = { packId -> activePackDetailId = packId },
                                        onNavigateToEditor = { currentTab = MainNavDestination.EDITOR }
                                    )
                                }
                                MainNavDestination.ARCHITECTURE -> {
                                    BackendArchitectureScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
