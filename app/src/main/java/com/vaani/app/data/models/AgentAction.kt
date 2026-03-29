package com.vaani.app.data.models

data class AgentAction(
    val type: ActionType,
    val resourceId: String? = null,
    val text: String? = null
)

enum class ActionType {
    OPEN_APP, CLICK, LONG_CLICK, TYPE, CLEAR_TYPE,
    SCROLL_UP, SCROLL_DOWN, SWIPE_LEFT, SWIPE_RIGHT,
    BACK, HOME, WAIT, VERIFY, UNKNOWN
}
