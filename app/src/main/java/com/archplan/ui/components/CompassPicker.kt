package com.archplan.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.archplan.data.model.FacingDirection

/**
 * Animated compass picker for selecting the plot facing direction.
 */
@Composable
fun CompassPicker(
    selected: FacingDirection,
    onSelected: (FacingDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Facing Direction",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.padding(12.dp))

        // Compass layout: N at top, E right, S bottom, W left
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // N
            CompassDirectionButton(
                text = "N",
                direction = FacingDirection.NORTH,
                isSelected = selected == FacingDirection.NORTH,
                onClick = { onSelected(FacingDirection.NORTH) },
                modifier = Modifier.align(Alignment.TopCenter)
            )
            // S
            CompassDirectionButton(
                text = "S",
                direction = FacingDirection.SOUTH,
                isSelected = selected == FacingDirection.SOUTH,
                onClick = { onSelected(FacingDirection.SOUTH) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            // E
            CompassDirectionButton(
                text = "E",
                direction = FacingDirection.EAST,
                isSelected = selected == FacingDirection.EAST,
                onClick = { onSelected(FacingDirection.EAST) },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            // W
            CompassDirectionButton(
                text = "W",
                direction = FacingDirection.WEST,
                isSelected = selected == FacingDirection.WEST,
                onClick = { onSelected(FacingDirection.WEST) },
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
private fun CompassDirectionButton(
    text: String,
    direction: FacingDirection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "compassBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = spring(),
        label = "compassText"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
