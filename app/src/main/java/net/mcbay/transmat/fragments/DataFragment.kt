package net.mcbay.transmat.fragments

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import net.mcbay.transmat.MainActivity
import net.mcbay.transmat.TransmatApplication

open class DataFragment : Fragment() {
    open fun updateView(scrollToEnd: Boolean = false) {

    }

    fun onDatabaseUpdate() {
        (TransmatApplication.INSTANCE.getCurrentActivity() as MainActivity).onDatabaseUpdate()
    }

    override fun onResume() {
        super.onResume()
        (TransmatApplication.INSTANCE.getCurrentActivity() as MainActivity).hideSystemUI()
    }

    fun initEditText(editText: EditText, onComplete: () -> Unit) {
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_NEXT,
                EditorInfo.IME_ACTION_PREVIOUS -> {
                    val imm: InputMethodManager = v.context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    (activity as MainActivity).hideSystemUI()
                    onComplete()
                    return@OnEditorActionListener true
                }
            }
            false
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                onComplete()
            }
        }
    }
}
