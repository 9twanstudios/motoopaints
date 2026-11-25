package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.OemPaintEntity
import com.example.data.local.PaintJobEntity
import com.example.data.local.RefinishSystemEntity
import com.example.data.local.StandardColourEntity
import com.example.data.local.VehicleModelEntity
import com.example.ui.components.ApproximationDisclaimerBanner
import com.example.ui.components.CodePill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SwatchCircle
import com.example.ui.components.VerificationBadge
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.MotoopaintViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColoursScreen(
    viewModel: MotoopaintViewModel,
    onOpenInStudio: (hex: String, finish: String) -> Unit,
    onJobCreated: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val oemPaints by viewModel.oemPaints.collectAsStateWithLifecycle()
    val standardColours by viewModel.standardColours.collectAsStateWithLifecycle()
    val vehicleModels by viewModel.vehicleModels.collectAsStateWithLifecycle()
    val refinishSystems by viewModel.refinishSystems.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedOemDetail by remember { mutableStateOf<OemPaintEntity?>(null) }
    var selectedChartColourId by remember { mutableStateOf<Long?>(null) }

    val q = filters.query.trim().lowercase()

    val filteredOem = remember(oemPaints, filters) {
        oemPaints.filter { paint ->
            val matchesQuery = q.isBlank() ||
                paint.oemCode.lowercase().contains(q) ||
                paint.factoryName.lowercase().contains(q) ||
                paint.make.lowercase().contains(q) ||
                paint.colourFamily.lowercase().contains(q) ||
                paint.notes.lowercase().contains(q)
            val matchesMake = filters.make == "All" || paint.make == filters.make
            val matchesFinish = filters.finish == "All" || paint.finish == filters.finish
            val matchesVer = filters.verification == "All" || paint.verificationState == filters.verification
            matchesQuery && matchesMake && matchesFinish && matchesVer
        }
    }

    val filteredDict = remember(standardColours, filters) {
        standardColours.filter { item ->
            val matchesQuery = q.isBlank() ||
                item.displayName.lowercase().contains(q) ||
                item.aliases.lowercase().contains(q) ||
                item.colourFamily.lowercase().contains(q)
            val matchesFinish = filters.finish == "All" || item.finish == filters.finish
            matchesQuery && matchesFinish
        }
    }

    val filteredVehicles = remember(vehicleModels, filters) {
        vehicleModels.filter { v ->
            val matchesQuery = q.isBlank() ||
                v.make.lowercase().contains(q) ||
                v.model.lowercase().contains(q) ||
                v.bodyType.lowercase().contains(q) ||
                v.vehicleClass.lowercase().contains(q) ||
                v.evidenceSourceId.lowercase().contains(q)
            val matchesMake = filters.make == "All" || v.make == filters.make
            val matchesBody = filters.bodyType == "All" || v.bodyType == filters.bodyType
            matchesQuery && matchesMake && matchesBody
        }
    }

    val savedPaints = remember(filteredOem) { filteredOem.filter { it.isFavourite } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search + Filter Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
        ) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = { text -> viewModel.updateFilters { it.copy(query = text) } },
                placeholder = { Text("Code, Toyota, VW, Probox, silver grey...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search catalogue") },
                trailingIcon = {
                    if (filters.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateFilters { it.copy(query = "") } }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .testTag("colours_search_input")
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (filters.make != "All" || filters.finish != "All" || filters.bodyType != "All" || filters.verification != "All")
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showFilterSheet = true
                    }
                    .testTag("colours_filter_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Open catalogue filters",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Quick Colour & Model Prompt Chips + Live Input Feedback
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(listOf("1F7", "Silver Grey", "Pearl White", "Maroon", "Probox", "Hiace", "Volkswagen")) { hint ->
                AssistChip(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.updateFilters { it.copy(query = hint) }
                    },
                    label = { Text(hint, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        AnimatedVisibility(visible = q.isNotEmpty() || filters.make != "All" || filters.finish != "All" || filters.bodyType != "All") {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${filteredOem.size} OEM • ${filteredDict.size} Chart • ${filteredVehicles.size} Vehicles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.resetFilters() }
                    )
                }
            }
        }

        val tabs = listOf("OEM Codes", "Colour Chart", "Vehicles", "Saved")
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = idx
                    },
                    text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ApproximationDisclaimerBanner()
            }

            when (selectedTab) {
                0 -> {
                    if (filteredOem.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.ColorLens,
                                title = "No OEM codes match",
                                subtitle = "Try clearing filters or tapping Colour Chart for standard aliases like 'silver grey'.",
                                action = {
                                    TextButton(onClick = viewModel::resetFilters) {
                                        Text("Reset Filters")
                                    }
                                }
                            )
                        }
                    } else {
                        items(filteredOem, key = { it.id }) { paint ->
                            OemPaintCard(
                                paint = paint,
                                onToggleFav = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.toggleFavourite(paint)
                                },
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.recordOemUsage(paint.id)
                                    selectedOemDetail = paint
                                }
                            )
                        }
                    }
                }

                1 -> {
                    if (filteredDict.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.Palette,
                                title = "No chart colours match",
                                subtitle = "Try terms like Pearl White, Maroon, Copper, or Ivory."
                            )
                        }
                    } else {
                        // 2-Column Visual Colour Chart Grid
                        val rows = filteredDict.chunked(2)
                        items(rows, key = { row -> row.first().id }) { pair ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                pair.forEach { item ->
                                    ColourChartGridTile(
                                        item = item,
                                        isSelected = selectedChartColourId == item.id,
                                        onSelect = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedChartColourId = item.id
                                            viewModel.showMessage("${item.displayName} (${item.finish}) selected")
                                        },
                                        onPreview = { onOpenInStudio(item.hexColor, item.finish) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                2 -> {
                    items(filteredVehicles, key = { it.id }) { vehicle ->
                        VehicleCatalogueCard(
                            vehicle = vehicle,
                            onLookupMake = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.recordVehicleLookup(vehicle.id)
                                viewModel.updateFilters { it.copy(make = vehicle.make, query = "") }
                                selectedTab = 0
                            }
                        )
                    }
                    item {
                        Text(
                            text = "Refinish System Pointers (Non-Proprietary)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(refinishSystems, key = { it.id }) { sys ->
                        RefinishSystemCard(sys = sys)
                    }
                }

                3 -> {
                    if (savedPaints.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.BookmarkBorder,
                                title = "No saved OEM codes yet",
                                subtitle = "Tap the bookmark icon on any OEM paint to pin it for quick bay access."
                            )
                        }
                    } else {
                        items(savedPaints, key = { it.id }) { paint ->
                            OemPaintCard(
                                paint = paint,
                                onToggleFav = { viewModel.toggleFavourite(paint) },
                                onClick = { selectedOemDetail = paint }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filter Catalogue", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset All")
                    }
                }

                Text("Make", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Toyota", "Nissan", "Isuzu", "Subaru", "Mazda", "Honda", "Mitsubishi", "Volkswagen").forEach { mk ->
                        FilterChip(
                            selected = filters.make == mk,
                            onClick = { viewModel.updateFilters { it.copy(make = mk) } },
                            label = { Text(mk) }
                        )
                    }
                }

                Text("Finish", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Solid", "Metallic", "Pearl").forEach { fin ->
                        FilterChip(
                            selected = filters.finish == fin,
                            onClick = { viewModel.updateFilters { it.copy(finish = fin) } },
                            label = { Text(fin) }
                        )
                    }
                }

                Text("Vehicle Body Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Panel Van", "Station Wagon", "Saloon", "Minibus/Matatu", "Pick-Up", "SUV", "Lorry/Truck", "Unspecified").forEach { bt ->
                        FilterChip(
                            selected = filters.bodyType == bt,
                            onClick = { viewModel.updateFilters { it.copy(bodyType = bt) } },
                            label = { Text(bt) }
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }

    selectedOemDetail?.let { paint ->
        val matchingSystems = remember(paint, refinishSystems) {
            refinishSystems.filter { it.oemCodeRef.equals(paint.oemCode, ignoreCase = true) }
        }
        ModalBottomSheet(onDismissRequest = { selectedOemDetail = null }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(parseHexColor(paint.hexColor))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CodePill(code = paint.oemCode)
                            IconButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    clipboard.setText(AnnotatedString("${paint.make} ${paint.oemCode} (${paint.factoryName})"))
                                    viewModel.showMessage("Copied ${paint.oemCode} to clipboard")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy OEM code", modifier = Modifier.size(16.dp))
                            }
                            VerificationBadge(state = paint.verificationState)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(paint.factoryName, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${paint.make} • ${paint.yearRange} • ${paint.finish}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("Colour & Provenance Specs", style = MaterialTheme.typography.labelLarge)
                        Text("Approx HEX / RGB: ${paint.hexColor}  (${paint.rgbSummary})", style = MaterialTheme.typography.labelSmall)
                        Text("LAB Ref: ${paint.labSummary}", style = MaterialTheme.typography.labelSmall)
                        Text("Source: ${paint.source} (${paint.sourceDate})", style = MaterialTheme.typography.bodySmall)
                        Text("Licence: ${paint.licenceStatus.replace('_', ' ')}", style = MaterialTheme.typography.bodySmall)
                        if (paint.notes.isNotBlank()) {
                            Text("Workshop Note: ${paint.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (matchingSystems.isNotEmpty()) {
                    matchingSystems.forEach { sys ->
                        RefinishSystemCard(sys = sys)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveJob(
                                PaintJobEntity(
                                    vehicleLabel = "${paint.make} Customer Vehicle",
                                    oemCode = paint.oemCode,
                                    colourName = paint.factoryName,
                                    finish = paint.finish,
                                    status = "Quoted",
                                    notes = "Created from Colour Library lookup"
                                )
                            )
                            selectedOemDetail = null
                            onJobCreated()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Job")
                    }
                    Button(
                        onClick = {
                            selectedOemDetail = null
                            onOpenInStudio(paint.hexColor, paint.finish)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test in Studio")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun OemPaintCard(
    paint: OemPaintEntity,
    onToggleFav: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            SwatchCircle(hexColor = paint.hexColor, finish = paint.finish, size = 46.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CodePill(code = paint.oemCode)
                    VerificationBadge(state = paint.verificationState)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paint.factoryName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${paint.make} • ${paint.yearRange} • ${paint.finish}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = if (paint.isFavourite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Save OEM code",
                    tint = if (paint.isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColourChartGridTile(
    item: StandardColourEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        label = "chart_border"
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(parseHexColor(item.hexColor))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${item.colourFamily} • ${item.finish}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.aliases,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(
                onClick = onPreview,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Preview in Studio →")
            }
        }
    }
}

@Composable
private fun VehicleCatalogueCard(
    vehicle: VehicleModelEntity,
    onLookupMake: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onLookupMake)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = vehicle.model,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${vehicle.make} ${vehicle.model}", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = listOfNotNull(
                        vehicle.bodyType.takeIf { it != "Unspecified" },
                        vehicle.vehicleClass.replace('_', ' ').takeIf { it.isNotBlank() },
                        vehicle.marketOrigin.takeIf { it != "Unspecified" }
                    ).joinToString(" • ").ifBlank { "Details pending verification" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${vehicle.evidenceSourceId.ifBlank { vehicle.source }} • ${vehicle.seedStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text("OEMs →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RefinishSystemCard(sys: RefinishSystemEntity) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${sys.paintBrand} — ${sys.productSystem}", style = MaterialTheme.typography.titleSmall)
                CodePill(code = sys.oemCodeRef)
            }
            Text("Formula Ref: ${sys.formulaRef}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("Compatible: ${sys.compatibleProducts}", style = MaterialTheme.typography.bodySmall)
            Text(sys.licenceNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
