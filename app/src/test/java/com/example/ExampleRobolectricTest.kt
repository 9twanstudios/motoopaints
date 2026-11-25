package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SeedData
import com.example.ui.components.LiveryConfig
import com.example.ui.components.MotifBadge
import com.example.ui.components.StripeStyle
import com.example.ui.components.parseHexColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Motoopaint", appName)
    }

    @Test
    fun `seed data includes Kenya priority vehicles and OEM provenance`() {
        assertEquals(28, SeedData.vehicleModels.size)
        assertTrue(SeedData.vehicleModels.any { it.model == "Probox" && it.evidenceSourceId == "SRC-GIZ-NTSA-2025" })
        assertTrue(SeedData.vehicleModels.any { it.make == "Volkswagen" && it.model == "Golf" && it.evidenceSourceId == "SRC-MOTIFITI-2026W46" })
        assertTrue(SeedData.vehicleModels.any { it.bodyType == "Lorry/Truck" && it.vehicleClass == "heavy_commercial" })
        assertTrue(SeedData.oemPaints.any { it.oemCode == "1F7" && it.verificationState == "VERIFIED" })
        assertTrue(SeedData.oemPaints.any { it.make == "Volkswagen" && it.oemCode == "LB9A" })
        assertTrue(SeedData.oemPaints.any { it.verificationState == "GAP_MISSING" })
    }

    @Test
    fun `livery config roundtrips cleanly through json`() {
        val original = LiveryConfig(
            bodyHex = "#00D2FF",
            roofHex = "#121417",
            stripeStyle = StripeStyle.WAVE,
            motif = MotifBadge.BOLT,
            routeText = "ROUTE 111 • NGONG"
        )
        val restored = LiveryConfig.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    @Test
    fun `hex parser handles valid and fallback values`() {
        assertEquals(Color(0xFFD95D1E), parseHexColor("#D95D1E"))
        assertEquals(Color(0xFFB8BDC2), parseHexColor("invalid"))
    }
}
