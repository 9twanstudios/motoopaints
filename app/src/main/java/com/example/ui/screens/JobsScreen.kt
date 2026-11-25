package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.PaintJobEntity
import com.example.data.local.SprayOutEntity
import com.example.data.local.StockItemEntity
import com.example.data.local.TechnicianFormulaEntity
import com.example.ui.components.CodePill
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatKsh
import com.example.ui.components.formatShortDate
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.viewmodel.MotoopaintViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobsScreen(
    viewModel: MotoopaintViewModel,
    triggerNewJobDialog: Boolean,
    onDismissNewJobTrigger: () -> Unit
) {
    val jobs by viewModel.paintJobs.collectAsStateWithLifecycle()
    val formulas by viewModel.technicianFormulas.collectAsStateWithLifecycle()
    val sprayOuts by viewModel.sprayOuts.collectAsStateWithLifecycle()
    val stockItems by viewModel.stockItems.collectAsStateWithLifecycle()
    val stockUsage by viewModel.stockUsage.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showNewJobDialog by remember { mutableStateOf(false) }
    var showNewFormulaDialog by remember { mutableStateOf(false) }
    var detailJob by remember { mutableStateOf<PaintJobEntity?>(null) }
    var sprayOutJobTarget by remember { mutableStateOf<PaintJobEntity?>(null) }
    var usageJobTarget by remember { mutableStateOf<PaintJobEntity?>(null) }

    if (triggerNewJobDialog) {
        showNewJobDialog = true
        onDismissNewJobTrigger()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            listOf("Paint Jobs", "Formulas", "Spray-Outs").forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Button(
                            onClick = { showNewJobDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Vehicle Paint Job")
                        }
                    }
                    if (jobs.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.Build,
                                title = "No Paint Jobs Yet",
                                subtitle = "Create a job to track OEM colour, formula adjustments, spray-outs, and material costs in KSh."
                            )
                        }
                    } else {
                        items(jobs, key = { it.id }) { job ->
                            val jobCost = remember(stockUsage, job.id) {
                                stockUsage.filter { it.jobId == job.id }.sumOf { it.costKsh }
                            }
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable { detailJob = job }
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
                                        Text(job.vehicleLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (job.status == "Rework") MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                                            else MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = job.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (job.status == "Rework") MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CodePill(code = job.oemCode.ifBlank { "CUSTOM" })
                                        Text("${job.colourName} • ${job.finish}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Updated ${formatShortDate(job.updatedAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Material: ${formatKsh(jobCost)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Button(
                            onClick = { showNewFormulaDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Technician Formula")
                        }
                    }
                    items(formulas, key = { it.id }) { formula ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        CodePill(code = formula.oemCodeRef)
                                        Text(formula.title, style = MaterialTheme.typography.titleSmall)
                                    }
                                    IconButton(onClick = { viewModel.deleteFormula(formula.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete formula")
                                    }
                                }
                                Text(
                                    text = "${formula.baseSystem} • Ratio: ${formula.mixingRatio}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(formula.tonerNotes, style = MaterialTheme.typography.bodySmall)
                                if (formula.notes.isNotBlank()) {
                                    Text(
                                        text = formula.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Technician Spray-Out Log: records subjective shop acceptance under sunlight or booth lighting—not an objective spectrophotometer score.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    if (sprayOuts.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.Science,
                                title = "No Spray-Outs Logged",
                                subtitle = "Open any Paint Job to log a spray-out test card outcome."
                            )
                        }
                    } else {
                        items(sprayOuts, key = { it.id }) { sp ->
                            val badgeColor = when (sp.outcome) {
                                "ACCEPTED" -> VerifiedEmerald
                                "ADJUSTED" -> CautionAmber
                                else -> AlertCrimson
                            }
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(sp.panelName, style = MaterialTheme.typography.titleSmall)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = badgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = sp.outcome,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text("${sp.jobLabel} • ${sp.lightingCondition}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    if (sp.notes.isNotBlank()) {
                                        Text(sp.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Job Detail Bottom Sheet
    detailJob?.let { currentJob ->
        val jobUsage = remember(stockUsage, currentJob.id) {
            stockUsage.filter { it.jobId == currentJob.id }
        }
        ModalBottomSheet(onDismissRequest = { detailJob = null }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(currentJob.vehicleLabel, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${currentJob.colourName} (${currentJob.oemCode}) • ${currentJob.finish}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Workflow Status", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Quoted", "Prep", "Basecoat", "Clearcoat", "Completed", "Rework").forEach { st ->
                        FilterChip(
                            selected = currentJob.status == st,
                            onClick = {
                                viewModel.updateJobStatus(currentJob, st)
                                detailJob = currentJob.copy(status = st)
                            },
                            label = { Text(st) }
                        )
                    }
                }

                if (currentJob.formulaNotes.isNotBlank()) {
                    Text("Formula Note: ${currentJob.formulaNotes}", style = MaterialTheme.typography.bodySmall)
                }
                if (currentJob.notes.isNotBlank()) {
                    Text("Notes: ${currentJob.notes}", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Materials Consumed", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = formatKsh(jobUsage.sumOf { it.costKsh }),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (jobUsage.isEmpty()) {
                    Text("No stock logged on this job yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    jobUsage.forEach { u ->
                        Text(
                            text = "• ${u.stockItemName}: ${u.quantityUsed} ${u.unit} (${formatKsh(u.costKsh)})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            usageJobTarget = currentJob
                            detailJob = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Inventory2, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Stock")
                    }
                    Button(
                        onClick = {
                            sprayOutJobTarget = currentJob
                            detailJob = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Science, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Spray-Out")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showNewJobDialog) {
        var vehicle by remember { mutableStateOf("") }
        var oemCode by remember { mutableStateOf("1F7") }
        var colourName by remember { mutableStateOf("Classic Silver Metallic") }
        var finish by remember { mutableStateOf("Metallic") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewJobDialog = false },
            title = { Text("New Paint Job") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vehicle,
                        onValueChange = { vehicle = it },
                        label = { Text("Plate & Vehicle (e.g. KDR 214M • Probox)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = oemCode,
                        onValueChange = { oemCode = it },
                        label = { Text("OEM Paint Code") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = colourName,
                        onValueChange = { colourName = it },
                        label = { Text("Colour Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = finish,
                        onValueChange = { finish = it },
                        label = { Text("Finish (Solid / Metallic / Pearl)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Panels & Job Notes") }
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = vehicle.isNotBlank(),
                    onClick = {
                        viewModel.saveJob(
                            PaintJobEntity(
                                vehicleLabel = vehicle.trim(),
                                oemCode = oemCode.trim(),
                                colourName = colourName.trim(),
                                finish = finish.trim().ifBlank { "Solid" },
                                status = "Quoted",
                                notes = notes.trim()
                            )
                        )
                        showNewJobDialog = false
                    }
                ) { Text("Create Job") }
            },
            dismissButton = {
                TextButton(onClick = { showNewJobDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNewFormulaDialog) {
        var title by remember { mutableStateOf("") }
        var oemRef by remember { mutableStateOf("") }
        var baseSys by remember { mutableStateOf("2K Basecoat") }
        var ratio by remember { mutableStateOf("100:50") }
        var tonerNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewFormulaDialog = false },
            title = { Text("Add Technician Formula") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Recipe Title") }, singleLine = true)
                    OutlinedTextField(value = oemRef, onValueChange = { oemRef = it }, label = { Text("OEM Code Ref") }, singleLine = true)
                    OutlinedTextField(value = baseSys, onValueChange = { baseSys = it }, label = { Text("Paint System") }, singleLine = true)
                    OutlinedTextField(value = ratio, onValueChange = { ratio = it }, label = { Text("Mixing Ratio") }, singleLine = true)
                    OutlinedTextField(value = tonerNotes, onValueChange = { tonerNotes = it }, label = { Text("Toner Weights / Shop Adjustments") })
                }
            },
            confirmButton = {
                Button(
                    enabled = title.isNotBlank(),
                    onClick = {
                        viewModel.saveFormula(
                            TechnicianFormulaEntity(
                                title = title.trim(),
                                oemCodeRef = oemRef.trim().ifBlank { "CUSTOM" },
                                baseSystem = baseSys.trim(),
                                tonerNotes = tonerNotes.trim(),
                                mixingRatio = ratio.trim()
                            )
                        )
                        showNewFormulaDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFormulaDialog = false }) { Text("Cancel") }
            }
        )
    }

    sprayOutJobTarget?.let { job ->
        var panelName by remember { mutableStateOf("Test Card #1") }
        var lighting by remember { mutableStateOf("Sunlight") }
        var outcome by remember { mutableStateOf("ACCEPTED") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { sprayOutJobTarget = null },
            title = { Text("Log Spray-Out Observation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = panelName, onValueChange = { panelName = it }, label = { Text("Card / Panel") }, singleLine = true)
                    Text("Lighting", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Sunlight", "Shade", "Booth LED").forEach { light ->
                            FilterChip(selected = lighting == light, onClick = { lighting = light }, label = { Text(light) })
                        }
                    }
                    Text("Outcome", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ACCEPTED", "ADJUSTED", "REJECTED").forEach { res ->
                            FilterChip(selected = outcome == res, onClick = { outcome = res }, label = { Text(res) })
                        }
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Observation / Adjustment Notes") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveSprayOut(
                        SprayOutEntity(
                            jobId = job.id,
                            jobLabel = "${job.vehicleLabel} (${job.oemCode})",
                            panelName = panelName.trim(),
                            lightingCondition = lighting,
                            outcome = outcome,
                            notes = notes.trim()
                        )
                    )
                    sprayOutJobTarget = null
                }) { Text("Save Log") }
            },
            dismissButton = {
                TextButton(onClick = { sprayOutJobTarget = null }) { Text("Cancel") }
            }
        )
    }

    usageJobTarget?.let { job ->
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<StockItemEntity?>(stockItems.firstOrNull()) }
        var qtyText by remember { mutableStateOf("0.5") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { usageJobTarget = null },
            title = { Text("Log Material Usage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = selectedItem?.let { "${it.name} (${it.quantity} ${it.unit})" } ?: "No stock items",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Stock Item") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            stockItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} • ${item.quantity} ${item.unit} (${formatKsh(item.unitCostKsh)}/${item.unit})") },
                                    onClick = {
                                        selectedItem = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantity Used (${selectedItem?.unit ?: "unit"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Usage Note") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedItem != null && (qtyText.toDoubleOrNull() ?: 0.0) > 0.0,
                    onClick = {
                        val item = selectedItem!!
                        val qty = qtyText.toDoubleOrNull() ?: 0.0
                        viewModel.logStockUsage(item.id, job.id, job.vehicleLabel, qty, notes.trim())
                        usageJobTarget = null
                    }
                ) { Text("Deduct & Log") }
            },
            dismissButton = {
                TextButton(onClick = { usageJobTarget = null }) { Text("Cancel") }
            }
        )
    }
}
