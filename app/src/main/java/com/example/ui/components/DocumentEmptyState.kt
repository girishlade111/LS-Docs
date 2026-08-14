package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyDocumentCanvasIllustration(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    tertiaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(width = 240.dp, height = 150.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 240.dp, height = 150.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // 1. Background Dashed Radar/Target Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.12f),
                radius = 65.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f * pulseAlpha),
                radius = 50.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )

            // 2. Back Document (Angled Left)
            val docWidth = 60.dp.toPx()
            val docHeight = 78.dp.toPx()

            val backDocPath = Path().apply {
                val left = centerX - 42.dp.toPx()
                val top = centerY - 38.dp.toPx()
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(left, top, left + docWidth, top + docHeight),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                )
            }
            drawPath(
                path = backDocPath,
                color = secondaryColor.copy(alpha = 0.35f)
            )

            // 3. Front Document Sheet (Center)
            val frontLeft = centerX - 24.dp.toPx()
            val frontTop = centerY - 32.dp.toPx()

            val frontDocPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(frontLeft, frontTop, frontLeft + docWidth, frontTop + docHeight),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                )
            }
            drawPath(
                path = frontDocPath,
                color = primaryColor.copy(alpha = 0.85f)
            )

            // Fold Corner on Front Document
            val foldSize = 14.dp.toPx()
            val foldPath = Path().apply {
                moveTo(frontLeft + docWidth - foldSize, frontTop)
                lineTo(frontLeft + docWidth, frontTop + foldSize)
                lineTo(frontLeft + docWidth - foldSize, frontTop + foldSize)
                close()
            }
            drawPath(
                path = foldPath,
                color = primaryColor.copy(alpha = 0.4f)
            )

            // Document Content Placeholder Lines inside front sheet
            val lineX = frontLeft + 10.dp.toPx()
            val lineWidth = docWidth - 20.dp.toPx()
            drawRoundRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(lineX, frontTop + 16.dp.toPx()),
                size = Size(lineWidth * 0.7f, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(lineX, frontTop + 24.dp.toPx()),
                size = Size(lineWidth, 3.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(lineX, frontTop + 31.dp.toPx()),
                size = Size(lineWidth * 0.85f, 3.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(lineX, frontTop + 38.dp.toPx()),
                size = Size(lineWidth * 0.5f, 3.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 4. Magnifying Glass / Scanner Focal Lens (Bottom Right of Sheet)
            val lensCenterX = centerX + 22.dp.toPx()
            val lensCenterY = centerY + 18.dp.toPx()
            val lensRadius = 18.dp.toPx()

            drawCircle(
                color = tertiaryColor.copy(alpha = 0.95f),
                radius = lensRadius,
                center = Offset(lensCenterX, lensCenterY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = lensRadius - 3.dp.toPx(),
                center = Offset(lensCenterX, lensCenterY),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Glass handle
            drawLine(
                color = tertiaryColor,
                start = Offset(lensCenterX + 12.dp.toPx(), lensCenterY + 12.dp.toPx()),
                end = Offset(lensCenterX + 22.dp.toPx(), lensCenterY + 22.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )
        }

        // Floating Format Badges
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(primaryColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "PDF",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(secondaryColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "DOC",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun DocumentEmptyState(
    modifier: Modifier = Modifier,
    title: String = "No Documents Found",
    description: String = "Your local workspace is currently empty. Open documents from storage or scan physical pages using local OCR.",
    onOpenStorage: () -> Unit = {},
    onScanOcr: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    HighDensityCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Graphic Illustration Canvas
            EmptyDocumentCanvasIllustration()

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Description
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Guidance Steps Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "HOW TO ADD DOCUMENTS:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.6.sp
                )

                GuidanceStepItem(
                    stepNumber = "1",
                    title = "Pick from Storage",
                    detail = "Select PDF, Markdown, TXT, CSV, or Code files from phone storage."
                )

                GuidanceStepItem(
                    stepNumber = "2",
                    title = "Scan Image (OCR)",
                    detail = "Capture or pick image photos to convert physical text locally."
                )

                if (onRefresh != null) {
                    GuidanceStepItem(
                        stepNumber = "3",
                        title = "Pull to Refresh",
                        detail = "Swipe down on the file browser to re-index device storage."
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenStorage,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Storage",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Browse Device Storage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (onScanOcr != null) {
                    OutlinedButton(
                        onClick = onScanOcr,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan OCR",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Image with OCR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onRefresh != null) {
                    OutlinedButton(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-scan Storage",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refresh File Index",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidanceStepItem(
    stepNumber: String,
    title: String,
    detail: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}
