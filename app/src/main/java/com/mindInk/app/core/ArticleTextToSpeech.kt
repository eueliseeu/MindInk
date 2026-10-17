package com.mindInk.app.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class ArticleTextToSpeech(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pendingText: String? = null
    private var activeUtterances = 0

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts?.language = Locale.forLanguageTag("pt-BR")
            tts?.setSpeechRate(0.88f)
            tts?.setPitch(1.0f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    activeUtterances = (activeUtterances - 1).coerceAtLeast(0)
                    if (activeUtterances == 0) {
                        onSpeakingChanged(false)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    activeUtterances = 0
                    onSpeakingChanged(false)
                }
            })
            pendingText?.let { speak(it) }
            pendingText = null
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!ready) {
            pendingText = text
            return
        }
        stop()
        val chunks = chunkText(text)
        activeUtterances = chunks.size
        chunks.forEachIndexed { index, chunk ->
            val mode = if (index == 0) {
                TextToSpeech.QUEUE_FLUSH
            } else {
                TextToSpeech.QUEUE_ADD
            }
            tts?.speak(chunk, mode, null, "mindink_$index")
        }
        if (chunks.isEmpty()) {
            onSpeakingChanged(false)
        }
    }

    fun stop() {
        tts?.stop()
        activeUtterances = 0
        onSpeakingChanged(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
        activeUtterances = 0
        onSpeakingChanged(false)
    }

    private fun chunkText(text: String): List<String> {
        val sentences = text
            .split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (sentences.isEmpty()) return listOf(text.take(300))

        val chunks = mutableListOf<String>()
        val buffer = StringBuilder()

        for (sentence in sentences) {
            if (buffer.length + sentence.length > 350) {
                if (buffer.isNotBlank()) {
                    chunks.add(buffer.toString().trim())
                    buffer.clear()
                }
                if (sentence.length > 350) {
                    sentence.chunked(300).forEach { chunks.add(it) }
                } else {
                    buffer.append(sentence).append(' ')
                }
            } else {
                buffer.append(sentence).append(' ')
            }
        }
        if (buffer.isNotBlank()) {
            chunks.add(buffer.toString().trim())
        }
        return chunks
    }
}
