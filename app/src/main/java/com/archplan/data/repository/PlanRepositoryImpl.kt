package com.archplan.data.repository

import com.archplan.data.local.PlanDao
import com.archplan.data.model.SavedPlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao
) : PlanRepository {

    override fun getAllPlans(): Flow<List<SavedPlan>> = planDao.getAllPlans()

    override fun searchPlans(query: String): Flow<List<SavedPlan>> =
        planDao.searchPlans(query)

    override suspend fun getPlanById(id: String): SavedPlan? =
        planDao.getPlanById(id)

    override suspend fun savePlan(plan: SavedPlan) =
        planDao.insertPlan(plan)

    override suspend fun deletePlan(plan: SavedPlan) =
        planDao.deletePlan(plan)

    override suspend fun deletePlanById(id: String) =
        planDao.deletePlanById(id)

    override suspend fun getPlanCount(): Int =
        planDao.getPlanCount()
}
