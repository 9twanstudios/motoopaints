package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun parseHexColor(hex: String, fallback: Color = Color(0xFFB8BDC2)): Color {
    return runCatching {
        val cleaned = hex.trim().removePrefix("#")
        val full = when (cleaned.length) {
            3 -> cleaned.map { "$it$it" }.joinToString("")
            6 -> cleaned
            8 -> cleaned
            else -> return fallback
        }
        val argb = if (full.length == 6) "FF$full" else full
        Color(argb.toLong(16).toInt())
    }.getOrDefault(fallback)
}

fun formatKsh(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "KSh ${formatter.format(amount.roundToInt())}"
}

fun formatShortDate(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

/**
 * Composites a target paint colour and finish sheen over the original car photo
 * ONLY where the user painted the mask bitmap, preserving highlights & shadows.
 */
fun renderRecolouredPhoto(
    original: Bitmap,
    mask: Bitmap,
    targetColor: Int,
    finish: String
): Bitmap {
    val width = original.width
    val height = original.height
    val output = original.copy(Bitmap.Config.ARGB_8888, true)

    val tintedLayer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val layerCanvas = Canvas(tintedLayer)

    // Draw grayscale/luminance version of original so body creases & reflections remain
    val desatMatrix = ColorMatrix().apply { setSaturation(0.15f) }
    val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(desatMatrix)
    }
    layerCanvas.drawBitmap(original, 0f, 0f, basePaint)

    // Apply finish-specific colour & gloss blending
    val r = android.graphics.Color.red(targetColor)
    val g = android.graphics.Color.green(targetColor)
    val b = android.graphics.Color.blue(targetColor)

    val blendAlpha = when (finish) {
        "Matte" -> 215
        "Satin" -> 200
        "Solid" -> 195
        "Pearl" -> 175
        "Candy" -> 185
        else -> 185 // Metallic
    }

    val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(blendAlpha, r, g, b)
        xfermode = PorterDuffXfermode(
            when (finish) {
                "Matte" -> PorterDuff.Mode.SRC_ATOP
                "Pearl" -> PorterDuff.Mode.SCREEN
                "Candy" -> PorterDuff.Mode.OVERLAY
                else -> PorterDuff.Mode.MULTIPLY
            }
        )
    }
    layerCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)

    // Subtle secondary coat for bright or dark paint richness
    val coatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(
            when (finish) {
                "Matte" -> 85
                "Pearl" -> 60
                else -> 48
            },
            r, g, b
        )
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    }
    layerCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coatPaint)

    // Clip tintedLayer by painted alpha mask
    val maskedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    val scaledMask = if (mask.width == width && mask.height == height) {
        mask
    } else {
        Bitmap.createScaledBitmap(mask, width, height, true)
    }
    layerCanvas.drawBitmap(scaledMask, 0f, 0f, maskedPaint)
    if (scaledMask !== mask) scaledMask.recycle()

    // Draw masked recolour back onto output
    val outCanvas = Canvas(output)
    outCanvas.drawBitmap(tintedLayer, 0f, 0f, null)
    tintedLayer.recycle()
    return output
}

fun colorRgbDistance(colorA: Int, colorB: Int): Int {
    val dr = android.graphics.Color.red(colorA) - android.graphics.Color.red(colorB)
    val dg = android.graphics.Color.green(colorA) - android.graphics.Color.green(colorB)
    val db = android.graphics.Color.blue(colorA) - android.graphics.Color.blue(colorB)
    return dr * dr + dg * dg + db * db
}

fun toHexString(argb: Int): String {
    return String.format("#%06X", 0xFFFFFF and argb)
}

