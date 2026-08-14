package com.example.data.util

data class MarkdownTocItem(
    val title: String,
    val level: Int,
    val lineIndex: Int
)

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String, val lineIndex: Int) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Blockquote(val text: String) : MarkdownBlock()
    data class ListItem(val text: String, val isChecklist: Boolean = false, val isChecked: Boolean = false) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    object Divider : MarkdownBlock()
}

object MarkdownParser {

    fun parseToc(markdown: String): List<MarkdownTocItem> {
        val lines = markdown.lines()
        val toc = mutableListOf<MarkdownTocItem>()
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                if (level in 1..6) {
                    val title = trimmed.removePrefix("#".repeat(level)).trim()
                    toc.add(MarkdownTocItem(title, level, index))
                }
            }
        }
        return toc
    }

    fun parseBlocks(markdown: String): List<MarkdownBlock> {
        val lines = markdown.lines()
        val blocks = mutableListOf<MarkdownBlock>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // Code block
            if (trimmed.startsWith("```")) {
                val lang = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size) i++ // skip closing ```
                blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
                continue
            }

            // Header
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                if (level in 1..6) {
                    val text = trimmed.removePrefix("#".repeat(level)).trim()
                    blocks.add(MarkdownBlock.Header(level, text, i))
                    i++
                    continue
                }
            }

            // Horizontal Rule
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(MarkdownBlock.Divider)
                i++
                continue
            }

            // Blockquote
            if (trimmed.startsWith(">")) {
                val text = trimmed.removePrefix(">").trim()
                blocks.add(MarkdownBlock.Blockquote(text))
                i++
                continue
            }

            // Checklist or Unordered List
            if (trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]")) {
                val isChecked = trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]")
                val text = trimmed.substring(5).trim()
                blocks.add(MarkdownBlock.ListItem(text, isChecklist = true, isChecked = isChecked))
                i++
                continue
            }

            if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) {
                val text = trimmed.substring(2).trim()
                blocks.add(MarkdownBlock.ListItem(text, isChecklist = false, isChecked = false))
                i++
                continue
            }

            // Table
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|")) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                if (tableLines.size >= 2) {
                    val headers = tableLines[0].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    val rows = tableLines.drop(2).map { rowLine ->
                        rowLine.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    blocks.add(MarkdownBlock.Table(headers, rows))
                }
                continue
            }

            // Paragraph
            blocks.add(MarkdownBlock.Paragraph(trimmed))
            i++
        }

        return blocks
    }
}
