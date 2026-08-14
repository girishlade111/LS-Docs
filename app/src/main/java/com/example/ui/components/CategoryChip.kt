package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentCategory

enum class ChipSize {
    Small,
    Medium,
    Large
}

@Composable
fun CategoryChip(
    category: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    isSelected: Boolean = false,
    showIcon: Boolean = true,
    size: ChipSize = ChipSize.Medium,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val displayCategory = category?.trim().takeUnless { it.isNullOrEmpty() } ?: "Uncategorized"
    val colorScheme = DocumentCategory.getColorScheme(category, isDark)
    val icon = DocumentCategory.getIcon(category)

    val (horizontalPadding, verticalPadding, fontSize, iconSize, cornerRadius) = when (size) {
        ChipSize.Small -> Tuple5(6.dp, 2.dp, 9.sp, 10.dp, 6.dp)
        ChipSize.Medium -> Tuple5(8.dp, 3.dp, 11.sp, 12.dp, 8.dp)
        ChipSize.Large -> Tuple5(12.dp, 6.dp, 13.sp, 15.dp, 10.dp)
    }

    val chipShape = RoundedCornerShape(cornerRadius)

    val backgroundModifier = if (isSelected) {
        Modifier.background(colorScheme.primaryColor)
    } else {
        Modifier
            .background(colorScheme.containerColor)
            .border(
                width = 1.dp,
                color = colorScheme.borderColor,
                shape = chipShape
            )
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(chipShape)
            .then(backgroundModifier)
            .then(clickableModifier)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showIcon && category?.isNotBlank() == true) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else colorScheme.primaryColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = displayCategory,
                color = if (isSelected) Color.White else colorScheme.textColor,
                fontSize = fontSize,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1
            )

            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Tag",
                        tint = if (isSelected) Color.White else colorScheme.primaryColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPickerDialog(
    currentCategory: String?,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentCategory ?: "") }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val presetCategories = remember { DocumentCategory.getCategoryNames() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Categorize Document", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign a color-coded category or custom label to organize and filter this document.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "PRESET CATEGORIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetCategories.forEach { catName ->
                        val isSelected = selectedCategory.equals(catName, ignoreCase = true)
                        CategoryChip(
                            category = catName,
                            isSelected = isSelected,
                            size = ChipSize.Large,
                            onClick = {
                                selectedCategory = if (isSelected) "" else catName
                                showCustomInput = false
                            }
                        )
                    }
                }

                if (showCustomInput) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        label = { Text("Custom Label / Category") },
                        placeholder = { Text("e.g., Medical, Taxes, Receipts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    TextButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom Category Label", fontSize = 12.sp)
                    }
                }

                if (selectedCategory.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Selected: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        CategoryChip(category = selectedCategory, size = ChipSize.Small)
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { selectedCategory = "" }) {
                            Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCategory = if (showCustomInput && customTagInput.isNotBlank()) {
                        customTagInput.trim()
                    } else {
                        selectedCategory.trim()
                    }
                    onCategorySelected(finalCategory)
                }
            ) {
                Text("Save Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
