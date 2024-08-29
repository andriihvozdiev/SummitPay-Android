package com.glance.streamline.ui.adapters.recycler_view

import android.view.View
import com.glance.streamline.R
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.ui.models.DemoScreenModel
import kotlinx.android.synthetic.main.list_item_demo_screens.*

class DemoScreensAdapter(
    screens: ArrayList<DemoScreenModel>,
    val clickListener: FilteredClickListener,
    val onClick: (Int, Any?) -> Unit
) : BaseAdapter<DemoScreenModel, BaseAdapter.BaseViewHolder>(screens) {

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = R.layout.list_item_demo_screens

    inner class NotificationViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(pos: Int) {
            screen_title_text_view.text = list[pos].title
            clickListener.setFilteredClickListener(screen_title_text_view) {
                onClick(list[pos].fragmentId, list[pos].navArgs)
            }
        }

    }

}
