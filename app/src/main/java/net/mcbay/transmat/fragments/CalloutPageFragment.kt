package net.mcbay.transmat.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import java.io.File


class CalloutPageFragment : DataFragment() {
    private var fragBinding: FragmentCalloutPageBinding? = null
    private val binding get() = fragBinding!!
    private var pageId = TransmatApplication.DEFAULT_PAGE_ID
    private val args: CalloutPageFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        pageId = args.calloutPageId
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

            if (pageName != previousPageName) {
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
                                findNavController().navigate(
                                    R.id.to_CalloutEditFragment,
                                    CalloutEditFragmentArgs(
                                        calloutPageAdapter.getCallout(position).id ?: 0L
                                    ).toBundle()
                                )
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
                var calloutCount = 0

                val dbJob = CoroutineScope(Dispatchers.IO).launch {
                    val dao = TransmatApplication.INSTANCE.getDatabase().calloutPageDao()
                    calloutCount = dao.getCalloutCount(pageId)
                }

                dbJob.invokeOnCompletion {
                    // If callout page has defined callouts, confirm deletion with user
                    if (calloutCount > 0) {
                        activity?.runOnUiThread {
                            AlertDialog.Builder(context)
                                .setTitle(context?.getString(R.string.confirm_page_delete_title))
                                .setMessage(
                                    context?.resources?.getQuantityString(
                                        R.plurals.confirm_page_delete, calloutCount, calloutCount
                                    )
                                )
                                .setPositiveButton(context?.getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                    deleteCalloutPage()
                                }
                                .setNegativeButton(
                                    context?.getString(R.string.cancel)
                                ) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create().show()
                        }
                    } else {
                        deleteCalloutPage()
                    }
                }
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
            val color = context?.let { ContextCompat.getColor(it, R.color.tm_primary) } ?: 0

            val data = CalloutData(
                pageId = pageId,
                displayOrder = nextDisplayOrder,
                label = label,
                callout = label,
                type = CalloutDisplayType.COLOR,
                data = String.format("%06X", 0xFFFFFF and color)
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
                val app = TransmatApplication.INSTANCE
                val dao = app.getDatabase().calloutPageDao()
                val existingBitmapDatas = dao.getCalloutsOfType(pageId, CalloutDisplayType.BITMAP)

                for (existingBitmapData in existingBitmapDatas) {
                    // If existing callout icon is a bitmap, delete the local file prior to
                    // deleting the database reference to this callout
                    val file = File(existingBitmapData.data.toString())

                    if (file.exists()) {
                        file.delete()
                    }
                }

                app.getDatabase().calloutDataDao().deletePage(pageId)
                dao.delete(pageId)

                // If we are deleting the currently selected callout page go back to the default
                // page selection (pageId 1 which can't be deleted)
                val prefs = app.getPrefs()

                if (prefs.getSelectedPageId() == pageId) {
                    prefs.setSelectedPageId(TransmatApplication.DEFAULT_PAGE_ID)
                }
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
