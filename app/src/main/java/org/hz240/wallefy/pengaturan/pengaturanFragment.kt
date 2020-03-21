package org.hz240.wallefy.pengaturan


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.hz240.wallefy.MainActivity
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.data.StoreCommunity
import org.hz240.wallefy.databinding.FragmentPengaturanBinding
import org.hz240.wallefy.login.LoginActivity

/**
 * A simple [Fragment] subclass.
 */
class pengaturanFragment : Fragment() {

    private lateinit var binding: FragmentPengaturanBinding
    private lateinit var userLoginVM: UserViewModel
    private lateinit var store: StoreCommunity

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    private lateinit var sharedPref : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//      Get ID from sharedpreferences
        sharedPref = activity?.getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)!!
        var idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pengaturan, container, false)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        binding.dataUsersViewModel = userLoginVM

        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            Log.i("tes_pngaturan", it.toString())
            var photoUrl = it.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_white_24dp).into(binding.ivUserImage)
            binding.tvEmail.text = it["email"].toString()
            binding.tvUsername.text = it["displayName"].toString()
        })

//        binding.btnClose.setOnClickListener {view: View ->
//            view.findNavController().navigate(R.id.action_pengaturanFragment_to_to_dashboard)
////            activity?.let{
////                val intent = Intent (it, MainActivity::class.java)
////                it.startActivity(intent)
////            }
//        }

        binding.toSignout.setOnClickListener {view: View ->
            FirebaseAuth.getInstance().signOut()
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                it.startActivity(intent)
            }
        }
        store = StoreCommunity(context, view)
        binding.toOutCommunity.setOnClickListener {
            store.toOutCommunity(idCommunity.toString())
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }
        return binding.root
    }


}
