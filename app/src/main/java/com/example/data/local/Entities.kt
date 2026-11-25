package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicle_models",
    indices = [
        Index(value = ["make", "model"]),
        Index(value = ["bodyType"]),
        Index(value = ["vehicleClass"])
    ]
)
data class VehicleModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val make: String,
    val model: String,
    val generation: String,
    val yearRange: String,
    val bodyType: String, // Station Wagon, Saloon, Pick-Up, Minibus/Matatu, SUV, Lorry/Truck, Panel Van, Unspecified
    val vehicleClass: String = "", // passenger_car, light_commercial, heavy_commercial, public_transport
    val marketOrigin: String, // JDM Import, Local CKD, UK/EU Import, Unspecified
    val lookupCount: Int = 0,
    val source: String,
    val evidenceSourceId: String = "", // e.g. SRC-GIZ-NTSA-2025, SRC-MOTIFITI-2026W47
    val seedStatus: String = "REPORT_EXAMPLE", // REPORT_EXAMPLE, MARKET_SEARCH_OBSERVATION
    val sourceDate: String
)

@Entity(
    tableName = "oem_paints",
    indices = [
        Index(value = ["make", "oemCode"]),
        Index(value = ["oemCode"]),
        Index(value = ["colourFamily"]),
        Index(value = ["isFavourite"])
    ]
)
data class OemPaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val make: String,
    val oemCode: String,
    val factoryName: String,
    val yearRange: String,
    val colourFamily: String,
    val finish: String, // Solid, Metallic, Pearl, Matte, Two-Tone
    val hexColor: String,
    val rgbSummary: String,
    val labSummary: String,
    val source: String,
    val sourceDate: String,
    val verificationState: String, // VERIFIED, COMMUNITY_REF, GAP_MISSING
    val licenceStatus: String, // PUBLIC_REFERENCE, LICENSED_REQUIRED
    val isFavourite: Boolean = false,
    val usageCount: Int = 0,
    val notes: String = ""
)

@Entity(
    tableName = "standard_colours",
    indices = [
        Index(value = ["displayName"]),
        Index(value = ["colourFamily"])
    ]
)
data class StandardColourEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val aliases: String, // comma-separated local search aliases
    val colourFamily: String,
    val finish: String,
    val hexColor: String,
    val isApproximation: Boolean = true,
    val source: String
)

@Entity(
    tableName = "refinish_systems",
    indices = [
        Index(value = ["oemCodeRef"]),
        Index(value = ["paintBrand"])
    ]
)
data class RefinishSystemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paintBrand: String,
    val productSystem: String,
    val oemCodeRef: String,
    val formulaRef: String,
    val variantNote: String,
    val compatibleProducts: String,
    val licenceNotice: String,
    val isLicensedData: Boolean = false
)

@Entity(
    tableName = "technician_formulas",
    indices = [
        Index(value = ["oemCodeRef"]),
        Index(value = ["updatedAt"])
    ]
)
data class TechnicianFormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val oemCodeRef: String,
    val baseSystem: String,
    val tonerNotes: String,
    val mixingRatio: String,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vehicle_records",
    indices = [
        Index(value = ["plateOrLabel"]),
        Index(value = ["make", "model"])
    ]
)
data class VehicleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plateOrLabel: String,
    val make: String,
    val model: String,
    val year: String,
    val bodyType: String,
    val ownerName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "paint_jobs",
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["oemCode"]),
        Index(value = ["status"]),
        Index(value = ["updatedAt"])
    ]
)
data class PaintJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long = 0,
    val vehicleLabel: String,
    val oemCode: String,
    val colourName: String,
    val finish: String,
    val status: String, // Quoted, Prep, Basecoat, Clearcoat, Completed, Rework
    val formulaNotes: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "spray_outs",
    indices = [
        Index(value = ["jobId"])
    ]
)
data class SprayOutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val jobLabel: String,
    val panelName: String,
    val lightingCondition: String, // Sunlight, Shade, Booth LED
    val outcome: String, // ACCEPTED, ADJUSTED, REJECTED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_items",
    indices = [
        Index(value = ["category"]),
        Index(value = ["productCode"]),
        Index(value = ["name"])
    ]
)
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Colour Material, Toner, Preparation, Finishing, Consumable, Equipment
    val name: String,
    val brand: String,
    val productCode: String,
    val unit: String, // L, ml, kg, pcs, sheets
    val quantity: Double,
    val unitCostKsh: Double,
    val supplier: String,
    val batchLot: String = "",
    val acquiredDate: String = "",
    val reorderThreshold: Double = 1.0
)

@Entity(
    tableName = "stock_usage",
    indices = [
        Index(value = ["stockItemId"]),
        Index(value = ["jobId"]),
        Index(value = ["usedAt"])
    ]
)
data class StockUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stockItemId: Long,
    val stockItemName: String,
    val jobId: Long,
    val jobTitle: String,
    val quantityUsed: Double,
    val costKsh: Double,
    val unit: String,
    val notes: String = "",
    val usedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "car_photos",
    indices = [
        Index(value = ["jobId"]),
        Index(value = ["createdAt"])
    ]
)
data class CarPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val vehicleLabel: String = "",
    val jobId: Long? = null,
    val capturedWithCamera: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "studio_projects",
    indices = [
        Index(value = ["mode"]),
        Index(value = ["updatedAt"])
    ]
)
data class StudioProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String, // PHOTO, LIVERY
    val vehicleLabel: String,
    val templateId: String = "PROBOX",
    val viewAngle: String = "SIDE",
    val photoPath: String = "",
    val maskPath: String = "",
    val liveryJson: String = "",
    val selectedHex: String = "#1F7",
    val selectedFinish: String = "Metallic",
    val updatedAt: Long = System.currentTimeMillis()
)
