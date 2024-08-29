package com.glance.streamline.ui.adapters.spinner

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.TextView
import com.glance.streamline.R
import android.view.LayoutInflater
import com.glance.streamline.ui.listeners.FilteredClickListener
import com.glance.streamline.ui.models.TableModel


class SpinnerTablesAdapter(
    context: Activity,
    val list: ArrayList<TableModel>
) : ArrayAdapter<TableModel>(context, R.layout.spinner_item_table, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            v = inflater.inflate(R.layout.spinner_item_table, null, false)
        }
        else v = convertView
        (v.findViewById(R.id.order_price_text_view) as? TextView)?.let {
            it.text = getItem(position).name
        }
        return v
    }

    override fun getItem(position: Int): TableModel {
        return list[position]
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            v = inflater.inflate(R.layout.spinner_item_table, null, false)
        }
        else v = convertView
        (v.findViewById(R.id.order_price_text_view) as? TextView)?.let {
            it.text = getItem(position).name
        }
        return v
    }
}