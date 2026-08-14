package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiffType
import com.example.data.util.CsvParser
import com.example.data.util.HexViewerUtil
import com.example.ui.components.HighDensityBadge
import com.example.ui.components.HighDensityCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    initialTab: String = "ocr"
) {
    var selectedToolTab by remember {
        mutableStateOf(
            when (initialTab) {
                "diff" -> 1
                "hex" -> 2
                "convert" -> 3
                else -> 0
            }
        )
    }

    val tabs = listOf("On-Device OCR", "Side-by-Side Diff", "Hex Inspection", "Format Converter", "Watermark & Sign")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedToolTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedToolTab == index,
                    onClick = { selectedToolTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedToolTab) {
            0 -> OcrToolSection(viewModel)
            1 -> DiffToolSection(viewModel)
            2 -> HexToolSection(viewModel)
            3 -> ConverterToolSection(viewModel)
            4 -> WatermarkToolSection(viewModel)
        }
    }
}

@Composable
fun OcrToolSection(viewModel: MainViewModel) {
    val ocrText by viewModel.ocrProcessingResult.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.performOcrOnImage(it) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ON-DEVICE OCR SCANNER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select an image or scanned document to extract text 100% locally.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Image")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Image for Local OCR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        ocrText?.let { text ->
            HighDensityCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("EXTRACTED TEXT RESULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(text)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DiffToolSection(viewModel: MainViewModel) {
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    val diffResult by viewModel.activeDiffResult.collectAsState()

    var file1Uri by remember { mutableStateOf<Uri?>(null) }
    var file2Uri by remember { mutableStateOf<Uri?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SIDE-BY-SIDE DIFF INSPECTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Compare two text-based local files line by line.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (sampleFiles.size >= 2) {
                            file1Uri = sampleFiles[0].uri
                            file2Uri = sampleFiles[3].uri
                            viewModel.compareTwoFiles(sampleFiles[0].uri, sampleFiles[3].uri)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Compare, contentDescription = "Compare")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Sample Diff Comparison",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        diffResult?.let { result ->
            HighDensityCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DIFF RESULTS: ${result.file1Name} vs ${result.file2Name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HighDensityBadge {
                            Text("+${result.addedCount} Added", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp))
                        }
                        HighDensityBadge {
                            Text("-${result.deletedCount} Deleted", fontSize = 10.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(4.dp))
                        }
                        HighDensityBadge {
                            Text("~${result.modifiedCount} Modified", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(4.dp))
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.height(250.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(result.lineDiffs) { diff ->
                            val bgColor = when (diff.type) {
                                DiffType.ADDED -> Color(0xFF1B6E33).copy(alpha = 0.2f)
                                DiffType.DELETED -> Color(0xFFB3261E).copy(alpha = 0.2f)
                                DiffType.MODIFIED -> Color(0xFF6750A4).copy(alpha = 0.2f)
                                DiffType.UNCHANGED -> Color.Transparent
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${diff.lineNumber1 ?: "-"} | ${diff.lineNumber2 ?: "-"}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
                                Text(diff.text1 ?: diff.text2 ?: "", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HexToolSection(viewModel: MainViewModel) {
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    var hexRows by remember { mutableStateOf(emptyList<com.example.data.util.HexRow>()) }
    var magicSig by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BINARY & HEXADECIMAL INSPECTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Inspect raw magic bytes, offsets, and ASCII streams.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val bytes = "LS Docs Binary Signature Header Test Data".toByteArray() + byteArrayOf(0.toByte(), 1.toByte(), 2.toByte(), 0xFF.toByte())
                        hexRows = HexViewerUtil.formatHexRows(bytes)
                        magicSig = HexViewerUtil.guessMagicSignature(bytes)
                        viewModel.showToast("Hex inspection completed")
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "Inspect")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Inspect Sample Binary Stream",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (hexRows.isNotEmpty()) {
            HighDensityCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MAGIC SIGNATURE: $magicSig", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(hexRows) { row ->
                            Text(
                                text = "${row.offsetHex}  ${row.hexBytes}  ${row.asciiText}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConverterToolSection(viewModel: MainViewModel) {
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    var convertedOutput by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LOCAL FORMAT CONVERTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Convert CSV spreadsheets directly to JSON or TSV on-device.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val csvSample = sampleFiles.find { it.extension == "csv" }
                        val table = CsvParser.parseCsv("ID,Name,Type\n1,PDF,Document\n2,MD,Markdown")
                        convertedOutput = CsvParser.exportToJson(table)
                        viewModel.showToast("CSV converted to JSON")
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Transform, contentDescription = "Convert")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Convert CSV Sample -> JSON",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        convertedOutput?.let { json ->
            HighDensityCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CONVERTED JSON OUTPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = json,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WatermarkToolSection(viewModel: MainViewModel) {
    val sampleFiles by viewModel.sampleFiles.collectAsState()
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL - J. DOE") }
    var watermarkStyle by remember { mutableStateOf("Diagonal Center") }
    var inkColor by remember { mutableStateOf(Color(0xFF1A237E)) }
    var opacity by remember { mutableStateOf(0.4f) }
    var includeTimestamp by remember { mutableStateOf(true) }
    var exportSuccess by remember { mutableStateOf(false) }

    val styleOptions = listOf("Diagonal Center", "Signature Stamp", "Top Header Stamp")
    val colorPalette = listOf(
        Color(0xFF1A237E) to "Navy",
        Color(0xFFB71C1C) to "Crimson",
        Color(0xFF1B5E20) to "Forest",
        Color(0xFF212121) to "Slate"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("DOCUMENT SIGNING & WATERMARK OVERLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Overlay custom text strings, digital signature badges, or watermarks onto documents before exporting.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark / Signer Text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Placement Style", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    styleOptions.forEach { style ->
                        FilterChip(
                            selected = watermarkStyle == style,
                            onClick = { watermarkStyle = style },
                            label = { Text(style, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Stamp Color", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colorPalette.forEach { (color, name) ->
                        val isSelected = inkColor == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { inkColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Opacity / Transparency (${(opacity * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.1f..1.0f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Date & Timestamp", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = includeTimestamp,
                        onCheckedChange = { includeTimestamp = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        exportSuccess = true
                        viewModel.showToast("Watermarked PDF exported")
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = watermarkText.isNotBlank()
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Sample Watermarked PDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Live Preview Box
        HighDensityCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LIVE CANVAS PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SAMPLE_DOCUMENT.PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        repeat(5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (it % 2 == 0) 0.9f else 0.7f)
                                    .height(8.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    when (watermarkStyle) {
                        "Diagonal Center" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .rotate(-30f)
                                        .alpha(opacity)
                                ) {
                                    Text(
                                        text = watermarkText.ifEmpty { "WATERMARK" },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = inkColor
                                    )
                                    if (includeTimestamp) {
                                        Text(
                                            text = "Aug 13, 2026 • Digitally Stamped",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor
                                        )
                                    }
                                }
                            }
                        }
                        "Signature Stamp" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier
                                        .alpha(opacity)
                                        .background(inkColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .border(1.dp, inkColor, RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "SIGNED: ${watermarkText.ifEmpty { "Authorized Signer" }}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = inkColor
                                    )
                                    if (includeTimestamp) {
                                        Text(
                                            text = "2026-08-13 03:33 UTC • Verified",
                                            fontSize = 8.sp,
                                            color = inkColor
                                        )
                                    }
                                }
                            }
                        }
                        "Top Header Stamp" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(
                                    text = "[ ${watermarkText.ifEmpty { "WATERMARK" }} ${if (includeTimestamp) "• Aug 13, 2026" else ""} ]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = inkColor,
                                    modifier = Modifier.alpha(opacity)
                                )
                            }
                        }
                    }
                }

                if (exportSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HighDensityBadge {
                        Text(
                            "Exported watermarked document as PDF to Downloads directory!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
