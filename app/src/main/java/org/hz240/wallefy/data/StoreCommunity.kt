package org.hz240.wallefy.data

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.Exception

class StoreCommunity(c: Context?, v: View?) {
    private val context = c
    private val view = v

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(
        FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build()

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        db.firestoreSettings = settings
    }

    fun toJoinCommunity(id: String) {
        crScope.launch {
            joinCommunity(id)
        }
    }
    fun toOutCommunity(id: String) {
        crScope.launch {
            outCommunity(id)
        }
    }

    private suspend fun outCommunity(id: String) {
        try {
            val uid = auth.currentUser?.uid.toString()
            val docRef = db.collection("organisasi").document(id)
            val usrRef = db.collection("users").document(uid)
            val upd = docRef.update("members", FieldValue.arrayRemove(usrRef)).await()
            Log.i("tes_upd", upd.toString())
        }catch (e: Exception) {
            Toast.makeText(context, "Gagal keluar komunitas", Toast.LENGTH_SHORT).show()
        }finally {
            Log.i("tes_upd_selesai", "udh")
            Toast.makeText(context, "Anda keluar dari komunitas", Toast.LENGTH_SHORT).show()
        }
    }
    private suspend fun joinCommunity(id: String) {
        try {
            val uid = auth.currentUser?.uid.toString()
            val docRef = db.collection("organisasi").document(id)
            val usrRef = db.collection("users").document(uid)
            val upd = docRef.update("members", FieldValue.arrayUnion(usrRef)).await()
            Log.i("tes_upd", upd.toString())
        }catch (e: Exception) {
            Toast.makeText(context, "Berhasil", Toast.LENGTH_SHORT).show()
        }finally {
            Toast.makeText(context, "Berhasil", Toast.LENGTH_SHORT).show()
        }
    }

}