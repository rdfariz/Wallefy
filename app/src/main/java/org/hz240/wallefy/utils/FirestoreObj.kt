package org.hz240.wallefy.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source

object FirestoreObj {
    val _db = FirebaseFirestore.getInstance()
    val _auth = FirebaseAuth.getInstance()
    val _settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build()
    var _sourceDynamic: Source = Source.DEFAULT
    fun changeSource(newSource: Source) {
        _sourceDynamic = newSource
    }

    init {
        _db.firestoreSettings = _settings
    }
}