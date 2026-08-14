package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.abs

data class CategoryColorScheme(
    val primaryColor: Color,
    val containerColor: Color,
    val textColor: Color,
    val borderColor: Color
)

enum class DocumentCategory(
    val displayName: String,
    val icon: ImageVector,
    val lightPrimary: Color,
    val lightContainer: Color,
    val lightText: Color,
    val darkPrimary: Color,
    val darkContainer: Color,
    val darkText: Color
) {
    WORK(
        displayName = "Work",
        icon = Icons.Default.Work,
        lightPrimary = Color(0xFF1E88E5),
        lightContainer = Color(0xFFE3F2FD),
        lightText = Color(0xFF0D47A1),
        darkPrimary = Color(0xFF90CAF9),
        darkContainer = Color(0xFF0D2A4A),
        darkText = Color(0xFFBBDEFB)
    ),
    PERSONAL(
        displayName = "Personal",
        icon = Icons.Default.Person,
        lightPrimary = Color(0xFF2E7D32),
        lightContainer = Color(0xFFE8F5E9),
        lightText = Color(0xFF1B5E20),
        darkPrimary = Color(0xFFA5D6A7),
        darkContainer = Color(0xFF0E3314),
        darkText = Color(0xFFC8E6C9)
    ),
    LEGAL(
        displayName = "Legal",
        icon = Icons.Default.AccountBalance,
        lightPrimary = Color(0xFF6A1B9A),
        lightContainer = Color(0xFFF3E5F5),
        lightText = Color(0xFF4A148C),
        darkPrimary = Color(0xFFCE93D8),
        darkContainer = Color(0xFF300D47),
        darkText = Color(0xFFE1BEE7)
    ),
    FINANCIAL(
        displayName = "Financial",
        icon = Icons.Default.Payments,
        lightPrimary = Color(0xFFE65100),
        lightContainer = Color(0xFFFFF3E0),
        lightText = Color(0xFFBF360C),
        darkPrimary = Color(0xFFFFB74D),
        darkContainer = Color(0xFF421A00),
        darkText = Color(0xFFFFE0B2)
    ),
    PROJECT(
        displayName = "Project",
        icon = Icons.Default.Assignment,
        lightPrimary = Color(0xFF00695C),
        lightContainer = Color(0xFFE0F2F1),
        lightText = Color(0xFF004D40),
        darkPrimary = Color(0xFF80CBC4),
        darkContainer = Color(0xFF002D27),
        darkText = Color(0xFFB2DFDB)
    ),
    STUDY(
        displayName = "Study",
        icon = Icons.Default.School,
        lightPrimary = Color(0xFFC2185B),
        lightContainer = Color(0xFFFCE4EC),
        lightText = Color(0xFF880E4F),
        darkPrimary = Color(0xFFF48FB1),
        darkContainer = Color(0xFF450720),
        darkText = Color(0xFFF8BBD0)
    ),
    ARCHIVE(
        displayName = "Archive",
        icon = Icons.Default.Archive,
        lightPrimary = Color(0xFF455A64),
        lightContainer = Color(0xFFECEFF1),
        lightText = Color(0xFF263238),
        darkPrimary = Color(0xFFB0BEC5),
        darkContainer = Color(0xFF1C252A),
        darkText = Color(0xFFCFD8DC)
    ),
    IMPORTANT(
        displayName = "Important",
        icon = Icons.Default.Star,
        lightPrimary = Color(0xFFC62828),
        lightContainer = Color(0xFFFFEBEE),
        lightText = Color(0xFFB71C1C),
        darkPrimary = Color(0xFFEF9A9A),
        darkContainer = Color(0xFF450909),
        darkText = Color(0xFFFFCDD2)
    );

    companion object {
        val ALL_CATEGORIES = values().toList()

        fun fromString(name: String?): DocumentCategory? {
            if (name.isNullOrBlank()) return null
            return values().firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) || it.name.equals(name.trim(), ignoreCase = true) }
        }

        fun getCategoryNames(): List<String> {
            return values().map { it.displayName }
        }

        fun getColorScheme(categoryName: String?, isDarkTheme: Boolean): CategoryColorScheme {
            val cat = fromString(categoryName)
            if (cat != null) {
                return if (isDarkTheme) {
                    CategoryColorScheme(
                        primaryColor = cat.darkPrimary,
                        containerColor = cat.darkContainer,
                        textColor = cat.darkText,
                        borderColor = cat.darkPrimary.copy(alpha = 0.5f)
                    )
                } else {
                    CategoryColorScheme(
                        primaryColor = cat.lightPrimary,
                        containerColor = cat.lightContainer,
                        textColor = cat.lightText,
                        borderColor = cat.lightPrimary.copy(alpha = 0.35f)
                    )
                }
            }

            if (categoryName.isNullOrBlank()) {
                return if (isDarkTheme) {
                    CategoryColorScheme(
                        primaryColor = Color(0xFF9E9E9E),
                        containerColor = Color(0xFF212121),
                        textColor = Color(0xFFE0E0E0),
                        borderColor = Color(0xFF424242)
                    )
                } else {
                    CategoryColorScheme(
                        primaryColor = Color(0xFF757575),
                        containerColor = Color(0xFFF5F5F5),
                        textColor = Color(0xFF424242),
                        borderColor = Color(0xFFE0E0E0)
                    )
                }
            }

            // Custom user tag - generate deterministic dynamic palette
            val hash = abs(categoryName.hashCode())
            val huePalette = listOf(
                Pair(Color(0xFF0288D1), Color(0xFFE1F5FE)), // Light Blue
                Pair(Color(0xFF7B1FA2), Color(0xFFF3E5F5)), // Purple
                Pair(Color(0xFF00796B), Color(0xFFE0F2F1)), // Teal
                Pair(Color(0xFFE64A19), Color(0xFFFBE9E7)), // Deep Orange
                Pair(Color(0xFF5D4037), Color(0xFFEFEBE9)), // Brown
                Pair(Color(0xFF388E3C), Color(0xFFE8F5E9)), // Green
                Pair(Color(0xFF512DA8), Color(0xFFEDE7F6)), // Deep Purple
                Pair(Color(0xFFC2185B), Color(0xFFFCE4EC))  // Pink
            )
            val selected = huePalette[hash % huePalette.size]

            return if (isDarkTheme) {
                CategoryColorScheme(
                    primaryColor = selected.first.copy(alpha = 0.85f),
                    containerColor = selected.first.copy(alpha = 0.2f),
                    textColor = Color(0xFFE6E1E5),
                    borderColor = selected.first.copy(alpha = 0.4f)
                )
            } else {
                CategoryColorScheme(
                    primaryColor = selected.first,
                    containerColor = selected.second,
                    textColor = selected.first,
                    borderColor = selected.first.copy(alpha = 0.3f)
                )
            }
        }

        fun getIcon(categoryName: String?): ImageVector {
            val cat = fromString(categoryName)
            return cat?.icon ?: Icons.Default.Category
        }
    }
}
