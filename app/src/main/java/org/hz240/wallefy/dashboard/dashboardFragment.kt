package org.hz240.wallefy.dashboard


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.databinding.FragmentDashboardBinding
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class dashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var transactionsVM: TransactionsViewModel
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var userLoginVM: UserViewModel
    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

//    private var idCommunity: String?

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

//      Init Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.setLifecycleOwner(this)
        binding.rvLatestTransactions.setNestedScrollingEnabled(false)

        transactionsVM = ViewModelProviders.of(this, viewModelFactory { TransactionsViewModel(idCommunity.toString()) }).get(TransactionsViewModel::class.java)
        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        binding.dataUsersViewModel = userLoginVM
        binding.dataCommunityViewModel = communityListVM

        viewManager = LinearLayoutManager(context)

        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            binding.tvMoneyTotal.text = "Rp. ${it["saldo"].toString()}"
            binding.tvBalanceLatestUpdate.text = "Latest Update: ${it["latestUpdated"].toString()}"

            val latestTransactions = it["latestTransactions"] as ArrayList<HashMap<String, Any?>>

            latestTransactions?.let {
                viewAdapter = TransactionsAdapter(latestTransactions)
                recyclerView = binding.rvLatestTransactions.apply {
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
        })

//      Recycleview Transactions
//        transactionsVM.transactions.observe(viewLifecycleOwner, Observer {
//            viewManager = LinearLayoutManager(context)
//            viewAdapter = TransactionsAdapter(it)
//            recyclerView = binding.rvLatestTransactions.apply {
//                layoutManager = viewManager
//                adapter = viewAdapter
//            }
//        })

//      Sync Photo
        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            var photoUrl = it.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_white_24dp).into(binding.ivUserImage)
        })

        binding.btnSetting.setOnClickListener {view: View ->
            try {
                view.findNavController().navigate(R.id.action_to_dashboard_to_pengaturanFragment)
            }catch (e: Exception) {
                Toast.makeText(this.context, "Ada kesalahan akses", Toast.LENGTH_SHORT).show()
            }
        }

        binding.itemsswipetorefresh.setOnRefreshListener {
            communityListVM.refresh()
            binding.itemsswipetorefresh.isRefreshing = false
        }

        return binding.root
    }


}
