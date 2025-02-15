package org.hz240.wallefy.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.hz240.wallefy.data.CommunityObj
import org.hz240.wallefy.data.GlobalObj
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommunityListViewModel(val idCommunity: String?= null): ViewModel() {
    private val _communityList = MutableLiveData<ArrayList<HashMap<String, Any?>>>()
    val communityList: LiveData<ArrayList<HashMap<String, Any?>>> get() = _communityList
    private val _communitySingle = MutableLiveData<HashMap<String, Any?>>()
    val communitySingle: LiveData<HashMap<String, Any?>> get() = _communitySingle

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _loadingRefresh = MutableLiveData<Boolean>()
    val loadingRefresh: LiveData<Boolean> get() = _loadingRefresh

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> get() = _error

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        _error.value = false
        crScope.launch {
            init()
        }
    }

    suspend fun init() {
        _loading.value = true
        migrateData()
        _loading.value = false
    }
    suspend fun refresh(): Boolean {
        if (GlobalObj.getPending() == true) { _loading.value = true }
        else { _loadingRefresh.value = true }

        var found = false
        if (idCommunity != null) {
            found = CommunityObj.checkUserLoginInCommunity(idCommunity.toString())
            if (found == true) {
                refreshData()
            }
        }else if (idCommunity == null){
            refreshData()
            found = true
        }else {
            found = false
        }

        _loading.value = false
        _loadingRefresh.value = false
        GlobalObj.setPending(false)
        return found
    }
    suspend fun checkIfMember(idCommunity: String):Boolean {
        _loadingRefresh.value = true
        val found = CommunityObj.checkUserLoginInCommunity(idCommunity)
        _loadingRefresh.value = false
        return found
    }
    suspend fun outCommunity(idCommunity: String): Boolean {
        _loading.value = true
        val status = CommunityObj.outCommunity(idCommunity)
        _loading.value = false
        return status
    }
    suspend fun joinCommunity(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.joinCommunity(idCommunity)
        _loading.value = false
        return obj
    }
    suspend fun checkCommunity(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.checkCommunity(idCommunity)
        _loading.value = false
        return obj
    }
    suspend fun createCommunity(displayName: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.createCommunity(displayName)
        _loading.value = false
        return obj
    }
    suspend fun deleteCommunity(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.deleteCommunity(idCommunity)
        _loading.value = false
        return obj
    }
    suspend fun changeDisplayName(idCommunity: String, displayName: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.changeDisplayName(idCommunity, displayName)
        refresh()
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }
    suspend fun setLock(idCommunity: String, state: Boolean): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.setLock(idCommunity, state)
        refresh()
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }
    suspend fun resetCommunity(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = CommunityObj.resetCommunity(idCommunity)
        refresh()
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }

    private fun _handlePendingState(obj: HashMap<String, Any?>) {
        if (obj["status"] == true) {
            GlobalObj.setPending(true)
        }else {
            GlobalObj.setPending(false)
        }
    }


    private suspend fun migrateData() {
        try {
            if (idCommunity == null) {
                _communityList.value = CommunityObj.getDataAll(false)
            }else {
                val getData = CommunityObj.getDataSingle(false, idCommunity)
                if (getData!!["idCommunity"] == null) {
                    _error.value = true
                }else {
                    _error.value = false
                    _communitySingle.value = getData
                }
            }
        }catch (e: Throwable) {

        }finally {
            _loading.value = false
        }
    }
    private suspend fun refreshData() {
        try {
            if (idCommunity == null) {
                _communityList.value = CommunityObj.getDataAll(true)
            }else {
                val getData = CommunityObj.getDataSingle(true, idCommunity)
                if (getData!!["idCommunity"] == null) {
                    _error.value = true
                }else {
                    _error.value = false
                    _communitySingle.value = getData
                }
            }
        }catch (e: Throwable) {

        }finally {
            _loadingRefresh.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }

}