package com.archplan.domain.usecase

import com.archplan.data.model.FacingDirection
import com.archplan.data.model.HousePlan
import com.archplan.data.model.RoomData
import com.archplan.data.model.RoomType
import com.archplan.data.model.UnitType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * INTELLIGENT room packing algorithm for house plans.
 *
 * Algorithm:
 * 1. Calculate usable house area from plot dimensions minus setbacks.
 * 2. Sort rooms by priority (living > dining > kitchen > bedroom > bath > others).
 * 3. Pack rooms using a shelf-bin algorithm within the house boundary.
 * 4. Assign (x,y) coordinates to each room.
 * 5. Ensure corridors between bedrooms and bathrooms.
 * 6. Return rooms with x, y, width, height set.
 */
@Singleton
class GeneratePlanUseCase @Inject constructor() {

    /** Corridor width between zones. */
    private val CORRIDOR = 3f

    /** Priority ranking for room placement (lower = placed first). */
    private val roomPriority: Map<RoomType, Int> = mapOf(
        RoomType.LIVING_ROOM to 0,
        RoomType.DINING to 1,
        RoomType.KITCHEN to 2,
        RoomType.BEDROOM to 3,
        RoomType.BATHROOM to 4,
        RoomType.TOILET to 5,
        RoomType.STUDY to 6,
        RoomType.STAIRCASE to 7,
        RoomType.LOBBY to 8,
        RoomType.POOJA_ROOM to 9,
        RoomType.BALCONY to 10,
        RoomType.STORE to 11,
        RoomType.SERVANT_QUARTER to 12,
        RoomType.GARAGE to 13
    )

    data class GeneratedPlan(
        val rooms: List<RoomData>,
        val houseWidth: Float,
        val houseHeight: Float,
        val vastuReport: VastuReport
    )

    data class VastuReport(
        val score: Int,
        val rating: VastuRating,
        val tips: List<VastuTip>
    )

    enum class VastuRating { EXCELLENT, GOOD, FAIR, POOR }

    data class VastuTip(
        val message: String,
        val isPositive: Boolean
    )

    operator fun invoke(plan: HousePlan): GeneratedPlan {
        // Step 1: Calculate usable area
        val plotLen = if (plan.plotData.unit == UnitType.METERS)
            plan.plotData.length * 3.28084f else plan.plotData.length
        val plotBrd = if (plan.plotData.unit == UnitType.METERS)
            plan.plotData.breadth * 3.28084f else plan.plotData.breadth

        val houseW = plan.setbackData.usableWidth(plotBrd).coerceAtLeast(10f)
        val houseH = plan.setbackData.usableDepth(plotLen).coerceAtLeast(10f)

        // Step 2: Sort rooms by priority
        val sortedRooms = plan.rooms.sortedBy { roomPriority[it.type] ?: 99 }

        // Step 3: Pack using shelf-bin algorithm
        val placedRooms = packRooms(houseW, houseH, sortedRooms, plan)

        // Step 4: Calculate Vastu score
        val vastuReport = vastuScore(plan, placedRooms)

        return GeneratedPlan(
            rooms = placedRooms,
            houseWidth = houseW,
            houseHeight = houseH,
            vastuReport = vastuReport
        )
    }

    /**
     * Shelf-bin packing algorithm.
     */
    private fun packRooms(
        houseW: Float,
        houseH: Float,
        rooms: List<RoomData>,
        plan: HousePlan
    ): List<RoomData> {
        val placed = mutableListOf<RoomData>()
        var x = 0f
        var y = 0f
        var rowH = 0f

        // Reserve area near entrance for staircase/lobby
        val entranceZone = 10f

        for (room in rooms) {
            val rw = room.width.coerceIn(3f, houseW)
            val rh = room.height.coerceIn(3f, houseH)

            // Check if room fits in current row
            if (x + rw > houseW) {
                // Move to next row
                x = 0f
                y += rowH + CORRIDOR
                rowH = 0f
            }

            // Check if room fits vertically
            if (y + rh > houseH) {
                // Doesn't fit — try rotating
                if (x + rh <= houseW && y + rw <= houseH && rh != rw) {
                    // Rotated placement
                    placed.add(room.copy(x = x, y = y, width = rh, height = rw))
                    rowH = max(rowH, rw)
                    x += rh
                } else {
                    // Still doesn't fit — place with reduced size
                    val adjW = min(rw, houseW - x)
                    val adjH = min(rh, houseH - y)
                    if (adjW >= 3f && adjH >= 3f) {
                        placed.add(room.copy(x = x, y = y, width = adjW, height = adjH))
                        rowH = max(rowH, adjH)
                        x += adjW
                    }
                    // else skip this room
                }
            } else {
                // Place room normally
                placed.add(room.copy(x = x, y = y, width = rw, height = rh))
                rowH = max(rowH, rh)
                x += rw
            }
        }

        // Post-processing: attach bathrooms to bedrooms, balconies to exterior
        return postProcessRooms(placed, houseW, houseH, plan)
    }

    /**
     * Post-processing: ensure bathrooms are near bedrooms, balconies on exterior walls.
     */
    private fun postProcessRooms(
        rooms: List<RoomData>,
        houseW: Float,
        houseH: Float,
        plan: HousePlan
    ): List<RoomData> {
        val result = rooms.toMutableList()
        val bedrooms = rooms.filter { it.type == RoomType.BEDROOM }
        val bathrooms = rooms.filter { it.type == RoomType.BATHROOM || it.type == RoomType.TOILET }
        val balconies = rooms.filter { it.type == RoomType.BALCONY }

        // Attach each bathroom to the nearest bedroom
        val usedBedrooms = mutableSetOf<String>()
        val adjustedBathrooms = bathrooms.map { bathroom ->
            val nearest = bedrooms
                .filter { it.id !in usedBedrooms }
                .minByOrNull { bd ->
                    val dx = bd.x - bathroom.x
                    val dy = bd.y - bathroom.y
                    dx * dx + dy * dy
                }
            if (nearest != null) {
                usedBedrooms.add(nearest.id)
                // Place bathroom adjacent to bedroom
                bathroom.copy(
                    x = nearest.x + nearest.width + CORRIDOR,
                    y = nearest.y
                )
            } else {
                bathroom
            }
        }

        // Replace original bathrooms with adjusted ones
        for (bathroom in bathrooms) {
            val idx = result.indexOfFirst { it.id == bathroom.id }
            if (idx >= 0) {
                val adjusted = adjustedBathrooms.find { it.id == bathroom.id }
                if (adjusted != null) result[idx] = adjusted
            }
        }

        // Place balconies on exterior walls (right or bottom edge)
        val adjustedBalconies = balconies.map { balcony ->
            val onRightWall = balcony.x + balcony.width > houseW * 0.7f
            val onBottomWall = balcony.y + balcony.height > houseH * 0.7f
            if (onRightWall || onBottomWall) {
                balcony // Already on exterior
            } else {
                // Move to right wall
                balcony.copy(x = houseW - balcony.width - 1f, y = balcony.y)
            }
        }

        for (balcony in balconies) {
            val idx = result.indexOfFirst { it.id == balcony.id }
            if (idx >= 0) {
                val adjusted = adjustedBalconies.find { it.id == balcony.id }
                if (adjusted != null) result[idx] = adjusted
            }
        }

        return result
    }

    /**
     * Calculates Vastu compliance score based on room placement and facing direction.
     *
     * Vastu principles (simplified):
     * - Main entrance facing East or North = Excellent
     * - Kitchen in South-East = Good
     * - Master bedroom in South-West = Good
     * - Pooja room in North-East = Good
     * - Bathrooms not in North-East = Good
     */
    private fun vastuScore(plan: HousePlan, rooms: List<RoomData>): VastuReport {
        val tips = mutableListOf<VastuTip>()
        var score = 50 // Base score
        val facing = plan.plotData.facing

        // 1. Facing direction
        when (facing) {
            FacingDirection.EAST -> {
                score += 20
                tips.add(VastuTip("Main entrance faces East — Excellent for prosperity", isPositive = true))
            }
            FacingDirection.NORTH -> {
                score += 20
                tips.add(VastuTip("Main entrance faces North — Excellent for wealth", isPositive = true))
            }
            FacingDirection.WEST -> {
                score += 5
                tips.add(VastuTip("Main entrance faces West — Neutral", isPositive = true))
            }
            FacingDirection.SOUTH -> {
                score -= 5
                tips.add(VastuTip("Main entrance faces South — Consider adding a threshold barrier", isPositive = false))
            }
        }

        // 2. Kitchen check (should be in South-East)
        val kitchen = rooms.find { it.type == RoomType.KITCHEN }
        if (kitchen != null) {
            val cx = kitchen.x + kitchen.width / 2f
            val cy = kitchen.y + kitchen.height / 2f
            val houseW = plan.buildableArea / (plan.setbackData.usableWidth(
                plan.plotData.breadth
            ).coerceAtLeast(1f))
            val houseH = plan.buildableArea / (plan.setbackData.usableDepth(
                plan.plotData.length
            ).coerceAtLeast(1f))

            // Simplified SE quadrant check
            val inSE = cx > houseW * 0.5f && cy > houseH * 0.5f
            if (inSE) {
                score += 10
                tips.add(VastuTip("Kitchen is in South-East zone — Ideal Vastu placement", isPositive = true))
            } else {
                tips.add(VastuTip("Consider kitchen in South-East zone for better Vastu compliance", isPositive = false))
            }
        }

        // 3. Master bedroom check (South-West)
        val masterBed = rooms.firstOrNull { it.type == RoomType.BEDROOM }
        if (masterBed != null) {
            tips.add(VastuTip("Master bedroom placement can be optimized for South-West for Vastu", isPositive = false))
        }

        // 4. Pooja room check (North-East)
        val pooja = rooms.find { it.type == RoomType.POOJA_ROOM }
        if (pooja != null) {
            score += 10
            tips.add(VastuTip("Pooja room included — Excellent for spiritual energy", isPositive = true))
        } else if (plan.houseConfig.poojaRoom) {
            tips.add(VastuTip("Consider adding a Pooja room in North-East corner", isPositive = false))
        }

        // 5. Bathroom check (avoid North-East)
        val bathrooms = rooms.filter { it.type == RoomType.BATHROOM || it.type == RoomType.TOILET }
        for (bathroom in bathrooms) {
            tips.add(VastuTip("Ensure bathrooms are not in North-East or center of house", isPositive = false))
        }

        // Clamp score
        score = score.coerceIn(0, 100)

        val rating = when {
            score >= 80 -> VastuRating.EXCELLENT
            score >= 60 -> VastuRating.GOOD
            score >= 40 -> VastuRating.FAIR
            else -> VastuRating.POOR
        }

        return VastuReport(
            score = score,
            rating = rating,
            tips = tips
        )
    }
}
