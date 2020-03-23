package org.hz240.wallefy.communityList

import android.content.Intent
import android.content.IntentFilter
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
import org.hz240.wallefy.login.LoginActivity
import org.hz240.wallefy.utils.ConnectivityReceiver
import org.hz240.wallefy.utils.FirestoreObj

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

    override fun onSupportNavigateUp(): Boolean {
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        return navCtrl.navigateUp()
    }

}
