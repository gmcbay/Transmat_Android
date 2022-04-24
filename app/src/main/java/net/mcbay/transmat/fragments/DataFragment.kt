package net.mcbay.transmat.fragments

import androidx.fragment.app.Fragment
import net.mcbay.transmat.MainActivity
import net.mcbay.transmat.TransmatApplication

open class DataFragment : Fragment() {
    open fun updateView(scrollToEnd: Boolean = false) {

    }

    fun onDatabaseUpdate() {
        (TransmatApplication.INSTANCE.getCurrentActivity() as MainActivity).onDatabaseUpdate()
    }
}
