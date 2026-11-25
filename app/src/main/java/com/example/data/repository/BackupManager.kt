package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.CarPhotoEntity
import com.example.data.local.MotoopaintDao
import com.example.data.local.PaintJobEntity
import com.example.data.local.SprayOutEntity
import com.example.data.local.StockItemEntity
import com.example.data.local.StockUsageEntity
import com.example.data.local.StudioProjectEntity
import com.example.data.local.TechnicianFormulaEntity
import com.example.data.local.VehicleRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val formulasCount: Int,
    val jobsCount: Int,
    val stockCount: Int,
    val projectsCount: Int
)

class BackupManager(
    private val context: Context,
    private val dao: MotoopaintDao
) {
    suspend fun exportToUri(uri: Uri): Result<BackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val formulas = dao.getAllFormulasSnapshot()
            val vehicles = dao.getAllVehicleRecordsSnapshot()
            val jobs = dao.getAllPaintJobsSnapshot()
            val sprayOuts = dao.getAllSprayOutsSnapshot()
            val stock = dao.getAllStockItemsSnapshot()
            val usage = dao.getAllStockUsageSnapshot()
            val projects = dao.getAllStudioProjectsSnapshot()
            val photos = dao.getAllCarPhotosSnapshot()

            val root = JSONObject().apply {
                put("app", "Motoopaint")
                put("schemaVersion", 1)
                put("exportedAt", System.currentTimeMillis())

                put("formulas", JSONArray().apply {
                    formulas.forEach { f ->
                        put(JSONObject().apply {
                            put("id", f.id)
                            put("title", f.title)
                            put("oemCodeRef", f.oemCodeRef)
                            put("baseSystem", f.baseSystem)
                            put("tonerNotes", f.tonerNotes)
                            put("mixingRatio", f.mixingRatio)
                            put("notes", f.notes)
                            put("updatedAt", f.updatedAt)
                        })
                    }
                })

                put("vehicles", JSONArray().apply {
                    vehicles.forEach { v ->
                        put(JSONObject().apply {
                            put("id", v.id)
                            put("plateOrLabel", v.plateOrLabel)
                            put("make", v.make)
                            put("model", v.model)
                            put("year", v.year)
                            put("bodyType", v.bodyType)
                            put("ownerName", v.ownerName)
                            put("notes", v.notes)
                            put("createdAt", v.createdAt)
                        })
                    }
                })

                put("jobs", JSONArray().apply {
                    jobs.forEach { j ->
                        put(JSONObject().apply {
                            put("id", j.id)
                            put("vehicleId", j.vehicleId)
                            put("vehicleLabel", j.vehicleLabel)
                            put("oemCode", j.oemCode)
                            put("colourName", j.colourName)
                            put("finish", j.finish)
                            put("status", j.status)
                            put("formulaNotes", j.formulaNotes)
                            put("notes", j.notes)
                            put("createdAt", j.createdAt)
                            put("updatedAt", j.updatedAt)
                        })
                    }
                })

                put("sprayOuts", JSONArray().apply {
                    sprayOuts.forEach { s ->
                        put(JSONObject().apply {
                            put("id", s.id)
                            put("jobId", s.jobId)
                            put("jobLabel", s.jobLabel)
                            put("panelName", s.panelName)
                            put("lightingCondition", s.lightingCondition)
                            put("outcome", s.outcome)
                            put("notes", s.notes)
                            put("createdAt", s.createdAt)
                        })
                    }
                })

                put("stock", JSONArray().apply {
                    stock.forEach { st ->
                        put(JSONObject().apply {
                            put("id", st.id)
                            put("category", st.category)
                            put("name", st.name)
                            put("brand", st.brand)
                            put("productCode", st.productCode)
                            put("unit", st.unit)
                            put("quantity", st.quantity)
                            put("unitCostKsh", st.unitCostKsh)
                            put("supplier", st.supplier)
                            put("batchLot", st.batchLot)
                            put("acquiredDate", st.acquiredDate)
                            put("reorderThreshold", st.reorderThreshold)
                        })
                    }
                })

                put("stockUsage", JSONArray().apply {
                    usage.forEach { u ->
                        put(JSONObject().apply {
                            put("id", u.id)
                            put("stockItemId", u.stockItemId)
                            put("stockItemName", u.stockItemName)
                            put("jobId", u.jobId)
                            put("jobTitle", u.jobTitle)
                            put("quantityUsed", u.quantityUsed)
                            put("costKsh", u.costKsh)
                            put("unit", u.unit)
                            put("notes", u.notes)
                            put("usedAt", u.usedAt)
                        })
                    }
                })

                put("projects", JSONArray().apply {
                    projects.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id)
                            put("title", p.title)
                            put("mode", p.mode)
                            put("vehicleLabel", p.vehicleLabel)
                            put("templateId", p.templateId)
                            put("viewAngle", p.viewAngle)
                            put("photoPath", p.photoPath)
                            put("maskPath", p.maskPath)
                            put("liveryJson", p.liveryJson)
                            put("selectedHex", p.selectedHex)
                            put("selectedFinish", p.selectedFinish)
                            put("updatedAt", p.updatedAt)
                        })
                    }
                })

                put("carPhotos", JSONArray().apply {
                    photos.forEach { ph ->
                        put(JSONObject().apply {
                            put("id", ph.id)
                            put("title", ph.title)
                            put("filePath", ph.filePath)
                            put("vehicleLabel", ph.vehicleLabel)
                            put("capturedWithCamera", ph.capturedWithCamera)
                            put("createdAt", ph.createdAt)
                        })
                    }
                })
            }

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(root.toString(2).toByteArray(Charsets.UTF_8))
            } ?: error("Unable to open backup destination")

            BackupSummary(
                formulasCount = formulas.size,
                jobsCount = jobs.size,
                stockCount = stock.size,
                projectsCount = projects.size
            )
        }
    }

    suspend fun restoreFromUri(uri: Uri): Result<BackupSummary> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Unable to read backup file")
            val root = JSONObject(raw)
            require(root.optString("app") == "Motoopaint") { "Not a valid Motoopaint backup file" }

            val formulas = parseList(root.optJSONArray("formulas")) { obj ->
                TechnicianFormulaEntity(
                    id = obj.optLong("id"),
                    title = obj.getString("title"),
                    oemCodeRef = obj.optString("oemCodeRef", "CUSTOM"),
                    baseSystem = obj.optString("baseSystem", "2K Basecoat"),
                    tonerNotes = obj.optString("tonerNotes", ""),
                    mixingRatio = obj.optString("mixingRatio", ""),
                    notes = obj.optString("notes", ""),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
            val vehicles = parseList(root.optJSONArray("vehicles")) { obj ->
                VehicleRecordEntity(
                    id = obj.optLong("id"),
                    plateOrLabel = obj.getString("plateOrLabel"),
                    make = obj.optString("make", ""),
                    model = obj.optString("model", ""),
                    year = obj.optString("year", ""),
                    bodyType = obj.optString("bodyType", "Station Wagon"),
                    ownerName = obj.optString("ownerName", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
            val jobs = parseList(root.optJSONArray("jobs")) { obj ->
                PaintJobEntity(
                    id = obj.optLong("id"),
                    vehicleId = obj.optLong("vehicleId"),
                    vehicleLabel = obj.getString("vehicleLabel"),
                    oemCode = obj.optString("oemCode", ""),
                    colourName = obj.optString("colourName", ""),
                    finish = obj.optString("finish", "Solid"),
                    status = obj.optString("status", "Quoted"),
                    formulaNotes = obj.optString("formulaNotes", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
            val sprayOuts = parseList(root.optJSONArray("sprayOuts")) { obj ->
                SprayOutEntity(
                    id = obj.optLong("id"),
                    jobId = obj.optLong("jobId"),
                    jobLabel = obj.optString("jobLabel", ""),
                    panelName = obj.optString("panelName", ""),
                    lightingCondition = obj.optString("lightingCondition", "Sunlight"),
                    outcome = obj.optString("outcome", "ACCEPTED"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
            val stock = parseList(root.optJSONArray("stock")) { obj ->
                StockItemEntity(
                    id = obj.optLong("id"),
                    category = obj.optString("category", "Colour Material"),
                    name = obj.getString("name"),
                    brand = obj.optString("brand", ""),
                    productCode = obj.optString("productCode", ""),
                    unit = obj.optString("unit", "L"),
                    quantity = obj.optDouble("quantity", 0.0),
                    unitCostKsh = obj.optDouble("unitCostKsh", 0.0),
                    supplier = obj.optString("supplier", ""),
                    batchLot = obj.optString("batchLot", ""),
                    acquiredDate = obj.optString("acquiredDate", ""),
                    reorderThreshold = obj.optDouble("reorderThreshold", 1.0)
                )
            }
            val usage = parseList(root.optJSONArray("stockUsage")) { obj ->
                StockUsageEntity(
                    id = obj.optLong("id"),
                    stockItemId = obj.optLong("stockItemId"),
                    stockItemName = obj.optString("stockItemName", ""),
                    jobId = obj.optLong("jobId"),
                    jobTitle = obj.optString("jobTitle", ""),
                    quantityUsed = obj.optDouble("quantityUsed", 0.0),
                    costKsh = obj.optDouble("costKsh", 0.0),
                    unit = obj.optString("unit", "L"),
                    notes = obj.optString("notes", ""),
                    usedAt = obj.optLong("usedAt", System.currentTimeMillis())
                )
            }
            val projects = parseList(root.optJSONArray("projects")) { obj ->
                StudioProjectEntity(
                    id = obj.optLong("id"),
                    title = obj.getString("title"),
                    mode = obj.optString("mode", "LIVERY"),
                    vehicleLabel = obj.optString("vehicleLabel", ""),
                    templateId = obj.optString("templateId", "PROBOX"),
                    viewAngle = obj.optString("viewAngle", "SIDE"),
                    photoPath = obj.optString("photoPath", ""),
                    maskPath = obj.optString("maskPath", ""),
                    liveryJson = obj.optString("liveryJson", ""),
                    selectedHex = obj.optString("selectedHex", "#1F7"),
                    selectedFinish = obj.optString("selectedFinish", "Metallic"),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
            val photos = parseList(root.optJSONArray("carPhotos")) { obj ->
                CarPhotoEntity(
                    id = obj.optLong("id"),
                    title = obj.optString("title", "Vehicle Photo"),
                    filePath = obj.optString("filePath", ""),
                    vehicleLabel = obj.optString("vehicleLabel", ""),
                    capturedWithCamera = obj.optBoolean("capturedWithCamera", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }

            dao.replaceUserBackupData(
                formulas = formulas,
                vehicles = vehicles,
                jobs = jobs,
                sprayOuts = sprayOuts,
                stock = stock,
                usage = usage,
                projects = projects,
                photos = photos
            )

            BackupSummary(
                formulasCount = formulas.size,
                jobsCount = jobs.size,
                stockCount = stock.size,
                projectsCount = projects.size
            )
        }
    }

    private inline fun <T> parseList(array: JSONArray?, mapper: (JSONObject) -> T): List<T> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                add(mapper(array.getJSONObject(i)))
            }
        }
    }
}
