package com.archplan.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.archplan.data.model.HousePlan
import com.archplan.data.model.RoomData
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExportPlanUseCase @Inject constructor() {

    fun exportToPdf(context: Context, plan: HousePlan): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawBlueprint(canvas, plan)

        pdfDocument.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val file = File(dir, "ArchPlan_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        return file
    }

    fun exportToBitmap(plan: HousePlan, widthPx: Int = 1200, heightPx: Int = 900): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBlueprint(canvas, plan)
        return bitmap
    }

    private fun drawBlueprint(canvas: Canvas, plan: HousePlan) {
        val bgPaint = Paint().apply { color = Color.parseColor("#0A1628") }
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bgPaint)

        val gridPaint = Paint().apply {
            color = Color.parseColor("#1A2E4A")
            strokeWidth = 0.5f
        }
        val step = 40f
        var x = 0f
        while (x <= canvas.width) {
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gridPaint)
            x += step
        }
        var y = 0f
        while (y <= canvas.height) {
            canvas.drawLine(0f, y, canvas.width.toFloat(), y, gridPaint)
            y += step
        }

        val wallPaint = Paint().apply {
            color = Color.parseColor("#378ADD")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val pad = 60f
        val pw = canvas.width - pad * 2
        val ph = canvas.height - pad * 2
        canvas.drawRect(pad, pad, pad + pw, pad + ph, wallPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
        }
        canvas.drawText("ArchPlan — House Blueprint", pad + 10f, pad - 10f, titlePaint)

        val roomColors = mapOf(
            "Bedroom" to "#4A90D9",
            "Living room" to "#5CB85C",
            "Kitchen" to "#E8A838",
            "Bathroom" to "#9B59B6",
            "Dining" to "#E67E22",
            "Study" to "#1ABC9C",
            "Balcony" to "#2ECC71",
            "Garage" to "#95A5A6",
            "Pooja" to "#E91E63",
            "Store" to "#7F8C8D"
        )

        val scaleX = (pw - 20f) / (plan.plotData.length + 1)
        val scaleY = (ph - 20f) / (plan.plotData.breadth + 1)
        val houseOffX = pad + plan.setbackData.left * scaleX
        val houseOffY = pad + plan.setbackData.front * scaleY

        for (room in plan.rooms) {
            val rx = houseOffX + room.x * scaleX
            val ry = houseOffY + room.y * scaleY
            val rw = room.width * scaleX
            val rh = room.height * scaleY

            val fillColor = Color.parseColor(roomColors[room.name] ?: "#888888")
            val fillPaint = Paint().apply {
                color = fillColor
                alpha = 80
                style = Paint.Style.FILL
            }
            canvas.drawRect(rx, ry, rx + rw, ry + rh, fillPaint)

            val strokePaint = Paint().apply {
                color = fillColor
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(rx, ry, rx + rw, ry + rh, strokePaint)

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = minOf(rw, rh) * 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(room.name, rx + rw / 2, ry + rh / 2, textPaint)

            val dimPaint = Paint().apply {
                color = Color.parseColor("#AAAAAA")
                textSize = minOf(rw, rh) * 0.13f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "${room.width}x${room.height}ft",
                rx + rw / 2,
                ry + rh / 2 + textPaint.textSize,
                dimPaint
            )
        }
    }
}
