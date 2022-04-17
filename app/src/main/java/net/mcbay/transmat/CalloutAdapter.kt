package net.mcbay.transmat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import net.mcbay.transmat.databinding.CalloutItemBinding

class CalloutAdapter internal constructor(
    private val context: Context?,
    private val data: Array<CalloutData>
) :
    RecyclerView.Adapter<CalloutAdapter.ViewHolder>() {
    private var clickListener: ItemClickListener? = null

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

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
                calloutText.text = data[position].label
                calloutImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        ctx,
                        data[position].resId
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getCallout(position: Int): String {
        return data[position].callout
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        clickListener = itemClickListener
    }
}
