package net.mcbay.transmat.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
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


class CalloutPageFragment : DataFragment() {
    private var fragBinding: FragmentCalloutPageBinding? = null
    private val binding get() = fragBinding!!
    private var pageId = 1L

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

        binding.pageName.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    val imm: InputMethodManager = v.context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    writePageNameToDatabase()
                    return@OnEditorActionListener true
                }
            }
            false
        })

        binding.pageName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                writePageNameToDatabase()
            }
        }

        return binding.root
    }

    // Avoid repeat writes in instances where multiple different events off the input
    // EditText that cause writePageNameToDatabase to be called might get thrown at once
    private var previousPageName: String? = null

    private fun writePageNameToDatabase() {
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
        menu.findItem(R.id.action_delete_callout_page).isVisible =
            (pageId != TransmatApplication.DEFAULT_PAGE_ID)
        super.onPrepareOptionsMenu(menu)
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
