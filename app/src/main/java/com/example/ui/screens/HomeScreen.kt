package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.OemPaintEntity
import com.example.data.local.PaintJobEntity
import com.example.ui.components.CodePill
import com.example.ui.components.OfflineStatusPill
import com.example.ui.components.SwatchCircle
import com.example.ui.components.ThemeSwitchPill
import com.example.ui.components.formatKsh
import com.example.ui.components.formatShortDate
import com.example.ui.viewmodel.MotoopaintViewModel

@Composable
fun HomeScreen(
    viewModel: MotoopaintViewModel,
    onNavigateColours: (String) -> Unit,
    onNavigateStudio: (Int) -> Unit, // 0 = Photo Painter, 1 = Bay Scan, 2 = Photo Library, 3 = Livery
    onNavigateJobs: () -> Unit,
    onNavigateMore: (Int) -> Unit // 0 = Stock, 1 = Analytics, 2 = Profile & Settings Grid
) {
    val haptics = LocalHapticFeedback.current
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val workshopName by viewModel.workshopName.collectAsStateWithLifecycle()
    val oemPaints by viewModel.oemPaints.collectAsStateWithLifecycle()
    val standardColours by viewModel.standardColours.collectAsStateWithLifecycle()
    val jobs by viewModel.paintJobs.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()

    var quickQuery by remember { mutableStateOf("") }
    val favourites = remember(oemPaints) { oemPaints.filter { it.isFavourite }.take(6) }
    val recentJobs = remember(jobs) { jobs.take(3) }

    val liveMatchesCount = remember(quickQuery, oemPaints, standardColours) {
        val q = quickQuery.trim().lowercase()
        if (q.isEmpty()) 0 else {
            oemPaints.count {
                it.oemCode.lowercase().contains(q) ||
                    it.factoryName.lowercase().contains(q) ||
                    it.make.lowercase().contains(q)
            } + standardColours.count {
                it.displayName.lowercase().contains(q) || it.aliases.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Bar with Workshop Profile Shortcut + Live Theme Switcher
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Motoopaint",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    OfflineStatusPill()
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeSwitchPill(
                        themeMode = themeMode,
                        onCycleTheme = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.cycleThemeMode()
                        }
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateMore(2) }
                            .testTag("home_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Open Profile & Settings Grid",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // Workshop Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_workshop),
                    contentDescription = "Workshop paint booth hero banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xEE0F1317),
                                    Color(0xB80F1317),
                                    Color(0x400F1317)
                                )
                            )
                        )
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                        modifier = Modifier.clickable { onNavigateMore(2) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = workshopName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Kenya Paint & Livery Studio",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${oemPaints.size} OEM refs • ${analytics.totalJobs} jobs • ${analytics.carPhotoCount} bay photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFDDE3EA)
                        )
                    }
                }
            }
        }

        // Clear 3-Step Bay Flow Stepper
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    FlowStepPill(
                        step = "1",
                        label = "Find Code",
                        onClick = { onNavigateColours("") },
                        modifier = Modifier.weight(1f)
                    )
                    FlowStepPill(
                        step = "2",
                        label = "Bay Scan",
                        onClick = { onNavigateStudio(1) },
                        modifier = Modifier.weight(1f)
                    )
                    FlowStepPill(
                        step = "3",
                        label = "Log Job",
                        onClick = onNavigateJobs,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Lookup Search Bar with Instant Match Feedback
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = quickQuery,
                    onValueChange = { quickQuery = it },
                    placeholder = {
                        Text(
                            text = "Search make, model, OEM code (1F7) or colour...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Quick lookup")
                    },
                    trailingIcon = {
                        if (quickQuery.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateColours(quickQuery)
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Go to search results",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onNavigateColours(quickQuery) }),
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_quick_search")
                )

                AnimatedVisibility(visible = quickQuery.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateColours(quickQuery) }
                    ) {
                        Text(
                            text = if (liveMatchesCount > 0)
                                "$liveMatchesCount offline catalogue matches for \"${quickQuery.trim()}\" — tap to open"
                            else "No exact match yet — tap to browse full colour chart & aliases",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2x2 Tactile Shortcut Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeShortcutTile(
                        title = "Colour Library",
                        subtitle = "OEM codes & charts",
                        icon = Icons.Filled.ColorLens,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateColours("") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shortcut_colours")
                    )
                    HomeShortcutTile(
                        title = "Paint Studio",
                        subtitle = "Masking & Nganya",
                        icon = Icons.Filled.Palette,
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigateStudio(0) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shortcut_studio")
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeShortcutTile(
                        title = "Bay Scanner",
                        subtitle = "CameraX & Car Photos",
                        icon = Icons.Filled.CameraAlt,
                        accent = MaterialTheme.colorScheme.tertiary,
                        onClick = { onNavigateStudio(1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shortcut_photo_library")
                    )
                    HomeShortcutTile(
                        title = "Workshop Stock",
                        subtitle = "${analytics.lowStockItems.size} low stock alerts",
                        icon = Icons.Filled.Inventory2,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateMore(0) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shortcut_stock")
                    )
                }
            }
        }

        // Saved / Frequently Used Swatches
        if (favourites.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pinned OEM Swatches",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "View all",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateColours("") }
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favourites, key = { it.id }) { paint ->
                            PinnedSwatchCard(
                                paint = paint,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.sendColourToStudio(paint.hexColor, paint.finish)
                                    onNavigateStudio(0)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Recent Jobs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Recent Paint Jobs",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "All jobs",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onNavigateJobs)
                    )
                }
                recentJobs.forEach { job ->
                    RecentJobRow(job = job, onClick = onNavigateJobs)
                }
            }
        }

        // 3-Way Match Clarity Card & Local Spend Summary
        item {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Three step verification workflow",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3-Step Paint Verification Discipline",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = "1. Digital Preview (Studio)  •  2. OEM Identification (Colours)  •  3. Physical Spray-Out Test (Jobs). Phone screens approximate colour; always confirm with a test card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Logged Stock Spend",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatKsh(analytics.totalMaterialCostKsh),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowStepPill(
    step: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeShortcutTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PinnedSwatchCard(
    paint: OemPaintEntity,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier
            .width(154.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SwatchCircle(hexColor = paint.hexColor, finish = paint.finish, size = 36.dp)
                CodePill(code = paint.oemCode)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = paint.factoryName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${paint.make} • ${paint.finish}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentJobRow(
    job: PaintJobEntity,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = "Vehicle job",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = job.vehicleLabel,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${job.colourName} (${job.oemCode}) • ${formatShortDate(job.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = job.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
