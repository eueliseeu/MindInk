package com.mindInk.app.core

import java.net.URI

fun normalizeGithubProfileUrl(value: String): String? =
    normalizeSocialProfileUrl(
        value = value,
        allowedHosts = setOf("github.com"),
        profileBase = "https://github.com"
    )

fun normalizeLinkedInProfileUrl(value: String): String? =
    normalizeSocialProfileUrl(
        value = value,
        allowedHosts = setOf("linkedin.com"),
        profileBase = "https://www.linkedin.com/in"
    )

fun normalizeSocialProfileUrl(
    value: String,
    allowedHosts: Set<String>,
    profileBase: String
): String? {
    val input = value
        .trim()
        .removePrefix("@")
        .trim()
        .trimEnd('/')
    if (input.isBlank()) return null

    val address = when {
        input.hasHttpScheme() -> input
        input.contains('.') -> "https://${input.trimStart('/')}"
        else -> {
            val username = input
                .substringAfterLast('/')
                .trim()
                .takeIf { it.isNotBlank() && ' ' !in it }
                ?: return null
            "${profileBase.trimEnd('/')}/$username"
        }
    }

    val uri = parseUri(address) ?: return null
    if (!uri.scheme.isHttpScheme()) return null

    val host = uri.normalizedHost() ?: return null
    if (host !in allowedHosts) return null

    val path = uri.path?.trim('/')?.takeIf { it.isNotBlank() } ?: return null
    val finalPath = if (host == "linkedin.com") {
        when {
            path.startsWith("in/", ignoreCase = true) -> path
            path.equals("in", ignoreCase = true) -> return null
            else -> "in/${path.substringAfterLast('/')}"
        }
    } else {
        path
    }

    val displayHost = if (host == "linkedin.com") "www.linkedin.com" else host
    return "https://$displayHost/$finalPath"
}

private fun String.hasHttpScheme(): Boolean =
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)

private fun String?.isHttpScheme(): Boolean =
    this.equals("http", ignoreCase = true) || this.equals("https", ignoreCase = true)

private fun URI.normalizedHost(): String? =
    host?.lowercase()?.removePrefix("www.")

private fun parseUri(value: String): URI? =
    runCatching { URI(value) }.getOrNull()