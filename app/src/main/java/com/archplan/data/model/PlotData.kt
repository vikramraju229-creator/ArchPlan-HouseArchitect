package com.archplan.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the plot / land dimensions and orientation.
 */
@Serializable
data class PlotData(
    val length: Float = 0f,
    val breadth: Float = 0f,
    val unit: UnitType = UnitType.FEET,
    val shape: PlotShape = PlotShape.RECTANGLE,
    val facing: FacingDirection = FacingDirection.NORTH
) {
    val areaSqFt: Float get() {
        val inFeet = if (unit == UnitType.METERS) {
            length * 3.28084f * (breadth * 3.28084f)
        } else {
            length * breadth
        }
        return inFeet
    }

    val areaSqMeters: Float get() {
        val inMeters = if (unit == UnitType.FEET) {
            length * 0.3048f * (breadth * 0.3048f)
        } else {
            length * breadth
        }
        return inMeters
    }
}

enum class UnitType { FEET, METERS }

enum class PlotShape { RECTANGLE, L_SHAPE, CORNER }

enum class FacingDirection {
    NORTH, EAST, SOUTH, WEST;

    val displayName: String get() = when (this) {
        NORTH -> "N"
        EAST -> "E"
        SOUTH -> "S"
        WEST -> "W"
    }
}
