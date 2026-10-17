package com.jansetu.sih26042.data.offline

/**
 * Unicode-aware word extraction for Devanagari and mixed Hindi classroom text.
 * Includes combining vowel signs / nukta, omits sentence punctuation.
 */
object HindiWordTokenizer {
    private val wordPattern = Regex("[\\p{L}\\p{M}\\p{N}]+(?:[’'-][\\p{L}\\p{M}\\p{N}]+)*")

    fun words(input: String): List<String> = wordPattern.findAll(input)
        .map { it.value }
        .filter { it.isNotBlank() }
        .toList()
}
