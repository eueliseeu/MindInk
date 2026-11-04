package com.mindInk.app.ui.articles.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object MarkdownFormatter {

    private val ListPattern = Regex("^(\\s*)([-*+]|\\d+\\.)\\s(.*)$")
    private val NumberedPattern = Regex("^\\d+\\.\\s")
    private val LinePrefixPattern = Regex("^(#{1,6}\\s|[-*+]\\s|>\\s?|\\d+\\.\\s)")

    private const val HEADING_MARKER = "## "
    private const val BULLET_MARKER = "- "
    private const val QUOTE_MARKER = "> "

    fun sanitize(value: TextFieldValue): TextFieldValue {
        val text = value.text
        if (text.none { it == '\r' || it == '\u00A0' }) return value

        val cleaned = text.normalizeBreaks().replace('\u00A0', ' ')
        val start = text.substring(0, value.selection.start).normalizeBreaks().length
        val end = text.substring(0, value.selection.end).normalizeBreaks().length

        return TextFieldValue(
            text = cleaned,
            selection = TextRange(start.coerceIn(0, cleaned.length), end.coerceIn(0, cleaned.length))
        )
    }

    fun continueList(previous: TextFieldValue, current: TextFieldValue): TextFieldValue {
        val text = current.text
        val cursor = current.selection.start
        val insertedLineBreak = current.selection.collapsed &&
            text.length == previous.text.length + 1 &&
            cursor > 0 &&
            text[cursor - 1] == '\n'

        if (!insertedLineBreak) return current

        val lineStart = text.lastIndexOf('\n', cursor - 2) + 1
        val line = text.substring(lineStart, cursor - 1)
        val match = ListPattern.matchEntire(line) ?: return current

        val indent = match.groupValues[1]
        val marker = match.groupValues[2]
        val content = match.groupValues[3]

        if (content.isBlank()) {
            val newText = text.substring(0, lineStart) + text.substring(cursor)
            return TextFieldValue(newText, TextRange(lineStart))
        }

        val nextMarker = if (marker.endsWith(".")) {
            val number = marker.dropLast(1).toIntOrNull() ?: return current
            "${number + 1}."
        } else {
            marker
        }
        val insertion = "$indent$nextMarker "
        val newText = text.substring(0, cursor) + insertion + text.substring(cursor)

        return TextFieldValue(newText, TextRange(cursor + insertion.length))
    }

    fun wrap(
        value: TextFieldValue,
        prefix: String,
        suffix: String,
        placeholder: String
    ): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end)

        val selectionIncludesWrapper = selected.length >= prefix.length + suffix.length &&
            selected.startsWith(prefix) &&
            selected.endsWith(suffix)
        val selectionIsSurrounded = start >= prefix.length &&
            text.startsWith(prefix, start - prefix.length) &&
            text.startsWith(suffix, end)

        return when {
            selectionIncludesWrapper -> {
                val inner = selected.substring(prefix.length, selected.length - suffix.length)
                replaceRange(value, start, end, inner, start, start + inner.length)
            }
            selectionIsSurrounded -> {
                val newText = text.substring(0, start - prefix.length) +
                    selected +
                    text.substring(end + suffix.length)
                TextFieldValue(newText, TextRange(start - prefix.length, end - prefix.length))
            }
            else -> {
                val inner = selected.ifEmpty { placeholder }
                val innerStart = start + prefix.length
                replaceRange(
                    value,
                    start,
                    end,
                    prefix + inner + suffix,
                    innerStart,
                    innerStart + inner.length
                )
            }
        }
    }

    fun link(value: TextFieldValue): TextFieldValue {
        val start = value.selection.min
        val end = value.selection.max
        val selected = value.text.substring(start, end)
        val label = selected.ifEmpty { "texto do link" }
        val url = "https://"
        val replacement = "[$label]($url)"

        return if (selected.isEmpty()) {
            replaceRange(value, start, end, replacement, start + 1, start + 1 + label.length)
        } else {
            val urlStart = start + label.length + 3
            replaceRange(value, start, end, replacement, urlStart, urlStart + url.length)
        }
    }

    fun code(value: TextFieldValue): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val selected = text.substring(start, end)

        if (!selected.contains('\n')) return wrap(value, "`", "`", "código")

        val leading = if (start > 0 && text[start - 1] != '\n') "\n" else ""
        val opening = "$leading```\n"
        val replacement = opening + selected + "\n```"
        val innerStart = start + opening.length

        return replaceRange(value, start, end, replacement, innerStart, innerStart + selected.length)
    }

    fun heading(value: TextFieldValue): TextFieldValue = togglePrefix(value, HEADING_MARKER)

    fun bullet(value: TextFieldValue): TextFieldValue = togglePrefix(value, BULLET_MARKER)

    fun quote(value: TextFieldValue): TextFieldValue = togglePrefix(value, QUOTE_MARKER)

    fun numbered(value: TextFieldValue): TextFieldValue = transformLines(value) { lines ->
        val hasContent = lines.any { it.isNotBlank() }
        val targets = if (hasContent) lines.filter { it.isNotBlank() } else lines
        val allNumbered = targets.all { NumberedPattern.containsMatchIn(it) }
        var index = 0

        lines.map { line ->
            when {
                hasContent && line.isBlank() -> line
                allNumbered -> NumberedPattern.replaceFirst(line, "")
                else -> {
                    index++
                    "$index. " + LinePrefixPattern.replaceFirst(line, "")
                }
            }
        }
    }

    private fun togglePrefix(value: TextFieldValue, marker: String): TextFieldValue =
        transformLines(value) { lines ->
            val hasContent = lines.any { it.isNotBlank() }
            val targets = if (hasContent) lines.filter { it.isNotBlank() } else lines
            val allMarked = targets.all { it.startsWith(marker) }

            lines.map { line ->
                when {
                    hasContent && line.isBlank() -> line
                    allMarked -> line.removePrefix(marker)
                    else -> marker + LinePrefixPattern.replaceFirst(line, "")
                }
            }
        }

    private fun transformLines(
        value: TextFieldValue,
        transform: (List<String>) -> List<String>
    ): TextFieldValue {
        val text = value.text
        val start = value.selection.min
        val end = value.selection.max
        val effectiveEnd = if (end > start && text[end - 1] == '\n') end - 1 else end

        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val lineEnd = text.indexOf('\n', effectiveEnd).let { if (it == -1) text.length else it }

        val block = text.substring(lineStart, lineEnd)
        val transformed = transform(block.split('\n')).joinToString("\n")
        val newText = text.substring(0, lineStart) + transformed + text.substring(lineEnd)

        val selection = if (value.selection.collapsed) {
            TextRange(lineStart + transformed.length)
        } else {
            TextRange(lineStart, lineStart + transformed.length)
        }

        return TextFieldValue(newText, selection)
    }

    private fun replaceRange(
        value: TextFieldValue,
        start: Int,
        end: Int,
        replacement: String,
        selectionStart: Int,
        selectionEnd: Int
    ): TextFieldValue {
        val newText = value.text.substring(0, start) + replacement + value.text.substring(end)
        return TextFieldValue(newText, TextRange(selectionStart, selectionEnd))
    }

    private fun String.normalizeBreaks(): String = replace("\r\n", "\n").replace('\r', '\n')
}