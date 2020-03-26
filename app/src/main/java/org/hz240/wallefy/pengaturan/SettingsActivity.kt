package org.hz240.wallefy.pengaturan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

//    override fun finish() {
//        super.finish()
//        overridePendingTransition(
//            R.anim.enter_from_right,
//            R.anim.exit_to_left
//        );
//    }
//
//    override fun startActivity(intent: Intent?) {
//        super.startActivity(intent)
//        overridePendingTransition(
//            R.anim.enter_from_left,
//            R.anim.exit_to_right
//        );
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.settings_activity)
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)

        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Account Info"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

//        NavigationUI.setupActionBarWithNavController(this, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
//        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
//        return navCtrl.navigateUp()
    }
}