package com.jansetu.sih26042.ui.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * 16 kHz microphone capture + sherpa-onnx CTC decode.
 * Entire transcription happens on-device, without Android's network speech services.
 */
class HindiOfflineRecognizer(private val context: Context, private val pack: HindiAsrModelPack) {
    suspend fun listen(shouldStop: () -> Boolean): String = withContext(Dispatchers.IO) {
        require(pack.ready()) { "Install the Hindi offline speech pack first." }
        check(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED) { "Microphone permission denied." }

        val config = OfflineRecognizerConfig(
            modelConfig = OfflineModelConfig(
                nemo = OfflineNemoEncDecCtcModelConfig(model = pack.model.absolutePath),
                tokens = pack.tokens.absolutePath,
                numThreads = 2
            )
        )
        val recognizer = OfflineRecognizer(config = config)
        val stream = recognizer.createStream()
        val minSize = AudioRecord.getMinBufferSize(
            16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        require(minSize > 0) { "16 kHz microphone audio is not supported on this device." }
        val bufferSize = max(minSize, 8192)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) {
                "Cannot open microphone for offline recognition."
            }
            val chunks = ArrayList<FloatArray>()
            var samples = 0
            val raw = ShortArray(2048)
            recorder.startRecording()
            while (!shouldStop() && samples < 16000 * 12) {
                val count = recorder.read(raw, 0, raw.size)
                if (count < 0) error("Microphone read failed: $count")
                if (count == 0) continue
                chunks.add(FloatArray(count) { i -> raw[i] / 32768.0f })
                samples += count
            }
            recorder.stop()
            require(samples > 1600) { "No Hindi speech was recorded." }
            val wave = FloatArray(samples)
            var at = 0
            for (chunk in chunks) { chunk.copyInto(wave, at); at += chunk.size }
            stream.acceptWaveform(wave, sampleRate = 16000)
            recognizer.decode(stream)
            val result = recognizer.getResult(stream).text.trim()
            require(result.isNotEmpty()) { "No Hindi speech was recognized." }
            result
        } finally {
            recorder.release()
            stream.release()
            recognizer.release()
        }
    }
}
