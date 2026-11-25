package com.example.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.local.StockItemEntity
import com.example.ui.components.ThemeSwitchPill
import com.example.ui.components.WorkshopPermissionHelper
import com.example.ui.components.formatKsh
import com.example.ui.components.rememberWorkshopPermissionsState
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.viewmodel.MotoopaintViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted

enum class SettingsCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    PROFILE("Workshop Profile", "Bay name & overview", Icons.Filled.Person),
    THEME("Theme & Display", "Daylight / Dark Booth", Icons.Filled.Palette),
    PERMISSIONS("Permissions & Storage", "Camera & storage helper", Icons.Filled.Security),
    BACKUP("Vault & Backup", "JSON export & restore", Icons.Filled.Backup),
    PROVENANCE("Data & Provenance", "Sources & match rules", Icons.Filled.Verified),
    RELEASE("App & Release Info", "Offline APK & version", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MoreScreen(
    viewModel: MotoopaintViewModel,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    triggerNewStockDialog: Boolean,
    onDismissNewStockTrigger: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val stockItems by viewModel.stockItems.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val workshopName by viewModel.workshopName.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    var selectedStockCategory by remember { mutableStateOf("All") }
    var showAddStockDialog by remember { mutableStateOf(false) }

    if (triggerNewStockDialog) {
        onSelectSubTab(0)
        showAddStockDialog = true
        onDismissNewStockTrigger()
    }

    if (selectedSubTab == 2 && selectedCategory != null) {
        BackHandler { selectedCategory = null }
    }

    val permissionsState = rememberWorkshopPermissionsState { results ->
        val camOk = results[Manifest.permission.CAMERA] == true
        viewModel.showMessage(
            if (camOk) "Workshop camera & storage permissions ready"
            else "Camera permission denied — Photo Picker still works without permissions"
        )
    }
    val cameraGranted = permissionsState.permissions.firstOrNull {
        it.permission == Manifest.permission.CAMERA
    }?.status?.isGranted == true

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) viewModel.exportBackup(uri)
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.restoreBackup(uri)
    }

    val categories = listOf("All", "Colour Material", "Toner", "Preparation", "Finishing", "Consumable", "Equipment")
    val filteredStock = remember(stockItems, selectedStockCategory) {
        if (selectedStockCategory == "All") stockItems else stockItems.filter { it.category == selectedStockCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedSubTab.coerceIn(0, 2)) {
            listOf("Stock", "Analytics", "Profile & Settings").forEachIndexed { idx, label ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectSubTab(idx)
                    },
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedSubTab) {
                0 -> {
                    item {
                        Button(
                            onClick = { showAddStockDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Workshop Stock Item")
                        }
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedStockCategory == cat,
                                    onClick = { selectedStockCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }

                    items(filteredStock, key = { it.id }) { item ->
                        val isLow = item.quantity <= item.reorderThreshold
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isLow) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${item.brand} • ${item.productCode} • ${item.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isLow) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CautionAmber.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = "LOW STOCK",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CautionAmber,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "On hand: ${item.quantity} ${item.unit}  •  ${formatKsh(item.unitCostKsh)}/${item.unit}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        TextButton(onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.saveStockItem(item.copy(quantity = item.quantity + 1.0))
                                        }) {
                                            Text("+1 ${item.unit}")
                                        }
                                        IconButton(onClick = { viewModel.deleteStockItem(item.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete stock item")
                                        }
                                    }
                                }
                                if (item.supplier.isNotBlank() || item.batchLot.isNotBlank()) {
                                    Text(
                                        text = "Supplier: ${item.supplier} • Lot: ${item.batchLot.ifBlank { "-" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnalyticsMetricCard(
                                label = "Total Jobs",
                                value = "${analytics.totalJobs}",
                                sub = "${analytics.completedJobs} done • ${analytics.reworkJobs} rework",
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricCard(
                                label = "Material Spend",
                                value = formatKsh(analytics.totalMaterialCostKsh),
                                sub = "${analytics.lowStockItems.size} low stock alerts",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnalyticsMetricCard(
                                label = "Saved Liveries",
                                value = "${analytics.savedLiveryCount}",
                                sub = "Nganya & fleet designs",
                                modifier = Modifier.weight(1f)
                            )
                            AnalyticsMetricCard(
                                label = "Bay Car Photos",
                                value = "${analytics.carPhotoCount}",
                                sub = "Stored offline locally",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

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
                                Text("Top Looked-Up Kenyan Vehicles", style = MaterialTheme.typography.titleSmall)
                                val maxLookup = (analytics.topVehicles.maxOfOrNull { it.lookupCount } ?: 1).coerceAtLeast(1)
                                analytics.topVehicles.forEach { v ->
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("${v.make} ${v.model}", style = MaterialTheme.typography.bodySmall)
                                        Text("${v.lookupCount}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    LinearProgressIndicator(
                                        progress = { v.lookupCount.toFloat() / maxLookup.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }

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
                                Text("Spray-Out Acceptance Log", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Accepted: ${analytics.acceptedSprayOuts}  •  Adjusted: ${analytics.adjustedSprayOuts}  •  Rejected: ${analytics.rejectedSprayOuts}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Note: Technician-entered spray-out outcomes track shop acceptance and are not a substitute for measured spectrophotometer colour accuracy.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Grid-Based Profile Management & Settings Hierarchy
                else -> {
                    item {
                        AnimatedContent(targetState = selectedCategory, label = "settings_hierarchy") { cat ->
                            if (cat == null) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Workshop Profile Header Card
                                    Surface(
                                        shape = MaterialTheme.shapes.large,
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Person,
                                                        contentDescription = "Workshop profile",
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(workshopName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        text = "${analytics.totalJobs} jobs • ${stockItems.size} stock • ${analytics.carPhotoCount} photos",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            ThemeSwitchPill(
                                                themeMode = themeMode,
                                                onCycleTheme = {
                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    viewModel.cycleThemeMode()
                                                }
                                            )
                                        }
                                    }

                                    Text("Settings & Profile Grid", style = MaterialTheme.typography.titleSmall)

                                    // 2-Column Settings Category Grid
                                    val gridRows = SettingsCategory.entries.chunked(2)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        gridRows.forEach { pair ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                pair.forEach { item ->
                                                    SettingsGridCard(
                                                        category = item,
                                                        badgeText = when (item) {
                                                            SettingsCategory.PERMISSIONS -> if (cameraGranted) "Camera OK" else "Check"
                                                            SettingsCategory.THEME -> themeMode.label
                                                            SettingsCategory.BACKUP -> "JSON"
                                                            else -> null
                                                        },
                                                        onClick = {
                                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            selectedCategory = item
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("settings_grid_${item.name.lowercase()}")
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Breadcrumb Back Bar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedCategory = null
                                            }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Settings Grid")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Settings Grid / ${cat.title}", style = MaterialTheme.typography.titleMedium)
                                    }

                                    when (cat) {
                                        SettingsCategory.PROFILE -> {
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Workshop Identity", style = MaterialTheme.typography.titleMedium)
                                                    var nameDraft by remember(workshopName) { mutableStateOf(workshopName) }
                                                    OutlinedTextField(
                                                        value = nameDraft,
                                                        onValueChange = { nameDraft = it },
                                                        label = { Text("Workshop / Bay Name") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Button(
                                                        onClick = {
                                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.setWorkshopName(nameDraft)
                                                            viewModel.showMessage("Workshop profile saved")
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("Save Workshop Profile")
                                                    }
                                                }
                                            }
                                        }

                                        SettingsCategory.THEME -> {
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Display Mode", style = MaterialTheme.typography.titleMedium)
                                                    Text(
                                                        text = "High-contrast Daylight mode is built for outdoor yards; Dark Booth reduces glare inside spray bays.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        ThemeMode.entries.forEach { mode ->
                                                            FilterChip(
                                                                selected = themeMode == mode,
                                                                onClick = {
                                                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                    viewModel.setThemeMode(mode)
                                                                },
                                                                label = { Text(mode.label) },
                                                                modifier = Modifier.testTag("theme_option_${mode.name.lowercase()}")
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        SettingsCategory.PERMISSIONS -> {
                                            val allPermissionsOk = permissionsState.allPermissionsGranted
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Phone Permissions & Storage Policy", style = MaterialTheme.typography.titleMedium)
                                                    PermissionStatusRow(
                                                        title = "Camera (android.permission.CAMERA)",
                                                        status = if (cameraGranted) "Granted for CameraX Bay Scan & Photos" else "Not Granted yet",
                                                        isOk = cameraGranted
                                                    )
                                                    PermissionStatusRow(
                                                        title = "External Storage & Photo Picker",
                                                        status = if (allPermissionsOk) {
                                                            "Accompanist permissions & Photo Picker ready"
                                                        } else {
                                                            "Legacy storage scoped by SDK; Photo Picker works without permission"
                                                        },
                                                        isOk = allPermissionsOk
                                                    )
                                                    PermissionStatusRow(
                                                        title = "Local Vault & Backups (SAF)",
                                                        status = "App-Private Vault + Document Picker",
                                                        isOk = true
                                                    )
                                                    PermissionStatusRow(
                                                        title = "Network / Cloud Telemetry",
                                                        status = "Disabled (No INTERNET permission in APK)",
                                                        isOk = true
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Button(
                                                            onClick = { permissionsState.launchMultiplePermissionRequest() },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text(if (allPermissionsOk) "Re-check Access" else "Grant Permissions")
                                                        }
                                                        OutlinedButton(
                                                            onClick = { WorkshopPermissionHelper.openAppSettings(context) },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("App Settings")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        SettingsCategory.BACKUP -> {
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Offline Vault Backup & Restore", style = MaterialTheme.typography.titleMedium)
                                                    Text(
                                                        text = "Export all jobs, formulas, spray-outs, stock items, and livery designs to a local JSON file.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Button(
                                                            onClick = { exportBackupLauncher.launch("motoopaint_backup_${System.currentTimeMillis()}.json") },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Export JSON")
                                                        }
                                                        OutlinedButton(
                                                            onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Icon(Icons.Filled.UploadFile, null, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Restore JSON")
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        SettingsCategory.PROVENANCE -> {
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Data Provenance & Match Rules", style = MaterialTheme.typography.titleMedium)
                                                    Text("• Vehicle records carry explicit evidence IDs (SRC-GIZ-NTSA-2025, SRC-MOTIFITI-2026W46/W47) and leave unverified body/year fields blank.", style = MaterialTheme.typography.bodySmall)
                                                    Text("• Proprietary manufacturer toner mixing formulas are never fabricated; only public reference codes and shop-entered formulas are stored.", style = MaterialTheme.typography.bodySmall)
                                                    Text("• Digital swatches and camera samples are approximations; always confirm with physical colour cards and spray-out tests.", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }

                                        SettingsCategory.RELEASE -> {
                                            Surface(
                                                shape = MaterialTheme.shapes.large,
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Text("Motoopaint Build & Release", style = MaterialTheme.typography.titleMedium)
                                                    Text("Version: ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall)
                                                    Text("Package ID: ${BuildConfig.APPLICATION_ID}", style = MaterialTheme.typography.labelSmall)
                                                    Text("Model: One-Time Offline Purchase • Zero Subscriptions • Zero Cloud Sync", style = MaterialTheme.typography.bodySmall)
                                                    HorizontalDivider()
                                                    Text("GitHub Releases Workflow enabled (.github/workflows/android-release.yml) for automated v* tag APK builds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStockDialog) {
        var name by remember { mutableStateOf("") }
        var brand by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Colour Material") }
        var unit by remember { mutableStateOf("L") }
        var qty by remember { mutableStateOf("1.0") }
        var cost by remember { mutableStateOf("2500") }
        var supplier by remember { mutableStateOf("") }
        var threshold by remember { mutableStateOf("0.5") }

        AlertDialog(
            onDismissRequest = { showAddStockDialog = false },
            title = { Text("Add Stock Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Colour Material", "Toner", "Preparation", "Finishing", "Consumable").forEach { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (L/pcs)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cost,
                            onValueChange = { cost = it },
                            label = { Text("Cost KSh/unit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = threshold,
                            onValueChange = { threshold = it },
                            label = { Text("Reorder Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.saveStockItem(
                            StockItemEntity(
                                category = category,
                                name = name.trim(),
                                brand = brand.trim(),
                                productCode = code.trim(),
                                unit = unit.trim().ifBlank { "L" },
                                quantity = qty.toDoubleOrNull() ?: 1.0,
                                unitCostKsh = cost.toDoubleOrNull() ?: 0.0,
                                supplier = supplier.trim(),
                                reorderThreshold = threshold.toDoubleOrNull() ?: 0.5
                            )
                        )
                        showAddStockDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddStockDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsGridCard(
    category: SettingsCategory,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(category.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    status: String,
    isOk: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = if (isOk) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = status,
            tint = if (isOk) VerifiedEmerald else CautionAmber,
            modifier = Modifier.size(20.dp)
        )
    }
}
