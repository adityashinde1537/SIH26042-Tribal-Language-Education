package com.jansetu.sih26042.ui.speech

import android.content.Context
import android.os.StatFs
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Experimental Santali Ol Chiki Piper model. No cloud TTS is used. */
class SantaliTtsPack(context: Context) {
    private val modelRepo = "Ashraf01k/vernacular-pedagogy-santhali"
    val directory = File(context.filesDir, "models/santali-tts")
    val model = File(directory, "sat_piper_model.onnx")
    val config = File(directory, "sat_piper_model.onnx.json")

    fun ready() = model.length() > 35_000_000 && config.length() > 100

    suspend fun install(progress: (String) -> Unit) = withContext(Dispatchers.IO) {
        directory.mkdirs()
        require(StatFs(directory.absolutePath).availableBytes > 150_000_000) {
            "Free at least 150 MB for the Santhali voice model."
        }
        for ((index, target) in listOf(model, config).withIndex()) {
            if (target.length() >= (if (index == 0) 35_000_000 else 100)) continue
            progress("Downloading offline Santhali voice (${index + 1}/2)…")
            val partial = File(directory, "${target.name}.part")
            try {
                val url = URL("https://huggingface.co/$modelRepo/resolve/main/${target.name}")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 30_000
                    readTimeout = 120_000
                }
                try {
                    require(connection.responseCode == 200) {
                        "Santhali voice download returned HTTP ${connection.responseCode}"
                    }
                    connection.inputStream.use { input ->
                        FileOutputStream(partial).use { out -> input.copyTo(out, 64 * 1024) }
                    }
                } finally { connection.disconnect() }
                require(partial.length() >= (if (index == 0) 35_000_000 else 100)) {
                    "The downloaded Santhali model was incomplete."
                }
                check(partial.renameTo(target)) { "Cannot install ${target.name}" }
            } finally { partial.delete() }
        }
        check(ready()) { "Santhali voice is not fully installed." }
        progress("Santhali Piper model installed. Test pronunciation with fluent speakers.")
    }
}
