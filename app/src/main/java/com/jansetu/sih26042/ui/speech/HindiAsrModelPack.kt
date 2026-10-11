package com.jansetu.sih26042.ui.speech

import android.content.Context
import android.os.StatFs
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Hindi CTC ASR is independent of Android's speech provider (works on Android 9+). */
class HindiAsrModelPack(context: Context) {
    companion object {
        const val REPO = "parismitaglobalsolutions/indicconformer-sherpa-onnx"
    }
    val directory = File(context.filesDir, "models/hindi-asr")
    val model = File(directory, "model.int8.onnx")
    val tokens = File(directory, "tokens.txt")

    fun ready() = model.length() >= 30_000_000L && tokens.length() >= 1000L

    suspend fun install(progress: (String) -> Unit) = withContext(Dispatchers.IO) {
        directory.mkdirs()
        require(StatFs(directory.absolutePath).availableBytes > 350_000_000) {
            "Free at least 350 MB for Hindi speech recognition."
        }
        val artifacts = listOf("hi/model.int8.onnx" to model, "tokens.txt" to tokens)
        artifacts.forEachIndexed { i, (path, target) ->
            if (target.length() >= (if (i == 0) 30_000_000 else 1000)) return@forEachIndexed
            progress("Downloading offline Hindi speech data (${i + 1}/2)…")
            val part = File(directory, "${target.name}.part")
            try {
                val url = URL("https://huggingface.co/$REPO/resolve/main/$path")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 30_000
                    readTimeout = 120_000
                }
                try {
                    require(connection.responseCode == 200) {
                        "Hindi ASR download failed: HTTP ${connection.responseCode}"
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(part).use { output -> input.copyTo(output, 64 * 1024) }
                    }
                } finally {
                    connection.disconnect()
                }
                require(part.length() >= (if (i == 0) 30_000_000 else 1000)) {
                    "Hindi ASR download was incomplete."
                }
                check(part.renameTo(target)) { "Could not move Hindi ASR model into app storage." }
            } finally { part.delete() }
        }
        check(ready()) { "Hindi recognition model is incomplete." }
        progress("Offline Hindi speech pack installed.")
    }
}
