package com.archplan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.archplan.data.model.FacingDirection
import com.archplan.data.model.GardenLawnArea
import com.archplan.data.model.HouseType
import com.archplan.data.model.ParkingType
import com.archplan.data.model.PlotShape
import com.archplan.data.model.RoomType
import com.archplan.data.model.StaircasePosition
import com.archplan.data.model.UnitType
import com.archplan.ui.components.CompassPicker
import com.archplan.ui.components.RoomCard
import com.archplan.ui.components.RoomTypeGrid
import com.archplan.ui.components.StepIndicator
import com.archplan.ui.components.UnitToggle
import com.archplan.ui.theme.AmberAccent
import com.archplan.ui.theme.BlueprintBg
import com.archplan.ui.theme.BlueprintLine
import com.archplan.ui.theme.SuccessGreen
import com.archplan.ui.theme.WarningOrange
import com.archplan.ui.viewmodel.PlanInputViewModel

/**
 * Plan input screen with 4-step wizard:
 * 1. Plot Dimensions
 * 2. Compound & Setbacks
 * 3. House Configuration
 * 4. Room Planner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanInputScreen(
    onNavigateToOutput: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PlanInputViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Observe generated plan ID to navigate
    LaunchedEffect(viewModel.generatedPlanId) {
        viewModel.generatedPlanId?.let { planId ->
            onNavigateToOutput(planId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (viewModel.currentStep) {
                            0 -> "Plot Dimensions"
                            1 -> "Setbacks"
                            2 -> "House Config"
                            3 -> "Room Planner"
                            else -> "New Plan"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.currentStep > 0) viewModel.previousStep()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step indicator
            StepIndicator(
                currentStep = viewModel.currentStep,
                onStepClicked = { viewModel.goToStep(it) }
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { (viewModel.currentStep + 1) / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Error message
            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Animated step content
            AnimatedContent(
                targetState = viewModel.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "stepContent"
            ) { step ->
                Box(modifier = Modifier.weight(1f)) {
                    when (step) {
                        0 -> PlotDimensionsStep(viewModel)
                        1 -> SetbacksStep(viewModel)
                        2 -> HouseConfigStep(viewModel)
                        3 -> RoomPlannerStep(viewModel)
                    }
                }
            }

            // Bottom navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (viewModel.currentStep > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (viewModel.currentStep < 3) {
                    Button(
                        onClick = { viewModel.nextStep() }
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = { viewModel.generatePlan() },
                        enabled = !viewModel.isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent
                        )
                    ) {
                        if (viewModel.isGenerating) {
                            Text("Generating...")
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Plan")
                        }
                    }
                }
            }
        }
    }

    // Room editor bottom sheet
    if (viewModel.showRoomEditor) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.showRoomEditor = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (viewModel.editingRoomIndex >= 0) "Edit Room" else "Add Room",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Room type grid
                RoomTypeGrid(
                    selectedType = viewModel.newRoomType,
                    onTypeSelected = { viewModel.newRoomType = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dimensions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.newRoomWidth.let { if (it == 0f) "" else it.toInt().toString() },
                        onValueChange = { viewModel.newRoomWidth = it.toFloatOrNull() ?: 0f },
                        label = { Text("Width (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = viewModel.newRoomHeight.let { if (it == 0f) "" else it.toInt().toString() },
                        onValueChange = { viewModel.newRoomHeight = it.toFloatOrNull() ?: 0f },
                        label = { Text("Length (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Min dimension warning
                if (viewModel.newRoomType == RoomType.BEDROOM &&
                    (viewModel.newRoomWidth < 8f || viewModel.newRoomHeight < 8f)
                ) {
                    Text(
                        text = "⚠ Minimum dimension for bedroom is 8 ft",
                        color = WarningOrange,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.addOrUpdateRoom() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (viewModel.editingRoomIndex >= 0) "Update Room" else "Add Room"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Step 1: Plot Dimensions ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlotDimensionsStep(viewModel: PlanInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Unit toggle
        UnitToggle(
            selected = viewModel.plotUnit,
            onSelected = { viewModel.plotUnit = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Length & Breadth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.plotLength.let { if (it == 0f) "" else it.toInt().toString() },
                onValueChange = { viewModel.plotLength = it.toFloatOrNull() ?: 0f },
                label = { Text("Plot Length") },
                suffix = { Text(if (viewModel.plotUnit == UnitType.FEET) "ft" else "m") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = viewModel.plotBreadth.let { if (it == 0f) "" else it.toInt().toString() },
                onValueChange = { viewModel.plotBreadth = it.toFloatOrNull() ?: 0f },
                label = { Text("Plot Breadth") },
                suffix = { Text(if (viewModel.plotUnit == UnitType.FEET) "ft" else "m") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Plot shape
        Text(
            text = "Plot Shape",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PlotShape.entries.forEachIndexed { index, shape ->
                SegmentedButton(
                    selected = viewModel.plotShape == shape,
                    onClick = { viewModel.plotShape = shape },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PlotShape.entries.size
                    )
                ) {
                    Text(shape.name.replace("_", "-"))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Compass
        CompassPicker(
            selected = viewModel.facingDirection,
            onSelected = { viewModel.facingDirection = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live plot preview
        val len = viewModel.plotLength.coerceAtLeast(1f)
        val brd = viewModel.plotBreadth.coerceAtLeast(1f)
        val ratio = brd / len
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = BlueprintBg
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewW = 200f
                val previewH = previewW * ratio
                Box(
                    modifier = Modifier
                        .size(
                            width = previewW.dp.coerceAtMost(250.dp),
                            height = (previewW * ratio).dp.coerceAtMost(150.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(BlueprintLine.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "${len.toInt()} x ${brd.toInt()} ${if (viewModel.plotUnit == UnitType.FEET) "ft" else "m"}",
                        color = BlueprintLine,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ── Step 2: Setbacks ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetbacksStep(viewModel: PlanInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Compound & Setbacks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.frontSetback.let { if (it == 0f) "" else it.toInt().toString() },
            onValueChange = { viewModel.frontSetback = it.toFloatOrNull() ?: 0f },
            label = { Text("Front Setback (road side)") },
            suffix = { Text("ft") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.rearSetback.let { if (it == 0f) "" else it.toInt().toString() },
            onValueChange = { viewModel.rearSetback = it.toFloatOrNull() ?: 0f },
            label = { Text("Rear Setback") },
            suffix = { Text("ft") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.leftSetback.let { if (it == 0f) "" else it.toInt().toString() },
                onValueChange = { viewModel.leftSetback = it.toFloatOrNull() ?: 0f },
                label = { Text("Left Setback") },
                suffix = { Text("ft") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = viewModel.rightSetback.let { if (it == 0f) "" else it.toInt().toString() },
                onValueChange = { viewModel.rightSetback = it.toFloatOrNull() ?: 0f },
                label = { Text("Right Setback") },
                suffix = { Text("ft") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wall thickness
        Text(
            text = "Wall Thickness",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(4.5f, 9f, 13.5f).forEachIndexed { index, thick ->
                SegmentedButton(
                    selected = viewModel.wallThickness == thick,
                    onClick = { viewModel.wallThickness = thick },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = 3
                    )
                ) {
                    Text("${thick.toInt()} in")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Live calculation
        val lenInFt = if (viewModel.plotUnit == UnitType.METERS)
            viewModel.plotLength * 3.28084f else viewModel.plotLength
        val brdInFt = if (viewModel.plotUnit == UnitType.METERS)
            viewModel.plotBreadth * 3.28084f else viewModel.plotBreadth
        val usableW = (brdInFt - viewModel.leftSetback - viewModel.rightSetback).coerceAtLeast(0f)
        val usableD = (lenInFt - viewModel.frontSetback - viewModel.rearSetback).coerceAtLeast(0f)
        val footprint = usableW * usableD
        val plotArea = lenInFt * brdInFt
        val coverage = if (plotArea > 0f) (footprint / plotArea) * 100f else 0f

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "House Footprint = ${"%.0f".format(usableW)} x ${"%.0f".format(usableD)} ft",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Area: ${"%.0f".format(footprint)} sq ft",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Coverage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                coverage > 80f -> SuccessGreen
                                coverage > 60f -> AmberAccent
                                else -> WarningOrange
                            }.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Coverage: ${"%.0f".format(coverage)}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            coverage > 80f -> SuccessGreen
                            coverage > 60f -> AmberAccent
                            else -> WarningOrange
                        }
                    )
                }
            }
        }
    }
}

// ── Step 3: House Configuration ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HouseConfigStep(viewModel: PlanInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // House type
        Text(
            text = "House Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Pill chips for house types
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HouseType.entries.take(4).forEach { type ->
                Chip(
                    label = type.displayName,
                    selected = viewModel.houseType == type,
                    onClick = { viewModel.houseType = type }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HouseType.entries.drop(4).forEach { type ->
                Chip(
                    label = type.displayName,
                    selected = viewModel.houseType == type,
                    onClick = { viewModel.houseType = type }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Floors
        Text(
            text = "Number of Floors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(1, 2, 3).forEachIndexed { index, floor ->
                SegmentedButton(
                    selected = viewModel.floors == floor,
                    onClick = { viewModel.floors = floor },
                    shape = SegmentedButtonDefaults.itemShape(index, 3)
                ) {
                    Text("$floor")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Staircase
        Text(
            text = "Staircase Position",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StaircasePosition.entries.forEach { pos ->
                Chip(
                    label = pos.displayName,
                    selected = viewModel.staircasePosition == pos,
                    onClick = { viewModel.staircasePosition = pos }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Parking
        Text(
            text = "Parking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParkingType.entries.forEach { park ->
                Chip(
                    label = park.displayName,
                    selected = viewModel.parkingType == park,
                    onClick = { viewModel.parkingType = park }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pooja room
        Text(
            text = "Pooja Room",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(label = "Yes", selected = viewModel.hasPoojaRoom, onClick = { viewModel.hasPoojaRoom = true })
            Chip(label = "No", selected = !viewModel.hasPoojaRoom, onClick = { viewModel.hasPoojaRoom = false })
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Garden/Lawn
        Text(
            text = "Garden/Lawn Area",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GardenLawnArea.entries.forEach { garden ->
                Chip(
                    label = garden.displayName,
                    selected = viewModel.gardenLawn == garden,
                    onClick = { viewModel.gardenLawn = garden }
                )
            }
        }
    }
}

// ── Step 4: Room Planner ─────────────────────────────────────────────────

@Composable
private fun RoomPlannerStep(viewModel: PlanInputViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // Room count summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total: ${viewModel.rooms.size} rooms",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Area progress
                        val totalArea = viewModel.rooms.sumOf { (it.width * it.height).toDouble() }.toFloat()
                        val maxArea = viewModel.plotLength * viewModel.plotBreadth
                        val progress = if (maxArea > 0f) (totalArea / maxArea).coerceIn(0f, 1f) else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Room Area: ${"%.0f".format(totalArea)} sq ft",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${"%.0f".format(progress * 100)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (progress > 0.9f) WarningOrange else SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (progress > 0.9f) WarningOrange else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Room cards
            itemsIndexed(
                items = viewModel.rooms,
                key = { _, room -> room.id }
            ) { index, room ->
                RoomCard(
                    room = room,
                    index = index,
                    onEdit = { viewModel.openEditRoomSheet(index) },
                    onDelete = { viewModel.deleteRoom(index) },
                    onDragStart = { },
                    onDrag = { },
                    onDragEnd = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to add room
        FloatingActionButton(
            onClick = { viewModel.openAddRoomSheet() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
        }
    }
}

// ── Reusable Chip ────────────────────────────────────────────────────────

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
