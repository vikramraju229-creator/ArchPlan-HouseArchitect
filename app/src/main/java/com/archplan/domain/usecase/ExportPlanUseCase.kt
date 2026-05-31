package com.archplan.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.archplan.data.model.HousePlan
import com.archplan.data.model.RoomData
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles exporting house plans as bitmap images and PDF files.
 */
@Singleton
class ExportPlanUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Exports the blueprint canvas to a high-resolution bitmap.
     */
    fun exportToBitmap(
        plan: HousePlan,
        rooms: List<RoomData>,
        houseWidth: Float,
        houseHeight: Float,
        canvasWidth: Int = 1400,
        canvasHeight: Int = 1000
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(android.graphics.Color.parseColor("#0A1628"))

        val scale = min(
            canvasWidth.toFloat() / (houseWidth + 40f),
            canvasHeight.toFloat() / (houseHeight + 40f)
        )
        val offsetX = (canvasWidth - houseWidth * scale) / 2f
        val offsetY = (canvasHeight - houseHeight * scale) / 2f

        // Draw compound wall
        val compoundPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#2E5090")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(offsetX, offsetY, offsetX + houseWidth * scale, offsetY + houseHeight * scale, compoundPaint)

        // Draw rooms
        for (room in rooms) {
            val rx = offsetX + room.x * scale
            val ry = offsetY + room.y * scale
            val rw = room.width * scale
            val rh = room.height * scale

            val fillPaint = Paint().apply {
                color = (room.colorArgb and 0xFFFFFFFF).toInt() or 0x40000000
                style = Paint.Style.FILL
            }
            val strokePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#4A8CFF")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            canvas.drawRect(rx, ry, rx + rw, ry + rh, fillPaint)
            canvas.drawRect(rx, ry, rx + rw, ry + rh, strokePaint)

            // Room label
            val textPaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val name = room.name
            val dim = "${room.width.toInt()}x${room.height.toInt()}"
            canvas.drawText(name, rx + rw / 2f, ry + rh / 2f - 10f, textPaint)
            textPaint.textSize = 18f
            textPaint.color = android.graphics.Color.parseColor("#4A8CFF")
            canvas.drawText(dim, rx + rw / 2f, ry + rh / 2f + 20f, textPaint)
        }

        return bitmap
    }

    /**
     * Generates an A4 landscape PDF of the house plan using iText7.
     */
    fun exportToPdf(
        plan: HousePlan,
        rooms: List<RoomData>,
        houseWidth: Float,
        houseHeight: Float,
        onComplete: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "ArchPlan_${plan.name.replace(" ", "_")}_$timestamp.pdf"
            val outputDir = File(context.filesDir, "exports").apply { mkdirs() }
            val pdfFile = File(outputDir, fileName)

            val writer = PdfWriter(FileOutputStream(pdfFile))
            val pdfDoc = PdfDocument(writer)
            val doc = Document(pdfDoc, PageSize.A4.rotate())
            doc.setMargins(20f, 20f, 20f, 20f)

            // Title
            doc.add(Paragraph("ArchPlan — House Blueprint").setFontSize(24f).setBold())
            doc.add(Paragraph("Plan: ${plan.name}"))
            doc.add(Paragraph("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())}"))
            doc.add(Paragraph(" "))

            // Stats table
            val table = Table(UnitValue.createPercentArray(4)).useAllAvailableWidth()
            table.addCell("Plot Size")
            table.addCell("House Area")
            table.addCell("Rooms")
            table.addCell("Coverage")
            table.addCell("${plan.plotData.length.toInt()}x${plan.plotData.breadth.toInt()} ft")
            table.addCell("${"%.0f".format(plan.totalRoomArea)} sq ft")
            table.addCell("${plan.rooms.size}")
            table.addCell("${"%.0f".format(plan.coveragePercent)}%")
            doc.add(table)

            doc.add(Paragraph(" "))

            // Room list
            doc.add(Paragraph("Room Schedule:").setFontSize(16f).setBold())
            val roomTable = Table(UnitValue.createPercentArray(5)).useAllAvailableWidth()
            roomTable.addCell("Room")
            roomTable.addCell("Width")
            roomTable.addCell("Length")
            roomTable.addCell("Area")
            roomTable.addCell("Position")
            for (room in rooms) {
                roomTable.addCell(room.name)
                roomTable.addCell("${room.width.toInt()} ft")
                roomTable.addCell("${room.height.toInt()} ft")
                roomTable.addCell("${room.area.toInt()} sq ft")
                roomTable.addCell("(${room.x.toInt()}, ${room.y.toInt()})")
            }
            doc.add(roomTable)

            doc.add(Paragraph(" "))
            doc.add(Paragraph("Generated by ArchPlan — Genius House Architect").setFontSize(10f).setTextAlignment(TextAlignment.CENTER))

            doc.close()

            onComplete(pdfFile)
        } catch (e: Exception) {
            onError(e)
        }
    }
}
