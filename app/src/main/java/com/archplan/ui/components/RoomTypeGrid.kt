package com.archplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.Countertops
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.DoorSliding
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Living
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.archplan.data.model.RoomType

/**
 * Grid of room type options for the room editor bottom sheet.
 * Each item shows an icon and room type name.
 */
@Composable
fun RoomTypeGrid(
    selectedType: RoomType,
    onTypeSelected: (RoomType) -> Unit,
    modifier: Modifier = Modifier
) {
    val roomTypes = RoomType.entries.map { type ->
        RoomTypeItem(
            type = type,
            icon = getIconForRoomType(type)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(roomTypes, key = { it.type.name }) { item ->
            val isSelected = item.type == selectedType
            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onTypeSelected(item.type) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.type.displayName,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private data class RoomTypeItem(
    val type: RoomType,
    val icon: ImageVector
)

private fun getIconForRoomType(type: RoomType): ImageVector = when (type) {
    RoomType.BEDROOM -> Icons.Default.BedroomParent
    RoomType.LIVING_ROOM -> Icons.Default.Living
    RoomType.KITCHEN -> Icons.Default.Kitchen
    RoomType.DINING -> Icons.Default.Countertops
    RoomType.BATHROOM -> Icons.Default.Bathtub
    RoomType.TOILET -> Icons.Default.Water
    RoomType.STUDY -> Icons.Default.Book
    RoomType.BALCONY -> Icons.Default.Deck
    RoomType.STAIRCASE -> Icons.Default.Stairs
    RoomType.STORE -> Icons.Default.Store
    RoomType.SERVANT_QUARTER -> Icons.Default.Cabin
    RoomType.GARAGE -> Icons.Default.Garage
    RoomType.POOJA_ROOM -> Icons.Default.AccessibilityNew
    RoomType.LOBBY -> Icons.Default.BorderAll
}
