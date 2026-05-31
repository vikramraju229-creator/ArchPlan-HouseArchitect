package com.archplan.domain.usecase

import com.archplan.data.model.HousePlan
import com.archplan.data.model.RoomType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates a house plan against minimum dimension and compliance rules.
 */
@Singleton
class ValidatePlanUseCase @Inject constructor() {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError> = emptyList(),
        val warnings: List<ValidationWarning> = emptyList()
    )

    data class ValidationError(
        val message: String,
        val field: String = ""
    )

    data class ValidationWarning(
        val message: String,
        val field: String = ""
    )

    /** Minimum room dimensions by type (width, length in ft). */
    private val minDimensions = mapOf(
        RoomType.BEDROOM to 8f,
        RoomType.LIVING_ROOM to 10f,
        RoomType.KITCHEN to 6f,
        RoomType.DINING to 8f,
        RoomType.BATHROOM to 4f,
        RoomType.TOILET to 3f,
        RoomType.STUDY to 6f,
        RoomType.BALCONY to 3f,
        RoomType.STAIRCASE to 4f,
        RoomType.STORE to 4f,
        RoomType.SERVANT_QUARTER to 6f,
        RoomType.GARAGE to 9f,
        RoomType.POOJA_ROOM to 5f,
        RoomType.LOBBY to 4f
    )

    operator fun invoke(plan: HousePlan): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<ValidationWarning>()

        // 1. Validate plot dimensions
        if (plan.plotData.length <= 0f || plan.plotData.breadth <= 0f) {
            errors.add(ValidationError("Plot dimensions must be greater than zero", "plot"))
        }

        // 2. Validate setbacks
        if (plan.setbackData.front < 0f || plan.setbackData.rear < 0f ||
            plan.setbackData.left < 0f || plan.setbackData.right < 0f) {
            errors.add(ValidationError("Setbacks cannot be negative", "setback"))
        }

        val plotLen = plan.plotData.length
        val plotBrd = plan.plotData.breadth
        if (plan.setbackData.left + plan.setbackData.right >= plotBrd) {
            errors.add(ValidationError("Combined side setbacks exceed plot width", "setback"))
        }
        if (plan.setbackData.front + plan.setbackData.rear >= plotLen) {
            errors.add(ValidationError("Combined front+rear setbacks exceed plot length", "setback"))
        }

        if (plan.setbackData.hasExcessiveSetback(plotLen, plotBrd)) {
            warnings.add(ValidationWarning("A setback exceeds 30% of the dimension — consider reducing", "setback"))
        }

        // 3. Validate rooms
        if (plan.rooms.isEmpty()) {
            errors.add(ValidationError("At least one room is required", "rooms"))
        }

        for (room in plan.rooms) {
            val minDim = minDimensions[room.type] ?: 3f
            if (room.width < minDim) {
                warnings.add(
                    ValidationWarning(
                        "\"${room.name}\" width (${room.width} ft) is below recommended minimum of ${minDim} ft",
                        "room_${room.id}"
                    )
                )
            }
            if (room.height < minDim) {
                warnings.add(
                    ValidationWarning(
                        "\"${room.name}\" length (${room.height} ft) is below recommended minimum of ${minDim} ft",
                        "room_${room.id}"
                    )
                )
            }
        }

        // 4. Check total room area against buildable area
        if (plan.rooms.isNotEmpty() && plan.buildableArea > 0f && plan.totalRoomArea > plan.buildableArea) {
            errors.add(
                ValidationError(
                    "Total room area (${"%.0f".format(plan.totalRoomArea)} sq ft) exceeds buildable area (${"%.0f".format(plan.buildableArea)} sq ft)",
                    "rooms"
                )
            )
        }

        // 5. Coverage check
        if (plan.coveragePercent > 90f) {
            warnings.add(ValidationWarning("Coverage is ${"%.0f".format(plan.coveragePercent)}% — very high density"))
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}
