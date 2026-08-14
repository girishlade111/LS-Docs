package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FolderRecord

val FOLDER_COLORS = listOf(
    "#6750A4" to "Purple",
    "#1976D2" to "Blue",
    "#00897B" to "Teal",
    "#2E7D32" to "Green",
    "#F57C00" to "Orange",
    "#C2185B" to "Rose",
    "#D32F2F" to "Red",
    "#455A64" to "Slate"
)

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF6750A4)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0x00000000FF000000)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun CreateFolderDialog(
    initialFolder: FolderRecord? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, colorHex: String) -> Unit
) {
    var folderName by remember { mutableStateOf(initialFolder?.name ?: "") }
    var folderDescription by remember { mutableStateOf(initialFolder?.description ?: "") }
    var selectedColorHex by remember { mutableStateOf(initialFolder?.colorHex ?: FOLDER_COLORS[0].first) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialFolder == null) Icons.Default.CreateNewFolder else Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialFolder == null) "Create New Folder" else "Edit Folder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Organize related documents into custom colored folders.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        if (it.isNotBlank()) hasError = false
                    },
                    label = { Text("Folder Name *") },
                    placeholder = { Text("e.g. Work Invoices, Receipts, Projects") },
                    isError = hasError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Text(
                        text = "Folder name cannot be empty.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                OutlinedTextField(
                    value = folderDescription,
                    onValueChange = { folderDescription = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g. Q3 Financial Documents") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "FOLDER COLOR ACCENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FOLDER_COLORS.forEach { (colorHex, _) ->
                        val color = parseHexColor(colorHex)
                        val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isBlank()) {
                        hasError = true
                    } else {
                        onConfirm(folderName.trim(), folderDescription.trim(), selectedColorHex)
                    }
                }
            ) {
                Text(if (initialFolder == null) "Create Folder" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    folders: List<FolderRecord>,
    currentFolderId: String?,
    documentCount: Int = 1,
    onDismiss: () -> Unit,
    onCreateNewFolder: () -> Unit,
    onFolderSelected: (folderId: String, folderName: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DriveFileMove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (documentCount > 1) "Move $documentCount Documents to Folder" else "Move Document to Folder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select destination folder to organize your library:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Root / No Folder Option
                val isRootSelected = currentFolderId.isNullOrEmpty()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isRootSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable { onFolderSelected("", "") }
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Root Library (No Folder)",
                            fontSize = 13.sp,
                            fontWeight = if (isRootSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = "Unassign from any specific folder",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isRootSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Current",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                if (folders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom folders created yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(folders) { folder ->
                            val isSelected = currentFolderId == folder.id
                            val folderColor = parseHexColor(folder.colorHex)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) folderColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) folderColor else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onFolderSelected(folder.id, folder.name) }
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(folderColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = folderColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (folder.description.isNotBlank()) {
                                        Text(
                                            text = folder.description,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Current",
                                        tint = folderColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onCreateNewFolder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Folder", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FolderCard(
    folder: FolderRecord,
    documentCount: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    val folderColor = parseHexColor(folder.colorHex)

    HighDensityCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isSelected) folderColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) folderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(folderColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = folderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (folder.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = folderColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showMenu = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Folder Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (folder.isPinned) "Unpin Folder" else "Pin Folder", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (folder.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onTogglePin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Folder", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Folder", fontSize = 12.sp, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = folder.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (folder.description.isNotBlank()) {
                Text(
                    text = folder.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                HighDensityBadge(
                    backgroundColor = folderColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$documentCount ${if (documentCount == 1) "file" else "files"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = folderColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
