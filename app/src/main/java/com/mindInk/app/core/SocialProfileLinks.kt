package com.mindInk.app.core

import java.net.URI

fun normalizeSocialProfileUrl(value: String, baseUrl: String): String? {
    val input = value.trim().removePrefix("@").trim().trimEnd('/')
    val base = baseUrl.trim().trimEnd('/')
    if (input.isBlank() || base.isBlank()) return null

    val baseUri = parseUri(if (base.hasHttpScheme()) base else "https://$base") ?: return null
    val baseHost = baseUri.host?.removePrefix("www.") ?: return null
    val inputHasScheme = input.hasHttpScheme()
    val inputHost = input.substringBefore('/').lowercase()
    val address = when {
        inputHasScheme -> input
        inputHost == baseHost || inputHost == "www.$baseHost" -> "https://$input"
        else -> "$base/${input.trimStart('/')}"
    }
    val uri = parseUri(address) ?: return null
    val uriHost = uri.host?.removePrefix("www.") ?: return null

    if (uri.scheme.lowercase() !in setOf("http", "https")) return null
    if (!uriHost.equals(baseHost, ignoreCase = true)) return null

    return uri.toASCIIString()
}

private fun String.hasHttpScheme(): Boolean =
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)

private fun parseUri(value: String): URI? =
    runCatching { URI(value) }.getOrNull()