package com.glance.streamline.ui.adapters.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.extensions.LayoutContainer

abstract class BaseAdapter<T, H : BaseAdapter.BaseViewHolder>(
    protected var list: ArrayList<T>
) : RecyclerView.Adapter<H>() {

    var compositeDisposable = CompositeDisposable()
    protected var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, @LayoutRes viewType: Int): H {
        val view = inflate(parent, viewType)
        return createViewHolder(view, viewType)
    }

    internal abstract fun createViewHolder(view: View, @LayoutRes viewType: Int): H

    @LayoutRes
    abstract override fun getItemViewType(position: Int): Int

    override fun onBindViewHolder(holder: H, position: Int) = holder.bind(position)
    override fun onBindViewHolder(holder: H, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) holder.bind(position)
        else holder.bind(position, payloads)
    }

    private fun post(action: () -> Unit) = recyclerView?.post(action)

    fun removeAll(removeIterator: Iterator<T>) {
        while (removeIterator.hasNext()) {
            val item = removeIterator.next()
            if (list.contains(item))
                remove(item)
        }
    }

    open fun addAll(addIterator: Iterator<T>) {
        while (addIterator.hasNext()) {
            val item = addIterator.next()
            if (!list.contains(item))
                add(item)
        }
    }

    fun addAll(elements: ArrayList<T>) {
        val startPosition = list.size
        list.addAll(elements)
        notifyItemRangeInserted(startPosition, elements.size)
    }

    fun addAllInStart(elements: ArrayList<T>) {
        val startPosition = 0
        list.addAll(startPosition, elements)
        notifyItemRangeInserted(startPosition, elements.size)
    }

    fun getItemsList() = list

    fun add(t: T) {
        add(t, list.size)
    }

    fun add(t: T, pos: Int) {
        list.add(pos, t)
        notifyItemInserted(pos)
    }

    fun update(t: T, pos: Int) {
        list.set(pos, t)
        notifyItemChanged(pos)
    }

    private fun notifyByItem(t: T) {
        val index = list.indexOf(t)
        if (index >= 0)
            notifyItemInserted(index)
    }

    fun remove(t: T) {
        val index = list.indexOf(t)
        list.remove(t)
        if (index >= 0)
            notifyItemRemoved(index)
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.clear()
    }

    abstract class BaseViewHolder constructor(val view: View) :
        RecyclerView.ViewHolder(view), LayoutContainer {

        override val containerView: View?
            get() = itemView

        abstract fun bind(pos: Int)
        open fun bind(pos: Int, payloads: MutableList<Any>) {}
    }

    companion object {
        fun inflate(parent: ViewGroup, @LayoutRes res: Int): View {
            return LayoutInflater.from(parent.context).inflate(res, parent, false)
        }
    }
}
