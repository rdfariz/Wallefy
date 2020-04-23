package org.hz240.wallefy.detailCommunity.anggota

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_people.view.*
import org.hz240.wallefy.R

class AnggotaAdapter(private val myDataset: ArrayList<HashMap<String, Any?>>) :
    RecyclerView.Adapter<AnggotaAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val people: View) : RecyclerView.ViewHolder(people)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val people_item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_people, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(people_item)
    }

    val picasso = Picasso.get()
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.people.tv_username.text = myDataset[position].get("displayName").toString()

        val type = myDataset[position].get("type").toString()
        if (type == "admin") { holder.people.tv_status.setTextColor(ContextCompat.getColor(holder.people.context, R.color.green)) }
        else { holder.people.tv_status.setTextColor(ContextCompat.getColor(holder.people.context, R.color.blue_custom)) }
        holder.people.tv_status.text = type.capitalize()

        picasso.load(myDataset[position].get("photoUrl").toString()).placeholder(R.drawable.ic_sync_black_24dp).error(R.drawable.ic_person_white_24dp).into(holder.people.iv_user_image)
    }

    override fun getItemCount() = myDataset.size
}