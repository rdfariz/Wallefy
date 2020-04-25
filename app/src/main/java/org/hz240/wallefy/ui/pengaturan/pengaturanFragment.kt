package org.hz240.wallefy.ui.main.pengeluaran.pengaturan


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.ui.communityList.CommunityListActivity
import org.hz240.wallefy.viewModel.CommunityListViewModel
import org.hz240.wallefy.viewModel.UserViewModel
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.databinding.FragmentPengaturanBinding
import org.hz240.wallefy.ui.login.LoginActivity

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

    private val isAdmin = MutableLiveData<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.actionBar?.setTitle("Pengaturan")

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
        communityListVM = ViewModelProviders.of(this, viewModelFactory {
            CommunityListViewModel(
                idCommunity.toString()
            )
        }).get(CommunityListViewModel::class.java)
        binding.dataUsersViewModel = userLoginVM
        binding.dataCommunityViewModel = communityListVM

        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            isAdmin.value = it!!["isAdmin"] as Boolean
        })

        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.toSettingsCommunity.visibility = View.VISIBLE
            }else {
                binding.toSettingsCommunity.visibility = View.GONE
            }
        })

        binding.toAccountInfo.setOnClickListener {
            it.findNavController().navigate(R.id.action_pengaturanFragment2_to_pengaturanUser2)
        }
        binding.toSettingsCommunity.setOnClickListener {
            it.findNavController().navigate(R.id.action_pengaturanFragment2_to_pengaturanKomunitas2)
        }
        binding.toSignout.setOnClickListener {view: View ->
            crScope.launch {
                signout()
            }
        }
        binding.toOutCommunity.setOnClickListener {view: View ->
            crScope.launch {
                outCommunity("Keluar komunitas", "Anda yakin ingin keluar komunitas?", idCommunity.toString())
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        _handleLoading()

        return binding.root
    }

    private fun showMessages(message: String, type: String) {
        var snackbar: Snackbar? = null
        snackbar = Snackbar.make(binding.root.rootView.findViewById(R.id.root_layout), message, Snackbar.LENGTH_SHORT)
        var color = when(type) {
            "success" -> R.color.green
            "error" -> R.color.colorAccent
            else -> R.color.colorPrimary
        }
        snackbar.setBackgroundTint(ResourcesCompat.getColor(resources, color, null))
        snackbar?.show()
    }

    fun _handleLoading() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_loading, null)

        alertDialogBuilder.setTitle("Memproses Data")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setView(dialogLayout)
        val dialog = alertDialogBuilder.create()

        communityListVM.loading.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialog.show()
            }else {
                dialog.dismiss()
            }
        })
    }

    private suspend fun signout() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
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
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
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
                    showMessages("Gagal keluar komunitas", "error")
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
