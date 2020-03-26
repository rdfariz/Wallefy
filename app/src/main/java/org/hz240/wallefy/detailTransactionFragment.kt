package org.hz240.wallefy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.hz240.wallefy.databinding.FragmentDetailTransactionBinding

/**
 * A simple [Fragment] subclass.
 */
class detailTransactionFragment : Fragment() {

    private lateinit var binding: FragmentDetailTransactionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_transaction, container, false)
        binding.tvData.text = arguments?.getString("dataTransaction").toString()
        return binding.root
    }

}
