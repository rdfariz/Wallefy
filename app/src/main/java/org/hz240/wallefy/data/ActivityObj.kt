package org.hz240.wallefy.data

import android.util.Log
import co.metalab.asyncawait.async
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.utils.FirestoreObj

object ActivityObj {
    private val db = FirestoreObj._db

    suspend fun addPemasukan(idCommunity: String, data: HashMap<String, Any?>) {
        val push = db.collection("organisasi").document(idCommunity).collection("activity").add(data).await()
        val org = db.collection("organisasi").document(idCommunity).get().await()
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


}