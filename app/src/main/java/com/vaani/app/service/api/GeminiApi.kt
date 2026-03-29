package com.vaani.app.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApi {
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Header("Authorization") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig? = null,
    @SerializedName("systemInstruction")
    val systemInstruction: Instruction? = null
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)

data class Instruction(
    @SerializedName("parts")
    val parts: List<Part>
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Double = 0.2,
    @SerializedName("topK")
    val topK: Int = 40,
    @SerializedName("topP")
    val topP: Double = 0.95,
    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 2048
)

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>?,
    @SerializedName("promptFeedback")
    val promptFeedback: PromptFeedback?
)

data class Candidate(
    @SerializedName("content")
    val content: Content?,
    @SerializedName("finishReason")
    val finishReason: String?,
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRating>?
)

data class SafetyRating(
    @SerializedName("category")
    val category: String?,
    @SerializedName("probability")
    val probability: String?
)

data class PromptFeedback(
    @SerializedName("safetyRatings")
    val safetyRatings: List<SafetyRating>?
)
