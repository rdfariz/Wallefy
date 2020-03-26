package org.hz240.wallefy.communityList

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.hz240.wallefy.data.AuthUserObj
import org.hz240.wallefy.R
import org.hz240.wallefy.data.CommunityObj
import org.hz240.wallefy.databinding.FragmentCommunityListBinding
import org.hz240.wallefy.login.LoginActivity

/**
 * A simple [Fragment] subclass.
 */
class communityListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentCommunityListBinding
    private lateinit var communityListVM: CommunityListViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

    init {
        CommunityObj.reset(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
//      Init Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_community_list, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)

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

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.community_list_menu,menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.to_joinCommunity -> view?.findNavController()?.navigate(R.id.action_communityListFragment_to_joinCommunityFragment)
            R.id.to_logout -> crScope.launch { signout() }
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun signout() {
        val alertDialogBuilder = AlertDialog.Builder(context)
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
