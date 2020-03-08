package org.hz240.wallefy.anggota

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.people_item.view.*
import org.hz240.wallefy.R

class AnggotaAdapter(private val myDataset: ArrayList<HashMap<String, Any>>) :
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
            .inflate(R.layout.people_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(people_item)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.people.tv_username.text = myDataset[position].get("username").toString()
        holder.people.tv_status.text = myDataset[position].get("status").toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}