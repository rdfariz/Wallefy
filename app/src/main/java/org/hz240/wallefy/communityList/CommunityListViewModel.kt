package org.hz240.wallefy.communityList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.metalab.asyncawait.async
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.model.CommunityInfo
import java.security.Key
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommunityListViewModel(val idCommunity: String?= null): ViewModel() {
    private val _communityList = MutableLiveData<ArrayList<HashMap<String, Any?>>>()
    val communityList: LiveData<ArrayList<HashMap<String, Any?>>> get() = _communityList
    private val _communitySingle = MutableLiveData<HashMap<String, Any?>>()
    val communitySingle: LiveData<HashMap<String, Any?>> get() = _communitySingle

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _members = MutableLiveData<ArrayList<HashMap<String, Any?>>>()
    private val _pengeluaran = MutableLiveData<ArrayList<HashMap<String, Any?>>>()
    private val _pemasukan = MutableLiveData<ArrayList<HashMap<String, Any?>>>()
    private val _latestTransactions = MutableLiveData<ArrayList<HashMap<String, Any?>>>()

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val settings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(
        FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build()

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        db.firestoreSettings = settings
        init()
    }

    fun init() {
        _loading.value = true
        migrateData()
    }
    fun refresh() {
        _loading.value = true
        migrateData()
    }

    fun migrateData() {
        crScope.launch {
            try {
                if (idCommunity == null) {
                    _communityList.value = getCommunityList()
                }else {
                    _communitySingle.value = getCommunitySingle(idCommunity)
                }
            }catch (e: Throwable) {

            }finally {
                _loading.value = false
            }
        }
    }

    fun convertToDate(time: com.google.firebase.Timestamp): String {
        val timestamp =  time
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm a")
        val netDate = Date(milliseconds)
        val date = sdf.format(netDate).toString()
        return date
    }

    suspend fun getCommunitySingle(idCommunity: String): HashMap<String, Any?> {
        var doc = HashMap<String, Any?>()
        val communityRef = db.collection("organisasi").document(idCommunity)
        val org = communityRef.get().await()
        val docsPemasukan = db.collection("organisasi").document(idCommunity).collection("pemasukan").orderBy("time", Query.Direction.DESCENDING).get().await()
        val docsPengeluaran = db.collection("organisasi").document(idCommunity).collection("pengeluaran").orderBy("time", Query.Direction.DESCENDING).get().await()
        val membersRef = org.data!!.get("members") as ArrayList<DocumentReference>
        val ltRef = org.data!!.get("latestTransactions") as ArrayList<DocumentReference>
        val ltRefFix = ltRef.asReversed()

//        Log.i("tesltRef", ltRef.toString())
//      Members
        var members = ArrayList<HashMap<String, Any?>>()
        membersRef.forEach {
            val member = it.get().await()
            var data: HashMap<String, Any?> = hashMapOf(
                "id" to it.id,
                "username" to member.data?.get("username").toString(),
                "displayName" to member.data?.get("displayName").toString(),
                "status" to member.data?.get("status").toString(),
                "email" to member.data?.get("email").toString(),
                "status" to member.data?.get("status").toString(),
                "photoUrl" to member.data?.get("photoUrl").toString()
            )
            members.add(data)
        }
        _members.value = members

//      Latest Transactions
        var latestTransactions = ArrayList<HashMap<String, Any?>>()
        ltRefFix.forEach {
            val transaction = db.document(communityRef.path+"/"+it.path).get().await()
            val date = convertToDate(transaction.data?.get("time") as com.google.firebase.Timestamp)
//            Log.i("testype", it.parent.path)
            var data: HashMap<String, Any?> = hashMapOf(
                "id" to it.id,
                "type" to it.parent.path,
                "title" to transaction.data?.get("title").toString(),
                "time" to date,
                "biaya" to transaction.data?.get("biaya").toString())
//            Log.i("tesEachlt", transaction.data.toString())
            latestTransactions.add(data)
        }
        _latestTransactions.value = latestTransactions

//      Pemasukan
        var pemasukan = ArrayList<HashMap<String, Any?>>()
        docsPemasukan.forEach {
            val date = convertToDate(it.data["time"] as com.google.firebase.Timestamp)
            var data = hashMapOf<String, Any?>("id" to it.id, "title" to it.data["title"].toString(), "time" to date,"biaya" to it.data["biaya"].toString())
            pemasukan.add(data)
        }
        _pemasukan.value = pemasukan

//      Pengeluaran
        var pengeluaran = ArrayList<HashMap<String, Any?>>()
        docsPengeluaran.forEach {
            val date = convertToDate(it.data["time"] as com.google.firebase.Timestamp)
            var data = hashMapOf<String, Any?>("id" to it.id, "title" to it.data["title"].toString(), "time" to date,"biaya" to it.data["biaya"].toString())
            pengeluaran.add(data)
        }
        _pengeluaran.value = pengeluaran

//      Convert Timestamp to Date
        val date = convertToDate(org.data!!["latestUpdated"] as com.google.firebase.Timestamp)

        doc = hashMapOf(
            "idCommunity" to org.id,
            "displayName" to org.data!!["displayName"],
            "saldo" to org.data!!["saldo"],
            "members" to _members.value,
            "pemasukan" to _pemasukan.value,
            "pengeluaran" to _pengeluaran.value,
            "latestTransactions" to _latestTransactions.value,
            "latestUpdated" to date
        )

        _communitySingle.value = doc
        return doc
    }

    suspend fun getCommunityList(): ArrayList<HashMap<String, Any?>> {
        val uid = auth.currentUser?.uid.toString()
        val usrRef = db.collection("users").document(uid)
        val docRef = db.collection("organisasi").whereArrayContains("members", usrRef).get().await()

        var myDataset: ArrayList<HashMap<String, Any?>> = ArrayList()
        docRef.forEach {
            myDataset.add(hashMapOf("idCommunity" to it.id, "displayName" to it.data!!["displayName"], "saldo" to it.data!!["saldo"]))
        }
        _communityList.value = myDataset
        return myDataset

//      Realtime Change
//        docRef.addSnapshotListener { snapshots, e ->
//            if (e != null) {
//                return@addSnapshotListener
//            }
//
//            Log.d("tes_up", "ubah cie")
//
//            myDataset.clear()
//            for (dc in snapshots!!.documentChanges) {
//                var it = dc.document
////                if (dc.type == DocumentChange.Type.ADDED) {
////                    Log.d("tess_add", "New city: ${dc.document.data}")
////                }
////                Log.d("tes_item", dc.document.data.toString())
//
////              Convert Timestamp to Date
////                val timestamp =  it.data!!["latestUpdated"] as com.google.firebase.Timestamp
////                val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
////                val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm a")
////                val netDate = Date(milliseconds)
////                val date = sdf.format(netDate).toString()
//                myDataset.add(hashMapOf("idCommunity" to it.id, "displayName" to it.data!!["displayName"], "saldo" to it.data!!["saldo"]))
//            }
//            _communityList.value = myDataset
//        }
    }

    fun setLoading(bool: Boolean) {
        _loading.value = bool
    }

    override fun onCleared() {
        super.onCleared()
        vm.cancel()
    }

}