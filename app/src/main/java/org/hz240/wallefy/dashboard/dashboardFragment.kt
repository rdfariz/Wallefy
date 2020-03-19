package org.hz240.wallefy.dashboard


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.hz240.wallefy.R
import org.hz240.wallefy.pengaturan.SettingsActivity
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
    private lateinit var userLoginVM: UserViewModel

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.setLifecycleOwner(this)
        transactionsVM = ViewModelProviders.of(this).get(TransactionsViewModel::class.java)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)

        binding.tvTitleTotal.text = "Total Balance"
        binding.tvUserName.text = "Hi, Raden Fariz"
        binding.tvUserStatus.text = "Bendahara Jepang"
        binding.tvMoneyTotal.text = "Rp. 80.000"
        binding.tvBalanceLatestUpdate.text = "Last Update: Maret, 20-2020 2:45 PM"
        binding.rvLatestTransactions.setNestedScrollingEnabled(false)

        binding.dataUsersViewModel = userLoginVM

        viewManager = LinearLayoutManager(context)
        viewAdapter = TransactionsAdapter(transactionsVM.transactions.value!!)
        recyclerView = binding.rvLatestTransactions.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val picasso = Picasso.get()
        userLoginVM.userLogin.observe(viewLifecycleOwner, Observer {
            var photoUrl = it.get("photoUrl").toString()
            picasso.load(photoUrl).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_black_24dp).into(binding.ivUserImage)
        })

        binding.btnSetting.setOnClickListener {view: View ->
            try {
                activity?.let{
                    val intent = Intent (it, SettingsActivity::class.java)
                    it.startActivity(intent)
                }
            }catch (e: Exception) {

            }
        }
        return binding.root
    }


}
