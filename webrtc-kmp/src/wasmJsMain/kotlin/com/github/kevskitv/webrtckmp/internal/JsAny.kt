package com.github.kevskitv.webrtckmp.internal

internal inline fun <reified T> JsAny.asTypeOrNull(): T? {
    val any = this as Any

    println("any: ${this::class}")

    return any as? T
}
