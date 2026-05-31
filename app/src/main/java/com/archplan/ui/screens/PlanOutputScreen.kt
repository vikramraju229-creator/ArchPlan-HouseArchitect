package com.archplan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.archplan.data.model.RoomType
import com.archplan.ui.components.BlueprintCanvas
import com.archplan.ui.components.MetricCard
import com.archplan.ui.theme.AmberAccent
import com.archplan.ui.theme.BlueprintBg
import com.archplan.ui.theme.SuccessGreen
import com.archplan.ui.theme.WarningOrange
import com.archplan.ui.viewmodel.PlanOutputViewModel

/**
 * Plan output screen showing the generated blueprint with stats and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanOutputScreen(
    planId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: PlanOutputViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }

    // Show messages
    LaunchedEffect(viewModel.exportMessage) {
        viewModel.exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.housePlan?.name ?: "Your Plan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlueprintBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Blueprint Canvas (60% of screen) ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                ) {
                    BlueprintCanvas(
                        rooms = viewModel.generatedRooms,
                        houseWidth = viewModel.houseWidth,
                        houseHeight = viewModel.houseHeight,
                        selectedRoomIndex = viewModel.selectedRoomIndex,
                        onRoomSelected = { viewModel.selectRoom(it) },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Vastu badge (top-left corner)
                    viewModel.vastuReport?.let { report ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (report.rating) {
                                    com.archplan.domain.usecase.GeneratePlanUseCase.VastuRating.EXCELLENT -> SuccessGreen.copy(alpha = 0.9f)
                                    com.archplan.domain.usecase.GeneratePlanUseCase.VastuRating.GOOD -> AmberAccent.copy(alpha = 0.9f)
                                    com.archplan.domain.usecase.GeneratePlanUseCase.VastuRating.FAIR -> WarningOrange.copy(alpha = 0.9f)
                                    com.archplan.domain.usecase.GeneratePlanUseCase.VastuRating.POOR -> MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (report.score >= 60) Icons.Default.Star else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Vastu ${report.score}/100",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // ── Stats Panel (40% of screen) ───────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    // Metric cards 2x2 grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.areaResult?.let { areas ->
                            MetricCard(
                                label = "Plot Area",
                                value = areas.plotAreaLabel,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                            MetricCard(
                                label = "House Area",
                                value = areas.houseFootprintLabel,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.areaResult?.let { areas ->
                            MetricCard(
                                label = "Free Space",
                                value = areas.freeSpaceLabel,
                                modifier = Modifier.weight(1f),
                                accentColor = SuccessGreen
                            )
                            MetricCard(
                                label = "Coverage",
                                value = "${"%.0f".format(areas.coveragePercent)}%",
                                modifier = Modifier.weight(1f),
                                accentColor = AmberAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Room area breakdown
                    Text(
                        text = "Room Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    viewModel.generatedRooms.take(6).forEach { room ->
                        val roomArea = room.area
                        val totalArea = viewModel.areaResult?.totalRoomAreaSqFt ?: 1f
                        val pct = if (totalArea > 0f) (roomArea / totalArea) * 100f else 0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(room.colorArgb))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(100.dp)
                            )
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(room.colorArgb),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${"%.0f".format(roomArea)} sq ft",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sharePlan() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { viewModel.exportPdf() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onNavigateToEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
