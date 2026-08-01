package com.storemanager.app.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.storemanager.app.data.entity.Sale
import com.storemanager.app.data.entity.SaleItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

    fun generate(context: Context, sale: Sale, items: List<SaleItem>, customerName: String, storeName: String = "Store Manager"): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true }
        val normalPaint = Paint().apply { textSize = 12f }
        val boldPaint = Paint().apply { textSize = 12f; isFakeBoldText = true }

        var y = 40f
        canvas.drawText(storeName, 40f, y, titlePaint)
        y += 25f
        canvas.drawText("Invoice: ${sale.invoiceNumber}", 40f, y, normalPaint)
        y += 18f
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(sale.createdAt))
        canvas.drawText("Date: $dateStr", 40f, y, normalPaint)
        y += 18f
        canvas.drawText("Customer: $customerName", 40f, y, normalPaint)
        y += 18f
        canvas.drawText("Payment: ${sale.paymentMethod}", 40f, y, normalPaint)
        y += 30f

        canvas.drawText("Item", 40f, y, boldPaint)
        canvas.drawText("Qty", 300f, y, boldPaint)
        canvas.drawText("Price", 380f, y, boldPaint)
        canvas.drawText("Total", 480f, y, boldPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, normalPaint)
        y += 20f

        for (item in items) {
            canvas.drawText(item.productName.take(30), 40f, y, normalPaint)
            canvas.drawText(item.quantity.toString(), 300f, y, normalPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.unitPrice), 380f, y, normalPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", item.lineTotal), 480f, y, normalPaint)
            y += 20f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, normalPaint)
        y += 20f
        canvas.drawText("Subtotal: ${String.format(Locale.getDefault(), "%.2f", sale.subtotal)}", 380f, y, normalPaint)
        y += 18f
        canvas.drawText("Discount: ${String.format(Locale.getDefault(), "%.2f", sale.discount)}", 380f, y, normalPaint)
        y += 18f
        canvas.drawText("Tax: ${String.format(Locale.getDefault(), "%.2f", sale.tax)}", 380f, y, normalPaint)
        y += 20f
        canvas.drawText("Total: ${String.format(Locale.getDefault(), "%.2f", sale.total)}", 380f, y, boldPaint)

        document.finishPage(page)

        val dir = File(context.getExternalFilesDir(null), "invoices").apply { mkdirs() }
        val file = File(dir, "${sale.invoiceNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
