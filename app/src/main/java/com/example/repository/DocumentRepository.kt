package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.AnnotationRecord
import com.example.data.database.AppDatabase
import com.example.data.database.BookmarkRecord
import com.example.data.database.ConversionRecord
import com.example.data.database.DocumentMetadataRecord
import com.example.data.database.DocumentRecord
import com.example.data.database.FolderRecord
import com.example.data.database.OcrRecord
import com.example.data.model.AppSettings
import com.example.data.model.DocumentFileType
import com.example.data.model.FileDetails
import com.example.data.model.OpenTab
import com.example.data.util.AesEncryptionHelper
import com.example.data.util.FileHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DocumentRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val docDao = db.documentDao()
    private val bookmarkDao = db.bookmarkDao()
    private val annotationDao = db.annotationDao()
    private val ocrDao = db.ocrDao()
    private val conversionDao = db.conversionDao()
    private val metadataDao = db.documentMetadataDao()
    private val folderDao = db.folderDao()

    val allRecentDocuments: Flow<List<DocumentRecord>> = docDao.getAllRecentDocuments()
    val pinnedDocuments: Flow<List<DocumentRecord>> = docDao.getPinnedDocuments()
    val favoriteDocuments: Flow<List<DocumentRecord>> = docDao.getFavoriteDocuments()
    val privateDocuments: Flow<List<DocumentRecord>> = docDao.getPrivateDocuments()
    val allBookmarks: Flow<List<BookmarkRecord>> = bookmarkDao.getAllBookmarks()
    val allAnnotations: Flow<List<AnnotationRecord>> = annotationDao.getAllAnnotations()
    val allOcrRecords: Flow<List<OcrRecord>> = ocrDao.getAllOcrRecords()
    val allConversions: Flow<List<ConversionRecord>> = conversionDao.getAllConversions()
    val allDocumentMetadata: Flow<List<DocumentMetadataRecord>> = metadataDao.getAllMetadata()
    val allFolders: Flow<List<FolderRecord>> = folderDao.getAllFolders()

    // Active open tabs state
    private val _openTabs = MutableStateFlow<List<OpenTab>>(emptyList())
    val openTabs: StateFlow<List<OpenTab>> = _openTabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    // Application Settings state
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    suspend fun openDocument(uri: Uri): OpenTab {
        val details = FileHelper.getFileDetails(context, uri)
        val existingTab = _openTabs.value.find { it.uriString == uri.toString() }

        if (existingTab != null) {
            _activeTabId.value = existingTab.tabId
            return existingTab
        }

        // Read text content for text-based formats
        val rawContent = if (details.fileType in listOf(
                DocumentFileType.MARKDOWN, DocumentFileType.JSON, DocumentFileType.YAML,
                DocumentFileType.XML, DocumentFileType.CSV, DocumentFileType.CODE, DocumentFileType.TEXT
            )
        ) {
            FileHelper.readTextFromUri(context, uri)
        } else null

        val record = docDao.getDocumentByUri(uri.toString())
        val selectedPage = record?.selectedPage ?: 1
        val scrollPos = (record?.readingProgress ?: 0f * 100).toInt()

        val newTab = OpenTab(
            uriString = uri.toString(),
            fileName = details.name,
            fileType = details.fileType,
            scrollPosition = scrollPos,
            selectedPage = selectedPage,
            content = rawContent
        )

        _openTabs.value = _openTabs.value + newTab
        _activeTabId.value = newTab.tabId

        // Save to Room recent docs
        val existingMetadata = metadataDao.getMetadataForUri(uri.toString())
        val existingCategory: String = if (!record?.category.isNullOrBlank()) record!!.category else (existingMetadata?.category ?: "")
        val existingTags: String = if (!record?.tags.isNullOrBlank()) record!!.tags else (existingMetadata?.tags ?: "")

        docDao.insertOrUpdateDocument(
            DocumentRecord(
                uriString = uri.toString(),
                title = details.name,
                path = details.path,
                type = details.fileType.name,
                fileName = details.name,
                mimeType = details.mimeType,
                extension = details.extension,
                fileSize = details.sizeBytes,
                lastOpenedTimestamp = System.currentTimeMillis(),
                selectedPage = selectedPage,
                isPinned = record?.isPinned ?: false,
                isFavorite = record?.isFavorite ?: false,
                isPrivate = record?.isPrivate ?: false,
                category = existingCategory,
                tags = existingTags
            )
        )

        // Save metadata to Room database
        val wordCount = rawContent?.trim()?.split(Regex("\\s+"))?.filter { it.isNotBlank() }?.size ?: 0
        val charCount = rawContent?.length ?: 0
        val lineCount = rawContent?.lines()?.size ?: 0

        metadataDao.insertOrUpdateMetadata(
            DocumentMetadataRecord(
                uriString = uri.toString(),
                title = existingMetadata?.title ?: details.name,
                path = details.path,
                type = details.fileType.name,
                fileType = details.fileType.name,
                fileSize = details.sizeBytes,
                lastOpenedTimestamp = System.currentTimeMillis(),
                creationDate = existingMetadata?.creationDate ?: System.currentTimeMillis(),
                lastModifiedDate = details.lastModified,
                mimeType = details.mimeType,
                wordCount = wordCount,
                characterCount = charCount,
                lineCount = lineCount,
                author = existingMetadata?.author ?: "",
                description = existingMetadata?.description ?: "",
                category = existingCategory,
                tags = existingTags
            )
        )

        return newTab
    }

    fun getDocumentMetadataFlow(uriString: String): Flow<DocumentMetadataRecord?> =
        metadataDao.getMetadataForUriFlow(uriString)

    suspend fun getDocumentMetadata(uriString: String): DocumentMetadataRecord? =
        metadataDao.getMetadataForUri(uriString)

    suspend fun saveDocumentMetadata(metadata: DocumentMetadataRecord) {
        metadataDao.insertOrUpdateMetadata(metadata)
        if (metadata.category.isNotEmpty()) {
            docDao.updateDocumentCategory(metadata.uriString, metadata.category)
        }
    }

    suspend fun updateDocumentCategory(uriString: String, category: String) {
        docDao.updateDocumentCategory(uriString, category)
        metadataDao.updateCategory(uriString, category)
    }

    suspend fun updateDocumentTags(uriString: String, tags: String) {
        docDao.updateDocumentTags(uriString, tags)
        metadataDao.updateTags(uriString, tags)
    }

    fun getDocumentsByCategory(category: String): Flow<List<DocumentRecord>> {
        return docDao.getDocumentsByCategory(category)
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun closeTab(tabId: String) {
        val currentTabs = _openTabs.value.toMutableList()
        val index = currentTabs.indexOfFirst { it.tabId == tabId }
        if (index != -1) {
            currentTabs.removeAt(index)
            _openTabs.value = currentTabs
            if (_activeTabId.value == tabId) {
                _activeTabId.value = currentTabs.lastOrNull()?.tabId
            }
        }
    }

    fun closeAllTabs() {
        _openTabs.value = emptyList()
        _activeTabId.value = null
    }

    fun updateTabContent(tabId: String, newContent: String) {
        _openTabs.value = _openTabs.value.map { tab ->
            if (tab.tabId == tabId) {
                tab.copy(content = newContent, isEdited = true)
            } else tab
        }
    }

    fun saveTabContent(tabId: String): Boolean {
        val tab = _openTabs.value.find { it.tabId == tabId } ?: return false
        val uri = Uri.parse(tab.uriString)
        val content = tab.content ?: return false

        val success = FileHelper.writeTextToUri(context, uri, content)
        if (success) {
            _openTabs.value = _openTabs.value.map {
                if (it.tabId == tabId) it.copy(isEdited = false) else it
            }
        }
        return success
    }

    suspend fun updateDocumentProgress(uriString: String, page: Int, progress: Float) {
        val record = docDao.getDocumentByUri(uriString)
        if (record != null) {
            docDao.insertOrUpdateDocument(
                record.copy(
                    selectedPage = page,
                    readingProgress = progress,
                    lastOpenedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun togglePinDocument(uriString: String) {
        val record = docDao.getDocumentByUri(uriString)
        if (record != null) {
            docDao.insertOrUpdateDocument(record.copy(isPinned = !record.isPinned))
        }
    }

    suspend fun toggleFavoriteDocument(uriString: String) {
        val record = docDao.getDocumentByUri(uriString)
        if (record != null) {
            docDao.insertOrUpdateDocument(record.copy(isFavorite = !record.isFavorite))
        }
    }

    suspend fun deleteDocumentRecord(uriString: String) {
        docDao.deleteDocumentByUri(uriString)
        metadataDao.deleteMetadataByUri(uriString)
        val tabToClose = _openTabs.value.find { it.uriString == uriString }
        if (tabToClose != null) {
            closeTab(tabToClose.tabId)
        }
    }

    suspend fun deleteMultipleDocumentRecords(uriStrings: List<String>) {
        uriStrings.forEach { uri ->
            docDao.deleteDocumentByUri(uri)
            metadataDao.deleteMetadataByUri(uri)
            val tabToClose = _openTabs.value.find { it.uriString == uri }
            if (tabToClose != null) {
                closeTab(tabToClose.tabId)
            }
        }
    }

    suspend fun addBookmark(uriString: String, title: String, note: String, pageOrLine: Int, fileType: String) {
        bookmarkDao.insertBookmark(
            BookmarkRecord(
                uriString = uriString,
                title = title,
                note = note,
                pageOrLine = pageOrLine,
                fileType = fileType
            )
        )
    }

    suspend fun addAnnotation(uriString: String, type: String, textContent: String, colorHex: String, pageOrLine: Int) {
        annotationDao.insertAnnotation(
            AnnotationRecord(
                uriString = uriString,
                type = type,
                textContent = textContent,
                colorHex = colorHex,
                pageOrLine = pageOrLine
            )
        )
    }

    suspend fun saveOcrRecord(imageUriString: String, extractedText: String, language: String) {
        ocrDao.insertOcrRecord(
            OcrRecord(
                imageUriString = imageUriString,
                extractedText = extractedText,
                language = language
            )
        )
    }

    suspend fun saveConversionRecord(sourceUri: String, targetFormat: String, outputUri: String) {
        conversionDao.insertConversion(
            ConversionRecord(
                sourceUriString = sourceUri,
                targetFormat = targetFormat,
                outputUriString = outputUri
            )
        )
    }

    suspend fun importToPrivateFolder(context: Context, sourceUri: Uri, move: Boolean): FileDetails? {
        val privateDir = File(context.filesDir, "private_vault")
        if (!privateDir.exists()) privateDir.mkdirs()

        val sourceDetails = FileHelper.getFileDetails(context, sourceUri)
        val targetFile = File(privateDir, "vault_${System.currentTimeMillis()}_${sourceDetails.name}")

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val targetUri = Uri.fromFile(targetFile)
            val details = FileHelper.getFileDetails(context, targetUri)

            docDao.insertOrUpdateDocument(
                DocumentRecord(
                    uriString = targetUri.toString(),
                    fileName = details.name,
                    mimeType = details.mimeType,
                    extension = details.extension,
                    fileSize = details.sizeBytes,
                    isPrivate = true
                )
            )

            if (move && sourceUri.scheme == "file") {
                File(sourceUri.path ?: "").delete()
            }

            return details
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun performDatabaseBackup(folderUriString: String?): Boolean {
        return try {
            val dbFile = context.getDatabasePath("ls_docs_database")
            if (!dbFile.exists()) return false

            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
            val backupFileName = "ls_docs_backup_$timestamp.db"

            if (!folderUriString.isNullOrEmpty()) {
                val folderUri = Uri.parse(folderUriString)
                if (folderUri.scheme == "file" || folderUri.path?.startsWith("/") == true) {
                    val targetDir = File(folderUri.path ?: "")
                    if (targetDir.exists() || targetDir.mkdirs()) {
                        val backupFile = File(targetDir, backupFileName)
                        dbFile.copyTo(backupFile, overwrite = true)
                        return true
                    }
                } else if (folderUri.scheme == "content") {
                    try {
                        context.contentResolver.openOutputStream(folderUri)?.use { output ->
                            dbFile.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }
                        return true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Fallback: save to local app backup directory
            val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            val backupFile = File(backupDir, backupFileName)
            dbFile.copyTo(backupFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun encryptDocumentRecordInRoom(uriString: String, rawContent: String): Boolean {
        val record = docDao.getDocumentByUri(uriString) ?: return false
        val encrypted = AesEncryptionHelper.encryptText(rawContent)
        val updatedRecord = record.copy(
            encryptedContent = encrypted,
            isEncryptedWithAes256 = true
        )
        docDao.insertOrUpdateDocument(updatedRecord)
        return true
    }

    suspend fun decryptDocumentRecordInRoom(uriString: String): String? {
        val record = docDao.getDocumentByUri(uriString) ?: return null
        if (!record.isEncryptedWithAes256 || record.encryptedContent.isEmpty()) {
            return null
        }
        return AesEncryptionHelper.decryptText(record.encryptedContent)
    }

    suspend fun toggleDocumentRoomEncryption(uriString: String, rawContent: String): Boolean {
        val record = docDao.getDocumentByUri(uriString) ?: return false
        if (record.isEncryptedWithAes256) {
            val updatedRecord = record.copy(
                encryptedContent = "",
                isEncryptedWithAes256 = false
            )
            docDao.insertOrUpdateDocument(updatedRecord)
            return false
        } else {
            val encrypted = AesEncryptionHelper.encryptText(rawContent)
            val updatedRecord = record.copy(
                encryptedContent = encrypted,
                isEncryptedWithAes256 = true
            )
            docDao.insertOrUpdateDocument(updatedRecord)
            return true
        }
    }

    suspend fun createFolder(name: String, description: String = "", colorHex: String = "#6750A4"): FolderRecord {
        val folder = FolderRecord(
            name = name.trim(),
            description = description.trim(),
            colorHex = colorHex
        )
        folderDao.insertOrUpdateFolder(folder)
        return folder
    }

    suspend fun updateFolder(folder: FolderRecord) {
        folderDao.insertOrUpdateFolder(folder)
    }

    suspend fun deleteFolder(folderId: String) {
        docDao.unassignDocumentsFromFolder(folderId)
        folderDao.deleteFolderById(folderId)
    }

    suspend fun togglePinFolder(folderId: String) {
        folderDao.togglePinFolder(folderId)
    }

    suspend fun moveDocumentToFolder(uriString: String, folderId: String, folderName: String) {
        docDao.moveDocumentToFolder(uriString, folderId, folderName)
        metadataDao.getMetadataForUri(uriString)?.let { meta ->
            metadataDao.insertOrUpdateMetadata(meta.copy(category = folderName))
        }
    }

    suspend fun moveMultipleDocumentsToFolder(uriStrings: List<String>, folderId: String, folderName: String) {
        uriStrings.forEach { uri ->
            moveDocumentToFolder(uri, folderId, folderName)
        }
    }

    suspend fun removeDocumentFromFolder(uriString: String) {
        docDao.moveDocumentToFolder(uriString, "", "")
    }

    suspend fun toggleDocumentFavorite(uri: Uri): Boolean {
        val uriString = uri.toString()
        val record = docDao.getDocumentByUri(uriString)
        if (record != null) {
            val newFav = !record.isFavorite
            docDao.insertOrUpdateDocument(record.copy(isFavorite = newFav))
            return newFav
        } else {
            val details = FileHelper.getFileDetails(context, uri)
            val newRecord = DocumentRecord(
                uriString = uriString,
                title = details.name,
                fileName = details.name,
                path = details.path,
                type = details.fileType.name,
                mimeType = details.mimeType,
                extension = details.extension,
                fileSize = details.sizeBytes,
                isFavorite = true
            )
            docDao.insertOrUpdateDocument(newRecord)
            return true
        }
    }

    suspend fun toggleDocumentPin(uri: Uri): Boolean {
        val uriString = uri.toString()
        val record = docDao.getDocumentByUri(uriString)
        if (record != null) {
            val newPinned = !record.isPinned
            docDao.insertOrUpdateDocument(record.copy(isPinned = newPinned))
            return newPinned
        } else {
            val details = FileHelper.getFileDetails(context, uri)
            val newRecord = DocumentRecord(
                uriString = uriString,
                title = details.name,
                fileName = details.name,
                path = details.path,
                type = details.fileType.name,
                mimeType = details.mimeType,
                extension = details.extension,
                fileSize = details.sizeBytes,
                isPinned = true
            )
            docDao.insertOrUpdateDocument(newRecord)
            return true
        }
    }
}
