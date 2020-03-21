package org.hz240.wallefy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import org.hz240.wallefy.communityList.CommunityListActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 1800 // 1 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            startActivity(Intent(this, CommunityListActivity::class.java))

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}
