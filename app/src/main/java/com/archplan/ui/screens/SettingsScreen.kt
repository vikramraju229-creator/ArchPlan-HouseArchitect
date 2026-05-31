package com.archplan.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.archplan.data.model.UnitType

/**
 * Settings screen with app configuration options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var defaultUnit by remember { mutableStateOf(UnitType.FEET) }
    var themeMode by remember { mutableStateOf("System") }
    var exportFormat by remember { mutableStateOf("PNG") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Default Unit ──────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Default.Straighten,
                title = "Default Unit"
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(UnitType.FEET, UnitType.METERS).forEachIndexed { index, unit ->
                        SegmentedButton(
                            selected = defaultUnit == unit,
                            onClick = { defaultUnit = unit },
                            shape = SegmentedButtonDefaults.itemShape(index, 2)
                        ) {
                            Text(unit.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Theme ─────────────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Default.DarkMode,
                title = "Theme"
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("Light", "Dark", "System").forEachIndexed { index, theme ->
                        SegmentedButton(
                            selected = themeMode == theme,
                            onClick = { themeMode = theme },
                            shape = SegmentedButtonDefaults.itemShape(index, 3)
                        ) {
                            Text(theme)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Export Format ─────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Default.SwapHoriz,
                title = "Export Format"
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("PNG", "PDF").forEachIndexed { index, format ->
                        SegmentedButton(
                            selected = exportFormat == format,
                            onClick = { exportFormat = format },
                            shape = SegmentedButtonDefaults.itemShape(index, 2)
                        ) {
                            Text(format)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Default Setbacks ──────────────────────────────────────────
            SettingsSection(
                icon = Icons.Default.Straighten,
                title = "Default Setbacks"
            ) {
                Text(
                    text = "Setback defaults can be configured in future updates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── About ─────────────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Default.Info,
                title = "About"
            ) {
                Text(
                    text = "ArchPlan — Genius House Architect",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Powered by GLM-4.7 • Built with Jetpack Compose & Material Design 3",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 ArchPlan. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
