package com.mindInk.app.core

private enum class BlockKind { BLANK, TEXT, LIST, QUOTE, TABLE, HEADING, RULE, FENCE }

private val RuleRegex = Regex("^\\s*([-*_])(\\s*\\1){2,}\\s*$")
private val HeadingRegex = Regex("^\\s{0,3}#{1,6}\\s+\\S.*$")
private val BulletRegex = Regex("^(\\s*)[-*+•·]\\s+(.*)$")
private val OrderedRegex = Regex("^\\s*\\d{1,9}[.)]\\s+.*$")
private val QuoteRegex = Regex("^\\s{0,3}>.*$")
private val TableRegex = Regex("^\\s*\\|.*\\|\\s*$")

fun normalizeMarkdown(raw: String): String {
    if (raw.isBlank()) return ""

    val lines = raw
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace('\u00A0', ' ')
        .replace("\u200B", "")
        .split('\n')

    val output = ArrayList<String>(lines.size + 8)
    var previous = BlockKind.BLANK
    var insideFence = false

    for (line in lines) {
        if (insideFence) {
            output += line
            if (isFence(line)) {
                insideFence = false
                previous = BlockKind.FENCE
            }
            continue
        }

        if (line.isBlank()) {
            if (previous != BlockKind.BLANK) output += ""
            previous = BlockKind.BLANK
            continue
        }

        val kind: BlockKind
        val formatted: String

        when {
            isFence(line) -> {
                kind = BlockKind.FENCE
                formatted = line.trim()
            }
            RuleRegex.matches(line) -> {
                kind = BlockKind.RULE
                formatted = line.trim()
            }
            HeadingRegex.matches(line) -> {
                kind = BlockKind.HEADING
                formatted = line.trim()
            }
            BulletRegex.matches(line) -> {
                val match = BulletRegex.matchEntire(line)!!
                kind = BlockKind.LIST
                formatted = match.groupValues[1] + "- " + match.groupValues[2].trimEnd()
            }
            OrderedRegex.matches(line) -> {
                kind = BlockKind.LIST
                formatted = line.trimEnd()
            }
            QuoteRegex.matches(line) -> {
                kind = BlockKind.QUOTE
                formatted = line.trim()
            }
            TableRegex.matches(line) -> {
                kind = BlockKind.TABLE
                formatted = line.trim()
            }
            previous == BlockKind.LIST && line.first().isWhitespace() -> {
                kind = BlockKind.LIST
                formatted = line.trimEnd()
            }
            else -> {
                kind = BlockKind.TEXT
                formatted = line.trim()
            }
        }

        val sameParagraph = previous == kind && (kind == BlockKind.TEXT || kind == BlockKind.QUOTE)

        when {
            sameParagraph -> {
                val last = output.lastIndex
                if (!output[last].endsWith("\\")) output[last] = output[last] + "  "
            }
            previous != BlockKind.BLANK && previous != kind -> output += ""
        }

        output += formatted
        previous = kind

        if (kind == BlockKind.FENCE) insideFence = true
    }

    return output.joinToString("\n").trim()
}

private fun isFence(line: String): Boolean {
    val trimmed = line.trim()
    if (trimmed.startsWith("~~~")) return true
    return trimmed.startsWith("```") && !trimmed.drop(3).contains("```")
}