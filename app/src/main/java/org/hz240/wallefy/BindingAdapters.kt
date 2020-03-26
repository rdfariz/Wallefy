package org.hz240.wallefy

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("toggleLoading")
fun toggleLoading(progressBar: ProgressBar, bool: Boolean) {
    progressBar.bringToFront()
    if (bool == true) {
        progressBar.visibility = View.VISIBLE
    }else {
        progressBar.visibility = View.INVISIBLE
    }
}

@BindingAdapter("toggleLoading")
fun toggleLoading(item: androidx.constraintlayout.widget.ConstraintLayout, bool: Boolean) {
    item.bringToFront()
    if (bool == true) {
        item.animate()
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }else {
        item.animate()
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.GONE
    }
}

@BindingAdapter("toggleLoading")
fun toggleLoading(item: RelativeLayout, bool: Boolean) {
    item.bringToFront()
    Log.i("tesBool", bool.toString())
    if (bool == true) {
        item.animate()
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }else {
        item.animate()
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.INVISIBLE
    }
}

@BindingAdapter("toggleLoading")
fun toggleLoading(item: TextView, bool: Boolean) {
    item.bringToFront()
    if (bool == true) {
        item.animate()
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.INVISIBLE
    }else {
        item.animate()
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }
}


@BindingAdapter("toggleLoading")
fun toggleLoading(item: androidx.recyclerview.widget.RecyclerView, bool: Boolean) {
    item.bringToFront()
    if (bool == true) {
        item.animate()
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.INVISIBLE
    }else {
        item.animate()
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }
}
@BindingAdapter("toggleLoading")
fun toggleLoading(item: ScrollView, bool: Boolean) {
    item.bringToFront()
    if (bool == true) {
        item.animate()
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.INVISIBLE
    }else {
        item.animate()
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }
}