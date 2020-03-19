package org.hz240.wallefy.anggota

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnggotaViewModel: ViewModel() {
    private var _anggota = MutableLiveData<ArrayList<HashMap<String, Any>>>()
    val anggota: LiveData<ArrayList<HashMap<String, Any>>> get() = _anggota

    private val db = FirebaseFirestore.getInstance()
    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        val default: ArrayList<HashMap<String, Any>> = ArrayList()
        default.add(hashMapOf("username" to "-", "status" to "-"))
        _anggota.value = default
        initData()
    }

    private fun initData() {
        crScope.launch {
            val data = getDataAnggota()
            _anggota.value = data
        }
    }

    private suspend fun getDataAnggota(): ArrayList<HashMap<String, Any>> {
        var arr: ArrayList<HashMap<String, Any>> = ArrayList()
        val docRef = db.collection("organisasi").document("Ntpj8HAvZgFtjIEb9imY")
        val doc = docRef.get().await()

        var members = doc.data?.get("members")
        Log.i("tes", members.toString())
//        for (m in members) {
//        }
        return arr
    }

    private suspend fun getDataUser(ref: DocumentReference): HashMap<String, Any> {
        val user= ref.get().await()
        return hashMapOf("username" to user.data?.get("username").toString(), "status" to user.data?.get("status").toString())
    }
}