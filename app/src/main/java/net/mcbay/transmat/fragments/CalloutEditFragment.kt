package net.mcbay.transmat.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larswerkman.holocolorpicker.ColorPicker
import com.larswerkman.holocolorpicker.SaturationBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.MainActivity
import net.mcbay.transmat.R
import net.mcbay.transmat.TransmatApplication
import net.mcbay.transmat.adapters.AdapterClickListener
import net.mcbay.transmat.adapters.BuiltinAdapter
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.data.CalloutDisplayType
import net.mcbay.transmat.data.DataInitializer
import net.mcbay.transmat.databinding.FragmentCalloutEditBinding
import net.mcbay.transmat.drawFrom
import java.io.File

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
                            context?.let { ctx ->
                                showColorChooser(ctx, currentColor)
                            }
                        }

                        imageButton.setOnClickListener {
                            (activity as MainActivity).pickCroppedImage { path ->
                                setDataType(calloutId, CalloutDisplayType.BITMAP, path)
                            }
                        }

                        builtInButton.setOnClickListener {
                            context?.let { ctx ->
                                showBuiltinChooser(ctx)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setDataType(calloutId: Long, type: CalloutDisplayType, data: String) {
        val dbSetDataJob = CoroutineScope(Dispatchers.IO).launch {
            val dao = TransmatApplication.INSTANCE.getDatabase().calloutDataDao()
            val existingData = dao.get(calloutId)

            existingData?.let {
                // If existing callout icon is a bitmap, delete the local file prior to
                // replacing the database reference to it
                if (existingData.type == CalloutDisplayType.BITMAP) {
                    val file = File(existingData.data.toString())

                    if (file.exists()) {
                        file.delete()
                    }
                }

                existingData.type = type
                existingData.data = data

                dao.update(existingData)
            }
        }

        dbSetDataJob.invokeOnCompletion {
            onDatabaseUpdate()
        }
    }

    private fun showBuiltinChooser(ctx: Context) {
        val popupView = View.inflate(
            ctx,
            R.layout.popup_builtin_list, null
        )
        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val list = popupView.findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(ctx)
        val builtinList = DataInitializer.getBuiltinList(ctx)
        val adapter = BuiltinAdapter(ctx, builtinList)
        list.adapter = adapter

        adapter.setClickListener(object : AdapterClickListener {
            override fun onItemClick(view: View?, position: Int) {
                popupWindow.dismiss()
                setDataType(calloutId, CalloutDisplayType.DRAWABLE, builtinList[position])
            }
        })

        popupView.findViewById<Button>(
            R.id.cancel_button
        ).setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(
            context?.let { ContextCompat.getColor(it, R.color.tm_dark) }?.let { ColorDrawable(it) }
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    private fun showColorChooser(ctx: Context, currentColor: Int) {
        val builder = AlertDialog.Builder(ctx, R.style.TransmatAlertDialog)
        val view = View.inflate(
            context, R.layout.dialog_color_picker,
            null
        )
        val titleView = View.inflate(
            context,
            R.layout.dialog_color_picker_title, null
        )
        titleView.findViewById<TextView>(R.id.title).text =
            ctx.getString(R.string.choose_color)
        builder.setView(view)
        builder.setCustomTitle(titleView)
        builder.setCancelable(false)

        val picker = view.findViewById<ColorPicker>(R.id.picker)
        val saturationBar = view.findViewById<SaturationBar>(
            R.id.saturation_bar
        )
        picker.addSaturationBar(saturationBar)
        picker.setNewCenterColor(currentColor)
        picker.color = currentColor
        picker.showOldCenterColor = false
        val dialog = builder.create()

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.choose_button).setOnClickListener {
            dialog.dismiss()
            setDataType(calloutId, CalloutDisplayType.COLOR, picker.color.toString())
        }

        dialog.show()
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
                val dao = TransmatApplication.INSTANCE.getDatabase().calloutDataDao()
                val existingData = dao.get(calloutId)

                existingData?.let {
                    // If existing callout icon is a bitmap, delete the local file prior to
                    // deleting the database reference to this callout
                    if (existingData.type == CalloutDisplayType.BITMAP) {
                        val file = File(existingData.data.toString())

                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }

                dao.delete(calloutId)
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
