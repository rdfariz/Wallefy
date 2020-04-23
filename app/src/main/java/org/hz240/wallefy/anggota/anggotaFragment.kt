package org.hz240.wallefy.detailCommunity.anggota


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Source
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_people.view.*
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.communityList.CommunityListActivity
import org.hz240.wallefy.communityList.CommunityListViewModel
import org.hz240.wallefy.dashboard.TransactionsAdapter
import org.hz240.wallefy.databinding.FragmentAnggotaBinding
import org.hz240.wallefy.utils.FirestoreObj
import org.hz240.wallefy.viewModel.ActivityViewModel

/**
 * A simple [Fragment] subclass.
 */
class anggotaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var binding: FragmentAnggotaBinding
    private lateinit var communityListVM: CommunityListViewModel
    private lateinit var activityVM: ActivityViewModel

    private lateinit var sharedPref : SharedPreferences

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
    }

    private val isAdmin = MutableLiveData<Boolean>()
    private var idCommunity : String? = null

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
        idCommunity = sharedPref.getString("idCommunity", null)
        if (idCommunity == null) {
            activity?.let{
                val intent = Intent (it, CommunityListActivity::class.java)
                it.startActivity(intent)
            }
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_anggota, container, false)
        binding.setLifecycleOwner(this)
        communityListVM = ViewModelProviders.of(this, viewModelFactory { CommunityListViewModel(idCommunity.toString()) }).get(CommunityListViewModel::class.java)
        activityVM = ViewModelProviders.of(this).get(ActivityViewModel::class.java)

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
            isAdmin.value = it!!["isAdmin"] as Boolean?
            if (anggota.size == 0) {
                binding.rvAnggota.visibility = View.INVISIBLE
                binding.emptyView.visibility = View.VISIBLE
            }else {
                binding.emptyView.visibility = View.INVISIBLE
                binding.rvAnggota.visibility = View.VISIBLE
            }
            viewAdapter = AnggotaAdapter(anggota, AnggotaAdapter.OnClickListener{
                toDetailPerson(it)
            })
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

        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.toAddAnggota.visibility = View.VISIBLE
            }else {
                binding.toAddAnggota.visibility = View.INVISIBLE
            }
        })
        binding.toAddAnggota.setOnClickListener {
            toAddPerson()
        }

        _handleLoading()

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

    fun toAddPerson() {
        val builder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        builder.setTitle("Tambah Anggota")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_display_name, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.et_displayName)
        editText.setText("GeWuTSgM6vWgSMbvwEaGhKfqnkF2")
        builder.setView(dialogLayout)
        builder.setPositiveButton("Tambah") { dialogInterface, i ->
            if (editText.text.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                addPerson(editText.text.toString())
            }else if(editText.text.length == 0){
                Toast.makeText(context, "ID User tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(context, "Anda harus terhubung ke jaringan", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun addPerson(idPerson: String) {
        crScope.launch {
            idCommunity?.let { activityVM.addPerson(it, idPerson) }
            refresh()
        }
    }

    private fun toDetailPerson(person: HashMap<String, Any?>) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_detail_person, null)
        val dialog: androidx.appcompat.app.AlertDialog

        val idPerson = person["id"].toString()
        val displayName = person["displayName"].toString()
        val type = person["type"].toString()
        val photoUrl = person["photoUrl"].toString()

        isAdmin.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                if (idPerson == FirestoreObj._auth.uid.toString()) {
                    dialogLayout.findViewById<Button>(R.id.to_delete_person).visibility = View.GONE
                    dialogLayout.findViewById<Button>(R.id.to_admin_person).visibility = View.GONE
                    dialogLayout.findViewById<Button>(R.id.to_unadmin_person).visibility = View.VISIBLE
                }else {
                    if (type.toUpperCase() == "ADMIN") {
                        dialogLayout.findViewById<Button>(R.id.to_delete_person).visibility = View.GONE
                        dialogLayout.findViewById<Button>(R.id.to_admin_person).visibility = View.GONE
                        dialogLayout.findViewById<Button>(R.id.to_unadmin_person).visibility = View.VISIBLE
                    }else {
                        dialogLayout.findViewById<Button>(R.id.to_delete_person).visibility = View.VISIBLE
                        dialogLayout.findViewById<Button>(R.id.to_admin_person).visibility = View.VISIBLE
                        dialogLayout.findViewById<Button>(R.id.to_unadmin_person).visibility = View.GONE
                    }
                }
            }else {
                dialogLayout.findViewById<Button>(R.id.to_delete_person).visibility = View.GONE
                dialogLayout.findViewById<Button>(R.id.to_admin_person).visibility = View.GONE
                dialogLayout.findViewById<Button>(R.id.to_unadmin_person).visibility = View.GONE
            }
        })

        dialogLayout.findViewById<TextView>(R.id.tv_user_name).text = displayName
        dialogLayout.findViewById<TextView>(R.id.tv_user_type).text = type
        dialogLayout.findViewById<TextView>(R.id.tv_user_id).text = idPerson

        val picasso = Picasso.get()
        picasso.load(photoUrl).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_white_24dp).into(dialogLayout.findViewById<ImageView>(R.id.iv_user_image))

        alertDialogBuilder.setView(dialogLayout)
        dialog = alertDialogBuilder.create()

        dialogLayout.findViewById<Button>(R.id.to_delete_person).setOnClickListener {
            toDeletePerson(idPerson, displayName)
            dialog.dismiss()
        }
        dialogLayout.findViewById<Button>(R.id.to_admin_person).setOnClickListener {
            toAdminPerson(idPerson, displayName)
            dialog.dismiss()
        }
        dialogLayout.findViewById<Button>(R.id.to_unadmin_person).setOnClickListener {
            toUnAdminPerson(idPerson, displayName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun toDeletePerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Hapus Dari Komunitas")
        alertDialogBuilder.setMessage("Anda yakin ingin menghapus ${displayName} dari komunitas?")
        alertDialogBuilder.setPositiveButton("Hapus") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                idCommunity?.let { activityVM.deletePerson(it, idPerson) }
                refresh()
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun toAdminPerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Jadikan Admin")
        alertDialogBuilder.setMessage("Anda yakin ingin menjadikan ${displayName} sebagai admin?")
        alertDialogBuilder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                idCommunity?.let { activityVM.adminPerson(it, idPerson) }
                refresh()
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun toUnAdminPerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Jadikan Member")
        alertDialogBuilder.setMessage("Anda yakin ingin menjadikan ${displayName} sebagai member?")
        alertDialogBuilder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
            crScope.launch {
                idCommunity?.let { activityVM.unAdminPerson(it, idPerson) }
                refresh()
            }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }

    fun _handleLoading() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_loading, null)

        alertDialogBuilder.setTitle("Memproses Data")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setView(dialogLayout)
        val dialog = alertDialogBuilder.create()

        activityVM.loading.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                dialog.show()
            }else {
                dialog.dismiss()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.anggota_menu,menu)
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
