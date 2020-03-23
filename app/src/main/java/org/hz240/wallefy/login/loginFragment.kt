package org.hz240.wallefy.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.databinding.FragmentLoginBinding

/**
 * A simple [Fragment] subclass.
 */
class loginFragment : Fragment() {
    // Configure Google Sign In
    private val RC_SIGN_IN: Int = 1
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth

    init {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = activity?.let { GoogleSignIn.getClient(it, mGoogleSignInOptions) }!!
    }
    private fun signIn() {
        mGoogleSignInClient.signOut()
        firebaseAuth.signOut()
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this.context, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this.context, "Login sebagai ${firebaseAuth.currentUser?.displayName.toString()}", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, CommunityListActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this.context, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        configureGoogleSignIn()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.login.setOnClickListener {
            signIn()
        }

        return binding.root
    }

}
