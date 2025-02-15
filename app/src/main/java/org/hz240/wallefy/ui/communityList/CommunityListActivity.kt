package org.hz240.wallefy.ui.communityList

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hz240.wallefy.R
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.data.CommunityObj
import org.hz240.wallefy.databinding.ActivityCommunityListBinding
import org.hz240.wallefy.ui.login.LoginActivity
import org.hz240.wallefy.ui.splash.SplashActivity
import org.hz240.wallefy.utils.ConnectivityReceiver
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.utils.ValidateApp

class CommunityListActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var binding: ActivityCommunityListBinding
    private var auth = FirestoreObj._auth

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    init {
        crScope.launch {
            AuthUserObj.migrateData(true)
            CommunityObj.reset(all = true)
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
        validateApp()

        val sharedPref = applicationContext.getSharedPreferences("selectedCommunity", MODE_PRIVATE)!!
        val editor = sharedPref.edit()
        editor.remove("idCommunity")
        editor.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

//      Auth Check
        val currentUser = auth.currentUser
        updateUI(currentUser)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_list)
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        NavigationUI.setupActionBarWithNavController(this, navCtrl)
    }

    private fun validateApp() {
        try {
            ValidateApp.checkVersionApp(crScope){ data ->
                val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                val verCode: Int = pInfo.versionCode
                var versionDB: Long = -1
                try {
                    versionDB = Integer.parseInt(data!!["minVersion"].toString()).toLong()
                }catch (e: Exception) {

                }finally {
                    if (verCode >= versionDB && versionDB > -1) {

                    }else {
                        val intent = Intent (this, SplashActivity::class.java)
                        intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } finally {

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        return navCtrl.navigateUp()
    }

}
