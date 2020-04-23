package org.hz240.wallefy.dashboard

import android.app.AlertDialog
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.databinding.FragmentDashboardBinding
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.viewModel.ActivityViewModel
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class dashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var userLoginVM: UserViewModel
    private lateinit var activityVM: ActivityViewModel

    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    private var myClipboard: ClipboardManager? = null

    private val isAdmin = MutableLiveData<Boolean>()
    private var idCommunity : String? = null

    override fun onResume() {
        super.onResume()
        crScope.launch {
            userLoginVM.migrateData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        myClipboard = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?;

//      Get ID from sharedpreferences
        sharedPref = activity?.getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)!!
        idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }

//      Init Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.setLifecycleOwner(this)
        binding.rvLatestTransactions.setNestedScrollingEnabled(false)

        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        activityVM = ViewModelProviders.of(this).get(ActivityViewModel::class.java)

        binding.dataUsersViewModel = userLoginVM
        binding.dataCommunityViewModel = communityListVM

        viewManager = LinearLayoutManager(context)

        communityListVM.error.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.errView.visibility = View.VISIBLE
                binding.latestTransactionsSection.visibility = View.INVISIBLE
                refresh()
            }else {
                binding.errView.visibility = View.INVISIBLE
                binding.latestTransactionsSection.visibility = View.VISIBLE
            }
        })
        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            val latestTransactions = it!!["latestTransactions"] as ArrayList<HashMap<String, Any?>>
            isAdmin.value = it!!["isAdmin"] as Boolean?
            if (latestTransactions.size == 0) {
                binding.latestTransactionsSection.visibility = View.INVISIBLE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.INVISIBLE
                binding.latestTransactionsSection.visibility = View.VISIBLE
            }
            binding.tvMoneyTotal.text = it!!["saldo"].toString()
            binding.tvBalanceLatestUpdate.text = "Latest Update: ${it!!["latestUpdated"].toString()}"

            latestTransactions?.let {
                viewAdapter = TransactionsAdapter(latestTransactions, TransactionsAdapter.OnClickListener{
                    toDetailTransaction(it)
                })
                recyclerView = binding.rvLatestTransactions.apply {
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
        })
        communityListVM.loadingRefresh.observe(viewLifecycleOwner, Observer {
            binding.itemsswipetorefresh.isRefreshing = it
        })

//      Sync Photo
        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            var photoUrl = it?.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_person_white_24dp).error(R.drawable.ic_person_white_24dp).into(binding.ivUserImage)
        })

        binding.btnSetting.setOnClickListener {view: View ->
            try {
                view.findNavController().navigate(R.id.action_to_dashboard_to_pengaturanFragment)
            }catch (e: Exception) {
                Toast.makeText(this.context, "Ada kesalahan akses", Toast.LENGTH_SHORT).show()
            }
        }

        binding.itemsswipetorefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.itemsswipetorefresh.setOnRefreshListener {
            refresh()
        }

        _handleInitLoading()
        _handleLoading()

        return binding.root
    }

    private fun refresh() {
        crScope.launch {
            val newData = communityListVM.refresh()
//              Check IfMember
            if (!newData) {
                Toast.makeText(context, "Anda tidak terdaftar dikomunitas terkait", Toast.LENGTH_SHORT).show()
                activity?.let{
                    val intent = Intent (it, CommunityListActivity::class.java)
                    intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    it.startActivity(intent)
                }
            }
        }
    }

    private fun toDetailTransaction(transaction: HashMap<String, Any?>) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_detail_transactions, null)
        val dialog: androidx.appcompat.app.AlertDialog

        val idTransaction = transaction["id"].toString()
        val title = transaction["title"].toString()
        val type = transaction["type"].toString()
        val biaya = transaction["biaya"].toString()
        when(type) {
            "pemasukan" -> dialogLayout.findViewById<ImageView>(R.id.iv_type_transaction).setImageResource(R.drawable.ic_undraw_savings_dwkw)
            "pengeluaran" -> dialogLayout.findViewById<ImageView>(R.id.iv_type_transaction).setImageResource(R.drawable.ic_undraw_result_5583)
        }

        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialogLayout.findViewById<Button>(R.id.to_delete_transactions).visibility = View.VISIBLE
            }else {
                dialogLayout.findViewById<Button>(R.id.to_delete_transactions).visibility = View.GONE
            }
        })

        alertDialogBuilder.setView(dialogLayout)
        dialog = alertDialogBuilder.create()

        dialogLayout.findViewById<TextView>(R.id.tv_title_transaction).text = title
        dialogLayout.findViewById<TextView>(R.id.tv_type_transaction).text = type
        dialogLayout.findViewById<TextView>(R.id.tv_biaya_transaction).text = biaya
        dialogLayout.findViewById<Button>(R.id.to_delete_transactions).setOnClickListener {
            idCommunity?.let { id ->
                toDeleteTransaction(id, idTransaction, title)
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    private fun toDeleteTransaction(idCommunity: String, idTransaction: String, title: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Hapus Transaksi")
        alertDialogBuilder.setMessage("Anda yakin ingin menghapus Transaksi ${title}")
        alertDialogBuilder.setPositiveButton("Hapus") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                activityVM.deleteTransaction(idCommunity, idTransaction)
                refresh()
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

        activityVM.loading.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialog.show()
            }else {
                dialog.dismiss()
            }
        })
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

    private fun showInfoId() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("ID Saya")
        alertDialogBuilder.setMessage(FirestoreObj._auth.uid.toString())
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("Salin ID") { dialogInterface: DialogInterface, i: Int ->
            copy(FirestoreObj._auth.uid.toString())
        }
        alertDialogBuilder.show()
    }

    private fun copy(txt: String) {
        val myClip = ClipData.newPlainText("ID User", txt)
        myClipboard?.setPrimaryClip(myClip)
        Toast.makeText(context, "ID Berhasil disalin", Toast.LENGTH_SHORT).show();
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.to_showId -> showInfoId()
            R.id.to_communityList -> {
                val intent = Intent(context, CommunityListActivity::class.java)
                intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context?.startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
