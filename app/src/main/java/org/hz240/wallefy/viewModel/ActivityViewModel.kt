package org.hz240.wallefy.viewModel

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.R
import org.hz240.wallefy.data.ActivityObj
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.data.GlobalObj


class ActivityViewModel: ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    suspend fun addPemasukan(idCommunity: String, title: String, biaya: Long): HashMap<String, Any?> {
        _loading.value = true
        val data = hashMapOf<String, Any?>("title" to title, "biaya" to biaya, "type" to "pemasukan", "time" to FieldValue.serverTimestamp())
        val obj = ActivityObj.addPemasukan(idCommunity, data)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }

    suspend fun addPengeluaran(idCommunity: String, title: String, biaya: Long): HashMap<String, Any?> {
        _loading.value = true
        val data = hashMapOf<String, Any?>("title" to title, "biaya" to biaya, "type" to "pengeluaran", "time" to FieldValue.serverTimestamp())
        val obj = ActivityObj.addPengeluaran(idCommunity, data)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }

    suspend fun clearPemasukan(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.clearPemasukan(idCommunity)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }

    suspend fun clearPengeluaran(idCommunity: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.clearPengeluaran(idCommunity)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }

    suspend fun deleteTransaction(idCommunity: String, idTransaction: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.deleteTransaction(idCommunity, idTransaction)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }


    suspend fun getPerson(idPerson: String): HashMap<String, Any?>? {
        _loading.value = true
        val obj = ActivityObj.getPerson(idPerson)
        _loading.value = false
        return obj
    }
    suspend fun addPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.addPerson(idCommunity, idPerson)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }
    suspend fun deletePerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.deletePerson(idCommunity, idPerson)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }
    suspend fun adminPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.adminPerson(idCommunity, idPerson)
        _handlePendingState(obj)
        _loading.value = false
        return obj
    }
    suspend fun unAdminPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        _loading.value = true
        val obj = ActivityObj.unAdminPerson(idCommunity, idPerson)
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

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}