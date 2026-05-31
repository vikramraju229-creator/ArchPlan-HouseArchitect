package com.archplan.data.model

import kotlinx.serialization.Serializable

/**
 * Configuration parameters for the house design.
 */
@Serializable
data class HouseConfig(
    val houseType: HouseType = HouseType.BHK2,
    val floors: Int = 1,
    val staircasePosition: StaircasePosition = StaircasePosition.INTERNAL,
    val parking: ParkingType = ParkingType.NONE,
    val poojaRoom: Boolean = false,
    val gardenLawn: GardenLawnArea = GardenLawnArea.NONE
)

@Serializable
enum class HouseType(val displayName: String, val defaultRooms: List<RoomType>) {
    STUDIO("Studio", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.BATHROOM, RoomType.TOILET)),
    BHK1("1 BHK", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.DINING, RoomType.BATHROOM, RoomType.TOILET, RoomType.BALCONY)),
    BHK2("2 BHK", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.DINING, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.TOILET, RoomType.BALCONY, RoomType.STAIRCASE)),
    BHK3("3 BHK", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.DINING, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.TOILET, RoomType.BALCONY, RoomType.STAIRCASE, RoomType.STUDY)),
    BHK4("4 BHK", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.DINING, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.TOILET, RoomType.BALCONY, RoomType.STAIRCASE, RoomType.STUDY, RoomType.LOBBY)),
    VILLA("Villa", listOf(RoomType.LIVING_ROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.BEDROOM, RoomType.KITCHEN, RoomType.DINING, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.BATHROOM, RoomType.TOILET, RoomType.STUDY, RoomType.BALCONY, RoomType.STAIRCASE, RoomType.STORE, RoomType.LOBBY)),
    CUSTOM("Custom", emptyList())
}

@Serializable
enum class StaircasePosition(val displayName: String) {
    INTERNAL("Internal"),
    EXTERNAL("External"),
    BOTH("Both")
}

@Serializable
enum class ParkingType(val displayName: String) {
    NONE("None"),
    CAR1("1 Car"),
    CAR2("2 Cars"),
    GARAGE("Garage")
}

@Serializable
enum class GardenLawnArea(val displayName: String) {
    NONE("None"),
    FRONT("Front"),
    REAR("Rear"),
    SIDE("Side")
}
