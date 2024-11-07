package ru.naviai.aiijc

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


@Serializable
data class HistoryItem(
    val modelResults: ModelResults,
    var datetime: String = "",
    val filtersParams: FiltersParams,
    val imageRect: ImageRect
)


fun saveResultsWithImageByDate(
    context: Context,
    item: HistoryItem,
    initialBitmap: Bitmap,
    filteredBitmap: Bitmap
) {
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    item.datetime = currentDate + timestamp
    Log.i("kilo", "Checking -3")

    val historyDir = File(context.cacheDir, "history")
    if (!historyDir.exists()) {
        historyDir.mkdirs()
    }
    Log.i("kilo", "Checking -2")

    val imageInitialFile = File(
        context.cacheDir,
        "history/${item.datetime}_${timestamp}_initial.png"
    )
    Log.i("kilo", "Checking -1")
    FileOutputStream(imageInitialFile).use { out ->
        initialBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    Log.i("kilo", "Checking 0")
    val imageFilteredFile = File(
        context.cacheDir,
        "history/${item.datetime}_${timestamp}_filtered.png"
    )
    Log.i("kilo", "Checking 1")
    FileOutputStream(imageFilteredFile).use { out ->
        filteredBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    Log.i("kilo", "Checking 2")

    val resultsFile = File(
        context.cacheDir,
        "history/${item.datetime}_$timestamp.ser"
    )
    FileWriter(resultsFile).use { writer ->
        writer.write(Json.encodeToString(item))
    }
}

fun loadHistory(context: Context): MutableList<HistoryItem> {
    val historyDir = File(context.cacheDir, "history")
    val resultsList = mutableListOf<HistoryItem>()

    if (historyDir.exists() && historyDir.isDirectory) {
        historyDir.listFiles()?.forEach { file ->
            if (file.extension == "ser") {
                FileReader(file).use { reader ->
                    resultsList.add(Json.decodeFromString(reader.readText()))
                }
            }
        }
    }

    return resultsList
}