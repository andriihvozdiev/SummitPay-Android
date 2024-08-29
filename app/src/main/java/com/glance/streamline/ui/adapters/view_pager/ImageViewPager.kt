package com.glance.streamline.ui.adapters.view_pager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.glance.streamline.R
import com.glance.streamline.utils.extensions.android.view.loadImage
import kotlinx.android.synthetic.main.view_pager_image.view.*

class ImageViewPagerAdapter(
    val context: Context,
    private val imageLinksList: ArrayList<String>
) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val currentImageLink = imageLinksList[position]
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.view_pager_image, collection, false) as ViewGroup

        if (currentImageLink.isNotEmpty()) {
            layout.loading_spinner.visibility = View.VISIBLE

            layout.view_pager_image.loadImage(
                imageUrl = currentImageLink,
                progress = layout.loading_spinner
            )
        }

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return imageLinksList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}