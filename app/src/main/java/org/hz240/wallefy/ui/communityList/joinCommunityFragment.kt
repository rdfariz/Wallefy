package org.hz240.wallefy.ui.communityList

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.data.GlobalObj
import org.hz240.wallefy.databinding.FragmentJoinCommunityBinding
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.viewModel.CommunityListViewModel

/**
 * A simple [Fragment] subclass.
 */
class joinCommunityFragment : Fragment() {

    private lateinit var binding: FragmentJoinCommunityBinding
    private lateinit var communityListVM: CommunityListViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_community, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)

        binding.dataCommunityViewModel = communityListVM

        binding.joinCommunity.setOnClickListener {
            hideKeyboard()
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                showMessages("Kamu harus terhubung ke jaringan", "error")
            }else if(binding.etCode.text.toString().length <= 0 || binding.etCode.text.toString() == "" || binding.etCode.text == null) {
                showMessages("Kode Komunitas tidak boleh kosong", "error")
            }else {
                crScope.launch {
                    val community = communityListVM.checkCommunity(binding.etCode.text.toString())
                    if (community["found"] == true) {
                        joinCommunity(community["displayName"].toString(), community["message"].toString())
                    }else {
                        showMessages(community["message"].toString(), "error")
                    }
                }
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

    suspend fun joinCommunity(title: String, message: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Gabung") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                val obj = communityListVM.joinCommunity(binding.etCode.text.toString())
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    GlobalObj.setPending(true)
                    activity?.onBackPressed()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
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
