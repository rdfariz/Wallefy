package org.hz240.wallefy.dashboard


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hz240.wallefy.R
import org.hz240.wallefy.SettingsActivity
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

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)

        binding.tvTitleTotal.text = "Total Balance"
        binding.tvUserName.text = "Hi, Raden Fariz"
        binding.tvUserStatus.text = "Bendahara Jepang"
        binding.tvMoneyTotal.text = "Rp. 80.000"
        binding.tvBalanceLatestUpdate.text = "Last Update: Maret, 20-2020 2:45 PM"

        binding.rvLatestTransactions.setNestedScrollingEnabled(false)

        viewManager = LinearLayoutManager(context)
        var myDataset: ArrayList<HashMap<String, Any>> = ArrayList()
        myDataset.add( hashMapOf("title" to "Cetak Baliho", "description" to "Event Wibu", "biaya" to "Rp. 400.000"))
        myDataset.add( hashMapOf("title" to "Penjualan Makanan", "description" to "Event Wibu", "biaya" to "Rp. 1.800.000"))
        myDataset.add( hashMapOf("title" to "Pelunasan Baju", "description" to "Internal", "biaya" to "Rp. 2.800.000"))
        viewAdapter =
            LatestTransactionsAdapter(myDataset)
        recyclerView = binding.rvLatestTransactions.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

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
