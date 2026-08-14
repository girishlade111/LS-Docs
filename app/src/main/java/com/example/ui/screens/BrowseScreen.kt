package com.example.ui.screens

import com.example.ui.components.DocumentEmptyState
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FolderRecord
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentSortOption
import com.example.data.model.FileDetails
import com.example.data.model.sortFiles
import com.example.ui.components.CategoryChip
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.ChipSize
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DocumentSortMenu
import com.example.ui.components.HighDensityBadge
import com.example.ui.components.HighDensityCard
import com.example.ui.components.MoveToFolderDialog
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: MainViewModel,
    onNavigateToWorkspace: () -> Unit
) {
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val metadataList by viewModel.allDocumentMetadata.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val haptic = LocalHapticFeedback.current

    val docCategoryMap = remember(recentDocs, metadataList) {
        val map = mutableMapOf<String, String>()
        recentDocs.forEach { if (it.category.isNotBlank()) map[it.uriString] = it.category }
        metadataList.forEach { if (it.category.isNotBlank()) map[it.uriString] = it.category }
        map
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    var selectedSortOption by remember { mutableStateOf(DocumentSortOption.DEFAULT) }
    var selectedFileForDetails by remember { mutableStateOf<FileDetails?>(null) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var fileToCategorize by remember { mutableStateOf<FileDetails?>(null) }
    var fileToMoveToFolder by remember { mutableStateOf<FileDetails?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name, desc, colorHex ->
                viewModel.createFolder(name, desc, colorHex)
                showCreateFolderDialog = false
            }
        )
    }

    if (fileToMoveToFolder != null) {
        val currentDoc = recentDocs.find { it.uriString == fileToMoveToFolder!!.uri.toString() }
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = currentDoc?.folderId,
            onDismiss = { fileToMoveToFolder = null },
            onCreateNewFolder = { showCreateFolderDialog = true },
            onFolderSelected = { fId, fName ->
                viewModel.moveDocumentToFolder(fileToMoveToFolder!!.uri.toString(), fId, fName)
                fileToMoveToFolder = null
            }
        )
    }

    if (fileToCategorize != null) {
        CategoryPickerDialog(
            currentCategory = docCategoryMap[fileToCategorize!!.uri.toString()],
            onDismiss = { fileToCategorize = null },
            onCategorySelected = { newCat ->
                viewModel.updateDocumentCategory(fileToCategorize!!.uri.toString(), newCat)
                fileToCategorize = null
            }
        )
    }

    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedFilePaths = remember { mutableStateListOf<String>() }
    var showBatchMoveDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showSingleDeleteDialog by remember { mutableStateOf(false) }
    var destinationFolderInput by remember { mutableStateOf("Documents/Archived") }

    // Storage analytics tab state
    var activeTab by remember { mutableStateOf(0) }

    val availableTags = remember {
        listOf("All") + DocumentCategory.getCategoryNames()
    }

    val context = androidx.compose.ui.platform.LocalContext.current

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

    val filteredFiles = remember(sampleFiles, searchQuery, selectedTagFilter, docCategoryMap, selectedSortOption, recentDocs, metadataList) {
        sampleFiles.filter { file ->
            val matchesFilename = file.name.contains(searchQuery, ignoreCase = true) || file.extension.contains(searchQuery, ignoreCase = true)
            val fileContent = try { com.example.data.util.FileHelper.readTextFromUri(context, file.uri) } catch (e: Exception) { "" }
            val matchesContent = fileContent.contains(searchQuery, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || matchesFilename || matchesContent
            val fileCat = docCategoryMap[file.uri.toString()] ?: ""
            val matchesTag = selectedTagFilter == null || selectedTagFilter == "All" ||
                    fileCat.equals(selectedTagFilter, ignoreCase = true) ||
                    file.path.contains(selectedTagFilter!!, ignoreCase = true)
            matchesSearch && matchesTag
        }.sortFiles(selectedSortOption, recentDocs, metadataList)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header: File Browser vs Storage Analytics Dashboard
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    activeTab = 0
                },
                text = { Text("File Explorer", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    activeTab = 1
                },
                text = { Text("Storage Usage Chart", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == 0) {
            // Multi-select Action Bar OR Search Bar
            if (isMultiSelectMode) {
                HighDensityCard(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isMultiSelectMode = false
                                selectedFilePaths.clear()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Exit Multi-Select")
                            }
                            Text(
                                text = "${selectedFilePaths.size} Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (selectedFilePaths.size == filteredFiles.size) {
                                    selectedFilePaths.clear()
                                } else {
                                    selectedFilePaths.clear()
                                    selectedFilePaths.addAll(filteredFiles.map { it.path })
                                }
                            }) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                            }
                            IconButton(
                                onClick = {
                                    if (selectedFilePaths.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showBatchMoveDialog = true
                                    }
                                },
                                enabled = selectedFilePaths.isNotEmpty()
                            ) {
                                Icon(Icons.Default.DriveFileMove, contentDescription = "Move to Folder")
                            }
                            IconButton(
                                onClick = {
                                    if (selectedFilePaths.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showBatchDeleteDialog = true
                                    }
                                },
                                enabled = selectedFilePaths.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Batch Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search local files...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isMultiSelectMode = true
                        }
                    ) {
                        Icon(Icons.Default.CheckBox, contentDescription = "Enable Multi-Select Mode", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category / Label Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableTags) { tag ->
                    val isSelected = (selectedTagFilter == tag) || (selectedTagFilter == null && tag == "All")
                    CategoryChip(
                        category = if (tag == "All") null else tag,
                        isSelected = isSelected,
                        size = ChipSize.Small,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTagFilter = if (tag == "All") null else tag
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Open External File Banner
            HighDensityCard(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    filePickerLauncher.launch(arrayOf("application/pdf", "text/*", "*/*"))
                },
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = "Pick Document",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Browse Device Storage", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Open any document or folder from system file picker", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMultiSelectMode) "MULTI-SELECT FILE LIST" else "DEVICE DOCUMENTS & FILES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentSortMenu(
                        selectedOption = selectedSortOption,
                        onOptionSelected = { selectedSortOption = it }
                    )
                    HighDensityBadge {
                        Text(
                            text = "${filteredFiles.size} items",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredFiles.isEmpty()) {
                DocumentEmptyState(
                    title = "No Local Documents Found",
                    description = "No document files match your current category or search query. Browse system storage or refresh the local file scanner.",
                    onOpenStorage = { filePickerLauncher.launch(arrayOf("*/*")) },
                    onRefresh = { viewModel.refreshDeviceFilesManually() }
                )
            } else {
                val isRefreshing by viewModel.isRefreshing.collectAsState()
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshDeviceFilesManually() },
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredFiles) { file ->
                            val isSelected = selectedFilePaths.contains(file.path)
                            val fileCat = docCategoryMap[file.uri.toString()]
                            HighDensityCard(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isMultiSelectMode) {
                                        if (isSelected) selectedFilePaths.remove(file.path)
                                        else selectedFilePaths.add(file.path)
                                    } else {
                                        viewModel.openDocument(file.uri)
                                        onNavigateToWorkspace()
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isMultiSelectMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (checked) selectedFilePaths.add(file.path)
                                                else selectedFilePaths.remove(file.path)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = "File",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                "${file.fileType.displayName} • ${(file.sizeBytes / 1024).coerceAtLeast(1)} KB",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (!fileCat.isNullOrBlank()) {
                                                CategoryChip(
                                                    category = fileCat,
                                                    size = ChipSize.Small,
                                                    onClick = {
                                                        fileToCategorize = file
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    IconButton(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        fileToMoveToFolder = file
                                    }) {
                                        Icon(
                                            Icons.Default.DriveFileMove,
                                            contentDescription = "Move to Folder",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        fileToCategorize = file
                                    }) {
                                        Icon(
                                            Icons.Default.Label,
                                            contentDescription = "Label Document",
                                            tint = if (!fileCat.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedFileForDetails = file
                                        showDetailsSheet = true
                                    }) {
                                        Icon(Icons.Default.Info, contentDescription = "File Details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Storage Analytics Dashboard Chart
            StorageAnalyticsDashboard(sampleFiles = sampleFiles)
        }
    }

    // Dialogs for Batch Operations
    if (showBatchMoveDialog) {
        val selectedUris = sampleFiles.filter { it.path in selectedFilePaths }.map { it.uri.toString() }
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = null,
            documentCount = selectedFilePaths.size,
            onDismiss = { showBatchMoveDialog = false },
            onCreateNewFolder = { showCreateFolderDialog = true },
            onFolderSelected = { fId, fName ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.moveMultipleDocumentsToFolder(selectedUris, fId, fName)
                showBatchMoveDialog = false
                isMultiSelectMode = false
                selectedFilePaths.clear()
            }
        )
    }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Batch Delete Confirmation") },
            text = { Text("Are you sure you want to delete ${selectedFilePaths.size} selected items permanently? This action cannot be undone and will permanently remove the files from your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedFilePaths.forEach { path ->
                            viewModel.deleteFilePermanently(path)
                        }
                        showBatchDeleteDialog = false
                        isMultiSelectMode = false
                        selectedFilePaths.clear()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSingleDeleteDialog && selectedFileForDetails != null) {
        val fileToDelete = selectedFileForDetails!!
        AlertDialog(
            onDismissRequest = { showSingleDeleteDialog = false },
            title = { Text("Delete File Permanently?") },
            text = { Text("Are you sure you want to permanently delete \"${fileToDelete.name}\" from your device storage? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.deleteFilePermanently(fileToDelete.uri.toString())
                        showSingleDeleteDialog = false
                        showDetailsSheet = false
                        selectedFileForDetails = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSingleDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // File Details Modal Bottom Sheet
    if (showDetailsSheet && selectedFileForDetails != null) {
        val file = selectedFileForDetails!!
        val currentCategory = docCategoryMap[file.uri.toString()]
        ModalBottomSheet(
            onDismissRequest = { showDetailsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(text = "FILE INSPECTOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = file.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))
                InfoItem("Type", file.fileType.displayName)
                InfoItem("Extension", file.extension.ifEmpty { "None" })
                InfoItem("Size", "${file.sizeBytes} bytes (${String.format("%.2f", file.sizeBytes / 1024.0)} KB)")
                InfoItem("MIME Type", file.mimeType)
                InfoItem("Path", file.path)
                InfoItem("Read Only", if (file.isReadOnly) "Yes" else "No")

                Spacer(modifier = Modifier.height(12.dp))
                Text("DOCUMENT CATEGORY / LABEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryChip(
                        category = currentCategory,
                        size = ChipSize.Large,
                        onClick = {
                            fileToCategorize = file
                            showDetailsSheet = false
                        }
                    )
                    TextButton(onClick = {
                        fileToCategorize = file
                        showDetailsSheet = false
                    }) {
                        Text(if (currentCategory.isNullOrBlank()) "Set Category" else "Change")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.openDocument(file.uri)
                        showDetailsSheet = false
                        onNavigateToWorkspace()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open in Document Workspace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        showSingleDeleteDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete File Permanently",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StorageAnalyticsDashboard(sampleFiles: List<FileDetails>) {
    val totalSizeBytes = sampleFiles.sumOf { it.sizeBytes }
    val formattedTotalKb = String.format("%.1f", totalSizeBytes / 1024.0)

    val typeGroups = remember(sampleFiles) {
        sampleFiles.groupBy { it.fileType.displayName }
            .mapValues { entry -> entry.value.sumOf { it.sizeBytes } }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    val colors = listOf(primaryColor, secondaryColor, tertiaryColor, errorColor)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HighDensityCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("STORAGE CONSUMPTION ANALYTICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Total Document Storage: $formattedTotalKb KB", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Storage Distribution Donut Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(150.dp)) {
                        var startAngle = -90f
                        val total = if (totalSizeBytes == 0L) 1L else totalSizeBytes
                        typeGroups.entries.forEachIndexed { index, entry ->
                            val sweepAngle = (entry.value.toFloat() / total) * 360f
                            val color = colors[index % colors.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 30.dp.toPx())
                            )
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${sampleFiles.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Files", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend breakdown
                typeGroups.entries.forEachIndexed { index, entry ->
                    val color = colors[index % colors.size]
                    val percentage = if (totalSizeBytes == 0L) 0f else (entry.value.toFloat() / totalSizeBytes) * 100f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(entry.key, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${String.format("%.1f", entry.value / 1024.0)} KB (${String.format("%.1f", percentage)}%)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

