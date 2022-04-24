package net.mcbay.transmat.fragments

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.R
import net.mcbay.transmat.TransmatApplication
import net.mcbay.transmat.adapters.AdapterClickListener
import net.mcbay.transmat.adapters.CalloutPagesAdapter
import net.mcbay.transmat.data.CalloutPage
import net.mcbay.transmat.databinding.FragmentCalloutPagesBinding

class CalloutPagesFragment : DataFragment() {
    private var fragBinding: FragmentCalloutPagesBinding? = null
    private val binding get() = fragBinding!!
    private var calloutPagesAdapter: CalloutPagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentCalloutPagesBinding.inflate(inflater, container, false)
        (fragBinding as FragmentCalloutPagesBinding).lifecycleOwner = this
        updateView()
        return binding.root
    }

    override fun updateView(scrollToEnd: Boolean) {
        super.updateView(scrollToEnd)

        var data: List<CalloutPage>? = null

        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            data = TransmatApplication.INSTANCE.getDatabase().calloutPageDao().getAll()
        }

        dbJob.invokeOnCompletion {
            data?.let { calloutPageData ->
                activity?.runOnUiThread {
                    with(binding) {
                        list.layoutManager = LinearLayoutManager(context)
                        calloutPagesAdapter = CalloutPagesAdapter(calloutPageData)
                        list.adapter = calloutPagesAdapter
                        calloutPagesAdapter?.setClickListener(object : AdapterClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val args = bundleOf(
                                    "calloutPageId" to calloutPagesAdapter?.getCalloutPage(
                                        position
                                    )?.id
                                )

                                findNavController().navigate(R.id.to_CalloutPageFragment, args)
                            }
                        })

                        if (scrollToEnd) {
                            list.scrollToPosition(calloutPageData.size - 1)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_callout_pages, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_new_callout_page -> {
                addCalloutPage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addCalloutPage() {
        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val database = TransmatApplication.INSTANCE.getDatabase()

            val nextId = database.calloutPageDao().getMaxId() + 1

            val calloutPage = CalloutPage(
                name = activity?.getString(R.string.callout_page_name_template, nextId),
                displayOrder = nextId
            )

            database.calloutPageDao().insertPage(calloutPage)
        }

        dbJob.invokeOnCompletion {
            onDatabaseUpdate()
        }
    }
}
