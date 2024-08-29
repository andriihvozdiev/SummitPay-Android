package com.glance.streamline.ui.adapters.recycler_view

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.DiffUtil
import com.glance.streamline.R
import com.glance.streamline.data.entities.PaymentHistoryInfo
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.utils.extensions.android.getColorRes
import com.glance.streamline.utils.extensions.parseRFC3339Nano
import kotlinx.android.synthetic.main.list_item_payment.*
import java.text.SimpleDateFormat
import java.util.*

class PaymentsListAdapter(
    paymentHistories: List<PaymentHistoryInfo>,
    val clickListener: FilteredClickListener,
    val onClick: (PaymentHistoryInfo) -> Unit = {},
) : BaseListAdapter<PaymentHistoryInfo, BaseListAdapter.BaseViewHolder>(ORDER_COMPARATOR) {

    private var mSelectedItem: Int = -1;

    init {
        submitList(paymentHistories)
    }

    override fun createViewHolder(view: View, viewType: Int): BaseViewHolder {
        val holder = ViewHolder(view)
        setUpItemLayouts(holder.view)
        return holder
    }

    override fun getItemViewType(position: Int): Int = R.layout.list_item_payment

    inner class ViewHolder(view: View) : BaseViewHolder(view) {
        override fun bind(pos: Int) {
            getItem(pos)?.let { paymentHistory ->

                txt_order_no?.text = String.format("#%04d", paymentHistory.orderNumber)
                val strDate = SimpleDateFormat("HH:mm dd/MM/yy", Locale.ENGLISH).format(paymentHistory.paymentDate)
                txt_order_date?.text = strDate
                txt_order_name?.text = paymentHistory.userName
                txt_total_price.text = "£" + paymentHistory.totalAmount

                if (paymentHistory.isRefunded) {
                    val greyColor = view.context.getColorRes(R.color.colorGray)
                    txt_order_no.setTextColor(greyColor)
                    txt_order_date.setTextColor(greyColor)
                    txt_order_name.setTextColor(greyColor)
                    txt_total_price.setTextColor(greyColor)
                } else {
                    val whiteColor = view.context.getColorRes(R.color.colorWhite)
                    txt_order_no.setTextColor(whiteColor)
                    txt_order_date.setTextColor(whiteColor)
                    txt_order_name.setTextColor(whiteColor)
                    txt_total_price.setTextColor(whiteColor)
                }

                if (pos == mSelectedItem) {
                    order_view?.background = view.context.getDrawable(R.drawable.bg_payment_list_item_selected)
                } else {
                    order_view?.background = view.context.getDrawable(R.drawable.bg_payment_list_item)
                }

                clickListener.setFilteredClickListener(itemView, 100) {
                    val item = getItem(adapterPosition)
                    mSelectedItem = pos
                    onClick(item)
                    notifyDataSetChanged()
                }

            }
        }
    }

    fun onClearSelected() {
        mSelectedItem = -1
        notifyDataSetChanged()
    }

    fun clear() {
        submitList(listOf())
    }

    private fun setUpItemLayouts(view: View) {
        view.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val lp: ViewGroup.LayoutParams = view.layoutParams
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                recyclerView?.layoutManager?.requestLayout()
                view.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    companion object {
        private val ORDER_COMPARATOR = object : DiffUtil.ItemCallback<PaymentHistoryInfo>() {
            override fun areItemsTheSame(oldItem: PaymentHistoryInfo, newItem: PaymentHistoryInfo): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: PaymentHistoryInfo, newItem: PaymentHistoryInfo): Boolean {
                return newItem == oldItem
            }
        }

    }
}
