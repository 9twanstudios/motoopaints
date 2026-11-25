package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.data.local.CarPhotoEntity
import com.example.data.local.OemPaintEntity
import com.example.data.local.PaintJobEntity
import com.example.data.local.RefinishSystemEntity
import com.example.data.local.SprayOutEntity
import com.example.data.local.StandardColourEntity
import com.example.data.local.StockItemEntity
import com.example.data.local.StockUsageEntity
import com.example.data.local.StudioProjectEntity
import com.example.data.local.TechnicianFormulaEntity
import com.example.data.local.VehicleModelEntity
import com.example.data.local.VehicleRecordEntity
import com.example.data.repository.BackupSummary
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class CatalogueFilters(
    val query: String = "",
    val make: String = "All",
    val bodyType: String = "All",
    val finish: String = "All",
    val verification: String = "All"
)

data class WorkshopAnalytics(
    val topVehicles: List<VehicleModelEntity> = emptyList(),
    val topOemCodes: List<OemPaintEntity> = emptyList(),
    val totalJobs: Int = 0,
    val completedJobs: Int = 0,
    val reworkJobs: Int = 0,
    val totalMaterialCostKsh: Double = 0.0,
    val lowStockItems: List<StockItemEntity> = emptyList(),
    val acceptedSprayOuts: Int = 0,
    val adjustedSprayOuts: Int = 0,
    val rejectedSprayOuts: Int = 0,
    val savedLiveryCount: Int = 0,
    val carPhotoCount: Int = 0
)

class MotoopaintViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val repo = container.repository
    private val prefs = container.preferences

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val workshopName: StateFlow<String> = prefs.workshopName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Nairobi Paint Bay")

    val vehicleModels: StateFlow<List<VehicleModelEntity>> = repo.vehicleModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val oemPaints: StateFlow<List<OemPaintEntity>> = repo.oemPaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val standardColours: StateFlow<List<StandardColourEntity>> = repo.standardColours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val refinishSystems: StateFlow<List<RefinishSystemEntity>> = repo.refinishSystems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val technicianFormulas: StateFlow<List<TechnicianFormulaEntity>> = repo.technicianFormulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicleRecords: StateFlow<List<VehicleRecordEntity>> = repo.vehicleRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paintJobs: StateFlow<List<PaintJobEntity>> = repo.paintJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sprayOuts: StateFlow<List<SprayOutEntity>> = repo.sprayOuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockItems: StateFlow<List<StockItemEntity>> = repo.stockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockUsage: StateFlow<List<StockUsageEntity>> = repo.stockUsage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val carPhotos: StateFlow<List<CarPhotoEntity>> = repo.carPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studioProjects: StateFlow<List<StudioProjectEntity>> = repo.studioProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filters = MutableStateFlow(CatalogueFilters())
    val filters: StateFlow<CatalogueFilters> = _filters.asStateFlow()

    private val _activePhotoForStudio = MutableStateFlow<String?>(null)
    val activePhotoForStudio: StateFlow<String?> = _activePhotoForStudio.asStateFlow()

    private val _activeColourForStudio = MutableStateFlow<Pair<String, String>?>(null)
    val activeColourForStudio: StateFlow<Pair<String, String>?> = _activeColourForStudio.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val analytics: StateFlow<WorkshopAnalytics> = combine(
        vehicleModels,
        oemPaints,
        paintJobs,
        stockItems,
        stockUsage,
        sprayOuts,
        studioProjects,
        carPhotos
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val vehicles = values[0] as List<VehicleModelEntity>
        @Suppress("UNCHECKED_CAST")
        val oems = values[1] as List<OemPaintEntity>
        @Suppress("UNCHECKED_CAST")
        val jobs = values[2] as List<PaintJobEntity>
        @Suppress("UNCHECKED_CAST")
        val stock = values[3] as List<StockItemEntity>
        @Suppress("UNCHECKED_CAST")
        val usage = values[4] as List<StockUsageEntity>
        @Suppress("UNCHECKED_CAST")
        val sprays = values[5] as List<SprayOutEntity>
        @Suppress("UNCHECKED_CAST")
        val projects = values[6] as List<StudioProjectEntity>
        @Suppress("UNCHECKED_CAST")
        val photos = values[7] as List<CarPhotoEntity>

        WorkshopAnalytics(
            topVehicles = vehicles.sortedByDescending { it.lookupCount }.take(4),
            topOemCodes = oems.sortedByDescending { it.usageCount }.take(4),
            totalJobs = jobs.size,
            completedJobs = jobs.count { it.status == "Completed" },
            reworkJobs = jobs.count { it.status == "Rework" },
            totalMaterialCostKsh = usage.sumOf { it.costKsh },
            lowStockItems = stock.filter { it.quantity <= it.reorderThreshold },
            acceptedSprayOuts = sprays.count { it.outcome == "ACCEPTED" },
            adjustedSprayOuts = sprays.count { it.outcome == "ADJUSTED" },
            rejectedSprayOuts = sprays.count { it.outcome == "REJECTED" },
            savedLiveryCount = projects.count { it.mode == "LIVERY" },
            carPhotoCount = photos.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkshopAnalytics())

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun cycleThemeMode() {
        val next = when (themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DAYLIGHT
            ThemeMode.DAYLIGHT -> ThemeMode.DARK_BOOTH
            ThemeMode.DARK_BOOTH -> ThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    fun setWorkshopName(name: String) {
        viewModelScope.launch { prefs.setWorkshopName(name) }
    }

    fun updateFilters(transform: (CatalogueFilters) -> CatalogueFilters) {
        _filters.value = transform(_filters.value)
    }

    fun resetFilters() {
        _filters.value = CatalogueFilters()
    }

    fun recordVehicleLookup(id: Long) {
        viewModelScope.launch { repo.incrementVehicleLookup(id) }
    }

    fun toggleFavourite(paint: OemPaintEntity) {
        viewModelScope.launch {
            repo.setOemFavourite(paint.id, !paint.isFavourite)
        }
    }

    fun recordOemUsage(id: Long) {
        viewModelScope.launch { repo.incrementOemUsage(id) }
    }

    fun sendColourToStudio(hex: String, finish: String) {
        _activeColourForStudio.value = hex to finish
    }

    fun openPhotoInStudio(path: String) {
        _activePhotoForStudio.value = path
    }

    fun createCameraTarget(): Pair<File, Uri> = repo.mediaStorage.createCameraCaptureFileAndUri()

    fun saveCapturedCameraPhoto(tempFile: File, vehicleLabel: String = "Bay Vehicle Photo") {
        viewModelScope.launch {
            val savedPath = repo.mediaStorage.persistCameraFile(tempFile)
            if (savedPath != null) {
                repo.saveCarPhoto(
                    CarPhotoEntity(
                        title = "Camera Capture",
                        filePath = savedPath,
                        vehicleLabel = vehicleLabel,
                        capturedWithCamera = true
                    )
                )
                _activePhotoForStudio.value = savedPath
                _uiMessage.value = "Car photo saved to Photo Library & loaded in Studio"
            } else {
                _uiMessage.value = "Camera capture cancelled or empty"
            }
        }
    }

    fun importGalleryCarPhoto(uri: Uri, vehicleLabel: String = "Imported Car Photo") {
        viewModelScope.launch {
            val savedPath = repo.mediaStorage.importPhotoFromUri(uri)
            if (savedPath != null) {
                repo.saveCarPhoto(
                    CarPhotoEntity(
                        title = "Gallery Import",
                        filePath = savedPath,
                        vehicleLabel = vehicleLabel,
                        capturedWithCamera = false
                    )
                )
                _activePhotoForStudio.value = savedPath
                _uiMessage.value = "Photo imported to Car Photo Library"
            } else {
                _uiMessage.value = "Could not import selected photo"
            }
        }
    }

    fun deleteCarPhoto(photo: CarPhotoEntity) {
        viewModelScope.launch {
            repo.deleteCarPhoto(photo)
            if (_activePhotoForStudio.value == photo.filePath) {
                _activePhotoForStudio.value = null
            }
            _uiMessage.value = "Photo removed from library"
        }
    }

    suspend fun loadBitmap(path: String): Bitmap? = repo.mediaStorage.loadBitmap(path)

    fun savePhotoProject(
        title: String,
        vehicleLabel: String,
        photoPath: String,
        maskBitmap: Bitmap?,
        hexColor: String,
        finish: String
    ) {
        viewModelScope.launch {
            val maskPath = if (maskBitmap != null) {
                repo.mediaStorage.saveMaskBitmap(maskBitmap)
            } else ""
            repo.saveStudioProject(
                StudioProjectEntity(
                    title = title.ifBlank { "Photo Preview" },
                    mode = "PHOTO",
                    vehicleLabel = vehicleLabel.ifBlank { "Workshop Vehicle" },
                    photoPath = photoPath,
                    maskPath = maskPath,
                    selectedHex = hexColor,
                    selectedFinish = finish
                )
            )
            _uiMessage.value = "Photo Painter project saved locally"
        }
    }

    fun saveLiveryProject(
        title: String,
        vehicleLabel: String,
        templateId: String,
        viewAngle: String,
        liveryJson: String,
        accentHex: String
    ) {
        viewModelScope.launch {
            repo.saveStudioProject(
                StudioProjectEntity(
                    title = title.ifBlank { "Nganya Concept" },
                    mode = "LIVERY",
                    vehicleLabel = vehicleLabel.ifBlank { templateId },
                    templateId = templateId,
                    viewAngle = viewAngle,
                    liveryJson = liveryJson,
                    selectedHex = accentHex,
                    selectedFinish = "Pearl"
                )
            )
            _uiMessage.value = "Livery design saved locally"
        }
    }

    fun deleteStudioProject(project: StudioProjectEntity) {
        viewModelScope.launch {
            repo.deleteStudioProject(project)
            _uiMessage.value = "Studio project deleted"
        }
    }

    suspend fun createShareExportUri(bitmap: Bitmap, prefix: String): Uri? {
        return repo.mediaStorage.saveShareableExport(bitmap, prefix)
    }

    fun saveJob(job: PaintJobEntity) {
        viewModelScope.launch {
            repo.savePaintJob(job.copy(updatedAt = System.currentTimeMillis()))
            _uiMessage.value = "Paint job saved"
        }
    }

    fun updateJobStatus(job: PaintJobEntity, newStatus: String) {
        viewModelScope.launch {
            repo.updatePaintJob(job.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
            _uiMessage.value = "Job moved to $newStatus"
        }
    }

    fun deleteJob(id: Long) {
        viewModelScope.launch { repo.deletePaintJob(id) }
    }

    fun saveFormula(formula: TechnicianFormulaEntity) {
        viewModelScope.launch {
            repo.saveFormula(formula.copy(updatedAt = System.currentTimeMillis()))
            _uiMessage.value = "Formula saved to notebook"
        }
    }

    fun deleteFormula(id: Long) {
        viewModelScope.launch { repo.deleteFormula(id) }
    }

    fun saveSprayOut(sprayOut: SprayOutEntity) {
        viewModelScope.launch {
            repo.saveSprayOut(sprayOut)
            _uiMessage.value = "Spray-out test logged (${sprayOut.outcome})"
        }
    }

    fun saveStockItem(item: StockItemEntity) {
        viewModelScope.launch {
            repo.saveStockItem(item)
            _uiMessage.value = "Stock item saved"
        }
    }

    fun deleteStockItem(id: Long) {
        viewModelScope.launch { repo.deleteStockItem(id) }
    }

    fun logStockUsage(
        stockItemId: Long,
        jobId: Long,
        jobTitle: String,
        quantityUsed: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repo.logStockUsageForJob(stockItemId, jobId, jobTitle, quantityUsed, notes)
            _uiMessage.value = "Stock deducted & costed to job"
        }
    }

    fun exportBackup(uri: Uri, onComplete: (Result<BackupSummary>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repo.exportBackup(uri)
            res.onSuccess {
                _uiMessage.value = "Backup exported (${it.jobsCount} jobs, ${it.stockCount} stock items)"
            }.onFailure {
                _uiMessage.value = "Backup failed: ${it.localizedMessage}"
            }
            onComplete(res)
        }
    }

    fun restoreBackup(uri: Uri, onComplete: (Result<BackupSummary>) -> Unit = {}) {
        viewModelScope.launch {
            val res = repo.restoreBackup(uri)
            res.onSuccess {
                _uiMessage.value = "Restored ${it.jobsCount} jobs, ${it.formulasCount} formulas, ${it.stockCount} stock items"
            }.onFailure {
                _uiMessage.value = "Restore failed: ${it.localizedMessage}"
            }
            onComplete(res)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MotoopaintViewModel(container) as T
        }
    }
}
