package com.glance.streamline.utils.recycler_view.touch_helper

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class ItemTouchHelperCallback() : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int { // Set movement flags based on the layout manager
        return if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags =
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            makeMovementFlags(
                dragFlags,
                swipeFlags
            )
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            makeMovementFlags(
                dragFlags,
                swipeFlags
            )
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (source.itemViewType != target.itemViewType) {
            return false
        }
        // Notify the adapter of the move
        (recyclerView.adapter as? AdapterTouchHelperListener)?.onItemMove(
            source.adapterPosition,
            target.adapterPosition
        )
        return true
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) { // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is ViewHolderTouchHelperListener) { // Let the view holder know that this item is being moved or dragged
                viewHolder.onItemSelected()
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is ViewHolderTouchHelperListener) { // Tell the view holder it's time to restore the idle state
            viewHolder.onItemClear()
        }
    }

}