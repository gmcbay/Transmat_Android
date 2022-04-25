package net.mcbay.transmat.fragments

import android.os.Bundle
import android.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.R
import net.mcbay.transmat.TransmatApplication
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.data.CalloutDisplayType
import net.mcbay.transmat.databinding.FragmentCalloutEditBinding
import net.mcbay.transmat.drawFrom
import top.defaults.colorpicker.ColorPickerPopup

class CalloutEditFragment : DataFragment() {
    private var fragBinding: FragmentCalloutEditBinding? = null
    private val binding get() = fragBinding!!
    private var calloutId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        calloutId = arguments?.getLong("calloutId") ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentCalloutEditBinding.inflate(inflater, container, false)
        (fragBinding as FragmentCalloutEditBinding).lifecycleOwner = this
        updateView()

        with(binding) {
            var previousLabel: String? = null
            var previousCallout: String? = null

            initEditText(calloutLabel) {
                val label = calloutLabel.text.toString()

                if (previousLabel?.equals(label) == true) {
                    val dbJob = CoroutineScope(Dispatchers.IO).launch {
                        TransmatApplication.INSTANCE.getDatabase().calloutDataDao().setLabel(
                            calloutId, label
                        )
                    }

                    dbJob.invokeOnCompletion {
                        previousLabel = label
                        onDatabaseUpdate()
                    }
                }
            }

            initEditText(calloutMade) {
                val callout = calloutMade.text.toString()

                if (previousCallout != callout) {
                    val dbJob = CoroutineScope(Dispatchers.IO).launch {
                        TransmatApplication.INSTANCE.getDatabase().calloutDataDao().setCallout(
                            calloutId, callout
                        )
                    }

                    dbJob.invokeOnCompletion {
                        previousCallout = callout
                        onDatabaseUpdate()
                    }
                }
            }
        }

        return binding.root
    }

    override fun updateView(scrollToEnd: Boolean) {
        super.updateView(scrollToEnd)

        var data: CalloutData? = null

        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            val database = TransmatApplication.INSTANCE.getDatabase()
            data = database.calloutDataDao().get(calloutId)
        }

        dbJob.invokeOnCompletion {
            data?.let { calloutData ->
                activity?.runOnUiThread {
                    with(binding) {
                        calloutLabel.setText(calloutData.label)
                        calloutMade.setText(calloutData.callout)
                        val currentColor = calloutImage.drawFrom(calloutData)

                        colorButton.setOnClickListener {
                            ColorPickerPopup.Builder(context)
                                .initialColor(currentColor)
                                .enableBrightness(true)
                                .enableAlpha(false)
                                .okTitle(context?.getString(R.string.choose))
                                .cancelTitle(context?.getString(R.string.cancel))
                                .showIndicator(true)
                                .showValue(false)
                                .build()
                                .show(colorButton, object : ColorPickerPopup.ColorPickerObserver() {
                                    override fun onColorPicked(color: Int) {
                                        val dbColorJob = CoroutineScope(Dispatchers.IO).launch {
                                            val dao = TransmatApplication.INSTANCE.getDatabase()
                                                .calloutDataDao()
                                            dao.setData(calloutId, color.toString())
                                            dao.setType(calloutId, CalloutDisplayType.COLOR)
                                        }

                                        dbColorJob.invokeOnCompletion {
                                            onDatabaseUpdate()
                                        }
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_callout_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_callout -> {
                deleteCallout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_delete_callout).isVisible = (calloutId != -1L)
        super.onPrepareOptionsMenu(menu)
    }

    private fun deleteCallout() {
        if (calloutId != -1L) {
            val dbJob = CoroutineScope(Dispatchers.IO).launch {
                TransmatApplication.INSTANCE.getDatabase().calloutDataDao().delete(calloutId)
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
