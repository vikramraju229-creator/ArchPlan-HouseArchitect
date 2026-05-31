package com.archplan.data.repository

import com.archplan.data.model.SavedPlan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun getAllPlans(): Flow<List<SavedPlan>>
    fun searchPlans(query: String): Flow<List<SavedPlan>>
    suspend fun getPlanById(id: String): SavedPlan?
    suspend fun savePlan(plan: SavedPlan)
    suspend fun deletePlan(plan: SavedPlan)
    suspend fun deletePlanById(id: String)
    suspend fun getPlanCount(): Int
}
