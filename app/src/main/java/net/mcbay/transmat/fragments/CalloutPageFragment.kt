package net.mcbay.transmat.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.R
import net.mcbay.transmat.TransmatApplication
import net.mcbay.transmat.adapters.AdapterClickListener
import net.mcbay.transmat.adapters.CalloutPageAdapter
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.databinding.FragmentCalloutPageBinding

class CalloutPageFragment : Fragment() {
    private var fragBinding: FragmentCalloutPageBinding? = null
    private val binding get() = fragBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentCalloutPageBinding.inflate(inflater, container, false)
        (fragBinding as FragmentCalloutPageBinding).lifecycleOwner = this
        updateView()
        return binding.root
    }

    private fun updateView(scrollToEnd: Boolean = false) {
        var data: List<CalloutData>? = null

        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            data = TransmatApplication.INSTANCE.getDatabase().calloutDataDao().getPage(
                arguments?.getLong("calloutPageId") ?: TransmatApplication.DEFAULT_PAGE_ID
            )
        }

        dbJob.invokeOnCompletion {
            data?.let { calloutData ->
                activity?.runOnUiThread {
                    with(binding) {
                        list.layoutManager = LinearLayoutManager(context)
                        val calloutPageAdapter = CalloutPageAdapter(calloutData)
                        list.adapter = calloutPageAdapter
                        calloutPageAdapter.setClickListener(object : AdapterClickListener {
                            override fun onItemClick(view: View?, position: Int) {

                            }
                        })

                        if (scrollToEnd) {
                            list.scrollToPosition(calloutData.size - 1)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_callout_page, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_callout_page -> {
                deleteCalloutPage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val pageId = arguments?.getLong("calloutPageId") ?: TransmatApplication.DEFAULT_PAGE_ID
        menu.findItem(R.id.action_delete_callout_page).isVisible =
            (pageId != TransmatApplication.DEFAULT_PAGE_ID)
        super.onPrepareOptionsMenu(menu)
    }

    private fun deleteCalloutPage() {
        val pageId = arguments?.getLong("calloutPageId") ?: TransmatApplication.DEFAULT_PAGE_ID

        if (pageId != TransmatApplication.DEFAULT_PAGE_ID) {
            val dbJob = CoroutineScope(Dispatchers.IO).launch {
                TransmatApplication.INSTANCE.getDatabase().calloutPageDao().delete(pageId)
            }

            dbJob.invokeOnCompletion {
                activity?.let {
                    it.runOnUiThread {
                        it.onBackPressed()
                    }
                }
            }
        }
    }
}
