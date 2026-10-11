package com.jansetu.sih26042.data.offline

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.Closeable
import java.io.File
import java.nio.LongBuffer
import org.json.JSONObject

/** Native CPU inference for IndicTrans2 encoder + autoregressive decoder, no web API. */
class OnDeviceTranslator(private val pack: OfflineModelPack) : Closeable {
    private val environment = OrtEnvironment.getEnvironment()
    private var encoder: OrtSession? = null
    private var decoder: OrtSession? = null
    private var decoderWithPast: OrtSession? = null
    private var sourceTokenizer: HuggingFaceTokenizer? = null
    private var targetTokenizer: HuggingFaceTokenizer? = null

    fun isInstalled(): Boolean = pack.ready()
    fun missingFiles(): List<String> = pack.missingFiles()
    suspend fun install(progress: (String) -> Unit) = pack.install(progress)

    private fun initialize() {
        if (encoder != null) return
        require(pack.ready()) { "Install the offline AI model in Offline translation library." }
        val options = OrtSession.SessionOptions().apply { setIntraOpNumThreads(2) }
        try {
            sourceTokenizer = HuggingFaceTokenizer.newInstance(
                File(pack.directory, "tokenizer_src.json").inputStream(), null
            )
            targetTokenizer = HuggingFaceTokenizer.newInstance(
                File(pack.directory, "tokenizer_tgt.json").inputStream(), null
            )
            encoder = environment.createSession(File(pack.directory, "encoder_model.onnx").absolutePath, options)
            decoder = environment.createSession(File(pack.directory, "decoder_model.onnx").absolutePath, options)
            decoderWithPast = environment.createSession(
                File(pack.directory, "decoder_with_past_model.onnx").absolutePath, options
            )
        } catch (error: Throwable) {
            close()
            throw IllegalStateException("Could not load offline translation AI: ${error.message}", error)
        } finally {
            options.close()
        }
    }

    private fun tensor(ids: LongArray): OnnxTensor = OnnxTensor.createTensor(
        environment, LongBuffer.wrap(ids), longArrayOf(1L, ids.size.toLong())
    )

    @Synchronized
    fun translate(hindi: String): String {
        require(hindi.isNotBlank()) { "Hindi text cannot be empty" }
        initialize()
        val meta = JSONObject(File(pack.directory, "tokenizer_meta.json").readText())
        val config = JSONObject(File(pack.directory, "generation_config.json").readText())
        val srcLimit = meta.getLong("src_dict_size")
        val tgtLimit = meta.getLong("tgt_dict_size")
        val unk = meta.getLong("unk_id")
        val start = config.optLong("decoder_start_token_id", 2L)
        val eos = config.optLong("eos_token_id", 2L)
        val encoded = checkNotNull(sourceTokenizer).encode("hin_Deva sat_Olck ${hindi.trim()}")
        val sourceIds = encoded.ids.map { if (it < srcLimit) it else unk }.toLongArray()
        require(sourceIds.size in 1..256) { "Try a shorter classroom sentence (maximum 256 tokens)." }
        val outputIds = mutableListOf(start)
        tensor(sourceIds).use { inputIds ->
            tensor(LongArray(sourceIds.size) { 1L }).use { attention ->
                checkNotNull(encoder).run(mapOf(
                    "input_ids" to inputIds, "attention_mask" to attention
                )).use { encodedResult ->
                    val states = encodedResult[0] as OnnxTensor
                    var previous: OrtSession.Result? = null
                    try {
                        for (step in 0 until 96) {
                            val inputs = mutableMapOf<String, OnnxTensor>()
                            val current = tensor(longArrayOf(outputIds.last()))
                            try {
                                inputs["input_ids"] = current
                                inputs["encoder_attention_mask"] = attention
                                val session: OrtSession
                                if (step == 0) {
                                    inputs["encoder_hidden_states"] = states
                                    session = checkNotNull(decoder)
                                } else {
                                    session = checkNotNull(decoderWithPast)
                                    val history = checkNotNull(previous)
                                    val layers = (history.size() - 1) / 4
                                    for (layer in 0 until layers) {
                                        val base = layer * 4 + 1
                                        inputs["past_key_values.$layer.decoder.key"] = history[base] as OnnxTensor
                                        inputs["past_key_values.$layer.decoder.value"] = history[base + 1] as OnnxTensor
                                        inputs["past_key_values.$layer.encoder.key"] = history[base + 2] as OnnxTensor
                                        inputs["past_key_values.$layer.encoder.value"] = history[base + 3] as OnnxTensor
                                    }
                                }
                                val next = session.run(inputs)
                                previous?.close()
                                previous = next
                                @Suppress("UNCHECKED_CAST")
                                val logits = (next[0].value as Array<Array<FloatArray>>)[0].last()
                                var best = 0
                                for (i in 1 until logits.size) if (logits[i] > logits[best]) best = i
                                outputIds.add(best.toLong())
                                if (best.toLong() == eos) break
                            } finally {
                                current.close()
                            }
                        }
                    } finally {
                        previous?.close()
                    }
                }
            }
        }
        val safeOutput = outputIds.map { if (it < tgtLimit) it else unk }.toLongArray()
        val translated = checkNotNull(targetTokenizer).decode(safeOutput, true).trim()
        require(translated.any { it.code in 0x1C50..0x1C7F }) {
            "AI did not return Ol Chiki text; the model output needs review."
        }
        return translated
    }

    @Synchronized
    override fun close() {
        decoderWithPast?.close(); decoderWithPast = null
        decoder?.close(); decoder = null
        encoder?.close(); encoder = null
        sourceTokenizer?.close(); sourceTokenizer = null
        targetTokenizer?.close(); targetTokenizer = null
    }
}
