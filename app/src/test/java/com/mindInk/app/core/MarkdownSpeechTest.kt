package com.mindInk.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownSpeechTest {

    @Test
    fun combinesTitleSubtitleAndPlainMarkdownContent() {
        val text = markdownToSpeechText(
            title = "Título",
            subtitle = "Subtítulo",
            content = "## Seção\n\nTexto **importante**"
        )

        assertEquals("Título. Subtítulo. Seção. Texto importante", text)
    }

    @Test
    fun removesCodeBlocksAndKeepsLinkLabels() {
        val text = markdownToSpeechText(
            title = "",
            subtitle = "",
            content = "Leia [a documentação](https://example.com).\n```kotlin\nval x = 1\n```"
        )

        assertEquals("Leia a documentação.", text)
    }
}
