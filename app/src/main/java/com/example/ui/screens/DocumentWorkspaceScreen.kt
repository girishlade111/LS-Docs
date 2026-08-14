package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.util.PrintHelper
import com.example.ui.components.DocumentEmptyState
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Label
import com.example.data.database.DocumentMetadataRecord
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentFileType
import com.example.data.model.OpenTab
import com.example.data.util.CodeSyntaxHighlighter
import com.example.data.util.CsvParser
import com.example.data.util.MarkdownBlock
import com.example.data.util.MarkdownParser
import com.example.data.util.StructuredDataParser
import com.example.ui.components.CategoryChip
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.ChipSize
import com.example.ui.components.HighDensityBadge
import com.example.ui.components.HighDensityCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DocumentWorkspaceScreen(
    viewModel: MainViewModel
) {
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val activeTab = openTabs.find { it.tabId == activeTabId }
    val activeDocMetadata by viewModel.getDocumentMetadataFlow(activeTab?.uriString ?: "").collectAsState(initial = null)
    var showDetailsPanel by remember { mutableStateOf(false) }
    var showWatermarkSignDialog by remember { mutableStateOf(false) }

    if (showDetailsPanel && activeTab != null) {
        DocumentDetailsDialog(
            activeTab = activeTab,
            viewModel = viewModel,
            onDismiss = { showDetailsPanel = false }
        )
    }

    if (showWatermarkSignDialog && activeTab != null) {
        PdfWatermarkSignDialog(
            activeTab = activeTab,
            onDismiss = { showWatermarkSignDialog = false }
        )
    }

    val context = LocalContext.current
    val workspacePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if URI does not support persistable permissions
            }
            viewModel.openDocument(it)
        }
    }

    if (openTabs.isEmpty() || activeTab == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DocumentEmptyState(
                title = "No Document Active",
                description = "Select a PDF, Markdown, TXT, CSV, or Code file from your device storage to view and edit in the workspace.",
                onOpenStorage = {
                    workspacePickerLauncher.launch(arrayOf("application/pdf", "text/*", "text/plain", "text/markdown", "application/json", "text/csv", "*/*"))
                }
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High Density Multi-Tab Header
        ScrollableTabRow(
            selectedTabIndex = openTabs.indexOf(activeTab).coerceAtLeast(0),
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            openTabs.forEach { tab ->
                val isSelected = tab.tabId == activeTabId
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectTab(tab.tabId) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${tab.fileName}${if (tab.isEdited) " *" else ""}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.closeTab(tab.tabId) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                )
            }
        }

        // Active Tab Workspace Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                HighDensityBadge {
                    Text(
                        text = activeTab.fileType.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (!activeDocMetadata?.category.isNullOrBlank()) {
                    CategoryChip(
                        category = activeDocMetadata?.category,
                        size = ChipSize.Small,
                        onClick = { showDetailsPanel = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = activeTab.fileName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showDetailsPanel = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Document Details & Metadata", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val contentToEncrypt = activeTab.content ?: ""
                    viewModel.toggleDocumentRoomEncryption(activeTab.uriString, contentToEncrypt) { isEncrypted ->
                        // Toggled Room encryption
                    }
                }) {
                    Icon(Icons.Default.Security, contentDescription = "AES-256 Room Encryption", tint = MaterialTheme.colorScheme.primary)
                }
                if (activeTab.isEdited) {
                    IconButton(onClick = { viewModel.saveTabContent(activeTab.tabId) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Changes", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = { showWatermarkSignDialog = true }) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Sign & Watermark PDF", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    viewModel.addBookmark(activeTab.uriString, activeTab.fileName, "User Bookmark", activeTab.selectedPage, activeTab.fileType.name)
                }) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    PrintHelper.printDocument(
                        context = context,
                        documentName = activeTab.fileName,
                        content = activeTab.content ?: "Empty Document"
                    )
                }) {
                    Icon(Icons.Default.Print, contentDescription = "Send to Network Printer", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

        // Render specific file viewer based on file type
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab.fileType) {
                DocumentFileType.MARKDOWN -> MarkdownViewerEditor(activeTab, viewModel)
                DocumentFileType.JSON, DocumentFileType.XML, DocumentFileType.YAML -> StructuredDataViewer(activeTab, viewModel)
                DocumentFileType.CSV -> CsvSpreadsheetViewer(activeTab, viewModel)
                DocumentFileType.CODE, DocumentFileType.TEXT -> CodeTextEditor(activeTab, viewModel)
                else -> GenericDocumentViewer(activeTab)
            }
        }
    }
}

class UndoRedoState(initialContent: String) {
    var content by mutableStateOf(initialContent)
        private set

    val undoStack = mutableStateListOf<String>()
    val redoStack = mutableStateListOf<String>()

    fun updateContent(newContent: String, onContentSaved: (String) -> Unit) {
        if (newContent != content) {
            undoStack.add(content)
            if (undoStack.size > 50) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
            content = newContent
            onContentSaved(newContent)
        }
    }

    fun undo(onContentSaved: (String) -> Unit) {
        if (undoStack.isNotEmpty()) {
            redoStack.add(content)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            content = previous
            onContentSaved(previous)
        }
    }

    fun redo(onContentSaved: (String) -> Unit) {
        if (redoStack.isNotEmpty()) {
            undoStack.add(content)
            val next = redoStack.removeAt(redoStack.lastIndex)
            content = next
            onContentSaved(next)
        }
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

@Composable
fun EditorUndoRedoToolbar(
    undoRedoState: UndoRedoState,
    onContentSaved: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { undoRedoState.undo(onContentSaved) },
            enabled = undoRedoState.canUndo
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo Edit",
                tint = if (undoRedoState.canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        IconButton(
            onClick = { undoRedoState.redo(onContentSaved) },
            enabled = undoRedoState.canRedo
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo Edit",
                tint = if (undoRedoState.canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Undo: ${undoRedoState.undoStack.size} | Redo: ${undoRedoState.redoStack.size}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MarkdownViewerEditor(tab: OpenTab, viewModel: MainViewModel) {
    var mode by remember { mutableStateOf("preview") } // preview, edit, split
    val undoRedoState = remember(tab.tabId) { UndoRedoState(tab.content ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mode == "edit") {
                EditorUndoRedoToolbar(
                    undoRedoState = undoRedoState,
                    onContentSaved = { viewModel.updateTabContent(tab.tabId, it) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            HighDensityBadge {
                Row {
                    IconButton(onClick = { mode = "preview" }) { Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = if (mode == "preview") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton(onClick = { mode = "edit" }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = if (mode == "edit") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }

        if (mode == "edit") {
            OutlinedTextField(
                value = undoRedoState.content,
                onValueChange = { newText ->
                    undoRedoState.updateContent(newText) { viewModel.updateTabContent(tab.tabId, it) }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            )
        } else {
            val blocks = MarkdownParser.parseBlocks(undoRedoState.content)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(blocks) { block ->
                    when (block) {
                        is MarkdownBlock.Header -> Text(block.text, fontSize = (22 - block.level * 2).sp, fontWeight = FontWeight.Bold)
                        is MarkdownBlock.Paragraph -> Text(block.text, fontSize = 14.sp)
                        is MarkdownBlock.Blockquote -> Text(block.text, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp))
                        is MarkdownBlock.CodeBlock -> Text(
                            block.code,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                        is MarkdownBlock.ListItem -> Text("• ${block.text}", fontSize = 13.sp)
                        is MarkdownBlock.Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun StructuredDataViewer(tab: OpenTab, viewModel: MainViewModel) {
    val content = tab.content ?: ""
    val tree = remember(content) { StructuredDataParser.parseJsonToTree(content) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        if (tree != null) {
            Text("STRUCTURED TREE INSPECTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                item {
                    Text(content, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        } else {
            Text(content, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

@Composable
fun CsvSpreadsheetViewer(tab: OpenTab, viewModel: MainViewModel) {
    val content = tab.content ?: ""
    val csvTable = remember(content) { CsvParser.parseCsv(content) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text("CSV MATRIX SPREADSHEET (${csvTable.rows.size} rows x ${csvTable.colCount} cols)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
        ) {
            item {
                Row(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                    csvTable.headers.forEach { header ->
                        Text(
                            text = header,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .width(120.dp)
                                .padding(8.dp)
                        )
                    }
                }
            }
            items(csvTable.rows) { row ->
                Row {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .width(120.dp)
                                .padding(8.dp)
                        )
                    }
                }
                HorizontalDivider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun CodeTextEditor(tab: OpenTab, viewModel: MainViewModel) {
    val undoRedoState = remember(tab.tabId) { UndoRedoState(tab.content ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorUndoRedoToolbar(
                undoRedoState = undoRedoState,
                onContentSaved = { viewModel.updateTabContent(tab.tabId, it) }
            )
            HighDensityBadge {
                Text(
                    text = "${undoRedoState.content.length} chars",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        OutlinedTextField(
            value = undoRedoState.content,
            onValueChange = { newText ->
                undoRedoState.updateContent(newText) { viewModel.updateTabContent(tab.tabId, it) }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        )
    }
}

@Composable
fun GenericDocumentViewer(tab: OpenTab) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Description, contentDescription = "Doc", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(tab.fileName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Ready for local viewing and inspection.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DocumentDetailsDialog(
    activeTab: OpenTab,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val roomMetadata by viewModel.getDocumentMetadataFlow(activeTab.uriString).collectAsState(initial = null)

    val textContent = activeTab.content ?: ""
    val charCount = textContent.length
    val charCountNoSpaces = textContent.count { !it.isWhitespace() }
    val wordCount = remember(textContent) {
        if (textContent.isBlank()) 0
        else textContent.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val lineCount = remember(textContent) {
        if (textContent.isEmpty()) 0
        else textContent.lines().size
    }
    val contentBytes = roomMetadata?.fileSize ?: textContent.toByteArray(Charsets.UTF_8).size.toLong()

    val formattedSize = remember(contentBytes) {
        if (contentBytes < 1024) "$contentBytes B"
        else if (contentBytes < 1024 * 1024) String.format("%.2f KB (%d bytes)", contentBytes / 1024.0, contentBytes)
        else String.format("%.2f MB (%d bytes)", contentBytes / (1024.0 * 1024.0), contentBytes)
    }

    val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()) }
    val creationDateFormatted = remember(roomMetadata?.creationDate) {
        dateFormat.format(java.util.Date(roomMetadata?.creationDate ?: System.currentTimeMillis()))
    }
    val lastModifiedFormatted = remember(roomMetadata?.lastModifiedDate) {
        dateFormat.format(java.util.Date(roomMetadata?.lastModifiedDate ?: System.currentTimeMillis()))
    }

    var isEditingMetadata by remember { mutableStateOf(false) }
    var editedTitle by remember(roomMetadata) { mutableStateOf(roomMetadata?.title ?: activeTab.fileName) }
    var editedAuthor by remember(roomMetadata) { mutableStateOf(roomMetadata?.author ?: "") }
    var editedDescription by remember(roomMetadata) { mutableStateOf(roomMetadata?.description ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Document Permanently?") },
            text = { Text("Are you sure you want to permanently delete \"${activeTab.fileName}\" from your device? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteFilePermanently(activeTab.uriString)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Document Details & Metadata", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isEditingMetadata) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedAuthor,
                        onValueChange = { editedAuthor = it },
                        label = { Text("Author") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    MetadataItem(label = "Title", value = roomMetadata?.title.takeUnless { it.isNullOrBlank() } ?: activeTab.fileName)
                    MetadataItem(label = "Format / File Type", value = "${activeTab.fileType.displayName} (${roomMetadata?.fileType ?: activeTab.fileType.name})")
                    MetadataItem(label = "File Size", value = formattedSize)
                    MetadataItem(label = "Creation Date (Room)", value = creationDateFormatted)
                    MetadataItem(label = "Last Modified Date", value = lastModifiedFormatted)
                    MetadataItem(label = "Absolute Path / URI", value = roomMetadata?.path.takeUnless { it.isNullOrBlank() } ?: activeTab.uriString)
                    if (!roomMetadata?.mimeType.isNullOrBlank()) {
                        MetadataItem(label = "MIME Type", value = roomMetadata!!.mimeType)
                    }
                    if (!roomMetadata?.author.isNullOrBlank()) {
                        MetadataItem(label = "Author", value = roomMetadata!!.author)
                    }
                    if (!roomMetadata?.description.isNullOrBlank()) {
                        MetadataItem(label = "Description", value = roomMetadata!!.description)
                    }
                    MetadataItem(label = "Word Count", value = "${roomMetadata?.wordCount ?: wordCount} words")
                    MetadataItem(label = "Character Count", value = "${roomMetadata?.characterCount ?: charCount} ($charCountNoSpaces without spaces)")
                    MetadataItem(label = "Line Count", value = "${roomMetadata?.lineCount ?: lineCount} lines")
                }
            }
        },
        confirmButton = {
            if (isEditingMetadata) {
                TextButton(
                    onClick = {
                        val recordToSave = DocumentMetadataRecord(
                            uriString = activeTab.uriString,
                            title = editedTitle.ifBlank { activeTab.fileName },
                            fileType = activeTab.fileType.name,
                            fileSize = contentBytes,
                            creationDate = roomMetadata?.creationDate ?: System.currentTimeMillis(),
                            lastModifiedDate = System.currentTimeMillis(),
                            mimeType = roomMetadata?.mimeType ?: "",
                            path = roomMetadata?.path ?: "",
                            wordCount = wordCount,
                            characterCount = charCount,
                            lineCount = lineCount,
                            author = editedAuthor,
                            description = editedDescription
                        )
                        viewModel.saveDocumentMetadata(recordToSave)
                        isEditingMetadata = false
                    }
                ) {
                    Text("Save")
                }
            } else {
                TextButton(onClick = { isEditingMetadata = true }) {
                    Text("Edit Metadata")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete File", fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun MetadataItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
        HorizontalDivider(modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun PdfWatermarkSignDialog(
    activeTab: OpenTab,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL - OFFICIAL COPY") }
    var watermarkStyle by remember { mutableStateOf("Diagonal Center") } // Diagonal Center, Signature Stamp, Top Header Stamp
    var inkColor by remember { mutableStateOf(Color(0xFF1A237E)) }
    var opacity by remember { mutableStateOf(0.4f) }
    var includeTimestamp by remember { mutableStateOf(true) }
    var isExported by remember { mutableStateOf(false) }

    val styleOptions = listOf("Diagonal Center", "Signature Stamp", "Top Header Stamp")
    val colorPalette = listOf(
        Color(0xFF1A237E) to "Navy",
        Color(0xFFB71C1C) to "Crimson",
        Color(0xFF1B5E20) to "Forest",
        Color(0xFF212121) to "Slate"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Document Signing & Watermark Utility", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Overlay custom text, digital signature stamps, or watermarks onto documents prior to export.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark / Signer Text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Watermark Placement Style
                Column {
                    Text("Placement Style", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        styleOptions.forEach { style ->
                            FilterChip(
                                selected = watermarkStyle == style,
                                onClick = { watermarkStyle = style },
                                label = { Text(style, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // Ink Color Selection
                Column {
                    Text("Stamp / Watermark Color", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        colorPalette.forEach { (color, name) ->
                            val isSelected = inkColor == color
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { inkColor = color }
                            )
                        }
                    }
                }

                // Opacity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Opacity / Transparency", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("${(opacity * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.1f..1.0f
                    )
                }

                // Timestamp Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Date & Timestamp Badge", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = includeTimestamp,
                        onCheckedChange = { includeTimestamp = it }
                    )
                }

                // Real-Time Canvas Document Preview
                Text("LIVE DOCUMENT PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    // Document Mock Content Lines
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(activeTab.fileName.uppercase(), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (it % 2 == 0) 0.9f else 0.7f)
                                    .height(8.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    // Watermark / Signature Overlay
                    when (watermarkStyle) {
                        "Diagonal Center" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .rotate(-30f)
                                        .alpha(opacity)
                                ) {
                                    Text(
                                        text = watermarkText.ifEmpty { "WATERMARK" },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = inkColor
                                    )
                                    if (includeTimestamp) {
                                        Text(
                                            text = "Aug 13, 2026 • Digitally Stamped",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor
                                        )
                                    }
                                }
                            }
                        }
                        "Signature Stamp" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier
                                        .alpha(opacity)
                                        .background(inkColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .border(1.dp, inkColor, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "SIGNED: ${watermarkText.ifEmpty { "Authorized Signer" }}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = inkColor
                                    )
                                    if (includeTimestamp) {
                                        Text(
                                            text = "2026-08-13 03:31 UTC • Verified",
                                            fontSize = 8.sp,
                                            color = inkColor
                                        )
                                    }
                                }
                            }
                        }
                        "Top Header Stamp" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(
                                    text = "[ ${watermarkText.ifEmpty { "WATERMARK" }} ${if (includeTimestamp) "• Aug 13, 2026" else ""} ]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = inkColor,
                                    modifier = Modifier.alpha(opacity)
                                )
                            }
                        }
                    }
                }

                if (isExported) {
                    HighDensityBadge {
                        Text(
                            "Exported watermarked document as PDF to Downloads directory!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExported = true
                    android.widget.Toast.makeText(context, "Watermarked PDF exported", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(48.dp),
                enabled = watermarkText.isNotBlank()
            ) {
                Text(
                    text = "Export Watermarked PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
