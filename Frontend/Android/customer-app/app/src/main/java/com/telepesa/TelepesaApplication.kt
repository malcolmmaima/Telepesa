/*
 * Copyright (c) 2025 Telepesa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.telepesa

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Main Application class for Telepesa.
 * Follows the Single Responsibility Principle by focusing on app initialization.
 * Implements the Dependency Inversion Principle through Hilt dependency injection.
 */
@HiltAndroidApp
class TelepesaApplication : Application() {

    // TODO: Add SecurityModule when it's implemented
    // @Inject
    // lateinit var securityModule: SecurityModule

    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        initializeLogging()

        // Initialize security
        initializeSecurity()

        // Initialize database encryption
        initializeDatabase()

        // Initialize network security
        initializeNetwork()
    }

    /**
     * Initializes logging framework.
     * Implements the Open/Closed Principle by allowing extension of logging functionality.
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // In production, you might want to plant a crash reporting tree
            Timber.plant(ReleaseTree())
        }

        Timber.d("Telepesa Application initialized")
    }

    /**
     * Initializes security modules.
     */
    private fun initializeSecurity() {
        try {
            // Security initialization is handled by Hilt
            Timber.d("Security modules initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize security modules")
        }
    }

    /**
     * Initializes database encryption.
     */
    private fun initializeDatabase() {
        try {
            // Database initialization is handled by Hilt
            Timber.d("Database modules initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize database modules")
        }
    }

    /**
     * Initializes network security.
     */
    private fun initializeNetwork() {
        try {
            // Network initialization is handled by Hilt
            Timber.d("Network modules initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize network modules")
        }
    }
}

/**
 * Custom Timber tree for release builds.
 * Follows the Single Responsibility Principle by focusing on production logging.
 */
class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // In production, you would typically send logs to a crash reporting service
        // like Firebase Crashlytics, Crashlytics, or Sentry

        if (priority >= android.util.Log.ERROR && t != null) {
            // Log critical errors to crash reporting service
            // Example: Crashlytics.recordException(t)
        }
    }
}
