package net.ivpn.client.v2.viewmodel

import android.widget.RadioGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import net.ivpn.client.common.dagger.ApplicationScope
import net.ivpn.client.common.prefs.Settings
import net.ivpn.client.v2.serverlist.dialog.Filters
import javax.inject.Inject

@ApplicationScope
class ServerListFilterViewModel @Inject constructor(
        private val settings: Settings
) : ViewModel() {

    var filterListener = RadioGroup.OnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
        onCheckedChanged(checkedId)
    }

    val filter = ObservableField<Filters>()

    private val selectedFilter = ObservableField<Filters>()

    var listeners = ArrayList<OnFilterChangedListener>()
//    var navigator: OnFilterChangedListener? = null

    fun onResume() {
        filter.set(settings.filter)
        selectedFilter.set(settings.filter)
    }

    fun applyMode() {
        filter.set(selectedFilter.get())
        settings.filter = filter.get()
        listeners.forEach { it.onFilterChanged(filter.get()) }
//        for (listener in listeners) {
//            listener.onFilterChanged(filter.get())
//        }
//        navigator?.onFilterChanged(filter.get())
    }

    private fun onCheckedChanged(checkedId: Int) {
        val filter = Filters.getById(checkedId)
        if (filter == this.selectedFilter.get()) {
            return
        }

        this.selectedFilter.set(filter)
    }

    interface OnFilterChangedListener {
        fun onFilterChanged(filter: Filters?)
    }
}