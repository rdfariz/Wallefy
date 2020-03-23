package org.hz240.wallefy.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object ValidateApp {
    private val db = FirestoreObj._db
    private var minApp = hashMapOf<String, Any?>("minVersion" to null, "minVersionName" to null)

    fun checkVersionApp(scope: CoroutineScope, callback: (HashMap<String, Any?>)->Unit) {
        scope.launch {
            val app = db.collection("app").document("config").get(FirestoreObj._sourceDynamic).await()
            try {
                if (app.data!!["minVersion"] != null) { minApp["minVersion"] = app.data!!["minVersion"] }
                else { minApp["minVersion"] = 1}
                if ( app.data!!["minVersionName"] != null) { minApp["minVersionName"] = app.data!!["minVersionName"] }
                else { minApp["minVersionName"] = 1}
            }catch (e: Exception) {

            }finally {
                callback(minApp)
            }
        }
    }
}