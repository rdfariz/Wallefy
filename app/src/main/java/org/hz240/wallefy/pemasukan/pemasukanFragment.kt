package org.hz240.wallefy.pemasukan


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.dashboard.TransactionsAdapter
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.databinding.FragmentPemasukanBinding
import org.hz240.wallefy.login.LoginActivity
import org.hz240.wallefy.viewModel.ActivityViewModel


/**
 * A simple [Fragment] subclass.
 */
class pemasukanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentPemasukanBinding
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var activityVM: ActivityViewModel

    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    private val isAdmin = MutableLiveData<Boolean>()
    private var idCommunity : String? = null

    override fun onResume() {
        super.onResume()
        if (arguments?.getBoolean("onRefresh") == true) {
            refresh()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
//      Get ID from sharedpreferences
        sharedPref = activity?.getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)!!
        idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pemasukan, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)
        activityVM = ViewModelProviders.of(this).get(ActivityViewModel::class.java)

        binding.dataCommunityViewModel = communityListVM
        viewManager = LinearLayoutManager(context)

        communityListVM.error.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.errView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvPemasukan.visibility = View.INVISIBLE
                refresh()
            }else {
                binding.errView.visibility = View.INVISIBLE
                binding.rvPemasukan.visibility = View.VISIBLE
            }
        })
        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            val pemasukan = it!!["pemasukan"] as ArrayList<HashMap<String, Any?>>
            isAdmin.value = it!!["isAdmin"] as Boolean?
            if (pemasukan.size == 0) {
                binding.rvPemasukan.visibility = View.INVISIBLE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvPemasukan.visibility = View.VISIBLE
            }
            viewAdapter = TransactionsAdapter(pemasukan, TransactionsAdapter.OnClickListener{
                toDetailTransaction(it)
            })
            recyclerView = binding.rvPemasukan.apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }
        })
        communityListVM.loadingRefresh.observe(viewLifecycleOwner, Observer {
            binding.itemsswipetorefresh.isRefreshing = it
        })

        binding.itemsswipetorefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.itemsswipetorefresh.setOnRefreshListener {
            refresh()
        }

        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.toAddPemasukan.visibility = View.VISIBLE
            }else {
                binding.toAddPemasukan.visibility = View.INVISIBLE
            }
        })
        binding.toAddPemasukan.setOnClickListener {
            it.findNavController()?.navigate(R.id.action_pemasukan_to_tambahPemasukan)
        }

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

    private fun toClearPemasukan() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Bersihkan Pemasukan")
        alertDialogBuilder.setMessage("Anda yakin ingin membersihkan semua data pemasukan?")
        alertDialogBuilder.setPositiveButton("Bersihkan") { dialogInterface: DialogInterface, i: Int ->
            clearPemasukan()
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun clearPemasukan() {
        crScope.launch {
            idCommunity?.let {
                activityVM.clearPemasukan(it)
                refresh()
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                menu.findItem(R.id.to_clear_pemasukan).setVisible(true);
            }else {
                menu.findItem(R.id.to_clear_pemasukan).setVisible(false);
            }
        })
        super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.pemasukan_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.to_clear_pemasukan -> toClearPemasukan()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
