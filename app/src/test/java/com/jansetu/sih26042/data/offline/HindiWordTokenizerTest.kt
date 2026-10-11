package com.jansetu.sih26042.data.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HindiWordTokenizerTest {
    @Test fun punctuationAndMatras() {
        assertEquals(
            listOf("नमस्ते", "आप", "कैसे", "हैं"),
            HindiWordTokenizer.words("नमस्ते, आप कैसे हैं?")
        )
    }

    @Test fun duplicateWordsRetainTheirOrder() {
        assertEquals(
            listOf("किताब", "किताब", "पढ़ना"),
            HindiWordTokenizer.words("किताब। किताब, पढ़ना!")
        )
    }

    @Test fun whitespaceAndNumbers() {
        assertEquals(
            listOf("१२३", "पानी", "45"),
            HindiWordTokenizer.words("  १२३\nपानी\t45  ")
        )
    }

    @Test fun punctuationOnlyIsEmpty() {
        assertTrue(HindiWordTokenizer.words("...।?!").isEmpty())
    }
}
