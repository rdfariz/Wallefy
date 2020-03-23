package org.hz240.wallefy.login

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.Source
import org.hz240.wallefy.MainActivity
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.databinding.ActivityLoginBinding
import org.hz240.wallefy.utils.ConnectivityReceiver
import org.hz240.wallefy.utils.FirestoreObj

class LoginActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private lateinit var binding: ActivityLoginBinding

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
}
