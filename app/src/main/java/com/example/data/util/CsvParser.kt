package com.example.data.util

data class CsvTable(
    val delimiter: Char,
    val headers: List<String>,
    val rows: List<List<String>>,
    val rawRowCount: Int,
    val colCount: Int
)

object CsvParser {

    fun detectDelimiter(content: String): Char {
        val sampleLines = content.lines().take(10).filter { it.isNotBlank() }
        if (sampleLines.isEmpty()) return ','

        val candidates = listOf(',', '\t', ';', '|')
        var bestChar = ','
        var maxAvgCount = -1.0

        for (ch in candidates) {
            val counts = sampleLines.map { line -> line.count { it == ch } }
            val avg = counts.average()
            if (avg > maxAvgCount && counts.toSet().size <= 2) { // Delimiter count should be relatively consistent across lines
                maxAvgCount = avg
                bestChar = ch
            }
        }
        return bestChar
    }

    fun parseCsv(
        content: String,
        customDelimiter: Char? = null,
        hasHeader: Boolean = true
    ): CsvTable {
        val delimiter = customDelimiter ?: detectDelimiter(content)
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return CsvTable(delimiter, emptyList(), emptyList(), 0, 0)
        }

        val parsedRows = lines.map { line -> parseLine(line, delimiter) }
        val colCount = parsedRows.maxOfOrNull { it.size } ?: 0

        val paddedRows = parsedRows.map { row ->
            if (row.size < colCount) row + List(colCount - row.size) { "" } else row
        }

        return if (hasHeader && paddedRows.isNotEmpty()) {
            val headers = paddedRows.first()
            val rows = paddedRows.drop(1)
            CsvTable(delimiter, headers, rows, lines.size, colCount)
        } else {
            val headers = List(colCount) { "Column ${it + 1}" }
            CsvTable(delimiter, headers, paddedRows, lines.size, colCount)
        }
    }

    private fun parseLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    fun exportToJson(table: CsvTable): String {
        val builder = StringBuilder("[\n")
        for ((rowIndex, row) in table.rows.withIndex()) {
            builder.append("  {\n")
            for ((colIndex, header) in table.headers.withIndex()) {
                val value = row.getOrNull(colIndex) ?: ""
                val escapedValue = value.replace("\"", "\\\"")
                val isLastCol = colIndex == table.headers.size - 1
                builder.append("    \"$header\": \"$escapedValue\"${if (isLastCol) "" else ","}\n")
            }
            val isLastRow = rowIndex == table.rows.size - 1
            builder.append("  }${if (isLastRow) "" else ","}\n")
        }
        builder.append("]")
        return builder.toString()
    }

    fun exportToTsv(table: CsvTable): String {
        val sb = StringBuilder()
        sb.append(table.headers.joinToString("\t")).append("\n")
        for (row in table.rows) {
            sb.append(row.joinToString("\t")).append("\n")
        }
        return sb.toString()
    }
}
