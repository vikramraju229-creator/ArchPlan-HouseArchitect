package com.archplan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archplan.data.model.HousePlan
import com.archplan.data.model.RoomData
import com.archplan.data.repository.PlanRepository
import com.archplan.domain.usecase.CalculateAreasUseCase
import com.archplan.domain.usecase.ExportPlanUseCase
import com.archplan.domain.usecase.GeneratePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * ViewModel for the plan output / blueprint screen.
 */
@HiltViewModel
class PlanOutputViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val generatePlanUseCase: GeneratePlanUseCase,
    private val calculateAreasUseCase: CalculateAreasUseCase,
    private val exportPlanUseCase: ExportPlanUseCase
) : ViewModel() {

    var housePlan by mutableStateOf<HousePlan?>(null)
        private set
    var generatedRooms by mutableStateOf<List<RoomData>>(emptyList())
        private set
    var houseWidth by mutableStateOf(0f)
        private set
    var houseHeight by mutableStateOf(0f)
        private set
    var areaResult by mutableStateOf<CalculateAreasUseCase.AreaResult?>(null)
        private set
    var vastuReport by mutableStateOf<GeneratePlanUseCase.VastuReport?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isExporting by mutableStateOf(false)
        private set
    var exportMessage by mutableStateOf<String?>(null)
        private set
    var selectedRoomIndex by mutableIntStateOf(-1)
        private set

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun loadPlan(planId: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                val saved = planRepository.getPlanById(planId)
                if (saved != null) {
                    val plan = json.decodeFromString<HousePlan>(saved.planJson)
                    housePlan = plan

                    val generated = generatePlanUseCase(plan)
                    generatedRooms = generated.rooms
                    houseWidth = generated.houseWidth
                    houseHeight = generated.houseHeight
                    vastuReport = generated.vastuReport

                    areaResult = calculateAreasUseCase(plan)
                } else {
                    errorMessage = "Plan not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load plan: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRoom(index: Int) {
        selectedRoomIndex = if (selectedRoomIndex == index) -1 else index
    }

    fun sharePlan() {
        isExporting = true
        viewModelScope.launch {
            try {
                val plan = housePlan ?: return@launch
                val bitmap = exportPlanUseCase.exportToBitmap(
                    plan = plan,
                    rooms = generatedRooms,
                    houseWidth = houseWidth,
                    houseHeight = houseHeight
                )
                exportMessage = "Plan ready to share"
                isExporting = false
            } catch (e: Exception) {
                errorMessage = "Export failed: ${e.message}"
                isExporting = false
            }
        }
    }

    fun exportPdf() {
        isExporting = true
        viewModelScope.launch {
            try {
                val plan = housePlan ?: return@launch
                exportPlanUseCase.exportToPdf(
                    plan = plan,
                    rooms = generatedRooms,
                    houseWidth = houseWidth,
                    houseHeight = houseHeight,
                    onComplete = { file ->
                        exportMessage = "PDF saved: ${file.name}"
                        isExporting = false
                    },
                    onError = { e ->
                        errorMessage = "PDF export failed: ${e.message}"
                        isExporting = false
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Export failed: ${e.message}"
                isExporting = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        exportMessage = null
    }
}

// Needed for the selectedRoomIndex
private fun mutableIntStateOf(default: Int) = androidx.compose.runtime.mutableIntStateOf(default)
