package com.example.data.util

import android.content.Context
import com.example.data.model.DuplicateGroup
import com.example.data.model.FileDetails
import java.io.File

object DuplicateScanner {

    fun scanDirectoryForDuplicates(context: Context, folder: File): List<DuplicateGroup> {
        if (!folder.exists() || !folder.isDirectory) return emptyList()

        val allFiles = folder.walkTopDown().filter { it.isFile }.toList()
        val groupedBySize = allFiles.groupBy { it.length() }.filter { it.value.size > 1 }

        val duplicateGroups = mutableListOf<DuplicateGroup>()

        for ((size, files) in groupedBySize) {
            val fileDetailsList = files.map { file ->
                FileHelper.getFileDetails(context, android.net.Uri.fromFile(file))
            }
            // Hash calculation sample
            val hash = "HASH_${size}_${files.first().name.hashCode()}"
            duplicateGroups.add(DuplicateGroup(size, hash, fileDetailsList))
        }

        return duplicateGroups
    }
}
