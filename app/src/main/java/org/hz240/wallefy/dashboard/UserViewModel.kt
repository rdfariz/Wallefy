package org.hz240.wallefy.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {
    private var _userLogin = MutableLiveData<HashMap<String, Any>>()
    val userLogin: LiveData<HashMap<String, Any>> get() = _userLogin

    init {
        Log.i("tes", "init")
        val user: HashMap<String, Any> = hashMapOf("username" to "RdFariz", "status" to "Bendahara Negara")
        _userLogin.value = user
    }

    fun changeName() {
        val user: HashMap<String, Any> = hashMapOf("username" to "Koko", "status" to "Bendahara Negara")
        _userLogin.value = user
        Log.i("tes", "changename")
    }
}