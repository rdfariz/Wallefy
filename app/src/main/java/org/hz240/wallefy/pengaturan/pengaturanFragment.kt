package org.hz240.wallefy.pengaturan


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.databinding.FragmentPengaturanBinding
import org.hz240.wallefy.login.LoginActivity

/**
 * A simple [Fragment] subclass.
 */
class pengaturanFragment : Fragment() {

    private lateinit var binding: FragmentPengaturanBinding
    private lateinit var userLoginVM: UserViewModel
    private lateinit var communityListVM: CommunityListViewModel

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }
    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)
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
        binding.setLifecycleOwner(this)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)
        binding.dataUsersViewModel = userLoginVM
        binding.dataCommunityViewModel = communityListVM

        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            Log.i("tes_pngaturan", it.toString())
            var photoUrl = it?.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_white_24dp).into(binding.ivUserImage)
            binding.tvEmail.text = it?.get("email").toString()
            binding.tvUsername.text = it?.get("displayName").toString()
        })

        binding.toSignout.setOnClickListener {view: View ->
            crScope.launch {
                signout()
            }
        }
        binding.toOutCommunity.setOnClickListener {
            crScope.launch {
                outCommunity("Keluar komunitas", "Anda yakin ingin keluar komunitas?", idCommunity.toString())
            }
        }
        return binding.root
    }


    private suspend fun signout() {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Keluar Akun")
        alertDialogBuilder.setMessage("Anda yakin ingin keluar dari akun ini?")
        alertDialogBuilder.setPositiveButton("Keluar") { dialogInterface: DialogInterface, i: Int ->
            AuthUserObj.toSignOut()
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                it.startActivity(intent)
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private suspend fun outCommunity(title: String, message: String, idCommunity: String) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Keluar") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                val outCommunity = communityListVM.outCommunity(idCommunity)
                if (outCommunity == true) {
                    activity?.let{
                        val intent = Intent (it, CommunityListActivity::class.java)
                        intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        it.startActivity(intent)
                    }
                }else {
                    Toast.makeText(context, "Gagal keluar komunitas", Toast.LENGTH_SHORT).show()
                }
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
