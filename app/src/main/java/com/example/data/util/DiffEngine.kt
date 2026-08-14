package com.example.data.util

import com.example.data.model.DiffResult
import com.example.data.model.DiffType
import com.example.data.model.LineDiff

object DiffEngine {

    fun compareTexts(
        file1Name: String,
        text1: String,
        file2Name: String,
        text2: String,
        ignoreWhitespace: Boolean = false,
        ignoreCase: Boolean = false
    ): DiffResult {
        val lines1 = text1.lines().map { normalize(it, ignoreWhitespace, ignoreCase) }
        val lines2 = text2.lines().map { normalize(it, ignoreWhitespace, ignoreCase) }
        val orig1 = text1.lines()
        val orig2 = text2.lines()

        val lineDiffs = mutableListOf<LineDiff>()
        var addedCount = 0
        var deletedCount = 0
        var modifiedCount = 0

        val maxLines = maxOf(orig1.size, orig2.size)
        var i1 = 0
        var i2 = 0

        while (i1 < orig1.size || i2 < orig2.size) {
            val l1 = orig1.getOrNull(i1)
            val l2 = orig2.getOrNull(i2)

            val n1 = lines1.getOrNull(i1)
            val n2 = lines2.getOrNull(i2)

            when {
                l1 != null && l2 != null && n1 == n2 -> {
                    lineDiffs.add(LineDiff(i1 + 1, i2 + 1, l1, l2, DiffType.UNCHANGED))
                    i1++
                    i2++
                }
                l1 != null && l2 != null && n1 != n2 -> {
                    // Check if l1 exists later in lines2
                    val posIn2 = lines2.drop(i2).indexOf(n1)
                    if (posIn2 in 1..3) { // Small lookahead for insertion
                        lineDiffs.add(LineDiff(null, i2 + 1, null, l2, DiffType.ADDED))
                        addedCount++
                        i2++
                    } else {
                        lineDiffs.add(LineDiff(i1 + 1, i2 + 1, l1, l2, DiffType.MODIFIED))
                        modifiedCount++
                        i1++
                        i2++
                    }
                }
                l1 == null && l2 != null -> {
                    lineDiffs.add(LineDiff(null, i2 + 1, null, l2, DiffType.ADDED))
                    addedCount++
                    i2++
                }
                l1 != null && l2 == null -> {
                    lineDiffs.add(LineDiff(i1 + 1, null, l1, null, DiffType.DELETED))
                    deletedCount++
                    i1++
                }
            }
        }

        val isIdentical = addedCount == 0 && deletedCount == 0 && modifiedCount == 0

        return DiffResult(
            file1Name = file1Name,
            file2Name = file2Name,
            lineDiffs = lineDiffs,
            addedCount = addedCount,
            deletedCount = deletedCount,
            modifiedCount = modifiedCount,
            isIdentical = isIdentical
        )
    }

    private fun normalize(text: String, ignoreWhitespace: Boolean, ignoreCase: Boolean): String {
        var res = text
        if (ignoreWhitespace) res = res.replace("\\s+".toRegex(), " ").trim()
        if (ignoreCase) res = res.lowercase()
        return res
    }
}
