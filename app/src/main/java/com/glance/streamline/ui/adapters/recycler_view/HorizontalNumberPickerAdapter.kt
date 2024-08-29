package com.glance.streamline.ui.adapters.recycler_view

import android.view.View
import com.glance.streamline.R
import com.glance.streamline.ui.listeners.FilteredClickListener
import kotlinx.android.synthetic.main.list_item_horizontal_number_picker.*

class HorizontalNumberPickerAdapter(
    itemsNumber: Int,
    val clickListener: FilteredClickListener,
    val onClick: (String) -> Unit = {}
) : BaseAdapter<String, BaseAdapter.BaseViewHolder>(convertToAdapterModel(itemsNumber)) {

    fun setItems(itemsNumber: Int) {
        clear()
        addAll(convertToAdapterModel(itemsNumber))
    }

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = R.layout.list_item_horizontal_number_picker

    inner class ViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(pos: Int) {
            number_picker_title_text_view.text = list[pos]
            clickListener.setFilteredClickListener(itemView) {
                onClick(list[adapterPosition])
            }
        }

    }

    companion object {
        fun convertToAdapterModel(itemsCount: Int): ArrayList<String> {
            val list = (1..itemsCount).flatMap { listOf(it.toString()) } as ArrayList
            list.add(0, "All")
            list.add(0, "")
            list.add("")
            return list
        }
    }
}
