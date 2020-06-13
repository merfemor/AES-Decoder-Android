package com.merfemor.aesencryptor.util

import android.util.Log

object KLog {
    val isEnabled = true

    inline fun d(tag: String, messageSupplier: () -> String) {
        if (isEnabled) {
            Log.d(tag, messageSupplier())
        }
    }

    inline fun e(tag: String, messageSupplier: () -> String, throwable: Throwable? = null) {
        if (isEnabled) {
            Log.e(tag, messageSupplier(), throwable)
        }
    }
}