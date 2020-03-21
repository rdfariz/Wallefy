package org.hz240.wallefy.communityList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.ActivityCommunityListBinding
import org.hz240.wallefy.login.LoginActivity

class CommunityListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityListBinding
    private lateinit var auth: FirebaseAuth

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    init {
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
