package net.mcbay.transmat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import net.mcbay.transmat.CalloutItemViewModel
import net.mcbay.transmat.MainActivity
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.data.CalloutDisplayType
import net.mcbay.transmat.databinding.CalloutItemBinding

class CalloutAdapter internal constructor(
    private val context: Context?,
    private val data: List<CalloutData>
) :
    RecyclerView.Adapter<CalloutAdapter.ViewHolder>() {
    private var clickListener: AdapterClickListener? = null

    inner class ViewHolder(val binding: CalloutItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        override fun onClick(view: View?) {
            if (clickListener != null) {
                clickListener!!.onItemClick(view, bindingAdapterPosition)
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CalloutItemBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        binding.lifecycleOwner = (context as MainActivity)
        binding.viewModel = CalloutItemViewModel.getInstance()
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        context?.let { ctx ->
            with(holder.binding) {
                calloutName.text = data[position].label

                if (data[position].type == CalloutDisplayType.DRAWABLE) {
                    calloutImage.setImageDrawable(
                        AppCompatResources.getDrawable(
                            ctx,
                            ctx.resources.getIdentifier(
                                data[position].data,
                                "drawable", ctx.packageName
                            )
                        )
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getCallout(position: Int): CalloutData {
        return data[position]
    }

    fun setClickListener(itemClickListener: AdapterClickListener?) {
        clickListener = itemClickListener
    }
}
