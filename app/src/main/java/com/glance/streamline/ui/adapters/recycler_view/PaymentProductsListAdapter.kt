package com.glance.streamline.ui.adapters.recycler_view

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.glance.streamline.R
import com.glance.streamline.domain.model.payment.PaymentHistoryModel.ProductModel
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.recycler_view.touch_helper.ViewHolderTouchHelperListener
import kotlinx.android.synthetic.main.list_item_payment.*
import kotlinx.android.synthetic.main.list_item_payment_product.*

class PaymentProductsListAdapter(
    val clickListener: FilteredClickListener,
    val onItemsChanged: (totalSize: Int) -> Unit = {},
) : BaseListAdapter<ProductModel, BaseListAdapter.BaseViewHolder>(PRODUCT_COMPARATOR) {

    private val dataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            updateEmptyState()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            updateEmptyState()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            updateEmptyState()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            updateEmptyState()
        }

        private fun updateEmptyState() {
            onItemsChanged(itemCount)
        }
    }

    init {
        registerAdapterDataObserver(dataObserver)
    }

    fun setProductItems(items: List<ProductModel>) {
        submitList(items)
        notifyDataSetChanged()
    }

    fun clear() {
        submitList(listOf())
        notifyDataSetChanged()
    }

    companion object {
        private val PRODUCT_COMPARATOR = object : DiffUtil.ItemCallback<ProductModel>() {
            override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
                return newItem == oldItem
            }
        }

    }

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        val holder = ViewHolder(view)
        return holder
    }

    inner class ViewHolder(view: View) : BaseViewHolder(view), ViewHolderTouchHelperListener {
        override fun bind(pos: Int) {
            getItem(pos)?.let { product ->

                txt_product_name?.text = product.productName
                txt_product_tax?.text = "Tax - £" + product.productTax
                txt_product_price?.text = "£" + product.productRetailPrice

                if (product.isRefunded) {
                    val greyColor = view.context.getColorRes(R.color.colorGray)
                    txt_product_name?.setTextColor(greyColor)
                    txt_product_tax?.setTextColor(greyColor)
                    txt_product_price?.setTextColor(greyColor)
                } else {
                    val whiteColor = view.context.getColorRes(R.color.colorWhite)
                    txt_product_name?.setTextColor(whiteColor)
                    txt_product_tax?.setTextColor(whiteColor)
                    txt_product_price?.setTextColor(whiteColor)
                }

                clickListener.setFilteredClickListener(itemView, 100) {
                    val item = getItem(adapterPosition)
//                    onClick(item)
                }

            }
        }

        override fun onItemSelected() {
            order_view?.background = view.context.getDrawable(R.drawable.bg_payment_list_item_selected)
        }

        override fun onItemClear() {
            order_view?.background = view.context.getDrawable(R.drawable.bg_payment_list_item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_payment_product
    }
}
