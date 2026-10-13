package com.franklider.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class CleaningAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Experimental: only used if the user manually enables it.
    }

    override fun onInterrupt() {
    }
}
