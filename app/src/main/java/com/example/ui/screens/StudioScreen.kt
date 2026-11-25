package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MotoopaintViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: MotoopaintViewModel,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onInspectOemCode: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val subTabs = listOf("Photo Painter", "Bay Scan", "Car Photos", "Nganya Livery")

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedSubTab.coerceIn(0, subTabs.lastIndex),
            edgePadding = 12.dp
        ) {
            subTabs.forEachIndexed { idx, label ->
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

        AnimatedContent(targetState = selectedSubTab, label = "studio_tab_transition") { tab ->
            when (tab) {
                0 -> PhotoPainterSection(
                    viewModel = viewModel,
                    onGoToLibrary = { onSelectSubTab(2) }
                )
                1 -> CameraXScannerSection(
                    viewModel = viewModel,
                    onCapturedToPainter = { onSelectSubTab(0) },
                    onOpenSampleInColours = onInspectOemCode
                )
                2 -> CarPhotoLibrarySection(
                    viewModel = viewModel,
                    onOpenInPainter = { path ->
                        viewModel.openPhotoInStudio(path)
                        onSelectSubTab(0)
                    }
                )
                else -> LiveryStudioSection(viewModel = viewModel)
            }
        }
    }
}
