package org.hz240.wallefy.communityList

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.FragmentJoinCommunityBinding
import org.hz240.wallefy.utils.FirestoreObj

/**
 * A simple [Fragment] subclass.
 */
class joinCommunityFragment : Fragment() {

    private lateinit var binding: FragmentJoinCommunityBinding
    private lateinit var communityListVM: CommunityListViewModel

    val vm = Job()
    val crScope = CoroutineScope(vm + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_community, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this).get(CommunityListViewModel::class.java)

        binding.dataCommunityViewModel = communityListVM

        binding.joinCommunity.setOnClickListener {
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                var snackbar: Snackbar? = null
                snackbar = Snackbar.make(binding.root.rootView.findViewById(R.id.root_layout), "You are offline", Snackbar.LENGTH_SHORT) //Assume "rootLayout" as the root layout of every activity
                snackbar.setBackgroundTint(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
                snackbar?.show()
            }else {
                crScope.launch {
                    val community = communityListVM.checkCommunity(binding.etCode.text.toString())
                    if (community["found"] == true) {
                        joinCommunity(community["displayName"].toString(), community["message"].toString())
                    }else {
                        Toast.makeText(context, community["message"].toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    suspend fun joinCommunity(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Gabung") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                val obj = communityListVM.joinCommunity(binding.etCode.text.toString())
                if (obj["status"] == true) {
                    Toast.makeText(context, obj["message"].toString(), Toast.LENGTH_SHORT).show()
                    view?.findNavController()?.navigate(R.id.action_joinCommunityFragment_to_communityListFragment)
                }else {
                    Toast.makeText(context, obj["message"].toString(), Toast.LENGTH_SHORT).show()
                }
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
