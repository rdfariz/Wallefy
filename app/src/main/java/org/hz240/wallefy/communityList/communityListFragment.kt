package org.hz240.wallefy.communityList

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
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import org.hz240.wallefy.R
import org.hz240.wallefy.dashboard.UserViewModel
import org.hz240.wallefy.databinding.FragmentCommunityListBinding
import org.hz240.wallefy.login.LoginActivity
import java.lang.Exception

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
//      Init Binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_community_list, container, false)
        binding.setLifecycleOwner(this)
        userLoginVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)

        binding.dataUsersViewModel = userLoginVM
        binding.dataCommunityViewModel = communityListVM

//      Recycleview Community List
        viewManager = LinearLayoutManager(context)
        communityListVM.communityList.observe(viewLifecycleOwner, Observer {
            Log.i("tesItem", it.toString())
            Log.i("tesLength", (it.size == 0).toString())
            if (it.size == 0) {
                binding.rvCommunityList.visibility = View.INVISIBLE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvCommunityList.visibility = View.VISIBLE
            }
            viewAdapter = CommunityListAdapter(it)
            recyclerView = binding.rvCommunityList.apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }
        })

        binding.itemsswipetorefresh.setOnRefreshListener {
            communityListVM.refresh()
            binding.itemsswipetorefresh.isRefreshing = false
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
            R.id.to_logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        activity?.let{
            val intent = Intent (it, LoginActivity::class.java)
            it.startActivity(intent)
        }
    }

}
