package com.jansetu.sih26042.data.offline

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OlChikiValidationTest {
    @Test fun acceptsOlChiki() {
        assertTrue(OlChikiValidation.isValid("ᱥᱟᱱᱛᱟᱲᱤ ᱾"))
    }
    @Test fun rejectsMixedHindiOrEnglish() {
        assertFalse(OlChikiValidation.isValid("ᱥᱟᱱᱛᱟᱲᱤ Hindi"))
        assertFalse(OlChikiValidation.isValid("नमस्ते"))
    }
    @Test fun rejectsPunctuationOnly() {
        assertFalse(OlChikiValidation.isValid("᱾!"))
        assertFalse(OlChikiValidation.isValid(""))
    }
}
