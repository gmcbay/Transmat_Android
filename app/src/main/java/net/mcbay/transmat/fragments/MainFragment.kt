package net.mcbay.transmat.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mcbay.transmat.*
import net.mcbay.transmat.adapters.AdapterClickListener
import net.mcbay.transmat.adapters.CalloutAdapter
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.databinding.FragmentMainBinding
import kotlin.math.ceil

class MainFragment : Fragment(), AdapterClickListener {
    private var fragBinding: FragmentMainBinding? = null
    private val binding get() = fragBinding!!
    private lateinit var adapter: CalloutAdapter

    private val dbInitializedReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            updateView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(
                dbInitializedReceiver,
                IntentFilter(TransmatApplication.DATABASE_INITIALIZED)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(dbInitializedReceiver)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentMainBinding.inflate(inflater, container, false)
        (fragBinding as FragmentMainBinding).lifecycleOwner = this
        (fragBinding as FragmentMainBinding).viewModel = CalloutItemViewModel.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calloutGrid.post {
            updateView()
        }
    }

    private fun updateView() {
        val data: MutableList<CalloutData> = mutableListOf()
        val viewWidth = binding.calloutGrid.width
        val viewHeight = binding.calloutGrid.height

        val dbJob = CoroutineScope(Dispatchers.IO).launch {
            data.addAll(
                TransmatApplication.INSTANCE.getDatabase().calloutDataDao().getPage(1)
            )
        }

        dbJob.invokeOnCompletion {
            // For the purposes of this app its best if all the callout buttons fit into the
            // given view space with no scrolling required, so make that happen
            val gridFitValues = findGridFit(
                viewWidth, viewHeight,
                resources.getDimensionPixelSize(R.dimen.callout_margin) * 2,
                data.size
            )

            activity?.runOnUiThread {
                CalloutItemViewModel.getInstance().setDynamicHeight(gridFitValues.first)
                binding.calloutGrid.layoutManager =
                    GridLayoutManager(activity, gridFitValues.second)
                adapter = CalloutAdapter(activity, data)
                binding.calloutGrid.adapter = adapter
                adapter.setClickListener(this)
            }
        }
    }

    private fun findGridFit(
        width: Int,
        height: Int,
        marginSize: Int,
        itemCount: Int
    ): Pair<Int, Int> {
        var resultItemSize = 1
        var resultColumnCount = 1

        for (columnCount in 1..itemCount) {
            val itemSize = (width / columnCount) - marginSize
            val rows = ceil(itemCount / columnCount.toFloat()).toInt()

            if (itemSize * rows < height - marginSize * rows) {
                resultItemSize = itemSize
                resultColumnCount = columnCount
                break
            }
        }

        return Pair(resultItemSize, resultColumnCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragBinding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bluetooth -> {
                (activity as MainActivity).toggleBluetooth()
                true
            }
            R.id.action_change_channel -> {
                val bluetoothUtil: BluetoothUtil = TransmatApplication.INSTANCE.getBluetoothUtil()

                if (bluetoothUtil.isDeviceConnected()) {
                    // Send an empty string, this will signal the receiver to change chat channels
                    bluetoothUtil.send("")
                } else {
                    val infoSnackbar = Snackbar.make(
                        binding.root, R.string.bluetooth_not_connected,
                        Snackbar.LENGTH_LONG
                    )
                    infoSnackbar.show()
                }
                true
            }
            R.id.action_edit_callout_pages -> {
                findNavController().navigate(R.id.to_CalloutPagesFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        activity?.let { ctx ->
            if (TransmatApplication.INSTANCE.getBluetoothUtil().isDeviceConnected()) {
                menu.findItem(R.id.action_bluetooth).icon = ContextCompat.getDrawable(
                    ctx,
                    R.drawable.ic_bluetooth_connected
                )
                CalloutItemViewModel.getInstance().setConnected(true)
            } else {
                menu.findItem(R.id.action_bluetooth).icon = ContextCompat.getDrawable(
                    ctx,
                    R.drawable.ic_bluetooth_disconnected
                )
                CalloutItemViewModel.getInstance().setConnected(false)
            }
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onItemClick(view: View?, position: Int) {
        val bluetoothUtil = TransmatApplication.INSTANCE.getBluetoothUtil()

        if (bluetoothUtil.isDeviceConnected()) {
            bluetoothUtil.send(adapter.getCallout(position).callout ?: "")
        } else {
            val infoSnackbar = Snackbar.make(
                binding.root, R.string.bluetooth_not_connected,
                Snackbar.LENGTH_LONG
            )
            infoSnackbar.show()
        }
    }
}
