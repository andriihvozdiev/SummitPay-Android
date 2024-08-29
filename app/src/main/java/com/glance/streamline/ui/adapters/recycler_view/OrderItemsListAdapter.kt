package com.glance.streamline.ui.adapters.recycler_view

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.glance.streamline.R
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.ui.models.ProductType
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.visible
import kotlinx.android.synthetic.main.list_item_product_order.*
import java.math.BigDecimal

class OrderItemsListAdapter(
    val clickListener: FilteredClickListener,
    val onClick: (BasketItem) -> Unit = {},
    val onMessage: (BasketItem) -> Unit = {},
    val onRemove: (BasketItem) -> Unit = {},
    val onItemsChanged: (totalSize: Int) -> Unit = {}
) : BaseAdapter<OrderItemsListAdapter.BasketItem, BaseAdapter.BaseViewHolder>(arrayListOf()) {

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

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = R.layout.list_item_product_order

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        val pos = holder.adapterPosition
        if (pos != RecyclerView.NO_POSITION) {
            list[pos].swipeOffset = holder.swipe_layout.offset
        }
    }

    inner class NotificationViewHolder(view: View) : BaseViewHolder(view) {

        override fun bind(pos: Int) {
            list[pos].let { item ->
                order_title_text_view.text = ""

                order_title_text_view.text =  when(item){
                    is BasketOption -> {"  *" + item.name + (if (item.itemsCount > 1) " (x${item.itemsCount})" else "")}
                    is BasketProduct -> {""+ item.name + (if (item.itemsCount > 1) " (x${item.itemsCount})" else "")}
                    else -> throw IllegalArgumentException("Unknown type")
                }

                order_price_text_view.text = when (item) {
                    is BasketProduct -> {
                        "£" + item.price
                    }
                    is BasketOption -> {
                        "£" + item.price
                    }
                    else -> throw IllegalArgumentException("Unknown type")
                }

                if (item.message.isNotBlank()) {
                    order_message_text_view.visible()
                    order_message_text_view.text = item.message
                } else order_message_text_view.gone()

                clickListener.setFilteredClickListener(itemView) { onClick(item) }
                clickListener.setFilteredClickListener(order_message_layout) { onMessage(item) }
                clickListener.setFilteredClickListener(order_remove_layout) { onRemove(item) }

                updateSwipeState(item)
            }
        }

        private fun updateSwipeState(item: BasketItem) {
            swipe_layout.offset = item.swipeOffset
        }
    }

    fun updateItemNumber(item: BasketItem, newNumber: Int) {
        val index = list.indexOf(item)
        list[index].itemsCount = newNumber
        list[index].swipeOffset = 0
        notifyItemChanged(index)
    }

    fun updateItemMessage(item: BasketItem, message: String) {
        val index = list.indexOf(item)
        list[index].message = message
        list[index].swipeOffset = 0
        notifyItemChanged(index)
    }

    fun onRemoveButtonClicked(item: BasketItem) {
        /*       if (item.parentId == null) {
                   val itemsToRemove = list.filter { *//*it.parentId == item.id ||*//* it.id == item.id }
            removeAll(itemsToRemove.listIterator())
        } else {*/
        remove(item)
//        }
    }

    fun getTotalPrice(): Float {
        var totalSum = 0f
        list.forEach {
            totalSum += when (it) {
                is BasketProduct -> {
                    BigDecimal(it.price).multiply(BigDecimal(it.itemsCount)).toFloat()
                }
                is BasketOption -> {
                    BigDecimal(it.price).multiply(BigDecimal(it.itemsCount)).toFloat()
                }
                else -> throw IllegalArgumentException("Unknown type")
            }
        }
        return totalSum
    }

    fun addOrderItem(order: BasketItem, customProductMultiplier: Int) {

//        list.find { it.id == order.id }?.let {
//            val index = list.indexOf(it)
//            it.setQuantity(customProductMultiplier, order is BasketOption /*order.parentId != null*/)
//            it.swipeOffset = 0
//            notifyItemChanged(index)
//        } ?: let {

            when(order){
                is BasketOption -> {
                    list.findLast { order.parentProductId == it.id }?.let {
                        val index = list.indexOf(it) + 1
                        add(order, index)
                    } ?: let {
                        order.setQuantity(customProductMultiplier, false)
                        add(order)
                        recyclerView?.layoutManager?.scrollToPosition(itemCount - 1)
                    }
                }
                is BasketProduct -> {
                    list.findLast {order.productId == it.id}?.let {
                        it.setQuantity(customProductMultiplier, true)
                        val index = list.indexOf(it)
                        update(it, index)
                    } ?: let {
                        order.setQuantity(customProductMultiplier, false)
                        add(order)
                        recyclerView?.layoutManager?.scrollToPosition(itemCount - 1)
                    }
                }
                else -> throw IllegalArgumentException("Unexpected type")
            }
//        }
    }

    private fun ProductLayout.ProductButton.setQuantity(
        customProductMultiplier: Int,
        shouldIncrement: Boolean
    ) {
        if (customProductMultiplier > 0)
            itemsCount = customProductMultiplier
        else if (shouldIncrement)
            itemsCount++
    }

    private fun BasketItem.setQuantity(
        customProductMultiplier: Int,
        shouldIncrement: Boolean
    ) {
        if (shouldIncrement) {
            if (customProductMultiplier > 0) {
                itemsCount += customProductMultiplier
            } else {
                itemsCount++
            }
        } else if (customProductMultiplier > 0)
            itemsCount = customProductMultiplier

    }

    fun setOrderItems(items: ArrayList<BasketItem>) {
        list = items
        notifyDataSetChanged()
    }

    fun onRemovingSpecificNumber(orderItem: BasketItem, itemsNumberToDelete: Int) {
        if (itemsNumberToDelete == 0) onRemoveButtonClicked(orderItem)
        else updateItemNumber(orderItem, orderItem.itemsCount - itemsNumberToDelete)
    }

    data class BasketProduct(
        val productId: String,
        val name: String = "Unknown Product",
        val price: String = "0.0",
        val isRestricted: Boolean = false,
        var productOptionsList: ArrayList<BasketOption> = ArrayList()
    ) : BasketItem(productId,ProductLayout.generateProductTypeByName(name)) {
        constructor(button: ProductButtonInfo) : this(
            button.product_id,
            button.product_name,
            button.product_retailPrice,
            button.product_category.isRestricted
        )

        constructor(productItem: ProductLayout.ProductButton.ProductItem) : this(
            productItem.id,
            productItem.name,
            productItem.retailPrice,
            productItem.category.isRestricted
        )
    }

    data class BasketOption(
        val parentProductId: String,
        val name: String = "Unknown Option",
        val price: String = "0.0",
        val optionId: String
    ) : BasketItem(optionId, ProductLayout.generateProductTypeByName(name)) {
        constructor(productOption: ProductLayout.ProductButton.ProductItem.ProductGroup.ProductOption) : this(
            productOption.parentProductId,
            productOption.name,
            productOption.price,
            productOption.id
        )
    }

    abstract class BasketItem(val id: String, val productType: ProductType) {
        var swipeOffset = 0
        var itemsCount = 1
        var message: String = ""
    }
}
