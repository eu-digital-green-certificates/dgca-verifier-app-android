package com.android.app.base

import android.content.Intent

/**
 * To support any type of QR code module should extend this interface.
 * Main app will use Processor abstraction to prefetch data and it will use module entry point if applicable.
 */
interface Processor {

    /**
     * Prefetch data that is required for validation
     */
    fun prefetchData()

    /**
     * Check if input string can be handled by this processor
     *
     * @param input string of type covid certificate, verifiable credentials etc.
     * @return Intent? entry point to specific module if can handle such input, null otherwise.
     */
    fun isApplicable(input: String): Intent?

    /**
     * Each module can provide own settings implementation
     *
     * @return Pair<String, Intent>? pair of title and intent action for redirection.
     */
    fun getSettingsIntent(): Pair<String, Intent>?
}