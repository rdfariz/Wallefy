package org.hz240.wallefy.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.hz240.wallefy.data.AuthUserObj

class UserViewModel: ViewModel() {
    private val _userLogin = MutableLiveData<HashMap<String, Any?>?>()
    val userLogin: LiveData<HashMap<String, Any?>?> get() = _userLogin

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        crScope.launch {
            migrateData()
        }
    }

    suspend fun migrateData() {
        try {
            _loading.value = true
            _userLogin.value = AuthUserObj.getData()
        }catch (e: Throwable) {

        }finally {
            _loading.value = false
        }
    }

    suspend fun changeDisplayName(newData: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = AuthUserObj.setDisplayName(newData)
        _loading.value = false
        return obj
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}