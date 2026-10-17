package com.jansetu.sih26042.data.offline

import android.content.Context
import android.os.StatFs
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Pinned model revision. Teacher explicitly chooses when to download. */
class OfflineModelPack(context: Context) {
    companion object {
        const val MODEL_ID = "hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8"
        const val REVISION = "b04956dee2f2a3e06e44bf09f8d654a6b81af99a"
        const val FREE_BYTES_REQUIRED = 850L * 1024L * 1024L
    }

    data class Entry(val filename: String, val minimumBytes: Long)

    // External weights are essential: the .onnx files by themselves cannot run.
    val files = listOf(
        Entry("encoder_model.onnx", 20_000),
        Entry("encoder_model.onnx.data", 50_000_000),
        Entry("decoder_model.onnx", 20_000),
        Entry("decoder_with_past_model.onnx", 20_000),
        Entry("decoder_shared.onnx.data", 50_000_000),
        Entry("tokenizer_src.json", 5_000_000),
        Entry("tokenizer_tgt.json", 5_000_000),
        Entry("tokenizer_meta.json", 10),
        Entry("generation_config.json", 10)
    )

    val directory: File = File(context.filesDir, "models/indictrans2-$REVISION")

    fun ready(): Boolean = files.all { item ->
        val file = File(directory, item.filename)
        file.isFile && file.length() >= item.minimumBytes
    }

    fun missingFiles(): List<String> = files.filterNot { entry ->
        val file = File(directory, entry.filename)
        file.isFile && file.length() >= entry.minimumBytes
    }.map { it.filename }

    suspend fun install(progress: (String) -> Unit) = withContext(Dispatchers.IO) {
        directory.mkdirs()
        require(StatFs(directory.absolutePath).availableBytes >= FREE_BYTES_REQUIRED || ready()) {
            "At least 850 MB of free space is recommended before downloading the translation model."
        }
        files.forEachIndexed { index, entry ->
            val target = File(directory, entry.filename)
            if (target.length() >= entry.minimumBytes) return@forEachIndexed
            progress("Downloading ${index + 1}/${files.size}: ${entry.filename}")
            val temp = File(directory, "${entry.filename}.part")
            try {
                val url = URL("https://huggingface.co/$MODEL_ID/resolve/$REVISION/${entry.filename}")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 30_000
                    readTimeout = 120_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "JanSetu-SIH26042/1.2")
                }
                try {
                    require(connection.responseCode == HttpURLConnection.HTTP_OK) {
                        "Model download HTTP ${connection.responseCode} for ${entry.filename}"
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(temp).use { output -> input.copyTo(output, 64 * 1024) }
                    }
                } finally {
                    connection.disconnect()
                }
                require(temp.length() >= entry.minimumBytes) {
                    "Incomplete model file: ${entry.filename} (${temp.length()} bytes)"
                }
                check(temp.renameTo(target)) { "Cannot install ${entry.filename}" }
            } finally {
                temp.delete()
            }
        }
        check(ready()) { "Some model files are missing. Try Install again." }
        progress("Offline translation model is installed.")
    }
}
