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
import net.mcbay.transmat.adapters.CalloutPageAdapter
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.data.CalloutDisplayType
import net.mcbay.transmat.databinding.FragmentCalloutPageBinding

class CalloutPageFragment : DataFragment() {
    private var fragBinding: FragmentCalloutPageBinding? = null
    private val binding get() = fragBinding!!
    private var pageId = TransmatApplication.DEFAULT_PAGE_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        pageId = arguments?.getLong("calloutPageId") ?: TransmatApplication.DEFAULT_PAGE_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentCalloutPageBinding.inflate(inflater, container, false)
        (fragBinding as FragmentCalloutPageBinding).lifecycleOwner = this
        updateView()

        var previousPageName: String? = null

        initEditText(binding.pageName) {
            val pageName = binding.pageName.text.toString()

            if (previousPageName != pageName) {
                val dbJob = CoroutineScope(Dispatchers.IO).launch {
                    TransmatApplication.INSTANCE.getDatabase().calloutPageDao().setName(
                        pageId,
                        pageName
                    )
                }

                dbJob.invokeOnCompletion {
                    previousPageName = pageName
                    onDatabaseUpdate()
                }
            }
        }

        return binding.root
    }

    override fun updateView(scrollToEnd: Boolean) {
        super.updateView(scrollToEnd)

        var data: List<CalloutData>? = null
        var name = ""

        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val database = TransmatApplication.INSTANCE.getDatabase()

            data = database.calloutDataDao().getPage(pageId)
            name = database.calloutPageDao().getName(pageId)
        }

        dbJob.invokeOnCompletion {
            data?.let { calloutData ->
                activity?.runOnUiThread {
                    with(binding) {
                        pageName.setText(name)
                        list.layoutManager = LinearLayoutManager(context)
                        val calloutPageAdapter = CalloutPageAdapter(calloutData)
                        list.adapter = calloutPageAdapter
                        calloutPageAdapter.setClickListener(object : AdapterClickListener {
                            override fun onItemClick(view: View?, position: Int) {
                                val args = bundleOf(
                                    "calloutId" to calloutPageAdapter.getCallout(
                                        position
                                    ).id
                                )

                                findNavController().navigate(R.id.to_CalloutEditFragment, args)
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
            R.id.action_add_callout -> {
                addCallout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_delete_callout_page).isVisible =
            (pageId != TransmatApplication.DEFAULT_PAGE_ID)
        super.onPrepareOptionsMenu(menu)
    }

    private fun addCallout() {
        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val database = TransmatApplication.INSTANCE.getDatabase()
            val nextDisplayOrder = (database.calloutDataDao().getMaxDisplayOrder(pageId) ?: 0L) + 1
            val label = context?.getString(R.string.callout_template, nextDisplayOrder) ?: ""
            val color = context?.getColor(R.color.tm_primary) ?: 0

            val data = CalloutData(
                pageId = pageId,
                displayOrder = nextDisplayOrder,
                label = label,
                callout = label,
                type = CalloutDisplayType.COLOR,
                data = String.format("#%06X", 0xFFFFFF and color)
            )

            database.calloutDataDao().insertData(data)
        }

        dbJob.invokeOnCompletion {
            onDatabaseUpdate()
        }
    }

    private fun deleteCalloutPage() {
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
