package com.mindInk.app.core

const val MAX_ARTICLE_WORDS = 10_000
const val MAX_ARTICLE_CHARS = 100_000

fun countWords(text: String): Int {
    var count = 0
    var inWord = false
    for (ch in text) {
        if (ch.isWhitespace()) {
            inWord = false
        } else if (!inWord) {
            inWord = true
            count++
        }
    }
    return count
}
