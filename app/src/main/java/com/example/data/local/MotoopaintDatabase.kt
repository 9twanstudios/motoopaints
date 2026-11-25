package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VehicleModelEntity::class,
        OemPaintEntity::class,
        StandardColourEntity::class,
        RefinishSystemEntity::class,
        TechnicianFormulaEntity::class,
        VehicleRecordEntity::class,
        PaintJobEntity::class,
        SprayOutEntity::class,
        StockItemEntity::class,
        StockUsageEntity::class,
        CarPhotoEntity::class,
        StudioProjectEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MotoopaintDatabase : RoomDatabase() {
    abstract fun dao(): MotoopaintDao

    companion object {
        @Volatile
        private var INSTANCE: MotoopaintDatabase? = null

        fun getInstance(context: Context): MotoopaintDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MotoopaintDatabase::class.java,
                    "motoopaint_offline.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}
