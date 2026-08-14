package com.example.data.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.DocumentFileType
import com.example.data.model.FileDetails
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import android.os.Environment

object FileHelper {

    fun deletePhysicalFile(context: Context, uriString: String): Boolean {
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" || uri.scheme == null) {
                val file = File(uri.path ?: uriString)
                if (file.exists()) {
                    return file.delete()
                }
            } else if (uri.scheme == "content") {
                try {
                    val deleted = context.contentResolver.delete(uri, null, null)
                    if (deleted > 0) return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getFileDetails(context: Context, uri: Uri): FileDetails {
        val contentResolver = context.contentResolver
        var name = "Document"
        var size = 0L
        var mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        if (uri.scheme == "content") {
            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                name = file.name
                size = file.length()
            }
        }

        val extension = if (name.contains(".")) name.substringAfterLast(".").lowercase() else ""
        val fileType = DocumentFileType.fromExtension(extension).takeIf { it != DocumentFileType.TEXT }
            ?: DocumentFileType.fromMimeType(mimeType)

        return FileDetails(
            name = name,
            uri = uri,
            path = uri.toString(),
            sizeBytes = size,
            lastModified = System.currentTimeMillis(),
            mimeType = mimeType,
            extension = extension,
            isReadOnly = false,
            fileType = fileType
        )
    }

    fun readTextFromUri(context: Context, uri: Uri, charset: Charset = StandardCharsets.UTF_8, maxBytes: Long = 10_000_000L): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, charset))
                val builder = StringBuilder()
                var line: String?
                var readBytes = 0L
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line).append("\n")
                    readBytes += line!!.length
                    if (readBytes > maxBytes) {
                        builder.append("\n\n[LS Docs Note: Content truncated for performance. Enable Large File Mode in Settings for full access.]")
                        break
                    }
                }
                builder.toString()
            } ?: ""
        } catch (e: Exception) {
            "Error reading document: ${e.localizedMessage}"
        }
    }

    fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                outputStream.write(text.toByteArray(StandardCharsets.UTF_8))
                outputStream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readBytesFromUri(context: Context, uri: Uri, offset: Long = 0L, length: Int = 4096): ByteArray {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.skip(offset)
                val buffer = ByteArray(length)
                val read = inputStream.read(buffer, 0, length)
                if (read > 0) buffer.copyOf(read) else ByteArray(0)
            } ?: ByteArray(0)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    /**
     * Scans device storage for real user documents and files.
     * Excludes sample/demo directories.
     */
    fun getDeviceFiles(context: Context): List<FileDetails> {
        // Delete legacy sample docs directory to remove demo/dummy data
        try {
            val sampleDir = File(context.filesDir, "sample_docs")
            if (sampleDir.exists()) {
                sampleDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val deviceFiles = mutableListOf<FileDetails>()
        val seenPaths = mutableSetOf<String>()

        val documentExtensions = setOf(
            "pdf", "md", "markdown", "txt", "json", "yaml", "yml", "xml", "csv",
            "epub", "kt", "java", "py", "js", "ts", "html", "css", "c", "cpp",
            "h", "sql", "sh", "png", "jpg", "jpeg", "webp", "zip"
        )

        val directoriesToScan = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            context.getExternalFilesDir(null),
            context.filesDir
        )

        for (dir in directoriesToScan) {
            if (dir.exists() && dir.isDirectory) {
                scanDirectoryRecursively(context, dir, documentExtensions, deviceFiles, seenPaths, maxDepth = 3)
            }
        }

        return deviceFiles
    }

    private fun scanDirectoryRecursively(
        context: Context,
        dir: File,
        allowedExtensions: Set<String>,
        result: MutableList<FileDetails>,
        seenPaths: MutableSet<String>,
        maxDepth: Int
    ) {
        if (maxDepth <= 0) return
        val files = try { dir.listFiles() } catch (e: Exception) { null } ?: return
        for (file in files) {
            if (file.isDirectory) {
                if (!file.name.startsWith(".") && file.name != "sample_docs" && file.name != "cache") {
                    scanDirectoryRecursively(context, file, allowedExtensions, result, seenPaths, maxDepth - 1)
                }
            } else if (file.isFile && !file.name.startsWith(".")) {
                val ext = if (file.name.contains(".")) file.name.substringAfterLast(".").lowercase() else ""
                if (allowedExtensions.contains(ext)) {
                    val canonicalPath = try { file.canonicalPath } catch (e: Exception) { file.absolutePath }
                    if (!seenPaths.contains(canonicalPath)) {
                        seenPaths.add(canonicalPath)
                        result.add(getFileDetails(context, Uri.fromFile(file)))
                    }
                }
            }
        }
    }
}
