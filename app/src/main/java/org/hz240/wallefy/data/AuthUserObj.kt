package org.hz240.wallefy.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.hz240.wallefy.model.UserInfo
import org.hz240.wallefy.utils.FirestoreObj

object AuthUserObj {
    private val db = FirestoreObj._db
    private var authObj: HashMap<String, Any?> = hashMapOf("username" to null, "displayName" to null, "status" to null, "email" to null, "photoUrl" to null)
    private var session: HashMap<String, Any?>? = null

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    fun migrateData(newData: Boolean = true) {
        crScope.launch {
            if (newData == true) {
                getNewData()
            } else {
                getData()
            }
        }
    }
    suspend fun getNewData(): HashMap<String, Any?>? {
        val newData = getUserLogin()
        session = newData
        return newData
    }
    suspend fun getData(): HashMap<String, Any?>? {
        if (FirestoreObj._auth.currentUser != null && session != null) {
            return session
        }else if(FirestoreObj._auth.currentUser != null && session == null) {
            return getUserLogin()
        }else {
            return null
        }
    }
    fun toSignOut() {
        signout()
    }
    suspend fun setDisplayName(newData: String) {
        changeDisplayName(newData)
    }


    private suspend fun changeDisplayName(newData: String) {
        val uid = FirestoreObj._auth.currentUser?.uid
        if (uid != null) {
            session?.set("displayName", newData)
            db.collection("users").document(uid).update("displayName", newData).await()
        }
    }
    private fun signout() {
        session = null
        authObj = hashMapOf("username" to null, "displayName" to null, "status" to null, "email" to null, "photoUrl" to null)
        CommunityObj.reset(true)
        FirebaseAuth.getInstance().signOut()
    }
    private suspend fun getUserLogin(): HashMap<String, Any?>? {
        if (FirestoreObj._auth.currentUser != null) {
            authObj["displayName"] = FirestoreObj._auth.currentUser?.displayName.toString()
            authObj["email"] = FirestoreObj._auth.currentUser?.email.toString()
            authObj["photoUrl"] = FirestoreObj._auth.currentUser?.photoUrl.toString()

            var uid = FirestoreObj._auth.currentUser?.uid.toString()
            var docRef = db.collection("users").document(uid)
            var doc = docRef.get().await()
            var map: HashMap<String, Any?>? = null
            try {
                if (doc.data == null) {
                    docRef.set(authObj).await()
                    doc = docRef.get().await()
                }else {
                    if (doc.data!!["username"] == null || doc.data!!["status"] == null) {
                        Log.i("tesAuth", "data null")
                    }
                }
                val obj = doc.toObject(UserInfo::class.java)
                map = hashMapOf("username" to obj?.username, "displayName" to obj?.displayName, "email" to obj?.email, "status" to obj?.status, "photoUrl" to obj?.photoUrl)
            }catch (e: Throwable) {

            }finally {
                session = map
                return map
            }
        }else {
            return null
        }
    }
}