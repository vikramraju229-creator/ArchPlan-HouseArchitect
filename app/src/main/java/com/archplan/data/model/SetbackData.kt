package com.archplan.data.model

import kotlinx.serialization.Serializable

/**
 * Setback (offset) distances from plot boundaries and wall thickness.
 */
@Serializable
data class SetbackData(
    val front: Float = 0f,
    val rear: Float = 0f,
    val left: Float = 0f,
    val right: Float = 0f,
    val wallThicknessInches: Float = 9f
) {
    /** Usable width after subtracting left+right setbacks. */
    fun usableWidth(plotBreadth: Float): Float =
        (plotBreadth - left - right).coerceAtLeast(0f)

    /** Usable depth after subtracting front+rear setbacks. */
    fun usableDepth(plotLength: Float): Float =
        (plotLength - front - rear).coerceAtLeast(0f)

    /** House footprint area. */
    fun houseFootprint(plotLength: Float, plotBreadth: Float): Float =
        usableWidth(plotBreadth) * usableDepth(plotLength)

    /** Coverage percentage of plot. */
    fun coveragePercent(plotLength: Float, plotBreadth: Float): Float {
        val plotArea = plotLength * plotBreadth
        if (plotArea <= 0f) return 0f
        return (houseFootprint(plotLength, plotBreadth) / plotArea) * 100f
    }

    /** True if any setback exceeds 30% of its dimension (warning). */
    fun hasExcessiveSetback(plotLength: Float, plotBreadth: Float): Boolean =
        front > plotLength * 0.3f ||
        rear > plotLength * 0.3f ||
        left > plotBreadth * 0.3f ||
        right > plotBreadth * 0.3f
}
