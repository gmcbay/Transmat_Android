package net.mcbay.transmat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import net.mcbay.transmat.databinding.FragmentMainBinding
import kotlin.math.ceil

class MainFragment : Fragment(), CalloutAdapter.ItemClickListener {
    private var fragBinding: FragmentMainBinding? = null
    private val binding get() = fragBinding!!
    private lateinit var adapter: CalloutAdapter

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

        val data = arrayOf(
            CalloutData("Next", R.drawable.next, "----------"),
            CalloutData("Ascendant Plane", R.drawable.ascendant_plane, "Ascendant Plane"),
            CalloutData("Black Garden", R.drawable.black_garden, "Black Garden"),
            CalloutData("Black Heart", R.drawable.black_heart, "Black Heart"),
            CalloutData("Commune", R.drawable.commune, "Commune"),
            CalloutData("Darkness", R.drawable.darkness, "Darkness"),
            CalloutData("Drink", R.drawable.drink, "Drink"),
            CalloutData("Earth", R.drawable.earth, "Earth"),
            CalloutData("Enter", R.drawable.enter, "Enter"),
            CalloutData("Fleet", R.drawable.fleet, "Fleet"),
            CalloutData("Give", R.drawable.give, "Give"),
            CalloutData("Grieve", R.drawable.grieve, "Grieve"),
            CalloutData("Guardian", R.drawable.guardian, "Guardian"),
            CalloutData("Hive", R.drawable.hive, "Hive"),
            CalloutData("Kill", R.drawable.kill, "Kill"),
            CalloutData("Knowledge", R.drawable.knowledge, "Knowledge"),
            CalloutData("Light", R.drawable.light, "Light"),
            CalloutData("Love", R.drawable.love, "Love"),
            CalloutData("Pyramid", R.drawable.pyramid, "Pyramid"),
            CalloutData("Savathun", R.drawable.savathun, "Savathun"),
            CalloutData("Scorn", R.drawable.scorn, "Scorn"),
            CalloutData("Stop", R.drawable.stop, "Stop"),
            CalloutData("Tower", R.drawable.tower, "Tower"),
            CalloutData("Traveler", R.drawable.traveler, "Traveler"),
            CalloutData("Witness", R.drawable.witness, "Witness"),
            CalloutData("Worm", R.drawable.worm, "Worm"),
            CalloutData("Worship", R.drawable.worship, "Worship")
        )

        binding.calloutGrid.visibility = View.INVISIBLE

        binding.root.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop,
                                                 oldRight, oldBottom ->
            val sizeChanged =
                (right - left) != (oldRight - oldLeft) || (bottom - top) != (oldBottom - oldTop)

            if (sizeChanged) {
                val gridFitValues = findGridFit(
                    right, bottom,
                    resources.getDimensionPixelSize(R.dimen.callout_margin) * 2, data.size
                )
                CalloutItemViewModel.getInstance().setDynamicHeight(gridFitValues.first)
                binding.calloutGrid.layoutManager =
                    GridLayoutManager(activity, gridFitValues.second)
                binding.calloutGrid.adapter = adapter
                binding.calloutGrid.visibility = View.VISIBLE
            }
        }

        adapter = CalloutAdapter(activity, data)
        adapter.setClickListener(this)

        binding.calloutGrid.adapter = adapter
    }

    private fun findGridFit(
        width: Int,
        height: Int,
        marginSize: Int,
        itemCount: Int
    ): Pair<Int, Int> {
        var resultItemSize = 0
        var resultColumnCount = 0

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

    override fun onItemClick(view: View?, position: Int) {
        val bluetoothUtil = (activity as MainActivity).getBluetoothUtil()

        if (bluetoothUtil.isDeviceConnected()) {
            bluetoothUtil.send(adapter.getCallout(position))
        } else {
            val infoSnackbar = Snackbar.make(binding.root, R.string.bluetooth_not_connected,
                Snackbar.LENGTH_LONG)
            infoSnackbar.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragBinding = null
    }
}
