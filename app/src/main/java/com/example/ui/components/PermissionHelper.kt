package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Helper utilities built on Accompanist Permissions for smooth runtime camera
 * and legacy storage permission handling across Motoopaint.
 */
object WorkshopPermissionHelper {

    fun requiredWorkshopPermissions(): List<String> = buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCameraScanPermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionState {
    return rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = onPermissionResult
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberWorkshopPermissionsState(
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState {
    return rememberMultiplePermissionsState(
        permissions = WorkshopPermissionHelper.requiredWorkshopPermissions(),
        onPermissionsResult = onPermissionsResult
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionPromptCard(
    permissionState: PermissionState,
    onFallbackToGallery: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val showRationale = permissionState.status.shouldShowRationale

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Camera permission required",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (showRationale) "Camera Access Needed for Bay Scanning" else "Enable Camera for Vehicle Scanning",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (showRationale) {
                    "Motoopaint uses your camera to photograph customer vehicles and sample panel colours offline. No photos ever leave your device."
                } else {
                    "Grant camera permission to launch the live CameraX bay viewfinder and capture vehicle photos."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { permissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("accompanist_request_camera_button")
                ) {
                    Text(if (showRationale) "Grant Access" else "Allow Camera")
                }
                OutlinedButton(
                    onClick = { WorkshopPermissionHelper.openAppSettings(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settings")
                }
            }
            if (onFallbackToGallery != null && !permissionState.status.isGranted) {
                OutlinedButton(
                    onClick = onFallbackToGallery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import From Photo Picker Instead")
                }
            }
        }
    }
}
