package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.database.DocumentRecord
import com.example.data.model.DocumentCategory
import com.example.ui.components.CategoryChip
import com.example.ui.components.CategoryPickerDialog
import com.example.ui.components.ChipSize
import com.example.ui.components.HighDensityBadge
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
    val isVaultUnlocked by viewModel.isPrivateVaultUnlocked.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var docToCategorize by remember { mutableStateOf<DocumentRecord?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var docToDelete by remember { mutableStateOf<DocumentRecord?>(null) }

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
            title = { Text("Delete Private Document?") },
            text = { Text("Are you sure you want to permanently delete \"${docToDelete?.fileName}\" from your private vault database record? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        docToDelete?.let { viewModel.deleteDocumentRecord(it.uriString) }
                        docToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val tabs = listOf("Categories", "Bookmarks", "Annotations", "Private Vault")

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
                // Categories Tab
                val categorizedDocs = remember(recentDocs, selectedCategoryFilter) {
                    if (selectedCategoryFilter.isNullOrBlank() || selectedCategoryFilter == "All") {
                        recentDocs
                    } else {
                        recentDocs.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
                    }
                }

                val categoryCounts = remember(recentDocs) {
                    val map = mutableMapOf<String, Int>()
                    recentDocs.forEach { doc ->
                        val cat = if (doc.category.isNotBlank()) doc.category else "Uncategorized"
                        map[cat] = (map[cat] ?: 0) + 1
                    }
                    map
                }

                val allCategories = remember { listOf("All") + DocumentCategory.getCategoryNames() }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DOCUMENT CATEGORIES & LABELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )

                    // Horizontal Category Chips Filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allCategories) { catName ->
                            val isSelected = (selectedCategoryFilter == null && catName == "All") || (selectedCategoryFilter == catName)
                            val count = if (catName == "All") recentDocs.size else (categoryCounts[catName] ?: 0)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                        EmptyLibraryState("No documents tracked yet. Open files in the Document Workspace to categorize them!")
                    } else if (categorizedDocs.isEmpty()) {
                        EmptyLibraryState("No documents found with category \"$selectedCategoryFilter\". Label documents to organize them here!")
                    } else {
                        Text(
                            text = "${categorizedDocs.size} documents in ${selectedCategoryFilter ?: "All Categories"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(categorizedDocs) { doc ->
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
                                                if (doc.category.isNotBlank()) {
                                                    CategoryChip(
                                                        category = doc.category,
                                                        size = ChipSize.Small,
                                                        onClick = { docToCategorize = doc }
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = { docToCategorize = doc }) {
                                            Icon(
                                                imageVector = Icons.Default.Label,
                                                contentDescription = "Change Category",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
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
            2 -> {
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
            3 -> {
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
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PROTECTED PRIVATE DOCUMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Button(
                                onClick = {
                                    viewModel.lockPrivateVault()
                                    viewModel.showToast("Private vault locked")
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.height(44.dp)
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

                        if (privateDocs.isEmpty()) {
                            EmptyLibraryState("Private vault is empty. Move files into private storage in Settings or File Inspector.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(privateDocs) { doc ->
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
