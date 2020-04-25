package org.hz240.wallefy.detailCommunity.anggota


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Source
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.hz240.wallefy.R
import org.hz240.wallefy.ui.communityList.CommunityListActivity
import org.hz240.wallefy.viewModel.CommunityListViewModel
import org.hz240.wallefy.data.GlobalObj
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
        }else if (GlobalObj.getPending() == true) {
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
        communityListVM = ViewModelProviders.of(this, viewModelFactory {
            CommunityListViewModel(
                idCommunity.toString()
            )
        }).get(CommunityListViewModel::class.java)
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
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }else {
                toAddPerson()
            }
        }

        _handleInitLoading()
        _handleLoading()

        return binding.root
    }

    private fun showMessages(message: String, type: String) {
        var snackbar: Snackbar? = null
        snackbar = Snackbar.make(binding.root.rootView.findViewById(R.id.root_layout), message, Snackbar.LENGTH_SHORT)
        var color = when(type) {
            "success" -> R.color.green
            "error" -> R.color.colorAccent
            else -> R.color.colorPrimary
        }
        snackbar.setBackgroundTint(ResourcesCompat.getColor(resources, color, null))
        snackbar?.show()
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
        builder.setMessage("Inputkan ID anggota")
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_display_name, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.et_displayName)
        builder.setView(dialogLayout)
        builder.setPositiveButton("Tambah") { dialogInterface, i ->
            if (editText.text.length != 0 && FirestoreObj._sourceDynamic != Source.CACHE) {
                crScope.launch {
                    val dataPerson = activityVM.getPerson(editText.text.toString())
                    if (dataPerson != null) {
                        confirmPerson(editText.text.toString(), dataPerson)
                    }else {
                        showMessages("User tidak ditemukan", "error")
                    }
                }
            }else if(editText.text.length == 0){
                showMessages("ID User tidak boleh kosong", "error")
            }else {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun confirmPerson(idPerson: String, dataPerson: HashMap<String, Any?>) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("${dataPerson.get("displayName")}")
        builder.setMessage("Anda yakin ingin menambahkan ${dataPerson.get("displayName")}")
        builder.setPositiveButton("Tambah") { dialogInterface, i ->
            idCommunity?.let { addPerson(it, idPerson) }
        }
        builder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        builder.show()
    }
    private fun addPerson(idCommunity: String, idPerson: String) {
        crScope.launch {
            try {
                val obj = activityVM.addPerson(idCommunity, idPerson)
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    refresh()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }catch (e: Exception) {

            }finally {

            }
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
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }else {
                toDeletePerson(idPerson, displayName)
                dialog.dismiss()
            }
        }
        dialogLayout.findViewById<Button>(R.id.to_admin_person).setOnClickListener {
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }else {
                toAdminPerson(idPerson, displayName)
                dialog.dismiss()
            }
        }
        dialogLayout.findViewById<Button>(R.id.to_unadmin_person).setOnClickListener {
            if (FirestoreObj._sourceDynamic == Source.CACHE) {
                showMessages("Anda harus terhubung ke jaringan", "error")
            }else {
                toUnAdminPerson(idPerson, displayName)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun toDeletePerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Hapus Dari Komunitas")
        alertDialogBuilder.setMessage("Anda yakin ingin menghapus ${displayName} dari komunitas?")
        alertDialogBuilder.setPositiveButton("Hapus") { dialogInterface: DialogInterface, i: Int ->
            idCommunity?.let { exeDeletePerson(it, idPerson) }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun exeDeletePerson(idCommunity: String, idPerson: String) {
        crScope.launch {
            try {
                val obj = activityVM.deletePerson(idCommunity, idPerson)
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    refresh()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }catch (e: Exception) {

            }finally {

            }
        }
    }

    private fun toAdminPerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Jadikan Admin")
        alertDialogBuilder.setMessage("Anda yakin ingin menjadikan ${displayName} sebagai admin?")
        alertDialogBuilder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
            idCommunity?.let { exeAdminPerson(it, idPerson) }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun exeAdminPerson(idCommunity: String, idPerson: String) {
        crScope.launch {
            try {
                val obj = activityVM.adminPerson(idCommunity, idPerson)
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    refresh()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }catch (e: Exception) {

            }finally {

            }
        }
    }

    private fun toUnAdminPerson(idPerson: String, displayName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle("Jadikan Member")
        alertDialogBuilder.setMessage("Anda yakin ingin menjadikan ${displayName} sebagai member?")
        alertDialogBuilder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
            idCommunity?.let { exeUnAdminPerson(it, idPerson) }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, i: Int ->

        }
        alertDialogBuilder.show()
    }
    private fun exeUnAdminPerson(idCommunity: String, idPerson: String) {
        crScope.launch {
            try {
                val obj = activityVM.unAdminPerson(idCommunity, idPerson)
                if (obj["status"] == true) {
                    showMessages(obj["message"].toString(), "success")
                    refresh()
                }else {
                    showMessages(obj["message"].toString(), "error")
                }
            }catch (e: Exception) {

            }finally {

            }
        }
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

    fun _handleInitLoading() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_loading, null)

        alertDialogBuilder.setTitle("Menyiapkan Data")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setView(dialogLayout)
        val dialog = alertDialogBuilder.create()

        communityListVM.loading.observe(viewLifecycleOwner, Observer {
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
