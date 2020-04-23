package org.hz240.wallefy.pengaturan

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Source
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

import org.hz240.wallefy.R
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.databinding.FragmentPengaturanUserBinding
import org.hz240.wallefy.utils.FirestoreObj
import kotlin.coroutines.CoroutineContext

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

//      Sync Photo
        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            var photoUrl = it?.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_person_white_24dp).error(R.drawable.ic_person_white_24dp).into(binding.ivUserImage)
        })

        binding.toChangeDisplayName.setOnClickListener {
            changeDisplayName()
        }

        _handleLoading()
        
        return binding.root
    }

    fun withEditText() {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        builder.setTitle("Change Display Name")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_display_name, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.et_displayName)
        editText.setText(userLoginVM.userLogin.value?.get("displayName").toString())
        builder.setView(dialogLayout)
        builder.setPositiveButton("Update") { dialogInterface, i ->
            if (editText.text.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                crScope.launch {
                    userLoginVM.changeDisplayName(editText.text.toString())
                    userLoginVM.migrateData()
                }
            }else if(editText.text.length == 0){
                Toast.makeText(context, "Display Name tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(context, "Anda harus terhubung ke jaringan", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }

    private fun changeDisplayName() {
        withEditText()
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
