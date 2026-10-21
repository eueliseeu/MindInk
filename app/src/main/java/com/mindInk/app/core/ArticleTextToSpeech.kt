package com.mindInk.app.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.text.BreakIterator
import java.util.Locale

class ArticleTextToSpeech(
    context: Context,
    private val speechRate: Float = 1.03f,
    private val onSpeakingChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private val localePtBr: Locale = Locale.forLanguageTag("pt-BR")

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    @Volatile private var ready = false
    private var pendingText: String? = null

    @Volatile private var sessionId = 0L
    @Volatile private var lastUtteranceId: String? = null

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        val engine = tts
        if (!ready || engine == null) return

        engine.setLanguage(localePtBr)

        pickBestVoice(engine)?.let { runCatching { engine.voice = it } }

        engine.setSpeechRate(speechRate)
        engine.setPitch(1.0f)

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (isCurrentSession(utteranceId) && isSpeakId(utteranceId)) {
                    onSpeakingChanged(true)
                }
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId != null && utteranceId == lastUtteranceId) {
                    onSpeakingChanged(false)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (isCurrentSession(utteranceId)) onSpeakingChanged(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                if (isCurrentSession(utteranceId)) onSpeakingChanged(false)
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
        val engine = tts ?: return

        val paragraphs = splitIntoParagraphs(cleanText(text))
        if (paragraphs.isEmpty()) return

        val session = ++sessionId
        engine.stop()

        var counter = 0
        var first = true
        var lastId: String? = null

        paragraphs.forEachIndexed { pIndex, sentences ->
            sentences.forEach { sentence ->
                val id = "mindink_${session}_s_${counter++}"
                val mode = if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                first = false
                lastId = id
                engine.speak(sentence, mode, null, id)
            }
            if (pIndex < paragraphs.lastIndex) {
                engine.playSilentUtterance(
                    PARAGRAPH_PAUSE_MS,
                    TextToSpeech.QUEUE_ADD,
                    "mindink_${session}_p_${counter++}"
                )
            }
        }
        lastUtteranceId = lastId
    }

    fun stop() {
        sessionId++
        lastUtteranceId = null
        tts?.stop()
        onSpeakingChanged(false)
    }

    fun shutdown() {
        sessionId++
        lastUtteranceId = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
        pendingText = null
        onSpeakingChanged(false)
    }

    private fun isCurrentSession(id: String?) =
        id != null && id.startsWith("mindink_${sessionId}_")

    private fun isSpeakId(id: String?) = id?.contains("_s_") == true

    private fun pickBestVoice(engine: TextToSpeech): Voice? = runCatching {
        engine.voices
            ?.filter {
                it.locale.toLanguageTag().equals("pt-BR", ignoreCase = true) &&
                    !it.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
            }
            ?.maxWithOrNull(
                compareBy<Voice>({ it.quality }, { !it.isNetworkConnectionRequired })
            )
    }.getOrNull()

    private fun cleanText(text: String): String =
        text
            .replace(Regex("https?://\\S+"), "")
            .replace(Regex("[*_#`>~]+"), "")
            .replace(Regex("[ \\t]+"), " ")
            .trim()

    private fun splitIntoParagraphs(text: String): List<List<String>> =
        text.split(Regex("\\n{2,}|\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { splitSentences(it) }
            .filter { it.isNotEmpty() }

    private fun splitSentences(paragraph: String): List<String> {
        val it = BreakIterator.getSentenceInstance(localePtBr)
        it.setText(paragraph)
        val out = mutableListOf<String>()
        var start = it.first()
        var end = it.next()
        while (end != BreakIterator.DONE) {
            val s = paragraph.substring(start, end).trim()
            if (s.isNotEmpty()) out.addAll(chunkIfTooLong(s))
            start = end
            end = it.next()
        }
        return out
    }

    private fun chunkIfTooLong(sentence: String, max: Int = 300): List<String> {
        if (sentence.length <= max) return listOf(sentence)
        val chunks = mutableListOf<String>()
        val sb = StringBuilder()
        sentence.split(Regex("(?<=[,;:])\\s+")).forEach { piece ->
            if (sb.isNotEmpty() && sb.length + piece.length > max) {
                chunks.add(sb.toString().trim()); sb.clear()
            }
            sb.append(piece).append(' ')
        }
        if (sb.isNotBlank()) chunks.add(sb.toString().trim())
        return chunks
    }

    private companion object {
        const val PARAGRAPH_PAUSE_MS = 280L
    }
}