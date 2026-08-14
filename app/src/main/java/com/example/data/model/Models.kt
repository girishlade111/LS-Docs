package com.example.data.model

import android.net.Uri

enum class DocumentFileType(
    val displayName: String,
    val extensions: List<String>,
    val defaultIconName: String
) {
    PDF("PDF Document", listOf("pdf"), "pdf"),
    MARKDOWN("Markdown", listOf("md", "markdown", "mdown", "mkd"), "markdown"),
    JSON("JSON Data", listOf("json"), "json"),
    YAML("YAML Data", listOf("yaml", "yml"), "yaml"),
    XML("XML Data", listOf("xml"), "xml"),
    CSV("CSV Spreadsheet", listOf("csv", "tsv"), "csv"),
    CODE("Source Code", listOf("kt", "java", "py", "js", "ts", "cpp", "c", "cs", "go", "rs", "php", "sql", "sh", "dart", "swift", "rb", "lua", "html", "css", "toml", "ini", "properties"), "code"),
    EPUB("EPUB eBook", listOf("epub"), "epub"),
    IMAGE("Image", listOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "svg", "heic", "heif"), "image"),
    TEXT("Plain Text", listOf("txt", "log", "conf", "cfg", "env"), "text"),
    ZIP("ZIP Archive", listOf("zip", "rar", "tar", "gz", "7z"), "zip"),
    HEX("Binary / Unknown", emptyList(), "hex");

    companion object {
        fun fromExtension(ext: String?): DocumentFileType {
            if (ext.isNullOrEmpty()) return HEX
            val cleanExt = ext.lowercase().removePrefix(".")
            return values().firstOrNull { type -> type.extensions.contains(cleanExt) } ?: TEXT
        }

        fun fromMimeType(mimeType: String?): DocumentFileType {
            val safeMime = mimeType?.lowercase() ?: return HEX
            return when {
                safeMime.contains("pdf") -> PDF
                safeMime.contains("json") -> JSON
                safeMime.contains("yaml") -> YAML
                safeMime.contains("xml") -> XML
                safeMime.contains("csv") -> CSV
                safeMime.contains("epub") -> EPUB
                safeMime.contains("image") -> IMAGE
                safeMime.contains("zip") -> ZIP
                safeMime.startsWith("text/") -> TEXT
                else -> HEX
            }
        }
    }
}

enum class ReaderTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SEPIA("Sepia"),
    HIGH_CONTRAST("High Contrast"),
    BLACK("OLED Black")
}

enum class CodeSyntaxTheme(val displayName: String) {
    DARK_MODERN("Dark Modern"),
    LIGHT_CLEAN("Light Clean"),
    MONOKAI("Monokai"),
    SOLARIZED("Solarized Dark"),
    HIGH_CONTRAST("High Contrast")
}

data class OpenTab(
    val tabId: String = java.util.UUID.randomUUID().toString(),
    val uriString: String,
    val fileName: String,
    val fileType: DocumentFileType,
    val scrollPosition: Int = 0,
    val selectedPage: Int = 1,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val content: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

data class FileDetails(
    val name: String,
    val uri: Uri,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val mimeType: String,
    val extension: String,
    val isReadOnly: Boolean,
    val fileType: DocumentFileType
)

data class DiffResult(
    val file1Name: String,
    val file2Name: String,
    val lineDiffs: List<LineDiff>,
    val addedCount: Int,
    val deletedCount: Int,
    val modifiedCount: Int,
    val isIdentical: Boolean
)

data class LineDiff(
    val lineNumber1: Int?,
    val lineNumber2: Int?,
    val text1: String?,
    val text2: String?,
    val type: DiffType
)

enum class DiffType {
    UNCHANGED, ADDED, DELETED, MODIFIED
}

data class DuplicateGroup(
    val sizeBytes: Long,
    val sampleHash: String,
    val files: List<FileDetails>
)

data class ZipEntryItem(
    val name: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long,
    val compressedSize: Long,
    val path: String
)

data class AppSettings(
    val isDarkTheme: Boolean = true,
    val glassmorphismEnabled: Boolean = true,
    val glassmorphismBlurIntensity: Float = 0.8f,
    val language: String = "system",
    val fontScale: Float = 1.0f,
    val codeFontSizeSp: Int = 14,
    val readerFontSizeSp: Int = 16,
    val codeFontFamily: String = "Monospace",
    val readerTheme: ReaderTheme = ReaderTheme.DARK,
    val syntaxTheme: CodeSyntaxTheme = CodeSyntaxTheme.DARK_MODERN,
    val restoreTabsOnRestart: Boolean = true,
    val rememberReadingPosition: Boolean = true,
    val keepScreenAwake: Boolean = false,
    val confirmOverwrite: Boolean = true,
    val confirmExternalLinks: Boolean = true,
    val appLockEnabled: Boolean = false,
    val pinHash: String = "",
    val biometricsEnabled: Boolean = false,
    val hideContentInAppSwitcher: Boolean = true,
    val hideRecentFileNamesInPrivacy: Boolean = false,
    val defaultPdfMode: String = "vertical",
    val defaultMarkdownMode: String = "split",
    val defaultCodeWordWrap: Boolean = true,
    val defaultOcrLanguage: String = "eng",
    val highContrastMode: Boolean = false,
    val reducedMotion: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val backupFolderUri: String = "",
    val backupIntervalHours: Int = 24,
    val lastBackupTimestamp: Long = 0L,
    val globalWatermarkString: String = "CONFIDENTIAL - OFFICIAL COPY",
    val autoApplyWatermarkOnPdfExport: Boolean = true,
    val globalWatermarkStyle: String = "Diagonal Center"
)
