package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import org.json.JSONObject

enum class VehicleTemplate(val id: String, val label: String) {
    PROBOX("PROBOX", "Probox / Wagon"),
    SALOON("SALOON", "Saloon / Axio"),
    PICKUP("PICKUP", "Isuzu Pickup"),
    HIACE("HIACE", "Hiace / Matatu")
}

enum class TemplateViewAngle(val id: String, val label: String) {
    SIDE("SIDE", "Side View"),
    FRONT("FRONT", "Front View")
}

enum class VehiclePanel(val label: String) {
    BODY("Body Shell"),
    ROOF("Roof"),
    HOOD("Bonnet / Front"),
    DOORS("Doors / Side"),
    BUMPERS("Bumpers"),
    RIMS("Rims & Trim")
}

enum class StripeStyle(val id: String, val label: String) {
    NONE("NONE", "Clean OEM"),
    LIGHTNING("LIGHTNING", "Nganya Bolt"),
    WAVE("WAVE", "Side Wave"),
    CHEVRON("CHEVRON", "PSV Band")
}

enum class MotifBadge(val id: String, val label: String) {
    NONE("NONE", "No Motif"),
    CROWN("CROWN", "Crown"),
    STAR("STAR", "Star"),
    BOLT("BOLT", "Lightning")
}

data class LiveryConfig(
    val bodyHex: String = "#F4F5F0",
    val roofHex: String = "#14171A",
    val hoodHex: String = "#D95D1E",
    val doorsHex: String = "#F4F5F0",
    val bumpersHex: String = "#14171A",
    val rimsHex: String = "#FFC300",
    val accentHex: String = "#00D2FF",
    val stripeStyle: StripeStyle = StripeStyle.LIGHTNING,
    val motif: MotifBadge = MotifBadge.CROWN,
    val routeText: String = "ROUTE 125 • RONGAI"
) {
    fun toJson(): String = JSONObject().apply {
        put("body", bodyHex)
        put("roof", roofHex)
        put("hood", hoodHex)
        put("doors", doorsHex)
        put("bumpers", bumpersHex)
        put("rims", rimsHex)
        put("accent", accentHex)
        put("stripeStyle", stripeStyle.id)
        put("motif", motif.id)
        put("routeText", routeText)
    }.toString()

    fun panelHex(panel: VehiclePanel): String = when (panel) {
        VehiclePanel.BODY -> bodyHex
        VehiclePanel.ROOF -> roofHex
        VehiclePanel.HOOD -> hoodHex
        VehiclePanel.DOORS -> doorsHex
        VehiclePanel.BUMPERS -> bumpersHex
        VehiclePanel.RIMS -> rimsHex
    }

    fun withPanelHex(panel: VehiclePanel, hex: String): LiveryConfig = when (panel) {
        VehiclePanel.BODY -> copy(bodyHex = hex)
        VehiclePanel.ROOF -> copy(roofHex = hex)
        VehiclePanel.HOOD -> copy(hoodHex = hex)
        VehiclePanel.DOORS -> copy(doorsHex = hex)
        VehiclePanel.BUMPERS -> copy(bumpersHex = hex)
        VehiclePanel.RIMS -> copy(rimsHex = hex)
    }

    companion object {
        fun fromJson(json: String): LiveryConfig = runCatching {
            val obj = JSONObject(json)
            LiveryConfig(
                bodyHex = obj.optString("body", "#F4F5F0"),
                roofHex = obj.optString("roof", "#14171A"),
                hoodHex = obj.optString("hood", "#D95D1E"),
                doorsHex = obj.optString("doors", "#F4F5F0"),
                bumpersHex = obj.optString("bumpers", "#14171A"),
                rimsHex = obj.optString("rims", "#FFC300"),
                accentHex = obj.optString("accent", "#00D2FF"),
                stripeStyle = StripeStyle.entries.find { it.id == obj.optString("stripeStyle") }
                    ?: StripeStyle.LIGHTNING,
                motif = MotifBadge.entries.find { it.id == obj.optString("motif") }
                    ?: MotifBadge.CROWN,
                routeText = obj.optString("routeText", "")
            )
        }.getOrDefault(LiveryConfig())
    }
}

@Composable
fun VehicleLiveryCanvas(
    template: VehicleTemplate,
    viewAngle: TemplateViewAngle,
    config: LiveryConfig,
    showOriginalPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    val effective = if (showOriginalPreview) {
        LiveryConfig(
            bodyHex = "#F4F5F0",
            roofHex = "#F4F5F0",
            hoodHex = "#F4F5F0",
            doorsHex = "#F4F5F0",
            bumpersHex = "#2A2E33",
            rimsHex = "#B8BDC2",
            accentHex = "#F4F5F0",
            stripeStyle = StripeStyle.NONE,
            motif = MotifBadge.NONE,
            routeText = "ORIGINAL FACTORY BASE"
        )
    } else config

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9.5f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A2129), Color(0xFF0E1217))
                )
            )
            .border(1.dp, Color(0xFF34414E), RoundedCornerShape(18.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStudioFloor()
            if (viewAngle == TemplateViewAngle.SIDE) {
                drawSideTemplate(template, effective)
            } else {
                drawFrontTemplate(template, effective)
            }
        }
    }
}

private fun DrawScope.drawStudioFloor() {
    val w = size.width
    val h = size.height
    // Overhead booth light reflection
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x28FFFFFF), Color.Transparent),
            startY = 0f,
            endY = h * 0.42f
        )
    )
    // Asphalt workshop bay floor
    drawRect(
        color = Color(0xFF111418),
        topLeft = Offset(0f, h * 0.78f),
        size = Size(w, h * 0.22f)
    )
    drawLine(
        color = Color(0x40FFFFFF),
        start = Offset(w * 0.05f, h * 0.78f),
        end = Offset(w * 0.95f, h * 0.78f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawSideTemplate(
    template: VehicleTemplate,
    cfg: LiveryConfig
) {
    val w = size.width
    val h = size.height
    val bodyColor = parseHexColor(cfg.bodyHex)
    val roofColor = parseHexColor(cfg.roofHex)
    val hoodColor = parseHexColor(cfg.hoodHex)
    val doorsColor = parseHexColor(cfg.doorsHex)
    val bumperColor = parseHexColor(cfg.bumpersHex)
    val rimColor = parseHexColor(cfg.rimsHex)
    val accentColor = parseHexColor(cfg.accentHex)

    val groundY = h * 0.79f

    when (template) {
        VehicleTemplate.HIACE -> {
            // Matatu / Minibus boxy silhouette
            val bodyPath = Path().apply {
                moveTo(w * 0.10f, groundY - h * 0.08f)
                lineTo(w * 0.10f, h * 0.26f)
                quadraticTo(w * 0.11f, h * 0.20f, w * 0.18f, h * 0.20f)
                lineTo(w * 0.78f, h * 0.20f)
                lineTo(w * 0.87f, h * 0.43f)
                lineTo(w * 0.90f, h * 0.56f)
                lineTo(w * 0.90f, groundY - h * 0.08f)
                close()
            }
            drawPath(bodyPath, bodyColor)

            // Roof strip
            drawRoundRect(
                color = roofColor,
                topLeft = Offset(w * 0.12f, h * 0.19f),
                size = Size(w * 0.66f, h * 0.07f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Front nose/hood panel
            val nosePath = Path().apply {
                moveTo(w * 0.74f, h * 0.43f)
                lineTo(w * 0.87f, h * 0.43f)
                lineTo(w * 0.90f, h * 0.58f)
                lineTo(w * 0.90f, groundY - h * 0.10f)
                lineTo(w * 0.74f, groundY - h * 0.10f)
                close()
            }
            drawPath(nosePath, hoodColor)

            // Sliding door & side body
            drawRoundRect(
                color = doorsColor,
                topLeft = Offset(w * 0.26f, h * 0.43f),
                size = Size(w * 0.46f, groundY - h * 0.54f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Graphics layer
            drawNganyaStripes(cfg.stripeStyle, accentColor, w * 0.11f, h * 0.45f, w * 0.78f, h * 0.22f)

            // Windows
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.15f, h * 0.27f), Size(w * 0.18f, h * 0.14f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.35f, h * 0.27f), Size(w * 0.18f, h * 0.14f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.55f, h * 0.27f), Size(w * 0.16f, h * 0.14f), CornerRadius(8f, 8f))
            val windShield = Path().apply {
                moveTo(w * 0.73f, h * 0.24f)
                lineTo(w * 0.78f, h * 0.24f)
                lineTo(w * 0.86f, h * 0.42f)
                lineTo(w * 0.73f, h * 0.42f)
                close()
            }
            drawPath(windShield, Color(0xFF233242))

            // Bumpers
            drawRoundRect(bumperColor, Offset(w * 0.08f, groundY - h * 0.12f), Size(w * 0.12f, h * 0.07f), CornerRadius(8f, 8f))
            drawRoundRect(bumperColor, Offset(w * 0.80f, groundY - h * 0.12f), Size(w * 0.12f, h * 0.07f), CornerRadius(8f, 8f))

            // Wheels
            drawWheel(w * 0.25f, groundY - h * 0.06f, h * 0.11f, rimColor)
            drawWheel(w * 0.76f, groundY - h * 0.06f, h * 0.11f, rimColor)

            // Motif & Route text
            drawMotif(cfg.motif, accentColor, Offset(w * 0.19f, h * 0.55f), h * 0.065f)
            drawRouteBanner(cfg.routeText, Offset(w * 0.50f, h * 0.62f), accentColor)
        }

        VehicleTemplate.PICKUP -> {
            // Double-cab Isuzu D-Max silhouette
            val bedPath = Path().apply {
                moveTo(w * 0.10f, h * 0.45f)
                lineTo(w * 0.42f, h * 0.45f)
                lineTo(w * 0.42f, groundY - h * 0.10f)
                lineTo(w * 0.10f, groundY - h * 0.10f)
                close()
            }
            drawPath(bedPath, bodyColor)

            // Cab
            val cabPath = Path().apply {
                moveTo(w * 0.42f, h * 0.26f)
                lineTo(w * 0.63f, h * 0.26f)
                lineTo(w * 0.73f, h * 0.44f)
                lineTo(w * 0.73f, groundY - h * 0.10f)
                lineTo(w * 0.42f, groundY - h * 0.10f)
                close()
            }
            drawPath(cabPath, doorsColor)

            // Roof
            drawRoundRect(roofColor, Offset(w * 0.43f, h * 0.25f), Size(w * 0.20f, h * 0.05f), CornerRadius(8f, 8f))

            // Bonnet
            val hoodPath = Path().apply {
                moveTo(w * 0.72f, h * 0.44f)
                lineTo(w * 0.89f, h * 0.48f)
                lineTo(w * 0.89f, groundY - h * 0.10f)
                lineTo(w * 0.72f, groundY - h * 0.10f)
                close()
            }
            drawPath(hoodPath, hoodColor)

            drawNganyaStripes(cfg.stripeStyle, accentColor, w * 0.12f, h * 0.48f, w * 0.74f, h * 0.18f)

            // Cab windows
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.45f, h * 0.30f), Size(w * 0.11f, h * 0.13f), CornerRadius(6f, 6f))
            val frontWin = Path().apply {
                moveTo(w * 0.58f, h * 0.30f)
                lineTo(w * 0.63f, h * 0.30f)
                lineTo(w * 0.70f, h * 0.43f)
                lineTo(w * 0.58f, h * 0.43f)
                close()
            }
            drawPath(frontWin, Color(0xFF233242))

            // Bumpers
            drawRoundRect(bumperColor, Offset(w * 0.08f, groundY - h * 0.13f), Size(w * 0.10f, h * 0.07f), CornerRadius(6f, 6f))
            drawRoundRect(bumperColor, Offset(w * 0.81f, groundY - h * 0.13f), Size(w * 0.11f, h * 0.07f), CornerRadius(6f, 6f))

            drawWheel(w * 0.24f, groundY - h * 0.07f, h * 0.115f, rimColor)
            drawWheel(w * 0.77f, groundY - h * 0.07f, h * 0.115f, rimColor)

            drawMotif(cfg.motif, accentColor, Offset(w * 0.25f, h * 0.54f), h * 0.055f)
            drawRouteBanner(cfg.routeText, Offset(w * 0.56f, h * 0.59f), accentColor)
        }

        VehicleTemplate.SALOON -> {
            // Saloon silhouette
            val bodyPath = Path().apply {
                moveTo(w * 0.10f, groundY - h * 0.09f)
                lineTo(w * 0.11f, h * 0.46f)
                lineTo(w * 0.25f, h * 0.44f)
                lineTo(w * 0.37f, h * 0.27f)
                lineTo(w * 0.62f, h * 0.27f)
                lineTo(w * 0.74f, h * 0.45f)
                lineTo(w * 0.89f, h * 0.49f)
                lineTo(w * 0.90f, groundY - h * 0.09f)
                close()
            }
            drawPath(bodyPath, bodyColor)

            // Roof
            drawRoundRect(roofColor, Offset(w * 0.37f, h * 0.26f), Size(w * 0.25f, h * 0.045f), CornerRadius(10f, 10f))

            // Doors
            drawRoundRect(doorsColor, Offset(w * 0.31f, h * 0.45f), Size(w * 0.38f, groundY - h * 0.56f), CornerRadius(8f, 8f))

            // Bonnet
            val hoodPath = Path().apply {
                moveTo(w * 0.70f, h * 0.45f)
                lineTo(w * 0.89f, h * 0.49f)
                lineTo(w * 0.90f, groundY - h * 0.10f)
                lineTo(w * 0.70f, groundY - h * 0.10f)
                close()
            }
            drawPath(hoodPath, hoodColor)

            drawNganyaStripes(cfg.stripeStyle, accentColor, w * 0.12f, h * 0.48f, w * 0.75f, h * 0.18f)

            // Windows
            val winPath = Path().apply {
                moveTo(w * 0.29f, h * 0.43f)
                lineTo(w * 0.38f, h * 0.31f)
                lineTo(w * 0.61f, h * 0.31f)
                lineTo(w * 0.71f, h * 0.43f)
                close()
            }
            drawPath(winPath, Color(0xFF1E2936))

            // Bumpers
            drawRoundRect(bumperColor, Offset(w * 0.08f, groundY - h * 0.13f), Size(w * 0.11f, h * 0.07f), CornerRadius(8f, 8f))
            drawRoundRect(bumperColor, Offset(w * 0.81f, groundY - h * 0.13f), Size(w * 0.11f, h * 0.07f), CornerRadius(8f, 8f))

            drawWheel(w * 0.25f, groundY - h * 0.06f, h * 0.105f, rimColor)
            drawWheel(w * 0.76f, groundY - h * 0.06f, h * 0.105f, rimColor)

            drawMotif(cfg.motif, accentColor, Offset(w * 0.20f, h * 0.55f), h * 0.05f)
            drawRouteBanner(cfg.routeText, Offset(w * 0.50f, h * 0.60f), accentColor)
        }

        VehicleTemplate.PROBOX -> {
            // Probox / Station Wagon silhouette
            val bodyPath = Path().apply {
                moveTo(w * 0.11f, groundY - h * 0.09f)
                lineTo(w * 0.12f, h * 0.28f)
                lineTo(w * 0.61f, h * 0.27f)
                lineTo(w * 0.72f, h * 0.45f)
                lineTo(w * 0.89f, h * 0.49f)
                lineTo(w * 0.89f, groundY - h * 0.09f)
                close()
            }
            drawPath(bodyPath, bodyColor)

            // Roof
            drawRoundRect(roofColor, Offset(w * 0.13f, h * 0.26f), Size(w * 0.48f, h * 0.045f), CornerRadius(8f, 8f))

            // Doors
            drawRoundRect(doorsColor, Offset(w * 0.30f, h * 0.45f), Size(w * 0.39f, groundY - h * 0.56f), CornerRadius(6f, 6f))

            // Hood
            val hoodPath = Path().apply {
                moveTo(w * 0.70f, h * 0.45f)
                lineTo(w * 0.89f, h * 0.49f)
                lineTo(w * 0.89f, groundY - h * 0.10f)
                lineTo(w * 0.70f, groundY - h * 0.10f)
                close()
            }
            drawPath(hoodPath, hoodColor)

            drawNganyaStripes(cfg.stripeStyle, accentColor, w * 0.12f, h * 0.47f, w * 0.74f, h * 0.19f)

            // Wagon windows
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.16f, h * 0.31f), Size(w * 0.16f, h * 0.12f), CornerRadius(6f, 6f))
            drawRoundRect(Color(0xFF1E2936), Offset(w * 0.34f, h * 0.31f), Size(w * 0.16f, h * 0.12f), CornerRadius(6f, 6f))
            val frontWin = Path().apply {
                moveTo(w * 0.52f, h * 0.31f)
                lineTo(w * 0.60f, h * 0.31f)
                lineTo(w * 0.69f, h * 0.43f)
                lineTo(w * 0.52f, h * 0.43f)
                close()
            }
            drawPath(frontWin, Color(0xFF233242))

            // Bumpers
            drawRoundRect(bumperColor, Offset(w * 0.09f, groundY - h * 0.13f), Size(w * 0.11f, h * 0.07f), CornerRadius(8f, 8f))
            drawRoundRect(bumperColor, Offset(w * 0.81f, groundY - h * 0.13f), Size(w * 0.10f, h * 0.07f), CornerRadius(8f, 8f))

            drawWheel(w * 0.25f, groundY - h * 0.06f, h * 0.105f, rimColor)
            drawWheel(w * 0.76f, groundY - h * 0.06f, h * 0.105f, rimColor)

            drawMotif(cfg.motif, accentColor, Offset(w * 0.21f, h * 0.54f), h * 0.055f)
            drawRouteBanner(cfg.routeText, Offset(w * 0.50f, h * 0.60f), accentColor)
        }
    }
}

private fun DrawScope.drawFrontTemplate(
    template: VehicleTemplate,
    cfg: LiveryConfig
) {
    val w = size.width
    val h = size.height
    val bodyColor = parseHexColor(cfg.bodyHex)
    val roofColor = parseHexColor(cfg.roofHex)
    val hoodColor = parseHexColor(cfg.hoodHex)
    val bumperColor = parseHexColor(cfg.bumpersHex)
    val rimColor = parseHexColor(cfg.rimsHex)
    val accentColor = parseHexColor(cfg.accentHex)

    val isTall = template == VehicleTemplate.HIACE
    val topY = if (isTall) h * 0.15f else h * 0.23f
    val cabW = if (isTall) w * 0.44f else w * 0.46f
    val leftX = (w - cabW) / 2f

    // Tyres peeking underneath
    drawRoundRect(Color(0xFF0A0C0E), Offset(leftX - w * 0.01f, h * 0.64f), Size(w * 0.08f, h * 0.17f), CornerRadius(10f, 10f))
    drawRoundRect(Color(0xFF0A0C0E), Offset(leftX + cabW - w * 0.07f, h * 0.64f), Size(w * 0.08f, h * 0.17f), CornerRadius(10f, 10f))

    // Roof
    drawRoundRect(roofColor, Offset(leftX + w * 0.04f, topY), Size(cabW - w * 0.08f, h * 0.07f), CornerRadius(16f, 16f))

    // Main cabin + fenders
    drawRoundRect(bodyColor, Offset(leftX, topY + h * 0.05f), Size(cabW, h * 0.58f - topY), CornerRadius(22f, 22f))

    // Windshield
    drawRoundRect(Color(0xFF1F2B38), Offset(leftX + w * 0.045f, topY + h * 0.08f), Size(cabW - w * 0.09f, h * 0.19f), CornerRadius(12f, 12f))

    // Hood / front nose
    drawRoundRect(hoodColor, Offset(leftX + w * 0.03f, topY + h * 0.29f), Size(cabW - w * 0.06f, h * 0.16f), CornerRadius(12f, 12f))

    // Grille & headlights
    drawRoundRect(Color(0xFF14171A), Offset(leftX + w * 0.14f, topY + h * 0.34f), Size(cabW - w * 0.28f, h * 0.08f), CornerRadius(6f, 6f))
    drawRoundRect(Color(0xFFFFF4D1), Offset(leftX + w * 0.035f, topY + h * 0.33f), Size(w * 0.075f, h * 0.065f), CornerRadius(8f, 8f))
    drawRoundRect(Color(0xFFFFF4D1), Offset(leftX + cabW - w * 0.11f, topY + h * 0.33f), Size(w * 0.075f, h * 0.065f), CornerRadius(8f, 8f))

    // Bumper
    drawRoundRect(bumperColor, Offset(leftX - w * 0.01f, h * 0.58f), Size(cabW + w * 0.02f, h * 0.11f), CornerRadius(12f, 12f))

    // Accent strip & motif on hood
    if (cfg.stripeStyle != StripeStyle.NONE) {
        drawRoundRect(accentColor, Offset(leftX + cabW * 0.38f, topY + h * 0.29f), Size(cabW * 0.24f, h * 0.04f), CornerRadius(4f, 4f))
    }
    drawMotif(cfg.motif, rimColor, Offset(w * 0.5f, h * 0.635f), h * 0.04f)
    drawRouteBanner(cfg.routeText, Offset(w * 0.5f, topY + h * 0.04f), accentColor)
}

private fun DrawScope.drawNganyaStripes(
    style: StripeStyle,
    accent: Color,
    startX: Float,
    startY: Float,
    width: Float,
    height: Float
) {
    when (style) {
        StripeStyle.NONE -> Unit
        StripeStyle.CHEVRON -> {
            drawRect(accent.copy(alpha = 0.88f), Offset(startX, startY + height * 0.35f), Size(width, height * 0.28f))
        }
        StripeStyle.WAVE -> {
            val path = Path().apply {
                moveTo(startX, startY + height * 0.6f)
                cubicTo(
                    startX + width * 0.35f, startY - height * 0.2f,
                    startX + width * 0.65f, startY + height * 1.1f,
                    startX + width, startY + height * 0.25f
                )
            }
            drawPath(path, accent, style = Stroke(width = 12f))
        }
        StripeStyle.LIGHTNING -> {
            val path = Path().apply {
                moveTo(startX, startY + height * 0.7f)
                lineTo(startX + width * 0.28f, startY + height * 0.2f)
                lineTo(startX + width * 0.42f, startY + height * 0.65f)
                lineTo(startX + width * 0.72f, startY + height * 0.1f)
                lineTo(startX + width, startY + height * 0.4f)
            }
            drawPath(path, accent, style = Stroke(width = 11f))
        }
    }
}

private fun DrawScope.drawWheel(cx: Float, cy: Float, radius: Float, rimColor: Color) {
    drawCircle(Color(0xFF0A0C0E), radius, Offset(cx, cy))
    drawCircle(rimColor, radius * 0.62f, Offset(cx, cy))
    drawCircle(Color(0xFF1B2026), radius * 0.24f, Offset(cx, cy))
}

private fun DrawScope.drawMotif(motif: MotifBadge, color: Color, center: Offset, radius: Float) {
    if (motif == MotifBadge.NONE) return
    when (motif) {
        MotifBadge.CROWN -> {
            val p = Path().apply {
                moveTo(center.x - radius, center.y + radius * 0.6f)
                lineTo(center.x - radius, center.y - radius * 0.4f)
                lineTo(center.x - radius * 0.4f, center.y + radius * 0.1f)
                lineTo(center.x, center.y - radius * 0.7f)
                lineTo(center.x + radius * 0.4f, center.y + radius * 0.1f)
                lineTo(center.x + radius, center.y - radius * 0.4f)
                lineTo(center.x + radius, center.y + radius * 0.6f)
                close()
            }
            drawPath(p, color)
        }
        MotifBadge.STAR -> {
            drawCircle(color, radius * 0.7f, center, style = Stroke(width = 5f))
            drawCircle(color, radius * 0.32f, center)
        }
        MotifBadge.BOLT -> {
            val p = Path().apply {
                moveTo(center.x + radius * 0.2f, center.y - radius)
                lineTo(center.x - radius * 0.6f, center.y + radius * 0.1f)
                lineTo(center.x, center.y + radius * 0.1f)
                lineTo(center.x - radius * 0.2f, center.y + radius)
                lineTo(center.x + radius * 0.6f, center.y - radius * 0.1f)
                lineTo(center.x, center.y - radius * 0.1f)
                close()
            }
            drawPath(p, color)
        }
        MotifBadge.NONE -> Unit
    }
}

private fun DrawScope.drawRouteBanner(text: String, center: Offset, accent: Color) {
    if (text.isBlank()) return
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (size.height * 0.068f).coerceAtLeast(18f)
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(4f, 0f, 2f, accent.toArgb())
    }
    drawContext.canvas.nativeCanvas.drawText(text.take(28), center.x, center.y, paint)
}

/**
 * Renders a high-resolution customer presentation card PNG for export/sharing.
 */
fun renderPresentationCardBitmap(
    title: String,
    template: VehicleTemplate,
    cfg: LiveryConfig
): Bitmap {
    val w = 1280
    val h = 760
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bmp)

    // Background
    val bgPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#11151A")
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    // Header
    val headerPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("MOTOOPAINT • ${title.uppercase()}", 56f, 76f, headerPaint)

    val subPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#B0BAC5")
        textSize = 24f
    }
    canvas.drawText("${template.label}  |  ${cfg.routeText.ifBlank { "Custom Concept" }}", 56f, 114f, subPaint)

    // Vehicle preview block
    val bodyPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = parseHexColor(cfg.bodyHex).toArgb()
    }
    val roofPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = parseHexColor(cfg.roofHex).toArgb()
    }
    val accentPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = parseHexColor(cfg.accentHex).toArgb()
    }
    val hoodPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = parseHexColor(cfg.hoodHex).toArgb()
    }
    val rimPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = parseHexColor(cfg.rimsHex).toArgb()
    }

    val bodyRect = RectF(160f, 200f, 1120f, 520f)
    canvas.drawRoundRect(bodyRect, 36f, 36f, bodyPaint)
    canvas.drawRoundRect(RectF(200f, 180f, 840f, 230f), 18f, 18f, roofPaint)
    canvas.drawRoundRect(RectF(840f, 310f, 1120f, 520f), 24f, 24f, hoodPaint)

    if (cfg.stripeStyle != StripeStyle.NONE) {
        val stripe = AndroidPath().apply {
            moveTo(190f, 430f)
            lineTo(520f, 310f)
            lineTo(660f, 390f)
            lineTo(940f, 290f)
            lineTo(1090f, 350f)
        }
        val sp = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = accentPaint.color
            style = AndroidPaint.Style.STROKE
            strokeWidth = 24f
        }
        canvas.drawPath(stripe, sp)
    }

    val tyrePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK }
    canvas.drawCircle(350f, 520f, 68f, tyrePaint)
    canvas.drawCircle(350f, 520f, 40f, rimPaint)
    canvas.drawCircle(930f, 520f, 68f, tyrePaint)
    canvas.drawCircle(930f, 520f, 40f, rimPaint)

    // Swatch legend bar
    val swatches = listOf(
        "Body" to cfg.bodyHex,
        "Roof" to cfg.roofHex,
        "Hood" to cfg.hoodHex,
        "Accent" to cfg.accentHex,
        "Rims" to cfg.rimsHex
    )
    swatches.forEachIndexed { idx, (label, hex) ->
        val x = 64f + idx * 230f
        val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = parseHexColor(hex).toArgb()
        }
        canvas.drawCircle(x + 24f, 650f, 22f, p)
        canvas.drawText("$label $hex", x + 58f, 658f, subPaint)
    }

    val footerPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#8894A0")
        textSize = 20f
    }
    canvas.drawText("Digital Concept Preview Only — Verify physical colour card & spray-out before mixing.", 56f, 722f, footerPaint)
    return bmp
}
