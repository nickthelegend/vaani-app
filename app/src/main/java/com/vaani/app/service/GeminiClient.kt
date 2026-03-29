package com.vaani.app.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.vaani.app.config.AppConfig
import com.vaani.app.config.SystemPrompts
import com.vaani.app.data.model.AgentAction
import com.vaani.app.data.model.ScreenNode
import com.vaani.app.service.api.GeminiRequest
import com.vaani.app.service.api.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {

    companion object {
        private const val TAG = "GeminiClient"
    }

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.GEMINI_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val geminiApi = retrofit.create(com.vaani.app.service.api.GeminiApi::class.java)

    suspend fun generateActions(
        screenNode: ScreenNode,
        userCommand: String,
        retryCount: Int = 0
    ): List<AgentAction> = withContext(Dispatchers.IO) {
        try {
            val screenTreeJson = buildScreenTreeJson(screenNode)
            
            val userPrompt = """
                SCREEN TREE:
                $screenTreeJson
                
                USER COMMAND: $userCommand
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    com.vaani.app.service.api.Content(
                        parts = listOf(
                            com.vaani.app.service.api.Part(text = userPrompt)
                        )
                    )
                ),
                systemInstruction = com.vaani.app.service.api.Instruction(
                    parts = listOf(
                        com.vaani.app.service.api.Part(text = SystemPrompts.AGENT_PROMPT)
                    )
                ),
                generationConfig = com.vaani.app.service.api.GenerationConfig(
                    temperature = AppConfig.TEMPERATURE,
                    maxOutputTokens = AppConfig.MAX_TOKENS
                )
            )

            Log.d(TAG, "Sending request to Gemini...")
            val response = geminiApi.generateContent(
                apiKey = "Bearer ${AppConfig.GEMINI_API_KEY}",
                request = request
            )

            val responseText = parseResponse(response)
            
            if (responseText.isNullOrBlank()) {
                Log.w(TAG, "Empty response from Gemini")
                if (retryCount < AppConfig.MAX_RETRIES) {
                    return@withContext generateActions(screenNode, userCommand, retryCount + 1)
                }
                return@withContext emptyList()
            }

            parseActionsFromResponse(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating actions", e)
            if (retryCount < AppConfig.MAX_RETRIES) {
                Log.d(TAG, "Retrying... ($retryCount/${AppConfig.MAX_RETRIES})")
                return@withContext generateActions(screenNode, userCommand, retryCount + 1)
            }
            emptyList()
        }
    }

    suspend fun parseIntent(userCommand: String): IntentParseResult? = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    com.vaani.app.service.api.Content(
                        parts = listOf(
                            com.vaani.app.service.api.Part(text = userCommand)
                        )
                    )
                ),
                systemInstruction = com.vaani.app.service.api.Instruction(
                    parts = listOf(
                        com.vaani.app.service.api.Part(text = SystemPrompts.INTENT_PARSER_PROMPT)
                    )
                ),
                generationConfig = com.vaani.app.service.api.GenerationConfig(
                    temperature = 0.1,
                    maxOutputTokens = 256
                )
            )

            val response = geminiApi.generateContent(
                apiKey = "Bearer ${AppConfig.GEMINI_API_KEY}",
                request = request
            )

            val responseText = parseResponse(response) ?: return@withContext null
            
            parseIntentFromResponse(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing intent", e)
            null
        }
    }

    suspend fun resolveAppName(appName: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = GeminiRequest(
                contents = listOf(
                    com.vaani.app.service.api.Content(
                        parts = listOf(
                            com.vaani.app.service.api.Part(text = appName)
                        )
                    )
                ),
                systemInstruction = com.vaani.app.service.api.Instruction(
                    parts = listOf(
                        com.vaani.app.service.api.Part(text = SystemPrompts.APP_FALLBACK_PROMPT)
                    )
                ),
                generationConfig = com.vaani.app.service.api.GenerationConfig(
                    temperature = 0.0,
                    maxOutputTokens = 64
                )
            )

            val response = geminiApi.generateContent(
                apiKey = "Bearer ${AppConfig.GEMINI_API_KEY}",
                request = request
            )

            parseResponse(response)?.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving app name", e)
            null
        }
    }

    suspend fun verifyTaskCompletion(
        screenNode: ScreenNode,
        originalCommand: String
    ): VerificationResult? = withContext(Dispatchers.IO) {
        try {
            val screenTreeJson = buildScreenTreeJson(screenNode)
            
            val userPrompt = """
                ORIGINAL COMMAND: $originalCommand
                
                CURRENT SCREEN:
                $screenTreeJson
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    com.vaani.app.service.api.Content(
                        parts = listOf(
                            com.vaani.app.service.api.Part(text = userPrompt)
                        )
                    )
                ),
                systemInstruction = com.vaani.app.service.api.Instruction(
                    parts = listOf(
                        com.vaani.app.service.api.Part(text = SystemPrompts.CONFIRMATION_PROMPT)
                    )
                ),
                generationConfig = com.vaani.app.service.api.GenerationConfig(
                    temperature = 0.1,
                    maxOutputTokens = 256
                )
            )

            val response = geminiApi.generateContent(
                apiKey = "Bearer ${AppConfig.GEMINI_API_KEY}",
                request = request
            )

            val responseText = parseResponse(response) ?: return@withContext null
            parseVerificationResult(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying task completion", e)
            null
        }
    }

    private fun parseResponse(response: Response<GeminiResponse>): String? {
        if (!response.isSuccessful) {
            Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
            return null
        }

        val body = response.body()
        if (body == null) {
            Log.e(TAG, "Empty response body")
            return null
        }

        val text = body.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text

        if (text.isNullOrBlank()) {
            Log.w(TAG, "No text in response")
            return null
        }

        Log.d(TAG, "Response received: ${text.take(200)}...")
        return text
    }

    private fun parseActionsFromResponse(responseText: String): List<AgentAction> {
        return try {
            val cleanJson = responseText
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .replace("```json\n", "")
                .trim()

            val jsonArray = JsonParser.parseString(cleanJson).asJsonArray

            jsonArray.mapNotNull { jsonElement ->
                try {
                    val obj = jsonElement.asJsonObject
                    val actionStr = obj.get("action")?.asString ?: return@mapNotNull null
                    
                    val action = when (actionStr.uppercase()) {
                        "OPEN_APP" -> ActionType.LAUNCH_APP
                        "CLICK" -> ActionType.CLICK
                        "LONG_CLICK" -> ActionType.CLICK
                        "TYPE" -> ActionType.TYPE
                        "CLEAR_AND_TYPE" -> ActionType.TYPE
                        "SCROLL_UP" -> ActionType.SCROLL_UP
                        "SCROLL_DOWN" -> ActionType.SCROLL_DOWN
                        "SWIPE_LEFT" -> ActionType.SCROLL_UP
                        "SWIPE_RIGHT" -> ActionType.SCROLL_DOWN
                        "BACK" -> ActionType.BACK
                        "HOME" -> ActionType.HOME
                        "WAIT" -> ActionType.WAIT
                        "VERIFY" -> ActionType.WAIT
                        "UNKNOWN" -> ActionType.WAIT
                        else -> return@mapNotNull null
                    }

                    AgentAction(
                        type = action,
                        resourceId = obj.get("resourceId")?.asString,
                        text = obj.get("text")?.asString,
                        appPackage = obj.get("package")?.asString,
                        description = obj.get("text")?.asString ?: actionStr
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse action: ${e.message}")
                    null
                }
            }.also { actions ->
                Log.d(TAG, "Parsed ${actions.size} actions")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response as JSON: ${e.message}")
            emptyList()
        }
    }

    private fun parseIntentFromResponse(responseText: String): IntentParseResult? {
        return try {
            val cleanJson = responseText
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val obj = JsonParser.parseString(cleanJson).asJsonObject

            IntentParseResult(
                app = obj.get("app")?.asString,
                packageName = obj.get("package")?.asString,
                action = obj.get("action")?.asString,
                parameters = parseParameters(obj.get("parameters")?.asJsonObject),
                language = obj.get("language")?.asString
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse intent: ${e.message}")
            null
        }
    }

    private fun parseParameters(paramsObj: JsonObject?): Map<String, String> {
        if (paramsObj == null) return emptyMap()
        
        return paramsObj.entry().associate { (key, value) ->
            key to value.asString
        }
    }

    private fun parseVerificationResult(responseText: String): VerificationResult? {
        return try {
            val cleanJson = responseText
                .trim()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val obj = JsonParser.parseString(cleanJson).asJsonObject

            VerificationResult(
                completed = obj.get("completed")?.asBoolean ?: false,
                evidence = obj.get("evidence")?.asString ?: "",
                nextAction = obj.get("nextAction")?.asString
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse verification result: ${e.message}")
            null
        }
    }

    private fun buildScreenTreeJson(node: ScreenNode, depth: Int = 0): String {
        if (depth > AppConfig.SCREEN_TREE_MAX_DEPTH) return "{}"

        val obj = JsonObject()
        
        node.resourceId?.let { obj.addProperty("resourceId", it) }
        node.text?.let { obj.addProperty("text", it) }
        node.contentDescription?.let { obj.addProperty("contentDescription", it) }
        node.className?.let { obj.addProperty("className", it) }
        obj.addProperty("clickable", node.clickable)
        obj.addProperty("editable", node.editable)
        
        node.bounds?.let { bounds ->
            val boundsObj = JsonObject()
            boundsObj.addProperty("left", bounds.left)
            boundsObj.addProperty("top", bounds.top)
            boundsObj.addProperty("right", bounds.right)
            boundsObj.addProperty("bottom", bounds.bottom)
            obj.add("bounds", boundsObj)
        }

        if (node.children.isNotEmpty()) {
            val childrenArray = JsonArray()
            node.children.take(10).forEach { child ->
                childrenArray.add(JsonParser.parseString(buildScreenTreeJson(child, depth + 1)))
            }
            obj.add("children", childrenArray)
        }

        return gson.toJson(obj)
    }

    data class IntentParseResult(
        val app: String?,
        val packageName: String?,
        val action: String?,
        val parameters: Map<String, String>,
        val language: String?
    )

    data class VerificationResult(
        val completed: Boolean,
        val evidence: String,
        val nextAction: String?
    )
}
