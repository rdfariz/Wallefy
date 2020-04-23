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

    suspend fun addPemasukan(idCommunity: String, data: HashMap<String, Any?>) {
        val push = db.collection("organisasi").document(idCommunity).collection("activity").add(data).await()
        val org = db.collection("organisasi").document(idCommunity).get(FirestoreObj._sourceDynamic).await()
        val updSaldo = db.collection("organisasi").document(idCommunity).update("saldo", FieldValue.increment(data["biaya"] as Long)).await()
    }

    suspend fun addPengeluaran(idCommunity: String, data: HashMap<String, Any?>) {
        val org = db.collection("organisasi").document(idCommunity)
        val biaya = data["biaya"] as Long

        val ref = db.collection("organisasi").document(idCommunity)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(org)
            val saldo = snapshot!!["saldo"] as Long
            if (saldo - biaya >= 0) {
                db.collection("organisasi").document(idCommunity).collection("activity").add(data)
                transaction.update(ref, "saldo", FieldValue.increment(-biaya))
            } else {
                Log.i("tes", "saldo kurang")
            }
        }.addOnSuccessListener { result ->

        }.addOnFailureListener { e ->

        }.await()
    }

    suspend fun clearPemasukan(idCommunity: String) {
        val type = "pemasukan"
        val ref = db.collection("organisasi").document(idCommunity).collection("activity")
        val pemasukan = ref.whereEqualTo("type", type).get(FirestoreObj._sourceDynamic).await()
        pemasukan.forEach {
            ref.document(it.id).delete().await()
        }
    }

    suspend fun clearPengeluaran(idCommunity: String) {
        val type = "pengeluaran"
        val ref = db.collection("organisasi").document(idCommunity).collection("activity")
        val pemasukan = ref.whereEqualTo("type", type).get(FirestoreObj._sourceDynamic).await()
        pemasukan.forEach {
            ref.document(it.id).delete().await()
        }
    }

    suspend fun deleteTransaction(idCommunity: String, idTransaction: String) {
        val ref = db.collection("organisasi").document(idCommunity).collection("activity").document(idTransaction)
        ref.delete().await()
    }

//  Person Interaction
    suspend fun addPerson(idCommunity: String, idPerson: String) {
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)
        val validData = usrRef.get().await()
        if (validData.exists()) {
            Log.i("tesADD", "EXIST")
            ref.update("members", FieldValue.arrayUnion(usrRef)).await()
        }else {
            Log.i("tesADD", "NOTT EXIST")
        }
    }
    suspend fun deletePerson(idCommunity: String, idPerson: String) {
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)
        ref.update("members", FieldValue.arrayRemove(usrRef)).await()

        val doc = ref.get().await()
        val admin = doc.data!!["admin"] as ArrayList<DocumentReference>
        if (admin.size > 1) {
            ref.update("admin", FieldValue.arrayRemove(usrRef)).await()
        }
    }
    suspend fun adminPerson(idCommunity: String, idPerson: String) {
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)
        ref.update("admin", FieldValue.arrayUnion(usrRef)).await()
    }
    suspend fun unAdminPerson(idCommunity: String, idPerson: String) {
        val ref = db.collection("organisasi").document(idCommunity)
        val usrRef = db.collection("users").document(idPerson)

        val doc = ref.get().await()
        val admin = doc.data!!["admin"] as ArrayList<DocumentReference>
        if (admin.size > 1) {
            ref.update("admin", FieldValue.arrayRemove(usrRef)).await()
        }
    }
}