package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.database.DocumentRecord
import com.example.data.database.FolderRecord
import com.example.data.model.DocumentCategory
import com.example.data.model.DocumentSortOption
import com.example.data.model.sortDocuments
import com.example.ui.components.CategoryChip
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.ChipSize
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DocumentSortMenu
import com.example.ui.components.FolderCard
import com.example.ui.components.HighDensityBadge
import com.example.ui.components.MoveToFolderDialog
import com.example.ui.components.parseHexColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HighDensityCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToWorkspace: () -> Unit
) {
    val recentDocs by viewModel.recentDocuments.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val annotations by viewModel.annotations.collectAsState()
    val privateDocs by viewModel.privateDocuments.collectAsState()
    val allMetadata by viewModel.allDocumentMetadata.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val isVaultUnlocked by viewModel.isPrivateVaultUnlocked.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedSortOption by remember { mutableStateOf(DocumentSortOption.DEFAULT) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<FolderRecord?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderRecord?>(null) }
    var docToMoveToFolder by remember { mutableStateOf<DocumentRecord?>(null) }
    var docToCategorize by remember { mutableStateOf<DocumentRecord?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<DocumentRecord?>(null) }

    if (showCreateFolderDialog || folderToEdit != null) {
        CreateFolderDialog(
            initialFolder = folderToEdit,
            onDismiss = {
                showCreateFolderDialog = false
                folderToEdit = null
            },
            onConfirm = { name, desc, colorHex ->
                if (folderToEdit != null) {
                    viewModel.updateFolder(folderToEdit!!.copy(name = name, description = desc, colorHex = colorHex))
                } else {
                    viewModel.createFolder(name, desc, colorHex)
                }
                showCreateFolderDialog = false
                folderToEdit = null
            }
        )
    }

    if (docToMoveToFolder != null) {
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = docToMoveToFolder?.folderId,
            onDismiss = { docToMoveToFolder = null },
            onCreateNewFolder = {
                showCreateFolderDialog = true
            },
            onFolderSelected = { fId, fName ->
                docToMoveToFolder?.let { doc ->
                    viewModel.moveDocumentToFolder(doc.uriString, fId, fName)
                }
                docToMoveToFolder = null
            }
        )
    }

    if (folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
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
                    text = "Delete Folder \"${folderToDelete?.name}\"?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this folder? All contained documents will be safely preserved in your library root.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        folderToDelete?.let { f ->
                            if (selectedFolderId == f.id) selectedFolderId = null
                            viewModel.deleteFolder(f.id, f.name)
                        }
                        folderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Folder", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancel")
                }
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

    val tabs = listOf("Folders & Labels", "Favorites", "Bookmarks", "Annotations", "Private Vault")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Folders & Categories Tab
                val folderDocCounts = remember(recentDocs) {
                    val map = mutableMapOf<String, Int>()
                    recentDocs.forEach { doc ->
                        if (doc.folderId.isNotBlank()) {
                            map[doc.folderId] = (map[doc.folderId] ?: 0) + 1
                        }
                    }
                    map
                }

                val activeFolder = remember(folders, selectedFolderId) {
                    folders.find { it.id == selectedFolderId }
                }

                val displayedDocs = remember(recentDocs, selectedFolderId, selectedCategoryFilter, selectedSortOption, allMetadata, activeFolder) {
                    val filteredByFolder = if (selectedFolderId != null) {
                        recentDocs.filter { it.folderId == selectedFolderId || (activeFolder != null && it.folderName.equals(activeFolder.name, ignoreCase = true)) }
                    } else {
                        recentDocs
                    }

                    val filteredByCategory = if (selectedCategoryFilter.isNullOrBlank() || selectedCategoryFilter == "All") {
                        filteredByFolder
                    } else {
                        filteredByFolder.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
                    }

                    filteredByCategory.sortDocuments(selectedSortOption, allMetadata)
                }

                val categoryCounts = remember(recentDocs, selectedFolderId, activeFolder) {
                    val baseList = if (selectedFolderId != null) {
                        recentDocs.filter { it.folderId == selectedFolderId || (activeFolder != null && it.folderName.equals(activeFolder.name, ignoreCase = true)) }
                    } else {
                        recentDocs
                    }
                    val map = mutableMapOf<String, Int>()
                    baseList.forEach { doc ->
                        val cat = if (doc.category.isNotBlank()) doc.category else "Uncategorized"
                        map[cat] = (map[cat] ?: 0) + 1
                    }
                    map
                }

                val allCategories = remember { listOf("All") + DocumentCategory.getCategoryNames() }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (activeFolder != null) {
                        // Inside Folder View Header Banner
                        val folderColor = parseHexColor(activeFolder.colorHex)
                        HighDensityCard(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = folderColor.copy(alpha = 0.12f),
                            borderColor = folderColor.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        IconButton(
                                            onClick = { selectedFolderId = null },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back to All Folders",
                                                tint = folderColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(folderColor.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = folderColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = activeFolder.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (activeFolder.description.isNotBlank()) {
                                                Text(
                                                    text = activeFolder.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { folderToEdit = activeFolder },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Folder", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { folderToDelete = activeFolder },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Folder", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Custom Folders Overview Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CUSTOM FOLDERS (${folders.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                            TextButton(
                                onClick = { showCreateFolderDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (folders.isEmpty()) {
                            HighDensityCard(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCreateFolderDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Create Your First Folder", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Group documents into custom colored folders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(folders) { folder ->
                                    val count = folderDocCounts[folder.id] ?: 0
                                    FolderCard(
                                        folder = folder,
                                        documentCount = count,
                                        isSelected = selectedFolderId == folder.id,
                                        onClick = {
                                            selectedFolderId = if (selectedFolderId == folder.id) null else folder.id
                                        },
                                        onEdit = { folderToEdit = folder },
                                        onDelete = { folderToDelete = folder },
                                        onTogglePin = { viewModel.togglePinFolder(folder.id) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "CATEGORY LABELS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )

                        // Horizontal Category Chips Filter
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allCategories) { catName ->
                                val isSelected = (selectedCategoryFilter == null && catName == "All") || (selectedCategoryFilter == catName)
                                CategoryChip(
                                    category = if (catName == "All") null else catName,
                                    isSelected = isSelected,
                                    size = ChipSize.Medium,
                                    onClick = {
                                        selectedCategoryFilter = if (catName == "All") null else catName
                                    }
                                )
                            }
                        }
                    }

                    if (recentDocs.isEmpty()) {
                        EmptyLibraryState("No documents tracked yet. Open files in the Document Workspace to organize them into folders!")
                    } else if (displayedDocs.isEmpty()) {
                        EmptyLibraryState(
                            if (activeFolder != null) "No documents currently in folder \"${activeFolder.name}\". Tap the Folder icon on any document to move it here!"
                            else "No documents found matching category \"$selectedCategoryFilter\"."
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${displayedDocs.size} ${if (displayedDocs.size == 1) "document" else "documents"} ${if (activeFolder != null) "in ${activeFolder.name}" else if (selectedCategoryFilter != null) "in $selectedCategoryFilter" else "in Library"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            DocumentSortMenu(
                                selectedOption = selectedSortOption,
                                onOptionSelected = { selectedSortOption = it }
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(displayedDocs) { doc ->
                                val docFolder = folders.find { it.id == doc.folderId }
                                HighDensityCard(
                                    onClick = {
                                        try {
                                            viewModel.openDocument(Uri.parse(doc.uriString))
                                            onNavigateToWorkspace()
                                        } catch (e: Exception) {
                                            viewModel.showToast("Cannot open document: ${e.localizedMessage}")
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
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
                                                text = doc.extension.take(3).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = doc.fileName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${doc.extension.uppercase()} • ${(doc.fileSize / 1024).coerceAtLeast(1)} KB",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (docFolder != null) {
                                                    val fColor = parseHexColor(docFolder.colorHex)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(fColor.copy(alpha = 0.15f))
                                                            .clickable { docToMoveToFolder = doc }
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(Icons.Default.Folder, contentDescription = null, tint = fColor, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(docFolder.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fColor)
                                                    }
                                                }
                                                if (doc.category.isNotBlank()) {
                                                    CategoryChip(
                                                        category = doc.category,
                                                        size = ChipSize.Small,
                                                        onClick = { docToCategorize = doc }
                                                    )
                                                }
                                            }
                                        }

                                        // Favorite Heart Toggle Button
                                        IconButton(onClick = { viewModel.toggleFavorite(doc.uriString) }) {
                                            Icon(
                                                imageVector = if (doc.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = if (doc.isFavorite) "Favorited" else "Favorite",
                                                tint = if (doc.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Move to Folder Action
                                        IconButton(onClick = { docToMoveToFolder = doc }) {
                                            Icon(
                                                imageVector = Icons.Default.DriveFileMove,
                                                contentDescription = "Move to Folder",
                                                tint = if (doc.folderId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Category Label Action
                                        IconButton(onClick = { docToCategorize = doc }) {
                                            Icon(
                                                imageVector = Icons.Default.Label,
                                                contentDescription = "Change Category",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Delete Record Action
                                        IconButton(onClick = { docToDelete = doc }) {
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
                    }
                }
            }
            1 -> {
                // Dedicated Favorites & Pinned View Tab
                val favoriteDocs = remember(recentDocs, selectedSortOption, allMetadata) {
                    recentDocs.filter { it.isFavorite || it.isPinned }.sortDocuments(selectedSortOption, allMetadata)
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FAVORITE & PINNED DOCUMENTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "${favoriteDocs.size} pinned items",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DocumentSortMenu(
                            selectedOption = selectedSortOption,
                            onOptionSelected = { selectedSortOption = it }
                        )
                    }

                    if (favoriteDocs.isEmpty()) {
                        EmptyLibraryState("No favorite documents yet. Tap the heart icon on any document card to pin it to your favorites for instant access!")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(favoriteDocs) { doc ->
                                val docFolder = folders.find { it.id == doc.folderId }
                                HighDensityCard(
                                    onClick = {
                                        try {
                                            viewModel.openDocument(Uri.parse(doc.uriString))
                                            onNavigateToWorkspace()
                                        } catch (e: Exception) {
                                            viewModel.showToast("Cannot open document: ${e.localizedMessage}")
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(14.dp)
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
                                                text = doc.extension.take(3).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = doc.fileName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${doc.extension.uppercase()} • ${(doc.fileSize / 1024).coerceAtLeast(1)} KB",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (docFolder != null) {
                                                    val fColor = parseHexColor(docFolder.colorHex)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(fColor.copy(alpha = 0.15f))
                                                            .clickable { docToMoveToFolder = doc }
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(Icons.Default.Folder, contentDescription = null, tint = fColor, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(docFolder.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fColor)
                                                    }
                                                }
                                                if (doc.category.isNotBlank()) {
                                                    CategoryChip(
                                                        category = doc.category,
                                                        size = ChipSize.Small,
                                                        onClick = { docToCategorize = doc }
                                                    )
                                                }
                                            }
                                        }

                                        // Favorite Heart Toggle Button
                                        IconButton(onClick = { viewModel.toggleFavorite(doc.uriString) }) {
                                            Icon(
                                                imageVector = if (doc.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = if (doc.isFavorite) "Favorited" else "Favorite",
                                                tint = if (doc.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Move to Folder Action
                                        IconButton(onClick = { docToMoveToFolder = doc }) {
                                            Icon(
                                                imageVector = Icons.Default.DriveFileMove,
                                                contentDescription = "Move to Folder",
                                                tint = if (doc.folderId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Delete Record Action
                                        IconButton(onClick = { docToDelete = doc }) {
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
                    }
                }
            }
            2 -> {
                if (bookmarks.isEmpty()) {
                    EmptyLibraryState("No bookmarks saved yet. Add bookmarks while reading documents!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(bookmarks) { b ->
                            HighDensityCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(b.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Page/Line ${b.pageOrLine} • ${b.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                if (annotations.isEmpty()) {
                    EmptyLibraryState("No annotations found. Highlight text or draw notes inside PDFs & Markdown files!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(annotations) { a ->
                            HighDensityCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EditNote, contentDescription = "Annotation", tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("${a.type} (Page ${a.pageOrLine})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(a.textContent, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            4 -> {
                if (!isVaultUnlocked) {
                    HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked Vault", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("PRIVATE VAULT LOCKED", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Enter PIN to unlock local protected documents.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("PIN Code") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (pinError) {
                                Text("Incorrect PIN. Default PIN is 1234 or leave blank.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    val success = viewModel.unlockPrivateVault(pinInput)
                                    if (success) {
                                        viewModel.showToast("Private vault unlocked")
                                    } else {
                                        pinError = true
                                        viewModel.showToast("Incorrect PIN")
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = "Unlock")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unlock Private Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    val sortedPrivateDocs = remember(privateDocs, selectedSortOption, allMetadata) {
                        privateDocs.sortDocuments(selectedSortOption, allMetadata)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PROTECTED PRIVATE DOCUMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DocumentSortMenu(
                                    selectedOption = selectedSortOption,
                                    onOptionSelected = { selectedSortOption = it }
                                )
                                Button(
                                    onClick = {
                                        viewModel.lockPrivateVault()
                                        viewModel.showToast("Private vault locked")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text(
                                        text = "Lock Vault",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (privateDocs.isEmpty()) {
                            EmptyLibraryState("Private vault is empty. Move files into private storage in Settings or File Inspector.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(sortedPrivateDocs) { doc ->
                                    HighDensityCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .padding(14.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = "Private File", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(doc.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Protected Local Storage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            IconButton(onClick = { docToDelete = doc }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Record",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(message: String) {
    HighDensityCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}
