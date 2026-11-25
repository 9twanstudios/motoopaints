package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CarPhotoEntity
import com.example.ui.components.ApproximationDisclaimerBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.WorkshopPermissionHelper
import com.example.ui.components.formatShortDate
import com.example.ui.components.parseHexColor
import com.example.ui.components.rememberCameraScanPermissionState
import com.example.ui.components.renderRecolouredPhoto
import com.example.ui.viewmodel.MotoopaintViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

private data class MaskStroke(
    val points: List<Offset>,
    val brushFraction: Float,
    val isEraser: Boolean
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberVehiclePhotoCapture(
    viewModel: MotoopaintViewModel
): Pair<() -> Unit, () -> Unit> {
    val context = LocalContext.current
    var pendingTempFile by remember { mutableStateOf<File?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingTempFile
        if (success && file != null) {
            viewModel.saveCapturedCameraPhoto(file)
        } else {
            file?.delete()
            viewModel.showMessage("Camera photo cancelled")
        }
        pendingTempFile = null
    }

    val cameraPermissionState = rememberCameraScanPermissionState { granted ->
        if (granted) {
            val (file, uri) = viewModel.createCameraTarget()
            pendingTempFile = file
            takePictureLauncher.launch(uri)
        } else {
            showPermissionRationale = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importGalleryCarPhoto(uri)
        }
    }

    val launchCamera = {
        if (cameraPermissionState.status.isGranted) {
            val (file, uri) = viewModel.createCameraTarget()
            pendingTempFile = file
            takePictureLauncher.launch(uri)
        } else if (cameraPermissionState.status.shouldShowRationale) {
            showPermissionRationale = true
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    val launchGallery = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Camera Permission Needed") },
            text = { Text(stringResource(R.string.camera_permission_rationale)) },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    if (cameraPermissionState.status.shouldShowRationale) {
                        cameraPermissionState.launchPermissionRequest()
                    } else {
                        WorkshopPermissionHelper.openAppSettings(context)
                    }
                }) {
                    Text(if (cameraPermissionState.status.shouldShowRationale) "Allow Camera" else "Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    launchGallery()
                }) {
                    Text("Use Photo Picker")
                }
            }
        )
    }

    return launchCamera to launchGallery
}

@Composable
fun PhotoPainterSection(
    viewModel: MotoopaintViewModel,
    onGoToLibrary: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activePhotoPath by viewModel.activePhotoForStudio.collectAsStateWithLifecycle()
    val activeColourPair by viewModel.activeColourForStudio.collectAsStateWithLifecycle()
    val oemPaints by viewModel.oemPaints.collectAsStateWithLifecycle()

    val (launchCamera, launchGallery) = rememberVehiclePhotoCapture(viewModel)

    var selectedHex by remember { mutableStateOf("#1F7") }
    var selectedFinish by remember { mutableStateOf("Metallic") }
    LaunchedEffect(activeColourPair) {
        activeColourPair?.let { (hex, fin) ->
            selectedHex = hex
            selectedFinish = fin
        }
    }
    LaunchedEffect(oemPaints) {
        if (selectedHex == "#1F7" && oemPaints.isNotEmpty()) {
            selectedHex = oemPaints.firstOrNull { it.oemCode == "1F7" }?.hexColor ?: oemPaints.first().hexColor
        }
    }

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recolouredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var maskBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val strokes = remember { mutableStateListOf<MaskStroke>() }
    val redoStrokes = remember { mutableStateListOf<MaskStroke>() }
    var activePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var strokeRevision by remember { mutableIntStateOf(0) }

    var brushSizePx by remember { mutableFloatStateOf(42f) }
    var isEraser by remember { mutableStateOf(false) }
    var splitPosition by remember { mutableFloatStateOf(0.55f) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activePhotoPath) {
        val path = activePhotoPath
        if (!path.isNullOrBlank()) {
            val loaded = viewModel.loadBitmap(path)
            originalBitmap = loaded
            strokes.clear()
            redoStrokes.clear()
            recolouredBitmap = loaded
        } else {
            originalBitmap = null
            recolouredBitmap = null
        }
    }

    // Recompute mask + tinted preview whenever strokes, colour, or finish change
    LaunchedEffect(originalBitmap, strokeRevision, selectedHex, selectedFinish) {
        val orig = originalBitmap ?: return@LaunchedEffect
        val targetInt = parseHexColor(selectedHex).toArgb()
        val result = withContext(Dispatchers.Default) {
            val mask = Bitmap.createBitmap(orig.width, orig.height, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(mask)
            val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                style = AndroidPaint.Style.STROKE
                strokeCap = AndroidPaint.Cap.ROUND
                strokeJoin = AndroidPaint.Join.ROUND
            }
            strokes.forEach { stroke ->
                paint.strokeWidth = stroke.brushFraction * orig.width
                if (stroke.isEraser) {
                    paint.color = android.graphics.Color.TRANSPARENT
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                } else {
                    paint.color = android.graphics.Color.WHITE
                    paint.xfermode = null
                }
                if (stroke.points.size == 1) {
                    val p = stroke.points.first()
                    canvas.drawPoint(p.x * orig.width, p.y * orig.height, paint)
                } else if (stroke.points.size > 1) {
                    val path = AndroidPath()
                    path.moveTo(stroke.points.first().x * orig.width, stroke.points.first().y * orig.height)
                    for (i in 1 until stroke.points.size) {
                        val pt = stroke.points[i]
                        path.lineTo(pt.x * orig.width, pt.y * orig.height)
                    }
                    canvas.drawPath(path, paint)
                }
            }
            maskBitmap = mask
            renderRecolouredPhoto(orig, mask, targetInt, selectedFinish)
        }
        recolouredBitmap = result
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = launchCamera,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("photo_painter_take_photo")
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Photo")
                }
                OutlinedButton(
                    onClick = launchGallery,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("photo_painter_pick_photo")
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
            }
        }

        if (originalBitmap == null) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.CameraAlt,
                    title = "No Car Photo Loaded",
                    subtitle = "Take a photo of a vehicle in your bay, import from your gallery, or choose one from your Car Photo Library.",
                    action = {
                        TextButton(onClick = onGoToLibrary) {
                            Text("Browse Car Photo Library")
                        }
                    }
                )
            }
        } else {
            item {
                val orig = originalBitmap!!
                val preview = recolouredBitmap ?: orig
                val aspect = (orig.width.toFloat() / orig.height.toFloat()).coerceIn(0.75f, 1.85f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .pointerInput(brushSizePx, isEraser) {
                            detectTapGestures { pos ->
                                val norm = Offset(
                                    (pos.x / size.width).coerceIn(0f, 1f),
                                    (pos.y / size.height).coerceIn(0f, 1f)
                                )
                                strokes.add(
                                    MaskStroke(
                                        points = listOf(norm),
                                        brushFraction = (brushSizePx / size.width).coerceIn(0.015f, 0.14f),
                                        isEraser = isEraser
                                    )
                                )
                                redoStrokes.clear()
                                strokeRevision++
                            }
                        }
                        .pointerInput(brushSizePx, isEraser) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    activePoints = listOf(
                                        Offset(
                                            (start.x / size.width).coerceIn(0f, 1f),
                                            (start.y / size.height).coerceIn(0f, 1f)
                                        )
                                    )
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val pt = Offset(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (change.position.y / size.height).coerceIn(0f, 1f)
                                    )
                                    activePoints = activePoints + pt
                                },
                                onDragEnd = {
                                    if (activePoints.isNotEmpty()) {
                                        strokes.add(
                                            MaskStroke(
                                                points = activePoints,
                                                brushFraction = (brushSizePx / size.width).coerceIn(0.015f, 0.14f),
                                                isEraser = isEraser
                                            )
                                        )
                                        redoStrokes.clear()
                                        activePoints = emptyList()
                                        strokeRevision++
                                    }
                                }
                            )
                        }
                ) {
                    val origImg = remember(orig) { orig.asImageBitmap() }
                    val prevImg = remember(preview) { preview.asImageBitmap() }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw original on right side, recoloured on left side of splitPosition
                        drawImage(
                            image = origImg,
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                        )
                        val splitX = (size.width * splitPosition).roundToInt().coerceIn(0, size.width.roundToInt())
                        if (splitX > 0) {
                            drawImage(
                                image = prevImg,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(
                                    (prevImg.width * splitPosition).roundToInt().coerceIn(1, prevImg.width),
                                    prevImg.height
                                ),
                                dstOffset = IntOffset.Zero,
                                dstSize = IntSize(splitX, size.height.roundToInt())
                            )
                        }
                        // Live stroke preview while dragging
                        if (activePoints.size > 1) {
                            val liveColor = if (isEraser) Color.White.copy(alpha = 0.55f)
                            else parseHexColor(selectedHex).copy(alpha = 0.65f)
                            for (i in 0 until activePoints.lastIndex) {
                                val p1 = Offset(activePoints[i].x * size.width, activePoints[i].y * size.height)
                                val p2 = Offset(activePoints[i + 1].x * size.width, activePoints[i + 1].y * size.height)
                                drawLine(liveColor, p1, p2, strokeWidth = brushSizePx)
                            }
                        }
                        // Split divider line
                        val divX = size.width * splitPosition
                        drawLine(Color.White, Offset(divX, 0f), Offset(divX, size.height), strokeWidth = 4f)
                    }

                    // Before / After Labels
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text("PAINTED", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text("ORIGINAL", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }

            // Before/After & Brush Controls
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text("Before / After Split (${(splitPosition * 100).roundToInt()}%)", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = splitPosition,
                            onValueChange = { splitPosition = it },
                            valueRange = 0f..1f
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = !isEraser,
                                    onClick = { isEraser = false },
                                    label = { Text("Paint Panel") },
                                    leadingIcon = { Icon(Icons.Filled.Brush, null, modifier = Modifier.size(16.dp)) }
                                )
                                FilterChip(
                                    selected = isEraser,
                                    onClick = { isEraser = true },
                                    label = { Text("Eraser") }
                                )
                            }
                            Row {
                                IconButton(
                                    enabled = strokes.isNotEmpty(),
                                    onClick = {
                                        val last = strokes.removeAt(strokes.lastIndex)
                                        redoStrokes.add(last)
                                        strokeRevision++
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo stroke")
                                }
                                IconButton(
                                    enabled = redoStrokes.isNotEmpty(),
                                    onClick = {
                                        val next = redoStrokes.removeAt(redoStrokes.lastIndex)
                                        strokes.add(next)
                                        strokeRevision++
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo stroke")
                                }
                                IconButton(
                                    enabled = strokes.isNotEmpty(),
                                    onClick = {
                                        strokes.clear()
                                        redoStrokes.clear()
                                        strokeRevision++
                                    }
                                ) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear mask")
                                }
                            }
                        }

                        Text("Brush Size (${brushSizePx.roundToInt()}px)", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = brushSizePx,
                            onValueChange = { brushSizePx = it },
                            valueRange = 16f..88f
                        )
                    }
                }
            }

            // OEM & Finish Picker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select OEM Colour & Finish", style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(oemPaints.take(12), key = { it.id }) { paint ->
                            val isSelected = selectedHex.equals(paint.hexColor, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(68.dp)
                                    .clickable {
                                        selectedHex = paint.hexColor
                                        selectedFinish = paint.finish
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(paint.hexColor))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(paint.oemCode, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Solid", "Metallic", "Pearl", "Matte", "Candy")) { fin ->
                            FilterChip(
                                selected = selectedFinish == fin,
                                onClick = { selectedFinish = fin },
                                label = { Text(fin) }
                            )
                        }
                    }
                }
            }

            // Save & Share Actions
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Project")
                    }
                    Button(
                        onClick = {
                            val bmp = recolouredBitmap ?: originalBitmap ?: return@Button
                            scope.launch {
                                val uri = viewModel.createShareExportUri(bmp, "photo_preview")
                                if (uri != null) {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, "Share Repaint Preview"))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Preview")
                    }
                }
            }
        }

        item { ApproximationDisclaimerBanner() }
    }

    if (showSaveDialog) {
        var title by remember { mutableStateOf("Panel Recolour Preview") }
        var vehicleLabel by remember { mutableStateOf("Customer Vehicle") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Photo Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Project Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vehicleLabel,
                        onValueChange = { vehicleLabel = it },
                        label = { Text("Vehicle / Plate") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.savePhotoProject(
                        title = title,
                        vehicleLabel = vehicleLabel,
                        photoPath = activePhotoPath ?: "",
                        maskBitmap = maskBitmap,
                        hexColor = selectedHex,
                        finish = selectedFinish
                    )
                    showSaveDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CarPhotoLibrarySection(
    viewModel: MotoopaintViewModel,
    onOpenInPainter: (String) -> Unit
) {
    val photos by viewModel.carPhotos.collectAsStateWithLifecycle()
    val (launchCamera, launchGallery) = rememberVehiclePhotoCapture(viewModel)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = launchCamera,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library_take_car_photo")
                ) {
                    Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Take Car Photo")
                }
                OutlinedButton(
                    onClick = launchGallery,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library_import_car_photo")
                ) {
                    Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Photo")
                }
            }
        }

        if (photos.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "Car Photo Library is Empty",
                    subtitle = "Capture customer vehicles in the bay or import photos from your phone's photo library. Everything stays offline on your device."
                )
            }
        } else {
            items(photos, key = { it.id }) { photo ->
                CarPhotoLibraryCard(
                    photo = photo,
                    onPaint = { onOpenInPainter(photo.filePath) },
                    onDelete = { viewModel.deleteCarPhoto(photo) }
                )
            }
        }
    }
}

@Composable
private fun CarPhotoLibraryCard(
    photo: CarPhotoEntity,
    onPaint: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(photo.vehicleLabel.ifBlank { photo.title }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${if (photo.capturedWithCamera) "Bay Camera" else "Phone Gallery"} • ${formatShortDate(photo.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onPaint) {
                        Icon(Icons.Filled.Palette, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paint")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete car photo", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
