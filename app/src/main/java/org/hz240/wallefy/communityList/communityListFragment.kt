package org.hz240.wallefy.communityList

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.R
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.data.CommunityObj
import org.hz240.wallefy.data.GlobalObj
import org.hz240.wallefy.databinding.FragmentCommunityListBinding
import org.hz240.wallefy.login.LoginActivity
import org.hz240.wallefy.utils.FirestoreObj

/**
 * A simple [Fragment] subclass.
 */
class communityListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentCommunityListBinding
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var userLoginVM: UserViewModel

    private var myClipboard: ClipboardManager? = null
    private var displayNameAccount : String? = FirestoreObj._auth.currentUser?.displayName.toString()

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        CommunityObj.reset(true)
    }

    override fun onResume() {
        super.onResume()
        crScope.launch {
            userLoginVM.migrateData()
            if (GlobalObj.getPending() == true) {
                communityListVM.refresh()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        myClipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?;

//      Init Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_community_list, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)

        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            displayNameAccount = it?.get("displayName").toString()
        })

        binding.dataCommunityViewModel = communityListVM

//      Recycleview Community List
        viewManager = LinearLayoutManager(context)
        communityListVM.communityList.observe(viewLifecycleOwner, Observer {
            Log.i("tesArr", it.toString())
            if (it.size == 0) {
                binding.rvCommunityList.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.GONE
                binding.rvCommunityList.visibility = View.VISIBLE
            }
            viewAdapter = CommunityListAdapter(it)
            recyclerView = binding.rvCommunityList.apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }
        })
        communityListVM.loadingRefresh.observe(viewLifecycleOwner, Observer {
            binding.itemsswipetorefresh.isRefreshing = it
        })

        binding.itemsswipetorefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.itemsswipetorefresh.setOnRefreshListener {
            crScope.launch {
                communityListVM.refresh()
            }
        }

        _handleInitLoading()

        return binding.root
    }

    private fun showInfoId() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Info Akun")
        alertDialogBuilder.setMessage("Nama: ${displayNameAccount}\nID: ${FirestoreObj._auth.currentUser?.uid.toString()}\n\nEmail: ${FirestoreObj._auth.currentUser?.email.toString()}")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("Salin ID") { dialogInterface: DialogInterface, i: Int ->
            copy(FirestoreObj._auth.uid.toString(), "ID")
        }
        alertDialogBuilder.show()
    }
    private fun copy(txt: String, label: String) {
        val myClip = ClipData.newPlainText(label, txt)
        myClipboard?.setPrimaryClip(myClip)
        showMessages("${label} Berhasil disalin", "success")
    }

    private fun showInfoApp() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Tentang Aplikasi")
        alertDialogBuilder.setMessage("Aplikasi dibuat oleh Tim 240Hz Telkom University")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.show()
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

    fun _handleInitLoading() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_loading, null)

        alertDialogBuilder.setTitle("Menyiapkan Data")
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.community_list_menu,menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.to_account_info -> showInfoId()
            R.id.to_createCommunity -> view?.findNavController()?.navigate(R.id.action_communityListFragment_to_toCreateCommunity)
            R.id.to_joinCommunity -> view?.findNavController()?.navigate(R.id.action_communityListFragment_to_joinCommunityFragment)
            R.id.to_logout -> crScope.launch { signout() }
            R.id.to_about_app -> showInfoApp()
        }
        return super.onOptionsItemSelected(item)
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

    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
