package org.hz240.wallefy.anggota


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.hz240.wallefy.R
import org.hz240.wallefy.databinding.FragmentAnggotaBinding

/**
 * A simple [Fragment] subclass.
 */
class anggotaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentAnggotaBinding
    private lateinit var anggotaVM: AnggotaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_anggota, container, false)
        binding.rvAnggota.setNestedScrollingEnabled(false)
        anggotaVM = ViewModelProviders.of(this).get(AnggotaViewModel::class.java)

        viewManager = LinearLayoutManager(context)
//        var myDataset: ArrayList<HashMap<String, Any>> = ArrayList()
//        myDataset.add( hashMapOf("username" to "Fariz", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Syahrul", "status" to "Freelance"))
//        myDataset.add( hashMapOf("username" to "Fauzian", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))
//        myDataset.add( hashMapOf("username" to "Fariz", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Syahrul", "status" to "Freelance"))
//        myDataset.add( hashMapOf("username" to "Fauzian", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))
//        myDataset.add( hashMapOf("username" to "Fariz", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Syahrul", "status" to "Freelance"))
//        myDataset.add( hashMapOf("username" to "Fauzian", "status" to "Mahasiswa"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))
//        myDataset.add( hashMapOf("username" to "Basari", "status" to "gamers"))

        viewAdapter = AnggotaAdapter(anggotaVM.anggota.value!!)
        recyclerView = binding.rvAnggota.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        return binding.root
    }


}
