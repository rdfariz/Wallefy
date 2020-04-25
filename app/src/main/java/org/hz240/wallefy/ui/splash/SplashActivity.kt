package org.hz240.wallefy.ui.splash

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hz240.wallefy.R
import org.hz240.wallefy.ui.communityList.CommunityListActivity
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.ui.login.LoginActivity
import org.hz240.wallefy.utils.ConnectivityReceiver
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.utils.ValidateApp

class SplashActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)
    private val SPLASH_TIME_OUT:Long = 1000

    init {
        crScope.launch {
            validateApp()
            AuthUserObj.migrateData(true)
        }
    }

    //  Listener connection
    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            FirestoreObj.changeSource(Source.CACHE)
        } else if (isConnected) {
            FirestoreObj.changeSource(Source.SERVER)
        } else {
            FirestoreObj.changeSource(Source.DEFAULT)
        }
    }
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }
    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun validateApp() {
        try {
            ValidateApp.checkVersionApp(crScope){data ->
                val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                val verCode: Int = pInfo.versionCode
                var versionDB: Long = -1
                try {
                    versionDB = Integer.parseInt(data!!["minVersion"].toString()).toLong()
                }catch (e: Exception) {

                }finally {
                    if (verCode >= versionDB && versionDB > -1) {
                        Handler().postDelayed({
                            checkAuth()
                        }, SPLASH_TIME_OUT)
                    }else {
                        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
                        alertDialogBuilder.setTitle("Tidak Support Versi Aplikasi")
                        alertDialogBuilder.setMessage("Versi aplikasi yang anda gunakan telalu lawas, silahkan update")
                        alertDialogBuilder.setPositiveButton("Keluar", DialogInterface.OnClickListener { dialog, which ->
                            finish()
                            moveTaskToBack(true)
                        })
                        alertDialogBuilder.setCancelable(false)
                        alertDialogBuilder.show()
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } finally {

        }
    }
    private fun checkAuth() {
        try {
            if (FirestoreObj._auth.currentUser != null) {
                startActivity(Intent(this, CommunityListActivity::class.java))
            }else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }catch (e: Exception) {
            Toast.makeText(this, "Kesalahan akses", Toast.LENGTH_SHORT).show()
        }finally {
            finish()
        }
    }
}
