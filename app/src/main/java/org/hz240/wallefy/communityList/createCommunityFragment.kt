package org.hz240.wallefy.communityList

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*

import org.hz240.wallefy.R
import org.hz240.wallefy.data.GlobalObj
import org.hz240.wallefy.databinding.FragmentCreateCommunityBinding
import org.hz240.wallefy.utils.FirestoreObj

/**
 * A simple [Fragment] subclass.
 */
class createCommunityFragment : Fragment() {

    private lateinit var binding: FragmentCreateCommunityBinding
    private lateinit var communityListVM: CommunityListViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_community, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)
        binding.dataCommunityViewModel = communityListVM

        binding.createCommunity.setOnClickListener {
            hideKeyboard()
            if (binding.etCode.text?.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                crScope.launch {
                    createCommunity(binding.etCode.text.toString())
                }
            }else if(binding.etCode.text?.length == 0){
                showMessages("Nama Komunitas tidak boleh kosong", "error")
            }else {
                showMessages("Kamu harus terhubung ke jaringan", "error")
            }
        }

        _handleLoading()
        return binding.root
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
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

    private suspend fun createCommunity(displayName: String) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Buat Komunitas")
        builder.setMessage("Anda yakin ingin membuat komunitas dengan nama ${displayName}")
        builder.setPositiveButton("Ya") { dialogInterface, i ->
            crScope.launch {
                val obj = communityListVM.createCommunity(displayName)
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    GlobalObj.setPending(true)
                    activity?.onBackPressed()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
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


    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
