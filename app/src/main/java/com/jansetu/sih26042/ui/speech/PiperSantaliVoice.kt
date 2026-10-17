package com.jansetu.sih26042.ui.speech

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import org.json.JSONArray
import org.json.JSONObject

/**
 * Experimental offline VITS/Piper inference. Uses the published model's own
 * phoneme_id_map (supports grapheme-based Ol Chiki only; never invents IPA IDs).
 */
class PiperSantaliVoice(private val pack: SantaliTtsPack) {
    suspend fun speak(text: String) = withContext(Dispatchers.IO) {
        require(pack.ready()) { "Install the Santhali voice model first." }
        val config = JSONObject(pack.config.readText())
        val symbolMap = config.optJSONObject("phoneme_id_map")
            ?: error("The Santhali model does not supply a phoneme ID mapping.")
        fun symbol(key: String): List<Long>? {
            val ids = symbolMap.optJSONArray(key) ?: return null
            return (0 until ids.length()).map { ids.getLong(it) }
        }
        val phonemes = mutableListOf<Long>()
        symbol("^")?.let(phonemes::addAll)
        for (character in text) {
            val key = character.toString()
            val mapped = symbol(key)
            if (mapped == null) {
                if (character.isWhitespace()) symbol(" ")?.let(phonemes::addAll)
                else if (character.code in 0x1C50..0x1C7F) {
                    error("This voice model has no Ol Chiki mapping for: $key")
                }
                continue
            }
            phonemes.addAll(mapped)
            symbol("_")?.let(phonemes::addAll)
        }
        symbol("$")?.let(phonemes::addAll)
        require(phonemes.size > 3) { "Could not phonemize Santhali speech." }
        val environment = OrtEnvironment.getEnvironment()
        val session = environment.createSession(pack.model.absolutePath, OrtSession.SessionOptions())
        try {
            val sequence = OnnxTensor.createTensor(
                environment, LongBuffer.wrap(phonemes.toLongArray()), longArrayOf(1L, phonemes.size.toLong())
            )
            val length = OnnxTensor.createTensor(environment, LongBuffer.wrap(longArrayOf(phonemes.size.toLong())), longArrayOf(1L))
            val scalesJson = config.optJSONObject("inference")
            val scales = floatArrayOf(
                scalesJson?.optDouble("noise_scale", 0.667)?.toFloat() ?: 0.667f,
                scalesJson?.optDouble("length_scale", 1.0)?.toFloat() ?: 1f,
                scalesJson?.optDouble("noise_w", 0.8)?.toFloat() ?: 0.8f
            )
            val scaleTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(scales), longArrayOf(3L))
            val sid = OnnxTensor.createTensor(environment, LongBuffer.wrap(longArrayOf(0L)), longArrayOf(1L))
            try {
                val inputs = mutableMapOf<String, OnnxTensor>(
                    "input" to sequence, "input_lengths" to length, "scales" to scaleTensor
                )
                if ("sid" in session.inputNames) inputs["sid"] = sid
                session.run(inputs).use { result ->
                    @Suppress("UNCHECKED_CAST")
                    val samples = (result[0].value as Array<Array<FloatArray>>)[0][0]
                    val sampleRate = config.optJSONObject("audio")?.optInt("sample_rate", 16000) ?: 16000
                    require(sampleRate in 8000..48000) { "Invalid TTS sample rate." }
                    require(samples.isNotEmpty()) { "Santhali voice returned no samples." }
                    val pcm = ShortArray(samples.size) { index ->
                        (samples[index].coerceIn(-1f, 1f) * 32767).toInt().toShort()
                    }
                    val bufferSize = max(pcm.size * 2, AudioTrack.getMinBufferSize(
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
                    ))
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                        .setAudioFormat(AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                    try {
                        check(track.write(pcm, 0, pcm.size) == pcm.size) { "Audio output write failed." }
                        track.play()
                        delay((pcm.size * 1000L / sampleRate) + 200L)
                    } finally { track.stop(); track.release() }
                }
            } finally {
                sid.close()
                scaleTensor.close()
                length.close()
                sequence.close()
            }
        } finally { session.close() }
    }
}
