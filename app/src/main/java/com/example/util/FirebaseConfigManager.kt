package com.example.util

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Initializes and provides direct single-instance access to Firebase with the
 * explicit project configuration provided:
 *
 * apiKey: "AIzaSyAKdQLsaOm_QdSW1poqMp65Hm4oI9X4jSc"
 * authDomain: "muskan-panel-store-website.firebaseapp.com"
 * databaseURL: "https://muskan-panel-store-website-default-rtdb.firebaseio.com"
 * projectId: "muskan-panel-store-website"
 * storageBucket: "muskan-panel-store-website.firebasestorage.app"
 * messagingSenderId: "609573277808"
 * appId: "1:609573277808:web:b670c8911ccd02316024b5"
 */
object FirebaseConfigManager {

    const val API_KEY = "AIzaSyAKdQLsaOm_QdSW1poqMp65Hm4oI9X4jSc"
    const val AUTH_DOMAIN = "muskan-panel-store-website.firebaseapp.com"
    const val DATABASE_URL = "https://muskan-panel-store-website-default-rtdb.firebaseio.com"
    const val PROJECT_ID = "muskan-panel-store-website"
    const val STORAGE_BUCKET = "muskan-panel-store-website.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "609573277808"
    const val APP_ID = "1:609573277808:web:b670c8911ccd02316024b5"

    @Volatile
    private var initialized = false

    fun initialize(context: Context): FirebaseApp {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    val app = if (FirebaseApp.getApps(context).isEmpty()) {
                        val options = FirebaseOptions.Builder()
                            .setApiKey(API_KEY)
                            .setApplicationId(APP_ID)
                            .setDatabaseUrl(DATABASE_URL)
                            .setProjectId(PROJECT_ID)
                            .setStorageBucket(STORAGE_BUCKET)
                            .setGcmSenderId(MESSAGING_SENDER_ID)
                            .build()
                        FirebaseApp.initializeApp(context.applicationContext, options)
                    } else {
                        FirebaseApp.getInstance()
                    }
                    initialized = true
                    return app
                }
            }
        }
        return FirebaseApp.getInstance()
    }

    fun getAuth(context: Context): FirebaseAuth {
        initialize(context)
        return FirebaseAuth.getInstance()
    }

    fun getDatabase(context: Context): FirebaseDatabase {
        initialize(context)
        val db = FirebaseDatabase.getInstance(DATABASE_URL)
        return db
    }
}
