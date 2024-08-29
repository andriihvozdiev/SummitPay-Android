package com.glance.streamline.ui.adapters.recycler_view

import android.view.View
import android.view.ViewGroup
import com.glance.streamline.R
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.recycler_view.RecyclerViewDragListener
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import kotlinx.android.synthetic.main.list_item_product_option.*


class ProductOptionsListAdapter(
    options: ArrayList<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>,
    private val clickListener: FilteredClickListener,
    private val onClick: (View, ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption) -> Unit = { _, _ -> }
) : BaseAdapter<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption, ProductOptionsListAdapter.NotificationViewHolder>(options) {

    override fun getItemViewType(position: Int): Int = R.layout.list_item_product_option

    override fun createViewHolder(view: View, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(view)
    }

    inner class NotificationViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(pos: Int) {
            val item = list[pos]

            if (list.isNullOrEmpty().not()) {
                products_options_arrow.visible()
                itemView.layoutParams.width = if (itemView.minimumWidth == 0) ViewGroup.LayoutParams.WRAP_CONTENT  else itemView.minimumWidth
            }
            else {
                products_options_arrow.gone()
            }

            //products_list_root_layout.setCardBackgroundColor(list[pos].color)
            products_name_text_view.text = item.name
//            products_name_text_view.setTextColor(
//                itemView.context.getColorRes(
//                    if (item.color.isDarkColor()) R.color.colorWhite
//                    else R.color.colorPrimary
//                )
//            )

            clickListener.setFilteredClickListener(itemView, 100) {
                onClick(it, item)
            }
            products_list_root_layout.tag = pos
            //products_list_root_layout.setOnLongClickListener(getLongClickListener())
            products_list_root_layout.setOnDragListener(RecyclerViewDragListener<ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption>())
        }
    }
}
