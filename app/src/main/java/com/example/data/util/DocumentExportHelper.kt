package com.example.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocumentExportHelper {

    /**
     * Shares one or multiple documents using the system share sheet (Intent.ACTION_SEND / ACTION_SEND_MULTIPLE).
     */
    fun shareDocuments(
        context: Context,
        uris: List<Uri>,
        title: String = "Share Documents"
    ): Boolean {
        if (uris.isEmpty()) return false
        return try {
            val shareableUris = ArrayList<Uri>()
            val cacheShareDir = File(context.cacheDir, "shared_docs").apply { mkdirs() }

            for (uri in uris) {
                try {
                    val fileDetails = FileHelper.getFileDetails(context, uri)
                    val safeName = fileDetails.name.ifBlank { "document_${System.currentTimeMillis()}" }
                    val tempFile = File(cacheShareDir, safeName)

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile
                    )
                    shareableUris.add(contentUri)
                } catch (e: Exception) {
                    shareableUris.add(uri)
                }
            }

            val intent = if (shareableUris.size == 1) {
                val singleUri = shareableUris.first()
                val mimeType = context.contentResolver.getType(singleUri) ?: "*/*"
                Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, singleUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareableUris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Bundles selected documents into a zip archive file in cache directory.
     */
    fun createZipArchive(
        context: Context,
        uris: List<Uri>,
        zipFileName: String = "LS_Docs_${System.currentTimeMillis()}.zip"
    ): File? {
        if (uris.isEmpty()) return null
        return try {
            val exportDir = File(context.cacheDir, "zip_exports").apply { mkdirs() }
            val zipFile = File(exportDir, zipFileName)
            if (zipFile.exists()) zipFile.delete()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
                val usedNames = mutableSetOf<String>()

                for (uri in uris) {
                    try {
                        val fileDetails = FileHelper.getFileDetails(context, uri)
                        var entryName = fileDetails.name.ifBlank { "file_${System.currentTimeMillis()}" }

                        var counter = 1
                        val baseName = entryName.substringBeforeLast(".")
                        val extension = if (entryName.contains(".")) ".${entryName.substringAfterLast(".")}" else ""
                        while (usedNames.contains(entryName)) {
                            entryName = "${baseName}_$counter$extension"
                            counter++
                        }
                        usedNames.add(entryName)

                        val zipEntry = ZipEntry(entryName)
                        zos.putNextEntry(zipEntry)

                        context.contentResolver.openInputStream(uri)?.use { input ->
                            input.copyTo(zos)
                        }
                        zos.closeEntry()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Shares a generated ZIP archive via the system share sheet.
     */
    fun shareZipFile(context: Context, zipFile: File, title: String = "Share ZIP Archive"): Boolean {
        return try {
            val zipUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, zipUri)
                putExtra(Intent.EXTRA_SUBJECT, zipFile.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
