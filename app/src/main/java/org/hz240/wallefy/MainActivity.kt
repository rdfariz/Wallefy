package org.hz240.wallefy

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.data.CommunityObj
import org.hz240.wallefy.databinding.ActivityMainBinding
import org.hz240.wallefy.login.LoginActivity
import org.hz240.wallefy.utils.ConnectivityReceiver
import org.hz240.wallefy.utils.FirestoreObj


class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var binding : ActivityMainBinding
    private var auth = FirestoreObj._auth
    private lateinit var sharedPref : SharedPreferences

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
        var snackbar: Snackbar? = null
        if (!isConnected) {
            snackbar?.dismiss()
            FirestoreObj.changeSource(Source.CACHE)
            snackbar = Snackbar.make(findViewById(R.id.root_layout), "You are offline", Snackbar.LENGTH_SHORT) //Assume "rootLayout" as the root layout of every activity
            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.colorAccent))
            snackbar?.show()
        } else if (isConnected) {
            snackbar?.dismiss()
            FirestoreObj.changeSource(Source.SERVER)
//            snackbar = Snackbar.make(findViewById(R.id.root_layout), "Connected to server", Snackbar.LENGTH_SHORT) //Assume "rootLayout" as the root layout of every activity
//            snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.green))
//            snackbar?.show()
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
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

//      Get ID from sharedpreferences
        sharedPref = this.getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)!!
        var idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            val intent = Intent (this, CommunityListActivity::class.java)
            startActivity(intent)
        }

//      Auth Check
        val currentUser = auth.currentUser
        updateUI(currentUser)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)

        NavigationUI.setupActionBarWithNavController(this, navCtrl)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        return navCtrl.navigateUp()
    }

//    override fun finish() {
//        super.finish()
//        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
//    }
//    override fun startActivity(intent: Intent?) {
//        super.startActivity(intent)
//        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//    }
}
