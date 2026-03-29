package com.vaani.app.config

object DemoApps {
    
    data class DemoApp(
        val name: String,
        val packageName: String,
        val keywords: List<String>,
        val commonActions: List<ActionHint>
    )

    data class ActionHint(
        val voicePattern: String,
        val language: String,
        val action: String
    )

    val WHATSAPP = DemoApp(
        name = "WhatsApp",
        packageName = "com.whatsapp",
        keywords = listOf("whatsapp", "whatsapp par", "whatsapp में", "whatsapp లो", "whatsapp-ல்", "whatsapp-ದಲ್ಲಿ"),
        commonActions = listOf(
            ActionHint("send message to {contact}", "hi", "open_chat"),
            ActionHint("message to {contact}", "hi", "open_chat"),
            ActionHint("new status", "hi", "new_status"),
            ActionHint("call {contact}", "hi", "voice_call"),
            ActionHint("video call {contact}", "hi", "video_call")
        )
    )

    val YOUTUBE = DemoApp(
        name = "YouTube",
        packageName = "com.google.android.youtube",
        keywords = listOf("youtube", "youtube par", "youtube में", "youtube లो", "youtube-ல்", "youtube-ದಲ್ಲಿ"),
        commonActions = listOf(
            ActionHint("play {song}", "hi", "search_play"),
            ActionHint("search for {query}", "hi", "search"),
            ActionHint("open trending", "hi", "trending"),
            ActionHint("play {video}", "hi", "search_play"),
            ActionHint("subscribe to {channel}", "hi", "subscribe")
        )
    )

    val SWIGGY = DemoApp(
        name = "Swiggy",
        packageName = "in.swiggy.android",
        keywords = listOf("swiggy", "swiggy par", "swiggy में", "swiggy లो", "swiggy-ல்", "swiggy-ದಲ್ಲಿ"),
        commonActions = listOf(
            ActionHint("order {food}", "hi", "search_order"),
            ActionHint("order chai", "hi", "quick_order"),
            ActionHint("delivery to home", "hi", "set_location"),
            ActionHint("check order status", "hi", "order_status"),
            ActionHint("reorder last", "hi", "reorder")
        )
    )

    val INSTAGRAM = DemoApp(
        name = "Instagram",
        packageName = "com.instagram.android",
        keywords = listOf("instagram", "instagram par", "instagram में", "instagram లो", "instagram-ல்", "instagram-ದಲ್ಲಿ"),
        commonActions = listOf(
            ActionHint("post photo", "hi", "new_post"),
            ActionHint("check notifications", "hi", "notifications"),
            ActionHint("open reels", "hi", "reels"),
            ActionHint("send message", "hi", "dm"),
            ActionHint("search for {query}", "hi", "search")
        )
    )

    val GOOGLE_MAPS = DemoApp(
        name = "Google Maps",
        packageName = "com.google.android.apps.maps",
        keywords = listOf("maps", "google maps", "map par", "maps में", "maps లो", "maps-ல்", "maps-ದಲ್ಲಿ"),
        commonActions = listOf(
            ActionHint("navigate to {place}", "hi", "navigate"),
            ActionHint("nearby {place}", "hi", "nearby"),
            ActionHint("directions to {place}", "hi", "directions"),
            ActionHint("share location", "hi", "share_location"),
            ActionHint("find {place}", "hi", "search")
        )
    )

    val allApps = listOf(WHATSAPP, YOUTUBE, SWIGGY, INSTAGRAM, GOOGLE_MAPS)

    fun findApp(keyword: String): DemoApp? {
        val lower = keyword.lowercase()
        return allApps.find { app ->
            app.keywords.any { lower.contains(it.lowercase()) }
        }
    }

    fun getActionHint(app: DemoApp, voiceCommand: String): String? {
        val lower = voiceCommand.lowercase()
        return app.commonActions.find { hint ->
            lower.contains(hint.voicePattern.substringBefore(" ").lowercase())
        }?.action
    }
}
