package net.mcbay.transmat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import net.mcbay.transmat.databinding.BuiltinItemBinding

class BuiltinAdapter internal constructor(
    private val context: Context?,
    private val data: Array<String>
) :
    RecyclerView.Adapter<BuiltinAdapter.ViewHolder>() {
    private var clickListener: AdapterClickListener? = null

    inner class ViewHolder(val binding: BuiltinItemBinding) :
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
        val binding = BuiltinItemBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        context?.let {
            with(holder.binding.builtinImage) {
                setBackgroundColor(0)
                context?.let { ctx ->
                    setImageDrawable(
                        AppCompatResources.getDrawable(
                            ctx,
                            ctx.resources.getIdentifier(
                                data[position],
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

    fun setClickListener(itemClickListener: AdapterClickListener?) {
        clickListener = itemClickListener
    }
}
