package org.hz240.wallefy.pemasukan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.data.ActivityObj
import org.hz240.wallefy.databinding.FragmentPemasukanBinding
import org.hz240.wallefy.databinding.FragmentTambahPemasukanBinding
import org.hz240.wallefy.viewModel.ActivityViewModel

/**
 * A simple [Fragment] subclass.
 */
class tambahPemasukan : Fragment() {

    private lateinit var binding: FragmentTambahPemasukanBinding
    private lateinit var sharedPref : SharedPreferences
    private lateinit var activityVM: ActivityViewModel

    private val vm = Job()
    private val crScope = CoroutineScope(vm + Dispatchers.Main)

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

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tambah_pemasukan, container, false)
        binding.setLifecycleOwner(this)
        activityVM = ViewModelProviders.of(this).get(ActivityViewModel::class.java)
        binding.dataActivityViewModel = activityVM

//        activityVM.loading.observe(viewLifecycleOwner, Observer {
//            Log.i("tesLOADING", it.toString())
//            binding.loadingPanel.bringToFront()
//            if (it == true) {
//                binding.loadingPanel.visibility = View.VISIBLE
//            }else {
//                binding.loadingPanel.visibility = View.INVISIBLE
//            }
//        })

        binding.pushItem.setOnClickListener {
            addPemasukan(idCommunity.toString())
        }
        return binding.root
    }

    fun addPemasukan(idCommunity: String) {
        crScope.launch {
            try {
                activityVM.addPemasukan(idCommunity, binding.etTitle.text.toString(), Integer.parseInt(binding.etBiaya.text.toString()).toLong())
                val bundle = bundleOf("onRefresh" to true)
                view?.findNavController()?.navigate(R.id.action_tambahPemasukan_to_pemasukan, bundle)
            }catch (e: Exception) {

            }finally {

            }
        }
    }

}
