package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MotoopaintDao {

    // Vehicle Catalogue
    @Query("SELECT * FROM vehicle_models ORDER BY lookupCount DESC, make ASC, model ASC")
    fun observeVehicleModels(): Flow<List<VehicleModelEntity>>

    @Query(
        "SELECT * FROM vehicle_models " +
            "WHERE make LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%' OR bodyType LIKE '%' || :query || '%' " +
            "ORDER BY lookupCount DESC, make ASC, model ASC"
    )
    fun searchVehicleModels(query: String): Flow<List<VehicleModelEntity>>

    @Query("SELECT COUNT(*) FROM vehicle_models")
    suspend fun getVehicleModelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleModels(items: List<VehicleModelEntity>)

    @Query("UPDATE vehicle_models SET lookupCount = lookupCount + 1 WHERE id = :id")
    suspend fun incrementVehicleLookup(id: Long)

    // OEM Paints
    @Query("SELECT * FROM oem_paints ORDER BY isFavourite DESC, usageCount DESC, make ASC, oemCode ASC")
    fun observeOemPaints(): Flow<List<OemPaintEntity>>

    @Query(
        "SELECT * FROM oem_paints " +
            "WHERE oemCode LIKE '%' || :query || '%' OR factoryName LIKE '%' || :query || '%' " +
            "OR make LIKE '%' || :query || '%' OR colourFamily LIKE '%' || :query || '%' " +
            "ORDER BY isFavourite DESC, usageCount DESC, make ASC, oemCode ASC"
    )
    fun searchOemPaints(query: String): Flow<List<OemPaintEntity>>

    @Query("SELECT * FROM oem_paints WHERE isFavourite = 1 ORDER BY make ASC, oemCode ASC")
    fun observeFavouritePaints(): Flow<List<OemPaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOemPaints(items: List<OemPaintEntity>)

    @Query("UPDATE oem_paints SET isFavourite = :favourite WHERE id = :id")
    suspend fun setOemFavourite(id: Long, favourite: Boolean)

    @Query("UPDATE oem_paints SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementOemUsage(id: Long)

    // Standard Colour Dictionary
    @Query("SELECT * FROM standard_colours ORDER BY displayName ASC")
    fun observeStandardColours(): Flow<List<StandardColourEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStandardColours(items: List<StandardColourEntity>)

    // Refinish System References
    @Query("SELECT * FROM refinish_systems ORDER BY paintBrand ASC, oemCodeRef ASC")
    fun observeRefinishSystems(): Flow<List<RefinishSystemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefinishSystems(items: List<RefinishSystemEntity>)

    // Technician Formulas
    @Query("SELECT * FROM technician_formulas ORDER BY updatedAt DESC")
    fun observeTechnicianFormulas(): Flow<List<TechnicianFormulaEntity>>

    @Query("SELECT * FROM technician_formulas ORDER BY updatedAt DESC")
    suspend fun getAllFormulasSnapshot(): List<TechnicianFormulaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(item: TechnicianFormulaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(items: List<TechnicianFormulaEntity>)

    @Query("DELETE FROM technician_formulas WHERE id = :id")
    suspend fun deleteFormula(id: Long)

    // Vehicle Records
    @Query("SELECT * FROM vehicle_records ORDER BY createdAt DESC")
    fun observeVehicleRecords(): Flow<List<VehicleRecordEntity>>

    @Query("SELECT * FROM vehicle_records ORDER BY createdAt DESC")
    suspend fun getAllVehicleRecordsSnapshot(): List<VehicleRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleRecord(item: VehicleRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleRecords(items: List<VehicleRecordEntity>)

    // Paint Jobs
    @Query("SELECT * FROM paint_jobs ORDER BY updatedAt DESC")
    fun observePaintJobs(): Flow<List<PaintJobEntity>>

    @Query("SELECT * FROM paint_jobs ORDER BY updatedAt DESC")
    suspend fun getAllPaintJobsSnapshot(): List<PaintJobEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaintJob(item: PaintJobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaintJobs(items: List<PaintJobEntity>)

    @Update
    suspend fun updatePaintJob(item: PaintJobEntity)

    @Query("DELETE FROM paint_jobs WHERE id = :id")
    suspend fun deletePaintJob(id: Long)

    // Spray-Out Logs
    @Query("SELECT * FROM spray_outs ORDER BY createdAt DESC")
    fun observeSprayOuts(): Flow<List<SprayOutEntity>>

    @Query("SELECT * FROM spray_outs ORDER BY createdAt DESC")
    suspend fun getAllSprayOutsSnapshot(): List<SprayOutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprayOut(item: SprayOutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprayOuts(items: List<SprayOutEntity>)

    // Workshop Stock Inventory
    @Query("SELECT * FROM stock_items ORDER BY category ASC, name ASC")
    fun observeStockItems(): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items WHERE category = :category ORDER BY name ASC")
    fun observeStockByCategory(category: String): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items WHERE quantity <= reorderThreshold ORDER BY quantity ASC, name ASC")
    fun observeLowStockItems(): Flow<List<StockItemEntity>>

    @Query("SELECT * FROM stock_items ORDER BY category ASC, name ASC")
    suspend fun getAllStockItemsSnapshot(): List<StockItemEntity>

    @Query("SELECT * FROM stock_items WHERE id = :id LIMIT 1")
    suspend fun getStockItemById(id: Long): StockItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItems(items: List<StockItemEntity>)

    @Update
    suspend fun updateStockItem(item: StockItemEntity)

    @Query("DELETE FROM stock_items WHERE id = :id")
    suspend fun deleteStockItem(id: Long)

    @Query("SELECT * FROM stock_usage ORDER BY usedAt DESC")
    fun observeStockUsage(): Flow<List<StockUsageEntity>>

    @Query("SELECT * FROM stock_usage ORDER BY usedAt DESC")
    suspend fun getAllStockUsageSnapshot(): List<StockUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockUsage(entry: StockUsageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockUsages(items: List<StockUsageEntity>)

    @Transaction
    suspend fun consumeStockForJob(
        stockItemId: Long,
        jobId: Long,
        jobTitle: String,
        quantityUsed: Double,
        notes: String
    ) {
        val item = getStockItemById(stockItemId) ?: return
        val clampedUsed = quantityUsed.coerceAtLeast(0.0)
        val remaining = (item.quantity - clampedUsed).coerceAtLeast(0.0)
        updateStockItem(item.copy(quantity = remaining))
        insertStockUsage(
            StockUsageEntity(
                stockItemId = item.id,
                stockItemName = item.name,
                jobId = jobId,
                jobTitle = jobTitle,
                quantityUsed = clampedUsed,
                costKsh = clampedUsed * item.unitCostKsh,
                unit = item.unit,
                notes = notes
            )
        )
    }

    // Car Photo Library
    @Query("SELECT * FROM car_photos ORDER BY createdAt DESC")
    fun observeCarPhotos(): Flow<List<CarPhotoEntity>>

    @Query("SELECT * FROM car_photos ORDER BY createdAt DESC")
    suspend fun getAllCarPhotosSnapshot(): List<CarPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarPhoto(photo: CarPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarPhotos(photos: List<CarPhotoEntity>)

    @Query("DELETE FROM car_photos WHERE id = :id")
    suspend fun deleteCarPhoto(id: Long)

    // Studio Projects
    @Query("SELECT * FROM studio_projects ORDER BY updatedAt DESC")
    fun observeStudioProjects(): Flow<List<StudioProjectEntity>>

    @Query("SELECT * FROM studio_projects ORDER BY updatedAt DESC")
    suspend fun getAllStudioProjectsSnapshot(): List<StudioProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudioProject(project: StudioProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudioProjects(projects: List<StudioProjectEntity>)

    @Query("DELETE FROM studio_projects WHERE id = :id")
    suspend fun deleteStudioProject(id: Long)

    // Restore wipe user-authored tables safely
    @Query("DELETE FROM technician_formulas")
    suspend fun clearFormulas()

    @Query("DELETE FROM vehicle_records")
    suspend fun clearVehicleRecords()

    @Query("DELETE FROM paint_jobs")
    suspend fun clearPaintJobs()

    @Query("DELETE FROM spray_outs")
    suspend fun clearSprayOuts()

    @Query("DELETE FROM stock_items")
    suspend fun clearStockItems()

    @Query("DELETE FROM stock_usage")
    suspend fun clearStockUsage()

    @Query("DELETE FROM studio_projects")
    suspend fun clearStudioProjects()

    @Query("DELETE FROM car_photos")
    suspend fun clearCarPhotos()

    @Transaction
    suspend fun replaceUserBackupData(
        formulas: List<TechnicianFormulaEntity>,
        vehicles: List<VehicleRecordEntity>,
        jobs: List<PaintJobEntity>,
        sprayOuts: List<SprayOutEntity>,
        stock: List<StockItemEntity>,
        usage: List<StockUsageEntity>,
        projects: List<StudioProjectEntity>,
        photos: List<CarPhotoEntity>
    ) {
        clearFormulas()
        clearVehicleRecords()
        clearPaintJobs()
        clearSprayOuts()
        clearStockItems()
        clearStockUsage()
        clearStudioProjects()
        if (photos.isNotEmpty()) {
            clearCarPhotos()
        }

        insertFormulas(formulas)
        insertVehicleRecords(vehicles)
        insertPaintJobs(jobs)
        insertSprayOuts(sprayOuts)
        insertStockItems(stock)
        insertStockUsages(usage)
        insertStudioProjects(projects)
        if (photos.isNotEmpty()) {
            insertCarPhotos(photos)
        }
    }
}
