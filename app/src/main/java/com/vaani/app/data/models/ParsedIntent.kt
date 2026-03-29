package com.vaani.app.data.models

data class ParsedIntent(
    val app: String? = null,
    val packageName: String? = null,
    val action: String = "UNKNOWN",
    val parameters: Map<String, String> = emptyMap(),
    val language: String = "en-IN",
    val translatedEnglish: String = ""
)
