package com.example.data.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

enum class CodeTokenType {
    KEYWORD, STRING, NUMBER, COMMENT, ANNOTATION, TYPE, NORMAL
}

object CodeSyntaxHighlighter {

    private val KEYWORDS = setOf(
        "val", "var", "fun", "class", "interface", "object", "import", "package", "return", "if", "else",
        "for", "while", "do", "when", "try", "catch", "finally", "throw", "null", "true", "false", "this",
        "super", "is", "as", "in", "by", "data", "sealed", "enum", "override", "abstract", "private", "protected",
        "public", "internal", "companion", "suspend", "inline", "crossinline", "noinline", "typealias", "const",
        "let", "function", "async", "await", "export", "from", "default", "struct", "impl",
        "fn", "pub", "use", "mod", "def", "self", "None", "True", "False", "lambda", "with", "yield", "SELECT",
        "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "TABLE", "JOIN", "ON", "GROUP", "BY", "ORDER"
    )

    fun highlightCode(
        code: String,
        isDark: Boolean = true
    ): AnnotatedString {
        val keywordColor = if (isDark) Color(0xFFC792EA) else Color(0xFF7C4DFF)
        val stringColor = if (isDark) Color(0xFFC3E88D) else Color(0xFF43A047)
        val numberColor = if (isDark) Color(0xFFF78C6C) else Color(0xFFE65100)
        val commentColor = if (isDark) Color(0xFF546E7A) else Color(0xFF90A4AE)
        val typeColor = if (isDark) Color(0xFF82AAFF) else Color(0xFF1E88E5)

        return buildAnnotatedString {
            append(code)
            var index = 0
            val len = code.length

            while (index < len) {
                // Comments
                if (index + 1 < len && code[index] == '/' && code[index + 1] == '/') {
                    val end = code.indexOf('\n', index).let { if (it == -1) len else it }
                    addStyle(SpanStyle(color = commentColor), index, end)
                    index = end
                    continue
                }

                // Strings
                if (code[index] == '"' || code[index] == '\'') {
                    val quote = code[index]
                    var end = index + 1
                    while (end < len && code[end] != quote) {
                        if (code[end] == '\\') end++
                        end++
                    }
                    if (end < len) end++
                    addStyle(SpanStyle(color = stringColor), index, end)
                    index = end
                    continue
                }

                // Numbers
                if (code[index].isDigit()) {
                    var end = index
                    while (end < len && (code[end].isDigit() || code[end] == '.')) end++
                    addStyle(SpanStyle(color = numberColor), index, end)
                    index = end
                    continue
                }

                // Identifiers / Keywords
                if (code[index].isLetter() || code[index] == '_') {
                    var end = index
                    while (end < len && (code[end].isLetterOrDigit() || code[end] == '_')) end++
                    val word = code.substring(index, end)
                    if (KEYWORDS.contains(word) || KEYWORDS.contains(word.uppercase())) {
                        addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), index, end)
                    } else if (word.first().isUpperCase()) {
                        addStyle(SpanStyle(color = typeColor), index, end)
                    }
                    index = end
                    continue
                }

                index++
            }
        }
    }
}
