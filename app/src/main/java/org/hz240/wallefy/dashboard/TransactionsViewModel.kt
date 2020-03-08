package org.hz240.wallefy.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TransactionsViewModel: ViewModel() {
    private var _transactions = MutableLiveData<ArrayList<HashMap<String, Any>>>()
    val transactions: LiveData<ArrayList<HashMap<String, Any>>> get() = _transactions

    init {
        var myDataset: ArrayList<HashMap<String, Any>> = ArrayList()
        myDataset.add(hashMapOf("title" to "Pelunasan Baju", "description" to "Internal", "biaya" to "Rp. 2.800.000"))
        myDataset.add( hashMapOf("title" to "Penjualan Makanan", "description" to "Event Wibu", "biaya" to "Rp. 1.800.000"))
        myDataset.add( hashMapOf("title" to "Pelunasan Baju", "description" to "Internal", "biaya" to "Rp. 2.800.000"))
        _transactions.value = myDataset
    }

}