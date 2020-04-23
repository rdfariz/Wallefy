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


class ActivityViewModel: ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    suspend fun addPemasukan(idCommunity: String, title: String, biaya: Long) {
        try {
            _loading.value = true
            val data = hashMapOf<String, Any?>("title" to title, "biaya" to biaya, "type" to "pemasukan", "time" to FieldValue.serverTimestamp())
            ActivityObj.addPemasukan(idCommunity, data)
        }catch (e: Exception) {

        }finally {
            _loading.value = false
        }
    }

    suspend fun addPengeluaran(idCommunity: String, title: String, biaya: Long) {
        try {
            _loading.value = true
            val data = hashMapOf<String, Any?>("title" to title, "biaya" to biaya, "type" to "pengeluaran", "time" to FieldValue.serverTimestamp())
            ActivityObj.addPengeluaran(idCommunity, data)
        }catch (e: Exception) {

        }finally {
            _loading.value = false
        }
    }

    suspend fun clearPemasukan(idCommunity: String) {
        try {
            _loading.value = true
            ActivityObj.clearPemasukan(idCommunity)
        }catch (e: Exception) {

        }finally {
            _loading.value = false
        }
    }

    suspend fun clearPengeluaran(idCommunity: String) {
        try {
            _loading.value = true
            ActivityObj.clearPengeluaran(idCommunity)
        }catch (e: Exception) {

        }finally {
            _loading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }
}