package com.ngengeapps.zicam

import android.view.View

/**
 *Adapted from https://androidwave.com/double-tap-listener-android-example/
 * The listener is set up such that onDoubleClick is called when the difference
 * between clicks is less than 300 milliseconds
 */
abstract class OnDoubleClickListener:View.OnClickListener {
    private var lastClickTime = 0L
    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < ON_DOUBLE_TAP_TIME_DELTA) {
            onDoubleClick(view)
            lastClickTime = 0L
        }
        lastClickTime = currentTime
    }

    companion object {
        private const val ON_DOUBLE_TAP_TIME_DELTA = 300
    }
    abstract fun onDoubleClick(view: View)
}