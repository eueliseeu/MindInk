package com.mindInk.app.core

import java.net.URI

fun normalizeSocialProfileUrl(value: String, baseUrl: String): String? {
    val input = value.trim().removePrefix("@").trim().trimEnd('/')
    val base = baseUrl.trim().trimEnd('/')
    if (input.isBlank() || base.isBlank()) return null

    val baseWithScheme = if (base.hasHttpScheme()) base else "https://$base"
    val baseHost = parseUri(baseWithScheme)?.normalizedHost() ?: return null

    val inputHost = input.substringBefore('/').lowercase()
    val address = when {
        input.hasHttpScheme() -> input
        inputHost == baseHost || inputHost == "www.$baseHost" -> "https://$input"
        else -> "$baseWithScheme/${input.trimStart('/')}"
    }

    val uri = parseUri(address) ?: return null
    if (!uri.scheme.isHttpScheme()) return null
    if (uri.normalizedHost() != baseHost) return null

    return uri.toASCIIString()
}

private fun String.hasHttpScheme(): Boolean =
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)

private fun String?.isHttpScheme(): Boolean =
    this.equals("http", ignoreCase = true) || this.equals("https", ignoreCase = true)

private fun URI.normalizedHost(): String? =
    host?.lowercase()?.removePrefix("www.")

private fun parseUri(value: String): URI? =
    runCatching { URI(value) }.getOrNull()
