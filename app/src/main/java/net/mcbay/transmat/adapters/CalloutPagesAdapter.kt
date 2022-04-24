package net.mcbay.transmat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.mcbay.transmat.data.CalloutPage
import net.mcbay.transmat.databinding.CalloutPagesItemBinding

class CalloutPagesAdapter internal constructor(
    private val data: List<CalloutPage>
) :
    RecyclerView.Adapter<CalloutPagesAdapter.ViewHolder>() {
    private var clickListener: AdapterClickListener? = null

    inner class ViewHolder(val binding: CalloutPagesItemBinding) :
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
        val binding = CalloutPagesItemBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            pageName.text = data[position].name
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getCalloutPage(position: Int): CalloutPage {
        return data[position]
    }

    fun setClickListener(itemClickListener: AdapterClickListener?) {
        clickListener = itemClickListener
    }
}
