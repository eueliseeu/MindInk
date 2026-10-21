package com.mindInk.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class NameFormatterTest {

    @Test
    fun returnsFallbackWhenNameIsMissing() {
        assertEquals("Autor", formatAuthorName(null))
        assertEquals("Autor", formatAuthorName("   "))
    }

    @Test
    fun formatsOnlyFirstTwoNameParts() {
        assertEquals("Ana Silva", formatAuthorName(" Ana Silva Rocha "))
    }

    @Test
    fun preservesSinglePartName() {
        assertEquals("Eliseu", formatAuthorName("Eliseu"))
    }
}
