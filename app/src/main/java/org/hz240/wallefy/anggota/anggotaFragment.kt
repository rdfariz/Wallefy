package org.hz240.wallefy.detailCommunity.anggota


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.dashboard.TransactionsAdapter
import org.hz240.wallefy.databinding.FragmentAnggotaBinding

/**
 * A simple [Fragment] subclass.
 */
class anggotaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentAnggotaBinding
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

    private var menuDynamic = R.menu.anggota_menu
    init {
        var admin = true
        if (admin) {
            menuDynamic = R.menu.anggota_menu
        }
    }

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

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
        var idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_anggota, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)

        binding.dataCommunityViewModel = communityListVM
        viewManager = LinearLayoutManager(context)

        communityListVM.error.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.errView.visibility = View.VISIBLE
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvAnggota.visibility = View.INVISIBLE
                refresh()
            }else {
                binding.errView.visibility = View.INVISIBLE
                binding.rvAnggota.visibility = View.VISIBLE
            }
        })
        communityListVM.communitySingle.observe(viewLifecycleOwner, Observer {
            val anggota = it!!["members"] as ArrayList<HashMap<String, Any?>>
            if (anggota.size == 0) {
                binding.rvAnggota.visibility = View.INVISIBLE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvAnggota.visibility = View.VISIBLE
            }
            viewAdapter = AnggotaAdapter(anggota)
            recyclerView = binding.rvAnggota.apply {
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(menuDynamic,menu)
//        menu.clear()
//        val searchView = SearchView((context as MainActivity).supportActionBar?.themedContext ?: context)
//        menu.findItem(R.id.app_bar_search).apply {
//            setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
//            actionView = searchView
//        }
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                return false
//            }
//        })
//        searchView.setOnClickListener {view ->  Log.i("tesmenu", "search..")}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        crScope.cancel()
    }

}
