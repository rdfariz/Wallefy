package org.hz240.wallefy.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel: ViewModel() {
    private val _userLogin = MutableLiveData<HashMap<String, Any?>>()
    val userLogin: LiveData<HashMap<String, Any?>> get() = _userLogin
    var userAuthData: HashMap<String, Any?>? = null

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build()

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        _loading.value = true
        db.firestoreSettings = settings
        migrateData()
    }

    fun migrateData() {
        crScope.launch {
            try {
                userAuthData = hashMapOf("username" to null, "displayName" to auth.currentUser?.displayName.toString(), "status" to null, "email" to auth.currentUser?.email.toString(), "photoUrl" to auth.currentUser?.photoUrl.toString())
                _userLogin.value = userAuthData
                userAuthData = getUserLogin()
            }catch (e: Throwable) {

            }finally {
                _userLogin.value = userAuthData
            }
        }
    }

    suspend fun getUserLogin(): HashMap<String, Any?> {
        val uid = auth.currentUser?.uid.toString()
        val docRef = db.collection("users").document(uid)
        var doc = docRef.get().await()
        try {
            if (doc.data == null) {
                userAuthData?.let { docRef.set(it).await() }
                doc = docRef.get().await()
            }else {

            }
        }catch (e: Throwable) {

        }finally {
//            val data = doc.toObject(UserInfo::class.java) ?: Info()
//            Log.d("tes", data.toString())
            _loading.value = false
        }

        return hashMapOf("username" to doc.data?.get("username").toString(),
                        "displayName" to doc.data?.get("displayName").toString(),
                        "status" to doc.data?.get("status").toString(),
                        "email" to doc.data?.get("email").toString(),
                        "status" to doc.data?.get("status").toString(),
                        "photoUrl" to doc.data?.get("photoUrl").toString())
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}