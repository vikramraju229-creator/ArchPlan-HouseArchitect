package com.archplan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archplan.data.model.FacingDirection
import com.archplan.data.model.GardenLawnArea
import com.archplan.data.model.HouseConfig
import com.archplan.data.model.HousePlan
import com.archplan.data.model.HouseType
import com.archplan.data.model.ParkingType
import com.archplan.data.model.PlotData
import com.archplan.data.model.PlotShape
import com.archplan.data.model.RoomData
import com.archplan.data.model.RoomType
import com.archplan.data.model.SetbackData
import com.archplan.data.model.StaircasePosition
import com.archplan.data.model.UnitType
import com.archplan.data.repository.PlanRepository
import com.archplan.domain.usecase.GeneratePlanUseCase
import com.archplan.domain.usecase.ValidatePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * ViewModel for the plan input wizard (4 steps).
 * Manages all form state, validation, and plan generation.
 */
@HiltViewModel
class PlanInputViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val generatePlanUseCase: GeneratePlanUseCase,
    private val validatePlanUseCase: ValidatePlanUseCase
) : ViewModel() {

    // ── Wizard step ──────────────────────────────────────────────────────
    var currentStep by mutableIntStateOf(0)
        private set

    // ── Step 1: Plot Dimensions ──────────────────────────────────────────
    var plotLength by mutableFloatStateOf(40f)
    var plotBreadth by mutableFloatStateOf(30f)
    var plotUnit by mutableStateOf(UnitType.FEET)
    var plotShape by mutableStateOf(PlotShape.RECTANGLE)
    var facingDirection by mutableStateOf(FacingDirection.NORTH)

    // ── Step 2: Setbacks ─────────────────────────────────────────────────
    var frontSetback by mutableFloatStateOf(5f)
    var rearSetback by mutableFloatStateOf(3f)
    var leftSetback by mutableFloatStateOf(3f)
    var rightSetback by mutableFloatStateOf(3f)
    var wallThickness by mutableFloatStateOf(9f)

    // ── Step 3: House Configuration ──────────────────────────────────────
    var houseType by mutableStateOf(HouseType.BHK2)
    var floors by mutableIntStateOf(1)
    var staircasePosition by mutableStateOf(StaircasePosition.INTERNAL)
    var parkingType by mutableStateOf(ParkingType.NONE)
    var hasPoojaRoom by mutableStateOf(false)
    var gardenLawn by mutableStateOf(GardenLawnArea.NONE)

    // ── Step 4: Room List ────────────────────────────────────────────────
    var rooms by mutableStateOf<List<RoomData>>(emptyList())
        private set

    // ── UI State ─────────────────────────────────────────────────────────
    var planName by mutableStateOf("My Dream Home")
    var generatedPlanId by mutableStateOf<String?>(null)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var validationErrors by mutableStateOf<List<ValidatePlanUseCase.ValidationError>>(emptyList())
        private set
    var validationWarnings by mutableStateOf<List<ValidatePlanUseCase.ValidationWarning>>(emptyList())
        private set

    // Bottom sheet state for adding rooms
    var showRoomEditor by mutableStateOf(false)
    var editingRoomIndex by mutableIntStateOf(-1)
    var newRoomType by mutableStateOf(RoomType.BEDROOM)
    var newRoomWidth by mutableFloatStateOf(10f)
    var newRoomHeight by mutableFloatStateOf(12f)

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    init {
        // Auto-populate rooms based on default house type
        autoPopulateRooms()
    }

    // ── Step Navigation ──────────────────────────────────────────────────

    fun nextStep(): Boolean {
        if (!validateCurrentStep()) return false
        if (currentStep < 3) {
            currentStep++
            if (currentStep == 3) autoPopulateRooms()
        }
        return true
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    fun goToStep(step: Int) {
        if (step in 0..3 && step <= currentStep + 1) {
            currentStep = step
            // Auto-populate rooms when entering the Room Planner step
            if (step == 3) autoPopulateRooms()
        }
    }

    // ── Validation ───────────────────────────────────────────────────────

    private fun validateCurrentStep(): Boolean {
        errorMessage = null
        return when (currentStep) {
            0 -> validatePlotStep()
            1 -> validateSetbackStep()
            2 -> validateConfigStep()
            3 -> validateRoomsStep()
            else -> true
        }
    }

    private fun validatePlotStep(): Boolean {
        return when {
            plotLength <= 0f -> { errorMessage = "Plot length must be greater than 0"; false }
            plotBreadth <= 0f -> { errorMessage = "Plot breadth must be greater than 0"; false }
            plotLength > 500f -> { errorMessage = "Plot length seems too large (max 500 ft)"; false }
            plotBreadth > 500f -> { errorMessage = "Plot breadth seems too large (max 500 ft)"; false }
            else -> true
        }
    }

    private fun validateSetbackStep(): Boolean {
        val lenInFt = if (plotUnit == UnitType.METERS) plotLength * 3.28084f else plotLength
        val brdInFt = if (plotUnit == UnitType.METERS) plotBreadth * 3.28084f else plotBreadth
        return when {
            frontSetback < 0f || rearSetback < 0f || leftSetback < 0f || rightSetback < 0f ->
                { errorMessage = "Setbacks cannot be negative"; false }
            frontSetback + rearSetback >= lenInFt ->
                { errorMessage = "Front + rear setbacks exceed plot length"; false }
            leftSetback + rightSetback >= brdInFt ->
                { errorMessage = "Left + right setbacks exceed plot width"; false }
            else -> true
        }
    }

    private fun validateConfigStep(): Boolean = true

    private fun validateRoomsStep(): Boolean {
        return when {
            rooms.isEmpty() -> { errorMessage = "Add at least one room"; false }
            else -> true
        }
    }

    // ── Room Management ──────────────────────────────────────────────────

    fun autoPopulateRooms() {
        val defaultRooms = houseType.defaultRooms.map { type ->
            val dims = getDefaultDimensions(type)
            RoomData(
                name = type.displayName,
                type = type,
                width = dims.first,
                height = dims.second
            )
        }
        rooms = defaultRooms
    }

    private fun getDefaultDimensions(type: RoomType): Pair<Float, Float> = when (type) {
        RoomType.BEDROOM -> 12f to 14f
        RoomType.LIVING_ROOM -> 16f to 20f
        RoomType.KITCHEN -> 10f to 12f
        RoomType.DINING -> 10f to 14f
        RoomType.BATHROOM -> 6f to 8f
        RoomType.TOILET -> 4f to 6f
        RoomType.STUDY -> 8f to 10f
        RoomType.BALCONY -> 6f to 10f
        RoomType.STAIRCASE -> 5f to 10f
        RoomType.STORE -> 5f to 6f
        RoomType.SERVANT_QUARTER -> 8f to 10f
        RoomType.GARAGE -> 10f to 18f
        RoomType.POOJA_ROOM -> 6f to 6f
        RoomType.LOBBY -> 5f to 8f
    }

    fun openAddRoomSheet() {
        editingRoomIndex = -1
        newRoomType = RoomType.BEDROOM
        newRoomWidth = 10f
        newRoomHeight = 12f
        showRoomEditor = true
    }

    fun openEditRoomSheet(index: Int) {
        val room = rooms.getOrNull(index) ?: return
        editingRoomIndex = index
        newRoomType = room.type
        newRoomWidth = room.width
        newRoomHeight = room.height
        showRoomEditor = true
    }

    fun addOrUpdateRoom() {
        val room = RoomData(
            name = newRoomType.displayName,
            type = newRoomType,
            width = newRoomWidth,
            height = newRoomHeight
        )
        if (editingRoomIndex >= 0 && editingRoomIndex < rooms.size) {
            rooms = rooms.toMutableList().apply { set(editingRoomIndex, room) }
        } else {
            rooms = rooms + room
        }
        showRoomEditor = false
    }

    fun deleteRoom(index: Int) {
        if (index in rooms.indices) {
            rooms = rooms.toMutableList().apply { removeAt(index) }
        }
    }

    fun moveRoom(fromIndex: Int, toIndex: Int) {
        val mutable = rooms.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        rooms = mutable
    }

    // ── Plan Generation ──────────────────────────────────────────────────

    fun generatePlan() {
        if (!validateCurrentStep()) return

        isGenerating = true
        errorMessage = null

        val plotData = PlotData(
            length = plotLength,
            breadth = plotBreadth,
            unit = plotUnit,
            shape = plotShape,
            facing = facingDirection
        )

        val setbackData = SetbackData(
            front = frontSetback,
            rear = rearSetback,
            left = leftSetback,
            right = rightSetback,
            wallThicknessInches = wallThickness
        )

        val houseConfig = HouseConfig(
            houseType = houseType,
            floors = floors,
            staircasePosition = staircasePosition,
            parking = parkingType,
            poojaRoom = hasPoojaRoom,
            gardenLawn = gardenLawn
        )

        val housePlan = HousePlan(
            name = planName.ifBlank { "Untitled Plan" },
            plotData = plotData,
            setbackData = setbackData,
            houseConfig = houseConfig,
            rooms = rooms
        )

        // Validate
        val validation = validatePlanUseCase(housePlan)
        validationErrors = validation.errors
        validationWarnings = validation.warnings

        if (!validation.isValid) {
            errorMessage = validation.errors.firstOrNull()?.message
                ?: "Plan validation failed. Try reducing room sizes or increasing the plot."
            isGenerating = false
            return
        }

        // Generate
        viewModelScope.launch {
            try {
                val generated = generatePlanUseCase(housePlan)

                val finalPlan = housePlan.copy(
                    rooms = generated.rooms,
                    updatedAt = System.currentTimeMillis()
                )

                val planJson = json.encodeToString(finalPlan)
                val plotSize = "${plotLength.toInt()}x${plotBreadth.toInt()} ${plotUnit.name.lowercase()}"
                val savedPlan = com.archplan.data.model.SavedPlan(
                    id = finalPlan.id,
                    name = finalPlan.name,
                    planJson = planJson,
                    plotSize = plotSize,
                    houseType = houseType.displayName,
                    coverage = finalPlan.coveragePercent
                )

                planRepository.savePlan(savedPlan)
                generatedPlanId = finalPlan.id
                isGenerating = false
            } catch (e: Exception) {
                errorMessage = "Failed to generate plan: ${e.message}"
                isGenerating = false
            }
        }
    }
}
