package org.hz240.wallefy.communityList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController

import org.hz240.wallefy.R
import org.hz240.wallefy.data.StoreCommunity
import org.hz240.wallefy.databinding.FragmentJoinCommunityBinding

/**
 * A simple [Fragment] subclass.
 */
class joinCommunityFragment : Fragment() {

    private lateinit var binding: FragmentJoinCommunityBinding
    private lateinit var store: StoreCommunity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        store = StoreCommunity(context, view)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_community, container, false)
        binding.joinCommunity.setOnClickListener {
            store.toJoinCommunity(binding.etCode.text.toString())
        }
        return binding.root
    }

}
