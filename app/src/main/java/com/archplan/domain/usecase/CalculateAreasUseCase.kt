package com.archplan.domain.usecase

import com.archplan.data.model.HousePlan
import com.archplan.data.model.UnitType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates all area metrics for a given house plan.
 */
@Singleton
class CalculateAreasUseCase @Inject constructor() {

    data class AreaResult(
        val plotAreaSqFt: Float,
        val houseFootprintSqFt: Float,
        val totalRoomAreaSqFt: Float,
        val freeSpaceSqFt: Float,
        val coveragePercent: Float,
        val plotAreaLabel: String,
        val houseFootprintLabel: String,
        val totalRoomAreaLabel: String,
        val freeSpaceLabel: String
    )

    operator fun invoke(plan: HousePlan): AreaResult {
        val plotData = plan.plotData
        val unit = plotData.unit

        val plotLenFt = if (unit == UnitType.METERS) plotData.length * 3.28084f else plotData.length
        val plotBrdFt = if (unit == UnitType.METERS) plotData.breadth * 3.28084f else plotData.breadth
        val plotArea = plotLenFt * plotBrdFt

        val usableW = plan.setbackData.usableWidth(plotBrdFt)
        val usableD = plan.setbackData.usableDepth(plotLenFt)
        val footprint = usableW * usableD

        val roomArea = plan.totalRoomArea
        val free = (footprint - roomArea).coerceAtLeast(0f)
        val coverage = if (footprint > 0f) (roomArea / footprint) * 100f else 0f

        fun fmt(v: Float) = "%.0f".format(v)

        return AreaResult(
            plotAreaSqFt = plotArea,
            houseFootprintSqFt = footprint,
            totalRoomAreaSqFt = roomArea,
            freeSpaceSqFt = free,
            coveragePercent = coverage,
            plotAreaLabel = "${fmt(plotArea)} sq ft",
            houseFootprintLabel = "${fmt(footprint)} sq ft",
            totalRoomAreaLabel = "${fmt(roomArea)} sq ft",
            freeSpaceLabel = "${fmt(free)} sq ft"
        )
    }
}
