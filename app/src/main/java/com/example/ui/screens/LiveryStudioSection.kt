package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ApproximationDisclaimerBanner
import com.example.ui.components.LiveryConfig
import com.example.ui.components.MotifBadge
import com.example.ui.components.StripeStyle
import com.example.ui.components.TemplateViewAngle
import com.example.ui.components.VehicleLiveryCanvas
import com.example.ui.components.VehiclePanel
import com.example.ui.components.VehicleTemplate
import com.example.ui.components.formatShortDate
import com.example.ui.components.parseHexColor
import com.example.ui.components.renderPresentationCardBitmap
import com.example.ui.theme.NganyaPalette
import com.example.ui.viewmodel.MotoopaintViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveryStudioSection(
    viewModel: MotoopaintViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val projects by viewModel.studioProjects.collectAsStateWithLifecycle()
    val oemPaints by viewModel.oemPaints.collectAsStateWithLifecycle()

    var template by remember { mutableStateOf(VehicleTemplate.HIACE) }
    var viewAngle by remember { mutableStateOf(TemplateViewAngle.SIDE) }
    var activePanel by remember { mutableStateOf(VehiclePanel.BODY) }
    var config by remember { mutableStateOf(LiveryConfig()) }
    var showOriginal by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val nganyaSwatches = remember {
        listOf(
            "Cyan" to "#00D2FF",
            "Magenta" to "#FF1E7A",
            "Gold" to "#FFC300",
            "Lime" to "#A6FF00",
            "Coral" to "#FF5A1F",
            "Violet" to "#7C3AED",
            "Asphalt" to "#121417",
            "Chrome" to "#F8FAFC"
        )
    }

    val liveryProjects = remember(projects) { projects.filter { it.mode == "LIVERY" } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Nganya Culture Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nganya_banner),
                    contentDescription = "Nganya and fleet custom livery header",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xE8101317), Color(0x88101317), Color.Transparent)
                            )
                        )
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Custom Livery & Nganya Studio",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Panel-by-panel colours, stripes, motifs & customer presentation exports",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD5DCE3)
                    )
                }
            }
        }

        // Template & Angle Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VehicleTemplate.entries) { item ->
                    FilterChip(
                        selected = template == item,
                        onClick = { template = item },
                        label = { Text(item.label) }
                    )
                }
            }
        }

        // Interactive Vehicle Canvas
        item {
            VehicleLiveryCanvas(
                template = template,
                viewAngle = viewAngle,
                config = config,
                showOriginalPreview = showOriginal
            )
        }

        // Angle + Before/After Toggle
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemplateViewAngle.entries.forEach { angle ->
                        FilterChip(
                            selected = viewAngle == angle,
                            onClick = { viewAngle = angle },
                            label = { Text(angle.label) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Compare, contentDescription = "Before/After", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Before", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(checked = showOriginal, onCheckedChange = { showOriginal = it })
                }
            }
        }

        // Panel-by-Panel Recolouring
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text("1. Tap Panel to Recolour", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        VehiclePanel.entries.forEach { panel ->
                            FilterChip(
                                selected = activePanel == panel,
                                onClick = { activePanel = panel },
                                label = { Text(panel.label) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(config.panelHex(panel)))
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }
                            )
                        }
                    }

                    Text("Nganya Vivid Palette", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(nganyaSwatches) { (name, hex) ->
                            val selected = config.panelHex(activePanel).equals(hex, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    config = config.withPanelHex(activePanel, hex)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(hex))
                                        .border(
                                            if (selected) 3.dp else 1.dp,
                                            if (selected) NganyaPalette.ElectricCyan else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                )
                                Text(name, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text("OEM & Fleet Colours", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(oemPaints.take(8), key = { it.id }) { paint ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    config = config.withPanelHex(activePanel, paint.hexColor)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(paint.hexColor))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Text(paint.oemCode, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Graphic Stripes, Motif & Route Typography
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text("2. Stripes, Motifs & Typography", style = MaterialTheme.typography.titleSmall)

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StripeStyle.entries.forEach { st ->
                            FilterChip(
                                selected = config.stripeStyle == st,
                                onClick = { config = config.copy(stripeStyle = st) },
                                label = { Text(st.label) }
                            )
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MotifBadge.entries.forEach { m ->
                            FilterChip(
                                selected = config.motif == m,
                                onClick = { config = config.copy(motif = m) },
                                label = { Text(m.label) }
                            )
                        }
                    }

                    Text("Graphic Accent Colour", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(nganyaSwatches) { (_, hex) ->
                            val selected = config.accentHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(
                                        if (selected) 3.dp else 1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                                    .clickable { config = config.copy(accentHex = hex) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = config.routeText,
                        onValueChange = { config = config.copy(routeText = it) },
                        label = { Text("Route / Fleet Typography Banner") },
                        placeholder = { Text("e.g. ROUTE 125 • NGONG") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save & Customer Presentation Export
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
                    Text("Save Livery")
                }
                Button(
                    onClick = {
                        scope.launch {
                            val bmp = renderPresentationCardBitmap(
                                title = config.routeText.ifBlank { template.label },
                                template = template,
                                cfg = config
                            )
                            val uri = viewModel.createShareExportUri(bmp, "nganya_presentation")
                            bmp.recycle()
                            if (uri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Customer Concept"))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Card")
                }
            }
        }

        if (liveryProjects.isNotEmpty()) {
            item {
                Text("Saved Livery Concepts", style = MaterialTheme.typography.titleMedium)
            }
            items(liveryProjects, key = { it.id }) { proj ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            template = VehicleTemplate.entries.find { it.id == proj.templateId } ?: VehicleTemplate.HIACE
                            viewAngle = TemplateViewAngle.entries.find { it.id == proj.viewAngle } ?: TemplateViewAngle.SIDE
                            config = LiveryConfig.fromJson(proj.liveryJson)
                            viewModel.showMessage("Loaded '${proj.title}'")
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(proj.selectedHex))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(proj.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${proj.vehicleLabel} • ${formatShortDate(proj.updatedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deleteStudioProject(proj) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete livery project")
                        }
                    }
                }
            }
        }

        item { ApproximationDisclaimerBanner() }
    }

    if (showSaveDialog) {
        var title by remember { mutableStateOf(config.routeText.ifBlank { "${template.label} Concept" }) }
        var vehicleLabel by remember { mutableStateOf(template.label) }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Livery Concept") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Concept Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = vehicleLabel,
                        onValueChange = { vehicleLabel = it },
                        label = { Text("Vehicle / Fleet Label") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveLiveryProject(
                        title = title,
                        vehicleLabel = vehicleLabel,
                        templateId = template.id,
                        viewAngle = viewAngle.id,
                        liveryJson = config.toJson(),
                        accentHex = config.accentHex
                    )
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}
