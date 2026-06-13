package com.majesty.lat

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LaTService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}

    fun processVoiceCommand(command: String) {
        val cleaned = command.lowercase().trim()
        
        when {
            cleaned == "go back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            cleaned == "go home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            cleaned == "scroll down" -> runScrollDownGesture()
            cleaned.startsWith("open ") -> launchAppPackage(cleaned.substring(5))
            cleaned.startsWith("tap ") -> tapScreenElement(cleaned.substring(4))
        }
    }

    private fun launchAppPackage(appName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(
            when (appName) {
                "youtube" -> "com.google.android.youtube"
                "whatsapp" -> "com.whatsapp"
                else -> return
            }
        )
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent?.let { startActivity(it) }
    }

    private fun runScrollDownGesture() {
        val swipePath = Path().apply {
            moveTo(500f, 1600f)
            lineTo(500f, 400f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 350))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun tapScreenElement(targetText: String) {
        val rootNode = rootInActiveWindow ?: return
        val matchingNodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        if (!matchingNodes.isNullOrEmpty()) {
            for (node in matchingNodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return
                    }
                    parent = parent.parent
                }
            }
        }
    }
}
