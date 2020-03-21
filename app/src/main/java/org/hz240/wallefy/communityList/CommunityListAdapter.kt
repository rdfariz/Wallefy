package org.hz240.wallefy.communityList

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_community_list.view.*
import kotlinx.android.synthetic.main.transactions_item.view.*
import kotlinx.android.synthetic.main.transactions_item.view.tv_biaya
import kotlinx.android.synthetic.main.transactions_item.view.tv_title
import org.hz240.wallefy.MainActivity
import org.hz240.wallefy.R

class CommunityListAdapter(private val myDataset: ArrayList<HashMap<String, Any?>>) :
    RecyclerView.Adapter<CommunityListAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val community_item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_list, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(
            community_item
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.view.tv_title.text = myDataset[position].get("displayName").toString()
        holder.view.tv_biaya.text = ""
        holder.view.tv_description.text = "ID: "+myDataset[position].get("idCommunity").toString()

        holder.view.to_detailCommunity.setOnClickListener { view: View ->
            try {
                val preferences = view.getContext().getSharedPreferences("selectedCommunity", Context.MODE_PRIVATE)
                with(preferences.edit()) {
                    putString("idCommunity", myDataset[position].get("idCommunity").toString())
                    commit()
                }
                val context = holder.view.context
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            } catch (e: Exception) {

            } finally {

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}