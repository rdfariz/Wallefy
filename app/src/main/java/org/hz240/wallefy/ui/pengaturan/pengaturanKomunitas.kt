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
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.firebase.firestore.Source

import org.hz240.wallefy.R
import org.hz240.wallefy.ui.communityList.CommunityListActivity
import org.hz240.wallefy.viewModel.CommunityListViewModel
import org.hz240.wallefy.databinding.FragmentPengaturanKomunitasBinding
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.utils.FormatText

/**
 * A simple [Fragment] subclass.
 */
class pengaturanKomunitas : Fragment() {

    private lateinit var binding: FragmentPengaturanKomunitasBinding
    private lateinit var communityListVM: CommunityListViewModel

    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    private val isAdmin = MutableLiveData<Boolean>()
    private var idCommunity : String? = null
    private var displayName: String? = null
    private var lock: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPref = activity?.getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)!!
        idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pengaturan_komunitas, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this, viewModelFactory {
            CommunityListViewModel(
                idCommunity.toString()
            )
        }).get(CommunityListViewModel::class.java)
        binding.dataCommunityViewModel = communityListVM

        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            isAdmin.value = it!!["isAdmin"] as Boolean
            displayName = it!!["displayName"] as String
            lock = it!!["lock"] as Boolean
            binding.tvCommunityName.text = displayName
            binding.tvCommunityId.text = idCommunity
            binding.tvCommunityLock.text = FormatText().printStatusLock(it!!["lock"] as Boolean)
        })

        binding.topAppBar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.toChangeDisplayName.setOnClickListener {
            idCommunity?.let { id -> toChangeDisplayName(id) }
        }
        binding.toLockCommunity.setOnClickListener {
            idCommunity?.let { id -> toLockCommunity(id) }
        }
        binding.toDeleteCommunity.setOnClickListener {
            idCommunity?.let { id -> toDeleteCommunity(id) }
        }
        binding.toResetSaldo.setOnClickListener {
            idCommunity?.let { id -> toResetCommunity(id) }
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

    private fun toChangeDisplayName(idCommunity: String) {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        builder.setTitle("Nama Komunitas")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_display_name, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.et_displayName)
        editText.setText(displayName)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Update") { dialogInterface, i ->
            if (editText.text.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                if (binding.tvCommunityName.text.toString() != editText.text.toString()) {
                    changeDisplayName(idCommunity, editText.text.toString())
                }
            }else if(editText.text.length == 0){
                showMessages("Nama Komunitas tidak boleh kosong", "error")
            }else {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun changeDisplayName(idCommunity: String, newDisplayName: String) {
        crScope.launch {
            val obj = communityListVM.changeDisplayName(idCommunity, newDisplayName)
            if (obj["status"] == true) {
                showMessages(obj["message"].toString(), "success")
            }else {
                showMessages(obj["message"].toString(), "error")
            }
        }
    }

    private fun toResetCommunity(idCommunity: String) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Reset Komunitas")
        builder.setMessage("Anda yakin ingin Reset Komunitas?\nSeluruh data transaksi akan dibersihkan & saldo akan menjadi 0")
        builder.setPositiveButton("Ya, Reset") { dialogInterface, i ->
            if (FirestoreObj._sourceDynamic != Source.CACHE) {
                resetCommunity(idCommunity)
            }else {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun resetCommunity(idCommunity: String) {
        crScope.launch {
            val obj = communityListVM.resetCommunity(idCommunity)
            if (obj["status"] == true) {
                showMessages(obj["message"].toString(), "success")
            }else {
                showMessages(obj["message"].toString(), "error")
            }
        }
    }

    private fun toLockCommunity(idCommunity: String) {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        builder.setTitle("Atur Kunci Komunitas")
        builder.setMessage("Mengunci komunitas akan membuat komunitas tertutup & tidak bisa menerima anggota baru")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_choose, null)
        val rbTrue  = dialogLayout.findViewById<RadioButton>(R.id.rb_true)
        val rbFalse  = dialogLayout.findViewById<RadioButton>(R.id.rb_false)

        if (lock == true) {
            dialogLayout.findViewById<RadioGroup>(R.id.rb_group).check(R.id.rb_true)
        }else {
            dialogLayout.findViewById<RadioGroup>(R.id.rb_group).check(R.id.rb_false)
        }

        builder.setView(dialogLayout)
        builder.setPositiveButton("Update") { dialogInterface, i ->
            var newLock: Boolean? = null
            if (rbTrue.isChecked) {
                newLock = true
            }else if (rbFalse.isChecked) {
                newLock = false
            }

            if (lock != null && newLock != null && FirestoreObj._sourceDynamic != Source.CACHE) {
                if (newLock != lock) {
                    newLock?.let { lockCommunity(idCommunity, it) }
                }
            }else if(lock == null){
                showMessages("Status Komunitas tidak boleh kosong", "error")
            }else {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun lockCommunity(idCommunity: String, lock: Boolean) {
        crScope.launch {
            val obj = communityListVM.setLock(idCommunity, lock)
            if (obj["status"] == true) {
                showMessages(obj["message"].toString(), "success")
            }else {
                showMessages(obj["message"].toString(), "error")
            }
        }
    }

    private fun toDeleteCommunity(idCommunity: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Hapus Komunitas")
        alertDialogBuilder.setMessage("Anda yakin ingin menghapus komunitas ${displayName}?")
        alertDialogBuilder.setPositiveButton("Ya, Hapus") { dialogInterface: DialogInterface, i: Int ->
            deleteCommunity(idCommunity)
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun deleteCommunity(idCommunity: String) {
        crScope.launch {
            val obj = communityListVM.deleteCommunity(idCommunity)
            if (obj["status"] == true) {
                Toast.makeText(context, obj["message"].toString(), Toast.LENGTH_SHORT).show()
                activity?.let{
                    val intent = Intent (it, CommunityListActivity::class.java)
                    intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    it.startActivity(intent)
                }
            }else {
                showMessages(obj["message"].toString(), "error")
            }
        }
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

}

private operator fun Int.invoke(rbTrue: Int) {

}
