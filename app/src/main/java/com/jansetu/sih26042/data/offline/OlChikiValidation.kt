package com.jansetu.sih26042.data.offline

/** Reject obvious wrong-script outputs instead of showing untranslated Hindi as Santhali. */
object OlChikiValidation {
    fun isValid(text: String): Boolean {
        val letters = text.filter { it.isLetter() }
        return letters.length >= 2 && letters.all { it.code in 0x1C50..0x1C7F }
    }
}
