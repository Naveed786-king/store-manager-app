package com.storemanager.app.util

import android.content.Context
import androidx.core.content.FileProvider
import com.storemanager.app.data.entity.Product
import java.io.File
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvUtils {

    private const val HEADER = "id,name,sku,barcode,category,brand,costPrice,sellingPrice,quantity,lowStockThreshold"

    fun exportProducts(context: Context, products: List<Product>): Uri {
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val file = File(dir, "products_export_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { writer ->
            writer.write(HEADER)
            writer.newLine()
            for (p in products) {
                writer.write(
                    listOf(
                        p.id, esc(p.name), esc(p.sku), esc(p.barcode ?: ""), esc(p.category),
                        esc(p.brand), p.costPrice, p.sellingPrice, p.quantity, p.lowStockThreshold
                    ).joinToString(",")
                )
                writer.newLine()
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun importProducts(context: Context, uri: Uri): List<Product> {
        val result = mutableListOf<Product>()
        context.contentResolver.openInputStream(uri)?.use { input ->
            val reader = BufferedReader(InputStreamReader(input))
            var line = reader.readLine() // skip header
            while (reader.readLine().also { line = it } != null) {
                val cols = parseCsvLine(line ?: continue)
                if (cols.size >= 9) {
                    result.add(
                        Product(
                            name = cols[1],
                            sku = cols[2],
                            barcode = cols[3].ifBlank { null },
                            category = cols[4],
                            brand = cols[5],
                            costPrice = cols[6].toDoubleOrNull() ?: 0.0,
                            sellingPrice = cols[7].toDoubleOrNull() ?: 0.0,
                            quantity = cols[8].toIntOrNull() ?: 0,
                            lowStockThreshold = cols.getOrNull(9)?.toIntOrNull() ?: 5
                        )
                    )
                }
            }
        }
        return result
    }

    private fun esc(value: String): String = if (value.contains(",")) "\"$value\"" else value

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { result.add(current.toString()); current = StringBuilder() }
                else -> current.append(c)
            }
        }
        result.add(current.toString())
        return result
    }
}
