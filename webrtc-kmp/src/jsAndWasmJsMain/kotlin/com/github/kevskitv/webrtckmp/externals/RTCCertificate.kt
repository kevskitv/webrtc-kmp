package com.github.kevskitv.webrtckmp.externals

import com.github.kevskitv.webrtckmp.KeyType

internal external interface RTCCertificate {
    val expires: Date
}

internal expect suspend fun generateRTCCertificate(
    keyType: KeyType,
    expires: Long,
): RTCCertificate
