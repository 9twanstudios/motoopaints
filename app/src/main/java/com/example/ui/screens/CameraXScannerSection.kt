package com.example.ui.screens

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.OemPaintEntity
import com.example.ui.components.ApproximationDisclaimerBanner
import com.example.ui.components.CameraPermissionPromptCard
import com.example.ui.components.CodePill
import com.example.ui.components.SwatchCircle
import com.example.ui.components.colorRgbDistance
import com.example.ui.components.parseHexColor
import com.example.ui.components.rememberCameraScanPermissionState
import com.example.ui.components.toHexString
import com.example.ui.viewmodel.MotoopaintViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraXScannerSection(
    viewModel: MotoopaintViewModel,
    onCapturedToPainter: () -> Unit,
    onOpenSampleInColours: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val oemPaints by viewModel.oemPaints.collectAsStateWithLifecycle()

    val cameraPermissionState = rememberCameraScanPermissionState { granted ->
        if (!granted) {
            viewModel.showMessage("Camera access is needed for live bay scanning")
        }
    }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var sampledHex by remember { mutableStateOf<String?>(null) }
    var closestOem by remember { mutableStateOf<OemPaintEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (!cameraPermissionState.status.isGranted) {
            item {
                CameraPermissionPromptCard(
                    permissionState = cameraPermissionState
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }.also { previewViewRef = it }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Center target crosshair reticle for colour sampling
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .background(Color.Black.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.68f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Aim reticle at clean, sunlit panel",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                DisposableEffect(lifecycleOwner, previewViewRef) {
                    val pv = previewViewRef ?: return@DisposableEffect onDispose {}
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    val executor = ContextCompat.getMainExecutor(context)

                    cameraProviderFuture.addListener({
                        runCatching {
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = pv.surfaceProvider
                            }
                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCaptureRef = capture
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                capture
                            )
                        }.onFailure {
                            viewModel.showMessage("Camera preview unavailable on this device")
                        }
                    }, executor)

                    onDispose {
                        runCatching {
                            if (cameraProviderFuture.isDone) {
                                cameraProviderFuture.get().unbindAll()
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            val bmp = previewViewRef?.bitmap
                            if (bmp != null) {
                                val avgArgb = sampleCenterRgb(bmp)
                                val hex = toHexString(avgArgb)
                                sampledHex = hex
                                closestOem = oemPaints
                                    .filter { it.verificationState != "GAP_MISSING" }
                                    .minByOrNull { colorRgbDistance(avgArgb, parseHexColor(it.hexColor).toArgb()) }
                                viewModel.showMessage("Sampled $hex — matched nearest reference")
                            } else {
                                viewModel.showMessage("Aim camera at vehicle panel first")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camerax_sample_colour")
                    ) {
                        Icon(Icons.Filled.Colorize, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sample Colour")
                    }

                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            val capture = imageCaptureRef
                            if (capture == null) {
                                viewModel.showMessage("Camera not ready yet")
                                return@Button
                            }
                            val (tempFile, _) = viewModel.createCameraTarget()
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                            capture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        viewModel.saveCapturedCameraPhoto(tempFile, "CameraX Bay Scan")
                                        onCapturedToPainter()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        tempFile.delete()
                                        viewModel.showMessage("Capture failed: ${exception.message}")
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camerax_capture_bay_photo")
                    ) {
                        Icon(Icons.Filled.PhotoCamera, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Capture Photo")
                    }
                }
            }

            sampledHex?.let { hex ->
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SwatchCircle(hexColor = hex, finish = "Solid", size = 48.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Camera Sampled Tone", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Sample HEX: $hex (Lighting dependent)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            closestOem?.let { match ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CodePill(code = match.oemCode)
                                        Column {
                                            Text(match.factoryName, style = MaterialTheme.typography.labelLarge)
                                            Text("${match.make} • ${match.finish}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Button(onClick = { onOpenSampleInColours(match.oemCode) }) {
                                        Text("Inspect")
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.sendColourToStudio(closestOem?.hexColor ?: hex, closestOem?.finish ?: "Metallic")
                                    onCapturedToPainter()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Palette, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send to Photo Painter")
                            }
                        }
                    }
                }
            }
        }

        item { ApproximationDisclaimerBanner() }
    }
}

private fun sampleCenterRgb(bitmap: Bitmap): Int {
    val cx = bitmap.width / 2
    val cy = bitmap.height / 2
    val radius = 12
    var rSum = 0L
    var gSum = 0L
    var bSum = 0L
    var count = 0
    for (x in (cx - radius).coerceAtLeast(0)..(cx + radius).coerceAtMost(bitmap.width - 1)) {
        for (y in (cy - radius).coerceAtLeast(0)..(cy + radius).coerceAtMost(bitmap.height - 1)) {
            val px = bitmap.getPixel(x, y)
            rSum += android.graphics.Color.red(px)
            gSum += android.graphics.Color.green(px)
            bSum += android.graphics.Color.blue(px)
            count++
        }
    }
    if (count == 0) return android.graphics.Color.GRAY
    return android.graphics.Color.rgb(
        (rSum / count).toInt(),
        (gSum / count).toInt(),
        (bSum / count).toInt()
    )
}
