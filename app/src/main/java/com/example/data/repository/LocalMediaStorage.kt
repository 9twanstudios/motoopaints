package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LocalMediaStorage(private val context: Context) {

    private val carPhotosDir: File
        get() = File(context.filesDir, "car_photos").apply { if (!exists()) mkdirs() }

    private val masksDir: File
        get() = File(context.filesDir, "masks").apply { if (!exists()) mkdirs() }

    private val exportsDir: File
        get() = File(context.filesDir, "livery_exports").apply { if (!exists()) mkdirs() }

    private val cameraCacheDir: File
        get() = File(context.cacheDir, "camera_captures").apply { if (!exists()) mkdirs() }

    fun createCameraCaptureFileAndUri(): Pair<File, Uri> {
        val file = File(cameraCacheDir, "car_cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    suspend fun persistCameraFile(tempFile: File): String? = withContext(Dispatchers.IO) {
        if (!tempFile.exists() || tempFile.length() == 0L) return@withContext null
        val bitmap = decodeSampledBitmapFromFile(tempFile.absolutePath, 1440) ?: return@withContext null
        val outFile = File(carPhotosDir, "car_${UUID.randomUUID()}.jpg")
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)
        }
        bitmap.recycle()
        tempFile.delete()
        outFile.absolutePath
    }

    suspend fun importPhotoFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext null
        val bitmap = decodeSampledBitmapFromBytes(bytes, 1440) ?: return@withContext null
        val outFile = File(carPhotosDir, "car_${UUID.randomUUID()}.jpg")
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)
        }
        bitmap.recycle()
        outFile.absolutePath
    }

    suspend fun saveMaskBitmap(bitmap: Bitmap, existingPath: String? = null): String = withContext(Dispatchers.IO) {
        val outFile = if (!existingPath.isNullOrBlank()) {
            File(existingPath)
        } else {
            File(masksDir, "mask_${UUID.randomUUID()}.png")
        }
        outFile.parentFile?.mkdirs()
        FileOutputStream(outFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        outFile.absolutePath
    }

    suspend fun loadBitmap(path: String, maxDim: Int = 1280): Bitmap? = withContext(Dispatchers.IO) {
        if (path.isBlank() || !File(path).exists()) return@withContext null
        decodeSampledBitmapFromFile(path, maxDim)
    }

    suspend fun saveShareableExport(bitmap: Bitmap, namePrefix: String): Uri? = withContext(Dispatchers.IO) {
        val file = File(exportsDir, "${namePrefix}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun deleteFileIfExists(path: String) {
        if (path.isNotBlank()) {
            runCatching { File(path).delete() }
        }
    }

    private fun decodeSampledBitmapFromFile(path: String, maxDim: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, opts)
    }

    private fun decodeSampledBitmapFromBytes(bytes: ByteArray, maxDim: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val maxSide = maxOf(width, height)
        while (maxSide / inSampleSize > maxDim) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
