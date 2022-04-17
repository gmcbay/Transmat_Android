package net.mcbay.transmat

import android.view.View
import androidx.annotation.MainThread
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter

class CalloutItemViewModel : BaseObservable() {
    companion object {
        private lateinit var instance: CalloutItemViewModel

        @MainThread
        fun getInstance(): CalloutItemViewModel {
            instance = if (::instance.isInitialized) instance else CalloutItemViewModel()
            return instance
        }
    }

    private var dynamicHeight = 0
    private var connected = false

    @Bindable
    fun getDynamicHeight(): Int {
        return dynamicHeight
    }

    @Bindable
    fun setDynamicHeight(dynamicHeight: Int) {
        this.dynamicHeight = dynamicHeight
        notifyPropertyChanged(BR.dynamicHeight)
    }

    @Bindable
    fun getConnected(): Boolean {
        return connected
    }

    @Bindable
    fun setConnected(connected: Boolean) {
        this.connected = connected
        notifyPropertyChanged(BR.connected)
    }
}

@BindingAdapter("item_layout_height")
fun setDynamicHeight(view: View, height: Int) {
    view.layoutParams = view.layoutParams.apply {
        if (height > 0) {
            this.height = height
        } else {
            this.height = CalloutItemViewModel.getInstance().getDynamicHeight()
        }
    }
}
