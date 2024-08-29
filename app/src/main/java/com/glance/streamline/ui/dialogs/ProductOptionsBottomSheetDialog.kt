package com.glance.streamline.ui.dialogs

import android.app.Activity
import androidx.recyclerview.widget.GridLayoutManager
import com.glance.streamline.R
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.adapters.recycler_view.OrderItemsListAdapter
import com.glance.streamline.ui.adapters.recycler_view.ProductOptionsListAdapter
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.convertDpToPixel
import com.glance.streamline.utils.extensions.android.view.recycler_view.SpacesItemDecoration
import com.glance.streamline.utils.extensions.android.view.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.popup_food_item_layout.*

class ProductOptionsBottomSheetDialog(
    context: Activity,
    private val filteredClickListener: FilteredClickListener
) : BottomSheetDialog(context, R.style.ProductOptionsBottomSheetDialogTheme) {

    var onReopen: (button: ProductButtonInfo) -> Unit = { _ -> }
    var onAddToOrderItemsList: (OrderItemsListAdapter.BasketItem) -> Unit = {}

    fun show(button: ProductButtonInfo) {
        if (button.allProductOptions?.isNullOrEmpty()?.not() == true) {
            setContentView(R.layout.dialog_product_option)
            options_title_text_view?.text = button.product_name
            if (button.allProductOptions.isNullOrEmpty().not()) {
                options_back_text_view.visible()
                filteredClickListener.setFilteredClickListener(options_back_text_view) {
                    //onReopen(OrderItemsListAdapter.BasketProduct(productItemButton = product))
                    dismiss()
                }
            }
            options_list?.apply {
                decorationsSpacing = context.convertDpToPixel(3f).toInt()
                val arrayOptions = button.allProductOptions as? ArrayList<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>

                adapter = ProductOptionsListAdapter(
                    arrayOptions ?: arrayListOf(), filteredClickListener
                ) { view, item ->
                    if (arrayOptions?.isNullOrEmpty()?.not() == true) {
                        onAddToOrderItemsList(OrderItemsListAdapter.BasketOption(item))
//                        onReopen(button)
                    } else {
                        onAddToOrderItemsList(OrderItemsListAdapter.BasketOption(item))
                    }
//
                    dismiss()
                }
                (layoutManager as? GridLayoutManager)?.let {
                    addItemDecoration(SpacesItemDecoration(it.spanCount, decorationsSpacing, false))
                }
            }
            show()
        }
    }
}