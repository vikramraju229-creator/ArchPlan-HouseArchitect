package com.archplan.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for persisting house plans in the local database.
 */
@Entity(tableName = "saved_plans")
data class SavedPlan(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Untitled Plan",
    val planJson: String = "",           // Serialized HousePlan
    val thumbnailPath: String = "",      // Path to cached blueprint image
    val plotSize: String = "",           // e.g. "40x60 ft"
    val houseType: String = "",          // e.g. "3 BHK"
    val coverage: Float = 0f,            // Coverage percentage
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
