package com.example.data.model

import com.example.data.database.DocumentMetadataRecord
import com.example.data.database.DocumentRecord

enum class DocumentSortOption(
    val displayName: String,
    val shortLabel: String
) {
    LAST_OPENED("Last Opened", "Last Opened"),
    DATE_ADDED("Date Added", "Date Added"),
    TITLE_AZ("Title (A-Z)", "Title (A-Z)"),
    TITLE_ZA("Title (Z-A)", "Title (Z-A)");

    companion object {
        val DEFAULT = LAST_OPENED
    }
}

/**
 * Sorts a list of [DocumentRecord] based on the selected [DocumentSortOption].
 */
fun List<DocumentRecord>.sortDocuments(
    sortOption: DocumentSortOption,
    metadataList: List<DocumentMetadataRecord> = emptyList()
): List<DocumentRecord> {
    val metadataMap = metadataList.associateBy { it.uriString }
    return when (sortOption) {
        DocumentSortOption.LAST_OPENED -> this.sortedByDescending { it.lastOpenedTimestamp }
        DocumentSortOption.DATE_ADDED -> this.sortedByDescending { doc ->
            metadataMap[doc.uriString]?.creationDate ?: doc.lastOpenedTimestamp
        }
        DocumentSortOption.TITLE_AZ -> this.sortedBy { doc ->
            val docTitle = doc.title.takeIf { it.isNotBlank() } ?: doc.fileName
            docTitle.lowercase()
        }
        DocumentSortOption.TITLE_ZA -> this.sortedByDescending { doc ->
            val docTitle = doc.title.takeIf { it.isNotBlank() } ?: doc.fileName
            docTitle.lowercase()
        }
    }
}

/**
 * Sorts a list of [FileDetails] based on the selected [DocumentSortOption],
 * optionally checking [recentDocs] and [metadataList] for timestamps and titles.
 */
fun List<FileDetails>.sortFiles(
    sortOption: DocumentSortOption,
    recentDocs: List<DocumentRecord> = emptyList(),
    metadataList: List<DocumentMetadataRecord> = emptyList()
): List<FileDetails> {
    val recentMap = recentDocs.associateBy { it.uriString }
    val metadataMap = metadataList.associateBy { it.uriString }

    return when (sortOption) {
        DocumentSortOption.LAST_OPENED -> this.sortedByDescending { file ->
            recentMap[file.uri.toString()]?.lastOpenedTimestamp ?: file.lastModified
        }
        DocumentSortOption.DATE_ADDED -> this.sortedByDescending { file ->
            metadataMap[file.uri.toString()]?.creationDate ?: file.lastModified
        }
        DocumentSortOption.TITLE_AZ -> this.sortedBy { file ->
            val docTitle = recentMap[file.uri.toString()]?.title
            (if (!docTitle.isNullOrBlank()) docTitle else file.name).lowercase()
        }
        DocumentSortOption.TITLE_ZA -> this.sortedByDescending { file ->
            val docTitle = recentMap[file.uri.toString()]?.title
            (if (!docTitle.isNullOrBlank()) docTitle else file.name).lowercase()
        }
    }
}
