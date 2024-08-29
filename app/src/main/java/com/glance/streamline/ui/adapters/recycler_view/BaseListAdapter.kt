package com.glance.streamline.ui.adapters.recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.extensions.LayoutContainer

abstract class BaseListAdapter<T, H : BaseListAdapter.BaseViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, H>(diffCallback) {

    var compositeDisposable = CompositeDisposable()
    protected var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.clear()
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

    abstract class BaseViewHolder(val view: View) : RecyclerView.ViewHolder(view), LayoutContainer {
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
