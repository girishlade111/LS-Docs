package com.example.ui.screens

import com.example.ui.components.DocumentEmptyState
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import com.example.data.util.AesEncryptionHelper
import com.example.data.util.FileHelper
import com.example.data.database.DocumentRecord
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentFileType
import com.example.data.model.DocumentSortOption
import com.example.data.model.sortDocuments
import com.example.data.model.sortFiles
import com.example.data.database.FolderRecord
import com.example.ui.components.CategoryChip
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.ChipSize
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DocumentSortMenu
import com.example.ui.components.HighDensityBadge
import com.example.ui.components.HighDensityCard
import com.example.ui.components.MoveToFolderDialog
import com.example.ui.components.parseHexColor
import com.example.ui.theme.DensityGreen
import com.example.ui.theme.Purple40
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.PurpleLightBg
import com.example.ui.viewmodel.MainViewModel

data class HomeScreenSearchResult(
    val fileName: String,
    val fileType: String,
    val uri: Uri,
    val matchedByFilename: Boolean,
    val matchedByContent: Boolean,
    val contentSnippet: String? = null,
    val progress: Float = 0f,
    val documentRecord: DocumentRecord? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToWorkspace: () -> Unit,
    onNavigateToTools: (String) -> Unit
) {
    val context = LocalContext.current
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    val allMetadata by viewModel.allDocumentMetadata.collectAsState()
    val folders by viewModel.folders.collectAsState()
    var docToDelete by remember { mutableStateOf<DocumentRecord?>(null) }
    var docToCategorize by remember { mutableStateOf<DocumentRecord?>(null) }
    var docToMoveToFolder by remember { mutableStateOf<DocumentRecord?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedRecentCategory by remember { mutableStateOf<String?>(null) }
    var selectedSortOption by remember { mutableStateOf(DocumentSortOption.DEFAULT) }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, desc, colorHex ->
                viewModel.createFolder(name, desc, colorHex)
                showCreateFolderDialog = false
            }
        )
    }

    if (docToMoveToFolder != null) {
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = docToMoveToFolder?.folderId,
            onDismiss = { docToMoveToFolder = null },
            onCreateNewFolder = { showCreateFolderDialog = true },
            onFolderSelected = { fId, fName ->
                docToMoveToFolder?.let { doc ->
                    viewModel.moveDocumentToFolder(doc.uriString, fId, fName)
                }
                docToMoveToFolder = null
            }
        )
    }

    if (docToCategorize != null) {
        CategoryPickerDialog(
            currentCategory = docToCategorize?.category,
            onDismiss = { docToCategorize = null },
            onCategorySelected = { newCat ->
                docToCategorize?.let { doc ->
                    viewModel.updateDocumentCategory(doc.uriString, newCat)
                }
                docToCategorize = null
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchScope by remember { mutableStateOf("All") }

    val documentContents = remember(recentDocs, sampleFiles) {
        val map = mutableMapOf<String, String>()
        recentDocs.forEach { doc ->
            try {
                val content = if (doc.isEncryptedWithAes256 && doc.encryptedContent.isNotEmpty()) {
                    AesEncryptionHelper.decryptText(doc.encryptedContent)
                } else {
                    FileHelper.readTextFromUri(context, Uri.parse(doc.uriString))
                }
                map[doc.uriString] = content
            } catch (e: Exception) {
                map[doc.uriString] = ""
            }
        }
        sampleFiles.forEach { file ->
            try {
                val content = FileHelper.readTextFromUri(context, file.uri)
                map[file.uri.toString()] = content
            } catch (e: Exception) {
                map[file.uri.toString()] = ""
            }
        }
        map
    }

    val searchResults = remember(searchQuery, selectedSearchScope, recentDocs, sampleFiles, documentContents) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim()
            val list = mutableListOf<HomeScreenSearchResult>()

            recentDocs.forEach { doc ->
                val filenameMatch = doc.fileName.contains(query, ignoreCase = true) || doc.extension.contains(query, ignoreCase = true)
                val content = documentContents[doc.uriString] ?: ""
                val contentMatch = content.contains(query, ignoreCase = true)

                val shouldInclude = when (selectedSearchScope) {
                    "Filename" -> filenameMatch
                    "Content" -> contentMatch
                    else -> filenameMatch || contentMatch
                }

                if (shouldInclude) {
                    val snippet = if (contentMatch) {
                        val index = content.indexOf(query, ignoreCase = true)
                        if (index >= 0) {
                            val start = (index - 25).coerceAtLeast(0)
                            val end = (index + query.length + 35).coerceAtMost(content.length)
                            "..." + content.substring(start, end).replace("\n", " ") + "..."
                        } else null
                    } else null

                    list.add(
                        HomeScreenSearchResult(
                            fileName = doc.fileName,
                            fileType = doc.extension.uppercase(),
                            uri = Uri.parse(doc.uriString),
                            matchedByFilename = filenameMatch,
                            matchedByContent = contentMatch,
                            contentSnippet = snippet,
                            progress = doc.readingProgress,
                            documentRecord = doc
                        )
                    )
                }
            }

            val recentUris = recentDocs.map { it.uriString }.toSet()
            sampleFiles.forEach { file ->
                if (!recentUris.contains(file.uri.toString())) {
                    val filenameMatch = file.name.contains(query, ignoreCase = true) || file.extension.contains(query, ignoreCase = true)
                    val content = documentContents[file.uri.toString()] ?: ""
                    val contentMatch = content.contains(query, ignoreCase = true)

                    val shouldInclude = when (selectedSearchScope) {
                        "Filename" -> filenameMatch
                        "Content" -> contentMatch
                        else -> filenameMatch || contentMatch
                    }

                    if (shouldInclude) {
                        val snippet = if (contentMatch) {
                            val index = content.indexOf(query, ignoreCase = true)
                            if (index >= 0) {
                                val start = (index - 25).coerceAtLeast(0)
                                val end = (index + query.length + 35).coerceAtMost(content.length)
                                "..." + content.substring(start, end).replace("\n", " ") + "..."
                            } else null
                        } else null

                        list.add(
                            HomeScreenSearchResult(
                                fileName = file.name,
                                fileType = file.fileType.displayName,
                                uri = file.uri,
                                matchedByFilename = filenameMatch,
                                matchedByContent = contentMatch,
                                contentSnippet = snippet,
                                progress = 0f
                            )
                        )
                    }
                }
            }

            list
        }
    }

    if (docToDelete != null) {
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Document Record?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently remove \"${docToDelete?.fileName}\" from your local database? This action cannot be undone and will delete all stored bookmarks, metadata, and reading progress.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        docToDelete?.let { viewModel.deleteDocumentRecord(it.uriString) }
                        docToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
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
            onNavigateToWorkspace()
        }
    }

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshDeviceFilesManually() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Main Search Bar at Top of Document View
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search documents by filename or content...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Documents",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        listOf("All", "Filename", "Content").forEach { scope ->
                            FilterChip(
                                selected = selectedSearchScope == scope,
                                onClick = { selectedSearchScope = scope },
                                label = { Text(scope, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        if (searchQuery.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SEARCH RESULTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${searchResults.size} found",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (searchResults.isEmpty()) {
                item {
                    HighDensityCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = "No Results",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No documents found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No files match \"$searchQuery\" in filename or content.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(searchResults) { result ->
                    SearchResultCard(
                        result = result,
                        onClick = {
                            viewModel.openDocument(result.uri)
                            onNavigateToWorkspace()
                        },
                        onDelete = if (result.documentRecord != null) {
                            { docToDelete = result.documentRecord }
                        } else null
                    )
                }
            }
        } else {
        // Privacy Hero Banner (High Density Portfolio style)
        item {
            HighDensityCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                backgroundColor = Purple40
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Privacy Shield",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% OFFLINE & PRIVACY-FIRST",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        HighDensityBadge(
                            backgroundColor = PurpleLightBg.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "ZERO TRACKING",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LS Docs Never Uploads Your Files",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your documents stay on your device forever. No servers, no accounts, no telemetry.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("application/pdf", "text/*", "*/*")) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PurpleLightBg,
                                contentColor = PurpleDark
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Open File", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Document",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Device Storage",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Storage & Privacy Stats (3-Column Matrix)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(
                    title = "RECENT FILES",
                    value = "${recentDocs.size}",
                    subtitle = "Inspected",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = "PRIVACY",
                    value = "Active",
                    subtitle = "100% Offline",
                    color = DensityGreen,
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    title = "VAULT",
                    value = "Protected",
                    subtitle = "Local Storage",
                    color = Purple40,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan OCR",
                    onClick = { onNavigateToTools("ocr") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    icon = Icons.Default.Compare,
                    label = "Diff Tool",
                    onClick = { onNavigateToTools("diff") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    icon = Icons.Default.Transform,
                    label = "Converter",
                    onClick = { onNavigateToTools("convert") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    icon = Icons.Default.Visibility,
                    label = "Hex View",
                    onClick = { onNavigateToTools("hex") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // File Type Shortcuts
        item {
            Text(
                text = "FORMAT SHORTCUTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    listOf(
                        Triple("PDF", Icons.Default.PictureAsPdf, DocumentFileType.PDF),
                        Triple("Markdown", Icons.Default.Description, DocumentFileType.MARKDOWN),
                        Triple("JSON/XML", Icons.Default.Code, DocumentFileType.JSON),
                        Triple("CSV Table", Icons.Default.TableChart, DocumentFileType.CSV),
                        Triple("Code", Icons.Default.Code, DocumentFileType.CODE),
                        Triple("Images", Icons.Default.Image, DocumentFileType.IMAGE)
                    )
                ) { (label, icon, type) ->
                    HighDensityCard(
                        onClick = {
                            val matchingSample = sampleFiles.find { it.fileType == type }
                            if (matchingSample != null) {
                                viewModel.openDocument(matchingSample.uri)
                                onNavigateToWorkspace()
                            } else {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.width(110.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent / Sample Documents
        item {
            val filteredRecentDocs = remember(recentDocs, selectedRecentCategory, selectedSortOption, allMetadata) {
                val filtered = if (selectedRecentCategory.isNullOrBlank() || selectedRecentCategory == "All") {
                    recentDocs
                } else {
                    recentDocs.filter { it.category.equals(selectedRecentCategory, ignoreCase = true) }
                }
                filtered.sortDocuments(selectedSortOption, allMetadata)
            }

            val sortedSampleFiles = remember(sampleFiles, recentDocs, selectedSortOption, allMetadata) {
                sampleFiles.sortFiles(selectedSortOption, recentDocs, allMetadata)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECENT DOCUMENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${filteredRecentDocs.size} / ${recentDocs.size} files",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                DocumentSortMenu(
                    selectedOption = selectedSortOption,
                    onOptionSelected = { selectedSortOption = it }
                )
            }

            // Category Filter Pills
            val categoryFilters = remember { listOf("All") + DocumentCategory.getCategoryNames() }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(categoryFilters) { catName ->
                    val isSelected = (selectedRecentCategory == null && catName == "All") || (selectedRecentCategory == catName)
                    CategoryChip(
                        category = if (catName == "All") null else catName,
                        isSelected = isSelected,
                        size = ChipSize.Small,
                        onClick = {
                            selectedRecentCategory = if (catName == "All") null else catName
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (recentDocs.isEmpty() && sampleFiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Device documents found on local storage:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                    sortedSampleFiles.take(5).forEach { file ->
                        DocumentRowItem(
                            fileName = file.name,
                            fileType = file.fileType.displayName,
                            progress = 0f,
                            category = null,
                            onClick = {
                                viewModel.openDocument(file.uri)
                                onNavigateToWorkspace()
                            }
                        )
                    }
                }
            } else if (recentDocs.isEmpty()) {
                DocumentEmptyState(
                    title = "No Documents Opened Yet",
                    description = "Your workspace is currently empty. Open files directly from your phone or scan images with OCR to begin inspecting documents.",
                    onOpenStorage = { filePickerLauncher.launch(arrayOf("*/*")) },
                    onScanOcr = { onNavigateToTools("ocr") }
                )
            } else if (filteredRecentDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No documents found in category \"$selectedRecentCategory\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredRecentDocs.take(10).forEach { doc ->
                        val docFolder = folders.find { it.id == doc.folderId }
                        DocumentRowItem(
                            fileName = doc.fileName,
                            fileType = doc.extension.uppercase(),
                            progress = doc.readingProgress,
                            category = doc.category.takeIf { it.isNotBlank() },
                            folderName = docFolder?.name,
                            folderColorHex = docFolder?.colorHex,
                            onFolderClick = {
                                docToMoveToFolder = doc
                            },
                            onCategoryClick = {
                                docToCategorize = doc
                            },
                            onClick = {
                                try {
                                    viewModel.openDocument(Uri.parse(doc.uriString))
                                    onNavigateToWorkspace()
                                } catch (e: Exception) {
                                    viewModel.showToast("Cannot open document: ${e.localizedMessage}")
                                }
                            },
                            onDelete = {
                                docToDelete = doc
                            }
                        )
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun SearchResultCard(
    result: HomeScreenSearchResult,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onCategorize: (() -> Unit)? = null
) {
    HighDensityCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.fileType.take(3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (result.matchedByFilename) {
                            HighDensityBadge(backgroundColor = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("Filename Match", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        if (result.matchedByContent) {
                            HighDensityBadge(backgroundColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                Text("Content Match", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        if (!result.documentRecord?.category.isNullOrBlank()) {
                            CategoryChip(
                                category = result.documentRecord?.category,
                                size = ChipSize.Small,
                                onClick = onCategorize
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Record",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (result.contentSnippet != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = result.contentSnippet,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    HighDensityCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HighDensityCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun DocumentRowItem(
    fileName: String,
    fileType: String,
    progress: Float,
    category: String? = null,
    folderName: String? = null,
    folderColorHex: String? = null,
    onFolderClick: (() -> Unit)? = null,
    onCategoryClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    HighDensityCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fileType.take(3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Local Document",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!folderName.isNullOrBlank()) {
                        val fColor = parseHexColor(folderColorHex ?: "#6750A4")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(fColor.copy(alpha = 0.15f))
                                .clickable(enabled = onFolderClick != null) { onFolderClick?.invoke() }
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = fColor, modifier = Modifier.size(9.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(folderName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fColor)
                        }
                    }

                    if (!category.isNullOrBlank()) {
                        CategoryChip(
                            category = category,
                            size = ChipSize.Small,
                            onClick = onCategoryClick
                        )
                    } else if (onCategoryClick != null) {
                        CategoryChip(
                            category = "+ Label",
                            size = ChipSize.Small,
                            showIcon = false,
                            onClick = onCategoryClick
                        )
                    }
                }

                if (progress > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (onFolderClick != null) {
                IconButton(onClick = onFolderClick) {
                    Icon(
                        imageVector = Icons.Default.DriveFileMove,
                        contentDescription = "Move to Folder",
                        tint = if (!folderName.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onCategoryClick != null && category == null) {
                IconButton(onClick = onCategoryClick) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = "Label Category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
