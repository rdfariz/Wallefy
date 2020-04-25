package org.hz240.wallefy.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.hz240.wallefy.utils.FirestoreObj

object GlobalObj {
    private val _pendingToLoad = MutableLiveData<Boolean>()
    val pendingToLoad : LiveData<Boolean> get() = _pendingToLoad

    init {
        _pendingToLoad.value = false
    }

    fun getPending(): Boolean? {
        return _pendingToLoad.value
    }
    fun setPending(boolean: Boolean) {
        _pendingToLoad.value = boolean
    }
}