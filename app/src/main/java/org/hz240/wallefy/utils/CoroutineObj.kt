package org.hz240.wallefy.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

object CoroutineObj {
    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    fun cancel() {
        Log.i("tesC", "cancell")
        crScope.cancel()
    }
}