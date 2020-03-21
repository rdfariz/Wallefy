package org.hz240.wallefy

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("toggleLoading")
fun toggleLoading(progressBar: ProgressBar, bool: Boolean) {
    if (bool == true) {
        progressBar.visibility = View.VISIBLE
    }else {
        progressBar.visibility = View.GONE
    }
}

@BindingAdapter("toggleLoading")
fun toggleLoading(item: androidx.constraintlayout.widget.ConstraintLayout, bool: Boolean) {
    if (bool == true) {
        item.animate()
            .translationY(item.getHeight().toFloat())
            .alpha(1.0f)
            .setDuration(300)
        item.visibility = View.VISIBLE
    }else {
        item.animate()
            .translationY(item.getHeight().toFloat())
            .alpha(0.0f)
            .setDuration(300)
        item.visibility = View.GONE
    }
}

@BindingAdapter("toggleLoading")
fun toggleLoading(item: TextView, bool: Boolean) {
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