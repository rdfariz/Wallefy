package org.hz240.wallefy.pemasukan


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.FragmentPemasukanBinding

/**
 * A simple [Fragment] subclass.
 */
class pemasukanFragment : Fragment() {

    private lateinit var binding: FragmentPemasukanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_pemasukan, container, false)
        return binding.root
    }


}
