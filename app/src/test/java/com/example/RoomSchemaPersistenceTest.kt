package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MotoopaintDao
import com.example.data.local.MotoopaintDatabase
import com.example.data.local.PaintJobEntity
import com.example.data.local.SeedData
import com.example.data.local.StockItemEntity
import com.example.data.local.VehicleRecordEntity
import com.example.data.repository.BackupManager
import com.example.data.repository.LocalMediaStorage
import com.example.data.repository.MotoopaintRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomSchemaPersistenceTest {

    private lateinit var database: MotoopaintDatabase
    private lateinit var dao: MotoopaintDao
    private lateinit var repository: MotoopaintRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MotoopaintDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dao()
        repository = MotoopaintRepository(
            dao = dao,
            mediaStorage = LocalMediaStorage(context),
            backupManager = BackupManager(context, dao)
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `offline seed populates vehicle colour and workshop stock tables`() = runBlocking {
        repository.ensureSeeded()

        val vehicles = repository.vehicleModels.first()
        val oemPaints = repository.oemPaints.first()
        val standardColours = repository.standardColours.first()
        val refinishSystems = repository.refinishSystems.first()
        val stockItems = repository.stockItems.first()

        assertEquals(SeedData.vehicleModels.size, vehicles.size)
        assertEquals(SeedData.oemPaints.size, oemPaints.size)
        assertEquals(SeedData.standardColours.size, standardColours.size)
        assertEquals(SeedData.refinishSystems.size, refinishSystems.size)
        assertEquals(SeedData.starterStock.size, stockItems.size)
    }

    @Test
    fun `indexed vehicle and oem colour search queries work offline`() = runBlocking {
        repository.ensureSeeded()

        val proboxHits = repository.searchVehicleModels("Probox").first()
        assertTrue(proboxHits.any { it.make == "Toyota" && it.model == "Probox" })

        val silverHits = repository.searchOemPaints("1F7").first()
        assertTrue(silverHits.any { it.oemCode == "1F7" && it.make == "Toyota" })

        val firstPaint = silverHits.first()
        repository.setOemFavourite(firstPaint.id, true)
        val favourites = repository.favouritePaints.first()
        assertTrue(favourites.any { it.id == firstPaint.id })
    }

    @Test
    fun `stock inventory consumption updates balance and logs ksh usage transactionally`() = runBlocking {
        val stockId = repository.saveStockItem(
            StockItemEntity(
                category = "Finishing",
                name = "2K HS Clearcoat",
                brand = "Duxone",
                productCode = "DX48",
                unit = "L",
                quantity = 3.0,
                unitCostKsh = 4200.0,
                supplier = "Nairobi Auto Paints",
                reorderThreshold = 1.5
            )
        )
        val vehicleId = repository.saveVehicleRecord(
            VehicleRecordEntity(
                plateOrLabel = "KDN 412T",
                make = "Toyota",
                model = "Fielder",
                year = "2017",
                bodyType = "Station Wagon"
            )
        )
        val jobId = repository.savePaintJob(
            PaintJobEntity(
                vehicleId = vehicleId,
                vehicleLabel = "KDN 412T • Toyota Fielder",
                oemCode = "1F7",
                colourName = "Classic Silver Metallic",
                finish = "Metallic",
                status = "Clearcoat"
            )
        )

        repository.logStockUsageForJob(
            stockItemId = stockId,
            jobId = jobId,
            jobTitle = "KDN 412T Clearcoat",
            quantityUsed = 2.0,
            notes = "Two coats rear quarter + bumper"
        )

        val updated = dao.getStockItemById(stockId)!!
        assertEquals(1.0, updated.quantity, 0.001)

        val lowStock = repository.lowStockItems.first()
        assertTrue(lowStock.any { it.id == stockId })

        val usage = repository.stockUsage.first()
        assertEquals(1, usage.size)
        assertEquals(8400.0, usage.first().costKsh, 0.001)
        assertEquals(jobId, usage.first().jobId)
    }
}
