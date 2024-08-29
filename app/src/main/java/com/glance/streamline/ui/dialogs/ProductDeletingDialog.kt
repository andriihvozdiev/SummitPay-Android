package com.glance.streamline.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.glance.streamline.R
import com.glance.streamline.ui.adapters.recycler_view.HorizontalNumberPickerAdapter
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.getInflater
import kotlinx.android.synthetic.main.dialog_remove_order_item.view.*

class ProductDeletingDialog(
    context: Context,
    private val filteredClickListener: FilteredClickListener
) : AlertDialog.Builder(context) {

    var onRemove: (itemsToRemove: Int) -> Unit = {}

    fun show(totalItemsQuantity: Int): AlertDialog {
        val removingOrderItemsDialogView =
            context.getInflater().inflate(R.layout.dialog_remove_order_item, null).apply {
                LinearSnapHelper().attachToRecyclerView(order_item_number_picker)
                order_item_number_picker.adapter =
                    HorizontalNumberPickerAdapter(totalItemsQuantity, filteredClickListener)
            }
        setView(removingOrderItemsDialogView)
        setTitle(R.string.remove_order_items_dialog_title)
        setMessage(R.string.remove_order_items_dialog_message)
        setPositiveButton(R.string.confirm) { _, _ ->
            removingOrderItemsDialogView.order_item_number_picker?.apply {
                val firstVisibleItemIndex =
                    (layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                        ?: 0
                onRemove(firstVisibleItemIndex)
            }
        }
        setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
        create()
        return show()
    }
}