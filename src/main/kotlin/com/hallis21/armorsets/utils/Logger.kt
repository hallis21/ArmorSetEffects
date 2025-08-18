package com.hallis21.armorsets.utils

import java.util.logging.Level
import java.util.logging.Logger as JavaLogger

object Logger {
    private var logger: JavaLogger? = null
    private var debugEnabled = false

    fun initialize(
        javaLogger: JavaLogger,
        debug: Boolean = false,
    ) {
        logger = javaLogger
        debugEnabled = debug
    }

    fun info(message: String) {
        logger?.info(message)
    }

    fun warn(message: String) {
        logger?.warning(message)
    }

    fun error(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            logger?.log(Level.SEVERE, message, throwable)
        } else {
            logger?.severe(message)
        }
    }

    fun debug(message: String) {
        if (debugEnabled) {
            logger?.info("[DEBUG] $message")
        }
    }

    fun setDebugEnabled(enabled: Boolean) {
        debugEnabled = enabled
    }
}
