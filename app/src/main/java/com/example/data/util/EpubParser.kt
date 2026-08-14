package com.example.data.util

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.util.zip.ZipInputStream

data class EpubBook(
    val title: String,
    val author: String,
    val publisher: String,
    val chapters: List<EpubChapter>,
    val coverImageBytes: ByteArray? = null
)

data class EpubChapter(
    val title: String,
    val contentHtml: String,
    val playOrder: Int
)

object EpubParser {

    fun parseEpub(context: Context, uri: Uri): EpubBook {
        var title = "EPUB Document"
        var author = "Unknown Author"
        var publisher = "Local Document"
        val chapters = mutableListOf<EpubChapter>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(inputStream)
                var entry = zipInputStream.nextEntry
                var chapterCount = 1

                while (entry != null) {
                    val entryName = entry.name.lowercase()
                    if ((entryName.endsWith(".html") || entryName.endsWith(".xhtml") || entryName.endsWith(".htm")) && !entryName.contains("cover")) {
                        val htmlContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        val chapterTitle = extractHtmlTitle(htmlContent) ?: "Chapter $chapterCount"
                        val cleanText = sanitizeHtml(htmlContent)
                        chapters.add(EpubChapter(chapterTitle, cleanText, chapterCount))
                        chapterCount++
                    } else if (entryName.endsWith("content.opf")) {
                        val opfText = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        extractTitleFromOpf(opfText)?.let { title = it }
                        extractAuthorFromOpf(opfText)?.let { author = it }
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (chapters.isEmpty()) {
            chapters.add(EpubChapter("Chapter 1", "This EPUB file contains no readable HTML chapter content.", 1))
        }

        return EpubBook(title, author, publisher, chapters)
    }

    private fun extractHtmlTitle(html: String): String? {
        val match = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE).find(html)
        return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun extractTitleFromOpf(opf: String): String? {
        val match = "<dc:title[^>]*>(.*?)</dc:title>".toRegex(RegexOption.IGNORE_CASE).find(opf)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun extractAuthorFromOpf(opf: String): String? {
        val match = "<dc:creator[^>]*>(.*?)</dc:creator>".toRegex(RegexOption.IGNORE_CASE).find(opf)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun sanitizeHtml(html: String): String {
        return html
            .replace("<style[\\s\\S]*?</style>".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("<script[\\s\\S]*?</script>".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("<br\\s*/?>".toRegex(RegexOption.IGNORE_CASE), "\n")
            .replace("</p>".toRegex(RegexOption.IGNORE_CASE), "\n\n")
            .replace("</div>".toRegex(RegexOption.IGNORE_CASE), "\n")
            .replace("<[^>]*>".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }
}
