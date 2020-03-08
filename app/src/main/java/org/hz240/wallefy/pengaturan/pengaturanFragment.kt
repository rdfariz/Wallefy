package org.hz240.wallefy.pengaturan


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.hz240.wallefy.MainActivity
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.FragmentPengaturanBinding

/**
 * A simple [Fragment] subclass.
 */
class pengaturanFragment : Fragment() {

    private lateinit var binding: FragmentPengaturanBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pengaturan, container, false)
        binding.btnClose.setOnClickListener {view: View ->
//            view.findNavController().navigate(R.id.action_pengaturanFragment_to_to_dashboard)
            activity?.let{
                val intent = Intent (it, MainActivity::class.java)
                it.startActivity(intent)
            }
        }
        return binding.root
    }


}
