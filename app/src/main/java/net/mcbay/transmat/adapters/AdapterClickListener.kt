package net.mcbay.transmat.adapters

import android.view.View

interface AdapterClickListener {
    fun onItemClick(view: View?, position: Int)
}