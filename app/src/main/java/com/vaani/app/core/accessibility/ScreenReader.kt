package com.vaani.app.core.accessibility

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object ScreenReader {
    private const val TAG = "ScreenReader"
    private val gson = Gson()
    private const val MAX_DEPTH = 12

    fun serializeNode(node: AccessibilityNodeInfo, depth: Int): String {
        if (depth > MAX_DEPTH) return "{}"
        if (!node.isVisibleToUser) return "{}"

        val obj = JsonObject()
        
        node.viewIdResourceName?.let { obj.addProperty("resourceId", it) }
        node.text?.let { obj.addProperty("text", it.toString()) }
        node.contentDescription?.let { obj.addProperty("contentDescription", it.toString()) }
        node.className?.let { obj.addProperty("className", it.toString()) }
        
        obj.addProperty("isClickable", node.isClickable)
        obj.addProperty("isEditable", node.isEditable)
        obj.addProperty("isScrollable", node.isScrollable)
        obj.addProperty("isEnabled", node.isEnabled)
        
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val boundsObj = JsonObject()
        boundsObj.addProperty("left", bounds.left)
        boundsObj.addProperty("top", bounds.top)
        boundsObj.addProperty("right", bounds.right)
        boundsObj.addProperty("bottom", bounds.bottom)
        obj.add("bounds", boundsObj)

        if (node.childCount > 0) {
            val childrenArray = JsonArray()
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    val childJson = serializeNode(child, depth + 1)
                    if (childJson != "{}") {
                        childrenArray.add(gson.fromJson(childJson, JsonObject::class.java))
                    }
                }
            }
            if (childrenArray.size() > 0) {
                obj.add("children", childrenArray)
            }
        }

        return gson.toJson(obj)
    }
}
