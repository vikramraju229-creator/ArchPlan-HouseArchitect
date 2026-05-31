package com.archplan.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable

/**
 * A single room in the house plan.
 * x, y, colorArgb are set during the packing/generation phase.
 */
@Serializable
data class RoomData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val type: RoomType = RoomType.BEDROOM,
    val width: Float = 0f,
    val height: Float = 0f,
    val x: Float = 0f,
    val y: Float = 0f,
    val colorArgb: Long = RoomType.BEDROOM.defaultColor.toArgb().toLong()
) {
    val area: Float get() = width * height
}

/**
 * Room types with default display properties.
 */
@Serializable
enum class RoomType(val displayName: String, val defaultColor: Color) {
    BEDROOM("Bedroom", com.archplan.ui.theme.RoomBedroom),
    LIVING_ROOM("Living Room", com.archplan.ui.theme.RoomLiving),
    KITCHEN("Kitchen", com.archplan.ui.theme.RoomKitchen),
    DINING("Dining", com.archplan.ui.theme.RoomDining),
    BATHROOM("Bathroom", com.archplan.ui.theme.RoomBathroom),
    TOILET("Toilet", com.archplan.ui.theme.RoomToilet),
    STUDY("Study", com.archplan.ui.theme.RoomStudy),
    BALCONY("Balcony", com.archplan.ui.theme.RoomBalcony),
    STAIRCASE("Staircase", com.archplan.ui.theme.RoomStaircase),
    STORE("Store", com.archplan.ui.theme.RoomStore),
    SERVANT_QUARTER("Servant Quarter", com.archplan.ui.theme.RoomServant),
    GARAGE("Garage", com.archplan.ui.theme.RoomGarage),
    POOJA_ROOM("Pooja Room", com.archplan.ui.theme.RoomPooja),
    LOBBY("Lobby", com.archplan.ui.theme.RoomLobby);
}
