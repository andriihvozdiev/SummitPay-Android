package com.glance.streamline.utils.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.glance.streamline.R
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.view.gone
import kotlinx.android.synthetic.main.layout_quick_action_button.view.*


class QuickActionButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    var selectedActionMenuItem: MenuItem? = null
    var clickListener: FilteredClickListener? = null
        set(value) {
            field = value
            field?.setFilteredClickListener(quick_action_button) {
                selectedActionMenuItem?.let(onClick) ?: showQuickActionPopupMenu()
            }
        }
    var onClick: (MenuItem) -> Unit = {}

    init {
        inflate(context, R.layout.layout_quick_action_button, this)
        quick_action_button.setOnLongClickListener {
            showQuickActionPopupMenu()
            true
        }
    }

    private fun showQuickActionPopupMenu() {
        PopupMenu(context, this).apply {
            menuInflater.inflate(R.menu.menu_navigation, menu)
            menu.findItem(R.id.menu_checkout).isEnabled = false
            menu.findItem(selectedActionMenuItem?.itemId ?: -1)?.isChecked = true
            setOnMenuItemClickListener {
                selectedActionMenuItem = it
                quick_action_image_view.gone()
                quick_action_button.text = it.title
                true
            }
            show()
        }
    }

}
