package com.archplan.di

import android.content.Context
import com.archplan.data.local.PlanDao
import com.archplan.data.local.PlanDatabase
import com.archplan.data.repository.PlanRepository
import com.archplan.data.repository.PlanRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePlanDatabase(@ApplicationContext context: Context): PlanDatabase =
        PlanDatabase.getInstance(context)

    @Provides
    @Singleton
    fun providePlanDao(database: PlanDatabase): PlanDao = database.planDao()

    @Provides
    @Singleton
    fun providePlanRepository(impl: PlanRepositoryImpl): PlanRepository = impl
}
