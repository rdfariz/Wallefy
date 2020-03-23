package org.hz240.wallefy.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}