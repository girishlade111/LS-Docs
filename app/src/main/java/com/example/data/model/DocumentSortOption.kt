package com.example.data.model

import com.example.data.database.DocumentRecord

enum class DocumentSortOption(
    val displayName: String,
    val shortLabel: String,
    val isDateSort: Boolean = false,
    val isNameSort: Boolean = false
) {
    DATE_ADDED_DESC("Date Added (Newest)", "Date Added", isDateSort = true),
    DATE_ADDED_ASC("Date Added (Oldest)", "Date Added (Oldest)", isDateSort = true),
    NAME_ASC("Name (A to Z)", "Name (A-Z)", isNameSort = true),
    NAME_DESC("Name (Z to A)", "Name (Z-A)", isNameSort = true);

    companion object {
        val DEFAULT = DATE_ADDED_DESC
    }
}

/**
 * Sorts a list of [DocumentRecord] based on the selected [DocumentSortOption].
 */
fun List<DocumentRecord>.sortDocuments(sortOption: DocumentSortOption): List<DocumentRecord> {
    return when (sortOption) {
        DocumentSortOption.DATE_ADDED_DESC -> this.sortedByDescending { it.lastOpenedTimestamp }
        DocumentSortOption.DATE_ADDED_ASC -> this.sortedBy { it.lastOpenedTimestamp }
        DocumentSortOption.NAME_ASC -> this.sortedBy { it.fileName.lowercase() }
        DocumentSortOption.NAME_DESC -> this.sortedByDescending { it.fileName.lowercase() }
    }
}

/**
 * Sorts a list of [FileDetails] based on the selected [DocumentSortOption],
 * optionally checking [recentDocs] for the latest opened timestamp.
 */
fun List<FileDetails>.sortFiles(
    sortOption: DocumentSortOption,
    recentDocs: List<DocumentRecord> = emptyList()
): List<FileDetails> {
    val recentTimestampMap = recentDocs.associate { it.uriString to it.lastOpenedTimestamp }

    return when (sortOption) {
        DocumentSortOption.DATE_ADDED_DESC -> this.sortedByDescending { file ->
            recentTimestampMap[file.uri.toString()] ?: file.lastModified
        }
        DocumentSortOption.DATE_ADDED_ASC -> this.sortedBy { file ->
            recentTimestampMap[file.uri.toString()] ?: file.lastModified
        }
        DocumentSortOption.NAME_ASC -> this.sortedBy { it.name.lowercase() }
        DocumentSortOption.NAME_DESC -> this.sortedByDescending { it.name.lowercase() }
    }
}
