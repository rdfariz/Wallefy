package org.hz240.wallefy.pengaturan

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*

import org.hz240.wallefy.R
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.databinding.FragmentPengaturanUserBinding
import org.hz240.wallefy.utils.FirestoreObj

/**
 * A simple [Fragment] subclass.
 */
class pengaturanUser : Fragment() {

    private lateinit var binding: FragmentPengaturanUserBinding
    private lateinit var userLoginVM: UserViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pengaturan_user, container, false)
        binding.setLifecycleOwner(this)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        binding.dataUsersViewModel = userLoginVM

        binding.toChangeDisplayName.setOnClickListener {
            toChangeDisplayName()
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


    private fun toChangeDisplayName() {
        changeDisplayName()
    }
    fun changeDisplayName() {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        builder.setTitle("Nama Akun")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_display_name, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.et_displayName)
        editText.setText(userLoginVM.userLogin.value?.get("displayName").toString())
        builder.setView(dialogLayout)
        builder.setPositiveButton("Update") { dialogInterface, i ->
            if (editText.text.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                if (binding.tvUserName.text.toString() != editText.text.toString()) {
                    exeChangeDisplayName(editText.text.toString())
                }
            }else if(editText.text.length == 0){
                showMessages("Nama Akun tidak boleh kosong", "error")
            }else {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun exeChangeDisplayName(newDisplayName: String) {
        crScope.launch {
            val obj = userLoginVM.changeDisplayName(newDisplayName)
            if (obj["status"] == true) {
                showMessages(obj["message"].toString(), "success")
                userLoginVM.migrateData()
            }else {
                showMessages(obj["message"].toString(), "error")
            }
        }
    }

    private fun changePhotoUrl() {

    }

    fun _handleLoading() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_loading, null)

        alertDialogBuilder.setTitle("Memproses Data")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setView(dialogLayout)
        val dialog = alertDialogBuilder.create()

        userLoginVM.loading.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialog.show()
            }else {
                dialog.dismiss()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.cancel()
    }

}
