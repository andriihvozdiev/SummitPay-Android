package com.glance.streamline.utils.recycler_view

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glance.streamline.ui.adapters.recycler_view.BaseAdapter

class RecyclerViewDragListener<T>(var selectedDrawable: Drawable? = null, val onDrop: (v: View, event: DragEvent) -> Unit = {v: View, event -> }): View.OnDragListener {
    private var isDropped = false
    var tmpBackgroundDrawable: Drawable? = null

    override fun onDrag(v: View, event: DragEvent): Boolean {
        when (event.action) {
            // signal for the start of a drag and drop operation
            DragEvent.ACTION_DRAG_STARTED -> {
                tmpBackgroundDrawable = v.background
            }
            // the drag point has entered the bounding box of the View
            DragEvent.ACTION_DRAG_ENTERED -> {
                selectedDrawable?.let {
                    v.background = it
                }
            }
            // the user has moved the drag shadow outside the bounding box of the View
            DragEvent.ACTION_DRAG_EXITED -> {
                v.background = tmpBackgroundDrawable
            }
            // the drag and drop operation has concluded
            DragEvent.ACTION_DRAG_ENDED -> {
                v.background = tmpBackgroundDrawable
            }
            DragEvent.ACTION_DROP -> {
                onDrop(v, event)
                isDropped = true
                val viewSource = event.localState as? View
                val sourceRecyclerView = viewSource?.parent as? RecyclerView
                val targetRecyclerView = v.parent as? RecyclerView

                if (sourceRecyclerView != null && targetRecyclerView != null) {
                    val sourceItemPosition =
                        sourceRecyclerView.findContainingViewHolder(viewSource)
                            ?.adapterPosition
                    val targetItemPosition =
                        targetRecyclerView.findContainingViewHolder(v)?.adapterPosition

                    if (sourceItemPosition != null && targetItemPosition != null
                        && sourceItemPosition != RecyclerView.NO_POSITION
                        && targetItemPosition != RecyclerView.NO_POSITION
                        && !(sourceRecyclerView == targetRecyclerView && sourceItemPosition == targetItemPosition)
                    ) {
                        val sourceAdapter = sourceRecyclerView.adapter as? BaseAdapter<T, *>
                        val targetAdapter = targetRecyclerView.adapter as? BaseAdapter<T, *>

                        if (sourceAdapter != null && targetAdapter != null) {
                            val sourceItemsList = sourceAdapter.getItemsList()
                            val sourceListItem = sourceItemsList[sourceItemPosition]
                            sourceItemsList.removeAt(sourceItemPosition)
                            sourceAdapter.notifyItemRemoved(sourceItemPosition)

                            val targetItemsList = targetAdapter.getItemsList()
                            targetItemsList.add(targetItemPosition, sourceListItem)
                            targetAdapter.notifyItemInserted(targetItemPosition)

                            targetRecyclerView.apply {
                                post {
                                    (layoutManager as? LinearLayoutManager)?.let {
                                        if (targetItemPosition < it.findFirstVisibleItemPosition()) {
                                            smoothScrollToPosition(targetItemPosition)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isDropped && event.localState != null) {
            (event.localState as View).visibility = View.VISIBLE
        }
        return true
    }
}