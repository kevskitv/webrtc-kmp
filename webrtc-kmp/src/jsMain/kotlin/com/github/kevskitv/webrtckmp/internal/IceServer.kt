package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.IceServer
import kotlin.js.Json
import kotlin.js.json

internal fun IceServer.toPlatform(): Json = json(
    "urls" to urls.toTypedArray(),
    "username" to username,
    "credential" to password
)
