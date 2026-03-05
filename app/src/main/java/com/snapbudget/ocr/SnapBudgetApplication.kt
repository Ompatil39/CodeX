package com.snapbudget.ocr

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

class SnapBudgetApplication : Application() {

    companion object {
        private const val TAG = "SnapBudgetApp"

        /** App-wide Firebase Analytics instance */
        lateinit var analytics: FirebaseAnalytics
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Support dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Initialize Firebase
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            // Analytics – enabled by default, capture screen views & events
            analytics = FirebaseAnalytics.getInstance(this)
            analytics.setAnalyticsCollectionEnabled(true)

            // Crashlytics – enable automatic crash & non-fatal reports
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
            }

            // Performance Monitoring – enable automatic traces
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true

            Log.i(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
        }
    }
}