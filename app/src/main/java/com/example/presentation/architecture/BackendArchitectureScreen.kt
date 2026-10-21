package com.example.presentation.architecture

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendArchitectureScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arquitetura & Backend", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Section Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "🗄️ Esquema DB (SQL)",
                    1 to "🚀 Backend REST (FastAPI)",
                    2 to "📱 Clean Architecture",
                    3 to "💬 Especificação WhatsApp"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("arch_tab_$idx")
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> DatabaseSchemaView()
                    1 -> BackendApiView()
                    2 -> CleanArchitectureView()
                    3 -> WhatsAppSpecView()
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DatabaseSchemaView() {
    Column {
        Text("Modelagem Relacional (PostgreSQL / MySQL)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tabelas principais projetadas para alto volume, busca com índice full-text e cache no Redis.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "1. Tabela 'users'",
            code = """
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "2. Tabela 'sticker_packs'",
            code = """
CREATE TABLE sticker_packs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    publisher VARCHAR(128) NOT NULL,
    tray_image_url TEXT NOT NULL,          -- 96x96 WEBP (<50KB)
    category VARCHAR(50) DEFAULT 'General',
    tags TEXT[] DEFAULT '{}',
    is_animated BOOLEAN DEFAULT FALSE,
    downloads_count INT DEFAULT 0,
    likes_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_pack_name_length CHECK (char_length(name) >= 3)
);

CREATE INDEX idx_packs_category ON sticker_packs(category);
CREATE INDEX idx_packs_downloads ON sticker_packs(downloads_count DESC);
CREATE INDEX idx_packs_tags ON sticker_packs USING GIN(tags);
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "3. Tabela 'stickers'",
            code = """
CREATE TABLE stickers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pack_id UUID NOT NULL REFERENCES sticker_packs(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,               -- 512x512 WEBP (<100KB)
    emojis VARCHAR(10)[] NOT NULL,         -- Até 3 emojis por figurinha
    file_size_bytes INT NOT NULL,          -- Exato tamanho (<102400)
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_sticker_weight CHECK (file_size_bytes <= 102400)
);

CREATE INDEX idx_stickers_pack_id ON stickers(pack_id);
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "4. Tabela 'pack_downloads'",
            code = """
CREATE TABLE pack_downloads (
    id BIGSERIAL PRIMARY KEY,
    pack_id UUID NOT NULL REFERENCES sticker_packs(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    device_id VARCHAR(100),
    downloaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_downloads_pack ON pack_downloads(pack_id);
            """.trimIndent()
        )
    }
}

@Composable
private fun BackendApiView() {
    Column {
        Text("Arquitetura da API REST (FastAPI / Node.js)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Microserviço em Python (FastAPI) com worker Celery/Pillow para processar upload e converter para WebP 512x512.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "Endpoints REST Principais",
            code = """
GET  /api/v1/packs/trending      -> Lista pacotes em alta (cache Redis 15m)
GET  /api/v1/packs/search?q=meme -> Busca por nome, criador ou tags
GET  /api/v1/packs/{id}          -> Detalhes completos + URLs 512x512 WEBP
POST /api/v1/packs/create        -> Cria novo pacote (JWT Auth)
POST /api/v1/packs/{id}/stickers -> Upload multipart/form-data
POST /api/v1/packs/{id}/download -> Incrementa métricas de download
            """.trimIndent()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CodeCard(
            title = "Pipeline de Conversão de Mídia (S3 / Storage)",
            code = """
def process_sticker_media(input_bytes: bytes) -> bytes:
    image = Image.open(io.BytesIO(input_bytes)).convert("RGBA")
    
    # Redimensiona para caber em 480x480 com proporção
    image.thumbnail((480, 480), Image.Resampling.LANCZOS)
    
    # Cria canvas 512x512 transparente
    canvas = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    offset = ((512 - image.width) // 2, (512 - image.height) // 2)
    canvas.paste(image, offset, mask=image)
    
    # Comprime em WEBP com limite < 100KB
    output = io.BytesIO()
    canvas.save(output, format="WEBP", quality=90, method=6)
    return output.getvalue()
            """.trimIndent()
        )
    }
}

@Composable
private fun CleanArchitectureView() {
    Column {
        Text("Estrutura do Projeto Android (Clean Architecture)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        CodeCard(
            title = "Árvore de Pastas & Camadas",
            code = """
app/src/main/java/com/example/
├── core/
│   ├── WhatsAppConstants.kt      # Constantes oficiais, intents e regras
│   └── ImageUtils.kt             # Formatação 512x512, margem e WEBP
│
├── domain/                       # Regras de Negócio Puras (sem Android SDK)
│   ├── model/
│   │   ├── Sticker.kt            # Entidade de domínio
│   │   ├── StickerPack.kt        # Pacote com validações 3..30
│   │   └── EditorOverlay.kt      # Textos, Acessórios e Recortes
│   └── repository/
│       └── IStickerRepository.kt # Contratos das operações
│
├── data/                         # Acesso a Dados & Implementação Técnica
│   ├── local/                    # Persistência Room (SQLite local)
│   │   ├── StickerEntity.kt
│   │   ├── StickerPackEntity.kt
│   │   ├── StickerDao.kt
│   │   └── StickerDatabase.kt
│   ├── storage/
│   │   └── StickerFileManager.kt # Salva arquivos .webp no filesDir
│   ├── provider/
│   │   └── StickerContentProvider.kt # Provider oficial WhatsApp
│   ├── ml/
│   │   └── AutoCutEngine.kt      # Segmentação ML Kit (Auto Cut)
│   └── whatsapp/
│       └── WhatsAppExporter.kt   # Dispatcher de Intent do WhatsApp
│
└── presentation/                 # Interface do Usuário (Jetpack Compose)
    ├── theme/                    # Material 3 Design System
    ├── components/               # Checkerboard, WhatsAppButton, etc.
    ├── feed/                     # Feed da comunidade
    ├── packdetail/               # Detalhes e inspeção do pacote
    ├── editor/                   # Editor visual (IA, Borda, Texto)
    └── mypacks/                  # Gerenciador de pacotes do usuário
            """.trimIndent()
        )
    }
}

@Composable
private fun WhatsAppSpecView() {
    Column {
        Text("Especificação Oficial de Figurinhas do WhatsApp", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        CodeCard(
            title = "Regras Obrigatórias do WhatsApp",
            code = """
1. Dimensões Exatas: 512 x 512 pixels.
2. Margem de Segurança: Margem transparente de 16 pixels nas 4 bordas (conteúdo máximo: 480x480).
3. Formato de Arquivo: WEBP (RGBA para estático, transparente).
4. Tamanho Máximo do Sticker: Menor que 100 KB por arquivo.
5. Tamanho do Ícone da Bandeja (Tray Icon): Exatos 96 x 96 pixels, WEBP/PNG, < 50 KB.
6. Quantidade de Figurinhas por Pacote: Mínimo 3 e Máximo 30 figurinhas.
7. ContentProvider Obrigatório: Deve implementar 'metadata', 'stickers/<id>' e 'stickers_asset/<id>/<file>'.
8. Intent de Adição: Ação 'com.whatsapp.intent.action.ENABLE_STICKER_PACK' com ID, Autoridade e Nome.
            """.trimIndent()
        )
    }
}

@Composable
private fun CodeCard(title: String, code: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
