package com.github.kevskitv.webrtckmp

import android.content.Context
import androidx.startup.Initializer

public class WebRtcInitializer : Initializer<WebRtc> {
    override fun create(context: Context): WebRtc {
        WebRtc.initializeApplicationContext(context)
        return WebRtc
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
