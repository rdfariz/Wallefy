package org.hz240.wallefy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.hz240.wallefy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)

//        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
////            when (item.itemId) {
////                R.id.to_dashboard -> {
//////                    val intent = Intent(this, MainActivity::class.java)
//////                    startActivity(intent)
////                    navCtrl.navigate(R.id.ac)
////                    Log.d("tes", "dashboard")
////                }
////                R.id.to_pemasukan -> {
//////                    val intent = Intent(this, PemasukanActivity::class.java)
//////                    startActivity(intent)
////                    Log.d("tes", "pemasukan")
////                }
////                R.id.to_pengeluaran -> {
////                    Log.d("tes", "pengeluaran")
////                }
////                R.id.to_anggota -> {
////                    Log.d("tes", "keanggota")
////                }
////            }
////            return@setOnNavigationItemSelectedListener true
////        }

        NavigationUI.setupActionBarWithNavController(this, navCtrl)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navCtrl = this.findNavController(R.id.nav_host_fragment_container)
        return navCtrl.navigateUp()
    }
}
