package org.hz240.wallefy.dashboard

import android.util.Log
import android.widget.Toast
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
import org.hz240.wallefy.model.Info
import org.hz240.wallefy.model.UserInfo

class UserViewModel: ViewModel() {
    private var _userLogin = MutableLiveData<HashMap<String, Any>>()
    val userLogin: LiveData<HashMap<String, Any>> get() = _userLogin

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build()

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        db.firestoreSettings = settings
        val default: HashMap<String, Any> = hashMapOf("username" to "-", "status" to "-")
        _userLogin.value = default
        initData()
    }

    fun initData() {
        crScope.launch {
            Log.i("userlogin", auth.currentUser?.email.toString())
            var user: HashMap<String, Any> = hashMapOf("username" to auth.currentUser?.displayName.toString(), "status" to auth.currentUser?.email.toString(), "photoUrl" to auth.currentUser?.photoUrl.toString())
            try {
//                user = getDataUser()
            }catch (e: Throwable) {

            }finally {
                _userLogin.value = user
            }
        }
    }

    suspend fun getDataUser(): HashMap<String, Any> {
        val docRef = db.collection("users").document("vO6eviH0cluMuIDg5jre")
        val user = docRef.get().await()
        val data = user.toObject(UserInfo::class.java) ?: Info()
//        Log.d("tes", data.toString())

//        db.collection("users").document("vO6eviH0cluMuIDg5jre")
//        .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
//            if (e != null) {
//                Log.w("tes", "Listen error", e)
//                return@addSnapshotListener
//            }
//
//            if (querySnapshot != null) {
//                Log.d("tes", querySnapshot.data.toString())
//            }
//
//            val source = if (querySnapshot?.metadata?.isFromCache!!)
//                "local cache"
//            else
//                "server"
//            Log.d("tes", "Data fetched from $source")
//        }

        return hashMapOf("username" to user.data?.get("username").toString(), "status" to user.data?.get("status").toString())
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}