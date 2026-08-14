package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_records")
data class DocumentRecord(
    @PrimaryKey val uriString: String,
    val title: String = "",
    val path: String = "",
    val type: String = "",
    val fileName: String,
    val mimeType: String,
    val extension: String,
    val fileSize: Long,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val readingProgress: Float = 0f,
    val selectedPage: Int = 1,
    val pageCount: Int = 1,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isPrivate: Boolean = false,
    val encryptedContent: String = "",
    val isEncryptedWithAes256: Boolean = false,
    val category: String = "",
    val tags: String = "",
    val folderId: String = "",
    val folderName: String = ""
)

@Entity(tableName = "folder_records")
data class FolderRecord(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val colorHex: String = "#6750A4",
    val iconName: String = "folder",
    val creationTimestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "bookmark_records")
data class BookmarkRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val title: String,
    val note: String = "",
    val pageOrLine: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val fileType: String
)

@Entity(tableName = "annotation_records")
data class AnnotationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val type: String, // HIGHLIGHT, UNDERLINE, STRIKETHROUGH, NOTE, CALLOUT, DRAWING
    val pageOrLine: Int = 1,
    val textContent: String = "",
    val colorHex: String = "#FFD700",
    val timestamp: Long = System.currentTimeMillis(),
    val extraData: String = "" // e.g. drawing path coordinates
)

@Entity(tableName = "ocr_records")
data class OcrRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUriString: String,
    val extractedText: String,
    val language: String = "eng",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversion_records")
data class ConversionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceUriString: String,
    val targetFormat: String,
    val outputUriString: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "document_metadata")
data class DocumentMetadataRecord(
    @PrimaryKey val uriString: String,
    val title: String,
    val path: String = "",
    val type: String = "",
    val fileType: String = "",
    val fileSize: Long = 0L,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val creationDate: Long = System.currentTimeMillis(),
    val lastModifiedDate: Long = System.currentTimeMillis(),
    val mimeType: String = "",
    val pageCount: Int = 1,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val lineCount: Int = 0,
    val author: String = "",
    val description: String = "",
    val category: String = "",
    val tags: String = ""
)

typealias DocumentMetadataEntity = DocumentMetadataRecord
