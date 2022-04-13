package com.android.app.base

import android.content.Intent

interface Processor {

    fun prefetchData()

    fun isApplicable(input: String): Intent?

    fun getSettingsIntent(): Pair<String, Intent>
}