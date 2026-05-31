package com.archplan.data.model

import kotlinx.serialization.Serializable

/**
 * Aggregate model representing a complete house plan.
 * Combines plot data, setbacks, house config, and room list.
 */
@Serializable
data class HousePlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Untitled Plan",
    val plotData: PlotData = PlotData(),
    val setbackData: SetbackData = SetbackData(),
    val houseConfig: HouseConfig = HouseConfig(),
    val rooms: List<RoomData> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Total area of all placed rooms. */
    val totalRoomArea: Float get() = rooms.sumOf { it.area.toDouble() }.toFloat()

    /** Available buildable area from setback calculations. */
    val buildableArea: Float get() {
        val plotLen = if (plotData.unit == UnitType.METERS) plotData.length * 3.28084f else plotData.length
        val plotBrd = if (plotData.unit == UnitType.METERS) plotData.breadth * 3.28084f else plotData.breadth
        return setbackData.houseFootprint(plotLen, plotBrd)
    }

    /** Free area after placing all rooms. */
    val freeArea: Float get() = (buildableArea - totalRoomArea).coerceAtLeast(0f)

    /** Coverage percentage. */
    val coveragePercent: Float get() {
        if (buildableArea <= 0f) return 0f
        return (totalRoomArea / buildableArea) * 100f
    }
}
