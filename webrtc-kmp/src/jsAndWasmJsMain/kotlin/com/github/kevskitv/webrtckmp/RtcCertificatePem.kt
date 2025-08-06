package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.externals.RTCCertificate
import com.github.kevskitv.webrtckmp.externals.generateRTCCertificate

public actual class RtcCertificatePem internal constructor(
    internal val js: RTCCertificate,
) {
    public actual val privateKey: String get() = ""
    public actual val certificate: String get() = ""

    public actual companion object {
        public actual suspend fun generateCertificate(
            keyType: KeyType,
            expires: Long,
        ): RtcCertificatePem = RtcCertificatePem(generateRTCCertificate(keyType, expires))
    }
}
