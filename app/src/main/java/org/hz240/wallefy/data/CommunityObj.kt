package org.hz240.wallefy.data

import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.utils.Converter
import org.hz240.wallefy.utils.FirestoreObj
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object CommunityObj {
    private val db = FirestoreObj._db

    private var _communitySingle = HashMap<String, Any?>()
    private var sessionSingle: HashMap<String, Any?>? = null

    private var _communityList = ArrayList<HashMap<String, Any?>>()
    private var sessionAll: ArrayList<HashMap<String, Any?>>? = null

    private var _members = ArrayList<HashMap<String, Any?>>()
    private var _pengeluaran = ArrayList<HashMap<String, Any?>>()
    private var _pemasukan = ArrayList<HashMap<String, Any?>>()
    private var _latestTransactions = ArrayList<HashMap<String, Any?>>()

    fun reset(all: Boolean) {
        _communitySingle = HashMap<String, Any?>()
        sessionSingle = null
        if (all == true) {
            _communityList = ArrayList<HashMap<String, Any?>>()
            sessionAll = null
        }
    }

    suspend fun getDataSingle(newData: Boolean = true, idCommunity: String): HashMap<String, Any?>? {
        if (newData == true) {
            return getCommunitySingle(idCommunity)
        }else {
            if (sessionSingle == null) {
                return getCommunitySingle(idCommunity)
            }else {
                return sessionSingle
            }
        }
    }
    suspend fun getDataAll(newData: Boolean = true): ArrayList<HashMap<String, Any?>>? {
        if (newData == true) {
            return getCommunityAll()
        }else {
            if (sessionAll == null) {
                return getCommunityAll()
            }else {
                return sessionAll
            }
        }
    }


    suspend fun outCommunity(id: String): Boolean {
        var status = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val docRef = db.collection("organisasi").document(id)
            val usrRef = db.collection("users").document(uid)
            val upd = docRef.update("members", FieldValue.arrayRemove(usrRef)).await()
            status = true
        }catch (e: Exception) {
            status = false
        }finally {
            return status
        }
    }
    suspend fun joinCommunity(id: String): HashMap<String, Any?> {
        var obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val docRef = db.collection("organisasi").document(id)
            val usrRef = db.collection("users").document(uid)
            val check = docRef.get(FirestoreObj._sourceDynamic).await()

            if (check.data!!["lock"] as Boolean == true) {
                obj["status"] = false
                obj["message"] = "Maaf, komunitas tidak menerima anggota lagi"
            }else {
                val arr = check.data!!["members"] as ArrayList<DocumentReference>
                if(arr.contains(usrRef)) {
                    obj["status"] = false
                    obj["message"] = "Maaf, kamu sudah bergabung"
                }else {
                    obj["status"] = true
                    obj["message"] = "Bergabung dengan ${check.data!!["displayName"].toString()}"
                    val upd = docRef.update("members", FieldValue.arrayUnion(usrRef)).await()
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Tidak bisa join ke komunitas"
        }finally {
            return obj
        }
    }
    suspend fun createCommunity(displayName: String): HashMap<String, Any?> {
        var obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val data = hashMapOf<String, Any?>(
                "displayName" to displayName,
                "lock" to false,
                "admin" to arrayListOf(usrRef),
                "members" to arrayListOf(usrRef),
                "saldo" to 0
            )
            val create = db.collection("organisasi").add(data).await()
            obj["status"] = true
            obj["message"] = "Berhasil membuat komunitas ${displayName}"
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Gagal membuat komunitas"
        }finally {
            return obj
        }
    }
    suspend fun deleteCommunity(idCommunity: String): HashMap<String, Any?> {
        val obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        var isAdmin = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val ref = db.collection("organisasi").document(idCommunity)
            val community = ref.get().await()
            val adminRef = community.data?.get("admin") as ArrayList<DocumentReference>
            adminRef.forEach {
                if (it == usrRef) {
                    isAdmin = true
                    ref.delete().await()
                    obj["status"] = true
                    obj["message"] = "Komunitas ${community.data?.get("displayName")} berhasil dihapus"
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Gagal menghapus komunitas"
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }
    suspend fun changeDisplayName(idCommunity: String, displayName: String): HashMap<String, Any?> {
        val obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        var isAdmin = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val ref = db.collection("organisasi").document(idCommunity)
            val community = ref.get().await()
            val adminRef = community.data?.get("admin") as ArrayList<DocumentReference>
            adminRef.forEach {
                if (it == usrRef) {
                    isAdmin = true
                    ref.update("displayName", displayName).await()
                    obj["status"] = true
                    obj["message"] = "Komunitas berhasil diupdate"
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Gagal mengupdate komunitas"
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }
    suspend fun setLock(idCommunity: String, state: Boolean): HashMap<String, Any?> {
        val obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        var isAdmin = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val ref = db.collection("organisasi").document(idCommunity)
            val community = ref.get().await()
            val adminRef = community.data?.get("admin") as ArrayList<DocumentReference>
            adminRef.forEach {
                if (it == usrRef) {
                    isAdmin = true
                    ref.update("lock", state).await()
                    obj["status"] = true
                    obj["message"] = "Komunitas berhasil diupdate"
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Gagal mengupdate komunitas"
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }
    suspend fun checkCommunity(id: String): HashMap<String, Any?> {
        var obj = hashMapOf<String, Any?>("displayName" to "-", "found" to false, "message" to null)
        try {
            val doc = db.collection("organisasi").document(id).get(FirestoreObj._sourceDynamic).await()
            val displayName = doc.data!!["displayName"]
            val members = doc.data!!["members"] as ArrayList<DocumentReference>
            obj["displayName"] = displayName
            obj["found"] = true
            if (members.size == 0) {
                obj["message"] = "Belum ada orang dikomunitas ini"
            }else {
                obj["message"] = "${members.size} Orang ada dikomunitas ini"
            }
        }catch (e: Exception) {
            obj["message"] = "Komunitas tidak ditemukan"
        }finally {
            return obj
        }
    }
    suspend fun checkUserLoginInCommunity(idCommunity: String): Boolean {
        var found = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val communityRef = db.collection("organisasi").document(idCommunity).get(FirestoreObj._sourceDynamic).await()
            val members = communityRef["members"] as ArrayList<DocumentReference>
            if (members.contains(usrRef)) {
                found = true
            }
        }catch (e: Exception) {

        }finally {
            return found
        }
    }
    suspend fun resetCommunity(idCommunity: String): HashMap<String, Any?> {
        val obj = hashMapOf<String, Any?>("status" to false, "message" to null)
        var isAdmin = false
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = db.collection("users").document(uid)
            val ref = db.collection("organisasi").document(idCommunity)
            val community = ref.get().await()
            val adminRef = community.data?.get("admin") as ArrayList<DocumentReference>
            adminRef.forEach {
                if (it == usrRef) {
                    isAdmin = true
                    ActivityObj.exeClearPemasukan(idCommunity)
                    ActivityObj.exeClearPengeluaran(idCommunity)
                    ref.update("saldo", 0).await()
                    obj["status"] = true
                    obj["message"] = "Komunitas Berhasil Direset"
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = "Gagal Reset Komunitas"
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }


    private suspend fun getCommunityAll(): ArrayList<HashMap<String, Any?>> {
        var myDataset: ArrayList<HashMap<String, Any?>> = ArrayList()
        try {
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = uid?.let { db.collection("users").document(it) }
            val docRef = db.collection("organisasi").whereArrayContains("members", usrRef).get(FirestoreObj._sourceDynamic).await()

            docRef.forEach {
                myDataset?.add(hashMapOf("idCommunity" to it.id, "displayName" to it.data!!["displayName"], "saldo" to Converter.rupiah(it.data!!["saldo"])))
            }
            _communityList = myDataset
            sessionAll = myDataset
        }catch (e: Exception) {
            if (sessionAll != null) {
                myDataset = sessionAll as ArrayList<HashMap<String, Any?>>
            }
        }finally {
            return myDataset
        }
    }
    private suspend fun getCommunitySingle(idCommunity: String): HashMap<String, Any?>? {
        var doc = HashMap<String, Any?>()
        val communityRef = db.collection("organisasi").document(idCommunity)
        val org = communityRef.get(FirestoreObj._sourceDynamic).await()
        var isAdmin = false
        try {
            val docsActivity = db.collection("organisasi").document(idCommunity).collection("activity")
            val docsPemasukan = docsActivity.whereEqualTo("type", "pemasukan")
                    .orderBy("time", Query.Direction.DESCENDING).get(FirestoreObj._sourceDynamic).await()
            val docsPengeluaran = docsActivity.whereEqualTo("type", "pengeluaran")
                    .orderBy("time", Query.Direction.DESCENDING).get(FirestoreObj._sourceDynamic).await()
            val docsLatestTransactions = docsActivity.orderBy("time", Query.Direction.DESCENDING).limit(4).get(FirestoreObj._sourceDynamic).await()
            var membersRef = ArrayList<DocumentReference>()
            var adminRef = ArrayList<DocumentReference>()
            var ltRef = ArrayList<DocumentReference>()

            if (org.data!!["admin"] != null) {
                adminRef = org.data!!.get("admin") as ArrayList<DocumentReference>
            }
            if (org.data!!["members"] != null) {
                membersRef = org.data!!.get("members") as ArrayList<DocumentReference>
            }
            if (org.data!!["latestTransactions"] != null) {
                ltRef = org.data!!.get("latestTransactions") as ArrayList<DocumentReference>
            }
            val ltRefFix = ltRef.asReversed()


    //      Check IF Admin
            val uid = FirestoreObj._auth.currentUser?.uid.toString()
            val usrRef = uid?.let { db.collection("users").document(it) }
            adminRef.forEach {
                if (it == usrRef) { isAdmin = true }
            }

    //      Members
            var members = ArrayList<HashMap<String, Any?>>()
            membersRef.forEach {
                val member = it.get(FirestoreObj._sourceDynamic).await()
                var data: HashMap<String, Any?> = hashMapOf(
                    "id" to it.id,
                    "username" to member.data!!["username"].toString(),
                    "displayName" to member.data!!["displayName"].toString(),
                    "status" to member.data!!["status"].toString(),
                    "email" to member.data!!["email"].toString(),
                    "status" to member.data!!["status"].toString(),
                    "photoUrl" to member.data!!["photoUrl"].toString(),
                    "type" to "member"
                )

                adminRef?.let { arr->
                    arr.forEach {adminID ->
                        if (adminID == it) {
                            data["type"] = "admin"
                        }
                    }
                }

                members.add(data)
            }
            _members = members

    //      Latest Transactions
            var latestTransactions = ArrayList<HashMap<String, Any?>>()
            docsLatestTransactions.forEach {
                var title = "Unknown"
                var date = "-"
                var biaya = "-"
                var type = "-"
                if (it.exists()) {
                    val time = it.data!!["time"]
                    date = convertToDate(time as com.google.firebase.Timestamp)
                    title = it.data!!["title"].toString()
                    biaya = Converter.rupiah(it.data!!["biaya"])
                    type = it.data!!["type"].toString()
                }
                var data: HashMap<String, Any?> = hashMapOf(
                    "id" to it.id,
                    "type" to type,
                    "title" to title,
                    "time" to date,
                    "biaya" to biaya
                )
                latestTransactions.add(data)
            }
            _latestTransactions = latestTransactions

    //      Pemasukan
            var pemasukan = ArrayList<HashMap<String, Any?>>()
            docsPemasukan.forEach {
                var title = "Unknown"
                var date = "-"
                var biaya = "-"
                var type = "pemasukan"
                if (it.exists()) {
                    title = it.data!!["title"].toString()
                    date = convertToDate(it.data!!["time"] as com.google.firebase.Timestamp)
                    biaya = Converter.rupiah(it.data!!["biaya"])
                    type = it.data!!["type"].toString()
                }
                var data = hashMapOf<String, Any?>(
                    "id" to it.id,
                    "title" to title,
                    "time" to date,
                    "biaya" to biaya,
                    "type" to type
                )
                pemasukan.add(data)
            }
            _pemasukan = pemasukan

    //      Pengeluaran
            var pengeluaran = ArrayList<HashMap<String, Any?>>()
            docsPengeluaran.forEach {
                var title = "Unknown"
                var date = "-"
                var biaya = "-"
                var type = "pengeluaran"
                if (it.exists()) {
                    title = it.data!!["title"].toString()
                    date = convertToDate(it.data!!["time"] as com.google.firebase.Timestamp)
                    biaya = Converter.rupiah(it.data!!["biaya"])
                    type = it.data!!["type"].toString()
                }
                var data = hashMapOf<String, Any?>(
                    "id" to it.id,
                    "title" to title,
                    "time" to date,
                    "biaya" to biaya,
                    "type" to type
                )
                pengeluaran.add(data)
            }
            _pengeluaran = pengeluaran

//          Convert Timestamp to Date & Assign default value
            var date = "-"
            if (docsLatestTransactions.size() > 0) {
                date = convertToDate(docsLatestTransactions.first()["time"] as com.google.firebase.Timestamp)
            }

            var saldo = "-"
            if(org.data!!["saldo"] != null) {
                saldo = Converter.rupiah(org.data!!["saldo"])
            }
            var displayName = "-"
            if(org.data!!["displayName"] != null) {
                displayName = org.data!!["displayName"].toString()
            }
            var lock: Boolean? = null
            if(org.data!!["lock"] != null) {
                lock = org.data!!["lock"] as Boolean
            }

//          Make Obj
            doc = hashMapOf(
                "idCommunity" to org.id,
                "lock" to lock,
                "displayName" to displayName,
                "saldo" to saldo,
                "members" to _members,
                "pemasukan" to _pemasukan,
                "pengeluaran" to _pengeluaran,
                "latestTransactions" to _latestTransactions,
                "latestUpdated" to date,
                "isAdmin" to isAdmin
            )
        }catch (e: Exception) {
            if (sessionSingle != null) {
                doc = sessionSingle as HashMap<String, Any?>
            }else {
                doc = hashMapOf(
                    "idCommunity" to null,
                    "lock" to null,
                    "displayName" to "-",
                    "saldo" to "-",
                    "members" to ArrayList<HashMap<String, Any>>(),
                    "pemasukan" to ArrayList<HashMap<String, Any>>(),
                    "pengeluaran" to ArrayList<HashMap<String, Any>>(),
                    "latestTransactions" to ArrayList<HashMap<String, Any>>(),
                    "latestUpdated" to "-",
                    "isAdmin" to isAdmin
                )
            }
        }finally {
            _communitySingle = doc
            sessionSingle = doc
            return doc
        }
    }

    private fun convertToDate(time: com.google.firebase.Timestamp): String {
        val timestamp =  time
        val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
        val sdf = SimpleDateFormat("MMMM dd, yyyy HH:mm a")
        val netDate = Date(milliseconds)
        val date = sdf.format(netDate).toString()
        return date
    }
}