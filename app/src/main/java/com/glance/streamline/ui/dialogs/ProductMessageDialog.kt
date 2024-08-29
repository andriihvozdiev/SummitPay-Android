package com.glance.streamline.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.glance.streamline.R
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.OrderItemsListAdapter
import com.glance.streamline.utils.extensions.android.getInflater
import kotlinx.android.synthetic.main.dialog_order_product_message.view.*

class ProductMessageDialog(context: Context) : AlertDialog.Builder(context) {

    var onMessageAdded: (OrderItemsListAdapter.BasketItem, String) -> Unit = { _, _ ->}

    fun show(productModel: OrderItemsListAdapter.BasketItem): AlertDialog {
        val removingOrderItemsDialogView =
            context.getInflater().inflate(R.layout.dialog_order_product_message, null).apply {
                order_message_edit_text.setText(productModel.message)
            }
        setView(removingOrderItemsDialogView)
        setTitle(R.string.order_product_message_dialog_title)
        setMessage(
            context.getString(
                R.string.order_product_message_dialog_message,
                productModel.message
            )
        )
        setPositiveButton(R.string.confirm) { _, _ ->
            val message =
                removingOrderItemsDialogView?.order_message_edit_text?.text?.toString() ?: ""
            onMessageAdded(productModel, message)
        }
        setNegativeButton(R.string.logout_dialog_cancel) { _, _ -> }
        create()
        return show()
    }
}