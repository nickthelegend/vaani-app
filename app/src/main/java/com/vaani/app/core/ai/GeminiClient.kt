package com.vaani.app.core.ai

import android.util.Log
import com.google.gson.*
import com.vaani.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {

    private val gson: Gson = GsonBuilder().setLenient().create()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val geminiApi = retrofit.create(GeminiApi::class.java)
    
    // Constant for the API Key as requested
    private val API_KEY = "AIzaSyCtgQyB1jvbcDqqw1grik2NB4QqVaCjSK0"

    suspend fun parseIntent(voiceInput: String, language: AppLanguage): ParsedIntent = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = SystemPrompts.INTENT_PARSER_PROMPT
            val userPrompt = "VOICE INPUT: $voiceInput\nLANGUAGE: ${language.displayName}"

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                systemInstruction = Instruction(parts = listOf(Part(text = systemPrompt))),
                generationConfig = GenerationConfig(temperature = 0.1f, maxOutputTokens = 256)
            )

            val response = geminiApi.generateContent(key = API_KEY, request = request)
            val responseText = parseResponse(response) ?: "{}"
            
            parseParsedIntent(responseText, language)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Intent parsing failed", e)
            ParsedIntent(language = language.code, translatedEnglish = voiceInput)
        }
    }

    suspend fun getActionPlan(screenTreeJson: String, intent: ParsedIntent): List<AgentAction> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = SystemPrompts.AGENT_PROMPT
            val userPrompt = """
                SCREEN TREE:
                $screenTreeJson
                
                USER INTENT:
                ${intent.translatedEnglish}
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = userPrompt)))),
                systemInstruction = Instruction(parts = listOf(Part(text = systemPrompt))),
                generationConfig = GenerationConfig(temperature = 0.1f, maxOutputTokens = 1024)
            )

            val response = geminiApi.generateContent(key = API_KEY, request = request)
            val responseText = parseResponse(response) ?: "[]"
            
            parseActionList(responseText)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Action planning failed", e)
            emptyList()
        }
    }

    private fun parseResponse(response: Response<GeminiResponse>): String? {
        if (!response.isSuccessful) return null
        return response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }

    private fun parseParsedIntent(jsonStr: String, language: AppLanguage): ParsedIntent {
        return try {
            val cleanJson = jsonStr.trim().removeSurrounding("```json", "```").trim()
            val obj = JsonParser.parseString(cleanJson).asJsonObject
            ParsedIntent(
                app = obj.get("app")?.asString,
                packageName = obj.get("package")?.asString,
                action = obj.get("action")?.asString ?: "UNKNOWN",
                translatedEnglish = obj.get("translated_english")?.asString ?: "",
                language = language.code
            )
        } catch (e: Exception) {
            ParsedIntent(language = language.code)
        }
    }

    private fun parseActionList(jsonStr: String): List<AgentAction> {
        return try {
            val cleanJson = jsonStr.trim().removeSurrounding("```json", "```").trim()
            val array = JsonParser.parseString(cleanJson).asJsonArray
            array.map { elem ->
                val obj = elem.asJsonObject
                AgentAction(
                    type = ActionType.valueOf(obj.get("action").asString),
                    resourceId = obj.get("resourceId")?.asString,
                    text = obj.get("text")?.asString
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// API Models for Retrofit
interface GeminiApi {
    @POST("models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Query("key") key: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Instruction? = null,
    val generationConfig: GenerationConfig? = null
)

data class Content(val parts: List<Part>)
data class Part(val text: String)
data class Instruction(val parts: List<Part>)
data class GenerationConfig(val temperature: Float, val maxOutputTokens: Int)

data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: Content)
