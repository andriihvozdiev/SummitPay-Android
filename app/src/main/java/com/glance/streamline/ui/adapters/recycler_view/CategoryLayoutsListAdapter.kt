package com.glance.streamline.ui.adapters.recycler_view

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.glance.streamline.R
import com.glance.streamline.data.entities.CategoryInfo
import com.glance.streamline.domain.model.ProductLayout
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.android.isDarkColor
import com.glance.streamline.utils.extensions.android.view.gone
import com.glance.streamline.utils.extensions.android.view.setMarginsDp
import com.glance.streamline.utils.extensions.android.view.visible
import com.glance.streamline.utils.recycler_view.touch_helper.AdapterTouchHelperListener
import com.glance.streamline.utils.recycler_view.touch_helper.ItemTouchHelperCallback
import com.glance.streamline.utils.recycler_view.touch_helper.ViewHolderTouchHelperListener
import kotlinx.android.synthetic.main.list_item_product_category.*
import java.util.*

private const val MARGIN_CELL = 2
class CategoryLayoutsListAdapter(
    layouts: List<CategoryInfo>,
    val clickListener: FilteredClickListener,
    val adapterType: AdapterType = AdapterType.FULL_SIZE,
    val onClick: (CategoryInfo) -> Unit = {}
) : BaseListAdapter<CategoryInfo, BaseListAdapter.BaseViewHolder>(CHAT_COMPARATOR),
    AdapterTouchHelperListener {

    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback())

    init {
        submitList(layouts)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    enum class AdapterType {
        FULL_SIZE, HALF_SIZE, GRID_SIZE;
    }

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        val holder = ViewHolder(view)
        setUpItemLayouts(holder.view)
        return holder
    }

    override fun getItemViewType(position: Int): Int = R.layout.list_item_product_category

    var selectedCategoryId: String? = null

    inner class ViewHolder(view: View) : BaseViewHolder(view), ViewHolderTouchHelperListener {
        override fun bind(pos: Int) {
            getItem(pos)?.let {
                when (adapterType) {
                    AdapterType.HALF_SIZE -> {
                        itemView.setBackgroundResource(R.drawable.bg_rounded_top_right_bottom_right_8px_selectable)
                        category_name_text_view?.gone()
                    }
                    else -> {
                        if (adapterType == AdapterType.GRID_SIZE && it.isSelected) category_selectable_layout?.visible()
                        else category_selectable_layout?.gone()
                        itemView.setBackgroundResource(R.drawable.bg_rounded_8px_gray_selectable)
                        category_name_text_view?.visible()
                    }
                }

                view.setMarginsDp(MARGIN_CELL, MARGIN_CELL, MARGIN_CELL, MARGIN_CELL)

                category_name_text_view?.text = it.name
                category_name_text_view?.setTextColor(
                    itemView.context.getColorRes(
                        if (it.color.toColorInt().isDarkColor()) R.color.colorWhite
                        else R.color.colorPrimary
                    )
                )
                ViewCompat.setBackgroundTintList(itemView, ColorStateList.valueOf(it.color.toColorInt()))
                clickListener.setFilteredClickListener(itemView, 100) {
                    val item = getItem(adapterPosition)
                    if (adapterType == AdapterType.GRID_SIZE) {
                        item.isSelected = !item.isSelected
                        notifyItemChanged(adapterPosition)
                        onClick(item)
                    } else if (selectedCategoryId != item.id) {
                        selectedCategoryId = item.id
                        onClick(item)
                    }
                }
                setItemDragListener()
            }
        }

        private fun setItemDragListener() {
            itemView.setOnLongClickListener {
                itemTouchHelper.startDrag(this)
                true
            }
        }

        override fun onItemSelected() {
            if (adapterType == AdapterType.HALF_SIZE)
                category_border_view?.visible()
        }

        override fun onItemClear() {
            if (adapterType == AdapterType.HALF_SIZE)
                category_border_view?.gone()
        }
    }

    private fun setUpItemLayouts(view: View) {
        view.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val lp: ViewGroup.LayoutParams = view.layoutParams
                lp.width = when(adapterType) {
                    AdapterType.FULL_SIZE -> lp.height
                    AdapterType.HALF_SIZE -> lp.height / 2
                    AdapterType.GRID_SIZE -> MATCH_PARENT
                }
                recyclerView?.layoutManager?.requestLayout()
                view.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    override fun onItemMove(startPosition: Int, endPosition: Int) {
        val swappedList = currentList.toMutableList()
        Collections.swap(swappedList, startPosition, endPosition)
        submitList(swappedList)
    }

    companion object {
        private val CHAT_COMPARATOR = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return newItem == oldItem && newItem.isSelected == oldItem.isSelected
            }
        }

    }
}
