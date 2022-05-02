package net.mcbay.transmat

import android.app.Application
import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

// Stored app preferences.  For now just stores an index to the currently active callout page
// (referenced by table id in the database)
class TransmatPrefs(private val ctx: Context) : BaseObservable() {
    companion object {
        const val PREFS_NAME = "TRANSMAT_PREFS"
        const val PREF_SELECTED_PAGE = "SELECTED_PAGE"
        const val UNSELECTED_PAGE_ID = -1L
    }

    private var selectedPageId = UNSELECTED_PAGE_ID

    @Bindable
    fun getSelectedPageId(): Long {
        if (selectedPageId == UNSELECTED_PAGE_ID) {
            val sharedPrefs = ctx.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
            selectedPageId = sharedPrefs.getLong(PREF_SELECTED_PAGE, UNSELECTED_PAGE_ID)

            if (selectedPageId == UNSELECTED_PAGE_ID) {
                selectedPageId = TransmatApplication.DEFAULT_PAGE_ID
            }
        }

        return selectedPageId
    }

    @Bindable
    fun setSelectedPageId(pageId: Long) {
        selectedPageId = pageId
        val sharedPrefsEditor = ctx.getSharedPreferences(
            PREFS_NAME,
            Application.MODE_PRIVATE
        ).edit()
        sharedPrefsEditor.putLong(PREF_SELECTED_PAGE, pageId).apply()
        notifyPropertyChanged(BR.selectedPageId)
    }
}