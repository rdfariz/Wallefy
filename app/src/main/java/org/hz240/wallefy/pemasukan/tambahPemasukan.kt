package org.hz240.wallefy.pemasukan

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.data.ActivityObj
import org.hz240.wallefy.databinding.FragmentPemasukanBinding
import org.hz240.wallefy.databinding.FragmentTambahPemasukanBinding
import org.hz240.wallefy.viewModel.ActivityViewModel

/**
 * A simple [Fragment] subclass.
 */
class tambahPemasukan : Fragment() {

    private lateinit var binding: FragmentTambahPemasukanBinding
    private lateinit var sharedPref : SharedPreferences
    private lateinit var activityVM: ActivityViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tambah_pemasukan, container, false)
        binding.setLifecycleOwner(this)
        activityVM = ViewModelProviders.of(this).get(ActivityViewModel::class.java)
        binding.dataActivityViewModel = activityVM

        binding.pushItem.setOnClickListener {
            hideKeyboard()
            addPemasukan(idCommunity.toString())
        }

        _handleLoading()

        return binding.root
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    fun addPemasukan(idCommunity: String) {
        crScope.launch {
            try {
                activityVM.addPemasukan(idCommunity, binding.etTitle.text.toString(), Integer.parseInt(binding.etBiaya.text.toString()).toLong())
                val bundle = bundleOf("onRefresh" to true)
                view?.findNavController()?.navigate(R.id.action_tambahPemasukan_to_pemasukan, bundle)
            }catch (e: Exception) {

            }finally {

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

        activityVM.loading.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialog.show()
            }else {
                dialog.dismiss()
            }
        })
    }
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
