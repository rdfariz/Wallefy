package org.hz240.wallefy.data

import android.util.Log
import android.widget.Toast
import co.metalab.asyncawait.async
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.utils.FirestoreObj

object ActivityObj {
    private val db = FirestoreObj._db

    suspend fun addPemasukan(idCommunity: String, data: HashMap<String, Any?>): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Pemasukan berhasil ditambahkan", "error" to "Gagal menambahkan pemasukan komunitas")
        return middleWareAdmin(idCommunity, messages, exeAddPemasukan(idCommunity, data))
    }
    suspend fun exeAddPemasukan(idCommunity: String, data: HashMap<String, Any?>) {
        val ref = db.collection("organisasi").document(idCommunity)
        val push = ref.collection("activity").add(data).await()
        val updSaldo = ref.update("saldo", FieldValue.increment(data["biaya"] as Long)).await()
    }

    suspend fun addPengeluaran(idCommunity: String, data: HashMap<String, Any?>): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Pengeluaran berhasil ditambahkan", "error" to "Gagal menambahkan pengeluaran komunitas")
        return middleWareAdmin(idCommunity, messages, exeAddPengeluaran(idCommunity, data))
    }
    suspend fun exeAddPengeluaran(idCommunity: String, data: HashMap<String, Any?>): HashMap<String, Any?> {
        val response = hashMapOf<String, Any?>("status" to false, "message" to null)
        val ref = db.collection("organisasi").document(idCommunity)
        val biaya = data["biaya"] as Long
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val saldo = snapshot!!["saldo"] as Long
            if (saldo - biaya >= 0) {
                db.collection("organisasi").document(idCommunity).collection("activity").add(data)
                transaction.update(ref, "saldo", FieldValue.increment(-biaya))
                response["status"] = true
                response["message"] = "Pengeluaran berhasil ditambahkan"
            } else {
                response["status"] = false
                response["message"] = "Saldo Komunitas Tidak Cukup"
            }
        }.await()
        return response
    }

    suspend fun clearPemasukan(idCommunity: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Pemasukan berhasil dibersihkan", "error" to "Gagal membersihkan pemasukan komunitas")
        return middleWareAdmin(idCommunity, messages, exeClearPemasukan(idCommunity))
    }
    private suspend fun exeClearPemasukan(idCommunity: String) {
        val type = "pemasukan"
        val ref = db.collection("organisasi").document(idCommunity).collection("activity")
        val pemasukan = ref.whereEqualTo("type", type).get(FirestoreObj._sourceDynamic).await()
        pemasukan.forEach {
            ref.document(it.id).delete().await()
        }
    }

    suspend fun clearPengeluaran(idCommunity: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Pengeluaran berhasil dibersihkan", "error" to "Gagal membersihkan pengeluaran komunitas")
        return middleWareAdmin(idCommunity, messages, exeClearPengeluaran(idCommunity))
    }
    private suspend fun exeClearPengeluaran(idCommunity: String) {
        val type = "pengeluaran"
        val ref = db.collection("organisasi").document(idCommunity).collection("activity")
        val pemasukan = ref.whereEqualTo("type", type).get(FirestoreObj._sourceDynamic).await()
        pemasukan.forEach {
            ref.document(it.id).delete().await()
        }
    }

    suspend fun deleteTransaction(idCommunity: String, idTransaction: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Berhasil menghapus transaksi", "error" to "Gagal menghapus transaksi komunitas")
        return middleWareAdmin(idCommunity, messages, exeDeleteTransaction(idCommunity, idTransaction))
    }
    suspend fun exeDeleteTransaction(idCommunity: String, idTransaction: String) {
        val ref = db.collection("organisasi").document(idCommunity).collection("activity").document(idTransaction)
        ref.delete().await()
    }

//  Person Interaction
    suspend fun getPerson(idPerson: String): HashMap<String, Any?> {
        val user = db.collection("users").document(idPerson).get().await()
        return user.data as HashMap<String, Any?>
    }
    suspend fun addPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Berhasil menghapus transaksi", "error" to "Gagal menghapus transaksi komunitas")
        return middleWareAdmin(idCommunity, messages, exeAddPerson(idCommunity, idPerson))
    }
    private suspend fun exeAddPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val response = hashMapOf<String, Any?>("status" to false, "message" to null)
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)
        val validData = usrRef.get().await()
        if (validData.exists()) {
            val org = ref.get().await()
            val membersRef = org.data!!.get("members") as ArrayList<DocumentReference>
            membersRef.forEach {
                if (it == usrRef) {
                    response["status"] = false
                    response["message"] = "${validData.data?.get("displayName")} sudah ada di komunitas"
                    return response
                }
            }
            ref.update("members", FieldValue.arrayUnion(usrRef)).await()
            response["status"] = true
            response["message"] = "Berhasil menambahkan ${validData.data?.get("displayName").toString()}"
        }else {
            response["status"] = false
            response["message"] = "User tidak ditemukan"
        }
        return response
    }

    suspend fun deletePerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Berhasil menghapus anggota", "error" to "Gagal menghapus anggota komunitas")
        return middleWareAdmin(idCommunity, messages, exeDeletePerson(idCommunity, idPerson))
    }
    private suspend fun exeDeletePerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val response = hashMapOf<String, Any?>("status" to false, "message" to null)
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)

        val doc = ref.get().await()
        val admin = doc.data!!["admin"] as ArrayList<DocumentReference>
        admin.forEach {
            if (it == usrRef) {
                response["status"] = false
                response["message"] = "Admin tidak bisa dikeluarkan"
                return response
            }
        }
        ref.update("members", FieldValue.arrayRemove(usrRef)).await()
        if (admin.size > 1) {
            ref.update("admin", FieldValue.arrayRemove(usrRef)).await()
        }
        response["status"] = true
        response["message"] = "Berhasil menghapus anggota"
        return response
    }

    suspend fun adminPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Berhasil memperbarui admin", "error" to "Gagal memperbarui admin")
        return middleWareAdmin(idCommunity, messages, exeAdminPerson(idCommunity, idPerson))
    }
    private suspend fun exeAdminPerson(idCommunity: String, idPerson: String) {
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)
        ref.update("admin", FieldValue.arrayUnion(usrRef)).await()
    }

    suspend fun unAdminPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val messages = hashMapOf<String, Any?>("success" to "Berhasil memperbarui admin", "error" to "Gagal memperbarui admin")
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
                    val response = exeUnAdminPerson(idCommunity, idPerson)
                    obj["status"] = response["status"]
                    obj["message"] = response["message"]
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = messages["error"]
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }
    private suspend fun exeUnAdminPerson(idCommunity: String, idPerson: String): HashMap<String, Any?> {
        val response = hashMapOf<String, Any?>("status" to false, "message" to null)
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)

        val doc = ref.get().await()
        val admin = doc.data!!["admin"] as ArrayList<DocumentReference>
        if (admin.size > 1) {
            ref.update("admin", FieldValue.arrayRemove(usrRef)).await()
            response["status"] = true
            response["message"] = "Berhasil memperbarui admin"
        }else {
            response["status"] = false
            response["message"] = "Harus terdapat minimal 1 admin komunitas"
        }

        return response
    }


    suspend fun middleWareAdmin(idCommunity: String, messages: HashMap<String, Any?>, func: Any): HashMap<String, Any?> {
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
                    if (func is Unit) {
                        func
                        obj["status"] = true
                        obj["message"] = messages["success"]
                    }else if(func is HashMap<*, *>) {
                        val response = func as HashMap<String, Any?>
                        obj["status"] = response["status"]
                        obj["message"] = response["message"]
                    }
                }
            }
        }catch (e: Exception) {
            obj["status"] = false
            obj["message"] = messages["error"]
        }finally {
            if (isAdmin == false) {
                obj["message"] = "Anda tidak punya akses mengatur komunitas"
            }
            return obj
        }
    }
}