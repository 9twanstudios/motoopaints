package com.example.data.repository

import android.net.Uri
import com.example.data.local.CarPhotoEntity
import com.example.data.local.MotoopaintDao
import com.example.data.local.OemPaintEntity
import com.example.data.local.PaintJobEntity
import com.example.data.local.RefinishSystemEntity
import com.example.data.local.SeedData
import com.example.data.local.SprayOutEntity
import com.example.data.local.StandardColourEntity
import com.example.data.local.StockItemEntity
import com.example.data.local.StockUsageEntity
import com.example.data.local.StudioProjectEntity
import com.example.data.local.TechnicianFormulaEntity
import com.example.data.local.VehicleModelEntity
import com.example.data.local.VehicleRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class MotoopaintRepository(
    private val dao: MotoopaintDao,
    val mediaStorage: LocalMediaStorage,
    private val backupManager: BackupManager
) {
    private val seedMutex = Mutex()

    val vehicleModels: Flow<List<VehicleModelEntity>> = dao.observeVehicleModels()
    val oemPaints: Flow<List<OemPaintEntity>> = dao.observeOemPaints()
    val favouritePaints: Flow<List<OemPaintEntity>> = dao.observeFavouritePaints()
    val standardColours: Flow<List<StandardColourEntity>> = dao.observeStandardColours()
    val refinishSystems: Flow<List<RefinishSystemEntity>> = dao.observeRefinishSystems()
    val technicianFormulas: Flow<List<TechnicianFormulaEntity>> = dao.observeTechnicianFormulas()
    val vehicleRecords: Flow<List<VehicleRecordEntity>> = dao.observeVehicleRecords()
    val paintJobs: Flow<List<PaintJobEntity>> = dao.observePaintJobs()
    val sprayOuts: Flow<List<SprayOutEntity>> = dao.observeSprayOuts()
    val stockItems: Flow<List<StockItemEntity>> = dao.observeStockItems()
    val lowStockItems: Flow<List<StockItemEntity>> = dao.observeLowStockItems()
    val stockUsage: Flow<List<StockUsageEntity>> = dao.observeStockUsage()
    val carPhotos: Flow<List<CarPhotoEntity>> = dao.observeCarPhotos()
    val studioProjects: Flow<List<StudioProjectEntity>> = dao.observeStudioProjects()

    fun searchVehicleModels(query: String): Flow<List<VehicleModelEntity>> = dao.searchVehicleModels(query)
    fun searchOemPaints(query: String): Flow<List<OemPaintEntity>> = dao.searchOemPaints(query)
    fun observeStockByCategory(category: String): Flow<List<StockItemEntity>> = dao.observeStockByCategory(category)

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            if (dao.getVehicleModelCount() == 0) {
                dao.insertVehicleModels(SeedData.vehicleModels)
                dao.insertOemPaints(SeedData.oemPaints)
                dao.insertStandardColours(SeedData.standardColours)
                dao.insertRefinishSystems(SeedData.refinishSystems)
                dao.insertFormulas(SeedData.starterFormulas)
                dao.insertVehicleRecords(SeedData.starterVehicles)
                dao.insertPaintJobs(SeedData.starterJobs)
                dao.insertSprayOuts(SeedData.starterSprayOuts)
                dao.insertStockItems(SeedData.starterStock)
                dao.insertStockUsages(SeedData.starterStockUsage)
                dao.insertStudioProjects(SeedData.starterProjects)
            }
        }
    }

    suspend fun incrementVehicleLookup(id: Long) = dao.incrementVehicleLookup(id)
    suspend fun setOemFavourite(id: Long, favourite: Boolean) = dao.setOemFavourite(id, favourite)
    suspend fun incrementOemUsage(id: Long) = dao.incrementOemUsage(id)

    suspend fun saveFormula(formula: TechnicianFormulaEntity) = dao.insertFormula(formula)
    suspend fun deleteFormula(id: Long) = dao.deleteFormula(id)

    suspend fun saveVehicleRecord(record: VehicleRecordEntity): Long = dao.insertVehicleRecord(record)
    suspend fun savePaintJob(job: PaintJobEntity): Long = dao.insertPaintJob(job)
    suspend fun updatePaintJob(job: PaintJobEntity) = dao.updatePaintJob(job)
    suspend fun deletePaintJob(id: Long) = dao.deletePaintJob(id)

    suspend fun saveSprayOut(sprayOut: SprayOutEntity): Long = dao.insertSprayOut(sprayOut)

    suspend fun saveStockItem(item: StockItemEntity): Long = dao.insertStockItem(item)
    suspend fun deleteStockItem(id: Long) = dao.deleteStockItem(id)

    suspend fun logStockUsageForJob(
        stockItemId: Long,
        jobId: Long,
        jobTitle: String,
        quantityUsed: Double,
        notes: String
    ) = dao.consumeStockForJob(stockItemId, jobId, jobTitle, quantityUsed, notes)

    suspend fun saveCarPhoto(photo: CarPhotoEntity): Long = dao.insertCarPhoto(photo)
    suspend fun deleteCarPhoto(photo: CarPhotoEntity) {
        mediaStorage.deleteFileIfExists(photo.filePath)
        dao.deleteCarPhoto(photo.id)
    }

    suspend fun saveStudioProject(project: StudioProjectEntity): Long = dao.insertStudioProject(project)
    suspend fun deleteStudioProject(project: StudioProjectEntity) {
        mediaStorage.deleteFileIfExists(project.maskPath)
        dao.deleteStudioProject(project.id)
    }

    suspend fun exportBackup(uri: Uri): Result<BackupSummary> = backupManager.exportToUri(uri)
    suspend fun restoreBackup(uri: Uri): Result<BackupSummary> = backupManager.restoreFromUri(uri)
}
