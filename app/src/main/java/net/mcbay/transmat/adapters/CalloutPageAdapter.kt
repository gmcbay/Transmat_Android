package net.mcbay.transmat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.mcbay.transmat.data.CalloutData
import net.mcbay.transmat.databinding.CalloutPageItemBinding
import net.mcbay.transmat.drawFrom

class CalloutPageAdapter internal constructor(
    private val data: List<CalloutData>
) :
    RecyclerView.Adapter<CalloutPageAdapter.ViewHolder>() {
    private var clickListener: AdapterClickListener? = null

    inner class ViewHolder(val binding: CalloutPageItemBinding) :
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
        val binding = CalloutPageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            calloutName.text = data[position].label
            calloutImage.drawFrom(data[position])
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
