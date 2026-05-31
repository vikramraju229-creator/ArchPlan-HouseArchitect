package com.archplan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.archplan.data.model.SavedPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM saved_plans ORDER BY updatedAt DESC")
    fun getAllPlans(): Flow<List<SavedPlan>>

    @Query("SELECT * FROM saved_plans WHERE id = :id")
    suspend fun getPlanById(id: String): SavedPlan?

    @Query("SELECT * FROM saved_plans WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPlans(query: String): Flow<List<SavedPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: SavedPlan)

    @Update
    suspend fun updatePlan(plan: SavedPlan)

    @Delete
    suspend fun deletePlan(plan: SavedPlan)

    @Query("DELETE FROM saved_plans WHERE id = :id")
    suspend fun deletePlanById(id: String)

    @Query("SELECT COUNT(*) FROM saved_plans")
    suspend fun getPlanCount(): Int
}
