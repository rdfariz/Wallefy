package org.hz240.wallefy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.login.LoginActivity
import org.hz240.wallefy.utils.FirestoreObj

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 1800

    init {
        AuthUserObj.migrateData(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            checkAuth()
        }, SPLASH_TIME_OUT)
    }

    fun checkAuth() {
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
