package com.merfemor.aesencryptor.util

object KAssert {
    val isEnabled = true

    inline fun fail(messageSupplier: () -> String) {
        if (isEnabled) {
            throw AssertionError(messageSupplier())
        }
    }
}