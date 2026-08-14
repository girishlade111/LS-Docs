package com.example.data.util

data class HexRow(
    val offsetHex: String,
    val hexBytes: String,
    val asciiText: String
)

object HexViewerUtil {

    fun formatHexRows(bytes: ByteArray, bytesPerRow: Int = 16, startOffset: Long = 0L): List<HexRow> {
        val rows = mutableListOf<HexRow>()
        var offset = startOffset
        var index = 0

        while (index < bytes.size) {
            val end = minOf(index + bytesPerRow, bytes.size)
            val chunk = bytes.copyOfRange(index, end)

            val hexBuilder = StringBuilder()
            val asciiBuilder = StringBuilder()

            for (b in chunk) {
                hexBuilder.append(String.format("%02X ", b))
                val c = b.toInt().toChar()
                if (c in ' '..'~') asciiBuilder.append(c) else asciiBuilder.append('.')
            }

            // Pad remaining space
            if (chunk.size < bytesPerRow) {
                val padding = (bytesPerRow - chunk.size) * 3
                hexBuilder.append(" ".repeat(padding))
            }

            val offsetStr = String.format("%08X", offset)
            rows.add(HexRow(offsetStr, hexBuilder.toString().trim(), asciiBuilder.toString()))

            offset += bytesPerRow
            index += bytesPerRow
        }

        return rows
    }

    fun guessMagicSignature(bytes: ByteArray): String {
        if (bytes.size < 4) return "Unknown Binary"
        val hex = bytes.take(4).joinToString("") { String.format("%02X", it) }

        return when {
            hex.startsWith("25504446") -> "PDF Document (%PDF)"
            hex.startsWith("89504E47") -> "PNG Image (.png)"
            hex.startsWith("FFD8FF") -> "JPEG Image (.jpg)"
            hex.startsWith("47494638") -> "GIF Image (.gif)"
            hex.startsWith("504B0304") -> "ZIP Archive / EPUB / DOCX (.zip)"
            hex.startsWith("7F454C46") -> "ELF Executable Binary"
            hex.startsWith("494433") -> "MP3 Audio (ID3)"
            else -> "Binary Data / Hex Stream"
        }
    }
}
