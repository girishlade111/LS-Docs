package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document_records WHERE isPrivate = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getAllRecentDocuments(): Flow<List<DocumentRecord>>

    @Query("SELECT * FROM document_records WHERE isPinned = 1 AND isPrivate = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getPinnedDocuments(): Flow<List<DocumentRecord>>

    @Query("SELECT * FROM document_records WHERE isFavorite = 1 AND isPrivate = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentRecord>>

    @Query("SELECT * FROM document_records WHERE isPrivate = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getPrivateDocuments(): Flow<List<DocumentRecord>>

    @Query("SELECT * FROM document_records WHERE uriString = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): DocumentRecord?

    @Query("SELECT * FROM document_records WHERE category = :category AND isPrivate = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getDocumentsByCategory(category: String): Flow<List<DocumentRecord>>

    @Query("SELECT * FROM document_records WHERE folderId = :folderId AND isPrivate = 0 ORDER BY lastOpenedTimestamp DESC")
    fun getDocumentsByFolderId(folderId: String): Flow<List<DocumentRecord>>

    @Query("UPDATE document_records SET category = :category WHERE uriString = :uri")
    suspend fun updateDocumentCategory(uri: String, category: String)

    @Query("UPDATE document_records SET folderId = :folderId, folderName = :folderName WHERE uriString = :uri")
    suspend fun moveDocumentToFolder(uri: String, folderId: String, folderName: String)

    @Query("UPDATE document_records SET folderId = '', folderName = '' WHERE folderId = :folderId")
    suspend fun unassignDocumentsFromFolder(folderId: String)

    @Query("UPDATE document_records SET isFavorite = NOT isFavorite WHERE uriString = :uri")
    suspend fun toggleFavorite(uri: String)

    @Query("UPDATE document_records SET isPinned = NOT isPinned WHERE uriString = :uri")
    suspend fun togglePin(uri: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDocument(doc: DocumentRecord)

    @Query("DELETE FROM document_records WHERE uriString = :uri")
    suspend fun deleteDocumentByUri(uri: String)

    @Query("DELETE FROM document_records")
    suspend fun clearAllRecentDocuments()
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folder_records ORDER BY isPinned DESC, creationTimestamp ASC")
    fun getAllFolders(): Flow<List<FolderRecord>>

    @Query("SELECT * FROM folder_records WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderRecord?

    @Query("SELECT * FROM folder_records WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): FolderRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFolder(folder: FolderRecord)

    @Query("UPDATE folder_records SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePinFolder(id: String)

    @Query("DELETE FROM folder_records WHERE id = :id")
    suspend fun deleteFolderById(id: String)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark_records WHERE uriString = :uri ORDER BY pageOrLine ASC")
    fun getBookmarksForUri(uri: String): Flow<List<BookmarkRecord>>

    @Query("SELECT * FROM bookmark_records ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkRecord)

    @Query("DELETE FROM bookmark_records WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
}

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotation_records WHERE uriString = :uri ORDER BY pageOrLine ASC")
    fun getAnnotationsForUri(uri: String): Flow<List<AnnotationRecord>>

    @Query("SELECT * FROM annotation_records ORDER BY timestamp DESC")
    fun getAllAnnotations(): Flow<List<AnnotationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationRecord)

    @Query("DELETE FROM annotation_records WHERE id = :id")
    suspend fun deleteAnnotationById(id: Long)
}

@Dao
interface OcrDao {
    @Query("SELECT * FROM ocr_records ORDER BY timestamp DESC")
    fun getAllOcrRecords(): Flow<List<OcrRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOcrRecord(record: OcrRecord)

    @Query("DELETE FROM ocr_records WHERE id = :id")
    suspend fun deleteOcrRecordById(id: Long)

    @Query("DELETE FROM ocr_records")
    suspend fun clearOcrRecords()
}

@Dao
interface ConversionDao {
    @Query("SELECT * FROM conversion_records ORDER BY timestamp DESC")
    fun getAllConversions(): Flow<List<ConversionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(record: ConversionRecord)

    @Query("DELETE FROM conversion_records WHERE id = :id")
    suspend fun deleteConversionById(id: Long)

    @Query("DELETE FROM conversion_records")
    suspend fun clearConversions()
}

@Dao
interface DocumentMetadataDao {
    @Query("SELECT * FROM document_metadata WHERE uriString = :uri LIMIT 1")
    fun getMetadataForUriFlow(uri: String): Flow<DocumentMetadataRecord?>

    @Query("SELECT * FROM document_metadata WHERE uriString = :uri LIMIT 1")
    suspend fun getMetadataForUri(uri: String): DocumentMetadataRecord?

    @Query("SELECT * FROM document_metadata ORDER BY lastOpenedTimestamp DESC")
    fun getAllMetadata(): Flow<List<DocumentMetadataRecord>>

    @Query("SELECT * FROM document_metadata ORDER BY lastOpenedTimestamp DESC")
    fun getAllMetadataOrderedByLastOpened(): Flow<List<DocumentMetadataRecord>>

    @Query("SELECT * FROM document_metadata WHERE title LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%' ORDER BY lastOpenedTimestamp DESC")
    fun searchMetadata(query: String): Flow<List<DocumentMetadataRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetadata(metadata: DocumentMetadataRecord)

    @Query("UPDATE document_metadata SET lastOpenedTimestamp = :timestamp WHERE uriString = :uri")
    suspend fun updateLastOpenedTimestamp(uri: String, timestamp: Long)

    @Query("UPDATE document_metadata SET category = :category WHERE uriString = :uri")
    suspend fun updateCategory(uri: String, category: String)

    @Query("DELETE FROM document_metadata WHERE uriString = :uri")
    suspend fun deleteMetadataByUri(uri: String)

    @Query("DELETE FROM document_metadata")
    suspend fun clearAllMetadata()
}
