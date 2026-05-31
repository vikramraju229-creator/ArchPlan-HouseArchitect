package com.archplan.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archplan.data.model.SavedPlan
import com.archplan.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the saved plans list screen.
 */
@HiltViewModel
class SavedPlansViewModel @Inject constructor(
    private val planRepository: PlanRepository
) : ViewModel() {

    val plans: StateFlow<List<SavedPlan>> = planRepository.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var searchQuery by mutableStateOf("")
        private set

    var isDeleting by mutableStateOf(false)
        private set

    var showDeleteConfirm by mutableStateOf<SavedPlan?>(null)
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun requestDelete(plan: SavedPlan) {
        showDeleteConfirm = plan
    }

    fun confirmDelete() {
        val plan = showDeleteConfirm ?: return
        isDeleting = true
        viewModelScope.launch {
            planRepository.deletePlan(plan)
            isDeleting = false
            showDeleteConfirm = null
        }
    }

    fun cancelDelete() {
        showDeleteConfirm = null
    }
}
