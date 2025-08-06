package com.github.kevskitv.webrtckmp

import java.nio.ByteBuffer

internal fun ByteBuffer.toByteArray(): ByteArray {
    val bytes = ByteArray(remaining())
    get(bytes)
    rewind()
    return bytes
}
