package com.glance.streamline.ui.adapters.recycler_view

import android.content.ClipData
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.glance.streamline.R
import com.glance.streamline.data.entities.ProductButtonInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.isDarkColor
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.setMarginsDp
import com.glance.streamline.utils.extensions.android.view.visible
import com.glance.streamline.utils.recycler_view.RecyclerViewDragListener
import kotlinx.android.synthetic.main.list_item_product.*

private const val MARGIN_CELL = 4
const val WEIGHT_CELL = 30

open class ProductsListAdapter(
    buttons: ArrayList<ProductButtonInfo>,
    val clickListener: FilteredClickListener,
    val onClick: (View, ProductButtonInfo) -> Unit = { _, _ -> }
) : BaseAdapter<ProductButtonInfo, BaseAdapter.BaseViewHolder>(buttons) {
    private val emptyButton = ProductButtonInfo(id = "empty button")

    init {
        if (buttons.size == 1) { //TODO: this is  fix for one cell bug
            buttons.add(emptyButton)
        }else{
            buttons.filterNot { it.id == emptyButton.id }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): BaseViewHolder {
        val view = inflate(parent, R.layout.list_item_product)
        return createViewHolder(view, viewType)
    }

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        val holder = NotificationViewHolder(view)
        holder.itemView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return holder
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        R.layout.list_item_product //list[position].popularity

    inner class NotificationViewHolder(view: View) : BaseViewHolder(view) {
        override fun bind(pos: Int) {
            val item = list[pos]

            if (item.id == emptyButton.id) return

            view.setMarginsDp(MARGIN_CELL, 0, MARGIN_CELL, MARGIN_CELL)

            if (item.product_groups?.any { it.options.isNotEmpty() } == true) products_options_arrow.visible()
            else products_options_arrow.gone()

            if (!item.product_category?.color.isNullOrEmpty()) {
                products_list_root_layout?.setCardBackgroundColor(Color.parseColor(item.product_category?.color!!))
                itemView.setBackgroundColor(Color.parseColor(item.product_category?.color!!))
            }

            products_name_text_view.text = item.product_name
            products_name_text_view.setTextColor(
                itemView.context.getColorRes(
                    if (item.product_category.color.isNotEmpty() && Color.parseColor(item.product_category?.color!!).isDarkColor()) R.color.colorWhite
                    else R.color.colorPrimary
                )
            )
            clickListener.setFilteredClickListener(itemView, 100) {
                onClick(it, item)
            }
            with(products_list_root_layout) {
                tag = pos
                setOnLongClickListener(getLongClickListener())
                setOnDragListener(RecyclerViewDragListener<ProductButtonInfo>(
                    selectedDrawable = context.resources.getDrawable(
                        R.drawable.bg_dashed_border,
                        Resources.getSystem().newTheme()
                    ),
                    onDrop = { v: View, event ->

                    }
                )
                )
            }
        }
    }

    protected fun getLongClickListener() = View.OnLongClickListener { v: View ->
        initDragAndDrop(v)
        true
    }

    protected fun initDragAndDrop(v: View) {
        val data = ClipData.newPlainText("", "")
        val shadowBuilder = View.DragShadowBuilder(v)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v.startDragAndDrop(data, shadowBuilder, v, 0)
        } else {
            v.startDrag(data, shadowBuilder, v, 0)
        }
    }

    fun setProductsList(products: ArrayList<ProductButtonInfo>) {
        clear()
        addAll(products)
    }
}

