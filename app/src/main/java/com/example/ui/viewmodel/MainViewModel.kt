package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AnnotationRecord
import com.example.data.database.BookmarkRecord
import com.example.data.database.ConversionRecord
import com.example.data.database.DocumentMetadataRecord
import com.example.data.database.DocumentRecord
import com.example.data.database.FolderRecord
import com.example.data.database.OcrRecord
import com.example.data.model.AppSettings
import com.example.data.model.DiffResult
import com.example.data.model.DocumentFileType
import com.example.data.model.DuplicateGroup
import com.example.data.model.FileDetails
import com.example.data.model.OpenTab
import com.example.data.util.DiffEngine
import com.example.data.util.DuplicateScanner
import com.example.data.util.FileHelper
import com.example.data.util.OcrEngine
import com.example.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DocumentRepository(application)

    fun showToast(message: String) {
        try {
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val recentDocuments: StateFlow<List<DocumentRecord>> = repository.allRecentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedDocuments: StateFlow<List<DocumentRecord>> = repository.pinnedDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<DocumentRecord>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateDocuments: StateFlow<List<DocumentRecord>> = repository.privateDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkRecord>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val annotations: StateFlow<List<AnnotationRecord>> = repository.allAnnotations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ocrRecords: StateFlow<List<OcrRecord>> = repository.allOcrRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversions: StateFlow<List<ConversionRecord>> = repository.allConversions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocumentMetadata: StateFlow<List<DocumentMetadataRecord>> = repository.allDocumentMetadata
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderRecord>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openTabs: StateFlow<List<OpenTab>> = repository.openTabs
    val activeTabId: StateFlow<String?> = repository.activeTabId
    val settings: StateFlow<AppSettings> = repository.settings

    private val _isPrivateVaultUnlocked = MutableStateFlow(false)
    val isPrivateVaultUnlocked: StateFlow<Boolean> = _isPrivateVaultUnlocked.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _sampleFiles = MutableStateFlow<List<FileDetails>>(emptyList())
    val sampleFiles: StateFlow<List<FileDetails>> = _sampleFiles.asStateFlow()
    val deviceFiles: StateFlow<List<FileDetails>> = sampleFiles

    private val _activeDiffResult = MutableStateFlow<DiffResult?>(null)
    val activeDiffResult: StateFlow<DiffResult?> = _activeDiffResult.asStateFlow()

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    private val _ocrProcessingResult = MutableStateFlow<String?>(null)
    val ocrProcessingResult: StateFlow<String?> = _ocrProcessingResult.asStateFlow()

    init {
        refreshDeviceFiles()
    }

    fun refreshDeviceFiles() {
        viewModelScope.launch {
            _sampleFiles.value = FileHelper.getDeviceFiles(getApplication())
        }
    }

    fun refreshDeviceFilesManually() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _sampleFiles.value = FileHelper.getDeviceFiles(getApplication())
            kotlinx.coroutines.delay(600)
            _isRefreshing.value = false
            showToast("Device storage re-scanned successfully (${_sampleFiles.value.size} documents found)")
        }
    }

    fun openDocument(uri: Uri) {
        viewModelScope.launch {
            repository.openDocument(uri)
            refreshDeviceFiles()
            showToast("Opening document...")
        }
    }

    fun selectTab(tabId: String) {
        repository.selectTab(tabId)
    }

    fun closeTab(tabId: String) {
        repository.closeTab(tabId)
    }

    fun closeAllTabs() {
        repository.closeAllTabs()
        showToast("All workspace tabs closed")
    }

    fun updateTabContent(tabId: String, newContent: String) {
        repository.updateTabContent(tabId, newContent)
    }

    fun saveTabContent(tabId: String): Boolean {
        val success = repository.saveTabContent(tabId)
        if (success) {
            showToast("Document changes saved successfully")
        } else {
            showToast("Failed to save document changes")
        }
        return success
    }

    fun updateDocumentProgress(uriString: String, page: Int, progress: Float) {
        viewModelScope.launch {
            repository.updateDocumentProgress(uriString, page, progress)
        }
    }

    fun togglePin(uriString: String) {
        viewModelScope.launch {
            val isPinned = repository.toggleDocumentPin(Uri.parse(uriString))
            showToast(if (isPinned) "Document Pinned" else "Document Unpinned")
        }
    }

    fun toggleFavorite(uriString: String) {
        viewModelScope.launch {
            val isFav = repository.toggleDocumentFavorite(Uri.parse(uriString))
            showToast(if (isFav) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun deleteDocumentRecord(uriString: String) {
        viewModelScope.launch {
            repository.deleteDocumentRecord(uriString)
            refreshDeviceFiles()
            showToast("Document record removed from history")
        }
    }

    fun deleteMultipleDocuments(uriStrings: List<String>) {
        viewModelScope.launch {
            repository.deleteMultipleDocumentRecords(uriStrings)
            refreshDeviceFiles()
            showToast("Removed ${uriStrings.size} documents from history")
        }
    }

    fun deleteFilePermanently(uriString: String) {
        viewModelScope.launch {
            FileHelper.deletePhysicalFile(getApplication(), uriString)
            repository.deleteDocumentRecord(uriString)
            refreshDeviceFiles()
            showToast("File permanently deleted from storage")
        }
    }

    fun addBookmark(uriString: String, title: String, note: String, pageOrLine: Int, fileType: String) {
        viewModelScope.launch {
            repository.addBookmark(uriString, title, note, pageOrLine, fileType)
            showToast("Bookmark added successfully")
        }
    }

    fun addAnnotation(uriString: String, type: String, textContent: String, colorHex: String, pageOrLine: Int) {
        viewModelScope.launch {
            repository.addAnnotation(uriString, type, textContent, colorHex, pageOrLine)
            showToast("Annotation saved")
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
        showToast("Settings saved")
    }

    fun unlockPrivateVault(pin: String): Boolean {
        if (!settings.value.appLockEnabled || settings.value.pinHash.isEmpty() || settings.value.pinHash == pin) {
            _isPrivateVaultUnlocked.value = true
            return true
        }
        return false
    }

    fun lockPrivateVault() {
        _isPrivateVaultUnlocked.value = false
    }

    fun compareTwoFiles(uri1: Uri, uri2: Uri) {
        viewModelScope.launch {
            val text1 = FileHelper.readTextFromUri(getApplication(), uri1)
            val text2 = FileHelper.readTextFromUri(getApplication(), uri2)
            val details1 = FileHelper.getFileDetails(getApplication(), uri1)
            val details2 = FileHelper.getFileDetails(getApplication(), uri2)

            val result = DiffEngine.compareTexts(details1.name, text1, details2.name, text2)
            _activeDiffResult.value = result
            showToast("Diff comparison completed")
        }
    }

    fun scanDuplicatesInFolder(folder: File) {
        viewModelScope.launch {
            _duplicateGroups.value = DuplicateScanner.scanDirectoryForDuplicates(getApplication(), folder)
            showToast("Duplicate scan completed")
        }
    }

    fun performOcrOnImage(imageUri: Uri) {
        viewModelScope.launch {
            val result = OcrEngine.recognizeTextFromUri(getApplication(), imageUri, settings.value.defaultOcrLanguage)
            _ocrProcessingResult.value = result.extractedText
            repository.saveOcrRecord(imageUri.toString(), result.extractedText, result.language)
            showToast("Local OCR text extraction completed")
        }
    }

    fun clearOcrResult() {
        _ocrProcessingResult.value = null
    }

    fun triggerDatabaseBackup(customFolderUriString: String? = null) {
        viewModelScope.launch {
            val uriToUse = customFolderUriString ?: settings.value.backupFolderUri
            val success = repository.performDatabaseBackup(uriToUse)
            if (success) {
                updateSettings(settings.value.copy(lastBackupTimestamp = System.currentTimeMillis()))
                showToast("Database backup created successfully")
            } else {
                showToast("Failed to create database backup")
            }
        }
    }

    fun checkAndPerformAutoBackup() {
        val currentSettings = settings.value
        if (currentSettings.autoBackupEnabled) {
            val intervalMs = currentSettings.backupIntervalHours * 3600 * 1000L
            if (System.currentTimeMillis() - currentSettings.lastBackupTimestamp >= intervalMs) {
                triggerDatabaseBackup()
            }
        }
    }

    fun toggleDocumentRoomEncryption(uriString: String, rawContent: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isNowEncrypted = repository.toggleDocumentRoomEncryption(uriString, rawContent)
            onResult(isNowEncrypted)
        }
    }

    fun decryptDocumentFromRoom(uriString: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val decrypted = repository.decryptDocumentRecordInRoom(uriString)
            onResult(decrypted)
        }
    }

    fun getDocumentMetadataFlow(uriString: String) = repository.getDocumentMetadataFlow(uriString)

    fun saveDocumentMetadata(metadata: DocumentMetadataRecord) {
        viewModelScope.launch {
            repository.saveDocumentMetadata(metadata)
        }
    }

    fun updateDocumentCategory(uriString: String, category: String) {
        viewModelScope.launch {
            repository.updateDocumentCategory(uriString, category)
            showToast(if (category.isNotBlank()) "Labeled as \"$category\"" else "Category cleared")
        }
    }

    fun createFolder(name: String, description: String = "", colorHex: String = "#6750A4") {
        viewModelScope.launch {
            repository.createFolder(name, description, colorHex)
            showToast("Folder \"$name\" created")
        }
    }

    fun updateFolder(folder: FolderRecord) {
        viewModelScope.launch {
            repository.updateFolder(folder)
            showToast("Folder updated")
        }
    }

    fun deleteFolder(folderId: String, folderName: String) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            showToast("Folder \"$folderName\" removed (documents unassigned)")
        }
    }

    fun togglePinFolder(folderId: String) {
        viewModelScope.launch {
            repository.togglePinFolder(folderId)
        }
    }

    fun moveDocumentToFolder(uriString: String, folderId: String, folderName: String) {
        viewModelScope.launch {
            repository.moveDocumentToFolder(uriString, folderId, folderName)
            showToast(if (folderName.isNotBlank()) "Moved to \"$folderName\"" else "Removed from folder")
        }
    }

    fun moveMultipleDocumentsToFolder(uriStrings: List<String>, folderId: String, folderName: String) {
        viewModelScope.launch {
            repository.moveMultipleDocumentsToFolder(uriStrings, folderId, folderName)
            showToast("Moved ${uriStrings.size} items to \"$folderName\"")
        }
    }

    fun removeDocumentFromFolder(uriString: String) {
        viewModelScope.launch {
            repository.removeDocumentFromFolder(uriString)
            showToast("Removed from folder")
        }
    }
}
