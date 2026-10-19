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
        if (!ready) return

        tts?.language = Locale.forLanguageTag("pt-BR")
        tts?.setSpeechRate(0.88f)
        tts?.setPitch(1.0f)
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeakingChanged(true)
            }

            override fun onDone(utteranceId: String?) {
                activeUtterances = (activeUtterances - 1).coerceAtLeast(0)
                if (activeUtterances == 0) onSpeakingChanged(false)
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

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!ready) {
            pendingText = text
            return
        }
        stop()

        val engine = tts ?: return
        val parts = splitByPunctuation(text)
        if (parts.isEmpty()) return

        activeUtterances = parts.count { it is SpeechPart.Speak }
        var first = true

        for (part in parts) {
            when (part) {
                is SpeechPart.Speak -> {
                    val mode = if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    first = false
                    engine.speak(part.text, mode, null, "mindink_speak_${part.text.hashCode()}")
                }
                is SpeechPart.Pause -> {
                    engine.playSilentUtterance(
                        part.durationMs,
                        TextToSpeech.QUEUE_ADD,
                        "mindink_pause_${part.durationMs}"
                    )
                }
            }
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

    private fun splitByPunctuation(text: String): List<SpeechPart> {
        val result = mutableListOf<SpeechPart>()
        val matches = Regex("([^.!?,;:]+[.!?,;:]?)").findAll(text.trim())

        for (match in matches) {
            val raw = match.value.trim()
            if (raw.isBlank()) continue

            val (spoken, pauseMs) = when (raw.last()) {
                '.', '!', '?' -> raw to 450L
                ',', ';', ':' -> raw to 220L
                else -> raw to 80L
            }
            if (spoken.isNotBlank()) result.add(SpeechPart.Speak(spoken))
            if (pauseMs > 0) result.add(SpeechPart.Pause(pauseMs))
        }

        if (result.isEmpty() && text.isNotBlank()) {
            result.add(SpeechPart.Speak(text.trim()))
        }
        return result
    }

    private sealed class SpeechPart {
        data class Speak(val text: String) : SpeechPart()
        data class Pause(val durationMs: Long) : SpeechPart()
    }
}
