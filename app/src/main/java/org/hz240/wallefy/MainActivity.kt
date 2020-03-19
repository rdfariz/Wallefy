package org.hz240.wallefy

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.hz240.wallefy.databinding.ActivityMainBinding
import org.hz240.wallefy.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }else {
            Toast.makeText(this, "Sudah login", Toast.LENGTH_SHORT).show()
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        NavigationUI.setupActionBarWithNavController(this, navCtrl)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        return navCtrl.navigateUp()
    }
}
